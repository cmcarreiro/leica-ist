#ifndef __CDK15_AST_UNARYEXPRESSION_NODE_H__
#define __CDK15_AST_UNARYEXPRESSION_NODE_H__

#include <cdk/ast/expression_node.h>

namespace cdk {

  /** Class for describing unary operations. */
  class unary_operation_node: public expression_node {
    expression_node *_argument;

  public:
    unary_operation_node(int lineno, expression_node *arg) :
        expression_node(lineno), _argument(arg) {
    }

    expression_node *argument() {
      return _argument;
    }

  };

} // cdk

#endif
