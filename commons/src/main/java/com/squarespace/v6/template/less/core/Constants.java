package com.squarespace.v6.template.less.core;

import java.nio.charset.Charset;

import com.squarespace.v6.template.less.model.False;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Selectors;
import com.squarespace.v6.template.less.model.True;


public class Constants {

  public static final String NULL_PLACEHOLDER = "???";

  public static final True TRUE = new True();
  
  public static final False FALSE = new False();

  public static final Features EMPTY_FEATURES = new Features();
  
  public static final Selectors EMPTY_SELECTORS = new Selectors();
  
  // Placed here to avoid pulling in any classes which have slf4j loggers.
  public static final Charset UTF8 = Charset.forName("UTF-8");
  
}
