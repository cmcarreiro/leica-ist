/* Nome: Catarina Carreiro
   Numero: 92438
   Disciplina: Introducao aos Algoritmos e Estruturas de Dados
   Descricao: Implementa as funcoes relacionadas com tabelas Hash
   Ficheiro: hashtable.c */

#include "hashtable.h"


/*
retornaHash -> retorna a chave hash associada aquela string
*/
unsigned long retornaHash(char * str){
    unsigned long hash = 5381;
    int c;
    while ((c = *str++))
      hash = ((hash << 5) + hash) + c; /* hash * 33 + c */
    return hash;
}

/*
inicializaTabela -> inicializa uma tabela
*/
void inicializaTabela(lista ** tabela, int dim){
  int i;
  for (i=0; i<dim; i++)
    tabela[i] = NULL;
}

/*
limpaTabela -> liberta a memoria associada a tabela
*/
void limpaTabela(lista ** tabela, int dim){
  int i;
  for (i=0; i<dim; i++)
    if(tabela[i]!=NULL)
      limpaLista(tabela[i]);
}

/*
insereElementoTabela -> insere um elemento na tabela especificada
*/
void insereElementoTabela(lista ** tabela, contacto * pcontacto, char * linha, int dim){
  unsigned long chave;
  chave = retornaHash(linha) % dim;
  if (tabela[chave] == NULL)
    tabela[chave] = inicializaLista();
  insereElemento(tabela[chave], pcontacto);
}

/*
removeElementoTabela -> remove um elemento da tabela especificada
*/
void removeElementoTabela(lista ** tabela, contacto * pcontacto, char * linha, int dim){
  unsigned long chave;
  elemento * pel = NULL;
  chave = retornaHash(linha) % dim;
  if (tabela[chave] != NULL){
    pel = removeElemento(tabela[chave], pcontacto->nome);
    limpaElemento(pel);
  }
}

/*
retornaContacto -> retorna um contacto da tabela especificada
*/
contacto * retornaContacto(char * nome, lista ** tabela, int dim){
  unsigned long chave;
  elemento * pelemento;
  chave = retornaHash(nome) % dim;
  if (tabela[chave] == NULL){
    return NULL;
  } else {
    for(pelemento = tabela[chave]->primeiro; pelemento!=NULL; pelemento=pelemento->proximo){
      if(strcmp(pelemento->c->nome, nome)==0)
        return pelemento->c;
      }
  }
  return NULL;
}

/*
contaDominio -> conta o numero de ocorrencias de um dominio
*/
void contaDominio(char * dominio, lista ** tabela, int dim){
  int conta=0;
  elemento * pelemento;
  unsigned long chave;
  chave = retornaHash(dominio) % dim;
  if (tabela[chave] != NULL){
    for(pelemento = tabela[chave]->primeiro; pelemento!=NULL; pelemento=pelemento->proximo){
      if(strcmp(pelemento->c->dominio, dominio)==0)
        conta++;
    }
  }
  printf("%s:%d\n", dominio, conta);
}
