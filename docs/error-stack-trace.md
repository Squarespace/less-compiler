## Error Reporting and Stack Traces

The mixin recursion limit is configurable.  Exceeding the recursion limit will
raise an error that includes the stack trace.  If the number of frames exceeds
the display window, the middle frames will be omitted.

#### Example 1: Recursion exceeds the limit

recursion.less:

    .mixin(@arg: 0) when (@arg < 30) {
        (~'rule-@{arg}') {
            color: #000 + @arg;
        }
        .mixin(@arg + 1);
    }
    .parent {
        .mixin;
    }

Executing the above template with a recursion limit of 20, output:

    An error occurred in 'recursion.less':

    Line               Statement
    ----------------------------
    recursion.less:9   .parent {
    recursion.less:10      .mixin;
    recursion.less:6           .mixin(1);
    recursion.less:6               .mixin(2);
    recursion.less:6                   .mixin(3);
    recursion.less:6                       .mixin(4);

    .. skipped 9 frames

    recursion.less:6                           .mixin(14);
    recursion.less:6                               .mixin(15);
    recursion.less:6                                   .mixin(16);
    recursion.less:6                                       .mixin(17);
    recursion.less:6                                           .mixin(18);
    recursion.less:6                                               .mixin(19);

    ExecuteError MIXIN_RECURSE: Mixin call [.mixin] exceeded the recursion limit of 20


#### Example 2: Mutual recursion without guard conditions

ping-pong.less:

    .foo(@val) {
        .bar(@val + 1);
    }
    .bar(@val) {
        .foo(@val + 1);
    }
    .ruleset {
        .foo(1);
    }

The mutually-recursing mixins lack guards and would execute forever, but the
code is stopped by the default recursion limit:

    An error occurred in 'ping-pong.less':

    Line              Statement
    ---------------------------
    ping-pong.less:8  .ruleset {
    ping-pong.less:9      .foo(1);
    ping-pong.less:3          .bar(2);
    ping-pong.less:6              .foo(3);
    ping-pong.less:3                  .bar(4);
    ping-pong.less:6                      .foo(5);

    .. skipped 53 frames

    ping-pong.less:6                          .foo(59);
    ping-pong.less:3                              .bar(60);
    ping-pong.less:6                                  .foo(61);
    ping-pong.less:3                                      .bar(62);
    ping-pong.less:6                                          .foo(63);
    ping-pong.less:3                                              .bar(64);

    ExecuteError MIXIN_RECURSE: Mixin call [.foo] exceeded the recursion limit of 64


