
2.x Core Checklist
---------

Ensure all @import keywords work (inline, once, etc)

Variable references in import paths:

    @import "@{var}/foo.less";

Variables in property names:

    @foo: 'font-size';
    @bar: 'color';
    .ruleset {
        @{foo}: 1px;
        background-@{bar}: red;
    }

Extend pseudo-class:

    nav ul {
      &:extend(.inline);
      background: blue;
    }
    .inline {
      color: red;
    }

Detached rulesets.

    // declare detached ruleset
    @detached-ruleset: { background: red; };

    // use detached ruleset
    .top {
        @detached-ruleset(); 
    }

Merging property values by comma or space:

    .mixin() {
      box-shadow+: inset 0 0 10px #555;
    }
    .myclass {
      .mixin();
      box-shadow+: 0 0 20px black;
    }

Default mixins (see default() in functions)

    .mixin(blue) {
        ..
    }
    .mixin(red) when (default()) {
        ..
    }


Completed
-----

Variable references in selectors.

    .@{path} {
        ...
    }


