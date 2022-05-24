package com.squarespace.less.match;


import static com.squarespace.less.match.Recognizers.characters;
import static com.squarespace.less.match.Recognizers.decimal;
import static com.squarespace.less.match.Recognizers.digits;
import static com.squarespace.less.match.Recognizers.literal;
import static com.squarespace.less.match.Recognizers.oneOrMore;
import static com.squarespace.less.match.Recognizers.sequence;
import static com.squarespace.less.match.Recognizers.units;
import static com.squarespace.less.match.Recognizers.zeroOrOne;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.Patterns;

@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 3, time = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class PatternBenchmark {

//  private static final String ALPHA_OK = "___alpha  (";
//  private static final String ALPHA_FAIL = "___alpha  %";

//  @Benchmark
//  public void regexAlphaOk(BenchmarkState state, Blackhole blackhole) {
//    blackhole.consume(state.matches(state.benchAlpha.regex, ALPHA_OK, 3));
//  }
//
//  @Benchmark
//  public void regexAlphaFail(BenchmarkState state, Blackhole blackhole) {
//    blackhole.consume(state.matches(state.benchAlpha.regex, ALPHA_FAIL, 3));
//  }
//
//  @Benchmark
//  public void recognizerAlphaOk(BenchmarkState state, Blackhole blackhole) {
//    blackhole.consume(state.matches(state.benchAlpha.recognizer, ALPHA_OK, 3));
//  }
//
//  @Benchmark
//  public void recognizerAlphaFail(BenchmarkState state, Blackhole blackhole) {
//    blackhole.consume(state.matches(state.benchAlpha.recognizer, ALPHA_FAIL, 3));
//  }

  private static final String ATTR_KEY_OK = "___foo\\tbar";
  private static final String ATTR_KEY_FAIL = "___foo\\tbar!";

  @Benchmark
  public void regexAttributeKeyOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeKey.regex, ATTR_KEY_OK, 3));
  }

  @Benchmark
  public void regexAttributeKeyFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeKey.regex, ATTR_KEY_FAIL, 3));
  }

  @Benchmark
  public void recognizerAttributeKeyOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeKey.recognizer, ATTR_KEY_OK, 3));
  }

  @Benchmark
  public void recognizerAttributeKeyFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeKey.recognizer, ATTR_KEY_FAIL, 3));
  }

  private static final String ATTR_OP_OK = "___^=";
  private static final String ATTR_OP_FAIL = "___^!";

  @Benchmark
  public void regexAttributeOpOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeOp.regex, ATTR_OP_OK, 3));
  }

  @Benchmark
  public void regexAttributeOpFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeOp.regex, ATTR_OP_FAIL, 3));
  }

  @Benchmark
  public void recognizerAttributeOpOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeOp.recognizer, ATTR_OP_OK, 3));
  }

  @Benchmark
  public void recognizerAttributeOpFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchAttributeOp.recognizer, ATTR_OP_FAIL, 3));
  }

  private static final String CALL_NAME_OK = "___foo-Bar3_baz1-QUUX(";
  private static final String CALL_NAME_FAIL = "___progid:foo.bar.baz-quux(";

  @Benchmark
  public void regexCallNameOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchCallName.regex, CALL_NAME_OK, 3));
  }

  @Benchmark
  public void regexCallNameFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchCallName.regex, CALL_NAME_FAIL, 3));
  }

  @Benchmark
  public void recognizerCallNameOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchCallName.recognizer, CALL_NAME_OK, 3));
  }

  @Benchmark
  public void recognizerCallNameFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchCallName.recognizer, CALL_NAME_FAIL, 3));
  }

  private static final String MIXIN_NAME_OK = "___.foo-Bar3_baz1-QUUX";
  private static final String MIXIN_NAME_FAIL = "___.foo-Bar3_baz1-QUUX!";

  @Benchmark
  public void regexMixinNameOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchMixinName.regex, MIXIN_NAME_OK, 3));
  }

  @Benchmark
  public void regexMixinNameFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchMixinName.regex, MIXIN_NAME_FAIL, 3));
  }

  @Benchmark
  public void recognizerMixinNameOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchMixinName.recognizer, MIXIN_NAME_OK, 3));
  }

  @Benchmark
  public void recognizerMixinNameFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchMixinName.recognizer, MIXIN_NAME_FAIL, 3));
  }

  private static final String LITERAL_OK = "___foobarfoobarfoobarfoobarfoobar";
  private static final String LITERAL_FAIL = "___xyz";

  @Benchmark
  public void regexLiteralOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral.regex, LITERAL_OK, 3));
  }

  @Benchmark
  public void regexLiteralFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral.regex, LITERAL_FAIL, 3));
  }

  @Benchmark
  public void recognizerLiteralOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral.recognizer, LITERAL_OK, 3));
  }

  @Benchmark
  public void recognizerLiteralFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral.recognizer, LITERAL_FAIL, 3));
  }

  private static final String IMPORTANT_OK = "___!   important";
  private static final String IMPORTANT_FAIL = "___!   importan$";

  @Benchmark
  public void regexImportantOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchImportant.regex, IMPORTANT_OK, 3));
  }

  @Benchmark
  public void regexImportantFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchImportant.regex, IMPORTANT_FAIL, 3));
  }

  @Benchmark
  public void recognizerImportantOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchImportant.recognizer, IMPORTANT_OK, 3));
  }

  @Benchmark
  public void recognizerImportantFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchImportant.recognizer, IMPORTANT_FAIL, 3));
  }

  private static final String HEXCOLOR_OK = "___#112233";
  private static final String HEXCOLOR_FAIL = "___#11223%";

  @Benchmark
  public void regexHexcolorOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchHexcolor.regex, HEXCOLOR_OK, 3));
  }

  @Benchmark
  public void regexHexcolorFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchHexcolor.regex, HEXCOLOR_FAIL, 3));
  }

  @Benchmark
  public void recognizerHexcolorOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchHexcolor.recognizer, HEXCOLOR_OK, 3));
  }

  @Benchmark
  public void recognizerHexcolorFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchHexcolor.recognizer, HEXCOLOR_FAIL, 3));
  }

  private static final String DIM_UNIT_OK = "___1.3514vm";
  private static final String DIM_UNIT_FAIL = "___1.3514v%";

  @Benchmark
  public void regexDimUnitOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimUnit.regex, DIM_UNIT_OK, 3));
  }

  @Benchmark
  public void regexDimUnitFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimUnit.regex, DIM_UNIT_FAIL, 3));
  }

  @Benchmark
  public void recognizerDimUnitOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimUnit.recognizer, DIM_UNIT_OK, 3));
  }

  @Benchmark
  public void recognizerDimUnitFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimUnit.recognizer, DIM_UNIT_FAIL, 3));
  }

  private static final String BOOL_OP_OK = "___!=";
  private static final String BOOL_OP_FAIL = "___!#";

  @Benchmark
  public void regexBoolOpOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchBoolOp.regex, BOOL_OP_OK, 3));
  }

  @Benchmark
  public void regexBoolOpFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchBoolOp.regex, BOOL_OP_FAIL, 3));
  }

  @Benchmark
  public void recognizerBoolOpOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchBoolOp.recognizer, BOOL_OP_OK, 3));
  }

  @Benchmark
  public void recognizerBoolOpFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchBoolOp.recognizer, BOOL_OP_FAIL, 3));
  }

  private static final String PROPERTY_OK = "___-*foo-bar0-9";
  private static final String PROPERTY_FAIL = "___-!foo-bar0-9";

  @Benchmark
  public void regexPropertyOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchProperty.regex, PROPERTY_OK, 3));
  }

  @Benchmark
  public void regexPropertyFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchProperty.regex, PROPERTY_FAIL, 3));
  }

  @Benchmark
  public void recognizerPropertyOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchProperty.recognizer, PROPERTY_OK, 3));
  }

  @Benchmark
  public void recognizerPropertyFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchProperty.recognizer, PROPERTY_FAIL, 3));
  }

  private static final String ELEMENT1_OK = "___#foo-bar-baz";
  private static final String ELEMENT1_FAIL = "___#---\n";

  @Benchmark
  public void regexElement1Ok(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchElement1.regex, ELEMENT1_OK, 3));
  }

  @Benchmark
  public void regexElement1Fail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchElement1.regex, ELEMENT1_FAIL, 3));
  }

  @Benchmark
  public void recognizerElement1Ok(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchElement1.recognizer, ELEMENT1_OK, 3));
  }

  @Benchmark
  public void recognizerElement1Fail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchElement1.recognizer, ELEMENT1_FAIL, 3));
  }

  private static final String DIM_OK = "___3.15151533333";
  private static final String DIM_FAIL = "___3.3333333xxxx";

  @Benchmark
  public void regexDimensionOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimension.regex, DIM_OK, 3));
  }

  @Benchmark
  public void regexDimensionFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimension.regex, DIM_FAIL, 3));
  }

  @Benchmark
  public void recognizerDimensiontOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimension.recognizer, DIM_OK, 3));
  }

  @Benchmark
  public void recognizerDimensionFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchDimension.recognizer, DIM_FAIL, 3));
  }

  private static final String PERCENT_OK = "___123123123.123123123%";
  private static final String PERCENT_FAIL = "___123123123.123123123_";

  @Benchmark
  public void regexPercentOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent.regex, PERCENT_OK, 3));
  }

  @Benchmark
  public void recognizerPercentOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent.recognizer, PERCENT_OK, 3));
  }

  @Benchmark
  public void regexPercentFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent.regex, PERCENT_FAIL, 3));
  }

  @Benchmark
  public void recognizerPercentFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent.recognizer, PERCENT_FAIL, 3));
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

