#ifndef __CDK15_EMITTERS_POSTFIX_IX86_EMITTER_H__
#define __CDK15_EMITTERS_POSTFIX_IX86_EMITTER_H__

#include <sstream>
#include <iostream>
#include <iomanip>
#include <cctype>
#include <cdk/emitters/basic_postfix_emitter.h>

namespace cdk {

  /** Class postfix_ix86_emitter: emitter for yasm/nasm code.  */
  class postfix_ix86_emitter: public basic_postfix_emitter {

    int _lbl = 0;

    /** Method used to generate sequential labels. */
    std::string mklbl(int lbl, const std::string &context = "") {
      if (lbl < 0)
        return ".L" + std::to_string(-lbl) + context;
      return "_L" + std::to_string(lbl) + context;
    }

    void debug(const std::string &s) {
      if (basic_postfix_emitter::debug()) {
        os() << "; " << s << std::endl;
      }
    }

    template<typename Type>
    void debug(const std::string &s, const Type &value) {
      if (basic_postfix_emitter::debug()) {
        os() << "; " << s << " " << value << std::endl;
      }
    }

    //----------------------------------------------------------------------

  public:
    postfix_ix86_emitter(std::shared_ptr<compiler> &compiler) :
        basic_postfix_emitter(compiler) {
      // Ensure that double literals are generated
      // in accordance with NASM rules.
      os() << std::setiosflags(std::ios::showpoint);
    }

    //----------------------------------------------------------------------

  public:

    std::string mklabel(const std::string &context = "") {
      return mklbl(++_lbl, context);
    }

    //----------------------------------------------------------------------

  private:
    void __cmd0(std::string mnemonic) {
      os() << "\t" << mnemonic << std::endl;
    }
    template<typename T> void __cmd1(std::string mnemonic, T arg) {
      os() << "\t" << mnemonic << "\t" << arg << std::endl;
    }
    template<typename T1, typename T2> void __cmd2(std::string mnemonic, T1 arg1, T2 arg2) {
      os() << "\t" << mnemonic << "\t" << arg1 << ", " << arg2 << std::endl;
    }
    template<typename T> std::string _byte(T what) {
      std::ostringstream oss;
      oss << "byte " << what;
      return oss.str();
    }
    template<typename T> std::string _word(T what) {
      std::ostringstream oss;
      oss << "word " << what;
      return oss.str();
    }
    template<typename T> std::string _dword(T what) {
      std::ostringstream oss;
      oss << "dword " << what;
      return oss.str();
    }
    template<typename T> std::string _qword(T what) {
      std::ostringstream oss;
      oss << "qword " << what;
      return oss.str();
    }
    template<typename T> std::string _lbladdr(T what) {
      std::ostringstream oss;
      oss << "$" << what;
      return oss.str();
    }
    template<typename T> std::string _deref(T what) {
      std::ostringstream oss;
      oss << "[" << what << "]";
      return oss.str();
    }
    template<typename T> std::string _offset(T what, int offset) {
      std::ostringstream oss;
      if (offset < 0)
        oss << what << "-" << -offset;
      else if (offset > 0)
        oss << what << "+" << offset;
      else
        oss << what;
      return oss.str();
    }
    template<typename T> std::string _deref(T what, int offset) {
      return _deref(_offset(what, offset));
    }
    void _pop(std::string what) {
      __cmd1("pop", what);
    }
    void _push(std::string what) {
      __cmd1("push", what);
    }
    void _mov(std::string a, std::string b) {
      __cmd2("mov", a, b);
    }

    //!
    //! Arithmetic operations
    //!
    template<typename T> void _add(std::string a, T b) {
      __cmd2("add", a, b);
    }
    template<typename T> void _sub(std::string a, T b) {
      __cmd2("sub", a, b);
    }
    void _neg(std::string a) {
      __cmd1("neg", a);
    }
    void _imul(std::string a, std::string b) {
      __cmd2("imul", a, b);
    }

    /* Comparison */
    void _cmp(std::string a, std::string b) {
      __cmd2("cmp", a, b);
    }

    /* Bitwise operations */
    void _xor(std::string a, std::string b) {
      __cmd2("xor", a, b);
    }
    void _and(std::string a, std::string b) {
      __cmd2("and", a, b);
    }
    void _or(std::string a, std::string b) {
      __cmd2("or", a, b);
    }
    void _not(std::string a) {
      __cmd1("not", a);
    }

