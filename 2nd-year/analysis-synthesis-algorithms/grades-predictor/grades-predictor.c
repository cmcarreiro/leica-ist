/* ASA 19/20 - 2o semestre
Grupo 23
Catarina Carreiro   92438
Cristiano Clemente  92440
proj1.c */

#include <stdio.h>
#include <stdlib.h>
/*#include <sys/time.h>   // for gettimeofday()*/

#define FALSO 0
#define VERDADEIRO 1

typedef struct No { /*no de lista ligada*/
    int valor;
    struct No* proximo_no;
} no;

typedef struct Grafo {          /*grafo representado por listas de adjacencia*/
    int num_vertices;
    int* visitado;              /*usado no algoritmo DFS*/
    int* valores;               /*cada vertice do grafo tem um inteiro correspondente*/
    no** listas_adjacencia;
} grafo;

int max(int a, int b);
no* criaNo(int valor);

grafo* criaGrafo(int num_vertices);
void associaValor(grafo* grafo, int indice, int valor);
void adicionaAresta(grafo* grafo, int origem, int destino);
void DFS(grafo* grafo);
void visitaDFS(grafo* grafo, int v_pai);
void imprimeValores(grafo* grafo);
void libertaGrafo(grafo* grafo);

int main() {
    /*struct timeval start, end;*/
    /*gettimeofday(&start, NULL);*/
    int i, num_alunos, num_amizades;
    if(!scanf("%d,%d", &num_alunos, &num_amizades))
        return -1;
    grafo* turma = criaGrafo(num_alunos);
    int nota;
    for(i=1; i<=num_alunos; i++) {
        if(!scanf("%d", &nota))
            return -1;
        associaValor(turma, i, nota);
    }
    int aluno, amigo;
    for(i=1; i<=num_amizades; i++) {
        if(!scanf("%d %d", &aluno, &amigo))
            return -1;
        adicionaAresta(turma, aluno, amigo);
    }
    DFS(turma);
    imprimeValores(turma);
    libertaGrafo(turma);
    /*gettimeofday(&end, NULL);*/
    /*long elapsed = (end.tv_sec-start.tv_sec)*1000000 + (end.tv_usec-start.tv_usec);*/
	/*printf("%f\n", (float)elapsed/1000000);*/
    return 0;
}

int max(int a, int b) {
    return a>b ? a:b;
}

no* criaNo(int valor) {
    no* novo_no = malloc(sizeof(no));
    novo_no->valor = valor;
    novo_no->proximo_no = NULL;
    return novo_no;
}

grafo* criaGrafo(int num_vertices) {
    grafo* g = (grafo*) malloc(sizeof(grafo));
    g->num_vertices = num_vertices;
    g->visitado = (int*) malloc((num_vertices+1) * sizeof(int));
    g->valores = (int*) malloc((num_vertices+1) * sizeof(int));
    g->listas_adjacencia = (no**) malloc((num_vertices+1) * sizeof(no*));

    int i;
    for (i=0; i<=num_vertices; i++) {
        g->visitado[i] = FALSO;
        g->listas_adjacencia[i] = NULL;
    }
    return g;
}

void associaValor(grafo* g, int indice, int valor) {
    g->valores[indice] = valor;
}

void adicionaAresta(grafo* g, int origem, int destino) { /*adiciona no ao inicio da lista ligada*/
    no* novo_no = criaNo(destino);
    novo_no->proximo_no = g->listas_adjacencia[origem];
    g->listas_adjacencia[origem] = novo_no;
}

void DFS(grafo* g) {
    int v;
    for (v=1; v<=g->num_vertices; v++)
        if (!g->visitado[v])
            visitaDFS(g, v);
}

void visitaDFS(grafo* g, int n_pai){
    g->visitado[n_pai] = VERDADEIRO;
    no* v_filho = g->listas_adjacencia[n_pai];
    while(v_filho != NULL) {
        if(!g->visitado[v_filho->valor])
            visitaDFS(g, v_filho->valor);
        g->valores[n_pai] = max(g->valores[n_pai], g->valores[v_filho->valor]);
        v_filho = v_filho->proximo_no;
    }
}

void imprimeValores(grafo* g) {
    int v;
    for (v=1; v<=g->num_vertices; v++)
        printf("%d\n", g->valores[v]);
}

void libertaGrafo(grafo* g) {
    int i;
    no *no, *proximo_no;
    for (i=0; i<=g->num_vertices; i++) {
        no = g->listas_adjacencia[i];
        while(no != NULL) {
            proximo_no = no->proximo_no;
            free(no);
            no = proximo_no;
        }
    }
    free(g->listas_adjacencia);
    free(g->visitado);
    free(g->valores);
    free(g);
}
