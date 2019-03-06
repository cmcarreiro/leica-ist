;--------------------------------------------------------------------------------------------
;INFORMACAO
;--------------------------------------------------------------------------------------------

;AUTORES:
;CATARINA CARREIRO, NO 92438
;JOAO ANTUNES, NO 92498
;GRUPO 13
;LEIC-A
;PROJETO GORILAS - INTRODUCAO A ARQUITETURA DE COMPUTADORES

;---------------------------------------------------------------------------------------------
;Zona de constantes;
;---------------------------------------------------------------------------------------------
					
G 					EQU 	0000010011100110b 			; metade da gravidade -> 4,9 em decimal (9.8/2)
IO_READ 			EQU 	FFFFh						; endereco do porto de leitura 
IO_WRITE			EQU 	FFFEh						; endereco do porto de escrita 
IO_STATUS 			EQU 	FFFDh 						; endereço do porto de estado 
IO_CTRL 			EQU 	FFFCh						; endereco do porto de controlo 
IO_INI 				EQU 	FFFFh 						; inicializar o controlo
SP_INICIAL      	EQU     FDFFh
INT_MASK_ADDR   	EQU     FFFAh						; endereco da mascara de interrupcoes
INT_MASK        	EQU     1000000000000010b			; ativa o temporizador
CLOCK_STATE_ADDR	EQU		FFF7h						; endereco para ligar ou parar o temporizador
CLOCK_STATE			EQU		1b							; usado para ligar o temporizador
TICKS_PER_SEC_ADDR	EQU		FFF6h						; endereco do valor do contador associado ao temporizador
TICKS_PER_SEC		EQU		1
SEC_PER_MIN			EQU		60
NUM_PER_DIGIT		EQU		10
DISPLAY_7			EQU 	FFF0h						; endereco da primeira posicao do display de 7 Leds
MASC_INI 			EQU 	3FFFh						; inicializacao das mascaras

;---------------------------------------------------------------------------------------------
;Zona de variáveis;
;---------------------------------------------------------------------------------------------

		ORIG 8000h
					
