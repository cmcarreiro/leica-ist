#ifndef __FIR_AST_LEAVE_NODE_H__
#define __FIR_AST_LEAVE_NODE_H__

#include <cdk/ast/basic_node.h>
#include <cdk/ast/integer_node.h>

namespace fir {

  class leave_node : public cdk::basic_node {
    int _value;
    int _level;

  public:
    leave_node(int lineno, int value, int level = 1) :
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
      sp->do_leave_node(this, level);
    }

  };

} // fir

#endif
