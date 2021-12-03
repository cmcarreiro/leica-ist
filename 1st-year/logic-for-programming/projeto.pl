%Catarina Carreiro 92438

:- consult(codigo_comum).

%num_vars(L, N): conta o numero N de variaveis numa lista L
num_vars([],0) :- !.
num_vars([P|R], N):-
  var(P), !, num_vars(R, N1), N is N1+1.
num_vars([_|R], N):-
  num_vars(R, N).

%num_el(El, L, N): conta o numero N de um certo elemento El numa lista L
num_el(_,[],0):- !.
num_el(El, L, N):-
    L = [P|R], \+var(P), El == P,
    num_el(El, R, N1), N is N1+1, !.
num_el(El, L, N):-
    L = [_|R], num_el(El, R, N).

%verificia_triplo(Triplo): verifica se um triplo tem dois 0s ou dois 1s
verifica_triplo(Triplo):-
  num_el(0, Triplo, 2), !, num_vars(Triplo, 1).
verifica_triplo(Triplo):-
  num_el(1, Triplo, 2), !, num_vars(Triplo, 1).

%preenche_triplo(Triplo, Res): preenche um triplo com o elemento necessario,
%ou seja, se tiver dois 0s preenche com um 1, se tiver dois 1s, preenche com um 0
preenche_triplo([X,Y,Z],Res):-
  var(X), member(0, [Y,Z]), NX = 1, Res = [NX, Y, Z], !;
  var(X), member(1, [Y,Z]), NX = 0, Res = [NX, Y, Z], !;
  var(Y), member(0, [X,Z]), NY = 1, Res = [X, NY, Z], !;
  var(Y), member(1, [X,Z]), NY = 0, Res = [X, NY, Z], !;
  var(Z), member(0, [X,Y]), NZ = 1, Res = [X, Y, NZ], !;
  var(Z), member(1, [X,Y]), NZ = 0, Res = [X, Y, NZ], !.

%aplica_R1_triplo(Triplo, R): retorna o triplo dado se tiver ja
%estiver preenchido, ou se tiver um 0, um 1, e uma variavel, ou so variaveis
aplica_R1_triplo(Triplo, R):-
  (num_el(0, Triplo, 2), num_el(1, Triplo, 1);
   num_el(1, Triplo, 2), num_el(0, Triplo, 1);
   num_vars(Triplo,2); num_vars(Triplo, 3);
   num_el(0, Triplo, 1), num_el(1, Triplo, 1), num_vars(Triplo, 1)),
  R = Triplo, !.

%aplica_R1_triplo(Triplo, R): aplica a regra 1, ou seja, aplica o
%verifica_triplo e o preenche_triplo
aplica_R1_triplo(Triplo, R):-
  verifica_triplo(Triplo), preenche_triplo(Triplo, R), !.

%aplica_R1_fila_aux(Lista, N_Lista): vai aplicando o aplica_R1_triplo
%sucessivamente aos elementos da Lista
aplica_R1_fila_aux([X,Y,Z,W|R], [A|N]) :-
  !, aplica_R1_triplo([X,Y,Z], [A,B,C]), aplica_R1_fila_aux([B,C,W|R], N).
aplica_R1_fila_aux(Triplo, N_Triplo):-
  aplica_R1_triplo(Triplo, N_Triplo).

%aplica_R1_fila(LI, LF): preenche sucessivamente a fila LI
aplica_R1_fila(LI, LF):-
  %verifica se o numero de variaveis mudou
  num_vars(LI, NI), aplica_R1_fila_aux(LI, LF), num_vars(LF, NF),
  NI =:= NF, !.
aplica_R1_fila(LI, Res):-  %se ainda ha mudancas a fazer
  num_vars(LI, NI),
  aplica_R1_fila_aux(LI, LF),
  num_vars(LF, NF), NI =\= NF, !,
  aplica_R1_fila(LF, Res).

%preenche_fila(El, LI, LF): preenche fila LI com o elemento El e retorna LF
preenche_fila(_,[],[]):- !.
preenche_fila(El, [P|R], [F|L]):-
  var(P), F = El, preenche_fila(El, R, L), !.
preenche_fila(El, [P|R], [F|L]):-
  \+var(P), F = P, preenche_fila(El, R, L), !.

