#ifndef __CDK15_AST_GT_NODE_H__
#define __CDK15_AST_GT_NODE_H__

#include <cdk/ast/binary_operation_node.h>

namespace cdk {

  /**
   * Class for describing the greater-than operator
   */
  class gt_node: public binary_operation_node {
  public:
    /**
     * @param lineno source code line number for this node
     * @param left first operand
     * @param right second operand
     */
    gt_node(int lineno, expression_node *left, expression_node *right) :
        binary_operation_node(lineno, left, right) {
    }

    /**
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    void accept(basic_ast_visitor *av, int level) {
      av->do_gt_node(this, level);
    }

  };

} // cdk

#endif
