# Grupo 58:
# 92438 Catarina Carreiro
# 92440 Cristiano Clemente

from sys import argv
from search import Problem, Node, \
    astar_search, breadth_first_tree_search, depth_first_tree_search, greedy_search
#from search import compare_searchers
from random import shuffle, random


class Board:
    """ Representacao interna de um tabuleiro de Ricochet Robots. """
    goal = {"color": None,              # cor do objetivo
            "position": (None, None)}   # posicao do objetivo
    barriers = {}                       # barreiras

    def __init__(self, r_row, r_col, g_row, g_col, b_row, b_col, y_row, y_col):
        self.r = (r_row, r_col)
        self.g = (g_row, g_col)
        self.b = (b_row, b_col)
        self.y = (y_row, y_col)

    def __eq__(self, other):
        """ Da override da igualdade entre instancias de Board.
        board1 == board2 sse todas as posicoes dos robots coincidem. """
        return self.r == other.r and self.g == other.g and self.b == other.b and self.y == other.y

    def robot_position(self, robot):
        """ Devolve a posição atual do robô passado como argumento. """
        if(robot == 'R'):
            return self.r
        elif(robot == 'G'):
            return self.g
        elif(robot == 'B'):
            return self.b
        elif(robot == 'Y'):
            return self.y

    def __no_barrier(self, position, dir):
        """ Nao ha barreiras nesta posicao? """
        if(position in Board.barriers):
            return dir not in Board.barriers[position]
        return True

    def __no_robot(self, position, dir):
        """ Nao ha robots nesta posicao? """
        r, c = position
        if(dir == 'u'):
            r -= 1
        elif (dir == 'r'):
            c += 1
        elif (dir == 'd'):
            r += 1
        elif (dir == 'l'):
            c -= 1
        for color in ["R", "Y", "G", "B"]:
            if(self.robot_position(color) == (r, c)):
                return False
        return True

    def actions(self):
        """ Retorna uma lista com as acoes possiveis dadas as posicoes dos robots e as barreiras. """
        actions = []
        for robot in ["R", "Y", "G", "B"]:
            for dir in ["u", "r", "l", "d"]:
                if(self.__no_barrier(self.robot_position(robot), dir) and self.__no_robot(self.robot_position(robot), dir)):
                    actions.append((robot, dir))
        shuffle(actions)  # heuristica para calcular a ordem das acoes
        return actions

    def result(self, action):
        """ Retorna um novo board que resulta de aplicar uma acao ao board original. """
        robot, dir = action
        r, c = self.robot_position(robot)
        while(self.__no_barrier((r, c), dir) and self.__no_robot((r, c), dir)):
            if(dir == 'u'):
                r -= 1
            elif (dir == 'r'):
                c += 1
            elif (dir == 'd'):
                r += 1
            elif (dir == 'l'):
                c -= 1
        if(robot == 'R'):
            return Board(r, c,
                         self.g[0],  self.g[1],
                         self.b[0], self.b[1],
                         self.y[0], self.y[1])
        elif(robot == 'G'):
            return Board(self.r[0], self.r[1],
                         r,  c,
                         self.b[0], self.b[1],
                         self.y[0], self.y[1])
        elif(robot == 'B'):
            return Board(self.r[0], self.r[1],
                         self.g[0],  self.g[1],
                         r, c,
                         self.y[0], self.y[1])
        elif(robot == 'Y'):
            return Board(self.r[0], self.r[1],
                         self.g[0],  self.g[1],
                         self.b[0], self.b[1],
                         r, c)

    def goal_test(self):
        """ O robot-objetivo esta na posicao-objetivo? """
        return self.robot_position(Board.goal["color"]) == Board.goal["position"]

    def h(self):
        """ Calcula a distancia de manhattan entre
        a posicao do robot-objetivo e
        a posicao-objetivo. """
        goal_color = Board.goal["color"]
        robot_position = self.robot_position(goal_color)
        goal_position = Board.goal["position"]
        manhattan_distance = abs(robot_position[0] - goal_position[0]) + \
            abs(robot_position[1] - goal_position[1])
        return manhattan_distance


class RRState:
    """ Representa um estado do problema Ricochet Robots. """
    all_states = []  # mantem o historico de todos os estados com boards diferentes gerados ate agora

    @staticmethod
    def new_state(board):
        """ Ja existe um estado com a mesma board?
        Se sim, devolve-o.
        Se nao, cria de facto um novo estado. """
        for state in RRState.all_states:
            if(state.board == board):
                return state
        new_state = RRState(board)
        RRState.all_states.append(new_state)
        return new_state

    def __init__(self, board):
        self.board = board

    def __lt__(self, other):
        """ Este método é utilizado em caso de empate na gestão da lista
        de abertos nas procuras informadas. """
        # return True
        return random() <= 0.5  # heuristica para escolher o estado em caso de empate


