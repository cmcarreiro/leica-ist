#ifndef __CDK15_BASIC_FACTORY_H__
#define __CDK15_BASIC_FACTORY_H__

#include <map>
#include <memory>
#include <string>
#include <cdk/compiler.h>

namespace cdk {

  class basic_scanner;
  class basic_parser;
  class basic_target;

  /**
   * This is the main factory abstract class: it provides methods
   * for creating the lexical analyser and the compiler itself.
   * Instances of concrete subclasses will be created by the
   * "main" function to provide instances of the scanner and compiler
   * objects for a concrete language.
   *   - keys are language names
   *   - values are compiler factories
   */
  class basic_factory {
  protected:
    //! @var the language managed by this factory
    const std::string _language = "";

  private:
    static basic_factory *&factoriesByLanguage(const std::string &language) {
      static std::map<std::string, basic_factory*> factories;
      return factories[language];
    }

  public:
    static basic_factory *get_implementation(const std::string &language) {
      return factoriesByLanguage(language);
    }

  protected:
    basic_factory(const std::string &language) :
        _language(language) {
      factoriesByLanguage(language) = this;
    }

  public:
    /**
     * The superclass destructor does not do anything.
     * Probably, it would be a good idea to clean up the factories map.
     */
    virtual ~basic_factory() {
    }

  protected:
    /**
     * Create a scanner object for a given language.
     * @param name name of the input file (for debug only)
     * @return scanner object pointer
     * @see createCompiler
     */
    virtual std::shared_ptr<basic_scanner> create_scanner() = 0;

    /**
     * Create a parser object for a given language.
     * @param name name of the input file (for debug only)
     * @return parser object pointer
     * @see createCompiler
     */
    virtual std::shared_ptr<basic_parser> create_parser() = 0;

  public:
    /**
     * Create a compiler object for a given language.
     * @param name name of the language handled by the compiler
     * @return compiler object pointer
     */
    virtual std::shared_ptr<compiler> create_compiler() {
      std::shared_ptr<basic_scanner> scanner = create_scanner();
      std::shared_ptr<basic_parser> parser = create_parser();
      return compiler::create(_language, scanner, parser);
    }

  };

} // cdk

#endif
