#include <string>
#include <sstream>
#include <memory>
#include <cdk/types/types.h>
#include "targets/postfix_writer.h"
#include "targets/type_checker.h"
#include "targets/frame_size_calculator.h"
#include "targets/symbol.h"
#include "ast/all.h"
// must come after other #includes
#include "fir_parser.tab.h"

void fir::postfix_writer::do_data_node(cdk::data_node *const node, int lvl) {
  // EMPTY
}

void fir::postfix_writer::do_nil_node(cdk::nil_node *const node, int lvl) {
  // EMPTY
}

void fir::postfix_writer::do_sequence_node(cdk::sequence_node *const node, int lvl) {
  for (size_t i = 0; i < node->size(); i++)
    node->node(i)->accept(this, lvl);
}

void fir::postfix_writer::do_block_node(fir::block_node *const node, int lvl) {
  // for now block and epilogue share vars 
  //_symtab.push();

  if (node->declarations()) node->declarations()->accept(this, lvl + 2);
  if (node->instructions()) node->instructions()->accept(this, lvl + 2);
  
  // for now block and epilogue share vars
  //_symtab.pop();
}

void fir::postfix_writer::do_return_node(fir::return_node *const node, int lvl) {
  ASSERT_SAFE;
  _pf.JMP(_currentReturnJumpLabel);
}

void fir::postfix_writer::do_variable_declaration_node(fir::variable_declaration_node *const node, int lvl) {
  ASSERT_SAFE;

  auto id = node->identifier();

  // type size
  int offset = 0, typesize = node->type()->size(); // in bytes
  if (_inFunctionBody) {
    _offset -= typesize;
    offset = _offset;
  } else if (_inFunctionArgs) {
    offset = _offset;
    _offset += typesize;
  } else {
    offset = 0; // global variable
  }

  auto symbol = new_symbol();
  if (symbol) {
    symbol->set_offset(offset);
    reset_new_symbol();
  }

  if (_inFunctionBody) {
    // if we are dealing with local variables, then no action is needed
    // unless an initializer exists
    if (node->initializer()) {
      node->initializer()->accept(this, lvl);
      if (node->is_typed(cdk::TYPE_INT) || node->is_typed(cdk::TYPE_STRING) || node->is_typed(cdk::TYPE_POINTER)) {
        _pf.LOCAL(symbol->offset());
        _pf.STINT();
      } else if (node->is_typed(cdk::TYPE_DOUBLE)) {
        if (node->initializer()->is_typed(cdk::TYPE_INT))
          _pf.I2D();
        _pf.LOCAL(symbol->offset());
        _pf.STDOUBLE();
      } else {
        std::cerr << "cannot initialize" << std::endl;
      }
    }
  } else {
    if (!_function) {
      if (node->initializer() == nullptr) {
        _pf.BSS();
        _pf.ALIGN();
        _pf.LABEL(id);
        _pf.SALLOC(typesize);
      } else {

        if (node->is_typed(cdk::TYPE_INT) || node->is_typed(cdk::TYPE_DOUBLE) || node->is_typed(cdk::TYPE_POINTER)) {
          _pf.DATA();
          _pf.ALIGN();
          _pf.LABEL(id);

          if (node->is_typed(cdk::TYPE_INT)) {
            node->initializer()->accept(this, lvl);
          } else if (node->is_typed(cdk::TYPE_POINTER)) {
            node->initializer()->accept(this, lvl);
          } else if (node->is_typed(cdk::TYPE_DOUBLE)) {
            if (node->initializer()->is_typed(cdk::TYPE_DOUBLE)) {
              node->initializer()->accept(this, lvl);
            } else if (node->initializer()->is_typed(cdk::TYPE_INT)) {
              cdk::integer_node *dclini = dynamic_cast<cdk::integer_node*>(node->initializer());
              cdk::double_node ddi(dclini->lineno(), dclini->value());
              ddi.accept(this, lvl);
            } else {
              std::cerr << node->lineno() << ": '" << id << "' has bad initializer for real value\n";
              _errors = true;
            }
          }
        } else if (node->is_typed(cdk::TYPE_STRING)) {
          _pf.DATA();
          _pf.ALIGN();
          _pf.LABEL(id);
          node->initializer()->accept(this, lvl);
        } else {
          std::cerr << node->lineno() << ": '" << id << "' has unexpected initializer\n";
          _errors = true;
        }

      }

    }
  }
}

