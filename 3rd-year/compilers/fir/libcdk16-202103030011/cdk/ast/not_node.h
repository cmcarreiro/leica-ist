#ifndef __CDK15_AST_NOT_NODE_H__
#define __CDK15_AST_NOT_NODE_H__

#include <cdk/ast/unary_operation_node.h>

namespace cdk {

  /**
   * Class for describing the not operator
   */
  class not_node: public unary_operation_node {

  public:
    /**
     * @param lineno source code line number for this node
     * @param left first operand
     * @param right second operand
     */
    not_node(int lineno, expression_node *argument) :
        unary_operation_node(lineno, argument) {
    }

    /**
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    void accept(basic_ast_visitor *av, int level) {
      av->do_not_node(this, level);
    }

  };

} // cdk

#endif
