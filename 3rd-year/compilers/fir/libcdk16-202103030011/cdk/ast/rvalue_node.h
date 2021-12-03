#ifndef __CDK15_RVALUE_NODE_H__
#define __CDK15_RVALUE_NODE_H__

#include <cdk/ast/expression_node.h>
#include <cdk/ast/lvalue_node.h>

namespace cdk {

  /**
   * Class for describing left-values used as expressions (i.e., right-values).
   */
  class rvalue_node: public cdk::expression_node {
    lvalue_node *_lvalue;

  public:
    rvalue_node(int lineno, lvalue_node *lvalue) :
        cdk::expression_node(lineno), _lvalue(lvalue) {
    }

  public:
    cdk::lvalue_node *lvalue() {
      return _lvalue;
    }

    void accept(basic_ast_visitor *av, int level) {
      av->do_rvalue_node(this, level);
    }

  };

} // cdk

#endif
