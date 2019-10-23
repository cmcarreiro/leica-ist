/* Nome: Catarina Carreiro
   Numero: 92438
   Disciplina: Introducao aos Algoritmos e Estruturas de Dados
   Descricao: Define as funcoes relacionadas com tabelas Hash
   Ficheiro: hashtable.h */

#ifndef HASHTABLE
#define HASHTABLE

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "lista_contactos.h"

unsigned long retornaHash(char * str);
void inicializaTabela(lista ** tabela, int dimensao);
void limpaTabela(lista ** table, int dimensao);
void insereElementoTabela(lista ** tabela, contacto * pcontacto, char * linha, int dim);
void removeElementoTabela(lista ** tabela, contacto * pcontacto, char * linha, int dim);
contacto * retornaContacto(char * nome, lista ** tabela, int dim);
void contaDominio(char * dominio, lista ** tabela, int dim);

#endif
