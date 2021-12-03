#ifndef __CDK15_AST_SEQUENCE_NODE_H__
#define __CDK15_AST_SEQUENCE_NODE_H__

#include <vector>
#include <cdk/ast/basic_node.h>

namespace cdk {

  /**
   * Class representing a node sequence, for instance, in an
   * instruction block or in an argument list.
   */
  class sequence_node: public basic_node {

    typedef std::vector<basic_node*> sequence_type;
    sequence_type _nodes;

  public:
    /**
     * Constructor for the empty sequence.
     */
    sequence_node(int lineno) :
        basic_node(lineno) {
      // EMPTY
    }

    /**
     * Example: constructor for a left recursive production node:
     * <pre>
     * sequence: item {$$ = new Sequence(LINE, $1);}* | sequence item {$$ = new Sequence(LINE, $2, $1);}* </pre>
     * The constructor of a sequence node takes the same first two
     * arguments as any other node.
     * The third argument is the number of child nodes: this
     * argument is followed by the child nodes themselves. Note
     * that no effort is made to ensure that the given number of
     * children matches the actual children passed to the
     * function. <b>You have been warned...</b>
     *
     * @param lineno the source code line number that originated the node
     * @param item is the single element to be added to the sequence
     * @param sequence is a previous sequence (nodes will be imported)
     */
    sequence_node(int lineno, basic_node *item, sequence_node *sequence = nullptr) :
        basic_node(lineno) {
      if (sequence != nullptr)
        _nodes = sequence->nodes();
      _nodes.push_back(item);
    }

  public:
    /**
     * This is the destructor for sequence nodes. Note that this
     * destructor also causes the destruction of the node's
     * children.
     */
    ~sequence_node() {
      for (auto node : _nodes)
        delete node;
      _nodes.clear();
    }

  public:
    basic_node *node(size_t i) {
      return _nodes[i];
    }
    sequence_type &nodes() {
      return _nodes;
    }
    size_t size() {
      return _nodes.size();
    }

    /**
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    void accept(basic_ast_visitor *av, int level) {
      av->do_sequence_node(this, level);
    }

  };

} // cdk

#endif
