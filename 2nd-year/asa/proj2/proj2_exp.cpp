// ASA 19/20 Proj2
// Alameda Grupo 23
// Catarina Carreiro    92438
// Cristiano Clemente   92440

#include <iostream>     //scanf, cout
#include <algorithm>    //find, remove
#include <vector>
#include <queue>
/*#include <sys/time.h>   // for gettimeofday()*/
using namespace std;

int m, n, num_s, num_c, V, s, t;
vector<int> *grafoR;
vector<int> pai;
vector<bool> visitado;

void adicionarArestas() {
  for(int i=1; i<=2*m*n-1; i+=2)
    grafoR[i].push_back(i+1);
  for(int l=0; l<=n-1; l++)
    for(int c=2; c<=2*m-2; c+=2) {
      grafoR[l*2*m+c].push_back(l*2*m+c+1);
      grafoR[l*2*m+c+2].push_back(l*2*m+c-1);
    }
  for(int l=0; l<=n-2; l++)
    for(int c=2; c<=2*m; c+=2) {
      grafoR[l*2*m+c].push_back((l+1)*2*m+c-1);
      grafoR[(l+1)*2*m+c].push_back(l*2*m+c-1);
    }
}

void criarGrafo() {
  if(!scanf("%d %d %d %d", &m, &n, &num_s, &num_c)) cout << "Erro ao criar grafo.";
  s = 0;
  V = 2*m*n+2;
  t = V-1;
  grafoR = new vector<int>[V];
  adicionarArestas();
}

int calcE(int c, int l) {
  return (l-1)*2*m + 2*c-1;
}

int calcS(int c, int l) {
  return (l-1)*2*m + 2*c;
}

void adicionarSupermercado(int c, int l) {
  int supS = calcS(c, l);
  if(find(grafoR[supS].begin(), grafoR[supS].end(), 2*m*n+1) == grafoR[supS].end())
    grafoR[supS].push_back(2*m*n+1);
}

void adicionarSupermercados() {
  int x, y;
  for(int i=0; i<num_s; i++) {
    if(!scanf("%d %d", &x, &y)) cout << "Erro ao adicionar supermercados.";
    adicionarSupermercado(x, y);
  }
}

void adicionarCidadao(int c, int l) {
  int cidE = calcE(c, l);
  if(find(grafoR[0].begin(), grafoR[0].end(), cidE) == grafoR[0].end())
    grafoR[0].push_back(cidE);
}

void adicionarCidadaos() {
  int x, y;
  for(int i=0; i<num_c; i++) {
    if(!scanf("%d %d", &x, &y)) cout << "Erro ao adicionar cidadaos.";
    adicionarCidadao(x, y);
  }
}

bool existeCaminhoAumento() { //bfs
  visitado.clear();
  queue <int> q;
  q.push(s);
  visitado.push_back(true);
  for(int i=1; i<V; i++) visitado.push_back(false);
  pai[s] = -1;
  while(!q.empty()) {
    int u = q.front();
    q.pop();
    for(unsigned int i=0; i<grafoR[u].size(); i++) {
      int v = grafoR[u][i];
      if(visitado[v] == false) {
        q.push(v);
        pai[v] = u;
        visitado[v] = true;
      }
    }
  }
  return (visitado[t] == true);
}

int calcularFluxoMaximo() { //edmonds-karp
  int u, v;
  int flux_max = 0;
  for(int i=0; i<V; i++) pai.push_back(0);
  while(existeCaminhoAumento()) {
    for (v=t; v != s; v=pai[v]) {
      u = pai[v];
        grafoR[u].erase(remove(grafoR[u].begin(), grafoR[u].end(), v), grafoR[u].end());
        grafoR[v].push_back(u);
    }
    flux_max += 1;
  }
  return flux_max;
}

int main() {
  /*struct timeval start, end;
  gettimeofday(&start, NULL);*/
  criarGrafo();
  adicionarSupermercados();
  adicionarCidadaos();
  cout << calcularFluxoMaximo() << endl;
  /*gettimeofday(&end, NULL);
  long elapsed = (end.tv_sec-start.tv_sec)*1000000 + (end.tv_usec-start.tv_usec);
	printf("%f\n", (float)elapsed/1000000);*/
  return 0;
}
