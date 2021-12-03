#ifndef __CDK15_AST_BINARY_OPERATION_NODE_H__
#define __CDK15_AST_BINARY_OPERATION_NODE_H__

#include <cdk/ast/expression_node.h>

namespace cdk {

  /**
   * Class for describing binary operations.
   */
  class binary_operation_node: public expression_node {
    expression_node *_left, *_right;

  public:
    /**
     * @param lineno source code line number for this node
     * @param left first operand
     * @param right second operand
     */
    binary_operation_node(int lineno, expression_node *left, expression_node *right) :
        expression_node(lineno), _left(left), _right(right) {
    }

    expression_node *left() {
      return _left;
    }
    expression_node *right() {
      return _right;
    }

  };

} // cdk

#endif
