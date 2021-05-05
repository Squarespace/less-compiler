package com.squarespace.less.parse2;

import static com.squarespace.less.core.CharClass.CLASSIFIER;
import static com.squarespace.less.core.SyntaxErrorMaker.alphaUnitsInvalid;
import static com.squarespace.less.core.SyntaxErrorMaker.bug;
import static com.squarespace.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.less.core.SyntaxErrorMaker.general;
import static com.squarespace.less.core.SyntaxErrorMaker.incompleteParse;
import static com.squarespace.less.core.SyntaxErrorMaker.javascriptDisabled;
import static com.squarespace.less.core.SyntaxErrorMaker.mixedDelimiters;
import static com.squarespace.less.core.SyntaxErrorMaker.quotedBareLF;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.NodeBuilder;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.core.Constants;
import com.squarespace.less.match.Recognizer;
import com.squarespace.less.model.Alpha;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Argument;
import com.squarespace.less.model.Assignment;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockLike;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Feature;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.FunctionCall;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Operator;
import com.squarespace.less.model.Parameter;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Ratio;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Shorthand;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.UnicodeRange;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.Url;
import com.squarespace.less.model.ValueElement;
import com.squarespace.less.model.Variable;
import com.squarespace.less.parse.ParseUtils;
import com.squarespace.less.parse.Patterns;

/**
 * Parser for the LESS language.
 *
 * The parser is structured to enable parsing of individual fragments of syntax, to both modularize the parser and
 * facilitate focused bottom-up testing of the syntax.
 *
 * The code may seem long but this is intentional to minimize the amount of jumping around during debugging and tracing
 * through the code. It makes it much easier to locate a particular piece of parser logic and how it relates to the rest
 * of the parse.
 *
 * The previous parser was too spread out over many classes and wired together in a way that was difficult to trace and
 * created too many intermediate Java stack frames.
 *
 * This new parser uses an explicit stack to track nested blocks, avoiding Java recursion. Fragments of syntax are
 * parsed by methods which themselves may call other methods, but the call stack should be shallow, non-recursive, and
 * much easier to trace and debug.
 *
 *
 * TODO: BUGS from the old parser which we need to keep working until the issues are resolved in the source stylesheets:
 *
 * - BUG1: a '+' character before end of block scope
 * - BUG2: an unclosed '@media' directive at end of file, either EOF or '}' char
 * - BUG3: variable references followed immediately by parenthesis '@foo()' at the end of a rule
 */
public class LessParser {

  // TODO: investigate unifying MIXIN, MIXIN_CALL, RULESET prefix parsing to lower the amount of backtracking.
  // there is overlap between these fragments, as they all use a selector prefix, and MIXIN and MIXIN_CALL
  // both have optional parameters, e.g. if we parse ".mixin();" as a MIXIN, the tokens up to the ';'
  // also correspond to a MIXIN_CALL.  see if we can parse these 3 prefxes somewhat generally and when certain
  // key fragments are detected, specialize at the last minute.

  /**
   * Flag that indicates we've just passed a character that should be considered equivalent
   * to open space.  For example, if you parse the sequence "}foo {" this is equivalent
   * to parsing "} foo {", creating a descendant combinator before the "foo" selector.
   */
  private static final int FLAG_OPENSPACE = 1;

  private static final Anonymous ANON = new Anonymous();

  private static final String ASTERISK = "*";

  private static final String AMPERSAND = "&";

  /**
   * Comment node that indicates a comment was parsed or skipped over, and should be suppressed from the output.
   */
  private static final Comment DUMMY_COMMENT = new Comment("<ignore me>", false);

  /**
   * TODO: See BUG2
   */
  private static final Media DUMMY_MEDIA = new Media(new Features(), new Block());

  /**
   * Dummy representing the default global scope, used when parsing block node fragments.
   * This ensures the top-level block always has a value.
   */
  private static final BlockLike DUMMY_STYLESHEET = new BlockLike() {
    public NodeType type() {
      return NodeType.STYLESHEET;
    }
    public void add(Node node) {
      // Does nothing
    }
    public void append(Block block) {
      // Does nothing
    }
  };

  /*
   * TODO: - perhaps we can remove the flags variable by tracking the openspace state in a different way. that would
   * shrink the marker array to 3 and speed up creation and rollback of marks.
   */

  /**
   * Stack of nested blocks encountered during the parse.
   */
  private BlockLike[] blocks = new BlockLike[32];

  /**
   * Current block (top of the stack). It is accessed frequently lot.
   */
  private BlockLike block;

  /**
   * Block stack pointer.
   */
  private int b_ptr = 0;

  /**
   * Marks of parser position: position, line, column, and flags.
   */
  private int[][] marks = new int[16][4];

  /**
   * Marker stack pointer.
   */
  private int m_ptr = 0;

  /**
   * Context for the parse.
   */
  private final LessContext ctx;

  /**
   * Ignore all comments (except bang-prefixed).
   */
  private final boolean ignoreComments;

  /**
   * Node builder instance.
   */
  private final NodeBuilder builder;

  /**
   * Parent directory of file being parsed.
   */
  private final Path rootPath;

  /**
   * Name of file being parsed.
   */
  private final Path fileName;

  /**
   * Source string.
   */
  private final String raw;

  /**
   * Source string length.
   */
  private final int len;

  /**
   * Source string position.
   */
  private int pos;

  /**
   * Index of furthest position we've parsed successfully.
   */
  private int furthest;

  /**
   * Current line number.
   */
  private int line = 0;

  /**
   * Current column number.
   */
  private int column = 0;

  /**
   * Flags for controlling parser state.
   */
  private int flags = 0;

  /**
   * Start of a pattern match.
   */
  private int m_start = 0;

  /**
   * End of a pattern match.
   */
  private int m_end = 0;

  /**
   * Safe mode, which allows a small class of bugs from the legacy parser.
   */
  private boolean safe_mode = false;

  /**
   * Construct a parser for the given context and source string.
   */
  public LessParser(LessContext ctx, String source) {
    this(ctx, source, null, null);
  }

  /**
   * Construct a parser for the given context and source string.
   */
  public LessParser(LessContext ctx, String source, Path rootPath, Path fileName) {
    this.ctx = ctx;
    this.builder = ctx.nodeBuilder();
    this.raw = source;
    this.len = source.length();
    this.ignoreComments = ctx.options().ignoreComments();
    this.rootPath = rootPath;
    this.fileName = fileName;
  }

  /**
   * Make sure the stream was fully parsed, otherwise throw an error.
   */
  public void complete() throws LessException {
    ws();

    // Throw an error if the parse didn't complete.
    if (peek() != Chars.EOF) {
      throw parseError(new LessException(incompleteParse()));
    }
    // If we have an unclosed block in the stream, we'll hit EOF with something
    // on the stack.
    if (b_ptr != 0) {
      throw parseError(new LessException(incompleteParse()));
    }
    // Mark pointer != 0 is a parser bug.
    if (m_ptr != 0) {
      throw parseError(new LessException(bug("mark pointer != 0")));
    }
  }

  /**
   * Enable or disable safe mode. Safe mode allows a small set of bugs to occur in stylesheets.
   */
  public void safeMode(boolean flag) {
    this.safe_mode = flag;
  }

  /**
   * Display parser state.
   */
  @Override
  public String toString() {
    return "LessParser(pos=" + pos + ", len=" + len + ")";
  }

  /**
   * Push a block onto the stack, growing the stack space if needed.
   */
  private void push(BlockLike b) {
    if (b_ptr + 1 == blocks.length) {
      BlockLike[] old = blocks;
      blocks = new BlockNode[b_ptr * 2];
      System.arraycopy(old, 0, blocks, 0, b_ptr);
    }

    b_ptr++;
    block = b;
    blocks[b_ptr] = b;
  }

  /**
   * Pop a block off the stack.
   */
  private void pop() {
    b_ptr--;
    block = blocks[b_ptr];
  }

  /**
   * Set line and column offsets for the given node.
   */
  private <T extends Node> T setpos(int[] mark, T node) {
    if (node != null) {
      node.setLineOffset(mark[1]);
      node.setCharOffset(mark[2]);
    }
    return node;
  }