    /* Rotation and shift operations */
    void _rol(std::string a, std::string b) {
      __cmd2("rol", a, b);
    }
    void _ror(std::string a, std::string b) {
      __cmd2("ror", a, b);
    }
    void _sal(std::string a, std::string b) {
      __cmd2("sal", a, b);
    }
    void _sar(std::string a, std::string b) {
      __cmd2("sar", a, b);
    }
    void _shr(std::string a, std::string b) {
      __cmd2("shr", a, b);
    }

    /* Calls, jumps, etc. */
    void _call(std::string what) {
      __cmd1("call", what);
    }
    void _jmp(std::string what) {
      __cmd1("jmp", what);
    }

    /* Segments */
    void _segment(std::string what) {
      os() << "segment\t" << what << "\n";
    }

    /* Floating point */
    void _fild(std::string what) {
      __cmd1("fild", what);
    }
    void _fistp(std::string what) {
      __cmd1("fistp", what);
    }
    template<typename T> void _fld(T what) {
      __cmd1("fld", what);
    }
    void _fstp(std::string what) {
      __cmd1("fstp", what);
    }

    //----------------------------------------------------------------------

    // Implementation of the postfix interface

  public:

    // Segment selection

    void BSS() override {
      debug("BSS");
      _segment(".bss");
    }
    void DATA() override {
      debug("DATA");
      _segment(".data");
    }
    void RODATA() override {
      debug("RODATA");
      _segment(".rodata");
    }
    void TEXT() override {
      debug("TEXT");
      _segment(".text");
    }

  public:

    // Values (declaration in segments)

    void SBYTE(char value) override {
      debug("SBYTE", (int)value);
      __cmd1("db", (int)value);
    }
    void SSHORT(short value) override {
      debug("SSHORT", value);
      __cmd1("dw", value);
    }
    void SINT(int value) override {
      debug("SINT", value);
      __cmd1("dd", value);
    }
    void SFLOAT(float value) override {
      debug("SFLOAT", value);
      __cmd1("dd", value);
    }
    void SDOUBLE(double value) override {
      debug("SDOUBLE", value);
      __cmd1("dq", value);
    }
    void SSTRING(std::string value) override {
      debug("SSTRING", "(value omitted -- see below)");
      os() << "\tdb\t";
      for (size_t ix = 0; ix < value.length();) {
        if (isalnum(value[ix])) {
          os() << '"';
          while (isalnum(value[ix]))
            os() << value[ix++];
          os() << '"';
        } else {
          os() << (int)(unsigned char)value[ix++];
        }
        os() << ", ";
      }
      os() << "0" << std::endl;
    }
    void SALLOC(int value) override {
      debug("SALLOC", value);
      __cmd1("resb", value);
    }
    void SADDR(std::string label) override {
      debug("SADDR", label);
      __cmd1("dd", label);
    }

  public:

    // Labels

    void ALIGN() override {
      debug("ALIGN");
      os() << "align\t4\n";
    }
    void LABEL(std::string label) override {
      debug("LABEL", label);
      os() << label << ":\n";
    }
    void EXTERN(std::string label) override {
      debug("EXTERN", label);
      os() << "extern\t" << label << "\n";
    }
    void GLOBAL(const char *label, std::string type) override {
      GLOBAL(std::string(label), type);
    }
    void GLOBAL(std::string label, std::string type) override {
      debug("GLOBAL", label + ", " + type);
      os() << "global\t" << label << type << "\n";
    }

  public:

    // Addressing, Loading and Storing

    void ADDR(std::string label) override {
      debug("ADDR", label);
      _push(_dword(_lbladdr(label)));
    }
    void ADDRA(std::string label) override {
      debug("ADDRA", label);
      _pop("eax");
      _mov(_deref(_lbladdr(label)), "eax");
    }
    void ADDRV(std::string label) override {
      debug("ADDRV", label);
      _push(_dword(_deref(_lbladdr(label))));
    }
    void LOCAL(int offset) override {
      debug("LOCAL", offset);
      os() << "\tlea\teax, [ebp+" << offset << "]\n";
      _push("eax");
    }
    void LOCA(int offset) override {
      debug("LOCA", offset);
      _pop("eax");
      _mov(_deref("ebp", offset), "eax");
    }
    void LOCV(int offset) override {
      debug("LOCV", offset);
      _push(_dword(_deref("ebp", offset)));
    }

  public:

    // Load operations

