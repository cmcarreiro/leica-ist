/* Nome: Catarina Carreiro
   Numero: 92438
   Disciplina: Introducao aos Algoritmos e Estruturas de Dados
   Descricao: Implementa funcoes relacionadas com a estrutura de dados contacto,
   elemento e lista
   Ficheiro: lista_contactos.c */

#include "lista_contactos.h"


/*
FUNCOES LIGADAS A CONTACTO
*/

/*
criaContacto -> cria um contacto
*/
contacto * criaContacto(char * nome, char * local, char * dominio, char * numero){
  contacto * novocontacto;
  novocontacto = (contacto *) malloc(sizeof(contacto));
  novocontacto->nome = nome;
  novocontacto->local = local;
  novocontacto->dominio = dominio;
  novocontacto->numero = numero;
  return novocontacto;
}

/*
limpaContacto -> limpa um contacto
*/
void limpaContacto(contacto * pcontacto){
  free(pcontacto->nome);
  free(pcontacto->local);
  free(pcontacto->dominio);
  free(pcontacto->numero);
  free(pcontacto);
}

/*
imprimeContacto -> imprime um contacto
*/
void imprimeContacto(contacto * contacto){
  printf("%s %s@%s %s\n", contacto->nome, contacto->local, contacto->dominio,
  contacto->numero);
}

/*
imprimeContactos -> imprime todos os contactos de uma lista
*/
void imprimeContactos(lista * plista){
  elemento * pelemento;
  for(pelemento = plista->primeiro; pelemento!=NULL; pelemento=pelemento->proximo)
    imprimeContacto(pelemento->c);
}

/*
mudaEmail -> muda o email de um contacto
*/
void mudaEmail(contacto * contacto, char * local, char * dominio){
  free(contacto->local);
  contacto->local = local;
  free(contacto->dominio);
  contacto->dominio = dominio;
}

/*
FUNCS LISTA
*/

/*
inicializaLista - cria inicializa uma lista
*/
lista * inicializaLista(){
  lista * plista;
  plista = (lista *) malloc(sizeof(lista));
  plista->primeiro = NULL;
  plista->ultimo = NULL;
  return plista;
}

/*
criaElemento -> cria um elemento
*/
elemento * criaElemento(contacto * pcontacto){
  elemento * pel;
  pel = (elemento *) malloc(sizeof(elemento));
  pel->c = pcontacto;
  pel->proximo = NULL;
  return pel;
}

/*
insereElemento -> insere um elemento numa lista
*/

void insereElemento(lista * plista, contacto * pcontacto){
  elemento * pel;
  pel = criaElemento(pcontacto);
  if (plista->primeiro == NULL){
    plista->primeiro = pel;
    plista->ultimo = pel;
  }
  else{
    plista->ultimo->proximo = pel;
    plista->ultimo = pel;
  }
}

/*
retornaAnterior -> retorna o elemento anterior, dado o nome de um contacto
*/
elemento * retornaAnterior(char * nome, lista * plista){
  elemento * pelemento;
  for(pelemento = plista->primeiro; pelemento->proximo!=NULL; pelemento=pelemento->proximo){
    if(strcmp(pelemento->proximo->c->nome, nome)==0)
      return pelemento;
  }
  return NULL;
}

/*
removeElemento -> remove o elemento da lista
*/
elemento * removeElemento(lista * plista, char * nome){
  elemento * pel, * paux;
  if (plista->primeiro == NULL)
    return NULL;
  else if (!strcmp(plista->primeiro->c->nome, nome)){
    pel = plista->primeiro->proximo;   /* como quero retirar o primeiro, agarro o proximo */
    paux = plista->primeiro;           /* agarro o primeiro */
    plista->primeiro = pel;            /* o primeiro agora passa a ser o primeiro->proximo */
    if (plista->primeiro == NULL || plista->primeiro->proximo == NULL)
      plista->ultimo = plista->primeiro;
    return paux;                      /* retorno o ex-primeiro */
  } else {
    pel = retornaAnterior(nome, plista);
    if (pel != NULL ){
      paux = pel->proximo;
      pel->proximo = paux->proximo;
        if (!strcmp(plista->ultimo->c->nome, paux->c->nome))
          plista->ultimo = pel;
      return paux;
    }
  }
  return NULL;
}

/*
limpaElemento -> liberta a memoria associada a um elemento
*/
void limpaElemento(elemento * pel){
  free(pel);
}

/*
limpaTudo -> liberta a memoria toda associada a uma lista (incluindo elementos
e contactos)
*/
void limpaTudo(lista * plista){
  elemento * pel;
  while(plista->primeiro != NULL){
    pel = plista->primeiro->proximo;
    limpaContacto(plista->primeiro->c);
    limpaElemento(plista->primeiro);
    plista->primeiro = pel;
  }
  free(plista);
}

/*
limpaLista -> liberta a memoria associada a uma lista
*/
void limpaLista(lista * plista){
  elemento * pel;
  while(plista->primeiro != NULL){
    pel = plista->primeiro->proximo;
    limpaElemento(plista->primeiro);
    plista->primeiro = pel;
  }
  free(plista);
}
