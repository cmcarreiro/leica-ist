#Catarina Manuel Cativo Carreiro
#92438

#1.1.1 celula
def cria_celula(v):
    #cria_celula: {1,0,-1} -> celula
    #recebe um inteiro (1, 0, -1) e retorna uma celula
    #verificar se o valor escrito pertence e 1,0 ou -1 e verificar se e um inteiro
    if (v in (1,0,-1) and isinstance(v,int)):
        #a representacao escolhida para celula foi uma lista com um unico elemento, visto poder ser mutavel
        return [v]
    else:
        raise ValueError('cria_celula: argumento invalido.')

def obter_valor(c):
    #obter_valor(c): celula -> {1, 0, -1}
    #obtem o valor (1,0 ou -1) contido na celula
    return c[0]

def inverte_estado(c):
    #inverte_estado: celula -> celula
    #se o valor for 0, passa para 1 (inativo -> ativo); se o valor for 1, passa para 0 (ativo->inativo)
    #se o valor for -1, nao tem inversa, pois e incerto
    if obter_valor(c) == 0:
        #utilizou-se o obter_valor de forma a manter a abstracao
        #modifica o valor da celula e devolve a celula
        c[0] = 1
        return c
    elif obter_valor(c) == 1:
        c[0] = 0
        return c
    else:
        return c

def eh_celula(arg):
    #eh_celula: universal -> logico
    #recebe um argumento e verifica se e uma celula, tendo em conta a representacao escolhida
    #ou seja, tem que ser uma lista, com um elemento que e um inteiro, -1,0 ou 1
    return (isinstance(arg, list) and len(arg) == 1 and obter_valor(arg) in (-1,0,1) and isinstance(obter_valor(arg), int))
    
def celulas_iguais(c1,c2):
    #celulas_iguais: celula^2 -> logico
    #recebe duas celulas e verifica se sao iguais, atraves da verificacao dos valores
    if eh_celula(c1) and eh_celula(c2):
        return (obter_valor(c1) == obter_valor(c2))
    else:
        return False

def celula_para_str(c):
    #celula_para_str(c): celula -> cadeia de caracteres
    #passa o valor da celula para uma string. se for 0 retorna '0', se for 1 retorna '1', se for -1 retorna 'x'
    if obter_valor(c) == -1:
        return 'x'
    else:
        return str(obter_valor(c))

#1.1.2 coordenada
def cria_coordenada(l,c):
    #cria_coordenada: r0+^2 -> coordenada
    #recebe dois argumentos, que devem ser ambos inteiros e que estejam contidos em (0,1,2)
    if (not isinstance(l, int) or not isinstance(c, int) or l not in (0,1,2) or c not in (0,1,2)):
        raise ValueError('cria_coordenada: argumentos invalidos.')
    else:
        #a representacao escolhida para as coordenadas e um tuplo com dois elementos, sendo o primeiro a linha e o segundo a coluna
        return (l,c)

def coordenada_linha(c):
    #coordenada_linha: coordenada -> n
    #retorna a linha da coordenada
    #tendo em conta a representacao escolhida,devolve o primeiro elemento do tuplo que contem as coordenadas
    return c[0]

def coordenada_coluna(c):
    #coordenada_linha: coordenada -> n
    #retorna a coluna da coordenada
    #tendo em conta a representacao escolhida, devolve o segundo elemento do tuplo que contem as coordenadas
    return c[1]

def eh_coordenada(arg):
    #eh_coordenada: universal -> logico
    #retorna True se o argumento for uma coordenada
    #tendo em conta a representacao, o argumento tem que ser um tuplo com dois elementos, sendo cada um um valor
    #inteiro entre (0,1,2). se a linha for = 2, nao pode ter coluna = 0, visto que a coordenada (2,0) nao existe
    return (isinstance(arg, tuple) and len(arg) == 2 and isinstance(coordenada_linha(arg), int) \
            and coordenada_linha(arg) in (0,1,2) and isinstance(coordenada_coluna(arg), int) and coordenada_coluna(arg) in (0,1,2) and arg != (2,0))