  /**
   * Create a mark in the stream that we can roll back to if necessary. We use a stack so we can mark the stream more
   * than once in nested code.
   *
   * We combine two ideas here to form an optimistic parsing strategy. We peek at a fragment of syntax to "sniff"
   * whether the current parsing direction has a high probability of success. On success we call unmark() commit the
   * current stream state. On failure we call restore() to restore the stream to its prior state.
   *
   * NOTE: every call to begin() must be paired with exactly one call to commit() or rollback() when the scope of the
   * begin() is exited.
   */
  private int[] begin() {
    if (m_ptr == marks.length) {
      int[][] old = marks;
      marks = new int[m_ptr * 2][4];
      System.arraycopy(old, 0, marks, 0, m_ptr);
    }
    int[] m = marks[m_ptr];
    m[0] = pos;
    m[1] = line;
    m[2] = column;
    m[3] = flags; // TODO: try to remove the flags, we currently only have one
    m_ptr++;
    return m;
  }

  /**
   * Drop the stream state associated with the last marked position.
   */
  private void commit() {
    m_ptr--;
  }

  /**
   * Restore the stream state to the last marked position.
   */
  private void rollback() {
    m_ptr--;
    int[] m = marks[m_ptr];
    pos = m[0];
    line = m[1];
    column = m[2];
    flags = m[3]; // TODO: try to remove the flags, we currently only have one
  }

  /**
   * Constructs a parse error and adds line number information.
   */
  public LessException parseError(LessException e) {
    return ParseUtils.parseError(e, fileName, raw, furthest);
  }

  /**
   * Parse the given fragment of LESS syntax.
   */
  public Node parse(LessSyntax syntax) throws LessException {
    if (syntax != LessSyntax.STYLESHEET) {
      push(DUMMY_STYLESHEET);
    }

    Node r = null;
    switch (syntax) {

      case ADDITION:
        r = addition();
        break;

      case ALPHA:
        r = alpha();
        break;

      case ASSIGNMENT:
        r = assignment();
        break;

      case COLOR:
        r = color();
        break;

      case COLOR_KEYWORD:
        r = color_keyword();
        break;

      case COMMENT:
        r = comment(true, false);
        break;

      case COMMENT_RULE:
        r = comment(true, true);
        break;

      case CONDITION:
        r = condition();
        break;

      case CONDITIONS:
        r = conditions();
        break;

      case DEFINITION:
        r = definition();
        break;

      case DIMENSION:
        r = dimension();
        break;

      case DIRECTIVE:
        Node directive = directive();
        r = directive;
        if (directive != null && directive != DUMMY_MEDIA) {
          NodeType type = directive.type();
          if (type == NodeType.MEDIA || type == NodeType.BLOCK_DIRECTIVE) {
            r = _parse((BlockNode) directive) ? directive : null;
          }
        }

        // Note that we don't handle '@import' here, just parse and return it
        break;

      case ELEMENT:
        r = element();
        break;

      case ELEMENT_SUB:
        r = element_sub();
        break;

      case EXPRESSION:
        r = expression();
        break;

      case EXPRESSION_LIST:
        r = expression_list();
        break;

      case FEATURES:
        r = features();
        break;

      case FONT:
        r = font();
        break;

      case FUNCTION_CALL:
        r = function_call();
        break;

      case GUARD:
        r = guard();
        break;

      case JAVASCRIPT:
        javascript();
        break;

      case KEYWORD:
        r = keyword();
        break;

      case MIXIN:
        Mixin mixin = mixin();
        r = _parse(mixin) ? mixin : null;
        break;

      case MIXIN_CALL:
        r = mixin_call();
        break;

      case MIXIN_CALL_ARGS:
        r = mixin_call_args();
        break;

      case MIXIN_PARAMS:
        r = mixin_params();
        break;

      case MULTIPLICATION:
        r = multiplication();
        break;

      case OPERAND:
        r = operand();
        break;

      case OPERAND_SUB:
        r = operand_sub();
        break;

      case PARAMETER:
        r = parameter();
        break;

      case QUOTED:
        r = quoted();
        break;

      case RATIO:
        r = ratio();
        break;

      case RULE:
        r = rule();
        break;

      case RULESET:
        Ruleset ruleset = ruleset();
        r = _parse(ruleset) ? ruleset : null;
        break;

      case SELECTOR:
        r = selector();
        break;

      case SELECTORS:
        r = selectors();
        break;

      case SHORTHAND:
        r = shorthand();
        break;

      case STYLESHEET:
        Stylesheet sheet = builder.buildStylesheet(new Block());
        r = _parse(sheet) ? sheet : null;
        break;

      case VARIABLE:
        r = variable(false);
        break;

      case VARIABLE_CURLY:
        r = variable(true);
        break;

      case UNICODE_RANGE:
        r = unicode_range();
        break;

      case URL:
        r = url(true);
        break;

      default:
        throw parseError(new LessException(bug("unsupported syntax fragment")));
    }

    if (syntax != LessSyntax.STYLESHEET) {
      pop();
    }

    // Confirm the parse is complete.
    complete();

    return r;
  }

  /**
   * Parse a block.
   */
  private boolean _parse(BlockNode top) throws LessException {
    if (top == null) {
      return false;
    }

    // STYLESHEET blocks are open, all other blocks are delimited by '{' .. '}'
    boolean delimited = top.type() != NodeType.STYLESHEET;

    // Push the block on top of the stack. This also sets the 'block' member variable.
    push(top);

    // Skip whitespace and add comments to the current block.
    ws_comments(true, true);

    // Loop until there are no more characters to inspect
    while (pos < len) {

      // Skip whitespace and add comments to the current block
      if (!ws_comments(true, true)) {
        break;
      }

      // Peek at the current character
      char c = raw.charAt(pos);
      switch (c) {

        case Chars.EOF: {
          // Check if we're in a delimited block.
          if (block.type() != NodeType.STYLESHEET) {
            throw parseError(new LessException(incompleteParse()));
          }
          return true;
        }

        case '}': {
          // Ensure we're inside a delimited block. The global scope is not closeable.
          if (block.type() == NodeType.STYLESHEET) {
            throw parseError(new LessException(general("unexpected '}' closing brace")));
          }

          // Pop the block off the stack
          pop();

          flags |= FLAG_OPENSPACE;

          // Move forward
          pos++;
          column++;
          continue;
        }

        case '@': {
          // Must be a DEFINITION, DIRECTIVE, or RULESET

          // TODO: we can combine these two to "sniff" for a definition while
          // falling back to parsing a directive.

          Definition def = definition();
          if (def != null) {
            block.add(def);
            continue;
          }

          Ruleset ruleset = ruleset();
          if (ruleset != null) {
            block.add(ruleset);
            push(ruleset);
            continue;
          }

          Node directive = directive();
          if (directive != null) {

            // TODO: see BUG2
            if (directive == DUMMY_MEDIA) {
              continue;
            }

            NodeType type = directive.type();

            if (type == NodeType.IMPORT) {

              // TODO: imports still add ~5 Java stack frames, see if we can shrink this.

              // TODO: make the parser re-entrant so it can simply append the imported nodes
              // directly to the current block.

              Node _import = ctx.importer().importStylesheet((Import) directive);

              // Check if the import node was resolved and produced a nested stylesheet.
              if (_import.type() == NodeType.BLOCK) {
                // The "@import" directive returns a block. We append its elements to the current block.
                this.block.append((Block) _import);

                // Do not append the import node itself.
                continue;
              }

              // Fall through ..
            }

            // Add the directive
            block.add(directive);

            // Check if we have a block directive and need to parse its nested block
            if (type == NodeType.MEDIA || type == NodeType.BLOCK_DIRECTIVE) {
              push((BlockLike) directive);
            }
            continue;
          }

          // If we're here, this is invalid LESS syntax.
          throw parseError(new LessException(incompleteParse()));
        }

        case '.':
        case '#': {
          // Possible MIXIN definition, MIXIN_CALL or RULESET start.

          // TODO: unify similarities between MIXIN and MIXIN_CALL? as there is some overlap in the syntax.
          // a declaration ".mixin()" is also a call, but we only know when we hit ';' or fail to hit '{'

          Mixin mixin = mixin();
          if (mixin != null) {
            block.add(mixin);
            push(mixin);
            continue;
          }

          Ruleset ruleset = ruleset();
          if (ruleset != null) {
            block.add(ruleset);
            push(ruleset);
            continue;
          }

          MixinCall call = mixin_call();
          if (call != null) {
            block.add(call);
            continue;
          }
          break;
        }

        default: {
          // Possible RULE
          Rule rule = rule();
          if (rule != null) {
            block.add(rule);
            continue;
          }

          // Must be a RULESET
          Ruleset ruleset = ruleset();
          if (ruleset != null) {
            block.add(ruleset);
            push(ruleset);
            continue;
          }

          // TODO: SEE BUG1
          if (safe_mode && bug1_plus_ending_block()) {
            continue;
          }

          // FALL THROUGH TO ERROR
        }
      }

      // TODO: explore the possibility of error recovery by moving ahead
      // in the stream to a valid synchronization point, like the next
      // ';' or '}' to enter a known state.

      // If we're here, the stylesheet contains invalid LESS syntax.
      throw parseError(new LessException(incompleteParse()));
    }

    if (!delimited) {
      pop();
    }

    return true;
  }

