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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class LessImporterTest extends LessTestBase {

  private static final LessCompiler COMPILER = new LessCompiler();

  @Test
  public void testImporter() throws LessException {
    LessLoader loader = new HashMapLessLoader(buildMap());
    LessOptions opts = buildOptions();
    LessContext ctx = new LessContext(opts, loader);
    ctx.setCompiler(COMPILER);
    String source = "@import 'base.less'; .ruleset { color: @color; font-size: @size; }";
    String result = COMPILER.compile(source, ctx, Paths.get("."), null, true);

    assertEquals(result, ".child{font-size:12px}.ruleset{color:#abc;font-size:12px}");
  }

  @Test
  public void testImporterRecursionLimit() throws LessException {
    int imports = 100;
    Map<Path, String> map = new HashMap<>();
    for (int i = 0; i < imports; i++) {
      map.put(path(i + ".less"), "@import '" + (i + 1) + ".less';\n");
    }
    map.put(path(imports + ".less"), ".parent { color: red; }\n");

    int recursionLimit = 90;
    LessLoader loader = new HashMapLessLoader(map);
    LessOptions opts = buildOptions();
    opts.importRecursionLimit(recursionLimit);
    LessContext ctx = new LessContext(opts, loader);
    ctx.setCompiler(COMPILER);

    String source = "@import '1.less';";
    try {
      COMPILER.compile(source, ctx, path("."), path("foo.less"), true);
      fail("Expected import recursion limit exception");
    } catch (LessException e) {
      assertTrue(e.getMessage().contains("limit of " + recursionLimit + " exceeded"), e.getMessage());
    }
  }

  private static Path path(String path) {
    return Paths.get(path).toAbsolutePath().normalize();
  }

  private static Map<Path, String> buildMap() {
    Map<Path, String> map = new HashMap<>();
    map.put(path("base.less"), "@color: #abc; @import 'child.less';");
    map.put(path("child.less"), ".child { font-size: 12px; }\n@size: 12px;");
    return map;
  }

  private static LessOptions buildOptions() {
    LessOptions opts = new LessOptions();
    opts.compress(true);
    opts.tracing(false);
    opts.indent(4);
    opts.importOnce(true);
    opts.strict(false);
    opts.hideWarnings(false);
    return opts;
  }

}
