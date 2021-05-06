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

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * <p>
 * A CSS media block.
 * </p>
 *
 * Example:
 * <pre>
 *     {@literal @}media print {
 *        ...
 *     }
 * </pre>
 */
public class Media extends BlockNode {

  /**
   * {@link Features} attached to the media block.
   */
  protected final Features features;

  /**
   * Constructs an empty media block with empty features.
   */
  public Media() {
    features = new Features();
  }

  /**
   * Constructs an empty media block with the given features.
   */
  public Media(Features features) {
    this.features = features;
  }

  /**
   * Constructs a media node with the given block and features.
   */
  public Media(Features features, Block block) {
    super(block);
    this.features = features;
  }

  /**
   * Creates a copy of this media node's features and block.
   */
  public Media copy(ExecEnv env) throws LessException {
    Features temp = features == null ? null : (Features) features.eval(env);
    Media result = new Media(temp, block.copy());
    result.copyStructure(this);
    result.fileName = fileName;
    return result;
  }

  /**
   * Returns the features attached to this media node.
   */
  public Features features() {
    return features;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.MEDIA;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append("@media ");
    if (features != null) {
      features.repr(buf);
      buf.append(' ');
    }
    if (buf.compress()) {
      buf.append('{');
    } else {
      buf.append("{\n");
    }
    buf.incrIndent();
    block.repr(buf);
    buf.decrIndent();
    if (buf.compress()) {
      buf.append('}');
    } else {
      buf.indent().append("}\n");
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    if (features != null) {
      buf.indent();
      features.modelRepr(buf);
      buf.append('\n');
    }
    buf.indent();
    super.modelRepr(buf);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Media) {
      Media other = (Media)obj;
      return LessUtils.safeEquals(features, other.features) && LessUtils.safeEquals(block, other.block);
    }
    return false;
  }

  @Override
  public String toString() {
    return ModelUtils.toString(this);
  }

  @Override
  public int hashCode() {
    return ModelUtils.notHashable();
  }

}
