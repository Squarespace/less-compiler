/**
 * Copyright (c) 2016 SQUARESPACE, Inc.
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
 * Keyword that behaves like a color but doesn't have a definite
 * color representation. When canonicalizing it is always represented
 * by its keyword.
 */
public class KeywordColor extends RGBColor {

  private final String keyword;

  public KeywordColor(String keyword, int r, int g, int b) {
    super(r, g, b, true);
    this.keyword = keyword;
  }

  public String keyword() {
    return keyword;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(keyword);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(" KEYWORD [fake rgb] ").append(keyword).append(' ');
    buf.append(red).append(' ').append(green).append(' ').append(blue).append(' ').append(alpha);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof KeywordColor) {
      KeywordColor other = (KeywordColor) obj;
      return this.keyword.equals(other.keyword);
    } else if (obj instanceof BaseColor) {
      // Compare as color in other circumstances
      return obj.equals(this);
    }
    return false;
  }

}
