/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
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

import static com.squarespace.less.model.CombinatorType.DESC;
import static com.squarespace.less.model.CombinatorType.SIB_ADJ;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.SelectorUtils;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.SelectorPart;
import com.squarespace.less.model.Selectors;


public class SelectorUtilsTest extends LessTestBase {

  @Test
  public void testNested() {
    SelectorPart parent = element(".parent");
    SelectorPart child = element(".child");
    SelectorPart sibling = element(".sibling");

    Selectors ancestors = selectors(selector(parent));
    Selectors current = selectors(selector(child), selector(sibling));
    Selectors result = SelectorUtils.combine(ancestors, current);

    List<Selector> actual = result.selectors();
    assertEquals(actual.size(), 2);
    assertEquals(actual.get(0), selector(parent, comb(DESC), child));
    assertEquals(actual.get(1), selector(parent, comb(DESC), sibling));
  }

  @Test
  public void testWildcard() {
    SelectorPart parent = element(".parent");
    SelectorPart child = element(".child");
    SelectorPart sibling = element(".sibling");
    SelectorPart wild = element("&");

    Selectors ancestors = selectors(selector(child), selector(sibling));
    Selectors current = selectors(selector(parent, wild));
    Selectors result = SelectorUtils.combine(ancestors, current);

    List<Selector> actual = result.selectors();
    assertEquals(actual.size(), 2);
    assertEquals(actual.get(0), selector(parent, child));
    assertEquals(actual.get(1), selector(parent, sibling));
  }

  @Test
  public void testMultipleWildcards() {
    SelectorPart child = element(".child");
    SelectorPart sibling = element(".sibling");
    SelectorPart wild = element("&");

    Selectors ancestors = selectors(selector(child), selector(sibling));
    Selectors current = selectors(selector(wild, comb(SIB_ADJ), wild));
    Selectors result = SelectorUtils.combine(ancestors, current);

    List<Selector> actual = result.selectors();
    assertEquals(actual.size(), 4);
    assertEquals(actual.get(0), selector(child, comb(SIB_ADJ), child));
    assertEquals(actual.get(1), selector(child, comb(SIB_ADJ), sibling));
    assertEquals(actual.get(2), selector(sibling, comb(SIB_ADJ), child));
    assertEquals(actual.get(3), selector(sibling, comb(SIB_ADJ), sibling));
  }

}