def parse_instance(filename: str) -> Board:
    """ Lê o ficheiro cujo caminho é passado como argumento e retorna
    uma instância da classe Board. """
    goal = {}       # objetivo
    barriers = {}   # barreiras
    with open(filename, "r") as f:
        size = int(f.readline())  # tamanho tabuleiro
        for _ in range(4):
            name, r, c = f.readline().split()
            r = int(r)
            c = int(c)
            if(name == 'R'):
                r_row = r
                r_col = c
            elif(name == 'G'):
                g_row = r
                g_col = c
            elif(name == 'B'):
                b_row = r
                b_col = c
            elif(name == 'Y'):
                y_row = r
                y_col = c
        name, r, c = f.readline().split()
        goal["color"] = name
        goal["position"] = (int(r), int(c))
        barriers[(1, 1)] = ['l', 'u']
        barriers[(1, size)] = ['u', 'r']
        barriers[(size, 1)] = ['l', 'd']
        barriers[(size, size)] = ['d', 'r']
        for i in range(2, size):
            barriers[(1, i)] = ['u']
            barriers[(size, i)] = ['d']
            barriers[(i, 1)] = ['l']
            barriers[(i, size)] = ['r']
        num_barriers = int(f.readline())
        for _ in range(num_barriers):
            r, c, dir = f.readline().split()
            if(dir == 'u'):
                if((int(r), int(c)) in barriers):
                    barriers[(int(r), int(c))] += ['u']
                else:
                    barriers[(int(r), int(c))] = ['u']
                if((int(r)-1, int(c)) in barriers):
                    barriers[(int(r)-1, int(c))] += ['d']
                else:
                    barriers[(int(r)-1, int(c))] = ['d']
            elif (dir == 'r'):
                if((int(r), int(c)) in barriers):
                    barriers[(int(r), int(c))] += ['r']
                else:
                    barriers[(int(r), int(c))] = ['r']
                if((int(r), int(c)+1) in barriers):
                    barriers[(int(r), int(c)+1)] += ['l']
                else:
                    barriers[(int(r), int(c)+1)] = ['l']
            elif (dir == 'd'):
                if((int(r), int(c)) in barriers):
                    barriers[(int(r), int(c))] += ['d']
                else:
                    barriers[(int(r), int(c))] = ['d']
                if((int(r)+1, int(c)) in barriers):
                    barriers[(int(r)+1, int(c))] += ['u']
                else:
                    barriers[(int(r)+1, int(c))] = ['u']
            elif (dir == 'l'):
                if((int(r), int(c)) in barriers):
                    barriers[(int(r), int(c))] += ['l']
                else:
                    barriers[(int(r), int(c))] = ['l']
                if((int(r), int(c)-1) in barriers):
                    barriers[(int(r), int(c)-1)] += ['r']
                else:
                    barriers[(int(r), int(c)-1)] = ['r']
    initial_board = Board(r_row, r_col,
                          g_row, g_col,
                          b_row, b_col,
                          y_row, y_col)
    Board.goal = goal
    Board.barriers = barriers
    return initial_board


class RicochetRobots(Problem):
    def __init__(self, board: Board):
        """ O construtor especifica o estado inicial. """
        self.initial = RRState.new_state(board)

    def actions(self, state: RRState):
        """ Retorna uma lista de ações que podem ser executadas a
        partir do estado passado como argumento. """
        return state.board.actions()

    def result(self, state: RRState, action):
        """ Retorna o estado resultante de executar a 'action' sobre
        'state' passado como argumento. A ação retornada deve ser uma
        das presentes na lista obtida pela execução de
        self.actions(state). """
        return RRState.new_state(state.board.result(action))

    def goal_test(self, state: RRState):
        """ Retorna True se e só se o estado passado como argumento é
        um estado objetivo. Deve verificar se o alvo e o robô da
        mesma cor ocupam a mesma célula no tabuleiro. """
        return state.board.goal_test()

    def h(self, node: Node):
        """ Função heuristica utilizada para a procura A*. """
        return node.state.board.h()


def getStepsToSolution(solution_node: Node):
    """ Dado um no solucao faz backtrack e devolve uma lista com
    os passos para a solucao por ordem cronologica. """
    steps_to_solution = []
    current_node = solution_node
    while(current_node.parent != None):
        steps_to_solution.append(current_node.action)
        current_node = current_node.parent
    steps_to_solution.reverse()
    return steps_to_solution


def printStepsToSolution(steps_to_solution: list):
    """ Dada uma lista com os passos para a solucao por ordem cronologica
    imprime-os no formato pretendido. """
    print(len(steps_to_solution))
    for (color, direction) in steps_to_solution:
        print(color + " " + direction)


if __name__ == "__main__":

    # [1] Ler o ficheiro de input,
    rr_board = parse_instance(argv[1])
    rr_problem = RicochetRobots(rr_board)

    # [2] Usar uma técnica de procura para resolver a instância,
    solution_node = astar_search(rr_problem)

    """compare_searchers(problems=[RicochetRobots(rr_board)],
                      header=['Algoritmo', 'Ricochet_Robots'],
                      searchers=[depth_first_tree_search,
                                 breadth_first_tree_search,
                                 greedy_search,
                                 astar_search])"""

    # [3] Retirar a solução a partir do nó resultante,
    steps_to_solution = getStepsToSolution(solution_node)

    # [4] Imprimir para o standard output no formato indicado.
    printStepsToSolution(steps_to_solution)