POS_CURSOR 			WORD 	0 							; posicao do cursor
tab_sin30			STR		0000000000000000b, 0000000000000100b, 0000000000001000b, 0000000000001101b, 0000000000010001b, 0000000000010110b, 0000000000011010b, 0000000000011111b, 0000000000100011b, 0000000000101000b, 0000000000101100b, 0000000000111100b, 0000000000110101b, 0000000000111001b, 0000000000111101b, 0000000001000010b, 0000000001000110b, 0000000001001010b, 0000000001001111b, 0000000001010011b, 0000000001010111b, 0000000001011011b, 0000000001011111b, 0000000001100110b, 0000000001101001b, 0000000001101101b, 0000000001110010b, 0000000001110110b, 0000000001111001b, 0000000001111101b, 0000000001111111b ;Q8.8
tab_sin60			STR 	0000000010000011b, 0000000010000111b, 0000000010001011b, 0000000010001111b, 0000000010010010b, 0000000010010110b, 0000000010011010b, 0000000010011101b, 0000000010100001b, 0000000010100100b, 0000000010100111b, 0000000010101011b, 0000000010101110b, 0000000010110001b, 0000000010110101b, 0000000010111001b, 0000000010111011b, 0000000010111110b, 0000000011001100b, 0000000011000100b, 0000000011000110b, 0000000011001001b, 0000000011001100b, 0000000011001111b, 0000000011010001b, 0000000011010110b, 0000000011010110b, 0000000011011001b, 0000000011011011b, 0000000011011101b  ;Q8.8
tab_sin90			STR 	0000000011011111b, 0000000011110111b, 0000000011100100b, 0000000011100110b, 0000000011101001b, 0000000011101001b, 0000000011101011b, 0000000011101101b, 0000000011101110b, 0000000011110001b, 0000000011110010b, 0000000011110011b, 0000000011110100b, 0000000011110110b, 0000000011110111b, 0000000011110111b, 0000000011111001b, 0000000011111010b, 0000000011111011b, 0000000011111101b, 0000000011111100b, 0000000011111101b, 0000000011111110b, 0000000011111110b, 0000000011111111b, 0000000011111111b, 0000000011111111b, 0000000011111111b, 0000000011111111b, 0000000100000000b  ;Q8.8
X 					WORD	0							; variavel do X 
Y 					WORD 	0							; variavel do Y
TEMPO 				WORD 	0							; variavel do tempo
INT_CLICKED 		WORD 	0							; verifica se foi pressionado algum botao. se estiver a 1, significa que foi pressionado
VAL_INT 			WORD 	0							; valor do butao pressionado
ESCREVE_INT 		WORD 	0							; valor do butao pressionado a ser escrito na janela de texto
PRESS_KEY 			STR 	'PRESS ANY KEY TO START%'	; janela de inicio de jogo
INPUT_ANGULO 		STR 	'ANGULO: %'						
ANG 				WORD 	0							; valor do angulo que o utilizador escreveu
INPUT_VELOCIDADE 	STR 	'VELOCIDADE: %'					
VEL 				WORD 	0							; valor da velocidade que o utilizador escreveu
COSSENO 			WORD 	0							; valor do cosseno
SENO 				WORD 	0							; valor do seno
POS_GORILA 			WORD 	0002H						; posicao do gorila
BAN					WORD 	1700h						; posicao da banana
BANX				WORD 	0005h						; valor inicial em x da banana
POS_GORILA1 		WORD 	1502H						; posicao do primeiro gorila (gorila que tem a banana)
POS_GORILA2 		WORD 	1532H						; posicao do segundo gorila 
HITBOX				WORD 	0000H						; valor da hitbox
mascara 			WORD 	1234h
Contador  			WORD    0000h
Activo				WORD    CLOCK_STATE
Pausa				WORD	0000h						; usado para pausar a incrementacao do contador, comeca a 0
POS_BANANA_INI 		WORD 	0000h
SCORE				WORD	0H							; valor da pontuacao

;--------------------------------------------------------------------
;INTERRUPCOES
;---------------------------------------------------------------------
	ORIG FE00h
														;associar funcoes as interrupcoes 
INT_0 				WORD 	int0
INT_1  				WORD 	int1
INT_2  				WORD 	int2
INT_3  				WORD 	int3
INT_4  				WORD 	int4
INT_5  				WORD 	int5
INT_6  				WORD 	int6
INT_7  				WORD 	int7
INT_8  				WORD 	int8
INT_9  				WORD 	int9
INT_A  				WORD 	inta
INT1   				WORD 	Stop
INT_C				WORD 	inta
INT_D				WORD 	inta
INT_E				WORD 	inta
INT15				WORD 	Segundos



;---------------------------------------------------------------------------------------------
;Programa;
;---------------------------------------------------------------------------------------------

		ORIG 0000h

					JMP     Inicio

;---------------------------------------------------------------------------------------------
;Timer;
;---------------------------------------------------------------------------------------------					

Stop:				PUSH	R1
					CMP		M[Pausa], R0 					; compara o valor de Pausa com 0
					BR.Z	StopA		 					; se Pausa for 0 , passa-o para 1, para parar a INC do Contador
					MOV     M[Pausa], R0
					POP		R1
					RTI
					
StopA:				MOV     R1, 1							; se Pausa for 1, passa-o para 0, reiniciar a INC do Contador
					MOV     M[Pausa], R1
					POP		R1
					RTI

Segundos:			PUSH 	R1
					PUSH	R2
					CMP		M[Pausa], R0					; se o valor de Pausa for 1, deixa de incrementar o Contador
					BR.NZ	CicloPausa						; se o valor de Pausa for 0, continua a INC normal do Contador
					MOV 	R1, M[Contador]
					MOV 	R2, 000bh
					ADD 	R1, R2
					MOV 	M[Contador], R1

