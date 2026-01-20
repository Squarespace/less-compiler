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

import java.nio.file.Paths;

import org.testng.annotations.Test;


/**
 * Regression tests: contexts must not share a mutable default
 * options object.
 */
public class LessContextTest {

  private static final String SOURCE = ".a { color: red; }";

  private static final String PLAIN = ".a {\n  color: red;\n}\n";

  private static final String COMPRESSED = ".a{color:red}";

  // Mutating one no-arg context's options must not affect another's output.
  @Test
  public void testNoSharedDefaultOptions() throws LessException {
    LessCompiler compiler = new LessCompiler();
    LessContext a = new LessContext();
    LessContext b = new LessContext();

    a.options().compress(true);

    assertEquals(compiler.compile(SOURCE, b), PLAIN);
    assertEquals(compiler.compile(SOURCE, a), COMPRESSED);
  }

  // Import paths added via one context must not appear in another.
  @Test
  public void testNoSharedImportPaths() throws LessException {
    LessContext a = new LessContext();
    LessContext b = new LessContext();

    a.options().addImportPath("/tenant/path");

    assertTrue(b.options().importPaths().isEmpty());
    assertEquals(a.options().importPaths().size(), 1);
  }

  // Outside callers get a read-only view of the import path list.
  @Test
  public void testImportPathsUnmodifiable() {
    LessOptions opts = new LessOptions();
    opts.addImportPath("/somewhere");

    try {
      opts.importPaths().add(Paths.get("/elsewhere"));
      fail("expected UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
      // pass
    }
  }

}
