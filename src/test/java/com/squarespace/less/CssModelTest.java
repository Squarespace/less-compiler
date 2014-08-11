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

package com.squarespace.less;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.less.exec.CssModel;
import com.squarespace.less.model.NodeType;


public class CssModelTest {

  @Test
  public void testBasic() {
    LessOptions opts = new LessOptions(true);
    CssModel model = new CssModel(new LessContext(opts));

    model.push(NodeType.MEDIA);
    model.header("@media foo and bar");
    model.value("font-size:10px");
    model.value("color:#fff");

    model.push(NodeType.RULESET);
    model.header(".foo");
    model.header(".bar");
    model.value("color:white");
    model.value("margin:12px");
    model.pop();

    model.value("background-color:black");
    model.pop();

    String expected = "@media foo and bar{font-size:10px;color:#fff;.foo,.bar{color:white;margin:12px}"
        + "background-color:black}";
    Assert.assertEquals(model.render(), expected);
  }

}
