# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry*

Para lançar o *ZooKeeper*, ir à pasta `zookeeper/bin` e correr o comando  
`./zkServer.sh start` (Linux) ou `zkServer.cmd` (Windows).

É possível também lançar a consola de interação com o *ZooKeeper*, novamente na pasta `zookeeper/bin` e correr `./zkCli.sh` (Linux) ou `zkCli.cmd` (Windows).

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar o *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar os servidores *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8091 1"
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8092 2"
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8093 3"
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8094 4"
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8095 5"
```

O comando recebe o host e port do Zookeper, bem como o host, o port, e a instância do rec.

Para confirmar o funcionamento dos servidores com um *ping* para cada um, fazer:

```sh
$ cd ../rec-tester
$ mvn exec:java -D"exec.args"="localhost 2181"
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify -DzooHost="localhost" -DzooPort="2181"
```

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *hub* .
Para isso basta ir à pasta *hub* e executar:

```sh
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8081 1 users.csv stations.csv initRec"
```

O comando recebe o host e port do Zookeper, bem como o host, o port, e a instância do hub. Recebe também um ficheiro com os dados dos utilizadores, e um ficheiro com os dados das estações. Vem opcionalmente com o argumento initRec.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd ../hub-tester
$ mvn exec:java -D"exec.args"="localhost 2181"
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify -DzooHost="localhost" -DzooPort="2181"
```

Todos os testes devem ser executados sem erros.

### 1.5. *App*

Importante: **Reiniciar o hub antes de lançar a App**

Iniciar a aplicação com a utilizadora alice:

```sh
$ mvn exec:java -D"exec.args"="localhost 2181 alice +35191102030 38.7380 -9.3000"
```

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.


Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente na *app*.

Vamos correr os testes na aplicação da Alice:

### 2.1. *balance*

```
> balance
alice 0 BIC
```

### 2.2 *top-up*

```
> top-up 15
alice 150 BIC
```

```
> top-up 50
ERRO: Invalid amount of money!
```

### 2.3 *tag*

```
> tag 38.7376 -9.3031 loc1
OK
```

### 2.4 *move*

```
> move loc1
alice em https://www.google.com/maps/place/38.7376,-9.3031
```

### 2.5 *at*

```
> at
alice em https://www.google.com/maps/place/38.7376,-9.3031
```

### 2.6 *scan*
```
> scan 3
istt, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, a 82 metros
stao, lat 38.6867, -9.3124 long, 30 docas, 3 BIC prémio, 20 bicicletas, a 5717 metros
jero, lat 38.6972, -9.2064 long, 30 docas, 3 BIC prémio, 20 bicicletas, a 9514 metros
```

```
> scan -2
ERRO: k needs to be bigger than 0!
```

### 2.7 *info*

```
> info istt
IST Taguspark, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, 0 levantamentos, 0 devoluções, https://www.google.com/maps/place/38.7372,-9.3023
```

```
> info blah
ERRO: Station does not exist!
```

### 2.8 *bike-up*
```
> bike-up ista
ERRO: Station too far away!
```

```
> bike-up istt
OK
```

```
> bike-up istt
ERRO: User already has a bike!
```

### 2.9 *bike-down*
```
> bike-down ista
ERRO: Station too far away!
```

```
> bike-down istt
OK
```

```
> bike-down istt
ERRO: User does not have a bike!
```

### 2.10 *ping*
```
> ping
Hub is running.
```

### 2.11 *sys-status*
```
> sys-status
path:/grpc/bicloin/rec/1 status:up
path:/grpc/bicloin/rec/2 status:up
path:/grpc/bicloin/rec/3 status:up
path:/grpc/bicloin/rec/4 status:up
path:/grpc/bicloin/rec/5 status:up
path:/grpc/bicloin/hub/1 status:up
```
### 2.12 *#*

```
> # comentario
```

### 2.13 *zzz*
```
> zzz 5000
```

### 2. 14 *help*

```
> help
balance                 get user balance
top-up                  adds money to user account
tag                     creates a location tag
move                    moves user to specified location
at                      prints user current location
scan                    locates n closest stations
info                    gives information about the station
bike-up                 lets user pick up a bike
bike-down               lets user return a bike
ping                    returns server status
sys-status              returns system status
zzz                     sleeps for a certain amount of milliseconds
#                       comments
help                    lists all possible commands and what they do
exit                    terminates app gracefully
```

### 2.15 *exit*

```
> exit
```

## 3 Testar tolerância a faltas 

Iremos agora testar a tolerânica a faltas do sistema. 

### 3.0 Situação normal
Em primeiro lugar, damos reset ao hub. Fazemos ```Ctrl-C``` e voltamos a correr:
```sh
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8081 1 users.csv stations.csv initRec"
```

Vamos agora lançar duas apps com a ajuda do ficheiro ```comandos.txt```. Para isso fazemos **simultaneamente**:
```sh
$ mvn exec:java -D"exec.args"="localhost 2181 alice +35191102030 38.7380 -9.3000" < comandos.txt
$ mvn exec:java -D"exec.args"="localhost 2181 bruno +35193334444 38.7376 -9.1545" < comandos.txt
```

Tudo corre bem mas quando chega o momento de deixarem a bicicleta na estação IST Alameda um deles não vai conseguir porque só há um lugar disponível.

### 3.1 Desligar um dos recs
Em primeiro lugar, damos reset ao hub. Fazemos ```Ctrl-C``` e voltamos a correr:
```sh
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8081 1 users.csv stations.csv initRec"
```

Depois, vamos ao ```rec4``` e fazemos ```Ctrl-C```, desligando-o. 

Vamos lançar de novo as duas apps. Para isso fazemos **simultaneamente**:
```sh
$ mvn exec:java -D"exec.args"="localhost 2181 alice +35191102030 38.7380 -9.3000" < comandos.txt
$ mvn exec:java -D"exec.args"="localhost 2181 bruno +35193334444 38.7376 -9.1545" < comandos.txt
```

Tudo corre bem mas quando chega o momento de deixarem a bicicleta na estação IST Alameda um deles não vai conseguir porque só há um lugar disponível.

### 3.2 Pausar um dos recs


Em primeiro lugar, voltamos a correr o  ```rec4``` com o comando:
```sh
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8094 4"
```

Depois damos reset ao hub. Fazemos ```Ctrl-C``` e voltamos a correr:
```sh
$ mvn exec:java -D"exec.args"="localhost 2181 localhost 8081 1 users.csv stations.csv initRec"
```

Vamos lançar de novo as duas apps. Para isso fazemos **simultaneamente**:
```sh
$ mvn exec:java -D"exec.args"="localhost 2181 alice +35191102030 38.7380 -9.3000" < comandos.txt
$ mvn exec:java -D"exec.args"="localhost 2181 bruno +35193334444 38.7376 -9.1545" < comandos.txt
```

Rapidamente, vamos ao ```rec2``` e fazemos ```Ctrl-Z```.  Passados 5 a 15 segundos fazemos ```fg```. A app irá suportar esta falha.

Tudo corre bem mas quando chega o momento de deixarem a bicicleta na estação IST Alameda um deles não vai conseguir porque só há um lugar disponível.

----

## 4. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.