%option c++ prefix="fir_scanner_" outfile="fir_scanner.cpp"
%option stack noyywrap yylineno 8bit debug
%{ 
// make relevant includes before including the parser's tab file
#include <string>
#include <cstring>
#include <sstream>
#include <cdk/ast/sequence_node.h>
#include <cdk/ast/expression_node.h>
#include <cdk/ast/lvalue_node.h>
#include "fir_parser.tab.h"

// output stream for building string literals
static std::ostringstream strlit;

// don't change this
#define yyerror LexerError
%}

%x X_COMMENT
%x X_STRING  X_TILDE

%%
  {/* yydebug=1; set_debug(true); */}

  /* comentario explicativo */
"!!".*$

  /* comentario operacional */
"(*"                   yy_push_state(X_COMMENT);
<X_COMMENT>"*)"        yy_pop_state();
<X_COMMENT>.|"\n"      ;

  /* tipos de dados */
"int"                    return tTYPE_INT;
"float"                  return tTYPE_FLOAT;
"string"                 return tTYPE_STRING;
"void"                   return tTYPE_VOID;
[<>]                     return *yytext;

  /* ponteiro nulo */
"null"                   return tNULL;

"sizeof"                 return tSIZEOF;

  /* simbolos globais */
"*"                   return *yytext;
"?"                   return *yytext;

  /* funcoes */
"->"                   return tDEFAULTRETVAL;
  /* "@"                   return *yytext; */
">>"                   return tEPILOGUE;

  /* instrucoes condicionais */
"if"                   return tIF;
"then"                 return tTHEN;
"else"                 return tELSE;

  /* instrucao de iteracao */
"while"                   return tWHILE;
"do"                      return tDO;
"finally"                 return tFINALLY;

  /* instrucao de terminacao */
"leave"                   return tLEAVE; 

  /* instrucao de continuacao */
"restart"                 return tRESTART;

  /* operadores de expressoes */
[\[\]()]                return *yytext;
  /* [+-?]                   return *yytext; */
[+-]                   return *yytext;
  /* [/%*]                   return *yytext; */
[/%]                   return *yytext;
  /* [+-]                    return *yytext; */
  /* [<>]                    return *yytext; */
"<="                    return tLE;
">="                    return tGE;
"=="                    return tEQ;
"!="                    return tNE;
"~"                     return *yytext;
"&&"                    return tAND;
"||"                    return tOR;
"="                     return *yytext;

  /* leitura */
"@"                               return *yytext;

  /* delimitadores e terminadores */
  /* [,;()]                  return *yytext; */
[,;]                              return *yytext;

  /* indexacao de ponteiros */
  /* [\[\]]                  return *yytext; */

  /* reserva de memoria */
  /* [\[\]]                  return *yytext; */

  /* parenteses envolvem bloco */
[{}]                              return *yytext;

"return"                          return tRETURN;
"write"                           return tWRITE;
"writeln"                         return tWRITELN;

  /* octal */
"0"[0-7]+                         yylval.i = strtoul(yytext, NULL, 8); return tINTEGER;

  /* base 10 */
"0"|[1-9][0-9]*                          yylval.i = strtoul(yytext, NULL, 10); return tINTEGER;

  /* reais */
([0-9]*\.[0-9]+|[0-9]+\.[0-9]*)([eE][-+]?[0-9]+)? { yylval.d = strtod(yytext, NULL); return tFLOAT; }

  /* identificadores */
[A-Za-z_]([A-Za-z_]|[0-9])*  yylval.s = new std::string(yytext); return tID;

  /* strings */
"\'"                           yy_push_state(X_STRING);
<X_STRING>"~"                   yy_push_state(X_TILDE);
<X_STRING>"\'"                 { yylval.s = new std::string(strlit.str()); strlit.str(""); yy_pop_state(); return tSTRING; }
<X_STRING>.                    strlit << *yytext;

<X_TILDE>"n"                 strlit << '\n'; yy_pop_state();
<X_TILDE>"r"                 strlit << '\r'; yy_pop_state();
<X_TILDE>"t"                 strlit << '\t'; yy_pop_state();
<X_TILDE>"~"                 strlit << '~'; yy_pop_state();

<X_TILDE>[0-9a-fA-F]{1,2} { strlit << (char)(unsigned char)strtoul(yytext, NULL, 16); yy_pop_state(); }
<X_TILDE>.                 strlit << *yytext; yy_pop_state();

  /* white space */
[ \n\t\v\f\r]

  /* outros caracteres */
.                      yyerror(yytext);

%%
