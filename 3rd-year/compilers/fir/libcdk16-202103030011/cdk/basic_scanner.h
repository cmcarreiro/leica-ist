#ifndef __CDK15_BASIC_SCANNER_H__
#define __CDK15_BASIC_SCANNER_H__

#include <iostream>
#include <memory>
#include <string>

namespace cdk {

  class compiler;
  class basic_parser;

  class basic_scanner {

    // this is a horrible way of not messing up the memory
    // when getting a shared pointer to a static object
    struct null_deleter {
      void operator()(void* const) {
        /* do nothing! */
      }
    };

    friend class basic_parser;
    friend class compiler;

  protected:
    //! @var the language to be scanned
    const std::string _language = "";

    //! @var the compiler this scanner belongs to
    std::shared_ptr<compiler> _compiler = nullptr;

  protected:
    //! @var _istream is the input stream
    std::shared_ptr<std::istream> _istream = std::shared_ptr<std::istream>(&std::cin,
                                                                           null_deleter());

    //! @var _ostream is the output stream
    std::shared_ptr<std::ostream> _ostream = std::shared_ptr<std::ostream>(&std::cout,
                                                                           null_deleter());

    //! @var _estream is the error stream
    std::shared_ptr<std::ostream> _estream = std::shared_ptr<std::ostream>(&std::cerr,
                                                                           null_deleter());

  protected:
    basic_scanner(const std::string &language) :
        _language(language), _compiler(nullptr), _istream(
            std::shared_ptr<std::istream>(&std::cin, null_deleter())), _ostream(
            std::shared_ptr<std::ostream>(&std::cout, null_deleter())), _estream(
            std::shared_ptr<std::ostream>(&std::cerr, null_deleter())) {
      // EMPTY
    }

  public:
    //! How to destroy a scanner.
    virtual ~basic_scanner() {
      _compiler = nullptr;
      _istream = nullptr;
      _ostream = nullptr;
      _estream = nullptr;
    }

  public:
    std::shared_ptr<std::istream> input_stream() {
      return _istream;
    }
    void input_stream(std::shared_ptr<std::istream> istr) {
      _istream = istr;
      switch_streams();
    }
    void input_stream(std::istream &istr) {
      _istream = std::shared_ptr<std::istream>(&istr, null_deleter());
      switch_streams();
    }

    std::shared_ptr<std::ostream> output_stream() {
      return _ostream;
    }
    void output_stream(std::shared_ptr<std::ostream> ostr) {
      _ostream = ostr;
      switch_streams();
    }
    void output_stream(std::ostream &ostr) {
      _ostream = std::shared_ptr<std::ostream>(&ostr, null_deleter());
      switch_streams();
    }

    std::shared_ptr<std::ostream> error_stream() {
      return _estream;
    }
    void error_stream(std::shared_ptr<std::ostream> estr) {
      _estream = estr;
      switch_streams();
    }
    void error_stream(std::ostream &estr) {
      _estream = std::shared_ptr<std::ostream>(&estr, null_deleter());
      switch_streams();
    }

    std::shared_ptr<compiler> owner() {
      return _compiler;
    }

  private:
    // for setting the scanner's owner
    void set_owner(std::shared_ptr<compiler> compiler) {
      _compiler = compiler;
    }

  public:
    virtual void switch_streams() = 0;

    /**
     * Scanning algorithm: the compiler object stores the result.
     * @param compiler the compiler object this parser belongs to.
     * @return true if the operation is successful
     */
    virtual int scan() = 0;

    /**
     * Return current source line.
     */
    virtual int lineno() const = 0;

    /**
     * Output error message.
     */
    virtual void error(const std::string &message) const {
      *_estream << lineno() << ": " << message << std::endl;
    }

    /**
     * Output error message.
     */
    virtual void error(const char *const message) const {
      *_estream << lineno() << ": " << message << std::endl;
    }

  };

} // cdk

#endif
