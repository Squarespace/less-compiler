package com.squarespace.less.parse;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Node;


/**
 * A fragment of parsing logic that can be composed into a full
 * parser.  Parselet instances should have no private data, using
 * only the LessStream interface and any static utility methods
 * required.
 *
 * Breaking up the parser this way is for two reasons:
 *
 *  1. Mirroring the same parser structure as less.js. This simplifies
 *     implementing the language and keeping it compatible with
 *     upstream.
 *
 *  2. Writing parser unit tests which can focus completely on
 *    a specific part of the syntax without requiring a lot of
 *    preamble, e.g. writing a full mixin when you just want to
 *    test the guard syntax.  This enables more efficient testing
 *    of many edge cases, improving coverage.
 */
public interface Parselet {

  Node parse(LessStream stm) throws LessException;

}