    void LDBYTE() override {
      debug("LDBYTE");
      _pop("ecx");
      __cmd2("movsx", "eax", _byte(_deref("ecx")));
      _push("eax");
    }
    void LDSHORT() override {
      debug("LDSHORT");
      _pop("ecx");
      __cmd2("movsx", "eax", _word(_deref("ecx")));
      _push("eax");
    }
    void LDINT() override {
      debug("LDINT");
      _pop("eax");
      _push(_dword(_deref("eax")));
    }
    void LDFLOAT() override {
      debug("LDFLOAT");
      _pop("eax");
      _push(_dword(_deref("eax")));
    }
    void LDDOUBLE() override {
      debug("LDDOUBLE");
      _pop("eax");
      _push(_dword(_deref("eax", 4)));
      _push(_dword(_deref("eax")));
    }

  public:

    // Store operations

    void STBYTE() override {
      debug("STBYTE");
      _pop("ecx");
      _pop("eax");
      _mov(_deref("ecx"), "al");
    }
    void STSHORT() override {
      debug("STSHORT");
      _pop("ecx");
      _pop("eax");
      _mov(_deref("ecx"), "ax");
    }
    void STINT() override {
      debug("STINT");
      _pop("ecx");
      _pop("eax");
      _mov(_deref("ecx"), "eax");
    }
    void STFLOAT() override {
      debug("STFLOAT");
      _pop("ecx");
      _pop("eax");
      _mov(_deref("ecx"), "eax");
    }
    void STDOUBLE() override {
      debug("DSTORE");
      _pop("ecx");
      _pop("eax");
      _mov(_deref("ecx"), "eax");
      _pop("eax");
      _mov(_deref("ecx", 4), "eax");
    }

  public:

    // Simple Stack Operations

    void SP() override {
      debug("SP");
      _push("esp");
    }
    void ALLOC() override {
      debug("ALLOC");
      _pop("eax");
      _sub("esp", "eax");
    }
    void DUP32() override {
      debug("DUP32");
      _push(_dword(_deref("esp")));
    }
    void DUP64() override {
      debug("DUP64");
      //DAVID: purrfect hack
      SP();
      LDDOUBLE();
    }
    void SWAP32() override {
      debug("SWAP32");
      _pop("eax");
      _pop("ecx");
      _push("eax");
      _push("ecx");
    }
    void SWAP64() override {
      debug("SWAP64");
      _pop("eax");
      _pop("ebx");
      _pop("ecx");
      _pop("edx");
      _push("ebx");
      _push("eax");
      _push("edx");
      _push("ecx");
    }
    void INT(int value) override {
      debug("INT", value);
      _push(_dword(value));
    }
    void FLOAT(float value) override {
      debug("FLOAT", value);
      DOUBLE(value);
      D2F();
    }
    void DOUBLE(double value) override {
      debug("DOUBLE", value);
      auto label = mklabel("_cdk_emitter_internal");
      RODATA();
      ALIGN();
      LABEL(label);
      SDOUBLE(value);
      TEXT();
      ADDR(label);
      LDDOUBLE();
    }

  public:

    // Integer operations

    void NEG() override {
      debug("NEG");
      _neg(_dword(_deref("esp")));
    }
    void ADD() override {
      debug("ADD");
      _pop("eax");
      _add(_dword(_deref("esp")), "eax");
    }
    void SUB() override {
      debug("SUB");
      _pop("eax");
      _sub(_dword(_deref("esp")), "eax");
    }
    void MUL() override {
      debug("MUL");
      _pop("eax");
      _imul(_dword("eax"), _deref("esp"));
      _mov(_deref("esp"), "eax");
    }
    void DIV() override {
      debug("DIV");
      _pop("ecx");
      _pop("eax");
      __cmd0("cdq");
      __cmd1("idiv", "ecx");
      _push("eax");
    }
    void UDIV() override {
      debug("UDIV");
      _pop("ecx");
      _pop("eax");
      _xor("edx", "edx");
      __cmd1("idiv", "ecx");
      _push("eax");
    }
    void MOD() override {
      debug("MOD");
      _pop("ecx");
      _pop("eax");
      __cmd0("cdq");
      __cmd1("idiv", "ecx");
      _push("edx");
    }
    void UMOD() override {
      debug("UMOD");
      _pop("ecx");
      _pop("eax");
      _xor("edx", "edx");
      __cmd1("idiv", "ecx");
      _push("edx");
    }

  public:

    // Floating-point operations

