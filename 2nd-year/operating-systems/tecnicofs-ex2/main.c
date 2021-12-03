/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <stdbool.h>
#include <semaphore.h>
#include "fs.h"
#include "constants.h"
#include "lib/timer.h"
#include "sync.h"


//args
char* global_inputFile = NULL;
char* global_outputFile = NULL;
int numberThreads = 0;
int numBuckets = 0;

//filesystem
tecnicofs* fs;

//producer-consumer
char inputCommands[BUFFER_SIZE][MAX_INPUT_SIZE];
int prodPtr, consPtr = 0;
sem_t canProd, canCons;
pthread_mutex_t consLock;


static void displayUsage (const char* appName){
    printf("Usage: %s input_filepath output_filepath threads_number buckets_number\n", appName);
    exit(EXIT_FAILURE);
}

static void parseArgs (long argc, char* const argv[]){
    if (argc != 5) {
        fprintf(stderr, "Invalid format:\n");
        displayUsage(argv[0]);
    }
    global_inputFile = argv[1];
    global_outputFile = argv[2];
    numberThreads = atoi(argv[3]);
    numBuckets = atoi(argv[4]);
    if (numberThreads < 1 || numBuckets < 1) {
        fprintf(stderr, "Invalid number of threads\n");
        displayUsage(argv[0]);
    }
}

void errorParse(int lineNumber){
    fprintf(stderr, "Error: line %d invalid\n", lineNumber);
    exit(EXIT_FAILURE);
}

void* prod_processInput(){
    FILE* inputFile;
    inputFile = fopen(global_inputFile, "r");
    if(!inputFile){
        fprintf(stderr, "Error: Could not read %s\n", global_inputFile);
        exit(EXIT_FAILURE);
    }
    char line[MAX_INPUT_SIZE];
    int lineNumber = 0;

    while (fgets(line, sizeof(line)/sizeof(char), inputFile)) {
        char token;
        char name1[MAX_INPUT_SIZE], name2[MAX_INPUT_SIZE];
        lineNumber++;
        int numTokens = sscanf(line, "%c %s %s", &token, name1, name2);

        if (numTokens < 1) {
            continue;
        }
        switch (token) {
            case 'r':
                if(numTokens != 3)
                    errorParse(lineNumber);
            case 'c':
            case 'l':
            case 'd':
                if(token!='r' && numTokens!=2)
                    errorParse(lineNumber);
                sem_wait(&canProd);
                strcpy(inputCommands[prodPtr], line);
                prodPtr = (prodPtr+1) % BUFFER_SIZE;
                sem_post(&canCons);
                break;
            case '#':
                break;
            default: { //error
                errorParse(lineNumber);
            }
        }
    }
    //found EOF
    sem_wait(&canProd);
    inputCommands[prodPtr][0] = EOF;
    //increments semaphore number_threads times
    for(int i=0; i<numberThreads; i++)
        sem_post(&canCons);
    fclose(inputFile);
    return 0;
}

FILE* openOutputFile() {
    FILE* outputFile;
    outputFile = fopen(global_outputFile, "w");
    if(!outputFile){
        fprintf(stderr, "Error: Could not read %s\n", global_outputFile);
        exit(EXIT_FAILURE);
    }
    return outputFile;
}

void* cons_applyCommands(){
    while(true) {
        sem_wait(&canCons);
        mutex_lock(&consLock);

        const char* command = inputCommands[consPtr];
        char token;
        char name1[MAX_INPUT_SIZE], name2[MAX_INPUT_SIZE];

        sscanf(command, "%c %s %s", &token, name1, name2);
        int iNumber;
        switch (token) {
            case 'c':
                consPtr = (consPtr+1) % BUFFER_SIZE;
                iNumber = obtainNewInumber(fs);
                mutex_unlock(&consLock);
                sem_post(&canProd);
                call_create(fs, name1, iNumber);
                break;
            case 'l':
                consPtr = (consPtr+1) % BUFFER_SIZE;
                mutex_unlock(&consLock);
                sem_post(&canProd);
                int searchResult = call_lookup(fs, name1);
                if(!searchResult)
                    printf("%s not found\n", name1);
                else
                    printf("%s found with inumber %d\n", name1, searchResult);
                break;
            case 'd':
                consPtr = (consPtr+1) % BUFFER_SIZE;
                mutex_unlock(&consLock);
                sem_post(&canProd);
                call_delete(fs, name1);
                break;
            case 'r':
                consPtr = (consPtr+1) % BUFFER_SIZE;
                mutex_unlock(&consLock);
                sem_post(&canProd);
                call_move(fs, name1, name2);
                break;
            case EOF:
                //consPtr does not move forward!
                mutex_unlock(&consLock);
                return 0;
            default: { //error
                mutex_unlock(&consLock);
                sem_post(&canProd);
                inputCommands[consPtr][0] = '\0';
                fprintf(stderr, "Error: commands to apply\n");
                exit(EXIT_FAILURE);
            }
        }
    }
    return 0;
}

void runThreads(FILE* timeFp){
    TIMER_T startTime, stopTime;
    pthread_t* prod = (pthread_t*) malloc(sizeof(pthread_t));
    pthread_t* cons = (pthread_t*) malloc(numberThreads * sizeof(pthread_t));

    TIMER_READ(startTime);

    //create 1 producer thread
    int errProd = pthread_create(&prod[0], NULL, prod_processInput, NULL);
    if (errProd != 0){
        perror("Can't create thread");
        exit(EXIT_FAILURE);
    }

    //create numberThreads consumer threads
    for(int i=0; i<numberThreads; i++){
        int errCons = pthread_create(&cons[i], NULL, cons_applyCommands, NULL);
        if (errCons != 0){
            perror("Can't create thread");
            exit(EXIT_FAILURE);
        }
    }

    //wait for 1 producer
    if(pthread_join(prod[0], NULL)) {
        perror("Can't join thread");
    }

    //wait for numberThreads consumers
    for(int i=0; i<numberThreads; i++) {
        if(pthread_join(cons[i], NULL)) {
            perror("Can't join thread");
        }
    }

    TIMER_READ(stopTime);
    fprintf(timeFp, "TecnicoFS completed in %.4f seconds.\n", TIMER_DIFF_SECONDS(startTime, stopTime));

    free(prod);
    free(cons);
}

void init_main() {
    sem_init(&canProd, 0, BUFFER_SIZE);
    sem_init(&canCons, 0, 0);
    mutex_init(&consLock);
}

void final_main() {
    sem_destroy(&canProd);
    sem_destroy(&canCons);
    mutex_destroy(&consLock);
}

int main(int argc, char* argv[]) {
    parseArgs(argc, argv);
    FILE* outputFp = openOutputFile();
    init_main();
    fs = new_tecnicofs(numBuckets);

    runThreads(stdout);
    print_tecnicofs_trees(outputFp, fs);
    fflush(outputFp);
    fclose(outputFp);

    final_main();
    free_tecnicofs(fs);
    exit(EXIT_SUCCESS);
}
