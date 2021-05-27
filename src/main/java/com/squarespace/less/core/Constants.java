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

package com.squarespace.less.core;

import com.squarespace.less.match.InternPool;
import com.squarespace.less.model.False;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.True;


public class Constants {

  public static final String NULL_PLACEHOLDER = "???";

  public static final True TRUE = new True();

  public static final False FALSE = new False();

  public static final Features EMPTY_FEATURES = new Features();

  public static final Selectors EMPTY_SELECTORS = new Selectors();

  public static final String UTF8 = "UTF-8";

  /**
   * Default string intern pool.
   */
  public static final InternPool INTERN_POOL = new InternPool();

}