def coordenadas_iguais(c1,c2):
    #coordenadas_iguais: coordenada x coordenada -> logico
    #retorna true se as duas coordenadas forem iguais
    return c1 == c2

def coordenada_para_str(c):
    #coordenada_para_str(c): coordenada -> cad caracteres
    #retorna a coordenada transformada em string
    return str(c)

#1.1.3 tabuleiro
def tabuleiro_inicial():
    #tabuleiro_inicial: {} -> tabuleiro
    #escolheu-se representar o tabuleiro como um dicionario
    #sendo as chaves as coordenadas, e os valores as celulas a elas associadas
    return {
        cria_coordenada(0,0): cria_celula(-1), cria_coordenada(0,1): cria_celula(-1), cria_coordenada(0,2): cria_celula(-1),
        cria_coordenada(1,0): cria_celula(0), cria_coordenada(1,1): cria_celula(0), cria_coordenada(1,2): cria_celula(-1),
        cria_coordenada(2,1): cria_celula(0), cria_coordenada(2,2): cria_celula(-1)
        }

def tabuleiro_celula(t, coor):
    #tabuleiro_celula: tabuleiro x coordenada -> celula
    #retorna a celula associada a uma certa coordenada
    return t[coor]

def tabuleiro_substitui_celula(t, cel, coor):
    #tabuleiro_substitui_celula: tabuleiro x celula x coordenada -> tabuleiro
    #no tabuleiro, substitui a celula de umas certas coordenadas, por outra celula
    if (eh_tabuleiro(t) and eh_celula(cel) and eh_coordenada(coor)):
        t[coor] = cel
        return t
    else:
        raise ValueError('tabuleiro_substitui_celula: argumentos invalidos.')

def tabuleiro_inverte_estado(t,coor):
    #tabuleiro_inverte_estado: tabuleiro x coordenada -> tabuleiro
    #no tabuleiro, inverte o estado da celula associada as coordenadas
    if eh_tabuleiro(t) and eh_coordenada(coor):
        t[coor] = inverte_estado(t[coor])
        return t
    else:
        raise ValueError('tabuleiro_inverte_estado: argumentos invalidos.')

def eh_tabuleiro(arg):
    #eh_tabuleiro: universal -> logico
    #retorna True se for um dicionario
    #tendo em conta a representacao escolhida, verifica se e um dicionario com 8 chaves,
    #e se cada chave e uma coordenada e se cada item e uma celula
    validar = True
    if (isinstance(arg, dict) and (len(arg)==8)):
        for c in arg:
            validar = validar and eh_coordenada(c) and eh_celula(arg[c])
    else:
        validar = False
    return validar

def tabuleiros_iguais(t1,t2):
    #tabuleiros_iguais: tabuleiro x tabuleiro -> logico
    #retorna true se os tabuleiros forem iguais, ou seja, se em cada coordenada tiverem o mesmo valor
    if eh_tabuleiro(t1) and eh_tabuleiro(t2):
        validar = True
        teste = ((0,0),(0,1),(0,2),(1,0),(1,1),(1,2),(2,1),(2,2))
        for c in teste:
            validar = validar and (t1[c] == t2[c])
        return validar
    else:
        return False

