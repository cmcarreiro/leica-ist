#include <iostream>
#include <string.h>
#include <algorithm>
#include <list>
#include <vector>
#include <queue>
using namespace std;

int m, n, V, s, t;
//list<int> *adj;
//vector<vector<int>> rGraph;
vector<int> *rGraph;
vector<int> parent;
vector<bool> visited;

void criaGrafo() {
  s = 0;
  V = 2*m*n+2;
  t = V-1;
  rGraph = new vector<int>[V];
  for(int i=1; i<=2*m*n-1; i+=2)
    rGraph[i].push_back(i+1);
  for(int l=0; l<=n-1; l++)
    for(int c=2; c<=2*m-2; c+=2) {
      rGraph[l*2*m+c].push_back(l*2*m+c+1);
      rGraph[l*2*m+c+2].push_back(l*2*m+c-1);
    }
  for(int l=0; l<=n-2; l++)
    for(int c=2; c<=2*m; c+=2) {
      rGraph[l*2*m+c].push_back((l+1)*2*m+c-1);
      rGraph[(l+1)*2*m+c].push_back(l*2*m+c-1);
    }
}

int calcE(int c, int l) {
  return (l-1)*2*m + 2*c-1;
}
int calcS(int c, int l) {
  return (l-1)*2*m + 2*c;
}

void adicionarSupermercado(int c, int l) {
  int supS = calcS(c, l);
  if(find(rGraph[supS].begin(), rGraph[supS].end(), 2*m*n+1) == rGraph[supS].end())
    rGraph[supS].push_back(2*m*n+1);
}

void adicionarCidadao(int c, int l) {
  int cidE = calcE(c, l);
  if(find(rGraph[0].begin(), rGraph[0].end(), cidE) == rGraph[0].end())
    rGraph[0].push_back(cidE);
}

void printRGraph() {
  for(int i=0; i<2*m*n + 2; i++) {
    printf("%d:\t", i);
    for(int j: rGraph[i]) {
        printf("%d ", j);
      }
    printf("\n");
  }
}

bool bfs() {
    visited.clear();

    queue <int> q;
    q.push(s);
    visited.push_back(true);
    for(int i=1; i<V; i++) visited.push_back(false);
    parent[s] = -1;

    while (!q.empty()) {
        int u = q.front();
        q.pop();

        for (unsigned int i=0; i<rGraph[u].size(); i++) {
        //for (int v=0; v<V; v++) {
            int v = rGraph[u][i];
            if (visited[v] == false) {
                q.push(v);
                parent[v] = u;
                visited[v] = true;
            }
        }
    }
    return (visited[t] == true);
}

// Returns the maximum flow from s to t in the given graph
int fordFulkerson() {
    int u, v;
    int max_flow = 0;

    for(int i=0; i<V; i++) parent.push_back(0);

    while (bfs()) {
        for (v=t; v != s; v=parent[v]) {
            u = parent[v];
            rGraph[u].erase(remove(rGraph[u].begin(), rGraph[u].end(), v), rGraph[u].end());
            rGraph[v].push_back(u);
        }
        max_flow += 1;
    }
    return max_flow;
}

int main() {
    int s, c;
    if(!scanf("%d %d %d %d", &m, &n, &s, &c)) return -1;
    criaGrafo();
    int x, y;
    for(int i=0; i<s; i++) {
      if(!scanf("%d %d", &x, &y)) return -1;
      adicionarSupermercado(x, y);
    }
    for(int i=0; i<c; i++) {
      if(!scanf("%d %d", &x, &y)) return -1;
      adicionarCidadao(x, y);
    }
    printf("%d\n", fordFulkerson());
    return 0;
}
