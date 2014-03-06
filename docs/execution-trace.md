## Execution Traces

Stack traces are useful in determining the source of the error.  However, often
the LESS file is perfectly valid and produces valid CSS but where the
properties or values are not the expected or intended values.

Once you are making several nested mixin calls across file boundaries with
many computed arguments, it can be difficult to identify how or where an error
originated.  As the complexity of the source file and imported libraries grows,
it gets harder to track down the source of a bad pixel value or a color that is
slightly off.

An execution trace inserts comments into the output which indicate the
operations taking place in and around each rule and ruleset.  They indicate the
begin/end boundaries of imports and mixin calls, including the exact arguments
used at the time of the call.

#### Example 1: Tracing mixins and imports

include.less:

    /** include.less is here **/

    .another-mixin(@size) {
        // add an additional pixel
        prop-c: 1px + @size;
    }

trace.less:

    @import "include.less";

    .set-opacity(@amount) {
        opacity: @amount;
    }
    .ruleset-1 {
        .set-opacity(.75);
    }
    .ruleset-2 {
        .set-opacity(.25);
    }

Output:

    /* TRACE[1]:     start   @import "trace-include.less";    trace.less:2  */
    /** include.less is here **/
    /* TRACE[2]:       end   @import "trace-include.less";    trace.less:2  */
    .ruleset-1 {
      /* TRACE[3]:     start   .set-opacity(.75);    trace.less:9  */
      /* TRACE[4]: next rule defined at 'trace.less:5' */
      opacity: .75;
      /* TRACE[5]:     start   .another-mixin(.75);    trace.less:6  */
      /* TRACE[6]: next rule defined at 'trace-include.less:6' */
      prop-c: 1.75px;
      /* TRACE[7]:       end   .another-mixin(.75);    trace.less:6  */
      /* TRACE[8]:       end   .set-opacity(.75);    trace.less:9  */
    }
    .ruleset-2 {
      /* TRACE[9]:     start   .set-opacity(.25);    trace.less:12  */
      /* TRACE[10]: next rule defined at 'trace.less:5' */
      opacity: .25;
      /* TRACE[11]:     start   .another-mixin(.25);    trace.less:6  */
      /* TRACE[12]: next rule defined at 'trace-include.less:6' */
      prop-c: 1.25px;
      /* TRACE[13]:       end   .another-mixin(.25);    trace.less:6  */
      /* TRACE[14]:       end   .set-opacity(.25);    trace.less:12  */
    }