def str_para_tabuleiro(s):
    #str_para_tabuleiro: cad caracteres -> tabuleiro
    #transforma uma string para um tabuleiro
    tab = {}
    forma = False
    conteudo = True
    try:
        #verifica se e possivel fazer eval
        eval(s)
    except:
        #raise ValueError('str_para_tabuleiro: argumento invalido.')
        forma = False
        conteudo = False
    else:
        #avalia se o argumento e um tuplo. se for, verifica se tem 3 tuplos la dentro, sendo os dois primeiros com
        #dimensao 3 e o ultimo com dimensao 2
        arg = eval(s)
        if (isinstance(arg, tuple) and len(arg) == 3 and isinstance(arg[0], tuple) and isinstance(arg[1], tuple) and isinstance(arg[2], tuple) and len(arg[0]) == len(arg[1]) == 3 \
            and len(arg[2]) == 2):
            forma = True
        if forma:
            #verifica se cada tuplo contem apenas inteiros, entre (-1,0,1)
            for l in range(len(arg)):
                for c in range(len(arg[l])):
                    conteudo = conteudo and  (arg[l][c] == -1 or arg[l][c] == 0 or arg[l][c] == 1) and isinstance(arg[l][c], int)
    if forma and conteudo:
        #se todas as condicoes anteriores se verificarem, cria um tabuleiro em que o numero do tuplo e a linha
        #e a posicao do item dentro da linha e a coluna 
        for l in range(len(arg)):
            for c in range(len(arg[l])):
                if l == 2:
                    tab[(l,c+1)] = cria_celula(arg[l][c])
                else:
                    tab[(l,c)] = cria_celula(arg[l][c])
        return tab
    else:
        raise ValueError('str_para_tabuleiro: argumento invalido.')

def tabuleiro_dimensao(t):
    #tabuleiro_dimensao: tabuleiro -> n
    #verifica a dimensao do tabuleiro (ou seja, numero de linhas e numero de colunas, sendo que ambos sao iguais)
    lista = []
    for i in t:
        lista = lista + [i[0]]
    return max(lista)+1

def tabuleiro_para_str(t):
    #tabuleiro_para_str: tabuleiro -> cad caracteres
    #retorna o tabuleiro em string
    return '+-------+\n|...' + celula_para_str(t[(0,2)]) + '...|\n|..' + celula_para_str(t[(0,1)]) + '.' + celula_para_str(t[(1,2)]) + '..|\n|.' \
                + celula_para_str(t[(0,0)] ) + '.' +celula_para_str(t[(1,1)])  + '.' + celula_para_str(t[(2,2)])  + \
               '.|\n|..' + celula_para_str(t[(1,0)]) + '.' + celula_para_str(t[(2,1)])  + '..|\n+-------+'

def copia_tabuleiro(t):
    #esta funcao copia um tabuleiro. funcao auxiliar
    t_copia = {}
    for coor in t:
        t_copia[coor] = tabuleiro_celula(t, coor)
    return t_copia


#portas
def porta_x(t,p):
    #porta_x: tabuleiro x {'E', 'D'} -> tabuleiro
    #inverte o estado das celulas de uma linha ou coluna do tabuleiro, consoante a porta
    if (eh_tabuleiro(t) and (p=='E' or p=='D')):
        if p == 'E':
            #se for a porta E, inverte o estado das celulas da linha 1
            for c in range(tabuleiro_dimensao(t)):
               t = tabuleiro_inverte_estado(t, cria_coordenada(1, c))
        elif p == 'D':
            #se a porta for D, inverte o estado das celulas da coluna 1
            for l in range(tabuleiro_dimensao(t)):
                t = tabuleiro_inverte_estado(t, cria_coordenada(l,1))
    else:
        raise ValueError('porta_x: argumentos invalidos.')
    return t

def porta_z(t,p):
    #porta_z: tabuleiro x {'E', 'D'} -> tabuleiro
    #inverte o estado das celulas de uma linha ou coluna do tabuleiro, consoante a porta
    if (eh_tabuleiro(t) and (p=='E' or p=='D')):
        if p == 'E':
            #se for a porta E, inverte o estado das celulas da linha 0
            for c in range(tabuleiro_dimensao(t)):
                t = tabuleiro_inverte_estado(t, cria_coordenada(0,c))
        elif p == 'D':
            #se for a porta D, inverte o estado das celulas da coluna 2
            for l in range(tabuleiro_dimensao(t)):
                t = tabuleiro_inverte_estado(t, cria_coordenada(l,2))
    else:
        raise ValueError('porta_z: argumentos invalidos.')
    return t