  /**
   * See BUG1
   */
  private boolean bug1_plus_ending_block() {
    if (peek() == '+') {
      next();
      ws();
      if (peek() == '}') {
        return true;
      }
    }
    return false;
  }

  /**
   * ADDITION
   *
   * Math operations plus and minus, with nested multiplication operations.
   */
  private Node addition() throws LessException {
    Node operation = multiplication();
    if (operation == null) {
      return null;
    }

    while (true) {
      // Skip whitespace
      if (!ws()) {
        break;
      }

      // Parse operator
      Operator operator = addition_op();
      if (operator == null) {
        break;
      }

      // Skip whitespace
      if (!ws()) {
        break;
      }

      // Parse right operand
      Node operand1 = multiplication();
      if (operand1 == null) {
        break;
      }

      operation = builder.buildOperation(operator, operation, operand1);
    }
    return operation;
  }

  /**
   * Parses an operator '+' or '-' for addition().
   */
  private Operator addition_op() {
    char c = peek();
    if (c != '+' && c != '-') {
      return null;
    }
    if (CLASSIFIER.whitespace(peek(pos + 1)) || !CLASSIFIER.whitespace(peek(pos - 1))) {
      next();
      return Operator.fromChar(c);
    }
    return null;
  }

  /**
   * ALPHA
   *
   * Matches the 'opacity=?' inside an 'alpha()' function call.
   */
  private Alpha alpha() throws LessException {
    if (!match("opacity=")) {
      return null;
    }
    consume(m_end);

    char c = peek();
    Node n = null;
    if (c == '@') {
      n = variable(false);
    } else {
      Dimension d = dimension();
      if (d != null && d.unit() != null) {
        throw parseError(new LessException(alphaUnitsInvalid(d)));
      }
      n = d;
    }

    ws();
    if (next() != ')') {
      if (n == null) {
        throw parseError(new LessException(expected("expected a unit-less number or variable for alpha")));
      }
      throw parseError(new LessException(expected("right parenthesis ')' to end alpha")));
    }
    return new Alpha(n == null ? ANON : n);
  }

  /**
   * ASSIGNMENT
   *
   * Assignments of the form 'key=val'.
   */
  private Assignment assignment() throws LessException {
    if (!match(Patterns.WORD)) {
      return null;
    }

    begin();
    consume(m_end);

    // Delay copying the string until confidence is high
    int ms = m_start;
    int me = m_end;

    if (!ws()) {
      rollback();
      return null;
    }

    if (next() != '=') {
      rollback();
      return null;
    }

    if (!ws()) {
      rollback();
      return null;
    }

    Node value = entity();
    if (value == null) {
      rollback();
      return null;
    }

    commit();
    String name = raw.substring(ms, me);
    return new Assignment(name, value);
  }

  /**
   * Skips whitespace and matches a right curly brace to open a block.
   *
   * Also sets the 'openspace' flag to indicate that an invisible space
   * exists just after the '{', so the sequence '{.foo' will be equivalent
   * to '{ .foo'.
   */
  private boolean block_open() {
    ws();
    if (peek() != '{') {
      return false;
    }
    next();
    flags |= FLAG_OPENSPACE;
    return true;
  }

  /**
   * COLOR
   *
   * Hexadecimal colors of the form '#123' or '#123456'.
   */
  private RGBColor color() {
    if (peek() != '#' || !match(Patterns.HEXCOLOR)) {
      return null;
    }
    String token = raw.substring(m_start, m_end);
    consume(m_end);

    return RGBColor.fromHex(token);
  }

  /**
   * COLOR_KEYWORD
   *
   * Named color values like 'red' or 'goldenrod'.
   */
  private RGBColor color_keyword() {
    if (!match(Patterns.KEYWORD)) {
      return null;
    }
    String token = raw.substring(m_start, m_end);
    RGBColor color = RGBColor.fromName(token);
    if (color == null) {
      return null;
    }
    consume(m_end);
    return color;
  }

  /**
   * COMMENT
   *
   * Parse and optionally ignore single line and Java-style block comments.
   *
   * If the 'keep' flag is true we construct and return the comment; otherwise
   * we just skip over the comment characters.
   *
   * The 'rulelevel' flag indicates the comment is at the same level as a RULE,
   * which changes its newline handling.
   */
  private Node comment(boolean keep, boolean rulelevel) {
    char c;
    int i = pos;
    if (i == len) {
      return null;
    }

    // Match '/'
    c = raw.charAt(i);
    if (c != '/') {
      return null;
    }

    i++;
    if (i == len) {
      return null;
    }

    // Match '*' or '/'
    c = raw.charAt(i);
    boolean isblock = c == '*';
    if (!isblock && c != '/') {
      return null;
    }

    i++;
    if (i == len) {
      // We've hit a comment start, but there are no additional chars to parse.
      // Example is a file ending in '//<eof>'.
      this.pos = i;
      this.column += 2;
      return DUMMY_COMMENT;
    }

    // We've found an unambiguous comment start, so start real parse
    this.column += 2;

    // We've definitely started parsing a comment, and there is no
    // turning back. We find the end or hit EOF, so we commit.
    pos = i;
    int end = len;

    // Comments that start with '!' are always retained in the output.
    c = raw.charAt(pos);
    boolean retain = c == '!';

    if (isblock) {
      // Block comments are ended by '*/'
      end = seek('*', '/');
      end -= 2;

//      if (end < len) {
//        end -= 2;
//      }

    } else {
      // Line comments end with '\n'
      while (pos < len) {
        c = raw.charAt(pos);
        if (c == '\n') {
          end = pos;
          pos++;
          this.line++;
          this.column = 0;
          break;

        } else {
          this.column++;
        }
        pos++;
      }
    }

    if (pos > furthest) {
      furthest = pos;
    }

    // TODO: look into whether some comments can be lifted. A rule-level comment
    // might be lifted to the block above the rule. For now we ignore them.

    flags |= FLAG_OPENSPACE;

    if (!keep) {
      return DUMMY_COMMENT;
    }

    if (ignoreComments && !retain) {
      return DUMMY_COMMENT;
    }

    String body = raw.substring(i, end);
    return builder.buildComment(body, isblock, rulelevel);
  }

  /**
   * CONDITIONS
   *
   * List of guard conditions joined by an "and".
   */
  private Condition conditions() throws LessException {
    Condition cond = condition();
    ws();
    while (match("and")) {
      consume(m_end);
      Condition sub = condition();
      cond = new Condition(Operator.AND, cond, sub, false);
      ws();
    }
    return cond;
  }

  /**
   * CONDITION
   *
   * Condition in a guard clause.
   */
  private Condition condition() throws LessException {
    ws();
    boolean negate = match("not");
    if (negate) {
      consume(m_end);
    }

    Condition res = null;
    ws();
    if (peek() != '(') {
      throw parseError(new LessException(expected("left parenthesis '(' to start guard condition")));
    }
    next();

    ws();
    Node left = condition_sub();
    if (left == null) {
      throw parseError(new LessException(expected("condition value")));
    }

    ws();
    if (match(Patterns.BOOL_OPERATOR)) {
      Operator op = Operator.fromString(raw.substring(m_start, m_end));
      consume(m_end);

      ws();
      Node right = condition_sub();
      if (right != null) {
        res = new Condition(op, left, right, negate);
      } else {
        throw parseError(new LessException(expected("expression")));
      }
    } else {
      res = new Condition(Operator.EQUAL, left, Constants.TRUE, negate);
    }

    ws();
    if (peek() != ')') {
      throw parseError(new LessException(expected("right parenthesis ')' to end guard condition")));
    }
    next();
    return res;
  }

  /**
   * Parses the left- or right-hand side value in a guard condition.
   */
  private Node condition_sub() throws LessException {
    Node n = addition();
    if (n != null) {
      return n;
    }
    n = keyword();
    if (n != null) {
      return n;
    }
    return quoted();
  }

