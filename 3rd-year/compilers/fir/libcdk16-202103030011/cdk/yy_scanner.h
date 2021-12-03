#ifndef __CDK15_YY_SCANNER_H__
#define __CDK15_YY_SCANNER_H__

#include <cdk/basic_scanner.h>

namespace cdk {

  /**
   * This class corresponds to the scanner as implemented by Flex.
   */
  template<typename LexerType>
  class yy_scanner: public basic_scanner {
    LexerType *_lexer = nullptr;

  public:
    yy_scanner(const std::string &language) :
        basic_scanner(language), _lexer(new LexerType(nullptr, nullptr)) {
    }

  public:
    LexerType *lexer() {
      return _lexer;
    }
    void lexer(LexerType *lexer) {
      _lexer = lexer;
      switch_streams();
    }

    /**
     * Update the scanner's input and output streams.
     * FIXME: &* is awful and probably bug-prone!
     */
    void switch_streams() {
      // &* is a robust way of getting very fragile raw pointers
      _lexer->switch_streams(&*input_stream(), &*output_stream());
    }

  public:

    /**
     * Scan the input.
     */
    int scan() {
      return _lexer->yylex();
    }

    /**
     * Return the current source line.
     */
    int lineno() const {
      return _lexer->lineno();
    }

  };

} // cdk

#endif
