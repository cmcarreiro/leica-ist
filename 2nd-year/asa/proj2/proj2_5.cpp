#include <iostream>
#include <string.h>
#include <list>
using namespace std;

class Grafo {
    int num_vertices;
    int num_colunas;
    int num_linhas;
    int num_supermercados;
    int num_cidadaos;
    list<int> *adj;
    int* tipo;
    int* cidadaos;
    list<int> *sup;

    void DFS(int v);
    void DFSUtil(int v, bool visitado[], int c);
    int calcularPosicao(int x, int y);
    int iMenorListaNaoVazia();
    bool bpm(int u, bool seen[], int matchR[]);

public:
    Grafo(int num_colunas, int num_linhas, int num_supermercados, int num_cidadaos);
    void adicionarAresta(int u, int v);
    void adicionarTodasArestas();
    void adicionarSupermercado(int x, int y);
    void adicionarCidadao(int x, int y);
    void DFSCidadaos();
    void imprimirSup();
    int emparelharSupermercadoCidadao();
    int maxBPM();
};

Grafo::Grafo(int num_colunas, int num_linhas, int num_supermercados, int num_cidadaos) {
    this->num_colunas = num_colunas;
    this->num_linhas = num_linhas;
    this->num_vertices = num_colunas*num_linhas;
    this->num_supermercados = num_supermercados;
    this->num_cidadaos = num_cidadaos;
    adj = new list<int>[num_vertices];
    tipo = new int[num_vertices];
    cidadaos = new int[num_cidadaos];
    for(int i=0; i<num_vertices; i++) tipo[i] = -2;
    sup = new list<int>[num_supermercados];
}

void Grafo::adicionarAresta(int u, int v) {
    adj[u].push_back(v); //adiciona ao fim da lista
}

void Grafo::adicionarTodasArestas() {
  for(int linha=0; linha<num_linhas; linha++)
    for(int coluna=0; coluna<num_colunas-1; coluna++) {
      adicionarAresta(linha*num_colunas+coluna, linha*num_colunas+coluna+1);
      adicionarAresta(linha*num_colunas+coluna+1, linha*num_colunas+coluna);
    }
  for(int coluna=0; coluna<num_colunas; coluna++)
    for(int linha=0; linha<num_linhas-1; linha++) {
      adicionarAresta(linha*num_colunas+coluna, (linha+1)*num_colunas+coluna);
      adicionarAresta((linha+1)*num_colunas+coluna, linha*num_colunas+coluna);
    }
}

void Grafo::DFSCidadaos() {
  for (int i=0;  i<num_cidadaos; i++) {
    DFS(cidadaos[i]);
  }
}

void Grafo::DFS(int v) {
    bool *visitado = new bool[num_vertices];
    for (int i = 0; i < num_vertices; i++)
        visitado[i] = false;
    DFSUtil(v, visitado , v);
    //printf("\n");
}

void Grafo::DFSUtil(int v, bool visitado[], int c) {
    visitado[v] = true;

    //printf("%d ", v);

    if(v!=c && tipo[v] == -1)
      return;
    else if(tipo[v] >= 0) {
        sup[tipo[v]].push_back(c);
      return;
    }

    list<int>::iterator i;
    for (i = adj[v].begin(); i != adj[v].end(); ++i)
        if (!visitado[*i])
            DFSUtil(*i, visitado, c);
}

int Grafo::calcularPosicao(int x, int y) {
  return (y-1)*this->num_colunas + (x-1);
}

void Grafo::adicionarSupermercado(int x, int y) {
  static int i_sup = 0;
  int pos = calcularPosicao(x, y);
  if(tipo[pos] >= 0) {
    num_supermercados--;
  }
  else {
    tipo[pos] = i_sup;
    i_sup++;
  }
}

void Grafo::adicionarCidadao(int x, int y) {
  static int i_cid = 0;
  int pos = calcularPosicao(x, y);
  if(tipo[pos] == -1) {
    num_cidadaos--;
  }
  else {
    tipo[pos] = -1;
    cidadaos[i_cid] = pos;
    i_cid++;
  }
}

void Grafo::imprimirSup() {
  for(int i=0; i<num_supermercados; i++) {
    printf("%d: ", i);
    for(int j: sup[i]) {
        printf("%d ", j);
      }
    printf("\n");
  }
}

int Grafo::iMenorListaNaoVazia() {
  unsigned int min_comp = num_cidadaos+1;
  int i_menor = -1;
  unsigned int size;
  for(int i=0; i<num_supermercados; i++) {
    size = sup[i].size();
    if(size<min_comp && size>0) {
      min_comp = sup[i].size();
      i_menor = i;
    }
  }
  return i_menor;
}

int Grafo::emparelharSupermercadoCidadao() {
  int i;
  int c;
  int resultado = 0;
  while((i=iMenorListaNaoVazia()) >= 0) {
    resultado++;
    c = sup[i].front();
    sup[i].clear();
    for(int i=0; i<num_supermercados; i++) sup[i].remove(c); //o erro esta aqui
  }
  return resultado;
}

int Grafo::maxBPM() {
    int matchR[num_vertices];
    memset(matchR, -1, sizeof(matchR));

    int result = 0;
    for (int u = 0; u < num_supermercados; u++)
    {
        bool seen[num_vertices];
        memset(seen, 0, sizeof(seen));

        if (bpm(u, seen, matchR))
            result++;
    }
    return result;
}

bool Grafo::bpm(int u, bool seen[], int matchR[]) {
    list<int>::iterator v;
    //printf("%d: ", u);
    for (v = sup[u].begin(); v != sup[u].end(); ++v) {
      //printf("%d ", *v);
        if (!seen[*v]) {
            seen[*v] = true;

            if (matchR[*v] < 0 || bpm(matchR[*v], seen, matchR)) {
              //printf("match %d : %d\n", u, *v);
                matchR[*v] = u;
                return true;
            }
        }
    }
    //printf("\n");
    return false;
}

int main() {
    int num_colunas, num_linhas, num_supermercados, num_cidadaos;
    if(!scanf("%d %d %d %d", &num_colunas, &num_linhas, &num_supermercados, &num_cidadaos)) return -1;
    Grafo cidade(num_colunas, num_linhas, num_supermercados, num_cidadaos);
    cidade.adicionarTodasArestas();
    int x, y;
    for(int s=0; s<num_supermercados; s++) {
      if(!scanf("%d %d", &x, &y)) return -1;
      cidade.adicionarSupermercado(x, y);
    }
    for(int c=0; c<num_cidadaos; c++) {
      if(!scanf("%d %d", &x, &y)) return -1;
      cidade.adicionarCidadao(x, y);
    }
    cidade.DFSCidadaos();
    //cidade.imprimirSup();
    //printf("%d\n", cidade.emparelharSupermercadoCidadao());
    //int res = cidade.emparelharSupermercadoCidadao();
    //printf("%d\n", res);
    printf("%d\n", cidade.maxBPM());
    return 0;
}
