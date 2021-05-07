# Bugs in legacy parser

This is a list of bugs in the legacy parser that we need to keep working until the bugs are resolved in the source stylesheets.

### BUG1

Extraneous `'+'` character at block scope.

**site**: carrot-avocado-4rwr line 13455:

```
.tag-wrapper {
    width: (100 - 2 * @contentPaddingLeftRight);
    padding: 0;
    .box-sizing(~'border-box');
    margin: -7.2rem @contentPaddingLeftRight 9.6rem;

    +
  }
```

- carrot-avocado-4rwr

### BUG2

Media directives not properly opened / closed.

**site**: julie-kim-rd7f line 16568:

```less
.transparent-header.collection-type-page .banner-thumbnail-wrapper {
     padding: 136px 0px;

@media (max-width:768px)
 }
```

**site**: fe-moran-backend line 17344

```less
@media (max-width: 640px) {
  #intro {
    .index-section-wrapper.page-content {
      margin-top: 0!important;
    }
  }
  @media only screen
and (min-device-width : 414px)
and (max-device-width : 736px)
<eof>
```

**site**: adracare line 16397:

```less
@media only screen and (max-width: 640px)
#folderNav+#content, #categoryNav+#content {
  padding-top: 50px;
}
```

**site**: aqua-caper-wd2j line 14408:

```less
@media (max-width: 479px);
.div-block-3 {
    width: auto!important;
}
```

### BUG3

Acceptance of variables followed by `"()"` parenthesis, e.g. `@dk-gray()`.

**site**: crane-bullfrog-mgel line 15944:

```less
  &.dark-bg {
    background-color: @dk-gray();
    color: @white;
  }
```

### BUG4

Acceptance of invalid addition expressions: `1 + px`.

**site**: brine-may2017 line 20614:

```less
.mySize {
  font-size: random(90) + px;
}
```
