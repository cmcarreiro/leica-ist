#include <iostream>
#include <string.h>
#include <algorithm>
#include <list>
#include <vector>
#include <queue>
using namespace std;

int m, n, V;
list<int> *adj;

void criaGrafo() {
  V = 2*m*n+2;
  adj = new list<int>[1 + 2*m*n + 1];
  for(int i=1; i<=2*m*n-1; i+=2)
    adj[i].push_back(i+1);
  for(int l=0; l<=n-1; l++)
    for(int c=2; c<=2*m-2; c+=2) {
      adj[l*2*m+c].push_back(l*2*m+c+1);
      adj[l*2*m+c+2].push_back(l*2*m+c-1);
    }
  for(int l=0; l<=n-2; l++)
    for(int c=2; c<=2*m; c+=2) {
      adj[l*2*m+c].push_back((l+1)*2*m+c-1);
      adj[(l+1)*2*m+c].push_back(l*2*m+c-1);
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
  if(find(adj[supS].begin(), adj[supS].end(), 2*m*n+1) == adj[supS].end())
    adj[supS].push_back(2*m*n+1);
}

void adicionarCidadao(int c, int l) {
  int cidE = calcE(c, l);
  if(find(adj[0].begin(), adj[0].end(), cidE) == adj[0].end())
    adj[0].push_back(cidE);
}

void printAdj() {
  for(int i=0; i<2*m*n + 2; i++) {
    printf("%d:\t", i);
    for(int j: adj[i]) {
        printf("%d ", j);
      }
    printf("\n");
  }
}



/* Returns true if there is a path from source 's' to sink 't' in
  residual graph. Also fills parent[] to store the path */
bool bfs(vector<vector<int>> rGraph, int s, int t, int parent[])
{
    // Create a visited array and mark all vertices as not visited
    bool visited[V];
    memset(visited, 0, sizeof(visited));

    // Create a queue, enqueue source vertex and mark source vertex
    // as visited
    queue <int> q;
    q.push(s);
    visited[s] = true;
    parent[s] = -1;

    // Standard BFS Loop
    while (!q.empty())
    {
        int u = q.front();
        q.pop();

        for (int v=0; v<V; v++)
        {
            if (visited[v]==false && rGraph[u][v] > 0)
            {
                q.push(v);
                parent[v] = u;
                visited[v] = true;
            }
        }
    }

    // If we reached sink in BFS starting from source, then return
    // true, else false
    return (visited[t] == true);
}

// Returns the maximum flow from s to t in the given graph
int fordFulkerson(vector<vector<int>> graph) {
    int s = 0;
    int t = V-1;
    int u, v;

    vector<int> v1;
    for(int i=0; i<V; i++) v1.push_back(0);

    vector<vector<int>> rGraph;
    for(int i=0; i<V; i++) rGraph.push_back(v1);

    for (u = 0; u < V; u++)
      for (v = 0; v < V; v++)
        rGraph[u][v] = graph[u][v];

    int parent[V];  // This array is filled by BFS and to store path

    int max_flow = 0;  // There is no flow initially

    while (bfs(rGraph, s, t, parent)) {
        int path_flow = 1;
        for (v=t; v!=s; v=parent[v]) {
            u = parent[v];
            path_flow = min(path_flow, rGraph[u][v]);
        }

        for (v=t; v != s; v=parent[v]) {
            u = parent[v];
            rGraph[u][v] -= path_flow;
            rGraph[v][u] += path_flow;
        }
        max_flow += path_flow;
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
    //printAdj();

    vector<int> v1;
    for(int i=0; i<V; i++) v1.push_back(0);

    vector<vector<int>> graph;
    for(int i=0; i<V; i++) graph.push_back(v1);

    //graph[i] = new vector<int>[V];
    //int graph[2*m*n+2][2*m*n+2];
    for(int z = 0; z < V; z++)
      for (int j = 0; j < V; j++ )
        if(find(adj[z].begin(), adj[z].end(), j) != adj[z].end()) graph[z][j] = 1;

    /*for (unsigned int i = 0; i < graph.size(); i++) {
        for (unsigned int j = 0; j < graph[i].size(); j++)
            cout << graph[i][j] << " ";
        cout << endl;
    }*/
    printf("%d\n", fordFulkerson(graph));
    //printf("%d\n", fordFulkersonBig());
    //printf("%d", cidade.calcularFluxoMaximo());
    return 0;
}
