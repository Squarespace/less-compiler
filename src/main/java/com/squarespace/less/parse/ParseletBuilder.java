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

import java.util.ArrayList;
import java.util.List;


public class ParseletBuilder {

  private static final Parselet[] EMPTY = new Parselet[] { };

  private List<Parselet> chain = new ArrayList<>();

  public ParseletBuilder add(Parselet ... parselets) {
    for (int i = 0; i < parselets.length; i++) {
      chain.add(parselets[i]);
    }
    return this;
  }

  public Parselet[] build() {
    return chain.toArray(EMPTY);
  }

}
