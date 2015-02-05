## Known Differences and Extensions

The following is a list of known differences between the Squarespace LESS
compiler and less.js 1.7.0.


#### Color keywords are allowed to participate in math expressions

    foo: blue + 1;
    foo: blue + red;

#### Alpha opacity can use floating-point values

    foo: alpha(opacity=.7)
    foo: alpha(opacity=0.35)

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


