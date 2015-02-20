
2.x Core Checklist
---------

Reorganize the stream to be hierarchical.  Parsing continues until an import
statement is encountered. If it has a non-interpolated path, push the current
stream / filename onto the stack and start parsing the import.  Once we hit
EOF, pop the stream stack and continue parsing.  Need to reconcile this with
import caching / reuse.

Adding !important to definitions, merging accurately in rule:

    @size: 1px !important;
    @color: black !important;
    color: solid @size @color !important

    color: solid 1px black !important;

Strict math mode, default this in 2.x mode.

Ensure all @import keywords work (inline, once, etc)

Extend pseudo-class:

    nav ul {
      &:extend(.inline);
      background: blue;
    }
    .inline {
      color: red;
    }

Default mixins (see default() in functions)

    .mixin(blue) {
        ..
    }
    .mixin(red) when (default()) {
        ..
    }

Add all relevant less 2.x test cases to the test suite.

When Javascript support is disabled, parse the values as opaque strings
and emit a warning. Filter them from the output.


Completed
-----

Variable references in import paths:

    @import "@{var}/foo.less";


Guards on Rulesets:

    @var: true;
    #foo when ( @var ) {
        ...
    }


Detached rulesets as mixin call arguments:

    .mixin({
        color: blue
    });

Rebuild condition comparator logic, revise tests accordingly.

Variable references in selectors.

    .@{path} {
        ...
    }


Mixin calls to interpolated selectors.

    @name: bar;

    .@{name} {
        color: red;
    }

    .ruleset {
        .bar;
    }

Variables in property names:

    @foo: 'font-size';
    @bar: 'color';
    .ruleset {
        @{foo}: 1px;
        background-@{bar}: red;
    }


Merging property values by comma or space:

    .mixin() {
      box-shadow+: inset 0 0 10px #555;
    }
    .myclass {
      .mixin();
      box-shadow+: 0 0 20px black;
    }


Detached rulesets.

    // declare detached ruleset
    @detached-ruleset: { background: red; };

    // use detached ruleset
    .top {
        @detached-ruleset(); 
    }


Custom units.
 * Requires reworking the enum units into classes.


