#ifndef __CDK15_EMITTERS_DEBUGONLY_EMITTERS_H__
#define __CDK15_EMITTERS_DEBUGONLY_EMITTERS_H__

#include <cdk/emitters/basic_postfix_emitter.h>

namespace cdk {

  /**
   * Class postfix_debug_emitter: generate PF assembly (debug only).
   * @see basic_postfix_emitter
   */
  class postfix_debug_emitter: public basic_postfix_emitter {

  public:
    postfix_debug_emitter(std::shared_ptr<compiler> &compiler) :
        basic_postfix_emitter(compiler) {
    }

  public:
    void BSS() override {
      os() << "BSS\n";
    }
    void DATA() override {
      os() << "DATA\n";
    }
    void RODATA() override {
      os() << "RODATA\n";
    }
    void TEXT() override {
      os() << "TEXT\n";
    }

    void SBYTE(char value) override {
      os() << "SBYTE " << value << "\n";
    }
    void SSHORT(short value) override {
      os() << "SSHORT " << value << "\n";
    }
    void SINT(int value) override {
      os() << "SINT " << value << "\n";
    }
    void SFLOAT(float value) override {
      os() << "SFLOAT " << value << "\n";
    }
    void SDOUBLE(double value) override {
      os() << "SDOUBLE " << value << "\n";
    }
    void SSTRING(std::string value) override {
      os() << "SSTRING " << value << "\n";
    }
    void SALLOC(int value) override {
      os() << "SALLOC " << value << "\n";
    }
    void SADDR(std::string label) override {
      os() << "SADDR " << label << "\n";
    }

    void ALIGN() override {
      os() << "ALIGN\n";
    }
    void LABEL(std::string label) override {
      os() << "LABEL " << label << "\n";
    }
    void EXTERN(std::string label) override {
      os() << "EXTERN " << label << "\n";
    }
    void GLOBAL(const char *label, std::string type) override {
      GLOBAL(std::string(label), type);
    }
    void GLOBAL(std::string label, std::string type) override {
      os() << "GLOBAL " << label << " " << type << "\n";
    }

    void ADDR(std::string label) override {
      os() << "ADDR " << label << "\n";
    }
    void ADDRA(std::string label) override {
      os() << "ADDRA " << label << "\n";
    }
    void ADDRV(std::string label) override {
      os() << "ADDRV " << label << "\n";
    }
    void LOCAL(int offset) override {
      os() << "LOCAL " << offset << "\n";
    }
    void LOCA(int offset) override {
      os() << "LOCA " << offset << "\n";
    }
    void LOCV(int offset) override {
      os() << "LOCV " << offset << "\n";
    }

    void LDBYTE() override {
      os() << "LDBYTE\n";
    }
    void LDSHORT() override {
      os() << "LDSHORT\n";
    }
    void LDINT() override {
      os() << "LDINT\n";
    }
    void LDFLOAT() override {
      os() << "LDFLOAT\n";
    }
    void LDDOUBLE() override {
      os() << "LDDOUBLE\n";
    }

    void STBYTE() override {
      os() << "STBYTE\n";
    }
    void STSHORT() override {
      os() << "STSHORT\n";
    }
    void STINT() override {
      os() << "STINT\n";
    }
    void STFLOAT() override {
      os() << "STFLOAT\n";
    }
    void STDOUBLE() override {
      os() << "STDOUBLE\n";
    }

    void SP() override {
      os() << "SP\n";
    }
    void ALLOC() override {
      os() << "ALLOC\n";
    }
    void DUP32() override {
      os() << "DUP32\n";
    }
    void DUP64() override {
      os() << "DUP64\n";
    }
    void SWAP32() override {
      os() << "SWAP32\n";
    }
    void SWAP64() override {
      os() << "SWAP64\n";
    }
    void INT(int value) override {
      os() << "INT " << value << "\n";
    }
    void FLOAT(float value) override {
      os() << "FLOAT " << value << "\n";
    }
    void DOUBLE(double value) override {
      os() << "DOUBLE " << value << "\n";
    }

    void NEG() override {
      os() << "NEG\n";
    }
    void ADD() override {
      os() << "ADD\n";
    }
    void SUB() override {
      os() << "SUB\n";
    }
    void MUL() override {
      os() << "MUL\n";
    }
    void DIV() override {
      os() << "DIV\n";
    }
    void UDIV() override {
      os() << "UDIV\n";
    }
    void MOD() override {
      os() << "MOD\n";
    }
    void UMOD() override {
      os() << "UMOD\n";
    }

