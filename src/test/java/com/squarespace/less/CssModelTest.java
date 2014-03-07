package com.squarespace.less;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.less.exec.CssModel;
import com.squarespace.less.model.NodeType;


public class CssModelTest {

  @Test
  public void testBasic() {
    Options opts = new Options(true);
    CssModel model = new CssModel(new Context(opts));

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

    String expected = "@media foo and bar{font-size:10px;color:#fff;.foo,.bar{color:white;margin:12px;}\n"
        + "background-color:black;}\n";
    Assert.assertEquals(model.render(), expected);
  }

}