%aplica_R2_fila(Fila, N_Fila): aplica a regra 2 a uma fila; se metade da fila
%ja estiver preenchida com 1s, preenche o resto com 0 e vice-versa;
%se tiver mais do que metade preenchido com 1s ou 0s, retorna falso
aplica_R2_fila(Fila, N_Fila):-
  length(Fila, X), N is X / 2, num_el(1, Fila, Num), Num =:= N, !,
  preenche_fila(0, Fila, N_Fila).
aplica_R2_fila(Fila, N_Fila):-
  length(Fila, X), N is X / 2, num_el(0, Fila, Num), Num =:= N, !,
  preenche_fila(1, Fila, N_Fila).
aplica_R2_fila(Fila, Fila):-
  length(Fila, X), N is X / 2, num_el(1, Fila, Num1), num_el(0, Fila, Num2),
  Num1 < N, Num2<N, !.

%aplica_R1_R2_fila(Fila, N_Fila): aplica a regra 1 e a regra 2 a uma fila
aplica_R1_R2_fila(Fila, N_Fila):-
  aplica_R1_fila(Fila, N), !,
  aplica_R2_fila(N, N_Fila).

%aplica_R1_R2_puzzle_linhas(Lista_Linhas, L_L_Final): aplica as regras R1  e R2 as
%linhas todas de um puzzle
aplica_R1_R2_puzzle_linhas([],[]).
aplica_R1_R2_puzzle_linhas([P|R],[F|L]):-
  aplica_R1_R2_fila(P,F), aplica_R1_R2_puzzle_linhas(R, L).

%aplica_R1_R2_puzzle(Puz, N_Puz): aplica as regras R1 e R2 a todas as linhas e
%colunas de um puzzle (usa a transposta para poder preencher as colunas)
aplica_R1_R2_puzzle(Puz, N_Puz):-
  aplica_R1_R2_puzzle_linhas(Puz, A), mat_transposta(A,B),
  aplica_R1_R2_puzzle_linhas(B, C), mat_transposta(C, N_Puz).

%num_vars_puzzle(Puz, NumVar): conta o numero de variaveis num puzzle
num_vars_puzzle([], 0):- !.
num_vars_puzzle([P|R], NumVar):-
  num_vars(P, NV), num_vars_puzzle(R, Vars), NumVar is Vars + NV, !.

%inicializa(Puz, N_Puz): vai aplicando a R1 e R2 a Puz ate ao numero de
%variaveis do puzzle antes das regras serem aplicadas e depois
%das regras serem aplicadas serem iguais
inicializa(Puz, N_Puz):-
  num_vars_puzzle(Puz, N1), aplica_R1_R2_puzzle(Puz, N_Puz),
  num_vars_puzzle(N_Puz, N2), N1 =:= N2, !.
inicializa(Puz, N_Puz):-
  aplica_R1_R2_puzzle(Puz, N_Puz), inicializa(N_Puz, N_Puz).

%verifica_diferencas(Puz): verifica se todas as linhas numa matriz sao diferentes
verifica_diferencas([_]):- !.
verifica_diferencas([P,S|R]):-
  \+(P == S), verifica_diferencas([P|R]), verifica_diferencas([S|R]), !.

%verifica_R3(Puz): verifica a regra R3
verifica_R3(L):-
  verifica_diferencas(L),
  mat_transposta(L, S),
  verifica_diferencas(S), !.


%descobre_diferencas(L1, L2, Pos): descobre a posicao em que as listas diferem
descobre_diferencas([P|_], [L|_], 1):-
  \+((var(P), var(L) ; P == L)).
descobre_diferencas([_|R], [_|T], I):-
  descobre_diferencas(R, T, I1), I is I1 + 1.

%transf_coord_linha(L, X, N): transforma L, X numa coordenada
transf_coord_linha(L, X, N):-
  N = (L, X).

%diferenca_linhas(L1, L2, L, Pos): devolva a lista com as posicoes diferentes da linha
diferenca_linhas(L1, L2, L, Pos):-
  bagof(N, descobre_diferencas(L1, L2, N), Lista), !,
  maplist(transf_coord_linha(L), Lista, Pos).
diferenca_linhas(_,_,_,[]).

%transf_coord_col(C, X, N): transforma C, X numa coordenada
transf_coord_col(C, X, N):-
  N = (X, C).

