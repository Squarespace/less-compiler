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
import java.util.Collections;
import java.util.List;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.SelectorUtils;


/**
 * A specific invocation of a mixin.
 */
public class MixinCall extends BaseNode {

  /**
   * The mixin call's selector.
   */
  protected final Selector selector;

  /**
   * Segmented selector path used to locate the mixin definition.
   */
  protected final List<String> selectorPath;

  /**
   * Arguments to the mixin call.
   */
  protected MixinCallArgs args;

  /**
   * Indicates whether this mixin call was marked important.
   */
  protected final boolean important;

  /**
   * Path to the file in which this mixin call was defined.
   */
  protected Path fileName;

  /**
   * Constructs a mixin call having the given selector, arguments, and whether
   * it is marked important.
   */
  public MixinCall(Selector selector, MixinCallArgs args, boolean important) {
    this.selector = selector;
    this.args = args;
    this.important = important;
    this.selectorPath = SelectorUtils.renderSelector(selector);
  }

  /**
   * Copies this mixin call.
   */
  public MixinCall copy() {
    MixinCall result = new MixinCall(selector, args, important);
    result.copyBase(this);
    result.fileName = fileName;
    return result;
  }

  /**
   * Returns the selector for this mixin call.
   */
  public Selector selector() {
    return selector;
  }

  /**
   * Returns the arguments to the mixin call.
   */
  public MixinCallArgs args() {
    return args;
  }

  /**
   * Sets the arguments to the mixin call.
   */
  public void args(MixinCallArgs args) {
    this.args = args;
  }

  /**
   * Indicates whether this mixin call was marked important.
   */
  public boolean important() {
    return important;
  }

  /**
   * Returns the segmented selector path used to locate the mixin definition.
   */
  public List<String> path() {
    if (selectorPath == null) {
      return Collections.emptyList();
    }
    return this.selectorPath;
  }

  /**
   * Returns the path to the file in which this mixin call was defined.
   */
  public Path fileName() {
    return fileName;
  }

  /**
   * Sets the path to the file in which this mixin call was defined.
   */
  public void fileName(Path fileName) {
    this.fileName = fileName;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.MIXIN_CALL;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    Selectors.reprSelector(buf, selector);
    if (args != null) {
      args.repr(buf);
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(important ? "important" : "");
    buf.append(' ').append(selectorPath.toString()).append('\n');
    buf.incrIndent().indent();
    selector.modelRepr(buf);
    if (args != null) {
      buf.append('\n').indent();
      args.modelRepr(buf);
    }
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    // 'path' field is derived from the selector, so doesn't need to be included.
    if (obj instanceof MixinCall) {
      MixinCall other = (MixinCall)obj;
      return important == other.important
          && safeEquals(selector, other.selector)
          && safeEquals(args, other.args);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
