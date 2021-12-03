#include <iostream>
#include <memory>
#include <string>
#include <cdk/compiler.h>
#include <cdk/basic_factory.h>

static void usage(const char *progname) {
  std::cerr << "Usage: " << std::endl;
  std::cerr << "\t" << progname
            << " [-O] [-g] [--tree] [--target output-format] [-o outfile] infile" << std::endl;
  std::cerr << " -h " << std::endl;
  std::cerr << "\t(if an option is specified multiple times, only the last one is considered)"
            << std::endl;
  exit(1);
}

//---------------------------------------------------------------------------

static void process_options(int argc, char *argv[], std::shared_ptr<cdk::compiler> compiler) {

#ifdef YYDEBUG
  extern int yydebug;
  yydebug = getenv("YYDEBUG") ? 1 : 0;
#endif

  compiler->extension("asm"); // default output extension/target: ASM

  std::string ifile = "", ofile = "";
  for (int ax = 1; ax < argc; ax++) {
    std::string option = argv[ax];
    if (option == "-h")
      usage(argv[0]);
    else if (option == "-O")
      compiler->optimize(true);
    else if (option == "-g")
      compiler->debug(true);
    else if (option == "--tree") {
      compiler->extension("xml");
    } else if (option == "--interpret") {
      compiler->extension("@@INTERPRET@@");
    } else if (option == "--target") {
      compiler->extension(argv[++ax]);
    } else if (option == "-o") {
      ofile = argv[++ax];
      size_t dot = ofile.find_last_of('.');
      if (dot != std::string::npos)
        compiler->extension(ofile.substr(dot + 1));
      compiler->ofile(ofile);
    } else {
      if (ifile == "") {
        compiler->ifile(ifile = argv[ax]);
      } else
        usage(argv[0]);
    }
  }

  if (compiler->extension() == "@@INTERPRET@@") {
    compiler->ofile("");
    return;
  }

  if (ofile == "" && ifile != "") {
    size_t dot = ifile.find_last_of('.');
    ofile = ifile.substr(0, dot) + "." + compiler->extension();
    compiler->ofile(ofile);
  }

}

//---------------------------------------------------------------------------

// determine language to compile from given name
static std::string language_name(const std::string &name) {
  // if name is dir1/dir2/compact-XML, extract "compact-XML"
  size_t ipos = name.find_last_of("/");
  if (ipos == std::string::npos)
    ipos = -1;
  std::string language = name.substr(ipos + 1);

  // if name is called compact-XML, extract "compact"
  size_t fpos = language.find_first_of("-.");
  language = language.substr(0, fpos);
  return language;
}

//---------------------------------------------------------------------------

int main(int argc, char *argv[]) {
  /* ====[ COMPILER INITIALIZATION ]==== */

  std::string language = language_name(argv[0]);

  cdk::basic_factory *factory = cdk::basic_factory::get_implementation(language);
  if (factory == nullptr) {
    std::cerr << "FATAL: No implementation available for language '" << language << "'. Exiting..."
              << std::endl;
    exit(1);
  }

  std::shared_ptr<cdk::compiler> compiler = factory->create_compiler();

  /* ====[ COMMAND LINE ARGUMENTS ]==== */
  process_options(argc, argv, compiler);

  /* ====[ SYNTACTIC ANALYSIS ]==== */
  if (compiler->parse() != 0 || compiler->errors() > 0) {
    std::cerr << "** Syntax errors in " << compiler->ifile() << std::endl;
    return 1;
  }

  /* ====[ SEMANTIC ANALYSIS ]==== */

  if (!compiler->evaluate()) {
    std::cerr << "** Semantic errors in " << compiler->ifile() << std::endl;
    return 1;
  }

  return 0;
}
