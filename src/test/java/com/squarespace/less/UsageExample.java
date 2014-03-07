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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;


public class UsageExample {

  private static final LessCompiler COMPILER = new LessCompiler();

  private static final LessMessages MESSAGES = new LessMessages();

  private static Options buildOptions(Path importPath) {
    Options opts = new Options();
    opts.addImportPath(importPath.toString());
    opts.compress(false);
    opts.tracing(false);
    opts.indent(4);
    opts.importOnce(true);
    opts.strict(false);
    opts.hideWarnings(false);
    return opts;
  }

  public static HashMap<Path, String> buildMap() {
    HashMap<Path, String> map = new HashMap<>();
    Path base = Paths.get("base.less").toAbsolutePath().normalize();
    map.put(base, "@baseColor: #abc;");
    return map;
  }

  public static void main(String[] args) {
    String source = "@import 'base.less'; .ruleset { color: @baseColor; }";
    Path path = Paths.get("sheet.less").toAbsolutePath();
    LessLoader loader = new HashMapLessLoader(buildMap());
    Options opts = buildOptions(path);
    Context ctx = new Context(opts, loader);
    ctx.setCompiler(COMPILER);
    try {
      String result = COMPILER.compile(source, ctx, path.getParent(), path);
      System.out.println(result);

    } catch (LessException exc) {
      System.err.println(MESSAGES.formatError(exc));
    }
  }

}
