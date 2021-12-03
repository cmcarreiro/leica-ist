/* Nome: Catarina Carreiro
   Numero: 92438
   Disciplina: Introducao aos Algoritmos e Estruturas de Dados
   Descricao: Lida com uma lista de contactos
   Ficheiro: proj2.c */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "lista_contactos.h"
#include "hashtable.h"

/*
CONSTANTES
*/
#define MAXNOME 1023     /*numero maximo de caracteres no nome*/
#define MAXEMAIL 511     /*numero maximo de caracteres no email*/
#define MAXNUMERO 63     /*numero maximo de caracteres no numero de telemovel*/
#define DIMNOMES 997     /*dimensao da tabela com as pessoas*/
#define DIMDOMINIO 737   /*dimensao da tabela com os dominios*/

/*
VARIAVEIS GLOBAIS
*/

lista * listaprincipal;                   /*lista com contactos*/
lista * tabelanomes[DIMNOMES];            /*tabela que organiza por nome*/
lista * tabeladominio[DIMDOMINIO];        /*tabela que organiza por dominio*/


/*
FUNCOES
*/

char * leLinha(int tamanho);
char * leLocal(int tamanho);

void trata_a();
void trata_l();
void trata_p();
void trata_r();
void trata_e();
void trata_c();


/*
FUNCAO MAIN
*/
int main(){
  char c;

  /*inicializacao das listas e tabelas*/
  listaprincipal = inicializaLista();
  inicializaTabela(tabelanomes, DIMNOMES);
  inicializaTabela(tabeladominio, DIMDOMINIO);


  while((c=getchar())!='x'){
    switch(c){
      case 'a':
        trata_a();
        break;
      case 'l':
        trata_l();
        break;
      case 'p':
        trata_p();
        break;
      case 'r':
        trata_r();
        break;
      case 'e':
        trata_e();
        break;
      case 'c':
        trata_c();
        break;
    }
  }

  /*limpar tudo*/
  limpaTabela(tabelanomes, DIMNOMES);
  limpaTabela(tabeladominio, DIMDOMINIO);
  limpaTudo(listaprincipal);
  return 0;
}


/*
FUNCOES QUE TRATAM DO MAIN
*/

/*
trata_a -> adiciona um contacto
*/
void trata_a(){
  char * pnome;
  char * plocal;
  char * pdominio;
  char * pnumero;
  contacto * pcontacto;

  pnome = leLinha(MAXNOME);
  plocal = leLocal(MAXEMAIL);
  pdominio = leLinha(MAXEMAIL);
  pnumero = leLinha(MAXNUMERO);
  if(retornaContacto(pnome, tabelanomes, DIMNOMES) == NULL){
    pcontacto = criaContacto(pnome, plocal, pdominio, pnumero);

    /*inserir o contacto na lista principal e nas tabelas*/
    insereElemento(listaprincipal, pcontacto);
    insereElementoTabela(tabelanomes, pcontacto, pcontacto->nome, DIMNOMES);
    insereElementoTabela(tabeladominio, pcontacto, pcontacto->dominio, DIMDOMINIO);

  }else{
    printf("Nome existente.\n");
    free(pnome); free(plocal); free(pdominio); free(pnumero);
  }
}


/*
trata_l -> imprime todos os contactos
*/
void trata_l(){
  imprimeContactos(listaprincipal);
}

/*
trata_p -> retorna um contacto especifico
*/
void trata_p(){
  char * pnome;
  contacto * pcontacto;
  pnome = leLinha(MAXNOME);
  pcontacto = retornaContacto(pnome, tabelanomes, DIMNOMES);
  if (pcontacto != NULL)
    imprimeContacto(pcontacto);
  else
    printf("Nome inexistente.\n");
  free(pnome);
}

/*
trata_r -> remove um contacto
*/
void trata_r(){
  char * pnome;
  elemento * pelemento;
  contacto * pcontacto;
  pnome = leLinha(MAXNOME);
  pcontacto = retornaContacto(pnome, tabelanomes, DIMNOMES);
  if (pcontacto==NULL)
    printf("Nome inexistente.\n");
  else{
    removeElementoTabela(tabeladominio, pcontacto, pcontacto->dominio, DIMDOMINIO);
    removeElementoTabela(tabelanomes, pcontacto, pcontacto->nome, DIMNOMES);
    pelemento = removeElemento(listaprincipal, pcontacto->nome);
    limpaContacto(pelemento->c);
    limpaElemento(pelemento);
  }
  free(pnome);
}

/*
trata_e -> muda email de um contacto
*/
void trata_e(){
  char * pnome, * plocal, * pdominio;
  contacto * pcontacto;
  pnome = leLinha(MAXNOME);
  plocal = leLocal(MAXEMAIL);
  pdominio = leLinha(MAXEMAIL);
  pcontacto = retornaContacto(pnome, tabelanomes, DIMNOMES);
  if (pcontacto != NULL){
    /*se o contacto existir, remove da tabela de dominios, muda o email, e insere
    de novo */
    removeElementoTabela(tabeladominio, pcontacto, pcontacto->dominio, DIMDOMINIO);
    mudaEmail(pcontacto, plocal, pdominio);
    insereElementoTabela(tabeladominio, pcontacto, pcontacto->dominio, DIMDOMINIO);
  }else{
   printf("Nome inexistente.\n");
   free(plocal); free(pdominio);
  }
  free(pnome);
}

/*
trata_c -> conta o numero de ocorrencias de um dominio
*/
void trata_c(){
  char * pdominio;
  pdominio = leLinha(MAXEMAIL);
  contaDominio(pdominio, tabeladominio, DIMDOMINIO);
  free(pdominio);
}


/*
FUNCS GERAIS
*/

/*
leLinha -> le uma linha e devolve um ponteiro para essa linha
*/
char * leLinha(int tamanho){
  char * linha;
  linha = (char *) malloc (sizeof(char) * (tamanho+1));
  scanf("%s", linha);
  linha = (char * ) realloc (linha, sizeof(char) * (strlen(linha) + 1));
  return linha;
}

/*
leLocal -> le um local e devolve um ponteiro para esse local
*/
char * leLocal(int tamanho){
  char * local;
  char c;
  int i;
  local = (char *) malloc (sizeof(char) * (tamanho+1));
  c = getchar();
  for(i=0; (c=getchar())!='@'; i++)
    local[i] = c;
  local[i] = '\0';
  local = (char *) realloc(local, sizeof(char) * (strlen(local) + 1));
  return local;
}
