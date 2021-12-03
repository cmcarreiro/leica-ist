#ifndef __CDK15_AST_INTEGER_NODE_H__
#define __CDK15_AST_INTEGER_NODE_H__

#include <cdk/ast/literal_node.h>

namespace cdk {

  /**
   * Class for describing syntactic tree leaves for holding integer values.
   */
  class integer_node: public virtual literal_node<int> {
  public:
    integer_node(int lineno, int i) :
        literal_node<int>(lineno, i) {
    }

    /**
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    void accept(basic_ast_visitor *av, int level) {
      av->do_integer_node(this, level);
    }

  };

} // cdk

#endif