CicloPausa:			POP 	R2
					POP 	R1
					PUSH	R2
					MOV     R2, TICKS_PER_SEC		
					MOV     M[TICKS_PER_SEC_ADDR], R2
					MOV     R2, CLOCK_STATE			
					MOV     M[CLOCK_STATE_ADDR], R2
					POP		R2
					RTI
					
;---------------------------------------------------------------------------------------------
;Inicio do jogo;
;---------------------------------------------------------------------------------------------

Inicio: 			MOV     R7, SP_INICIAL					; iniciar o SP
					MOV     SP, R7							  
					MOV		R7, MASC_INI					; iniciar as mascaras de interrupcoes
					MOV		M[INT_MASK_ADDR], R7			
					MOV     R7, CLOCK_STATE					; iniciar o temporizador
					MOV     M[CLOCK_STATE_ADDR], R7                
					MOV     R7, TICKS_PER_SEC				; valor do contador associado ao temporizador
					MOV     M[TICKS_PER_SEC_ADDR], R7  
					MOV 	M[SCORE], R0					; passar o score para zero 
					MOV 	M[DISPLAY_7], R0				; passar o display de sete digitos para zero
					ENI				
					MOV 	R1, IO_INI					
					MOV 	M[IO_CTRL], R1 					; inicializar porto de controlo
					
sub_inicio:			MOV 	R6, R0
					JMP 	start							; chamar a funcao start
					
beginning2:			ENI
					MOV 	R6, R0
					MOV     R7, INT_MASK					; iniciar o vetor de interrupcoes usados para o temporizador
					MOV     M[INT_MASK_ADDR], R7			
					MOV 	M[Contador], R0					; passar o contador para zero
					
Ciclo:				PUSH	R1
					PUSH	R2
					PUSH	R3
					PUSH 	R4
					
subCiclo:			MOV		R5, M[Contador]					
					MOV 	M[TEMPO], R5
					CMP 	M[Contador], R5
					BR.NP 	subCiclo
					MOV 	R1, 20h
					MOV		M[IO_WRITE], R1					; limpar o valor anterior da janela de texto		
					CALL 	func_movimento					; chamar a funcao movimento
					MOV 	R1, M[X]						; passar o valor de x para R1
					MOV 	R2, M[Y]						; passar o valor de y para R1
					MOV 	R4, M[BAN]						; passar para R4 o valor da banana
					AND 	R4, FF00h						; fazer com que a banana comece ao lado do gorila
					SUB 	R4, R2
					MOV 	R2, R4
					ADD		R2,	R0
					SHR 	R1, 8
					MOV 	R4, M[BANX]
					ADD 	R1, R4
					ADD 	R1, 4h			
					AND 	R1, 00FFh
					AND 	R2, FF00h
					ADD 	R1, R2				
					MOV 	R6, R1
					MOV 	M[IO_CTRL], R1								
					MOV 	R3, ')'	
					MOV 	M[IO_WRITE], R3					; desenha a banana
					POP 	R4
					POP		R3
					POP		R2
					POP		R1
					MOV		R7, R6
					AND 	R6, FF00h
					AND		R7, 00FFh
					MOV 	R1, M[HITBOX]
					MOV		R2, 0002h
					ADD		R1, R2
					MOV 	R2, M[HITBOX]
					AND		R2, 00FFh
					AND		R1, 00FFh
					MOV		R3, M[HITBOX]
					MOV 	R4, 0300h
					SUB		R3, R4
					MOV		R4, M[HITBOX]
					AND 	R4, FF00h
					AND		R3,	FF00h
					CMP		R7, R2
					BR.N	next
					CMP		R7, R1
					BR.P	next
					CMP		R6, R4
					BR.P	next
					CMP		R6, R3
					BR.N	next
					MOV		M[IO_CTRL], R0
					CALL 	limpar
					DSI
					MOV 	R1, M[SCORE]
					CMP 	R1, 4
					JMP.Z 	Inicio
					INC 	R1
					MOV 	M[SCORE], R1
					MOV 	M[DISPLAY_7], R1
					JMP		begin

