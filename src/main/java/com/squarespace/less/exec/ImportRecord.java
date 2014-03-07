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

import java.nio.file.Path;

import com.squarespace.less.model.Stylesheet;


/**
 * Associates an imported stylesheet with the 'onlyOnce' flag value set when it was imported.
 */
public class ImportRecord {

  private final Path exactPath;

  private final Stylesheet stylesheet;

  private final boolean onlyOnce;

  public ImportRecord(Path exactPath, Stylesheet stylesheet, boolean onlyOnce) {
    this.exactPath = exactPath;
    this.stylesheet = stylesheet;
    this.onlyOnce = onlyOnce;
  }

  public Path exactPath() {
    return exactPath;
  }

  public Stylesheet stylesheeet() {
    return stylesheet;
  }

  public boolean onlyOnce() {
    return onlyOnce;
  }

}