//===========================================================================

void fir::postfix_writer::do_double_node(cdk::double_node *const node, int lvl) {
  if (_inFunctionBody) {
    _pf.DOUBLE(node->value()); // load number to the stack
  } else {
    _pf.SDOUBLE(node->value());    // double is on the DATA segment
  }
}

void fir::postfix_writer::do_integer_node(cdk::integer_node *const node, int lvl) {
  if (_inFunctionBody) {
    _pf.INT(node->value()); // integer literal is on the stack: push an integer
  } else {
    _pf.SINT(node->value()); // integer literal is on the DATA segment
  }
}

void fir::postfix_writer::do_string_node(cdk::string_node *const node, int lvl) {
  int lbl1;
  /* generate the string literal */
  _pf.RODATA(); // strings are readonly DATA
  _pf.ALIGN(); // make sure the address is aligned
  _pf.LABEL(mklbl(lbl1 = ++_lbl)); // give the string a name
  _pf.SSTRING(node->value()); // output string characters
  if (_function) {
    // local variable initializer
    _pf.TEXT();
    _pf.ADDR(mklbl(lbl1));
  } else {
    // global variable initializer
    _pf.DATA();
    _pf.SADDR(mklbl(lbl1));
  }
}

//===========================================================================

void fir::postfix_writer::do_variable_node(cdk::variable_node *const node, int lvl) {
  ASSERT_SAFE;

	const std::string &id = node->name();

	if(id == _currentFunctionName) {
		_specialVarSeen = true;
	} else {
		const std::string &id = node->name();
		auto symbol = _symtab.find(id);
		if (symbol->global()) {
			_pf.ADDR(symbol->name());
		} else {
			_pf.LOCAL(symbol->offset());
		}
	}
}

void fir::postfix_writer::do_index_node(fir::index_node *const node, int lvl) {
  ASSERT_SAFE;
  
  if (node->base()) {
    node->base()->accept(this, lvl);
  } else {
    if (_function) {
      _pf.LOCV(-_function->type()->size());
    } else {
      std::cerr << "FATAL: " << node->lineno() << ": trying to use return value outside function" << std::endl;
    }
  }
  node->index()->accept(this, lvl);
  _pf.INT(3);
  _pf.SHTL();
  _pf.ADD(); // add pointer and index
}

void fir::postfix_writer::do_rvalue_node(cdk::rvalue_node *const node, int lvl) {
  ASSERT_SAFE
  ;
  node->lvalue()->accept(this, lvl);
  if (node->type()->name() == cdk::TYPE_DOUBLE) {
    _pf.LDDOUBLE();
  } else {
    // integers, pointers, and strings
    _pf.LDINT();
  }
}

void fir::postfix_writer::do_assignment_node(cdk::assignment_node *const node, int lvl) {
  ASSERT_SAFE;

  node->rvalue()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE) {
    if (node->rvalue()->type()->name() == cdk::TYPE_INT) _pf.I2D();
    _pf.DUP64();
  } else {
    _pf.DUP32();
  }

	_specialVarSeen = false;
  node->lvalue()->accept(this, lvl);

	if(_specialVarSeen) {
		if (_function->type()->name() != cdk::TYPE_VOID) {
			if (_function->type()->name() == cdk::TYPE_INT || _function->type()->name() == cdk::TYPE_STRING || _function->type()->name() == cdk::TYPE_POINTER)
				_pf.STFVAL32();
			else if (_function->type()->name() == cdk::TYPE_DOUBLE)
				_pf.STFVAL64();
		}
		_newRetValSeen = true;
	}
	else {
		if (node->type()->name() == cdk::TYPE_DOUBLE) {
			_pf.STDOUBLE();
		}
		else {
				_pf.STINT();
		}
	}

	_specialVarSeen = false;
}

//===========================================================================

