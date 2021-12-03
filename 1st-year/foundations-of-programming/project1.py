#92438  Catarina Manuel Cativo Carreiro

def eh_tabuleiro(t):
    """eh_tabuleiro: universal -> booleano
        recebe um argumento de qualquer tipo e devolve true se o seu argumento corresponde
        a um tabuleiro e false caso contrario. um tabuleiro corresponde a um tuplo contendo tres tuplos,
        os dois primeiros com tres elementos e o ultimo com dois elementos e que os elementos destes tuplos
        sao 0, 1, ou -1."""
    test = (-1,0,1)                                                                                                                                              #tuplo de teste
    if (isinstance(t, tuple) and len(t) ==3):                                                                                                        #se for tuplo e tiver tres elementos
        if (isinstance(t[0], tuple) and isinstance(t[1], tuple) and isinstance(t[2], tuple) ):                                 #se cada um dos elementos for um tuplo
            if (len(t[0]) ==len(t[1]) == 3 and len(t[2])==2):                                                                                   #se o tuplo 0 e 1 tiver 3 elementos e o tuplo 2 tiver 2 elementos
                for i in range(len(t)):
                    for x in range(len(t[i])):
                        if (t[i][x] not in test or not isinstance(t[i][x], int)):                                                               #se os elementos nao forem -1,0 ou 1 ou nao forem numeros inteiros retorna falso
                            return False
                return True                                                                                                                                    #se verificar as outras condicoes todas e verdadeiro
            else:
                return False                                                                                                                                   #retornar falso
        else:
            return False                                                                                                                                       #retornar falso
    else:
        return False                                                                                                                                           #retornar falso

def tuple_to_list(t):                                                                                                                                      #transforma o tuplo numa lista
    lista_final = []
    for x in range(0, len(t)):
        lista = list(t[x])
        lista_final.append(lista)
    return lista_final

def list_to_tuple(t):                                                                                                                                     #transforma a lista num tuplo
    tuplo_final = []
    for x in range(0, len(t)):
        tuplo = tuple(t[x])
        tuplo_final.append(tuplo)
    return tuple(tuplo_final)

def tabuleiro_str(t):
    """tabuleiro_str: tabuleiro -> cad. caracteres
        esta funcao recebe um tabuleiro e devolve a cadeia de caracteres que o representa."""
    if eh_tabuleiro(t) is False:                                                                                                                      
        raise ValueError('tabuleiro_str: argumento invalido')                                                                         #se o tabuleiro for falso, levantar erro
    else:
        t1 = tuple_to_list(t)                                                                                                                             #transformar o tuplo numa lista
        for x in range(0, len(t1)):                                                                                                                     #variar tuplos (que agora foram transformados em listas)                                                                                                             
            for i in range(0, len(t1[x])):                                                                                                             #variar elementos (células)
                if t1[x][i] == -1:
                    t1[x][i] = 'x'                                                                                                                             #transformar -1 em 'x'
        return '+-------+\n|...' + str(t1[0][2]) + '...|\n|..' + str(t1[0][1]) + '.' + str(t1[1][2]) + '..|\n|.' + str(
            t1[0][0]) + '.' + str(t1[1][1]) + '.' + str(t1[2][1]) + \
               '.|\n|..' + str(t1[1][0]) + '.' + str(t1[2][0]) + '..|\n+-------+'                                                          #retornar o tabuleiro

def tabuleiros_iguais(t1,t2):
    """tabuleiros_iguais: tabuleiro x tabuleiro -> booleano
        esta funcao recebe dois tabuleiros e devolve true se os tabuleiros sao iguais e
        false caso contrario. dois tabuleiros sao iguais se e so se o conteudo de cada
        celula de um tabuleiro e o mesmo que a correspondente celula no outro tabuleiro."""
    if (eh_tabuleiro(t1) is False or eh_tabuleiro(t2) is False):
        raise ValueError('tabuleiros_iguais: um dos argumentos nao e tabuleiro')                                     
    else:
        for i in range(0,len(t1)):                                                                                                                     #variar tuplos
            for x in range(0,len(t1[i])):                                                                                                            #variar elementos
                if t1[i][x] != t2[i][x]:                                                                                                                  #se forem diferentes, retornar falso
                    return False
        return True

