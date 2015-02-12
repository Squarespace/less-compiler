
# Differences between squarespace-less and [Less.js][lessjs]

## Rule property merging

Proper merging of the `!important` flag.

Source:

    .spaces {
        a-prop+_: 1px !important;
        a-prop+_: 2px;

        b-prop+_: 3px;
        b-prop+_: 4px !important;
    }

    .commas {
        a-prop+: 1px !important;
        a-prop+: 2px;

        b-prop+: 3px;
        b-prop+: 4px !important;
    }

squarespace-less:

    .spaces {
      a-prop: 1px 2px !important;
      b-prop: 3px 4px !important;
    }
    .commas {
      a-prop: 1px, 2px !important;
      b-prop: 3px, 4px !important;
    }

Less.js (as of 2.4):

    .spaces {
      a-prop: 1px !important 2px;
      b-prop: 3px 4px !important;
    }
    .commas {
      a-prop: 1px !important, 2px;
      b-prop: 3px, 4px !important;
    }


[lessjs]: http://lesscss.org/ "Less.js"

