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

package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import java.nio.file.Path;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;


public class Import extends BaseNode {

  private final Node path;

  private final Features features;

  private final boolean once;

  private Block block;

  private boolean suppress;

  private Path rootPath;

  private Path fileName;

  public Import(Node path, Features features, boolean once) {
    this.path = path;
    this.features = features;
    this.once = once;
  }

  public Node path() {
    return path;
  }

  public Features features() {
    return features;
  }

  public boolean once() {
    return once;
  }

  public Block block() {
    return block;
  }

  public void block(Block block) {
    this.block = block;
  }

  public boolean suppress() {
    return suppress;
  }

  public void suppress(boolean flag) {
    suppress = flag;
  }

  public Path rootPath() {
    return rootPath;
  }

  public Path fileName() {
    return fileName;
  }

  public void rootPath(Path rootPath) {
    this.rootPath = rootPath;
  }

  public void fileName(Path fileName) {
    this.fileName = fileName;
  }

  public String renderPath(ExecEnv env) throws LessException {
    Node value = path;
    if (value.is(NodeType.URL)) {
      value = ((Url)value).value();
    }

    LessContext ctx = env.context();
    Quoted quoted = null;
    String rendered = null;
    if (value.is(NodeType.QUOTED)) {

      // Strip quote delimiters and render inner string. This technique allows
      // for variable substitution inside @import paths, which may or may not
      // be useful.
      //
      // Conversely, Less.js performs all importing during the parse phase, so
      // it has to assume all paths are bare strings.
      quoted = ((Quoted)value).copy();
      quoted.setEscape(true);
      rendered = ctx.render(quoted);

    } else {
      rendered = ctx.render(value);
    }
    return rendered;
  }

  @Override
  public boolean needsEval() {
    return path.needsEval() || (features != null && features.needsEval());
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    Import result = new Import(path.eval(env), features == null ? null : (Features)features.eval(env), once);
    result.rootPath(rootPath);
    result.fileName(fileName);
    result.copyPosition(this);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Import) ? safeEquals(path, ((Import)obj).path) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.IMPORT;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append("@import");
    if (once) {
      buf.append("-once ");
    } else {
      buf.append(' ');
    }
    path.repr(buf);
    if (features != null) {
      buf.append(" ");
      features.repr(buf);
    }
    buf.append(";\n");
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    if (once) {
      buf.append(" [once] ");
    }
    buf.append('\n').incrIndent().indent();
    path.modelRepr(buf);
    buf.append('\n');
    if (features != null) {
      buf.indent();
      ReprUtils.modelRepr(buf, "\n", true, features.features());
    }
    buf.decrIndent();
  }

}