//    public final BenchCase benchAlpha = benchCase(
//        "alpha\\s*\\(",
//        Patterns.ALPHA_START
//    );

    public final BenchCase benchAttributeKey = benchCase(
        "([\\w-]|\\\\.)+",
        Patterns.ATTRIBUTE_KEY
    );

    public final BenchCase benchAttributeOp = benchCase(
        "[|~*$^]?=",
        Patterns.ATTRIBUTE_OP
    );

    public final BenchCase benchCallName = benchCase(
        "([\\w-_]+|%|progid:[\\w\\.]+)\\(",
        Patterns.CALL_NAME
    );

    public final BenchCase benchMixinName = benchCase(
        "[#.](?:[\\w-]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+",
        Patterns.MIXIN_NAME
    );

    public final BenchCase benchDimUnit = benchCase(
        "\\d+(\\.\\d+)?(" + Unit.REGEX + ")",
        sequence(decimal(), units())
    );

    public final BenchCase benchDimension = benchCase(
        "[+-]?\\d*\\.?\\d+",
        Recognizers.dimension()
    );

    public static final String _HEXCHAR = "[A-Fa-f0-9]";
    public final BenchCase benchHexcolor = benchCase(
        "#(" + _HEXCHAR + "{6}|" + _HEXCHAR + "{3})",
        Patterns.HEXCOLOR
    );

    public final BenchCase benchLiteral = benchCase(
        "(?:foobar)+",
        oneOrMore(literal("foobar")));

    public final BenchCase benchImportant = benchCase(
        "! *important",
        Patterns.IMPORTANT
        );

    public final BenchCase benchPercent = benchCase(
        "(?:\\d+\\.\\d+|\\d+)%",
        sequence(digits(), zeroOrOne(sequence(characters('.'), digits())), characters('%')));

    public final BenchCase benchBoolOp = benchCase(
        "<>|=[<>]*|[<>]=*|!=",
        Recognizers.boolOperator());

    public final BenchCase benchProperty = benchCase(
        "\\*?-?[_a-z0-9-]+",
        Recognizers.property()
        );

    public final BenchCase benchElement1 = benchCase(
        "(?:[.#]?|:*)(?:[\\w-]|[^\\u0000-\\u009f]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+",
        Recognizers.element1()
        );

    public boolean matches(Matcher matcher, String str, int pos) {
      int len = str.length();
      matcher.reset(str).region(pos, len);
      if (matcher.lookingAt()) {
        return matcher.end() == len;
      }
      return false;
    }

    public boolean matches(Recognizer recognizer, String str, int pos) {
      int len = str.length();
      return recognizer.match(str, pos, len) == len;
    }

    private BenchCase benchCase(String pattern, Recognizer recognizer) {
      return new BenchCase(pattern, recognizer);
    }

  }

  public static class BenchCase {

    public final Matcher regex;

    public final Recognizer recognizer;

    public BenchCase(String pattern, Recognizer recognizer) {
      this.regex = Pattern.compile(pattern).matcher("");
      this.recognizer = recognizer;
    }
  }

