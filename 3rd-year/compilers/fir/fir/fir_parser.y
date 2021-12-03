%{
//-- don't change *any* of these: if you do, you'll break the compiler.
#include <algorithm>
#include <memory>
#include <cstring>
#include <cdk/compiler.h>
#include <cdk/types/types.h>
#include "ast/all.h"
#define LINE                  compiler->scanner()->lineno()
#define yylex()               compiler->scanner()->scan()
#define yyerror(compiler, s)  compiler->scanner()->error(s)
//-- don't change *any* of these --- END!

#define NIL (new cdk::nil_node(LINE))
%}

%parse-param {std::shared_ptr<cdk::compiler> compiler}

%union {
  //--- don't change *any* of these: if you do, you'll break the compiler.
  YYSTYPE() : type(cdk::primitive_type::create(0, cdk::TYPE_VOID)) {}
  ~YYSTYPE() {}
  YYSTYPE(const YYSTYPE &other) { *this = other; }
  YYSTYPE& operator=(const YYSTYPE &other) { type = other.type; return *this; }

  std::shared_ptr<cdk::basic_type> type;        /* expression type */
  //-- don't change *any* of these --- END!

  int                   i;  // inteiro
  double                d;  // real
  std::string          *s;  // id ou string

  cdk::basic_node      *node;
  cdk::sequence_node   *sequence;
  cdk::expression_node *expression;
  cdk::lvalue_node     *lvalue;
  cdk::integer_node    *integer;

  fir::block_node       *block;
  fir::body_node       *body;
}

%token tAND tOR tNE tLE tGE tSIZEOF
%token tDEFAULTRETVAL tEPILOGUE
%token tWRITE tWRITELN
%token tTYPE_STRING tTYPE_INT tTYPE_FLOAT tTYPE_VOID
%token tIF tTHEN tELSE
%token tWHILE tDO tFINALLY
%token tLEAVE tRESTART tRETURN

%token<i> tINTEGER
%token<d> tFLOAT
%token<s> tSTRING tID
%token<expression> tNULL

%type<node> instruction
%type<sequence> file instructions opt_instructions 
%type<sequence> expressions opt_expressions
%type<expression> expression integer float opt_initializer 
%type<lvalue> lvalue
%type<block> block opt_prologue opt_block opt_epilogue

%type<node> declaration  argdec  vardec fundec fundef
%type<sequence> declarations argdecs vardecs opt_vardecs
%type<body> body

%type<s> string
%type<type> data_type

%nonassoc tIF
%nonassoc tTHEN
%nonassoc tELSE

%nonassoc tWHILE
%nonassoc tDO
%nonassoc tFINALLY

%right '='
%left tOR
%left tAND
%right '~'
%left tNE tEQ
%left '<' tLE tGE '>'
%left '+' '-'
%left '*' '/' '%'
%right tUOP

%%
file         : /* empty */  { compiler->ast($$ = new cdk::sequence_node(LINE)); }
             | declarations { compiler->ast($$ = $1); }
             ;

declarations :              declaration { $$ = new cdk::sequence_node(LINE, $1);     }
             | declarations declaration { $$ = new cdk::sequence_node(LINE, $2, $1); }
             ;

declaration  : vardec ';' { $$ = $1; }
             | fundec     { $$ = $1; }
             | fundef     { $$ = $1; }
             ;

vardec        : data_type tID opt_initializer     { $$ = new fir::variable_declaration_node(LINE, ' ',  $1, *$2, $3); }
              | data_type '*' tID opt_initializer { $$ = new fir::variable_declaration_node(LINE, '*',  $1, *$3, $4); }
              | data_type '?' tID                 { $$ = new fir::variable_declaration_node(LINE, '?',  $1, *$3, nullptr); }
              ;
             
data_type    : tTYPE_STRING                     { $$ = cdk::primitive_type::create(4, cdk::TYPE_STRING);  }
             | tTYPE_INT                        { $$ = cdk::primitive_type::create(4, cdk::TYPE_INT);     }
             | tTYPE_FLOAT                       { $$ = cdk::primitive_type::create(8, cdk::TYPE_DOUBLE);  }
             | '<' data_type '>'  { $$ = cdk::reference_type::create(4, $2); }
             | tTYPE_VOID                       { $$ = cdk::primitive_type::create(0, cdk::TYPE_VOID);  }
             ;
       