  /**
   * DEFINITION
   *
   * Variable definition at RULE level, like '@myColor: red;'.
   */
  private Definition definition() throws LessException {
    int ms = pos;
    if (peek() != '@') {
      return null;
    }
    if (!match(Patterns.IDENTIFIER, pos + 1)) {
      return null;
    }

    // Mark start of rule.
    int[] mark = begin();

    consume(m_end);
    int me = m_end;

    // Skip whitespace and look for ':' definition delimiter
    ws();
    if (peek() != ':') {
      rollback();
      return null;
    }
    next();

    ws();

    // Mark start of value.
    begin();

    // TODO: this is quite similar to the parsing of the value of a rule()
    // but is tricky to unify at the moment. reorganize to share some code.

    Node value = expression_list();

    // Look for "!important" suffix. We have to strip it off, but don't
    // use it for definitions.
    ws();
    if (peek() == '!' && match(Patterns.IMPORTANT)) {
      consume(m_end);
    }

    boolean fallback = false;

    ws();
    if (!rule_end_peek()) {
      rollback();
      fallback = true;

// TODO: disabled for compatibility, but we may want this match here.
//      fallback = true;
//
//      // Roll back to value checkpoint
//      rollback();
//      if (match(Patterns.ANON_RULE_VALUE)) {
//        // Don't skip over the trailing char
//        consume(m_end - 1);
//        value = new Anonymous(raw.substring(m_start, m_end - 1).trim());
//      }

    } else if (value == null) {
      value = ANON;
    }

    ws();
    if (value != null && rule_end()) {
      if (!fallback) {
        commit();
      }

      // High confidence we have a valid definition, so copy the name
      String name = raw.substring(ms, me);

      flags |= FLAG_OPENSPACE;

      Definition result = setpos(mark, builder.buildDefinition(name, value == null ? ANON : value));
      result.fileName(fileName);
      commit();
      return result;
    }

    if (!fallback) {
      rollback();
    }
    rollback();
    return null;
  }

  /**
   * DIMENSION
   *
   * Numeric values with optional unit suffix, like '1.3' or '-12px'.
   */
  private Dimension dimension() {
    if (!match(Patterns.DIMENSION_VALUE)) {
      return null;
    }

    String value = raw.substring(m_start, m_end);
    consume(m_end);

    Unit unit = null;
    if (match(Patterns.DIMENSION_UNIT)) {

      // TODO: faster lookup for these without copying the substring

      unit = Unit.get(raw.substring(m_start, m_end));
      consume(m_end);
    }

    // TODO: builder
    return new Dimension(Double.parseDouble(value), unit);
  }

  /**
   * DIRECTIVE, BLOCK_DIRECTIVE, or MEDIA
   *
   * Handles single-line and block directives, and imports.
   */
  private Node directive() throws LessException {
    if (!match(Patterns.DIRECTIVE)) {
      return null;
    }

    int[] mark = begin();
    consume(m_end);

    String name = raw.substring(m_start, m_end);
    String nvname = name;

    // Look for '-' prefixes and remove them for matching
    if (name.charAt(1) == '-') {
      int i = name.indexOf('-', 2);
      if (i > 0) {
        nvname = "@" + name.substring(i + 1);
      }
    }

    boolean has_block = false;
    boolean hasexpr = false;
    boolean has_ident = false;

    switch (nvname) {
      case "@import":
      case "@import-once":
        ws();
        Node node = setpos(mark, directive_import(nvname));
        commit();
        return node;

      case "@media":
        ws();
        Media media = setpos(mark, directive_media());
        commit();
        return media;

      case "@font-face":
      case "@viewport":
      case "@top-left":
      case "@top-left-corner":
      case "@top-center":
      case "@top-right":
      case "@top-right-corner":
      case "@bottom-left":
      case "@bottom-left-corner":
      case "@bottom-center":
      case "@bottom-right":
      case "@bottom-right-corner":
      case "@left-top":
      case "@left-middle":
      case "@left-bottom":
      case "@right-top":
      case "@right-middle":
      case "@right-bottom":
        has_block = true;
        break;

      case "@page":
      case "@document":
      case "@supports":
      case "@keyframes":
        has_block = true;
        has_ident = true;
        break;

      case "@namespace":
        hasexpr = true;
        break;

      default:
        break;
    }

    if (has_ident) {
      ws();
      int start = pos;
      int end = pos;
      while (end < len) {
        char c = raw.charAt(end);
        if (c == '{') {
          break;
        }
        end++;
      }

      consume(end);
      name += " " + StringUtils.strip(raw.substring(start, end));
    }

    if (has_block) {
      ws();
      if (peek() == '{') {
        block_open();
        Node node = setpos(mark, builder.buildBlockDirective(name, new Block()));
        commit();
        return node;
      }

    } else {
      ws();
      Node value = hasexpr ? expression() : entity();
      ws();
      if (peek() == ';') {
        next();
        if (value != null) {
          Node node = setpos(mark, builder.buildDirective(name,  value));
          commit();
          return node;
        }
      }
    }

    rollback();
    return null;
  }

  /**
   * Completes parsing an '@import' directive with optional features.
   */
  private Node directive_import(String name) throws LessException {
    boolean once = name.endsWith("-once");
    Node path = quoted();
    if (path == null) {
      path = url(true);
    }
    if (path == null) {
      return null;
    }

    ws();
    Features features = features();

    ws();
    if (peek() == ';') {
      next();
      Import imp = new Import(path, features, once);
      imp.rootPath(rootPath);
      imp.fileName(fileName);
      return imp;
    }
    return null;
  }

  /**
   * Completes parsing a media directive with optional features.
   */
  private Media directive_media() throws LessException {
    Features features = features();

    ws();

    // Make sure a block follows, otherwise this is invalid.
    if (!block_open()) {
      // TODO: see BUG2
      return safe_mode ? DUMMY_MEDIA : null;
    }

    Media media = builder.buildMedia(features, new Block());
    media.fileName(fileName);
    return media;
  }

  /**
   * ELEMENT
   *
   * Single element in a selector.
   */
  private Element element() throws LessException {
    Combinator comb = element_combinator();
    ws();

    if (match(Patterns.ELEMENT0) || match(Patterns.ELEMENT1)) {
      consume(m_end);
      return new TextElement(comb, raw.substring(m_start, m_end));

    } else {
      // Look for bare '*' or '&'
      char ch = peek();
      if (ch == '*' || ch == '&') {
        next();
        return new TextElement(comb, ch == '*' ? ASTERISK : AMPERSAND);
      }
    }

    // See if we have an attribute
    Element elem = element_attr(comb);
    if (elem != null) {
      return elem;
    }

    if (match(Patterns.ELEMENT2) || match(Patterns.ELEMENT3)) {
      consume(m_end);
      return new TextElement(comb, raw.substring(m_start, m_end));

    } else {
      Node var = variable(true);
      if (var != null) {
        return new ValueElement(comb, var);
      }
    }

    Node node = element_sub();
    if (node != null) {
      return new ValueElement(comb, node);
    }
    return null;
  }

  /**
   * Element of an attribute selector.
   */
  private Element element_attr(Combinator comb) throws LessException {
    if (peek() != '[') {
      return null;
    }
    next();
    ws();

    Node key = null;
    if (match(Patterns.ATTRIBUTE_KEY)) {
      consume(m_end);
      key = new Anonymous(raw.substring(m_start, m_end));
    } else {
      key = quoted();
    }
    if (key == null) {
      return null;
    }

    AttributeElement elem = new AttributeElement(comb);
    elem.add(key);

    ws();
    if (match(Patterns.ATTRIBUTE_OP)) {
      consume(m_end);
      ws();

      Node oper = new Anonymous(raw.substring(m_start, m_end));
      Node val = quoted();
      if (val == null && match(Patterns.IDENTIFIER)) {
        consume(m_end);
        val = new Anonymous(raw.substring(m_start, m_end));
      }
      if (val != null) {
        elem.add(oper);
        elem.add(val);
      }
    }

    ws();
    if (peek() != ']') {

      // TODO: should we throw error here? after all we have left bracket

      return null;
    }
    next();
    return elem;
  }

  /**
   * ELEMENT_SUB
   *
   * Element nested in parenthesis.
   */
  private Node element_sub() throws LessException {
    ws();
    if (peek() != '(') {
      return null;
    }
    next();

    Node n = null;
    ws();
    if (peek() == '@') {
      n = variable(true);
      if (n == null) {
        n = variable(false);
      }
    } else {
      n = selector();
    }

    ws();
    if (n != null && peek() == ')') {
      next();
      return new Paren(n);
    }
    return null;
  }

