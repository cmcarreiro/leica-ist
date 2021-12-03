#ifndef __CDK15_YY_FACTORY_H__
#define __CDK15_YY_FACTORY_H__

#include <cdk/basic_factory.h>
#include <cdk/yy_scanner.h>
#include <cdk/yy_parser.h>

namespace cdk {

  /**
   * This class implements the compiler factory for the byacc/flex compilers.
   */
  template<typename LexerType>
  class yy_factory: public basic_factory {

  protected:
    yy_factory(const std::string &language) :
        basic_factory(language) {
    }

  protected:
    /**
     * @param name name of the input file (for debug only)
     * @return parser object pointer
     * @see createCompiler
     */
    std::shared_ptr<basic_parser> create_parser() {
      return std::make_shared<yy_parser<LexerType>> (_language);
    }

    /**
     * Create a scanner object for the Compact language.
     * @param name name of the input file (for debug only)
     * @return scanner object pointer
     * @see createCompiler
     */
    std::shared_ptr<basic_scanner> create_scanner() {
      return std::make_shared<yy_scanner<LexerType>> (_language);
    }

  };

} // namespace cdk

#endif
