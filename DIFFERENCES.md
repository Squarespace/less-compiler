## Known Differences and Extensions

The following is a list of known differences between the Squarespace LESS
compiler and less.js 1.7.0.


#### Compatibility levels

The compiler accepts a per-compile compatibility level that selects how
many legacy behaviors ("patches") are fixed: level 0 keeps every legacy
behavior active, preserving the previous release's behavior surface, and
at level N every patch whose threshold is at most N is fixed. The highest
level is the fully-fixed compiler.

    LessOptions opts = new LessOptions();
    opts.compatLevel(0);   // released behavior: no fixes, every legacy behavior active
    opts.compatLevel(1);   // level-1 fixes applied (BUG1..BUG4)
    opts.compatLevel(2);   // fully fixed: every fix applied

    LessContext ctx = new LessContext(opts);
    new LessCompiler().compile(raw, ctx);

Current patches, by threshold:

Threshold 1 - legacy generation 1 (the former safe-mode tolerances, see
docs/legacy-bugs.md; active below level 1):

- `BUG1` - extraneous `'+'` at block scope is tolerated.
- `BUG2` - a `@media` directive without a following block is dropped and the
  statements that follow attach to the enclosing block.
- `BUG3` - a variable reference followed by empty parentheses (`@var();`) is
  accepted.
- `BUG4` - invalid addition expressions such as `random(90) + px` are
  tolerated.

Threshold 2 - legacy generation 2 (gated bug fixes; active below level 2):

- `SELECTOR_COMPLEXITY_OVERFLOW` - selector-combine overflow is swallowed and
  the current selector is dropped instead of failing the compile.
- `IMPORT_URL_INLINE` - `@import url("x.less")` is emitted literally
  instead of being resolved and inlined.
- `NONFINITE_AS_ZERO` - NaN and Infinity values render as `0` instead of
  visible text.
- `MOD_ZERO_STRICT` - `mod(x, 0)` silently returns NaN instead of obeying
  the division contract (strict fails, lenient warns).
- `CONVERT_INCOMPATIBLE_UNITS` - `convert()` to an incompatible unit
  silently emits 0 instead of failing the compile.
