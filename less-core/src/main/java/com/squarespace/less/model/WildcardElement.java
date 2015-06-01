/**
 * Copyright, 2015, Squarespace, Inc.
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

import com.squarespace.less.core.Buffer;


/**
 * Node representing the wildcard element "&".
 */
public class WildcardElement extends SelectorPart {

  @Override
  public NodeType type() {
    return NodeType.WILDCARD_ELEMENT;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append('&');
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
  };

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof WildcardElement);
  }

  @Override
  public int hashCode() {
    return hashCode == 0 ? buildHashCode("&") : hashCode;
  }

}