  /**
   * Matches an element combinator, handling cases of defaulting for DESC combinators.
   */
  private Combinator element_combinator() {
    boolean block = (flags & FLAG_OPENSPACE) != 0;

    char prev = peekprev();

    // Skip whitespace and count chars
    int mark = pos;
    ws();
    int skipped = pos - mark;

    char ch = peek();
    if (CharClass.CLASSIFIER.combinator(ch)) {
      next();
      return Combinator.fromChar(ch);

    } else if (block || skipped > 0 || CharClass.CLASSIFIER.whitespace(prev) || prev == Chars.EOF || prev == ',') {
      return Combinator.DESC;
    }
    return null;
  }

  /**
   * ENTITY
   *
   * Literal, variable, function call, keyword, or comment.
   */
  private Node entity() throws LessException {
    Node n = null;

    n = literal();
    if (n != null) {
      return n;
    }
    n = variable(false);
    if (n != null) {
      return n;
    }
    n = function_call();
    if (n != null) {
      return n;
    }

    n = keyword();
    if (n != null) {
      return n;
    }

    // JavaScript is unsupported so this should throw an exception, but never return a value.
    javascript();

    return comment(true, false);
  }

// TODO: possibly in the future
//  private Node escape() {
//    if (peek() == '\\' && match(Patterns.ESCAPE, pos + 1)) {
//      consume(m_end);
//      return new Anonymous(raw.substring(m_start, m_end));
//    }
//    return null;
//  }

  /**
   * EXPRESSION
   *
   * List of elements separated by whitespace.
   */
  private Node expression() throws LessException {
    Node first = expression_sub();
    if (first == null) {
      return null;
    }

    List<Node> elements = null;
    Node elem = null;
    while (true) {
      if (!ws()) {
        break;
      }

      char c = peek();
      if (c == '/') {
        // Parse the slash but avoid parsing a comment
        c = peek(pos + 1);
        if (c != '/' && c != '*') {
          next();
          if (elements == null) {
            elements = new ArrayList<>();
            elements.add(first);
          }
          elements.add(new Anonymous("/"));
          ws();
        }
      }

      ws();
      elem = expression_sub();
      if (elem == null) {
        break;
      }
      if (elements == null) {
        elements = new ArrayList<>();
        elements.add(first);
      }
      elements.add(elem);
    }

    return elements == null ? first : new Expression(elements);
  }

  /**
   * One element in an expression.
   */
  private Node expression_sub() throws LessException {
    // TODO: peek to speed this up

    Node n = null;

    n = comment(true, false);
    if (n != null) {
      return n;
    }
    n = addition();
    if (n != null) {
      return n;
    }
    n = ratio();
    if (n != null) {
      return n;
    }
    n = color();
    if (n != null) {
      return n;
    }
    n = quoted();
    if (n != null) {
      return n;
    }
    n = unicode_range();
    if (n != null) {
      return n;
    }
    n = function_call();
    if (n != null) {
      return n;
    }

//    n = escape();
//    if (n != null) {
//      return n;
//    }

    n = keyword();
    if (n != null) {
      return n;
    }

    javascript();
    return null;
  }

  /**
   * EXPRESSION_LIST
   *
   * List of expressions separated by commas.
   */
  private Node expression_list() throws LessException {
    Node first = expression();
    if (first == null) {
      return null;
    }

    List<Node> elements = null;
    Node elem = null;
    while (ws()) {
      if (peek() != ',') {
        break;
      }
      next();
      ws();

      elem = expression();
      if (elem == null) {
        break;
      }
      if (elements == null) {
        elements = new ArrayList<>();
        elements.add(first);
      }
      elements.add(elem);
    }
    return elements == null ? first : new ExpressionList(elements);
  }

  /**
   * FEATURE
   *
   * One feature in an '@import' or '@media' directive.
   */
  private Expression feature() throws LessException {
    begin();

    Node n = feature_sub();
    if (n == null) {
      rollback();
      return null;
    }

    Expression expr = new Expression();
    while (n != null) {
      expr.add(n);
      ws();
      n = feature_sub();
    }
    commit();
    return expr;
  }

  /**
   * Mixed keyword and parenthesized features.
   */
  private Node feature_sub() throws LessException {
    begin();
    Node node = keyword();
    if (node != null) {
      commit();
      return node;
    }

    if (peek() == '(') {
      next();

      ws();
      Node prop = feature_property();

      ws();
      node = entity();

      ws();
      if (peek() == ')') {
        next();
        if (prop != null && node != null) {
          commit();
          return new Paren(new Feature(prop, node));
        } else if (node != null) {
          commit();
          return new Paren(node);
        }
      }
    }
    rollback();
    return null;
  }

  /**
   * Property inside a feature.
   */
  private Node feature_property() {
    begin();
    if (match(Patterns.PROPERTY)) {
      consume(m_end);
      ws();
      if (peek() == ':') {
        commit();
        next();
        String token = raw.substring(m_start, m_end);
        return new Property(token);
      }
    }
    rollback();
    return null;
  }

  /**
   * FEATURES
   *
   * List of features in an '@import' or '@media' directive.
   */
  private Features features() throws LessException {
    Node n = feature();
    if (n == null) {
      n = variable(false);
    }
    if (n == null) {
      return null;
    }

    Features features = new Features();
    while (n != null) {
      features.add(n);
      ws();
      if (peek() != ',') {
        break;
      }
      next();

      ws();
      n = feature();
      if (n == null) {
        n = variable(false);
      }
    }

    return features;
  }

  /**
   * FONT
   *
   * Special syntax for 'font' rules.
   */
  private Node font() throws LessException {
    List<Node> expr = new ArrayList<>(2);
    Node n = font_sub();
    while (n != null) {
      expr.add(n);
      ws();
      if (peek() == '/') {
        char c = peek(pos + 1);
        // Ensure this is not a comment start.
        if (c != '*' && c != '/') {
          next();
          expr.add(new Anonymous("/"));
        }
      }
      n = font_sub();
    }

    ExpressionList value = new ExpressionList(new Expression(expr));
    ws();
    if (peek() == ',') {
      next();
      ws();
      n = expression();
      while (n != null) {
        value.add(n);
        ws();
        if (peek() != ',') {
          break;
        }
        next();
        ws();
        n = expression();
      }
    }
    return value;
  }

  /**
   * Shorthand or entity inside a font rule.
   */
  private Node font_sub() throws LessException {
    ws();
    Node n = shorthand();
    if (n != null) {
      return n;
    }
    return entity();
  }

  /**
   * FUNCTION_CALL
   *
   * LESS or CSS function call syntax, e.g. alpha(opacity=1), url(http://example.com), etc.
   */
  private Node function_call() throws LessException {
    // TODO: drop the recognizer and match identifier, ws, then paren

    if (!match(Patterns.CALL_NAME)) {
      return null;
    }

    begin();
    consume(m_end);

    String name = raw.substring(m_start, m_end - 1).toLowerCase();
    if (name.equals("url")) {
      commit();
      return url(false);
    }
    if (name.equals("alpha")) {
      Node value = alpha();
      if (value != null) {
        commit();
        return value;
      }

      // Fall through
    }

    FunctionCall call = new FunctionCall(name);
    while (ws()) {
      Node value = assignment();
      if (value == null) {
        value = expression();
      }
      if (value != null) {
        call.add(value);
      }

      ws();
      if (peek() != ',') {
        break;
      }
      next();
    }

    if (next() != ')') {
      rollback();
      return null;
    }

    commit();
    return call;
  }

  /**
   * GUARD
   *
   * Guard clause on a mixin definition.
   */
  private Guard guard() throws LessException {
    if (!match("when")) {
      return null;
    }
    consume(m_end);

    Guard guard = new Guard();
    Condition condition = null;
    while (true) {
      ws();
      condition = conditions();
      if (condition == null) {
        break;
      }
      guard.add(condition);

      ws();
      if (peek() != ',') {
        break;
      }
      next();
    }

    // TODO: should we ignore empty guards? old parser did.

    return guard;
  }

  /**
   * JAVASCRIPT
   *
   * Inline JavaScript is unsupported, so this just throws an error.
   */
  private void javascript() throws LessException {
    int i = pos;
    if (peek() == '~') {
      i++;
    }
    if (peek(i) == '`') {
      throw parseError(new LessException(javascriptDisabled()));
    }
  }

