package com.squarespace.less.exec;

import java.util.HashSet;
import java.util.LinkedHashSet;
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
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 3, time = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class CssSetBenchmark {

  private static final String HIT_KEY = "foobar18";
  private static final String MISS_KEY = "foobar99";

  @Benchmark
  public void hashsetHit(BenchmarkState state, Blackhole blackhole) {
    state.add(state.hashset, HIT_KEY);
  }

  @Benchmark
  public void hashsetMiss(BenchmarkState state, Blackhole blackhole) {
    state.add(state.hashset, MISS_KEY);
  }

  @Benchmark
  public void csssetHit(BenchmarkState state, Blackhole blackhole) {
    state.add(state.cssset, HIT_KEY);
  }

  @Benchmark
  public void csssetMiss(BenchmarkState state, Blackhole blackhole) {
    state.add(state.cssset, MISS_KEY);
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    HashSet<String> hashset = new LinkedHashSet<>(32);
    CssSet<String> cssset = new CssSet<>(32);

    public void add(HashSet<String> set, String key) {
      // logic required to maintain unique ordered list of nodes inside
      // a css block
      if (set.contains(key)) {
        set.remove(key);
      }
      set.add(key);
    }

    public void add(CssSet<String> set, String key) {
      set.add(key);
    }

    @Setup
    public void setup() {
      int size = 20;
      for (int i = 0; i < size; i++) {
        String key = "foobar" + i;
        this.hashset.add(key);
        this.cssset.add(key);
      }
    }
  }

//  public static void main(String[] args) {
//    BenchmarkState state = new BenchmarkState();
//    state.setup();
//    System.out.println(state.cssset.toString());
//  }
}
