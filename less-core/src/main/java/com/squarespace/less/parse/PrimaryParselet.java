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

package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.PRIMARY_SUB;

import java.nio.file.Path;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.ImportMarker;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Node;


public class PrimaryParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Block block = new Block();
    stm.execEnv().push(block);
    parseBlock(block, stm);
    stm.execEnv().pop();
    return block;
  }

  /**
   * Parse all rules that can exist as part of a block.
   *
   *  1. All imports with static paths are handled immediately by
   *     appending the imported nodes onto the current block.
   *
   *  2. All imports whose path requires variable interpolation, we
   *     mark the block as deferred and push it onto a list for
   *     evaluation once the parse completes.
   *
   * This avoids having to use a visitor to walk the entire tree
   * scanning for import nodes, which can result in a large
   * performance hit for deeply-nested styleheets with many
   * imports.
   *
   */
  private static void parseBlock(Block block, LessStream stm) throws LessException {
    Node node = null;
    stm.skipEmpty();

    // Save current stream position before parsing each primary rule type.
    Mark position = stm.mark();
    while ((node = stm.parse(PRIMARY_SUB)) != null) {
      // Assign stream position to successfully-parsed rule.
      node.setLineOffset(position.lineOffset);
      node.setCharOffset(position.charOffset);

      if (node instanceof Import) {
        Import importNode = (Import)node;
        Node pathNode = importNode.path();

        // If the import's path requires variable interpolation, defer
        // its evaluation until after the parse completes.
        if (pathNode.needsEval()) {
          stm.defer();
          block.appendNode(importNode);

        } else {
          evaluateImport(stm.context().importer(), stm.parser(), stm.execEnv(), block, importNode);
        }

      } else {
        block.appendNode(node);
      }

      stm.skipEmpty();
      stm.mark(position);
    }
  }

  /**
   * Performs an import by loading a new stream and in-lining the rules
   * onto the current block.  It uses a temporary block to hold the
   * rules as they are parsed, and then appends them to the current block.
   */
  public static boolean evaluateImport(Importer importer, LessParser parser, ExecEnv execEnv,
      Block parentBlock, Import importNode)
      throws LessException {

    Path path = importer.resolvePath(importNode);
    if (path == null) {
      parentBlock.appendNode(importNode);
      return false;
    }

    if (importer.shouldSuppressImport(path)) {
      // Append nothing, just return true indicating we consumed the import.
      return true;
    }

    // If features are attached to this import, we wrap its rules in a Media.
    Features features = importNode.features();
    boolean wrapMedia = (features != null && !features.isEmpty());
    if (wrapMedia) {
      // Create a MEDIA block and push it onto the stack. Imported
      // rules will be appended to the media block instead of the
      // parent block.
      Block mediaBlock = new Block();
      execEnv.push(mediaBlock);
      Media media = new Media(features, mediaBlock);
      parentBlock.appendNode(media);
      parentBlock = mediaBlock;
    }

    // When tracing mode is active, we wrap the boundaries of each
    // imported file with markers.
    boolean tracing = parser.context().options().tracing();
    if (tracing) {
      parentBlock.prependNode(new ImportMarker(importNode, true));
    }

    // Push a new stream onto the stack and parse it.
    importer.recordImport(importNode, path);
    String source = importer.loadSource(path);
    LessStream childStream = parser.push(source, path, execEnv);
    parseBlock(parentBlock, childStream);
    childStream.checkComplete();
    parser.pop();

    // Pop the media block, if any.
    if (wrapMedia) {
      execEnv.pop();
    }

    // In tracing mode, add the end marker for the import.
    if (tracing) {
      parentBlock.appendNode(new ImportMarker(importNode, false));
    }

    return true;
  }

}