def porta_x(t, car):
    """porta_x: tabuleiro x {"E", "D"} -> tabuleiro
        o operador correspondente a esta porta, aplicado a um qubit, tem como resultado a
        inversao do valor da celula inferior desse qubit, e consequentemente, de todas as restantes
        celulas que estejam na mesma linha ou coluna, caso seja o qubit da esquerda ou da direita,
        respetivamente."""
    if (car == "E" and eh_tabuleiro(t) is True):                                                                                        
        t = tuple_to_list(t)
        for x in range(len(t[1])):                                                                                                                  #variar os elementos do primeiro tuplo (porta_x, lado esquerdo)
            if t[1][x] == 1:                                                                                                                              #se o elemento for igual a 1, passa a 0
                t[1][x] = 0                                                                                                                                #mudar o valor da variavel
            elif t[1][x] == 0:                                                                                                                          #se o elemento for igual a 0, passa a 1
                t[1][x] = 1
        return list_to_tuple(t)
    elif (car == "D" and eh_tabuleiro(t) is True):                                         
        t = tuple_to_list(t)
        for x in range(2):                                                                                                                           #variar pelos elementos [0][1],[1][1],[2][0] (porta_x, lado direito)
            if t[x][1] == 1:
                t[x][1] = 0
            elif t[x][1] == 0:
                t[x][1] = 1
        if t[2][0] == 1:
            t[2][0] = 0
        elif t[2][0] == 0:
            t[2][0] = 1
        return list_to_tuple(t)
    raise ValueError('porta_x: um dos argumentos e invalido')

def porta_z(t, car):
    """porta_z: tabuleiro x {"E", "D"} -> tabuleiro
        o operador correspondente a esta porta, aplicado a um qubit, apresenta
        resultados semelhantes aos da porta X, excetuando que opera sobre a
        celula superior."""
    if (car == "E" and eh_tabuleiro(t) is True):
        t = tuple_to_list(t)
        for x in range(len(t[1])):                                                                                                             #variar os elementos do tuplo [0]   (porta_z, lado esquerdo)                                                                                                         
            if t[0][x] == 1:
                t[0][x] = 0
            elif t[0][x] == 0:
                t[0][x] = 1
        return list_to_tuple(t)
    elif (car == "D" and eh_tabuleiro(t) is True):
        t = tuple_to_list(t)
        for x in range(2):                                                                                                                         #variar os elementos [0][2],[1][2],[2][1] (porta_z, lado direito)
            if t[x][2] == 1:
                t[x][2] = 0
            elif t[x][2] == 0:
                t[x][2] = 1
        if t[2][1]==1:
            t[2][1]=0
        elif t[2][1] ==0:
            t[2][1]=1
        return list_to_tuple(t)
    else:
        raise ValueError('porta_z: um dos argumentos e invalido')


def porta_h(t, car):
    """porta_h: tabuleiro x{"E", "D"} -> tabuleiro
        o operador correspondente a esta porta, aplicado a um qubit, tem como resultado a
        troca de estado de ambas as suas células entre si, com o resultado de afetar,
        consequentemente, as linhas ou colunas onde se encontra, quando aplicado ao qubit da
        esquerda ou direita, respetivamente."""  
    if (car == "E" and eh_tabuleiro(t) is True):
        t = tuple_to_list(t)
        for x in range(3):
            t[1][x], t[0][x] = t[0][x], t[1][x]                                                                                              #trocar elementos dos tuplos [0] e [1] -> lado esquerdo
        return list_to_tuple(t)
    elif (car == "D" and eh_tabuleiro(t) is True):
        t = tuple_to_list(t)
        for x in range(2):
            t[x][2], t[x][1]  = t[x][1], t[x][2]                                                                                            #trocar elementos t[0][1] por t[0][2] , e t[2][1]  por t[2][0]                                                                                       
        t[2][1], t[2][0] = t[2][0], t[2][1]
        return list_to_tuple(t)
    else:
        raise ValueError('porta_h: um dos argumentos e invalido')





