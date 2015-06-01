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
import static com.squarespace.less.model.NodeType.COMMENT;

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.Chars;
import com.squarespace.less.core.LessInternalException;


/**
 * Single-line and block CSS comments.
 */
public class Comment extends BaseNode {

  /**
   * Body of the comment.
   */
  protected final String body;

  /**
   * Indicates if this is a block (multi-line) comment.
   */
  protected final boolean block;

  /**
   * Indicates if a newline should be appended when the comment is emitted.
   */
  protected final boolean newline;

  /**
   * Constructs a comment with the given body, indicating if it is a block-style comment.
   */
  public Comment(String body, boolean block) {
    this(body, block, false);
  }

  /**
   * Constructs a comment with the given body, indicating if it is a block-style comment,
   * and if a newline should be added.
   */
  public Comment(String body, boolean block, boolean newline) {
    if (body == null) {
      throw new LessInternalException("Serious error: body cannot be null.");
    }
    this.body = body;
    this.block = block;
    this.newline = newline;
  }

  /**
   * Returns the body of the comment.
   */
  public String body() {
    return body;
  }

  /**
   * Indicates whether this is a block-style comment.
   */
  public boolean block() {
    return block;
  }

  /**
   * Indicates whether a newline should be emitted after the comment.
   */
  public boolean newline() {
    return newline;
  }

  /**
   * Indicates if this comment body starts with an exclamation character.
   * When the final output is "minified", comments are removed. If a comment
   * starts with a '!' it will be retained.
   */
  public boolean hasBang() {
    return !body.isEmpty() && body.charAt(0) == Chars.EXCLAMATION_MARK;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return COMMENT;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    // Emit comments if we're not compressing the output, or if this is a block comment
    // that starts with an exclamation point.
    if (!buf.compress() || (block && hasBang())) {
      if (block) {
        buf.append("/*").append(body).append("*/");
      } else {
        buf.append("//").append(body);
      }
      if (newline) {
        buf.append("\n");
      }
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(block ? "block" : "single line");
    buf.append(' ').append(newline ? "newline" : "inline").append('\n');
    buf.incrIndent();
    buf.indent();
    buf.append(StringEscapeUtils.escapeJava(body));
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Comment) {
      Comment other = (Comment)obj;
      return block == other.block && safeEquals(body, other.body);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
