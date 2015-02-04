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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.core.TypeRef;


/**
 * Generic table mapping symbol strings to values. Raises an error when an attempt
 * is made to register a symbol more than once.
 */
public abstract class SymbolTable<V> {

  /**
   * Set which tracks the packages that have been registered to
   * this instance, to detect accidental re-registrations.
   */
  private final Set<Class<?>> packages;

  /**
   * Mapping of symbol to implementation.
   */
  private final Map<String, V> table;

  /**
   * Accessor for the generic type parameter.
   */
  private final TypeRef<V> typeRef;

  /**
   * Flag to indicate whether this symbol table is in use. If the symbol table
   * is marked "in use", any attempt to modify it will throw an exception.
   */
  private boolean inUse = false;

  /**
   * Constructs a table that accepts implementations of type V and sets the
   * initial number of {@link HashMap} buckets.
   */
  public SymbolTable(TypeRef<V> typeRef, int numBuckets) {
    this.packages = new HashSet<>();
    this.table = new HashMap<>(numBuckets);
    this.typeRef = typeRef;
  }

  /**
   * Mark this table as being in use.
   */
  public void setInUse() {
    this.inUse = true;
  }

  /**
   * Register all symbols discovered in the given package.
   *
   * @param pkg  package to scan for implementations
   */
  public void register(Registry<V> pkg) {
    registerByClass(pkg);
  }

  public V get(String symbol) {
    return table.get(symbol);
  }

  /**
   * Override extraction of the symbol name from the given implementation.
   * @param implementation
   */
  public abstract void registerSymbol(Object implementation);

  /**
   * Maps a symbol to its value.
   */
  protected void put(String symbol, V value) {
    if (inUse) {
      throw new LessInternalException("Attempt to add a symbol after table in use.");
    }
    if (table.containsKey(symbol)) {
      throw new LessInternalException("A symbol named '" + symbol + "' is already registered!");
    }
    table.put(symbol, value);
  }

  /**
   * Registers a package of implementations.
   *
   * @param pkg  package to scan for implementations
   */
  private void registerByClass(Registry<V> pkg) {
    Class<?> cls = pkg.getClass();
    if (!packages.add(cls)) {
      throw new LessInternalException("package " + cls.getName() + " is already registered");
    }
    Field[] fields = pkg.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      Class<?> type = field.getType();
      if (type.equals(typeRef.type())) {
        field.setAccessible(true);
        try {
          registerSymbol(field.get(pkg));
        } catch (IllegalAccessException e) {
          throw new LessInternalException("Failed to register source " + pkg, e);
        }
      }
    }
  }

}