opt_initializer  : /* empty */         { $$ = nullptr; /* must be nullptr, not NIL */ }
                 | '=' expression      { $$ = $2; }
                 ;
             
fundec    : data_type     tID '(' argdecs ')' { $$ = new fir::function_declaration_node(LINE, ' ', $1, *$2, $4); }
          | data_type '?' tID '(' argdecs ')' { $$ = new fir::function_declaration_node(LINE, '?', $1, *$3, $5); }
          | data_type '*' tID '(' argdecs ')' { $$ = new fir::function_declaration_node(LINE, '*', $1, *$3, $5); }
          ;

fundef    : data_type     tID '(' argdecs ')'                             body { $$ = new fir::function_definition_node(LINE, ' ', $1, *$2, $4, $6); }
          | data_type     tID '(' argdecs ')' tDEFAULTRETVAL expression   body { $$ = new fir::function_definition_node(LINE, ' ', $1, *$2, $4, $8, $7); }
          | data_type '*' tID '(' argdecs ')'                             body { $$ = new fir::function_definition_node(LINE, '*', $1, *$3, $5, $7); }
          | data_type '*' tID '(' argdecs ')' tDEFAULTRETVAL expression   body { $$ = new fir::function_definition_node(LINE, '*', $1, *$3, $5, $9, $8); }
          ;

argdecs  : /* empty */         { $$ = new cdk::sequence_node(LINE);  }
         |             argdec  { $$ = new cdk::sequence_node(LINE, $1);     }
         | argdecs ',' argdec  { $$ = new cdk::sequence_node(LINE, $3, $1); }
         ;

argdec   : data_type tID { $$ = new fir::variable_declaration_node(LINE, ' ', $1, *$2, nullptr); }
         ;

block : '{' opt_vardecs opt_instructions '}'          { $$ = new fir::block_node(LINE, $2, $3); }
      ;

body          : opt_prologue opt_block opt_epilogue       { $$ = new fir::body_node(LINE, $1, $2, $3); }

opt_prologue  : /* vazio */                               { $$ = nullptr; }
              | '@' block  { $$ = $2; }
              ;

opt_block     : /* vazio */                               { $$ = nullptr; }
              | block  { $$ = $1; }
              ;

opt_epilogue  : /* vazio */                               { $$ = nullptr; }
              | tEPILOGUE block  { $$ = $2; }
              ;

vardecs      : vardec ';'           { $$ = new cdk::sequence_node(LINE, $1);     }
             | vardecs vardec ';'   { $$ = new cdk::sequence_node(LINE, $2, $1); }
             ;
             
opt_vardecs  : /* empty */ { $$ = NULL; }
             | vardecs     { $$ = $1; }
             ;

instructions    : instruction                { $$ = new cdk::sequence_node(LINE, $1);     }
                | instructions instruction   { $$ = new cdk::sequence_node(LINE, $2, $1); }
                ;

opt_instructions  : /* empty */  { $$ = new cdk::sequence_node(LINE); }
                  | instructions { $$ = $1; }
                  ;


instruction     : tIF expression tTHEN instruction                        { $$ = new fir::if_node(LINE, $2, $4); }
                | tIF expression tTHEN instruction tELSE instruction      { $$ = new fir::if_else_node(LINE, $2, $4, $6); }
                | tWHILE expression tDO instruction                       { $$ = new fir::while_node(LINE, $2, $4); }
                | tWHILE expression tDO instruction tFINALLY instruction  { $$ = new fir::while_finally_node(LINE, $2, $4, $6); }
                | expression ';'                                          { $$ = new fir::evaluation_node(LINE, $1); }
                | tWRITE   expressions ';'                                { $$ = new fir::print_node(LINE, $2, false); }
                | tWRITELN expressions ';'                                { $$ = new fir::print_node(LINE, $2, true); }
                | tLEAVE  ';'                                                { $$ = new fir::leave_node(LINE, 1); }
                | tLEAVE tINTEGER ';'                                        { $$ = new fir::leave_node(LINE, $2); }
                | tRESTART ';'                                           { $$ = new fir::restart_node(LINE, 1); }
                | tRESTART tINTEGER ';'                                       { $$ = new fir::restart_node(LINE, $2); }                              
                | tRETURN                                                 { $$ = new fir::return_node(LINE); }
                | block                                                   { $$ = $1; }
                ;

