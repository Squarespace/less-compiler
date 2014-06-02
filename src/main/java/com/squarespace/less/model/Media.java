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


public class Media extends BlockNode {

  private final Features features;

  public Media() {
    features = new Features();
  }

  public Media(Features features) {
    this.features = features;
  }

  public Media(Features features, Block block) {
    super(block);
    this.features = features;
  }

  public Media copy(ExecEnv env) throws LessException {
    Features temp = features == null ? null : (Features) features.eval(env);
    Media result = new Media(temp, block.copy());
    result.fileName = fileName;
    return result;
  }

  public Features features() {
    return features;
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
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.MEDIA;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append("@media ");
    if (features != null) {
      features.repr(buf);
      buf.append(' ');
    }
    buf.append("{\n");
    buf.incrIndent();
    block.repr(buf);
    buf.decrIndent();
    buf.indent().append("}\n");
  }

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

}
