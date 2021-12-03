#ifndef __CDK15_AST_TYPEDNODE_NODE_H__
#define __CDK15_AST_TYPEDNODE_NODE_H__

#include <cdk/ast/basic_node.h>
#include <cdk/types/types.h>
#include <memory>

namespace cdk {

  /**
   * Typed nodes store a type description.
   */
  class typed_node: public basic_node {
  protected:
    // This must be a pointer, so that we can anchor a dynamic
    // object and be able to change/delete it afterwards.
    std::shared_ptr<basic_type> _type;

  public:
    /**
     * @param lineno the source code line number corresponding to
     * the node
     */
    typed_node(int lineno) :
        basic_node(lineno), _type(nullptr) {
    }

    std::shared_ptr<basic_type> type() {
      return _type;
    }
    void type(std::shared_ptr<basic_type> type) {
      _type = type;
    }

    bool is_typed(typename_type name) const {
      return _type->name() == name;
    }

  };

} // cdk

#endif