    void DNEG() override {
      debug("DNEG");
      _fld(_qword(_deref("esp")));
      os() << "\tfchs\n";
      _fstp(_qword(_deref("esp")));
    }
    void DADD() override {
      debug("DADD");
      _fld(_qword(_deref("esp")));
      _add("esp", _byte(8));
      _fld(_qword(_deref("esp")));
      __cmd1("faddp", "st1");
      _fstp(_qword(_deref("esp")));
    }
    void DSUB() override {
      debug("DSUB");
      _fld(_qword(_deref("esp")));
      _add("esp", _byte(8));
      _fld(_qword(_deref("esp")));
      __cmd1("fsubrp", "st1");
      _fstp(_qword(_deref("esp")));
    }
    void DMUL() override {
      debug("DMUL");
      _fld(_qword(_deref("esp")));
      _add("esp", _byte(8));
      _fld(_qword(_deref("esp")));
      __cmd1("fmulp", "st1");
      _fstp(_qword(_deref("esp")));
    }
    void DDIV() override {
      debug("DDIV");
      _fld(_qword(_deref("esp")));
      _add("esp", _byte(8));
      _fld(_qword(_deref("esp")));
      __cmd1("fdivrp", "st1");
      _fstp(_qword(_deref("esp")));
    }

  public:

    // Increment and Decrement Operations

    void INCR(int value) override {
      debug("INCR", value);
      DUP32();
      _pop("eax");
      _add(_dword(_deref("eax")), value);
    }
    void DECR(int value) override {
      debug("DECR", value);
      DUP32();
      _pop("eax");
      _sub(_dword(_deref("eax")), value);
    }

  public:

    // Type Conversion Operations

    void D2F() override {
      debug("D2F");
      _fld(_qword(_deref("esp")));
      _add("esp", _byte(4));
      _fstp(_dword(_deref("esp")));
    }
    void F2D() override {
      debug("F2D");
      _fld(_dword(_deref("esp")));
      _sub("esp", _byte(4));
      _fstp(_qword(_deref("esp")));
    }
    void D2I() override {
      debug("D2I");
      _fld(_qword(_deref("esp")));
      _add("esp", _byte(4));
      _fistp(_dword(_deref("esp")));
    }
    void I2D() override {
      debug("I2D");
      _fild(_dword(_deref("esp")));
      _sub("esp", _byte(4));
      _fstp(_qword(_deref("esp")));
    }

  public:

    // Comparison instructions

    void EQ() override {
      debug("EQ");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      os() << "\tsete\tcl\n";
      _mov(_deref("esp"), "ecx");
    }
    void NE() override {
      debug("NE");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      os() << "\tsetne\tcl\n";
      _mov(_deref("esp"), "ecx");
    }

    void GT() override {
      debug("GT");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      os() << "\tsetg\tcl\n";
      _mov(_deref("esp"), "ecx");
    }
    void GE() override {
      debug("GE");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      os() << "\tsetge\tcl\n";
      _mov(_deref("esp"), "ecx");
    }
    void LE() override {
      debug("LE");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      os() << "\tsetle\tcl\n";
      _mov(_deref("esp"), "ecx");
    }
    void LT() override {
      debug("LT");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      os() << "\tsetl\tcl\n";
      _mov(_deref("esp"), "ecx");
    }

    void UGT() override {
      debug("UGT");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      __cmd1("seta", "cl");
      _mov(_deref("esp"), "ecx");
    }
    void UGE() override {
      debug("UGE");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      __cmd1("setae", "cl");
      _mov(_deref("esp"), "ecx");
    }
    void ULE() override {
      debug("ULE");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      __cmd1("setbe", "cl");
      _mov(_deref("esp"), "ecx");
    }
    void ULT() override {
      debug("ULT");
      _pop("eax");
      _xor("ecx", "ecx");
      _cmp(_deref("esp"), "eax");
      __cmd1("setb", "cl");
      _mov(_deref("esp"), "ecx");
    }

    void DCMP() override {
      debug("DCMP");
      _fld(_qword(_deref("esp")));
      _fld(_qword(_deref("esp", 8)));
      _add("esp", _byte(12));
      __cmd1("fsubrp", "st1");
      os() << "\tfxtract\n";
      __cmd1("ffree", "st1");
      _fistp(_dword(_deref("esp")));
    }

  public:

    // Bitwise Operations

