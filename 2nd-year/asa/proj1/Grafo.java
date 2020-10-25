/*
ASA 2019/2020
Catarina Carreiro   92438
Cristiano Clemente  92440
Grupo               23
*/

import java.util.Scanner;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.Math;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;


public class Grafo{

  private int _numvertices;
  private ArrayList<LinkedList<Integer>> _adj;
  private int[] _vals;

  Grafo(int numvertices) {
    _numvertices = numvertices;
    _adj = new ArrayList<LinkedList<Integer>>(numvertices+1);
    for (int i = 0; i < _numvertices + 1; i++){
      _adj.add(new LinkedList<Integer>());
    }
    _vals = new int[numvertices + 1];
  }

  void adicionarAresta(int orig, int destino){
    _adj.get(orig).add(destino);
  }

  void associarValor(int indice, int val){
    _vals[indice] = val;
  }

  void imprimeVals(){
    for (int i = 1; i < _numvertices + 1 ; i++){
      System.out.println(_vals[i]);
    }
  }

  void DFS(){
    boolean visitado[] = new boolean[_numvertices+1];
    for(int v = 1; v < _numvertices + 1; v++){
      if (!visitado[v])
        visitarDFS(v, visitado);
    }
  }

  void visitarDFS(int v, boolean visitado[]){
    visitado[v] = true;
    Iterator<Integer> veradj = _adj.get(v).listIterator();
    while(veradj.hasNext()){
      int next = veradj.next();
      if(!visitado[next])
        visitarDFS(next, visitado);
      _vals[v] = Math.max(_vals[v], _vals[next]);
    }
  }



  public static void main(String[] args) {

      BufferedReader input = new BufferedReader(new InputStreamReader (System.in));
      String inputString;
      try {
         inputString = input.readLine();
      } catch(IOException e) {
        return;
      }
      String[] strs = inputString.trim().split("\\D");

      int num_alunos = Integer.parseInt(strs[0]);
      int num_amizades = Integer.parseInt(strs[1]);

      Grafo turma = new Grafo(num_alunos);

      for(int i = 1; i <= num_alunos; i++){
        try {
           inputString = input.readLine();
        } catch(IOException e) {
          return;
        }
        turma.associarValor(i, Integer.parseInt(inputString));
      }

      for(int i = 0; i < num_amizades; i++){
        try {
           inputString = input.readLine();
        } catch(IOException e) {
          return;
        }
        strs = inputString.trim().split("\\D");
        turma.adicionarAresta(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
      }



      try{
        input.close();
      } catch (IOException e) {
        return;
      }




      turma.DFS();
      turma.imprimeVals();


  }

}
