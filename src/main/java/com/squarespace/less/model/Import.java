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


/**
 * Imports an external stylesheet.
 */
public class Import extends BaseNode {

  /**
   * Path of the stylesheet to import.
   */
  protected final Node path;

  /**
   * Media features associated with this import.
   */
  protected final Features features;

  /**
   * Indicates whether this stylesheet must only be imported once.
   */
  protected final boolean once;

  /**
   * Indicates whether this import statement should be evaluated.
   */
  protected boolean suppress;

  /**
   * Path to the directory for the file.
   */
  protected Path rootPath;

  /**
   * Name of the file to import.
   */
  protected Path fileName;

  /**
   * Constructs an import node with the given path, features and "import once" flag.
   */
  public Import(Node path, Features features, boolean once) {
    this.path = path;
    this.features = features;
    this.once = once;
  }

  /**
   * Returns the path value.
   */
  public Node path() {
    return path;
  }

  /**
   * Returns the media features or {@code null}.
   */
  public Features features() {
    return features;
  }

  /**
   * Indicates whether this stylesheet must only be imported once.
   */
  public boolean once() {
    return once;
  }

  /**
   * Indicates whether this import must be suppressed, because the same
   * stylesheet was imported from another place that was marked "only once".
   */
  public boolean suppress() {
    return suppress;
  }

  /**
   * Sets the value of the suppress flag, indicating whether this node should
   * be evaluated.
   */
  public void suppress(boolean flag) {
    suppress = flag;
  }

  /**
   * Returns the directory for the stylesheet to be imported.
   */
  public Path rootPath() {
    return rootPath;
  }

  /**
   * Returns the filename of the stylesheet to be imported.
   */
  public Path fileName() {
    return fileName;
  }

  /**
   * Sets the directory for the stylesheet to be imported.
   */
  public void rootPath(Path rootPath) {
    this.rootPath = rootPath;
  }

  /**
   * Sets the filename of the stylesheet to be imported.
   */
  public void fileName(Path fileName) {
    this.fileName = fileName;
  }

  /**
   * Renders the path {@link Node} to get the filesystem path for the
   * stylesheet to be imported.
   */
  public String renderPath(ExecEnv env) throws LessException {
    Node value = path;
    if (value instanceof Url) {
      value = ((Url)value).value();
    }

    LessContext ctx = env.context();
    Quoted quoted = null;
    String rendered = null;
    if (value instanceof Quoted) {

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

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return path.needsEval() || (features != null && features.needsEval());
  }

  /**
   * See {@link Node#eval(ExecEnv env)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    Import result = new Import(path.eval(env), features == null ? null : (Features)features.eval(env), once);
    result.rootPath(rootPath);
    result.fileName(fileName);
    result.copyBase(this);
    return result;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.IMPORT;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
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
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
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

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Import) ? safeEquals(path, ((Import)obj).path) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