- `REPLACE_REGEX_GROUPS` - `replace()` treats `$` and `\` in the
  replacement as regex group references instead of inserting them literally.
- `VARIADIC_NAMED_ARG` - a named arg targeting the variadic parameter fails
  with ARG_NAMED_NOTFOUND instead of binding to it.
- `ARGUMENTS_ORDER` - `@arguments` follows binding insertion order instead
  of parameter declaration order.
- `GUARD_COMPARE_UNCOMPARABLE` - uncomparable guard operands compare as -1,
  so `<`, `<=` and `!=` evaluate true (the released truth table, pinned
  by the compat matrix; at the fixed level only `<` stays true).
- `IMPORT_EXT_CASE` - `@import` extensions match case-sensitively.
- `IMPORT_ONCE_SUPPRESS` - a plain import caching the file first defeats a
  later `@import-once`, which re-inlines the file.
- `COLOR_BLEND_ALPHA` - color-blend results are opaque instead of keeping
  the larger input alpha.
- `COLOR_CHANNEL_PRECISION` - color channel math truncates fractional
  intermediates before the final rounding.
- `ATTR_SELECTOR_UNTERMINATED` - an unterminated attribute selector or
  parenthesized element is silently dropped instead of failing the compile.

At level 0 every legacy behavior is active. At level 1 the threshold-1
patches are fixed. At level 2 every patch is fixed. The fix ladder is
pinned by CompatPatchTest and CompatLevelTest.

Per-site patches can be forced on for stylesheets that need an irregular
combination the ladder cannot express:

    opts.compatPatch(Patch.BUG2);

New fixes ship with a legacy path gated by a new patch at a new higher
threshold, so higher levels apply more fixes without changing the released
surface at level 0. Patches are retired when the last site that needs them
migrates to their threshold level or above.

For backwards compatibility, the parser's boolean `safeMode()` flag is the
recovery-mode flag (see below): `true` = best effort, `false` (default) =
strict. It is independent of the compat level, which is set exclusively via
`LessOptions.compatLevel(int)`.

#### Recovery mode (safe mode)

A second, orthogonal axis to the compat level: **what happens when the
compiler must reject something**. The compat level decides *what* is fixed;
the mode decides *how violations behave*.

- **strict** (default, `safeMode(false)`): any hard error aborts the
  compile with a `LessException` — released behavior.
- **safe** (`safeMode(true)`): best effort. The offending construct is
  dropped at a well-defined boundary, a `WARNING[n] ... raised during
  recovery: ...` comment is emitted, and compilation continues:

  - parser: the stream is resynchronized at well-defined boundaries —
    a well-formed statement that follows the error (including a complete
    top-level block) is re-parsed and kept; only the broken region is
    dropped; an unterminated tail is truncated; input that recovers to nothing reports
    `stylesheet produced no output`;
  - evaluation: a failed block member (rule, mixin call, ...) is dropped
    and the next sibling is evaluated;
  - render: a node that fails to render is skipped;
  - complexity overflow: the combined selector is truncated at the
    limit and the rule body is kept.

  The released surface is level 0 + strict; level 0 + safe behaves
  identically until something must be rejected, then it degrades with
  warnings instead of failing — production keeps rendering while the
  warning stream is the migration ledger. The matrix of (patch x level x
  mode) outcomes is pinned by RecoveryMatrixTest.


#### Non-finite math values render as text at the fully-fixed level

The `NONFINITE_AS_ZERO` patch is fixed at level 2: non-finite math values
(NaN, Infinity) render as visible text there, same as less.js. At the
released levels (below 2) they render as `0`:

    y: sqrt(-1);   /* level 0 (released) -> 0; level 2 (fixed) -> NaN */
    y: pow(0, -1); /* level 0 (released) -> 0; level 2 (fixed) -> Infinity */

Modulo by zero follows the same contract as division: strict mode fails the
compile, lenient mode warns, and the NaN result renders according to the
level (see `MOD_ZERO_STRICT`).


#### Color keywords are allowed to participate in math expressions

    foo: blue + 1;
    foo: blue + red;

#### Alpha opacity can use floating-point values

    foo: alpha(opacity=.7)
    foo: alpha(opacity=0.35)

#### Non-integer numeric values are rounded to at most 8 decimal places

Squarespace LESS renders non-integer numbers with at most 8 decimals
(`Buffer.DEFAULT_PRECISION`), rounding to the nearest value with ties going
to the even neighbor (`RoundingMode.HALF_EVEN`), then strips trailing zeros:

    x: 123.456456456456;  /* -> 123.45645646  (rounded to 8 decimals) */
    x: 0.500000001;       /* -> 0.5           (trailing zeros stripped) */

A leading zero is also dropped for values with magnitude less than 1:

    x: 0.4;               /* -> .4 */
    rgba(0, 0, 0, 0.4);   /* -> rgba(0, 0, 0, .4) */

less.js 1.7.0 instead emits the number in its full JavaScript
double representation, with no default rounding or zero stripping:

    x: 123.456456456456;  /* -> 123.456456456456 */
    x: 0.4;               /* -> 0.4 */

This behavior is tested by the files in the test suite
(`dimension.css`, `css-3.css`).

#### Additional comparison operator variants for guard conditions

Squarespace LESS allows the use of both `>=` and `=>` forms for
greater-or-equal-to, `<=` and `=<` for less-or-equal-to conditions, as well as
`!=` for not-equal condition:

    .mixin-1 (@foo, @bar) when (@foo >= 20px), (@bar => 20px) { }

    .mixin-2 (@foo, @bar) when (@foo <= 20px), (@bar =< 20px) { }

    .mixin-3 (@foo) when (@foo != 2) { }

#### Tolerance of comments in some places

For example, comments between selectors:

    h1 /* a */ span:hover /* b */ {
        color: /* c */ red;
    }

Squarespace LESS:

    h1 span:hover {
        color: /* c */ red;
    }

less.js:

    % lessc-1.7.0 comment.less
    ParseError: Unrecognised input in comment.less on line 24, column 12:
    23
    24 h1 /* a */ span:hover /* b */ {
    25     color: /* c */ red;


#### Block-less `@media` directives are dropped in safe mode (the default)

A `@media` directive that is not followed by a block is silently dropped in
safe mode, and the statements that follow it attach to the enclosing block:

    @media only screen and (max-width: 640px)
    #content {
      padding-top: 50px;
    }

Squarespace LESS (safe mode):

    #content {
      padding-top: 50px;
    }

less.js:

    % lessc-1.7.0 bug2.less
    ParseError: Unrecognised input in bug2.less on line 1, column 1:
    1 @media only screen and (max-width: 640px)
    2 #content {

This leniency is intentional legacy compatibility: production stylesheets
containing unclosed or block-less `@media` directives must keep compiling
(see docs/legacy-bugs.md, BUG2, for the affected sites). It applies only in
safe mode, which is the default for both `LessCompiler.parse()` and
`LessCompiler.compile()`. With safe mode disabled the same input fails the
compile.

