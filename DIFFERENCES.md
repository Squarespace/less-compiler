## Known Differences and Extensions

The following is a list of known differences between the Squarespace LESS
compiler and less.js 1.7.0.


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

