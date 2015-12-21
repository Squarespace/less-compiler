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



/**
 * Lookup table, holds registered {@link Function}s by name.
 */
public class FunctionTable extends SymbolTable<Function> {

  /**
   * Tunes the internal symbol table's hashmap.
   */
  private static final int NUM_BUCKETS = 64;

  /**
   * Construct a table with the default number of hashmap buckets.
   */
  public FunctionTable() {
    super(NUM_BUCKETS);
  }

  /**
   * Construct a table with the given number of hashmap buckets.
   */
  public FunctionTable(int numBuckets) {
    super(numBuckets);
  }

  /**
   * Register all functions in the given package.
   *
   * @param pkg  package to scan for implementations
   */
  public void register(Registry<Function> pkg) {
    pkg.registerPlugins(this);
  }

  /**
   * Registers a {@link Function} under its {@link Function#name()}
   */
  @Override
  public void add(Object impl) {
    Function func = (Function)impl;
    put(func.name(), func);
  }

}