    void NOT() {
      debug("NOT");
      _not(_dword(_deref("esp")));
    }
    void AND() {
      debug("AND");
      _pop("eax");
      _and(_dword(_deref("esp")), "eax");
    }
    void OR() {
      debug("OR");
      _pop("eax");
      _or(_dword(_deref("esp")), "eax");
    }
    void XOR() {
      debug("XOR");
      _pop("eax");
      _xor(_dword(_deref("esp")), "eax");
    }

  public:

    // Rotation and Shift Operations

    void ROTL() override {
      debug("ROTL");
      _pop("ecx");
      _rol(_dword(_deref("esp")), "cl");
    }
    void ROTR() override {
      debug("ROTR");
      _pop("ecx");
      _ror(_dword(_deref("esp")), "cl");
    }
    void SHTL() override {
      debug("SHTL");
      _pop("ecx");
      _sal(_dword(_deref("esp")), "cl");
    }
    void SHTRU() override {
      debug("SHTRU");
      _pop("ecx");
      _shr(_dword(_deref("esp")), "cl");
    }
    void SHTRS() override {
      debug("SHTRS");
      _pop("ecx");
      _sar(_dword(_deref("esp")), "cl");
    }

  public:

    // Starting a function

    void ENTER(size_t bytes) override {
      debug("ENTER", bytes);
      _push("ebp");
      _mov("ebp", "esp");
      _sub("esp", bytes);
    }
    void START() override {
      debug("START");
      _push("ebp");
      _mov("ebp", "esp");
    }

  public:

    // Leaving a function

    void STFVAL32() override {
      debug("STFVAL32");
      _pop("eax");
    }
    void STFVAL64() override {
      debug("STFVAL64");
      _fld(_qword(_deref("esp")));
      _add("esp", _byte(8));
    }

    void LEAVE() override {
      debug("LEAVE");
      os() << "\tleave\n";
    }

    void RET() override {
      debug("RET");
      os() << "\tret\n";
    }
    void RETN(int bytes) override {
      debug("RETN", bytes);
      os() << "\tret\t" << bytes << "\n";
    }

  public:

    // Function calls

    void CALL(std::string label) override {
      debug("CALL", label);
      _call(label);
    }

    void TRASH(int bytes) override {
      debug("TRASH", bytes);
      _add("esp", bytes);
    }

    void LDFVAL32() override {
      debug("LDFVAL32");
      _push("eax");
    }
    void LDFVAL64() override {
      debug("LDFVAL64");
      _sub("esp", _byte(8));
      _fstp(_qword(_deref("esp")));
    }

  public:

    // Basic Jump Operations

    void JMP(std::string label) override {
      debug("JMP", label);
      _jmp(_dword(label));
    }
    void LEAP() override {
      debug("LEAP");
      _pop("eax");
      _jmp("eax");
    }
    void BRANCH() override {
      debug("BRANCH");
      _pop("eax");
      _call("eax");
    }

  public:

    // Conditional Jump Operations[edit]

    void JZ(std::string label) override {
      debug("JZ", label);
      _pop("eax");
      _cmp("eax", _byte(0));
      os() << "\tje\tnear " << label << "\n";
    }
    void JNZ(std::string label) override {
      debug("JNZ", label);
      _pop("eax");
      _cmp("eax", _byte(0));
      os() << "\tjne\tnear " << label << "\n";
    }

    void JEQ(std::string label) override {
      debug("JEQ", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tje near " << label << "\n";
    }
    void JNE(std::string label) override {
      debug("JNE", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjne near " << label << "\n";
    }

    void JGT(std::string label) override {
      debug("JGT", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjg near " << label << "\n";
    }
    void JGE(std::string label) override {
      debug("JGE", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjge near " << label << "\n";
    }
    void JLE(std::string label) override {
      debug("JLE", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjle near " << label << "\n";
    }
    void JLT(std::string label) override {
      debug("JLT", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjl near " << label << "\n";
    }

    void JUGT(std::string label) override {
      debug("JUGT", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tja near " << label << "\n";
    }
    void JUGE(std::string label) override {
      debug("JUGE", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjae near " << label << "\n";
    }
    void JULE(std::string label) override {
      debug("JULE", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjb near " << label << "\n";
    }
    void JULT(std::string label) override {
      debug("JULT", label);
      _pop("ecx");
      _pop("eax");
      _cmp("eax", "ecx");
      os() << "\tjbe near " << label << "\n";
    }

  public:

    // Other Operations

    void NIL() override {
      debug("NIL");
    }
    void NOP() override {
      debug("NOP");
      os() << "\tnop\n";
    }

  };

} // namespace cdk

#endif
