/**
 * Copyright, 2015, Squarespace, Inc.
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

import static com.squarespace.less.parse.PrimaryParselet.evaluateImport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;


/**
 * Entry point for all LESS stylesheet parsing.
 */
public class LessParser {

  /**
   * Context for this parse.
   */
  private final LessContext context;

  /**
   * Maintains a stack of streams during the parse.
   */
  private final FlexList<LessStream> streams = new FlexList<>();

  /**
   * List of stream paths currently being parsed.
   */
  private final Set<Path> streamPaths = new HashSet<>();

  /**
   * Root block that this parser is populating.
   */
  private final Block rootBlock;

  /**
   * List of deferred blocks / closures for later evaluation.
   */
  private final List<Deferred> deferreds = new ArrayList<>();

  /**
   * Execution environment captured during the parse. It tracks
   * the current stack of all entered blocks at each point in
   * the parse.  This is used to save a closure around any
   * block which requires deferred evaluation.
   */
  private final ExecEnv parseEnv;

  /**
   * Construct a parser with the given context.
   */
  public LessParser(LessContext context) {
    this.context = context;
    this.rootBlock = new Block();
    this.parseEnv = new ExecEnv(context);
  }

  /**
   * Returns a stylesheet wrapping the parser's root block.
   */
  public Stylesheet stylesheet() {
    return context.nodeBuilder().buildStylesheet(rootBlock);
  }

  /**
   * Returns the context associated with this parser instance.
   */
  public LessContext context() {
    return this.context;
  }

  /**
   * Top-level parse entry point.  Parses the given string and file path
   * and appends it to the current global block.
   */
  public void parse(String raw, Path filePath) throws LessException {
    LessStream stream = push(raw, filePath, parseEnv);
    Block block = (Block)stream.parse(Parselets.PRIMARY);

    // Ensure the stream was completely consumed by the parser.
    stream.checkComplete();
    pop();

    // Evaluate all deferred blocks.
    evaluateDeferred();

    // Append the parsed block to the global block.  This allows
    // more than one independent parse populate the same global
    // stylesheet.
    rootBlock.appendBlock(block);
  }

  /**
   * Push a stream onto the stack, typically to process an {@link Import} statement.
   */
  public LessStream push(String raw, Path filePath, ExecEnv env) {
    LessStream stream = null;
    if (filePath == null) {
      stream = new LessStream(this, raw, null, env);
    } else {
      stream = new LessStream(this, raw, filePath, env);
    }
    this.streamPaths.add(filePath);
    this.streams.push(stream);
    return stream;
  }

  /**
   * Pops the current stream.
   */
  public LessStream pop() {
    LessStream stream = this.streams.pop();
    this.streamPaths.remove(stream.path());
    List<ExecEnv> envs = stream.deferreds();
    if (!envs.isEmpty()) {
      deferreds.add(new Deferred(envs, stream.raw()));
    }
    return stream;
  }

  /**
   * Carry out evaluation of parsed blocks with one or more children
   * which require evaluation.  These evaluations have been deferred.
   *
   * There are 2 types of imports encountered while parsing a stream:
   *  1. static path
   *  2. interpolated path
   *
   * Imports with static paths can be processed immediately during the
   * parse.
   *
   * Imports whose paths require variable interpolation need
   * to be deferred until the parse completes, since the variables they
   * reference may not have been parsed yet.
   */
  private void evaluateDeferred() throws LessException {
    while (!deferreds.isEmpty()) {
      List<Deferred> processing = new ArrayList<>(deferreds);
      deferreds.clear();

      for (Deferred deferred : processing) {
        for (ExecEnv env : deferred.envs) {
          evaluateDeferredClosure(deferred, env);
        }
      }
    }
  }

  /**
   * Evaluate a deferred block against its closure.  The closure is
   * the stack captured at the time the block was parsed.
   */
  private void evaluateDeferredClosure(Deferred deferred, ExecEnv env) throws LessException {
    // Get the block at the top of the closure stack.
    Block block = env.frames().last();

    // Clear the deferred evaluation flag.  This is because we will be
    // adding new imports to this block which may require variable interpolation.
    // Clearing the flag ensures that, if any interpolated imports are present,
    // this block will be deferred for evaluation again.
    block.clearDeferred();

    // Iterate over all rules in the block looking for nodes which require evaluation.
    FlexList<Node> rules = block.rules();
    for (int i = 0; i < rules.size(); i++) {
      Node node = rules.get(i);

      // Handle imports which require variable interpolation.
      if (node instanceof Import) {
        Import oldImport = (Import)node;

        // Evaluate the import's path against the closure and perform the import.
        Import newImport = new Import(oldImport.path().eval(env), oldImport.features(), oldImport.once());
        newImport.rootPath(oldImport.rootPath());
        newImport.fileName(oldImport.fileName());

        // Store all imported rules on a temporary block.
        Block tempBlock = new Block();
        try {

          // Perform the import.  This will append all imported rules to the
          // temporary block, returning true if the import was processed.
          if (evaluateImport(context.importer(), this, env, tempBlock, newImport)) {

            // Splice imported rules into block, replacing the import node.
            FlexList<Node> nested = tempBlock.rules();
            rules.splice(i, 1, nested);
            i += nested.size() - 1;

            // Ensure the variable cache gets rebuilt, so any variable lookups
            // see definitions added by the import.
            block.resetVariableCache();
          }
        } catch (LessException e) {
          throw ParseUtils.parseError(e, newImport.fileName(), deferred.raw, newImport.parseOffset());
        }
      }
    }
  }

  /**
   * Captures a list of closures for later evaluation
   */
  private static class Deferred {

    /**
     * The closures captured and deferred during the parse.
     */
    private final List<ExecEnv> envs;

    /**
     * Input source for the active stream when this closure was deferred.
     */
    private final String raw;

    public Deferred(List<ExecEnv> envs, String raw) {
      this.envs = envs;
      this.raw = raw;
    }

  }

}
