#ifndef __FIR_AST_RESTART_NODE_H__S
#define __FIR_AST_RESTART_NODE_H__S

#include <cdk/ast/basic_node.h>
#include <cdk/ast/integer_node.h>

namespace fir {

  class restart_node : public cdk::basic_node {
    int _value;
    int _level;

  public:
    restart_node(int lineno, int value, int level = 1) :
      cdk::basic_node(lineno), _value(value), _level(level) {
    }

  public:
    int value() {
      return _value;
    }

    int level() const {
      return _level;
    }

    void accept(basic_ast_visitor* sp, int level) {
      sp->do_restart_node(this, level);
    }

  };

} // fir

#endif
