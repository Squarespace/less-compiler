package com.squarespace.less.match;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.less.model.Combinator;

public class StatsInternPoolTest {

  @Test
  public void testHits() {
    StatsInternPool pool = new StatsInternPool();
    pool.color("#000", 0, 4);
    pool.keywordColor("red", 0, 3);
    pool.keyword("solid", 0, 5);
    pool.dimension("12px", 0, 4);
    pool.element(Combinator.DESC, "section", 0, 7);
    pool.element(Combinator.CHILD, "section", 0, 7);
    pool.element(Combinator.NAMESPACE, "section", 0, 7);
    pool.element(Combinator.SIB_GEN, "section", 0, 7);
    pool.element(Combinator.SIB_ADJ, "section", 0, 7);
    pool.property("font-size", 0, 9);
    pool.unit("rem", 0, 3);
    pool.function("matrix3d", 0, 8);

    // values that were not interned produce a miss
    pool.keyword("xyzxyz", 0, 6);
    pool.keyword("zzzzzz", 0, 6);
    pool.unit("crab", 0, 4);
    pool.dimension("15.345678em", 0, 11);

    String report = pool.report(1000, 10);
    assertTrue(report.contains("total:\n      hits: 12\n    misses: 4"));
  }
}
