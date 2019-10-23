/* Nome: Catarina Carreiro
   Numero: 92438
   Disciplina: Introducao aos Algoritmos e Estruturas de Dados
   Descricao: Define a estrutura de dados contacto, elemento e lista
   Ficheiro: lista_contactos.h */

#ifndef LISTA_CONTACTOS
#define LISTA_CONTACTOS

#include <stdio.h>
#include <string.h>
#include <stdlib.h>


typedef struct {
  char * nome;
  char * local;
  char * dominio;
  char * numero;
} contacto;

typedef struct el{
  contacto * c;
  struct el * proximo;
}elemento;

typedef struct {
  elemento * primeiro, * ultimo;
} lista;


/*
FUNCOES LIGADAS AO CONTACTO
*/
contacto * criaContacto(char * nome, char * local, char * dominio, char * numero);
void limpaContacto(contacto * pcontacto);
void imprimeContacto(contacto * contacto);
void imprimeContactos(lista * plista);
void mudaEmail(contacto * pcontacto, char * local, char * dominio);
/*
FUNCOES LIGADAS A LISTA
*/
lista * inicializaLista();
elemento * criaElemento(contacto * pcontacto);
void insereElemento(lista * plista, contacto * pcontacto);
elemento * retornaAnterior(char * nome, lista * plista);
elemento * removeElemento(lista * plista, char * pnome);
void limpaElemento(elemento * pel);
void limpaLista(lista * plista);
void limpaTudo(lista * plista);

#endif