  /**
   * KEYWORD
   *
   * Plain or color keywords.
   */
  private Node keyword() {
    if (!match(Patterns.KEYWORD)) {
      return null;
    }
    String token = raw.substring(m_start, m_end);
    consume(m_end);

    // TODO: named color lookup can be done fast without copying the token

    Node color = RGBColor.fromName(token);
    return color == null ? new Keyword(token) : color;
  }

  /**
   * LITERAL
   *
   * Quoted string, unicode range, ratio, dimension, or color.
   */
  public Node literal() throws LessException {
    Node node;

    // peek quoted
    char c = peek();
    if (c == '~' || c == '\'' || c == '"') {
      return quoted();
    }

    if (c == 'U') {
      node = unicode_range();
      if (node != null) {
        return node;
      }
    }

    node = ratio();
    if (node != null) {
      return node;
    }

    node = dimension();
    if (node != null) {
      return node;
    }

    return color();
  }

  /**
   * MIXIN
   *
   * Subroutine definition with optional parameters.
   */
  private Mixin mixin() throws LessException {
    if (!match(Patterns.MIXIN_NAME)) {
      return null;
    }

    int[] mark = begin();

    String name = raw.substring(m_start, m_end);
    consume(m_end);

    // Required parameters
    ws();
    MixinParams params = mixin_params();
    if (params == null) {
      rollback();
      return null;
    }

    // Optional guard clause
    ws();
    Guard guard = guard();

    // Must have a block open to be a valid mixin definition
    if (!block_open()) {
      rollback();
      return null;
    }

    // Definition is valid
    Mixin mixin = setpos(mark, builder.buildMixin(name, params, guard, new Block()));
    commit();
    return mixin;
  }

  /**
   * MIXIN_CALL
   *
   * A call to a mixin with optional arguments.
   */
  private MixinCall mixin_call() throws LessException {
    int[] mark = begin();

    Combinator comb = null;
    Selector selector = builder.buildSelector();
    while (match(Patterns.MIXIN_NAME)) {
      consume(m_end);

      String name = raw.substring(m_start, m_end);
      selector.add(new TextElement(comb, name));

      // Compute number of spaces skipped
      int here = pos;
      ws();
      int skipped = pos - here;

      // Determine combinator, if any
      if (peek() == '>') {
        next();
        comb = Combinator.CHILD;

      } else if (skipped > 0) {
        comb = Combinator.DESC;

      } else {
        comb = null;
      }
      ws();
    }

    // If we failed to parse a valid selector, bail out
    if (selector.isEmpty()) {
      rollback();
      return null;
    }

    // Parse mixin call arguments
    ws();
    MixinCallArgs args = mixin_call_args();

    // Set optional important flag
    ws();
    boolean important = match(Patterns.IMPORTANT);
    if (important) {
      consume(m_end);
    }

    // Loop for end of mixin call
    ws_comments(false, false);
    char c = peek();
    boolean semi = c == ';';
    if (semi || c == '}' || c == Chars.EOF) {
      if (semi) {
        next();
      }

      MixinCall call = setpos(mark, builder.buildMixinCall(selector, args, important));
      call.fileName(fileName);
      commit();
      return call;
    }

    // Didn't get a full match
    rollback();
    return null;
  }

  /**
   * MIXIN_CALL_ARGS
   *
   * Arguments to a mixin call.
   */
  private MixinCallArgs mixin_call_args() throws LessException {
    ws();
    if (peek() != '(') {
      return null;
    }
    next();

    MixinCallArgs argscomma = new MixinCallArgs(Chars.COMMA);
    MixinCallArgs argssemi = new MixinCallArgs(Chars.SEMICOLON);
    ExpressionList expr = new ExpressionList();
    String name = null;

    boolean delimsemi = false;
    boolean hasnamed = false;

    ws();
    Node n = expression();
    while (n != null) {
      String nameloop = null;
      Node value = n;

      if (n.type() == NodeType.VARIABLE) {
        Variable var = (Variable) n;
        Node temp = mixin_call_vararg();
        if (temp != null) {
          if (expr.size() > 0) {
            if (delimsemi) {
              throw parseError(new LessException(mixedDelimiters()));
            }
            hasnamed = true;
          }
          value = temp;
          nameloop = name = var.name();
        }
      }

      expr.add(value);
      argscomma.add(new Argument(nameloop, value));

      ws();
      char c = peek();
      if (c == ',') {
        next();
        ws();
        n = expression();
        continue;
      }

      // Detect whether args are semicolon-delimited
      if (c == ';') {
        next();
        delimsemi = true;

      } else if (!delimsemi) {
        ws();
        n = expression();
        continue;
      }

      // Handle semicolon-delimited arguments.
      if (hasnamed) {
        throw parseError(new LessException(mixedDelimiters()));
      }
      if (expr.size() > 1) {
        value = expr;
      }

      argssemi.add(new Argument(name, value));
      name = null;
      expr = new ExpressionList();
      hasnamed = false;

      ws();
      n = expression();
    }

    ws();
    if (peek() != ')') {
      throw parseError(new LessException(expected("right parenthesis ')' to end mixin call arguments")));
    }
    next();

    return delimsemi ? argssemi : argscomma;
  }

  /**
   * Completes parsing a named variable argument in a mixin call.
   */
  private Node mixin_call_vararg() throws LessException {
    if (peek() != ':') {
      return null;
    }
    next();
    ws();
    Node value = expression();
    if (value == null) {
      throw parseError(new LessException(expected("expression for named argument")));
    }
    return value;
  }

  /**
   * MIXIN_PARAMS
   *
   * Parameters for a mixin definition.
   */
  private MixinParams mixin_params() throws LessException {
    if (peek() != '(') {
      return null;
    }

    begin();
    next();

    MixinParams params = new MixinParams();
    while (true) {
      ws_comments(false, false);
      if (peek() == '.' && peek(pos + 1) == '.' && peek(pos + 2) == '.') {
        consume(pos + 3);
        params.add(builder.buildParameter(null, true));
        break;
      }

      Parameter param = parameter();
      if (param == null) {
        break;
      }
      params.add(param);

      ws();
      char c = peek();
      if (c != ',' && c != ';') {
        break;
      }
      next();
    }

    ws();
    if (peek() != ')') {
      rollback();
      return null;
    }
    next();

    commit();
    return params;
  }

  /**
   * MULTIPLICATION
   *
   * Math operations multiply and divide applied to operands.
   */
  private Node multiplication() throws LessException {
    Node operation = operand();
    if (operation == null) {
      return null;
    }

    while (true) {
      ws();

      char c = peek();
      if (c != '*' && c != '/') {
        break;
      }

      // Avoid treating a comment as the start of a divide.
      char c2 = peek(pos + 1);
      if (c == '/' && (c2 == '*' || c2 == '/')) {
        return operation;
      }
      next();

      if (!ws()) {
        break;
      }
      Node operand1 = operand();
      if (operand1 == null) {
        break;
      }

      Operator operator = Operator.fromChar(c);
      operation = builder.buildOperation(operator, operation, operand1);
    }
    return operation;
  }

  /**
   * OPERAND
   *
   * An operand in a math operation.
   */
  private Node operand() throws LessException {
    boolean negate = false;
    char c = peek();
    if (c == '-') {
      c = peek(pos + 1);
      if (c == '@' || c == '(') {
        negate = true;
        next();
      }
    }

    Node node = null;
    switch (c) {
      case '(':
        // sub
        node = operand_sub();
        break;

      case '-':
      case '+':
      case '.':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        // must be a dimension start
        node = dimension();
        break;

      case '@':
        // must be a variable
        node = variable(false);
        break;

      default:
        // maybe function call
        node = function_call();
        if (node != null) {
          break;
        }

        // maybe color keyword
        node = color_keyword();
        if (node != null) {
          break;
        }

        // maybe color parselet
        node = color();
        break;
    }

    // TODO: possible for node to be null here?

    return negate ? builder.buildOperation(Operator.MULTIPLY, node, new Dimension(-1, null)) : node;
  }

  /**
   * OPERAND_SUB
   *
   * Parenthesized operand in a math operation.
   */
  private Node operand_sub() throws LessException {
    begin();

    // skip '('
    char c = next();
    if (c == '(') {
      if (!ws()) {
        rollback();
        return null;
      }

      Node node = expression();
      if (node != null) {
        if (!ws()) {
          rollback();
          return null;
        }

        c = next();
        if (c == ')') {
          commit();
          return node;
        }
      }
    }
    rollback();
    return null;
  }