void fir::postfix_writer::do_input_node(fir::input_node *const node, int lvl) {
  if (_lvalueType == cdk::TYPE_DOUBLE) {
    _functions_to_declare.insert("readd");
    _pf.CALL("readd");
    _pf.LDFVAL64();
  } else if (_lvalueType == cdk::TYPE_INT) {
    _functions_to_declare.insert("readi");
    _pf.CALL("readi");
    _pf.LDFVAL32();
  } else {
    std::cerr << "FATAL: " << node->lineno() << ": cannot read type" << std::endl;
    return;
  }
}

void fir::postfix_writer::do_print_node(fir::print_node *const node, int lvl) {
  ASSERT_SAFE;

  for (size_t ix = 0; ix < node->arguments()->size(); ix++) {
    auto child = dynamic_cast<cdk::expression_node*>(node->arguments()->node(ix));

    std::shared_ptr<cdk::basic_type> etype = child->type();
    child->accept(this, lvl); // expression to print
    if (etype->name() == cdk::TYPE_INT) {
      _functions_to_declare.insert("printi");
      _pf.CALL("printi");
      _pf.TRASH(4); // trash int
    } else if (etype->name() == cdk::TYPE_DOUBLE) {
      _functions_to_declare.insert("printd");
      _pf.CALL("printd");
      _pf.TRASH(8); // trash double
    } else if (etype->name() == cdk::TYPE_STRING) {
      _functions_to_declare.insert("prints");
      _pf.CALL("prints");
      _pf.TRASH(4); // trash char pointer
    } else {
      std::cerr << "cannot print expression of unknown type" << std::endl;
      return;
    }

  }

  if (node->newline()) {
    _functions_to_declare.insert("println");
    _pf.CALL("println");
  }
}

void fir::postfix_writer::do_evaluation_node(fir::evaluation_node *const node, int lvl) {
  ASSERT_SAFE;
  node->argument()->accept(this, lvl);
  _pf.TRASH(node->argument()->type()->size());
}

//===========================================================================

void fir::postfix_writer::do_neg_node(cdk::neg_node *const node, int lvl) {
  ASSERT_SAFE;
  node->argument()->accept(this, lvl); // process node data
  _pf.NEG();
}

void fir::postfix_writer::do_identity_node(fir::identity_node *const node, int lvl) {
  ASSERT_SAFE;
  node->argument()->accept(this, lvl); // process node data
}

void fir::postfix_writer::do_not_node(cdk::not_node *const node, int lvl) {
  ASSERT_SAFE
  ;
  node->argument()->accept(this, lvl + 2);
  _pf.INT(0);
  _pf.EQ();
  //_pf.NOT();
}

//===========================================================================

void fir::postfix_writer::do_add_node(cdk::add_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->left()->type()->name() == cdk::TYPE_INT) {
    _pf.I2D();
  } else if (node->type()->name() == cdk::TYPE_POINTER && node->left()->type()->name() == cdk::TYPE_INT) {
    _pf.INT(3);
    _pf.SHTL();
  }

  node->right()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->right()->type()->name() == cdk::TYPE_INT) {
    _pf.I2D();
  } else if (node->type()->name() == cdk::TYPE_POINTER && node->right()->type()->name() == cdk::TYPE_INT) {
    _pf.INT(3);
    _pf.SHTL();
  }

  if (node->type()->name() == cdk::TYPE_DOUBLE)
    _pf.DADD();
  else
    _pf.ADD();
}

void fir::postfix_writer::do_sub_node(cdk::sub_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->left()->type()->name() == cdk::TYPE_INT) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->right()->type()->name() == cdk::TYPE_INT) {
    _pf.I2D();
  } else if (node->type()->name() == cdk::TYPE_POINTER && node->right()->type()->name() == cdk::TYPE_INT) {
    _pf.INT(3);
    _pf.SHTL();
  }

  if (node->type()->name() == cdk::TYPE_DOUBLE)
    _pf.DSUB();
  else
    _pf.SUB();
}

void fir::postfix_writer::do_mul_node(cdk::mul_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->left()->type()->name() == cdk::TYPE_INT) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->right()->type()->name() == cdk::TYPE_INT) _pf.I2D();

  if (node->type()->name() == cdk::TYPE_DOUBLE)
    _pf.DMUL();
  else
    _pf.MUL();
}

