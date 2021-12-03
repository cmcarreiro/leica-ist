#ifndef __CDK15_AST_NEG_NODE_H__
#define __CDK15_AST_NEG_NODE_H__

#include <cdk/ast/unary_operation_node.h>

namespace cdk {

  /**
   * Class for describing the negation operator
   */
  class neg_node: public unary_operation_node {
  public:
    neg_node(int lineno, expression_node *arg) :
        unary_operation_node(lineno, arg) {
    }

    /**
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    void accept(basic_ast_visitor *av, int level) {
      av->do_neg_node(this, level);
    }

  };

} // cdk

#endif
