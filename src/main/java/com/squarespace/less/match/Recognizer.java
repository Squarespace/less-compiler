package com.squarespace.less.match;


public interface Recognizer {

  int match(CharSequence seq, int pos, int len);

}