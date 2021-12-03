#=====================================================================
#==========      TWEAK THE 'ROOT' VARIABLE IF NECESSARY     ==========
#=====================================================================

# install in user's home directory
#ROOT = ${HOME}/compiladores/root

# if installing in system dirs, use the following line instead
ROOT= 

#=====================================================================
#==========      DO NOT CHANGE ANYTHING AFTER THIS LINE     ==========
#=====================================================================

LIBNAME=cdk

CXXFLAGS=-I. -Icdk -ggdb -Wall -Wextra -std=c++17 -pedantic
#CXXFLAGS=-O3 -I. -ggdb -Wall -std=c++11 -pedantic

INST_INC_DIR = $(ROOT)/usr/include/$(LIBNAME)
INST_BIN_DIR = $(ROOT)/usr/bin
INST_LIB_DIR = $(ROOT)/usr/lib
INST_DOC_DIR = $(ROOT)/usr/share/doc/packages/$(LIBNAME)

SRC_CPP = $(wildcard *.cpp) $(wildcard cdk/*.cpp) $(wildcard cdk/ast/*.cpp) \
    $(wildcard cdk/emitters/*.cpp) $(wildcard cdk/targets/*.cpp) $(wildcard cdk/types/*.cpp)
	        
OFILES = $(SRC_CPP:%.cpp=%.o)

YACC=byacc
LEX=flex

%.o:: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

all: cdk/ast/all.h cdk/ast/visitor_decls.h lib$(LIBNAME).a docs

docs: doc/.docsdone

.PHONY: bin/mk-node-decls.pl
cdk/ast/all.h: bin/mk-node-decls.pl
	(cd cdk; ../bin/cdk ast --decls ast --cdk > ast/all.h)

.PHONY: bin/mk-visitor-decls.pl
cdk/ast/visitor_decls.h: ./bin/mk-visitor-decls.pl
	(cd cdk; ../bin/cdk ast --decls target --cdk > ast/visitor_decls.h)

doc/.docsdone: $(LIBNAME).doxyfile
	doxygen $<
	touch $@

$(LIBNAME).doxyfile: $(SRC_CPP)

lib$(LIBNAME).a: $(OFILES)
	ar cq $@ $^

clean:
	rm -f lib$(LIBNAME).a $(OFILES) cdk/ast/all.h cdk/ast/visitor_decls.h a.out core y.tab.[ch] lex.yy.c

install: all
	mkdir -p $(INST_LIB_DIR) $(INST_DOC_DIR) $(INST_BIN_DIR)
	mkdir -p $(INST_INC_DIR)/ast $(INST_INC_DIR)/emitters $(INST_INC_DIR)/targets $(INST_INC_DIR)/types
	cp -a bin/* $(INST_BIN_DIR)
	cp -a cdk/*.h $(INST_INC_DIR)
	cp -a cdk/ast/*.h $(INST_INC_DIR)/ast
	cp -a cdk/emitters/*.h $(INST_INC_DIR)/emitters
	cp -a cdk/targets/*.h $(INST_INC_DIR)/targets
	cp -a cdk/types/*.h $(INST_INC_DIR)/types
	cp -a lib$(LIBNAME).a $(INST_LIB_DIR)
	#DOC cp -a doc/. $(INST_DOC_DIR)/
	# remove these files: the actual ones will be provided by each compiler
	rm $(INST_INC_DIR)/targets/basic_ast_visitor.h

depend:
	$(CXX) $(CXXFLAGS) -MM $(SRC_CPP) > .makedeps

-include .makedeps

#=====================================================================
#==========             T  H  E         E  N  D             ==========
#=====================================================================