  /**
   * PARAMETER
   *
   * Parameter in a mixin definition.
   *
   * We assume if we're here we have high confidence that we're inside a parameter list, so we don't bother marking the
   * stream, we just press forward.
   */
  private Parameter parameter() throws LessException {
    // Sniff to detect which type of parameter we have.
    char c = peek();

    // Check if this is a literal or a keyword
    if (c != '@') {
      // We have a literal or a keyword
      Node n = literal();
      if (n == null) {
        n = keyword();
      }
      return n == null ? null : builder.buildParameter(null, n);
    }

    // We have a named parameter
    Variable var = variable(false);
    if (var == null) {
      return null;
    }

    // Skip whitespace before parsing ':' or '...' sequence
    ws();

    // Colon indicates this parameter has a default value
    if (peek() == ':') {
      // Skip this char
      next();

      // Eat whitespace
      ws();

      // Value is parsed as an expression
      Node value = expression();
      if (value == null) {
        // TODO: errors
        throw parseError(new LessException(expected("an expression")));
      }
      return builder.buildParameter(var.name(), value);

    } else if (peek() == '.' && peek(pos + 1) == '.' && peek(pos + 2) == '.') {
      // Variadic parameter
      consume(pos + 3);
      return builder.buildParameter(var.name(), true);
    }

    // Just a plain named parameter
    return builder.buildParameter(var.name());
  }

  /**
   * QUOTED
   *
   * Quoted string, optionally escaped, with variable interpolation.
   */
  private Quoted quoted() throws LessException {
    boolean escaped = false;
    int p = pos;

    // Peek to see if we have a quoted string
    char c = peek();
    if (c == '~') {
      escaped = true;
      p++;
    }

    // Check for a single or double-quote delimiter
    char delim = peek(p);
    if (delim != '\'' && delim != '"') {
      return null;
    }
    p++;

    // Update the stream pointer and consume the characters.
    consume(p);

    // TODO: possible to avoid constructing this list if the quoted
    // contains a single part?

    // Array of parts (variable references or plain text)
    List<Node> parts = new ArrayList<>();

    // Temporary buffer for accumulating characters
    StringBuilder buf = new StringBuilder();

    // TODO: restructure so we peek until reaching an '@', delimiter, '\n'

    while (pos < len) {
      c = raw.charAt(pos);

      // Look for an embedded variable reference.
      if (c == '@') {
        Node var = variable(true);
        if (var != null) {
          if (buf.length() > 0) {
            parts.add(new Anonymous(buf.toString()));
            buf.setLength(0);
          }
          parts.add(var);
          continue;
        }
      }

      // Not a variable, so continue
      pos++;
      column++;

      // Check if we're at the end of the string
      if (c == delim) {
        break;
      }

      // TODO: should '\r' also be illegal?

      // Bare linefeeds are illegal
      if (c == '\n') {
        // TODO: errors
        throw parseError(new LessException(quotedBareLF()));
      }

      buf.append(c);
      if (c == '\\' && pos < len) {
        c = raw.charAt(pos);
        buf.append(c);
        pos++;
        column++;
      }
    }

    // Append any remaining characters
    if (buf.length() > 0) {
      parts.add(new Anonymous(buf.toString()));
    }
    return new Quoted(delim, escaped, parts);
  }

  /**
   * RATIO
   *
   * Ratio syntax, e.g. '1/3'.
   */
  private Ratio ratio() {
    if (!match(Patterns.RATIO)) {
      return null;
    }
    Ratio ratio = new Ratio(raw.substring(m_start, m_end));
    consume(m_end);
    return ratio;
  }

  /**
   * RULE
   *
   * Property / value pair, e.g. 'color: red;'.
   */
  private Rule rule() throws LessException {
    if (!match(Patterns.PROPERTY)) {
      return null;
    }

    int[] mark = begin();
    consume(m_end);

    ws();
    if (peek() != ':') {
      rollback();
      return null;
    }
    next();
    ws();

    // Mark start of value
    begin();

    String property = raw.substring(m_start, m_end);
    Node value = null;
    if (property.equals("font")) {
      // parse font
      value = font();
    } else {
      // parse expression list
      value = expression_list();
    }

    // Look for "!important" suffix.
    boolean important = false;
    ws();
    if (peek() == '!' && match(Patterns.IMPORTANT)) {
      important = true;
      consume(m_end);
    }

    boolean fallback = false;

    ws();
    if (!rule_end_peek()) {
      fallback = true;
      important = false;

      // Roll back to value checkpoint
      rollback();
      if (match(Patterns.ANON_RULE_VALUE)) {
        // Don't skip over the trailing char
        consume(m_end - 1);
        value = new Anonymous(raw.substring(m_start, m_end - 1).trim());
      }

    } else if (value == null) {
      value = ANON;
    }

    // TODO: flatten rule to use the 'property' as a string directly
    // we had it structured differently to support parsing of the property
    // separately in the old parser

    ws();
    if (value != null && rule_end()) {
      if (!fallback) {
        commit();
      }

      flags |= FLAG_OPENSPACE;

      Rule rule = setpos(mark, builder.buildRule(new Property(property), value, important));
      rule.fileName(fileName);
      commit();
      return rule;
    }

    if (!fallback) {
      rollback();
    }
    rollback();
    return null;
  }

  /**
   * Peek at the end of the rule.
   */
  private boolean rule_end_peek() {
    char c = peek();
    return c == ';' || c == '}' || c == Chars.EOF;
  }

  /**
   * Find the end of the rule, consuming the ';' if exists.
   */
  private boolean rule_end() {
    char c = peek();
    if (c == ';') {
      next();
    } else if (c != '}' && c != Chars.EOF) {
      return false;
    }
    return true;
  }

  /**
   * RULESET
   *
   * Selectors and a nested set of rules.
   */
  private Ruleset ruleset() throws LessException {
    int[] mark = begin();
    Selectors group = selectors();
    if (group == null) {
      rollback();
      return null;
    }

    if (!block_open()) {
      rollback();
      return null;
    }

    Ruleset ruleset = setpos(mark, builder.buildRuleset(group, new Block()));
    ruleset.fileName(fileName);
    commit();
    return ruleset;
  }

  /**
   * SELECTORS
   *
   * List of selectors.
   */
  private Selectors selectors() throws LessException {
    Selector selector = selector();
    if (selector == null) {
      return null;
    }

    Selectors group = new Selectors();
    while (selector != null) {
      group.add(selector);
      ws_comments(false, false);
      if (peek() != ',') {
        break;
      }
      next();
      ws_comments(false, false);
      selector = selector();
    }
    return group;
  }

  /**
   * SELECTOR
   *
   * List of elements and combinators.
   */
  private Selector selector() throws LessException {
    ws();
    if (peek() == '(') {
      next();
      Node value = entity();
      ws();
      if (peek() == ')') {
        next();
        Selector selector = builder.buildSelector();
        selector.add(new ValueElement(Combinator.DESC, value));
        return selector;
      }
      return null;
    }

    Element elem = element();
    Selector selector = null;
    while (elem != null) {
      if (selector == null) {
        selector = builder.buildSelector();
      }
      selector.add(elem);
      ws_comments(false, false);

      if (CLASSIFIER.selectorEnd(peek())) {
        break;
      }

      elem = element();
    }
    return selector;
  }

  /**
   * SHORTHAND
   *
   * Special font shorthand syntax.
   */
  private Shorthand shorthand() throws LessException {
    if (!match(Patterns.SHORTHAND)) {
      return null;
    }
    begin();
    ws();
    Node left = entity();

    ws();
    if (next() != '/') {
      rollback();
      return null;
    }

    ws();
    Node right = entity();
    if (left == null || right == null) {
      rollback();
      throw parseError(new LessException(general("Shorthand pattern matched but failed to complete parse")));
    }

    commit();
    return new Shorthand(left, right);
  }

  /**
   * UNICODE_RANGE
   *
   * Range of fonts defined by a '@font-face'.
   */
  private UnicodeRange unicode_range() {
    if (peek() == 'U' && match(Patterns.UNICODE_DESCRIPTOR)) {
      consume(m_end);
      String token = raw.substring(m_start, m_end);
      return new UnicodeRange(token);
    }
    return null;
  }