//  public static void main(String[] args) {
//    BenchmarkState state = new BenchmarkState();
//    boolean flag;
//  }

  /* Sanity check each set of tests before adding to benchmark.
   *
  public static void main(String[] args) {
    BenchmarkState state = new BenchmarkState();
    boolean flag;

    flag = state.matches(state.benchAlpha.regex, ALPHA_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchAlpha.regex, ALPHA_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchAlpha.recognizer, ALPHA_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchAlpha.recognizer, ALPHA_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchAttributeKey.regex, ATTR_KEY_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchAttributeKey.regex, ATTR_KEY_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchAttributeKey.recognizer, ATTR_KEY_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchAttributeKey.recognizer, ATTR_KEY_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchAttributeOp.regex, ATTR_OP_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchAttributeOp.regex, ATTR_OP_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchAttributeOp.recognizer, ATTR_OP_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchAttributeOp.recognizer, ATTR_OP_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchCallName.regex, CALL_NAME_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchCallName.regex, CALL_NAME_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchCallName.recognizer, CALL_NAME_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchCallName.recognizer, CALL_NAME_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchMixinName.regex, MIXIN_NAME_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchMixinName.regex, MIXIN_NAME_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchMixinName.recognizer, MIXIN_NAME_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchMixinName.recognizer, MIXIN_NAME_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchHexcolor.regex, HEXCOLOR_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchHexcolor.recognizer, HEXCOLOR_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchHexcolor.recognizer, HEXCOLOR_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchHexcolor.recognizer, HEXCOLOR_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchDimUnit.regex, DIM_UNIT_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchDimUnit.recognizer, DIM_UNIT_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchDimUnit.regex, DIM_UNIT_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchDimUnit.recognizer, DIM_UNIT_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchBoolOp.regex, BOOL_OP_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchBoolOp.recognizer, BOOL_OP_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchBoolOp.regex, BOOL_OP_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchBoolOp.recognizer, BOOL_OP_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchProperty.regex, PROPERTY_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchProperty.recognizer, PROPERTY_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchProperty.regex, PROPERTY_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchProperty.recognizer, PROPERTY_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchElement1.regex, ELEMENT1_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchElement1.recognizer, ELEMENT1_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchElement1.regex, ELEMENT1_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchElement1.recognizer, ELEMENT1_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchImportant.regex, IMPORTANT_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchImportant.recognizer, IMPORTANT_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchImportant.regex, IMPORTANT_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchImportant.recognizer, IMPORTANT_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchDimension.regex, DIM_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchDimension.recognizer, DIM_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchDimension.regex, DIM_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchDimension.recognizer, DIM_FAIL, 3);
    isfalse(flag);

    flag = state.matches(state.benchPercent.regex, PERCENT_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchPercent.regex, PERCENT_FAIL, 3);
    isfalse(flag);
    flag = state.matches(state.benchPercent.recognizer, PERCENT_OK, 3);
    istrue(flag);
    flag = state.matches(state.benchPercent.recognizer, PERCENT_FAIL, 3);
    isfalse(flag);
  }
  */

//  private static void istrue(boolean flag) {
//    if (!flag) {
//      throw new RuntimeException("Expected true");
//    }
//  }
//
//  private static void isfalse(boolean flag) {
//    if (flag) {
//      throw new RuntimeException("Expected false");
//    }
//  }
}