%diferenca_colunas(L1, L2, C, Pos): devolva a lista com as posicoes diferentes da coluna
diferenca_colunas(L1, L2, C, Pos):-
  bagof(N, descobre_diferencas(L1, L2, N), Lista), !,
  maplist(transf_coord_col(C), Lista, Pos).
diferenca_colunas(_,_,_,[]).

%propaga_posicoes(Pos, Puz, N_Puz): recebe uma lista de posicoes e vai aplicando
%R1 e R2 a linha e coluna de cada uma das posicoes
propaga_posicoes([],_,_).
propaga_posicoes([(L,C)|R],Puz,N_Puz):-
  nth1(L, Puz, Linha_In),                               %descobre a linha L
  aplica_R1_R2_fila(Linha_In, Linha_Out),
  diferenca_linhas(Linha_In, Linha_Out, L, Pos_Linhas), %retorna as posicoes que mudaram
  mat_muda_linha(Puz, L, Linha_Out, N_Puz_1),           %modifica a linha
  mat_elementos_coluna(N_Puz_1, C, Coluna_In),          %descobre a coluna C
  aplica_R1_R2_fila(Coluna_In, Coluna_Out),
  diferenca_colunas(Coluna_In, Coluna_Out, C, Pos_Coluna),
  mat_muda_coluna(N_Puz_1, C, Coluna_Out, N_Puz), !,
  append(Pos_Linhas, Pos_Coluna, X),                    %une as listas com posicoes que mudaram
  append(X, R, Y),
  propaga_posicoes(Y, N_Puz, N_Puz).
propaga_posicoes([_|R], Puz, Puz):-
  propaga_posicoes(R, Puz, Puz), !.

%resolve(Puz, N_Puz): resolve o puzzle Puz
resolve(Puz, N_Puz):-
  inicializa(Puz, N_Puz),
  verifica_R3(N_Puz),
  num_vars_puzzle(N_Puz, 0), !.                         %se ja nao houver variaveis, Puz esta resolvido
resolve(Puz, Sol_Puz):-
  inicializa(Puz, N_Puz),
  verifica_R3(N_Puz),
  mat_dimensoes(Puz, Dim, _),
  findall(X, between(1, Dim, X), Lst),                  %retorna uma lista com os numeros de todas as linhas
  encontra_primeira_var(N_Puz, Lst, Lst, (L,C)),        %encontra a primeira variavel
  mat_ref(N_Puz, (L,C), 0),                             %experimenta substituir a variavel por 0
  propaga_posicoes([(L,C)], N_Puz, N_Puz_2),
  verifica_R3(N_Puz_2),
  (num_vars_puzzle(N_Puz_2, 0),
  Sol_Puz = N_Puz_2, !
  ;
  resolve(N_Puz_2, Sol_Puz), !).
resolve(Puz, Sol_Puz):-
  inicializa(Puz, N_Puz),
  verifica_R3(N_Puz),
  mat_dimensoes(Puz, Dim, _),
  findall(X, between(1, Dim, X), Lst),
  encontra_primeira_var(N_Puz, Lst, Lst, (L,C)),
  mat_ref(Puz, (L,C), 1),                                %se subsituir por 0 nao funcionar, substitui por 1
  propaga_posicoes([(L,C)], N_Puz, N_Puz_2),
  verifica_R3(N_Puz_2),
  (num_vars_puzzle(N_Puz_2, 0),
  Sol_Puz = N_Puz_2, !
  ;
  resolve(N_Puz_2, Sol_Puz), !).

%encontra_primeira_var(Puz, Lst, Lista, Pos): encontra a primeira variavel num puzzle
encontra_primeira_var(Puz, Lst, [L|_], (L,C)):-
  findall((L,X), member(X, Lst), Pos),
  nth1(L, Puz, Linha),
  encontra_primeira_var_aux(Linha, Pos, (L,C)),
  C=\= 0, !.
encontra_primeira_var(Puz, Lst, [_|R], (L, C)):-
   encontra_primeira_var(Puz,Lst, R, (L, C)).

%encontra_primeira_var_aux(Linha, Pos, (L,C)): retorna a coluna onde se encontra a primeira variavel
encontra_primeira_var_aux(_, [], (_,0)).
encontra_primeira_var_aux([P|_], [(_,C)|_], (_,Y)):-
  var(P), !, Y = C.
encontra_primeira_var_aux([_|Res], [_|R], W):-
  encontra_primeira_var_aux(Res, R, W).
