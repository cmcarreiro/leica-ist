#ifndef __CDK15_TARGETS_BASIC_AST_VISITOR_H__
#define __CDK15_TARGETS_BASIC_AST_VISITOR_H__

#include <iostream>

/* include node forward declarations */
#define __NODE_DECLARATIONS_ONLY__
#include "ast/all.h"  // automatically generated
#undef __NODE_DECLARATIONS_ONLY__

//!
//! This class is only for compiling the package: it will not be
//! installed. Specific compilers are supposed to define their own,
//! with the SAME NAME, but defining ALL processing functions
//! corresponding to their specific problem.
//!
class basic_ast_visitor {
  //! The output stream
  std::ostream &_os;

protected:
  //! Debug flag
  bool _debug;

protected:
  /**
   * Initialization of a semantic processor.
   *
   * @param os is the output stream to be used by the
   *        semantic processor.
   */
  basic_ast_visitor(std::ostream &os = std::cout, bool debug = false);

  /**
   * Return the current output stream.
   * @return an output stream.
   */
  std::ostream &os() {
    return _os;
  }

public:
  /**
   * How to destroy a semantic processor.
   */
  virtual ~basic_ast_visitor();

public:
  // do not edit these lines
#define __IN_VISITOR_HEADER__
#define __NODE_DECLARATIONS_ONLY__
#include <cdk/ast/visitor_decls.h>   // automatically generated
#undef __NODE_DECLARATIONS_ONLY__
#undef __IN_VISITOR_HEADER__
  // do not edit these lines: end

};

#endif