lvalue          : tID                                            { $$ = new cdk::variable_node(LINE, *$1); delete $1; }
                | lvalue             '[' expression ']'          { $$ = new fir::index_node(LINE, new cdk::rvalue_node(LINE, $1), $3); }
                | '(' expression ')' '[' expression ']'          { $$ = new fir::index_node(LINE, $2, $5); }
                | tID '(' opt_expressions ')' '[' expression ']' { $$ = new fir::index_node(LINE, new fir::function_call_node(LINE, *$1, $3), $6); }
                ;

expression      : integer                       { $$ = $1; }
                | float                         { $$ = $1; }
                | string                        { $$ = new cdk::string_node(LINE, $1); }
                | tNULL                         { $$ = new fir::nullptr_node(LINE); }
                /* LEFT VALUES */
                | lvalue                        { $$ = new cdk::rvalue_node(LINE, $1); }
                /* ASSIGNMENTS */
                | lvalue '=' expression         { $$ = new cdk::assignment_node(LINE, $1, $3); }
                /* ARITHMETIC EXPRESSIONS */
                | expression '+' expression    { $$ = new cdk::add_node(LINE, $1, $3); }
                | expression '-' expression    { $$ = new cdk::sub_node(LINE, $1, $3); }
                | expression '*' expression    { $$ = new cdk::mul_node(LINE, $1, $3); }
                | expression '/' expression    { $$ = new cdk::div_node(LINE, $1, $3); }
                | expression '%' expression    { $$ = new cdk::mod_node(LINE, $1, $3); }
                /* LOGICAL EXPRESSIONS */
                | expression  '<' expression    { $$ = new cdk::lt_node(LINE, $1, $3); }
                | expression tLE  expression    { $$ = new cdk::le_node(LINE, $1, $3); }
                | expression tEQ  expression    { $$ = new cdk::eq_node(LINE, $1, $3); }
                | expression tGE  expression    { $$ = new cdk::ge_node(LINE, $1, $3); }
                | expression  '>' expression    { $$ = new cdk::gt_node(LINE, $1, $3); }
                | expression tNE  expression    { $$ = new cdk::ne_node(LINE, $1, $3); }
                /* LOGICAL EXPRESSIONS */
                | expression tAND  expression    { $$ = new cdk::and_node(LINE, $1, $3); }
                | expression tOR   expression    { $$ = new cdk::or_node (LINE, $1, $3); }
                /* UNARY EXPRESSION */
                | '-' expression %prec tUOP  { $$ = new cdk::neg_node(LINE, $2); }
                | '+' expression %prec tUOP  { $$ = new fir::identity_node(LINE, $2); }
                | '~' expression             { $$ = new cdk::not_node(LINE, $2); }
                /* OTHER EXPRESSION */
                | '@'                        { $$ = new fir::input_node(LINE); }
                /* OTHER EXPRESSION */
                | tID '(' opt_expressions ')'   { $$ = new fir::function_call_node(LINE, *$1, $3); delete $1; }
                | tSIZEOF '(' expression ')'   { $$ = new fir::sizeof_node(LINE, $3); }
                /* OTHER EXPRESSION */
                | '(' expression ')'            { $$ = $2; }
                | '[' expression ']'            { $$ = new fir::stack_alloc_node(LINE, $2); }
                | lvalue '?'                    { $$ = new fir::address_of_node(LINE, $1); }
                ;

expressions     : expression                     { $$ = new cdk::sequence_node(LINE, $1);     }
                | expressions ',' expression     { $$ = new cdk::sequence_node(LINE, $3, $1); }
                ;

opt_expressions : /* empty */         { $$ = new cdk::sequence_node(LINE); }
                | expressions         { $$ = $1; }
                ;

integer         : tINTEGER                      { $$ = new cdk::integer_node(LINE, $1); };

float           : tFLOAT                         { $$ = new cdk::double_node(LINE, $1); };

string          : tSTRING                       { $$ = $1; }
                | string tSTRING                { $$ = $1; $$->append(*$2); delete $2; }
                ;

%%