next:				CMP		R6, 1700h
					JMP.P 	begin
					CMP 	R7, 004Fh
					JMP.P 	begin
					JMP		Ciclo
								
;---------------------------------------------------------------------------------------------
;Funcao do menu;
;---------------------------------------------------------------------------------------------
					
					
					
start:				ENI
					MOV 	R1, PRESS_KEY					; passar para R1 o valor da string PRESS_KEY
					MOV 	M[POS_CURSOR], R0				; passar a posicao do cursor para zero
					CALL	escrever						; escrever a string
					MOV 	R2, R0							; limpar R2
					CALL 	press_start						; chamar a funcao que permite pressionar um botao para o jogo iniciar
					DSI
					
begin:				ENI										; fazer enable das interrupcoes
					MOV 	M[IO_CTRL], R0					; passar a posicao do cursor para 0
					CALL 	limpar							; limpar o ecra
					CALL 	random
					MOV 	R7, MASC_INI					; iniciar as interrupcoes
					MOV		M[INT_MASK_ADDR], R7
					MOV 	R1, M[mascara]
					AND 	R1, 0001Fh
					CALL 	random							; chamar a funcao random
					MOV 	R2, M[mascara]
					AND 	R2, 000Fh
					SHL 	R2, 8
					MOV 	R3, 1700h
					SUB 	R3, R2
					MOV 	R2, R3 
					MOV 	R3, 2ah
					ADD 	R2, R3
					ADD 	R1, R2
					MOV 	M[POS_GORILA], R1
					MOV 	M[HITBOX], R1
					CALL 	desenha_gorila					; desenha o segundo gorila
					CALL 	random
					MOV 	R1, M[mascara]
					AND 	R1, 001Fh
					MOV 	M[BANX], R1
					CALL 	random
					MOV 	R2, M[mascara]
					AND 	R2, 000Fh
					SHL 	R2, 8
					MOV 	R3, 1700h
					SUB 	R3, R2
					MOV 	R2, R3 
					ADD 	R1, R2
					MOV 	M[POS_GORILA], R1				; passar a posicao do gorila para R1
					MOV 	M[BAN], R2						; guardar a posicao da banana
					CALL 	desenha_gorila					; desenhar o primeiro gorila
					MOV 	M[POS_CURSOR], R0
					MOV 	R1, INPUT_ANGULO
					CALL 	escrever						; fazer input do angulo
					CALL 	escreve_int
					MOV 	R1, M[VAL_INT]
					MOV 	R2, AH							
					MUL 	R2, R1							; multiplicar o primeiro valor por 10
					MOV 	M[ANG], R1						; guardar o valor no angulo
					MOV 	R1, R0							; limpar R1
					CALL 	escreve_int						; input das unidades do angulo
					MOV 	R1, M[VAL_INT]					; passar para R1 o valor das unidades do angulo
					MOV 	R2, M[ANG]					
					ADD 	R1, R2							; somar as unidades ao angulo
					MOV 	R2, R0 
					SHL 	R1, 8							; passar para Q8.8
					MOV 	M[ANG], R1						; guardar o angulo				
					MOV 	R1, M[POS_CURSOR]				; passar para R1 o valor da posicao do cursor
					AND 	R1, FF00H						; passar para a coluna 0 da linha seguinte
					ADD 	R1, 0100H
					MOV 	M[IO_CTRL], R1
					MOV 	M[POS_CURSOR], R1				
					MOV 	R1, INPUT_VELOCIDADE			; fazer input da velocidade
					CALL 	escrever
					CALL 	escreve_int
					MOV 	R1, M[VAL_INT]					; mesma logica que foi feita para o angulo
					MOV 	R2, AH
					MUL 	R2, R1
					MOV 	M[VEL], R1
					MOV 	R1, R0
					CALL 	escreve_int
					MOV 	R1, M[VAL_INT]
					MOV 	R2, M[VEL]
					ADD 	R1, R2
					MOV 	R2, R0
					SHL 	R1, 8
					MOV 	M[VEL], R1
					DSI										; disable das interrupcoes
					JMP 	beginning2