void fir::postfix_writer::do_div_node(cdk::div_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->left()->type()->name() == cdk::TYPE_INT) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->type()->name() == cdk::TYPE_DOUBLE && node->right()->type()->name() == cdk::TYPE_INT) _pf.I2D();

  if (node->type()->name() == cdk::TYPE_DOUBLE)
    _pf.DDIV();
  else
    _pf.DIV();
}

void fir::postfix_writer::do_mod_node(cdk::mod_node *const node, int lvl) {
  ASSERT_SAFE
  ;
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);
  _pf.MOD();
}

//===========================================================================

void fir::postfix_writer::do_sizeof_node(fir::sizeof_node *const node, int lvl) {
  ASSERT_SAFE;
  _pf.INT(node->expression()->type()->size());
}

//===========================================================================

void fir::postfix_writer::do_ge_node(cdk::ge_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->left()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->right()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  _pf.GE();
}

void fir::postfix_writer::do_gt_node(cdk::gt_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->left()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->right()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  _pf.GT();
}

void fir::postfix_writer::do_le_node(cdk::le_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->left()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->right()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  _pf.LE();
}

void fir::postfix_writer::do_lt_node(cdk::lt_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->left()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->right()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  _pf.LT();
}

//===========================================================================

void fir::postfix_writer::do_and_node(cdk::and_node *const node, int lvl) {
  ASSERT_SAFE
  ;
  int lbl = ++_lbl;
  node->left()->accept(this, lvl + 2);
  _pf.DUP32();
  _pf.JZ(mklbl(lbl));
  node->right()->accept(this, lvl + 2);
  _pf.AND();
  _pf.ALIGN();
  _pf.LABEL(mklbl(lbl));
}

void fir::postfix_writer::do_or_node(cdk::or_node *const node, int lvl) {
  ASSERT_SAFE
  ;
  int lbl = ++_lbl;
  node->left()->accept(this, lvl + 2);
  _pf.DUP32();
  _pf.JNZ(mklbl(lbl));
  node->right()->accept(this, lvl + 2);
  _pf.OR();
  _pf.ALIGN();
  _pf.LABEL(mklbl(lbl));
}

//===========================================================================

void fir::postfix_writer::do_eq_node(cdk::eq_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->left()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->right()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  _pf.EQ();
}