    void DNEG() override {
      os() << "DNEG\n";
    }
    void DADD() override {
      os() << "DADD\n";
    }
    void DSUB() override {
      os() << "DSUB\n";
    }
    void DMUL() override {
      os() << "DMUL\n";
    }
    void DDIV() override {
      os() << "DDIV\n";
    }

    void INCR(int value) override {
      os() << "INCR " << value << "\n";
    }
    void DECR(int value) override {
      os() << "DECR " << value << "\n";
    }

    void D2F() override {
      os() << "D2F\n";
    }
    void F2D() override {
      os() << "F2D\n";
    }
    void D2I() override {
      os() << "D2I\n";
    }
    void I2D() override {
      os() << "I2D\n";
    }

    void EQ() override {
      os() << "EQ\n";
    }
    void NE() override {
      os() << "NE\n";
    }

    void GT() override {
      os() << "GT\n";
    }
    void GE() override {
      os() << "GE\n";
    }
    void LE() override {
      os() << "LE\n";
    }
    void LT() override {
      os() << "LT\n";
    }

    void UGT() override {
      os() << "UGT\n";
    }
    void UGE() override {
      os() << "UGE\n";
    }
    void ULE() override {
      os() << "ULE\n";
    }
    void ULT() override {
      os() << "ULT\n";
    }

    void DCMP() override {
      os() << "DCMP\n";
    }

    void NOT() override {
      os() << "NOT\n";
    }
    void AND() override {
      os() << "AND\n";
    }
    void OR() override {
      os() << "OR\n";
    }
    void XOR() override {
      os() << "XOR\n";
    }

    void ROTL() override {
      os() << "ROTL\n";
    }
    void ROTR() override {
      os() << "ROTR\n";
    }
    void SHTL() override {
      os() << "SHTL\n";
    }
    void SHTRU() override {
      os() << "SHTRU\n";
    }
    void SHTRS() override {
      os() << "SHTRS\n";
    }

    void ENTER(size_t bytes) override {
      os() << "ENTER " << bytes << "\n";
    }
    void START() override {
      os() << "START\n";
    }
    void STFVAL32() override {
      os() << "STFVAL32\n";
    }
    void STFVAL64() override {
      os() << "STFVAL64\n";
    }
    void LEAVE() override {
      os() << "LEAVE\n";
    }
    void RET() override {
      os() << "RET\n";
    }
    void RETN(int bytes) override {
      os() << "RETN " << bytes << "\n";
    }
    void CALL(std::string label) override {
      os() << "CALL " << label << "\n";
    }
    void TRASH(int bytes) override {
      os() << "TRASH " << bytes << "\n";
    }
    void LDFVAL32() override {
      os() << "LDFVAL32\n";
    }
    void LDFVAL64() override {
      os() << "LDFVAL64\n";
    }

    void JMP(std::string label) override {
      os() << "JMP " << label << "\n";
    }
    void LEAP() override {
      os() << "LEAP\n";
    }
    void BRANCH() override {
      os() << "BRANCH\n";
    }

    void JZ(std::string label) override {
      os() << "JZ " << label << "\n";
    }
    void JNZ(std::string label) override {
      os() << "JNZ " << label << "\n";
    }

    void JEQ(std::string label) override {
      os() << "JEQ " << label << "\n";
    }
    void JNE(std::string label) override {
      os() << "JNE " << label << "\n";
    }

    void JGT(std::string label) override {
      os() << "JGT " << label << "\n";
    }
    void JGE(std::string label) override {
      os() << "JGE " << label << "\n";
    }
    void JLE(std::string label) override {
      os() << "JLE " << label << "\n";
    }
    void JLT(std::string label) override {
      os() << "JLT " << label << "\n";
    }

    void JUGT(std::string label) override {
      os() << "JUGT " << label << "\n";
    }
    void JUGE(std::string label) override {
      os() << "JUGE " << label << "\n";
    }
    void JULE(std::string label) override {
      os() << "JULE " << label << "\n";
    }
    void JULT(std::string label) override {
      os() << "JULT " << label << "\n";
    }

    void NIL() override {
      os() << "NIL\n";
    }
    void NOP() override {
      os() << "NOP\n";
    }

  };

} // namespace cdk

#endif