;---------------------------------------------------------------------------------------------
;Movimento;
;---------------------------------------------------------------------------------------------		
		
func_movimento: 	PUSH 	R1
					PUSH 	R2
					PUSH 	R3
					PUSH 	R4
					PUSH 	R5
					PUSH 	R6
					PUSH 	R7
					
seno:				MOV		R1, M[ANG]
					SHR 	R1, 8
					CMP 	R1, 61 							; verificar se esta na tabela 3 (senos de 61 a 90)
					BR.N 	tab2 							; se nao, vai para a tabela 2 (senos de 31 a 60)
					SUB 	R1, 61
					MOV 	R2, M[R1 + tab_sin90]  			; passar para R2 o valor do seno 
					MOV 	M[SENO], R2
					BR 		cosseno  						; saltar para o cosseno depois de se ter o seno
					
tab2: 				CMP 	R1, 31 							; verificar se esta na tabela 2
					BR.N 	tab1 							; se nao, passar para a tabela 1
					SUB		R1, 31
					MOV 	R2, M[R1 + tab_sin60]  			; passar para R2 o valor do seno
					MOV 	M[SENO], R2
					BR 		cosseno  						; saltar para o cosseno depois de se ter o seno
tab1:				MOV 	R2, M[R1 + tab_sin30]
					MOV 	M[SENO], R2
		
cosseno:			MOV		R3, M[ANG]
					SHR 	R3, 8
					MOV 	R1, 90
					SUB 	R1, R3  						; cosseno é 90-angulo
					CMP 	R1, 61 							; verificar em que tabela está
					BR.N 	tb2  							; se nao, saltar
					SUB 	R1, 61 
					MOV 	R3, M[R1 + tab_sin90] 			; retirar o valor do cosseno
					MOV 	M[COSSENO], R3
					BR 		funcx 							; passar para a funcao que calcula o x
					
tb2: 				CMP 	R1, 31
					BR.N 	tb1
					SUB		R1, 31
					MOV 	R3, M[R1 + tab_sin60]
					MOV 	M[COSSENO], R3
					BR 		funcx
						
tb1:				MOV 	R3, M[R1 + tab_sin30]
					MOV 	M[COSSENO], R3
		
funcx:				MOV 	R4, M[VEL]  					; funcao que calcula o x. passar para R4 o valor da velocidade
					MOV 	R5, M[TEMPO] 					; passar para R5 o valor do tempo
					MUL 	R5, R4	 						; multiplicar tempo por velocidade	; no r5 ficam os valores mais significativos, no r4 os menos
					SHL 	R5, 8							; passar para Q8.8 
					SHR 	R4, 8
					MVBL 	R5, R4
					MOV 	R3, M[COSSENO]					; guardar na variavel cosseno o valor do cosseno 
					MUL 	R5, R3 							; tempo * velocidade * cosseno
					SHL 	R5, 8							; passar para R4
					SHR 	R3, 8
					MVBL	R5, R3
					MOV 	M[X], R5  						; passar o valor de R3 para variavel X
				
funcy:				MOV 	R4, M[VEL] 						; funcao que calcula o y . passar velocidade para R4
					MOV 	R5, M[TEMPO] 					; passar tempo para R5
					MUL 	R5, R4 							; multiplicar tempo por velocidade 
					SHL 	R5, 8
					SHR 	R4, 8
					MVBL 	R5, R4
					MOV 	R2, M[SENO]
					MUL 	R5, R2 							; multiplicar (tempo*velocidade)* seno
					SHL 	R5, 8
					SHR 	R2, 8
					MVBL 	R5, R2
					MOV 	R6, G  							; passar para R6 a gravidade
					MOV 	R7, M[TEMPO] 
					MOV 	R1, M[TEMPO]
					MUL 	R7, R1 							; tempo ao quadrado 
					SHL 	R7, 8
					SHR 	R1, 8
					MVBL 	R7, R1
					MUL 	R6, R7							; multiplicar gravidade por tempo ao quadrado
					SHL 	R6, 8
					SHR 	R7, 8
					MVBL 	R6, R7
					SUB 	R5, R6 							; subtrair 
					MOV 	M[Y], R5 
					POP 	R7
					POP 	R6
					POP 	R5
					POP 	R4
					POP 	R3	
					POP 	R2
					POP 	R1
					RET