  /**
   * URL
   *
   * A function-like syntax for a url, e.g. url(http://example.com).
   */
  private Url url(boolean matchfunc) throws LessException {
    if (matchfunc) {
      if (!match(Patterns.URLSTART)) {
        return null;
      }
      consume(m_end);
    }

    // Value can be a quoted or variable reference
    Node n = quoted();
    if (n == null) {
      n = variable(false);
    }

    if (n == null) {
      // Treat all characters before the closing ')' as the URL value
      int start = pos;
      char c = peek();
      while (c != ')' && c != Chars.EOF) {
        next();
        c = peek();
      }
      n = new Anonymous(raw.substring(start, pos).trim());
    }

    ws();
    if (next() != ')') {
      throw parseError(new LessException(expected("right parenthesis ')' to end url")));
    }
    return new Url(n);
  }

  /**
   * VARIABLE
   *
   * Either '@foo', '@@foo' indirect syntax, or both in curly form '@{foo}'.
   */
  private Variable variable(boolean curly) {
    int start = pos;
    if (peek(start) != '@') {
      return null;
    }
    start++;

    boolean indirect = false;
    if (peek(start) == '@') {
      indirect = true;
      start++;
    }

    if (curly) {
      if (peek(start) != '{') {
        return null;
      }
      start++;
    }

    if (!match(Patterns.IDENTIFIER, start)) {
      return null;
    }

    // Track the end of the parse
    int end = m_end;
    if (curly) {
      if (peek(m_end) != '}') {
        return null;
      }
      end++;
    }

    // TODO: see BUG3
    if (safe_mode && !curly && peek(end) == '(' && peek(end + 1) == ')' && peek(end + 2) == ';') {
      end += 2;
    }

    // TODO: avoid prepending variable '@', restructure code accordingly.
    // the tweak parser will need to also change how it constructs variables

    String name = '@' + raw.substring(m_start, m_end);
    if (indirect) {
      name = '@' + name;
    }

    // Finally, consume the characters from the stream
    consume(end);

    return builder.buildVariable(name, curly);
  }

  /**
   * Skip whitespace.
   *
   * The return value is true when there are characters left to parse. Hitting EOF will return false, indicating to the
   * caller that the parse must be aborted.
   */
  private boolean ws() {
loop:
    while (pos < len) {
      char c = raw.charAt(pos);
      switch (c) {

        // U+0009 TAB
        // U+000B LINE TAB
        // U+000C FORM FEED
        // U+000D CARRIAGE RETURN
        // U+0020 SPACE
        // U+00A0 NO-BREAK SPACE
        // U+FEFF ZERO WIDTH NO BREAK SPACE / BOM
        // All chars in Unicode category Zs "Space_Separator"
        case '\t':
        case '\u000b':
        case '\f':
        case '\r':
        case ' ':
        case '\u00a0':
        case '\u1680':
        case '\u180e':
        case '\u2000':
        case '\u2001':
        case '\u2002':
        case '\u2003':
        case '\u2004':
        case '\u2005':
        case '\u2006':
        case '\u2007':
        case '\u2008':
        case '\u2009':
        case '\u200a':
        case '\u2028':
        case '\u2029':
        case '\u202f':
        case '\u205f':
        case '\u3000':
        case '\ufeff':
          this.pos++;
          this.column++;
          break;

        case '\n':
          this.pos++;
          line++;
          column = 0;
          break;

        default:
          break loop;
      }
    }
    flags &= ~FLAG_OPENSPACE;
    if (pos > furthest) {
      furthest = pos;
    }
    return pos < len;
  }

  /**
   * Skip whitespace and comments.
   *
   * If the 'keep' comments flag is set, append the comments to the current block scope.
   */
  private boolean ws_comments(boolean keepcomments, boolean rulelevel) {
loop:
    while (pos < len) {
      char c = raw.charAt(pos);
      switch (c) {

        // U+0009 TAB
        // U+000B LINE TAB
        // U+000C FORM FEED
        // U+000D CARRIAGE RETURN
        // U+0020 SPACE
        // U+00A0 NO-BREAK SPACE
        // U+FEFF ZERO WIDTH NO BREAK SPACE / BOM
        // All chars in Unicode category Zs "Space_Separator"
        case '\t':
        case '\u000b':
        case '\f':
        case '\r':
        case ' ':
        case '\u00a0':
        case '\u1680':
        case '\u180e':
        case '\u2000':
        case '\u2001':
        case '\u2002':
        case '\u2003':
        case '\u2004':
        case '\u2005':
        case '\u2006':
        case '\u2007':
        case '\u2008':
        case '\u2009':
        case '\u200a':
        case '\u2028':
        case '\u2029':
        case '\u202f':
        case '\u205f':
        case '\u3000':
        case '\ufeff':
          this.pos++;
          this.column++;
          break;

        case ';':
          if (!rulelevel) {
            break loop;
          }
          // Skip extraneous semicolons at rule level
          this.pos++;
          this.column++;
          break;

        case '\n':
          this.pos++;
          line++;
          column = 0;
          break;

        // TODO: investigate whether comments should be lifted out into the enclosing block
        // there may be cases where a comment is placed between two syntax fragments that
        // have no space for the comment to fit. For example:
        //
        // .mixin(@a) /* bar */ when (@a > 1) { .. }
        //
        // We can lift this to produce this canonical result:
        //
        // /* bar */
        // .mixin(@a) when (@a > 1) { .. }

        case '/': {
          Node node = comment(keepcomments, rulelevel);
          if (node != null) {

            // A dummy comment indicates we successfully skipped over
            // a comment, but don't want to include it in the tree.
            if (node != DUMMY_COMMENT) {
              block.add(node);
            }

          } else {
            // The '/' is not part of a comment, so bail out
            break loop;
          }
          break;
        }

        default:
          // Found a non-whitespace non-comment start, bail out
          break loop;
      }
    }
    flags &= ~FLAG_OPENSPACE;
    if (pos > furthest) {
      furthest = pos;
    }
    return pos < len;
  }

  /**
   * Seek ahead to locate a 2-char sequence. A successful match will
   * position the stream pointer just past the 2nd char. A failure
   * will return 'len'.
   */
  private int seek(char c0, char c1) {
    while (pos < len) {
      char c = next();
      if (c == c0) {
        if (peek(pos) == c1) {
          next();
          return pos;
        }
      }
    }
    return len;
  }

  /**
   * Return the next character from the stream.
   */
  private char next() {
    if (pos < len) {
      char c = raw.charAt(pos);
      if (c == '\n') {
        line++;
        column = 0;
      } else {
        column++;
      }
      pos++;
      if (pos > furthest) {
        furthest = pos;
      }
      return c;
    }
    return Chars.EOF;
  }

  /**
   * Match a string literal.
   */
  private boolean match(String literal) {
    int ilen = literal.length();
    int i = 0;
    while (i < ilen) {
      int j = pos + i;
      if (j >= len) {
        return false;
      }
      char a = literal.charAt(i);
      char b = raw.charAt(j);
      if (a != b) {
        return false;
      }
      i++;
    }

    // Matched
    m_start = pos;
    m_end = pos + ilen;
    return true;
  }

  /**
   * Match the pattern and if successful set the start/end bounds of the match.
   */
  private boolean match(Recognizer r) {
    int i = r.match(raw, pos, len);
    boolean matched = i != -1;
    if (matched) {
      m_start = pos;
      m_end = i;
    }
    return matched;
  }

  /**
   * Match the pattern at 'start' and if successful set the start/end bounds of the match.
   */
  private boolean match(Recognizer r, int start) {
    int i = r.match(raw, start, len);
    boolean matched = i != -1;
    if (matched) {
      m_start = start;
      m_end = i;
    }
    return matched;
  }

  /**
   * Consume characters up to 'end' position.
   */
  private void consume(int end) {
    while (pos < end) {
      char c = raw.charAt(pos);
      if (c == '\n') {
        line++;
        column = 0;
      } else {
        column++;
      }
      pos++;
    }
    flags &= ~FLAG_OPENSPACE;
    if (pos > furthest) {
      furthest = pos;
    }
  }

  /**
   * Peek at the character in the stream.
   */
  private char peek() {
    return pos < len ? raw.charAt(pos) : Chars.EOF;
  }

  /**
   * Peek at previous character in the stream.
   */
  private char peekprev() {
    int p = pos - 1;
    return p >= 0 ? raw.charAt(p) : Chars.EOF;
  }

  /**
   * Peek at the character at the given position in the stream.
   */
  private char peek(int index) {
    return index < len ? raw.charAt(index) : Chars.EOF;
  }
}
