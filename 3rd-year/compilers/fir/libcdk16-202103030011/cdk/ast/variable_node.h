#ifndef __CDK15_AST_VARIABLE_NODE_H__
#define __CDK15_AST_VARIABLE_NODE_H__

#include <cdk/ast/lvalue_node.h>
#include <string>

namespace cdk {

  /**
   * Class for describing syntactic tree leaves for holding variables..
   */
  class variable_node: public lvalue_node {
    std::string _name;

  public:
    variable_node(int lineno, const char *s) :
        lvalue_node(lineno), _name(s) {
    }
    variable_node(int lineno, const std::string &s) :
        lvalue_node(lineno), _name(s) {
    }
    variable_node(int lineno, const std::string *s) :
        lvalue_node(lineno), _name(*s) {
    }

  public:
    const std::string& name() const {
      return _name;
    }

    /**
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    void accept(basic_ast_visitor *av, int level) {
      av->do_variable_node(this, level);
    }

  };

} // cdk

#endif
