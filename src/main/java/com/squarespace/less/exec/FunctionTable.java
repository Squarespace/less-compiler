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

package com.squarespace.less.exec;

import com.squarespace.less.core.TypeRef;


/**
 * Lookup table, holds registered functions by name.
 */
public class FunctionTable extends SymbolTable<Function> {

  private static final int NUM_BUCKETS = 64;

  private static final TypeRef<Function> TYPE_REF = new TypeRef<Function>() { };

  public FunctionTable() {
    super(TYPE_REF, NUM_BUCKETS);
  }

  public FunctionTable(int numBuckets) {
    super(TYPE_REF, numBuckets);
  }

  @Override
  public void registerSymbol(Object impl) {
    Function func = (Function)impl;
    put(func.name(), func);
  }

}
