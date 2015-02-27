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

package com.squarespace.less.parse;

import static com.squarespace.less.core.SyntaxErrorMaker.javascriptDisabled;

import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;


/**
 * For now, just detect JavaScript syntax so we can raise an error.
 */
public class JavascriptParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    LessOptions options = stm.context().options();
    int pos = 0;
    if (stm.peek() == Chars.TILDE) {
      pos++;
    }
    if (stm.peek(pos) == Chars.GRAVE_ACCENT) {
      if (options.strict()) {
        throw stm.parseError(new LessException(javascriptDisabled()));
      }
      stm.seek(2);
      stm.seekTo(Chars.GRAVE_ACCENT);
      if (!options.hideWarnings()) {
        stm.execEnv().addWarning("javascript expressions are currently unsupported");
      }
    }
    return null;
  }

}
