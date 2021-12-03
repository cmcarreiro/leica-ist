#ifndef __FIR_AST_WHILE_FINALLY_NODE_H__
#define __FIR_AST_WHILE_FINALLY_NODE_H__

#include <cdk/ast/expression_node.h>

namespace fir {

  /**
   * Class for describing while-do-finally nodes.
   */
  class while_finally_node : public cdk::basic_node {
    cdk::expression_node* _condition;
    cdk::basic_node* _doblock, * _finallyblock;

  public:
    inline while_finally_node(int lineno, cdk::expression_node* condition, cdk::basic_node* doblock, cdk::basic_node* finallyblock) :
      basic_node(lineno), _condition(condition), _doblock(doblock), _finallyblock(finallyblock) {
    }

  public:
    inline cdk::expression_node* condition() {
      return _condition;
    }
    inline cdk::basic_node* doblock() {
      return _doblock;
    }
    inline cdk::basic_node* finallyblock() {
      return _finallyblock;
    }

    void accept(basic_ast_visitor* sp, int level) {
      sp->do_while_finally_node(this, level);
    }

  };

} // fir

#endif
