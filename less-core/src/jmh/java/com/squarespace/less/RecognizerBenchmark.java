/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less;

import static com.squarespace.less.parse.Recognizers.characters;
import static com.squarespace.less.parse.Recognizers.digits;
import static com.squarespace.less.parse.Recognizers.literal;
import static com.squarespace.less.parse.Recognizers.oneOrMore;
import static com.squarespace.less.parse.Recognizers.sequence;
import static com.squarespace.less.parse.Recognizers.zeroOrOne;

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

import com.squarespace.less.parse.Recognizers.Recognizer;


@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RecognizerBenchmark {

  private static final String LITERAL_OK = "___foobarfoobarfoobarfoobarfoobar";

  private static final String LITERAL_FAIL = "___xyz";

  private static final String PERCENT_OK = "___123123123.123123123%";

  private static final String PERCENT_FAIL = "___123123123.123123123_";

  @Benchmark
  public void regexLiteralOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral().regex, LITERAL_OK, 3));
  }

  @Benchmark
  public void recognizerLiteralOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral().recognizer, LITERAL_OK, 3));
  }

  @Benchmark
  public void regexLiteralFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral().regex, LITERAL_FAIL, 3));
  }

  @Benchmark
  public void recognizerLiteralFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchLiteral().recognizer, LITERAL_FAIL, 3));
  }

  @Benchmark
  public void regexPercentOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent().regex, PERCENT_OK, 3));
  }

  @Benchmark
  public void recognizerPercentOk(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent().recognizer, PERCENT_OK, 3));
  }

  @Benchmark
  public void regexPercentFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent().regex, PERCENT_FAIL, 3));
  }

  @Benchmark
  public void recognizerPercentFail(BenchmarkState state, Blackhole blackhole) {
    blackhole.consume(state.matches(state.benchPercent().recognizer, PERCENT_FAIL, 3));
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private final BenchCase benchLiteral = benchCase(
        "(?:foobar)+",
        oneOrMore(literal("foobar")));

    private final BenchCase benchPercent = benchCase(
        "(?:\\d+\\.\\d+|\\d+)%",
        sequence(digits(), zeroOrOne(sequence(characters('.'), digits())), characters('%')));

    public BenchCase benchLiteral() {
      return benchLiteral;
    }

    public BenchCase benchPercent() {
      return benchPercent;
    }

    public boolean matches(Matcher matcher, String str, int pos) {
      return matcher.reset(str).region(pos, str.length()).lookingAt();
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

  /* Sanity check each set of 4 tests before adding to benchmark.
   *
  public static void main(String[] args) {
    BenchmarkState state = new BenchmarkState();
    boolean flag = state.matches(state.benchPercent().regex, PERCENT_OK, 3);
    System.out.println(flag);
    flag = state.matches(state.benchPercent().recognizer, PERCENT_OK, 3);
    System.out.println(flag);
    flag = state.matches(state.benchPercent().regex, PERCENT_FAIL, 3);
    System.out.println(flag);
    flag = state.matches(state.benchPercent().recognizer, PERCENT_FAIL, 3);
    System.out.println(flag);
  }
  */

}