def porta_h(t,p):
    #porta_h: tabuleiro x {'E', 'D'} -> tabuleiro
    #troca duas linhas ou colunas do tabuleiro, consoante a porta
    if (eh_tabuleiro(t) and (p=='E' or p=='D')):
        #usa-se um tabuleiro auxiliar, igual ao inicial, para se copiar o valor das celulas, visto que
        #a funcao tabuleiro_substitui_celula e destrutivo
        t1 = copia_tabuleiro(t)
        if p == 'E':
            #se a porta for E, troca as celulas da linha 1 pelas celulas da linha 0, e vice-versa
            for c in range(tabuleiro_dimensao(t)):
               t = tabuleiro_substitui_celula(t, tabuleiro_celula(t1, cria_coordenada(1,c)), cria_coordenada(0,c))
               t = tabuleiro_substitui_celula(t, tabuleiro_celula(t1, cria_coordenada(0,c)), cria_coordenada(1,c))
        elif p == 'D':
            #se a porta for D, troca as celulas da coluna 1 pelas celulas da coluna 2, e vice-versa
            for l in range(tabuleiro_dimensao(t)):
                t = tabuleiro_substitui_celula(t, tabuleiro_celula(t1, cria_coordenada(l,1)), cria_coordenada(l,2))
                t = tabuleiro_substitui_celula(t, tabuleiro_celula(t1, cria_coordenada(l,2)), cria_coordenada(l,1))
    else:
        raise ValueError('porta_h: argumentos invalidos.')
    return t

#hello_quantum
def hello_quantum(s):
    for i in range(len(s)):
        if s[i] == ':':
            #separa a string inicial num tuplo de tuplos onde esta contido o tabuleiro, e num numero
            tabuleiro_objetivo = s[:i]
            num = s[i+1:]
    tabuleiro_objetivo = str_para_tabuleiro(tabuleiro_objetivo)
    tabuleiro_jogo = tabuleiro_inicial() #tabuleiro que se vai alter; onde o jogador joga
    objetivo_jogadas = eval(num)
    jogadas = 1
    print("Bem-vindo ao Hello Quantum!\nO seu objetivo e chegar ao tabuleiro:")
    print(tabuleiro_para_str(tabuleiro_objetivo))
    print("Comecando com o tabuleiro que se segue:")
    print(tabuleiro_para_str(tabuleiro_inicial()))
    #enquanto o numero de jogadas for menor ou igual ao objetivo do numero de jogadas:
    while (jogadas<=objetivo_jogadas):
        #se os tabuleiros forem iguais, retorna true e o jogo acaba
        if tabuleiros_iguais(tabuleiro_jogo, tabuleiro_objetivo):
            print('Parabens, conseguiu converter o tabuleiro em %s jogadas!' % jogadas)
            return True
        #se nao, pede ao utilizador para escolher uma porta, e modifica o tabuleiro consoante essa escolha
        #incrementa o numero de jogadas tambem
        else:
            porta = input('Escolha uma porta para aplicar (X, Z ou H): ')
            qubit = input('Escolha um qubit para analisar (E ou D): ')
            if porta == 'X':
                tabuleiro_jogo = porta_x(tabuleiro_jogo, qubit)
                jogadas += 1
                print(tabuleiro_para_str(tabuleiro_jogo))
            elif porta == 'Z':
                tabuleiro_jogo = porta_z(tabuleiro_jogo, qubit)
                jogadas += 1
                print(tabuleiro_para_str(tabuleiro_jogo))
            elif porta == 'H':
                tabuleiro_jogo = porta_h(tabuleiro_jogo, qubit)
                jogadas += 1
                print(tabuleiro_para_str(tabuleiro_jogo))
    #quando o numero de jogadas chegar ao numero objetivo de jogadas, verifica se o tabuleiro do jogo e igual ao tabuleiro objetivo
    #se for, retorna true, se nao, retorna false            
    if tabuleiros_iguais(tabuleiro_jogo, tabuleiro_objetivo):
        jogadas -= 1
        print('Parabens, conseguiu converter o tabuleiro em %s jogadas!' % jogadas)
        return True
    else:
        return False
