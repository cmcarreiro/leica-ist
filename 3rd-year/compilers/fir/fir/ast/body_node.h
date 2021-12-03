#ifndef __FIR_AST_BODY_NODE_H__
#define __FIR_AST_BODY_NODE_H__

#include <cdk/ast/sequence_node.h>
#include "ast/block_node.h"

namespace fir {
    class body_node : public cdk::sequence_node {
        block_node* _prologue;
        block_node* _block;
        block_node* _epilogue;

    public:
        body_node(int lineno, block_node* prologue, block_node* block, block_node* epilogue) :
            cdk::sequence_node(lineno), _prologue(prologue), _block(block), _epilogue(epilogue) {
        }

    public:
        block_node* prologue() {
            return _prologue;
        }

        block_node* block() {
            return _block;
        }

        block_node* epilogue() {
            return _epilogue;
        }

        void accept(basic_ast_visitor* sp, int level) {
            sp->do_body_node(this, level);
        }

    };

} // fir

#endif
