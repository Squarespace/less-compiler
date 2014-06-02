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
import static com.squarespace.less.model.NodeType.RATIO;

import com.squarespace.less.core.Buffer;


public class Ratio extends BaseNode {

  private final String value;

  public Ratio(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Ratio) ? safeEquals(value, ((Ratio)obj).value) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return RATIO;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(value);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(value);
  }

}