void fir::postfix_writer::do_ne_node(cdk::ne_node *const node, int lvl) {
  ASSERT_SAFE
  ;

  node->left()->accept(this, lvl + 2);
  if (node->left()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  node->right()->accept(this, lvl + 2);
  if (node->right()->type()->name() == cdk::TYPE_INT && node->right()->type()->name() == cdk::TYPE_DOUBLE) _pf.I2D();

  _pf.NE();
}

//===========================================================================

void fir::postfix_writer::do_address_of_node(fir::address_of_node *const node, int lvl) {
  ASSERT_SAFE
  ;
  // since the argument is an lvalue, it is already an address
  node->lvalue()->accept(this, lvl + 2);
}

void fir::postfix_writer::do_stack_alloc_node(fir::stack_alloc_node *const node, int lvl) {
  ASSERT_SAFE
  ;
  node->argument()->accept(this, lvl);
  _pf.INT(3);
  _pf.SHTL();
  _pf.ALLOC(); // allocate
  _pf.SP(); // put base pointer in stack
}

void fir::postfix_writer::do_nullptr_node(fir::nullptr_node *const node, int lvl) {
  ASSERT_SAFE
  ; // a pointer is a 32-bit integer
  if (_inFunctionBody) {
    _pf.INT(0);
  } else {
    _pf.SINT(0);
  }
}

void fir::postfix_writer::do_restart_node(fir::restart_node *const node, int lvl) {
  if (_whileTest.size() != 0) {

    // for now only restart 1 works
    // for (int i = 1; i <= node->value()-1; i++) {
    //   _whileTest.pop();
    //   _whileEnd.pop();
    // }

    _pf.JMP(mklbl(_whileTest.top())); // jump to next cycle
  } else
    error(node->lineno(), "'restart' outside 'while'");
}

void fir::postfix_writer::do_leave_node(fir::leave_node *const node, int lvl) {
  if (_whileTest.size() != 0) {

    // for now only leave 1 works
    // for (int i = 1; i <= node->value()-1; i++) {
    //   _whileTest.pop();
    //   _whileEnd.pop();
    // }
    
    _pf.JMP(mklbl(_whileEnd.top())); // jump to while end
  } else
    error(node->lineno(), "'leave' outside 'while'");
}

//===========================================================================

void fir::postfix_writer::do_if_node(fir::if_node *const node, int lvl) {
  ASSERT_SAFE;
  int lbl_end;
  node->condition()->accept(this, lvl);
  _pf.JZ(mklbl(lbl_end = ++_lbl));
  node->block()->accept(this, lvl + 2);
  _pf.LABEL(mklbl(lbl_end));
}

void fir::postfix_writer::do_if_else_node(fir::if_else_node *const node, int lvl) {
  ASSERT_SAFE;
  int lbl_else, lbl_end;
  node->condition()->accept(this, lvl);
  _pf.JZ(mklbl(lbl_else = lbl_end = ++_lbl));
  node->thenblock()->accept(this, lvl + 2);
  if (node->elseblock()) {
    _pf.JMP(mklbl(lbl_end = ++_lbl));
    _pf.LABEL(mklbl(lbl_else));
    node->elseblock()->accept(this, lvl + 2);
  }
  _pf.LABEL(mklbl(lbl_end));
}

//===========================================================================

void fir::postfix_writer::do_while_node(fir::while_node *const node, int lvl) {
  ASSERT_SAFE;

  _whileTest.push(++_lbl);
  _whileEnd.push(++_lbl);

  // prepare to test
  _pf.ALIGN();
  _pf.LABEL(mklbl(_whileTest.top()));
  node->condition()->accept(this, lvl + 2);
  _pf.JZ(mklbl(_whileEnd.top()));

  // execute block
  node->block()->accept(this, lvl + 2);

  _pf.JMP(mklbl(_whileTest.top()));

  _pf.ALIGN();
  _pf.LABEL(mklbl(_whileEnd.top()));

  _whileTest.pop();
  _whileEnd.pop();
}

void fir::postfix_writer::do_while_finally_node(fir::while_finally_node *const node, int lvl) {
    ASSERT_SAFE;

  _whileTest.push(++_lbl);
  _whileEnd.push(++_lbl);

  // prepare to test
  _pf.ALIGN();
  _pf.LABEL(mklbl(_whileTest.top()));
  node->condition()->accept(this, lvl + 2);
  _pf.JZ(mklbl(_whileEnd.top()));

  // execute block
  node->doblock()->accept(this, lvl + 2);

  _pf.JMP(mklbl(_whileTest.top()));

  _pf.ALIGN();
  _pf.LABEL(mklbl(_whileEnd.top()));
  node->finallyblock()->accept(this, lvl + 2);

  _whileTest.pop();
  _whileEnd.pop();
}

//===========================================================================

void fir::postfix_writer::do_function_call_node(fir::function_call_node *const node, int lvl) {
  ASSERT_SAFE;

  auto symbol = _symtab.find(node->identifier());

  size_t argsSize = 0;
  if (node->arguments()->size() > 0) {
    for (int ax = node->arguments()->size() - 1; ax >= 0; ax--) {
      cdk::expression_node *arg = dynamic_cast<cdk::expression_node*>(node->arguments()->node(ax));
      arg->accept(this, lvl + 2);
      if (symbol->argument_is_typed(ax, cdk::TYPE_DOUBLE) && arg->is_typed(cdk::TYPE_INT)) {
        _pf.I2D();
      }
      argsSize += symbol->argument_size(ax);
    }
  }
  _pf.CALL(node->identifier());
  if (argsSize != 0) {
    _pf.TRASH(argsSize);
  }

  if (symbol->is_typed(cdk::TYPE_INT) || symbol->is_typed(cdk::TYPE_POINTER) || symbol->is_typed(cdk::TYPE_STRING)) {
    _pf.LDFVAL32();
  } else if (symbol->is_typed(cdk::TYPE_DOUBLE)) {
    _pf.LDFVAL64();
  } else {
    // cannot happen!
  }
}

//---------------------------------------------------------------------------

void fir::postfix_writer::do_function_declaration_node(fir::function_declaration_node *const node, int lvl) {
  ASSERT_SAFE;

  if (_inFunctionBody || _inFunctionArgs) {
    error(node->lineno(), "cannot declare function in body or in args");
    return;
  }


  if (!new_symbol()) return;

  auto function = new_symbol();
  _functions_to_declare.insert(function->name());
  reset_new_symbol();
}

//---------------------------------------------------------------------------

void fir::postfix_writer::do_function_definition_node(fir::function_definition_node *const node, int lvl) {
  ASSERT_SAFE;
  
  if (_inFunctionBody || _inFunctionArgs) {
    error(node->lineno(), "cannot define function in body or in arguments");
    return;
  }

  // remember symbol so that args and body know
  _function = new_symbol();
  _functions_to_declare.erase(_function->name());  // just in case
  reset_new_symbol();

  _currentReturnJumpLabel = mklbl(++_lbl);

  _offset = 8; // prepare for arguments (4: remember to account for return address)
  _symtab.push(); // scope of args

  if (node->arguments()->size() > 0) {
    _inFunctionArgs = true;
    for (size_t ix = 0; ix < node->arguments()->size(); ix++) {
      cdk::basic_node *arg = node->arguments()->node(ix);
      if (arg == nullptr) break; // this means an empty sequence of arguments
      arg->accept(this, 0); // the function symbol is at the top of the stack
    }
    _inFunctionArgs = false;
  }

  _pf.TEXT();
  _pf.ALIGN();
  if (node->qualifier() == '*') _pf.GLOBAL(_function->name(), _pf.FUNC());
  _pf.LABEL(_function->name());

  // compute stack size to be reserved for local variables
  frame_size_calculator lsc(_compiler, _symtab, _function);
  node->accept(&lsc, lvl);
  _pf.ENTER(lsc.localsize()); // total stack size reserved for local variables

  _offset = 0; // prepare for local variable

  _newRetValSeen = false;
  _currentFunctionName = node->identifier();
  // the following flag is a slight hack: it won't work with nested functions
  _inFunctionBody = true;
  node->body()->accept(this, lvl + 4); // body has its own scope

  if(!_newRetValSeen) {
    if (node->type()->name() != cdk::TYPE_VOID) {

			// set value
			if(node->type()->name() == cdk::TYPE_INT) {
				if(node->retval())
          node->retval()->accept(this, lvl + 2);
				else
          _pf.INT(0);
			}
      else if(node->type()->name() == cdk::TYPE_DOUBLE) {
        if(node->retval()) {
          node->retval()->accept(this, lvl + 2);
          if(node->retval()->type()->name() == cdk::TYPE_INT)
            _pf.I2D();
        }
				else
          _pf.DOUBLE(0);
			}

			// make it a return value
      if (node->type()->name() == cdk::TYPE_INT || node->type()->name() == cdk::TYPE_STRING || node->type()->name() == cdk::TYPE_POINTER)
        _pf.STFVAL32();
      else if (node->type()->name() == cdk::TYPE_DOUBLE)
        _pf.STFVAL64();
			else
        std::cerr << node->lineno() << ": should not happen: unknown return type" << std::endl;
    }
  }


  _inFunctionBody = false;
  _newRetValSeen = false;
  _currentFunctionName = "";

  _pf.LEAVE();
  _pf.RET();

  _symtab.pop(); // scope of arguments

  if (node->identifier() == "fir") {
    // declare external functions
    for (std::string s : _functions_to_declare)
      _pf.EXTERN(s);
  }

}

void fir::postfix_writer::do_body_node(fir::body_node *const node, int lvl) {
  if (node->prologue()) node->prologue()->accept(this, lvl + 2);
  if (node->block()) node->block()->accept(this, lvl + 2);

  // this is where return should jump to
  // if there is an epilogue return will jump to epilogue
  // if there isnt an epilogue return will jump to the end of the function
  _pf.LABEL(_currentReturnJumpLabel);

  if (node->epilogue()) node->epilogue()->accept(this, lvl + 2);
}

void fir::postfix_writer::do_prologue_node(fir::prologue_node *const node, int lvl) {
  if (node->declarations()) node->declarations()->accept(this, lvl + 2);
  if (node->instructions()) node->instructions()->accept(this, lvl + 2);
}