;---------------------------------------------------------------------------------------------
;Random number;
;---------------------------------------------------------------------------------------------

random:				PUSH 	R1								; cria um numero aleatorio
					MOV 	R1, M[mascara]
					AND 	R1, 0001h
					BR.Z 	etiq
					MOV 	R1, M[mascara]
					XOR 	R1, M[Contador]
					ROR 	R1, 1
					BR 		jp
					
etiq:				MOV 	R1, M[mascara]
					ROR 	R1, 1
					
jp:					MOV 	M[mascara], R1
					POP 	R1
					RET
				
;-------------------------------------------------------------------- 
;CICLO ESCRITA
;--------------------------------------------------------------------

escrever: 			MOV 	R3, M[R1]						; mover para R3 o conteudo de R1, que tem a string que se quer escrever
					CMP 	R3, '%'							; se R3 for % , o ciclo para
					JMP.NZ 	aux_escrever
					RET
			
aux_escrever:		MOV 	R2, M[R1]						; mover para R2 o conteudo de R1	
					MOV 	M[IO_WRITE], R2					; escrever na janela de texto
					INC 	R1								; incrementar R1, passando assim para outro caracter na string
					MOV 	R2, M[POS_CURSOR]				; passar para R2 a posicao do cursor
					INC 	R2								; incrementar R2 de forma a incrementar a posicao do cursor
					MOV 	M[IO_CTRL], R2					; atualizar IO_CTRL
					MOV 	M[POS_CURSOR], R2				; atualizar a posicao do cursor
					BR 		escrever
	
;--------------------------------------------------------------------
;ESCREVER INTERRUPCOES
;--------------------------------------------------------------------

escreve_int:		MOV 	R1, M[INT_CLICKED]				; verificar se foi pressionado algum botao
					CMP 	R1, 1							; se o valor de INT_CLICKED for 1, significa que foi pressionado
					BR.NZ 	escreve_int						; se nao, continuar a espera
					MOV 	M[INT_CLICKED], R0				; passar o valor para 0
					MOV 	R1, M[POS_CURSOR]				; passar para R1 a posicao do cursor
					MOV 	M[IO_CTRL], R1					; passar para IO_CTRL a posicao do cursor
					MOV 	R1, M[ESCREVE_INT]				; passar para R1 o valor do butao
					MOV 	M[IO_WRITE], R1					; escrever esse valor na janela de texto
					MOV 	R1, M[POS_CURSOR]	
					INC 	R1								; passar a posicao do cursor para R1 de novo e incrementa-lo
					MOV 	M[IO_CTRL], R1					; passar para IO_CTRL
					MOV 	M[POS_CURSOR], R1				; passar para a posicao do cursor
					RET
		
;--------------------------------------------------------------------
;PRESS START 
;--------------------------------------------------------------------

press_start:		MOV 	R1, M[INT_CLICKED]				; verificar se foi pressionado algum botao
					CMP 	R1, 1							; se o valor de INT_CLICKED for 1, significa que foi pressionado
					BR.NZ 	press_start						; se nao foi pressionado, continuar a espera
					MOV 	M[INT_CLICKED], R0				; passar o valor para 0
					RET
					
;--------------------------------------------------------------------
;LIMPAR
;--------------------------------------------------------------------

