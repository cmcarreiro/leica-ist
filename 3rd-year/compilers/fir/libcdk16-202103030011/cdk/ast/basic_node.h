#ifndef __CDK15_AST_BASIC_NODE_H__
#define __CDK15_AST_BASIC_NODE_H__

#include <typeinfo>
#include <iostream>
#include "targets/basic_ast_visitor.h"

namespace cdk {

  /**
   * Class for describing AST nodes.
   * This is an abstract class and forms the root of the node hierarchy.
   * The node hierarchy is organized in a structure according to the
   * <i>Composite</i> design pattern: class <tt>node</tt> is the root
   * of the hierarchy; class <tt>simple</tt> is a template for leaves
   * holding simple (atomic) types; <tt>composite</tt> represents the
   * recursive structure. Note that other recursion classes are possible
   * by extending any of the classes in this family.
   */
  class basic_node {
    int _lineno; // source line

  protected:
    /**
     * Simple constructor.
     *
     * @param lineno the source code line number corresponding to the node
     */
    basic_node(int lineno) :
        _lineno(lineno) {
    }

  public:
    virtual ~basic_node() {
      // EMPTY
    }

  public:
    /** @return the line number of the corresponding source code */
    int lineno() const {
      return _lineno;
    }

    /**
     * @return the label of the node (i.e., it's class)
     */
    std::string label() const {
      std::string fullname = typeid(*this).name();
      int last = fullname.find_last_of("0123456789");
      return fullname.substr(last + 1, fullname.length() - last - 1 - 1);
    }

    /**
     * Every node must provide this method.
     *
     * @param av basic AST visitor
     * @param level syntactic tree level
     */
    virtual void accept(basic_ast_visitor *av, int level) = 0;

  };

} // cdk

#endif
