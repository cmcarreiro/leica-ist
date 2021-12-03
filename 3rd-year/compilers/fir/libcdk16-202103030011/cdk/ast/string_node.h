#ifndef __CDK15_AST_STRING_NODE_H__
#define __CDK15_AST_STRING_NODE_H__

#include <cdk/ast/literal_node.h>
#include <string>

namespace cdk {

  /**
   * Class for describing syntactic tree leaves for holding string
   * values.
   */
  class string_node: public virtual literal_node<std::string> {
  public:
    string_node(int lineno, const char *s) :
        literal_node<std::string>(lineno, s) {
    }
    string_node(int lineno, const std::string &s) :
        literal_node<std::string>(lineno, s) {
    }
    string_node(int lineno, const std::string *s) :
        literal_node<std::string>(lineno, *s) {
    }

    /**
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    void accept(basic_ast_visitor *av, int level) {
      av->do_string_node(this, level);
    }

  };

} // cdk

#endif