limpar:				PUSH 	R1
					MOV 	R1, IO_INI						; reiniciar a janela de texto							
					MOV 	M[IO_CTRL], R1
					POP 	R1
					RET
				
				
;--------------------------------------------------------------------
;DESENHAR GORILA
;--------------------------------------------------------------------

desenha_gorila: 	MOV 	R1, M[POS_GORILA]				; a funcao desenha gorila vai posicao a posicao para desenhar o corpo do gorila
					MOV 	M[IO_CTRL], R1					; comeca pela perna esquerda do gorila
					MOV 	R1, '|'
					MOV 	M[IO_WRITE], R1
					MOV 	R1, M[POS_GORILA]
					INC 	R1
					INC 	R1
					MOV 	M[POS_GORILA], R1
					MOV 	M[IO_CTRL], R1
					MOV 	R1, '|'
					MOV 	M[IO_WRITE], R1
					MOV 	R1, M[POS_GORILA]
					MOV 	R2, 0100h						
					SUB 	R1, R2							; subir de linha
					MOV 	M[POS_GORILA], R1
					MOV 	M[IO_CTRL], R1
					MOV 	R1, '\'
					MOV 	M[IO_WRITE], R1
					MOV 	R1, M[POS_GORILA]
					DEC 	R1
					MOV 	M[POS_GORILA], R1
					MOV 	M[IO_CTRL], R1
					MOV 	R1, '@' 
					MOV 	M[IO_WRITE], R1
					MOV 	R1, M[POS_GORILA]
					DEC 	R1
					MOV 	M[POS_GORILA], R1
					MOV 	M[IO_CTRL], R1
					MOV 	R1, '/'
					MOV 	M[IO_WRITE], R1
					MOV 	R1, M[POS_GORILA]
					MOV 	R2, 0100H						
					SUB 	R1, R2							; subir a linha
					MOV 	M[POS_GORILA], R1
					MOV 	M[IO_CTRL], R1
					INC 	R1
					MOV 	M[POS_GORILA], R1
					MOV 	M[IO_CTRL], R1
					MOV 	R1, 'O'
					MOV 	M[IO_WRITE], R1
					MOV 	R1, R0
					MOV 	M[IO_CTRL], R1
					MOV 	M[POS_CURSOR], R1
					RET
				
;--------------------------------------------------------------------
;INTERRUPCOES
;--------------------------------------------------------------------

int0: 				MOV 	R1, 1							
					MOV 	M[INT_CLICKED], R1				; passar para INT_CLICKED o valor de 1
					MOV 	R1, 0	
					MOV 	M[VAL_INT], R1					; passar para VAL_INT o valor do butao
					MOV 	R1, '0'
					MOV 	M[ESCREVE_INT], R1				; passar para ESCREVE_INT o que se quer que se escreva na janela de texto
					RTI
		
int1: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 1
					MOV 	M[VAL_INT], R1
					MOV 	R1, '1'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int2: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 2
					MOV 	M[VAL_INT], R1
					MOV 	R1, '2'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int3: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 3
					MOV 	M[VAL_INT], R1
					MOV 	R1, '3'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int4: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 4
					MOV 	M[VAL_INT], R1
					MOV 	R1, '4'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int5: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 5
					MOV 	M[VAL_INT], R1
					MOV	 	R1, '5'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int6: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 6
					MOV 	M[VAL_INT], R1
					MOV 	R1, '6'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int7: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 7
					MOV 	M[VAL_INT], R1
					MOV 	R1, '7'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int8: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 8
					MOV 	M[VAL_INT], R1
					MOV 	R1, '8'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
int9: 				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 9
					MOV 	M[VAL_INT], R1
					MOV 	R1, '9'
					MOV 	M[ESCREVE_INT], R1
					RTI
		
inta:				MOV 	R1, 1
					MOV 	M[INT_CLICKED], R1
					MOV 	R1, 0
					MOV 	M[VAL_INT], R1
					MOV 	R1, '0'
					MOV 	M[ESCREVE_INT], R1
					RTI