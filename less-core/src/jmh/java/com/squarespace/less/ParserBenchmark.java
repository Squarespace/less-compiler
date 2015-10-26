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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;

import com.squarespace.less.core.LessUtils;
import com.squarespace.less.parse.LessParser;
import com.squarespace.less.parse.LessStream;
import com.squarespace.less.parse.Parselets;


@Fork(1)
@Measurement(iterations = 5, time = 5)
@Warmup(iterations = 5, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ParserBenchmark {

  @Benchmark
  public void parseSelector(BenchmarkState state) throws LessException {
    state.streamExample().parse(Parselets.STYLESHEET);
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private String example;

    @Setup
    public void setupResources() throws RunnerException {
      try {
        example = load("example.less");
      } catch (IOException e) {
        throw new RunnerException("Failed to init benchmark state", e);
      }
    }

    public LessStream streamExample() {
      return stream(example);
    }

    private String load(String path) throws IOException {
      return LessUtils.loadResource(ParserBenchmark.class, path);
    }

    private LessStream stream(String string) {
      LessContext context = new LessContext();
      LessParser parser = new LessParser(context);
      return new LessStream(parser, string);
    }
  }

}
