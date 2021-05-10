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

package com.squarespace.less.exec;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


/**
 * Routines shared among multiple unit test classes.
 */
public class LessSuiteBase {

  protected static final String GLOB_LESS = "glob:*.less";

  protected static final String SUITE_RESOURCE = "test-suite";

  public static Path testSuiteRoot() {
    URL top = LessTestBase.class.getClassLoader().getResource(SUITE_RESOURCE);
    if (top == null) {
      throw new RuntimeException("Cannot locate test suite resource '" + SUITE_RESOURCE + "'");
    }
    return Paths.get(top.getPath());
  }

  protected Stylesheet parse(String source, Path importRoot) throws LessException {
    return parse(source, importRoot, false);
  }

  protected Stylesheet parse(String source, Path importRoot, boolean safeMode) throws LessException {
    LessOptions opts = new LessOptions();
    opts.addImportPath(importRoot.toString());
    LessContext ctx = new LessContext(opts);
    LessCompiler compiler = new LessCompiler();
    ctx.setCompiler(compiler);
    return compiler.parse(source, ctx, safeMode);
  }

  protected String compile(String source, Path importRoot, boolean tracing) throws LessException {
    return compile(source, importRoot, tracing, null, null);
  }

  protected String compile(String source, Path importRoot, boolean tracing, Path parent, Path fileName)
      throws LessException {

    // Setup the compiler
    LessOptions opts = new LessOptions();
    opts.tracing(tracing);
    opts.addImportPath(importRoot.toString());

    LessContext ctx = new LessContext(opts);
    LessCompiler compiler = new LessCompiler();
    ctx.setCompiler(compiler);

    // First, parse the stylesheet and generate the parse tree and canonical representations,
    // in order to exercise more parts of the code.
    Buffer buf = new Buffer(2);
    Stylesheet sheet = compiler.parse(source, ctx, parent, fileName);
    sheet.modelRepr(buf);
    sheet.repr(buf);

    // Hack to detect case-specific options enabled via comments.
    // Next version of the compiler will make this easier.
    Block block = sheet.block();
    FlexList<Node> rules = block.rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node rule = rules.get(i);
      if (rule instanceof Comment) {
        Comment comment = (Comment)rule;
        if (comment.body().trim().equals("strict=false")) {
          opts.strict(false);
        }
      }
    }

    // Finally, compile and execute the stylesheet.
    ctx = new LessContext(opts);
    ctx.setCompiler(compiler);
    String result = compiler.compile(source, ctx, parent, fileName, true);
    ctx.sanityCheck();
    return result;
  }

  /**
   * Parse source and return canonical representation of stylesheet.
   */
  protected String canonicalize(String source, Path importRoot, int indent) throws LessException {
    LessOptions opts = new LessOptions();
    opts.addImportPath(importRoot.toString());
    LessContext ctx = new LessContext(opts);
    LessCompiler compiler = new LessCompiler();
    ctx.setCompiler(compiler);

    Stylesheet sheet = compiler.parse(source, ctx);
    Buffer buf = new Buffer(2);
    sheet.repr(buf);
    return buf.toString();
  }

  /**
   * Create a diff between the expected and actual strings. If any
   * differences are found, format an error message.
   */
  public static String diff(String expected, String actual) {
    List<String> expList = Arrays.asList(expected.split("\n"));
    List<String> actList = Arrays.asList(actual.split("\n"));
    Patch<String> patch = DiffUtils.diff(expList, actList);
    List<Delta<String>> deltas = patch.getDeltas();
    if (deltas.size() == 0) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    for (Delta<String> delta : deltas) {
      Chunk<String> chunk1 = delta.getOriginal();
      int pos1 = chunk1.getPosition();
      List<String> lines1 = chunk1.getLines();

      Chunk<String> chunk2 = delta.getRevised();
      int pos2 = chunk2.getPosition();
      List<String> lines2 = chunk2.getLines();

      buf.append("@@ -" + pos1 + "," + lines1.size());
      buf.append(" +" + pos2 + "," + lines2.size()).append(" @@\n");
      for (String row : lines1) {
        buf.append("- ").append(row).append('\n');
      }
      for (String row : lines2) {
        buf.append("+ ").append(row).append('\n');
      }
    }
    return buf.toString();
  }

  protected void logFailure(String header, int index, Object ... arguments) {
    System.err.print(header + " failure " + index + ":");
    for (Object arg : arguments) {
      System.err.print(" " + arg.toString());
    }
    System.err.println();
  }


}
