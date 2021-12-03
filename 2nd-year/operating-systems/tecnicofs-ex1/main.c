/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#include <stdio.h>
#include <stdlib.h> //EXIT_FAILURE, atoi
#include <getopt.h>
#include <string.h>
#include <ctype.h>
#include <pthread.h>
#include <sys/time.h> //gettimeofday()
#include "fs.h"

#define MAX_COMMANDS 150000
#define MAX_INPUT_SIZE 100

//args
char* inputFileName;
char* outputFileName;
int numberThreads = 0;

//common to all threads
tecnicofs* fs;

#ifdef MUTEX
    pthread_mutex_t mutex;
#elif RWLOCK
    pthread_rwlock_t rwlock;
#endif

char inputCommands[MAX_COMMANDS][MAX_INPUT_SIZE];
int numberCommands = 0;
int headQueue = 0;


//lock functions
void lockInit() {
	#ifdef MUTEX
		if(pthread_mutex_init(&mutex, NULL)) {
            fprintf(stderr, "Error: could not initialize mutex\n");
            exit(EXIT_FAILURE);
        }
	#elif RWLOCK
		if(pthread_rwlock_init(&rwlock, NULL)) {
            fprintf(stderr, "Error: could not initialize rwlock\n");
            exit(EXIT_FAILURE);
        }
	#endif
}

void lockWrite() {
	#ifdef MUTEX
		if(pthread_mutex_lock(&mutex)) {
            fprintf(stderr, "Error: could not lock mutex\n");
            exit(EXIT_FAILURE);
        }
	#elif RWLOCK
		if(pthread_rwlock_wrlock(&rwlock)) {
            fprintf(stderr, "Error: could not writelock rwlock\n");
            exit(EXIT_FAILURE);
        }
	#endif
}

void lockRead() {
	#ifdef MUTEX
        if(pthread_mutex_lock(&mutex)) {
            fprintf(stderr, "Error: could not lock mutex\n");
            exit(EXIT_FAILURE);
        }
	#elif RWLOCK
		if(pthread_rwlock_rdlock(&rwlock)) {
            fprintf(stderr, "Error: could not readlock rwlock\n");
            exit(EXIT_FAILURE);
        }
	#endif
}

void unlock() {
	#ifdef MUTEX
		if(pthread_mutex_unlock(&mutex)) {
            fprintf(stderr, "Error: could not unlock mutex\n");
            exit(EXIT_FAILURE);
        }
	#elif RWLOCK
		if(pthread_rwlock_unlock(&rwlock)) {
            fprintf(stderr, "Error: could not unlock rwlock\n");
            exit(EXIT_FAILURE);
        }
	#endif
}

void lockDestroy() {
	#ifdef MUTEX
		if(pthread_mutex_destroy(&mutex)) {
            fprintf(stderr, "Error: could not destroy mutex\n");
            exit(EXIT_FAILURE);
        }
	#elif RWLOCK
		if(pthread_rwlock_destroy(&rwlock)) {
            fprintf(stderr, "Error: could not destroy rwlock\n");
            exit(EXIT_FAILURE);
        }
	#endif
}

//private functions
static void displayUsage (const char* appName){
    printf("Usage: %s inputfile outputfile numthreads\n", appName);
    exit(EXIT_FAILURE);
}

int insertCommand(char* data) {
    if(numberCommands < MAX_COMMANDS) {
        strcpy(inputCommands[numberCommands++], data);
        return 1;
    }
    return 0;
}

void errorParse(){
    fprintf(stderr, "Error: command invalid\n");
    //exit(EXIT_FAILURE);
}

char* removeCommand() {
    char* new_command = NULL;
    if(numberCommands > 0){
        numberCommands--;
        new_command = inputCommands[headQueue++];
    }
    return new_command;
}

int getNumberCommands() {
    int result;
    lockRead();
    result = numberCommands;
    unlock();
    return result;
}

void* applyCommands() {
    while(getNumberCommands() > 0) {
        //get command
        lockWrite();
        const char* command = removeCommand();
        if (command == NULL)
            continue;

        //parse command
        char token;
        char name[MAX_INPUT_SIZE];
        int numTokens = sscanf(command, "%c %s", &token, name);
        if (numTokens != 2) {
            fprintf(stderr, "Error: invalid command in Queue\n");
            exit(EXIT_FAILURE);
        }

        int searchResult;
        int iNumber;
        //executes corresponding command
        switch (token) {
            case 'c':
                iNumber = obtainNewInumber(fs);
                unlock();
                create(fs, name, iNumber);
                break;
            case 'l':
                unlock();
                searchResult = lookup(fs, name);
                if(!searchResult)
                    printf("%s not found\n", name);
                else
                    printf("%s found with inumber %d\n", name, searchResult);
                break;
            case 'd':
                unlock();
                delete(fs, name);
                break;
            default:
                fprintf(stderr, "Error: command to apply\n");
                exit(EXIT_FAILURE);
        }
    }
    return NULL;
}

//public functions
static void parseArgs (long argc, char* const argv[]){
    if (argc == 4 && (numberThreads=atoi(argv[3])) >= 1) {
        #ifndef MUTEX
            #ifndef RWLOCK
                numberThreads = 1; //if file -nosync numberThreads=1
            #endif
        #endif
        inputFileName = argv[1];
        outputFileName = argv[2];
    }
    else {
        fprintf(stderr, "Invalid format:\n");
        displayUsage(argv[0]);
    }
}

void processInput(){
    char line[MAX_INPUT_SIZE];
    FILE *inputFile;

    if ((inputFile = fopen(inputFileName, "r")) == NULL) {
        fprintf(stderr, "Error: invalid input file\n");
        exit(EXIT_FAILURE);
    }

    while (fgets(line, sizeof(line)/sizeof(char), inputFile)) {
        char token;
        char name[MAX_INPUT_SIZE];

        int numTokens = sscanf(line, "%c %s", &token, name);

        /* perform minimal validation */
        if (numTokens < 1) {
            continue;
        }
        switch (token) {
            case 'c':
            case 'l':
            case 'd':
                if(numTokens != 2)
                    errorParse();
                if(insertCommand(line))
                    break;
                return;
            case '#':
                break;
            default: { /* error */
                errorParse();
            }
        }
    }
    fclose(inputFile);
}

void threadPool() {
    int i;
    pthread_t tid[numberThreads];
    struct timeval start, end;
    float timeSpent;

    lockInit();

    if(gettimeofday(&start, NULL)) {
        fprintf(stderr, "Error: couldn't get time of day\n");
        exit(EXIT_FAILURE);
    }

    for(i=0; i<numberThreads; i++) {
        if(pthread_create(&tid[i], 0, applyCommands, NULL))
            exit(EXIT_FAILURE);
    }
    for(i=0; i<numberThreads; i++) {
        if(pthread_join(tid[i], NULL))
            exit(EXIT_FAILURE);
    }

    lockDestroy();

    if(gettimeofday(&end, NULL)) {
        fprintf(stderr, "Error: couldn't get time of day\n");
        exit(EXIT_FAILURE);
    }
    timeSpent = (end.tv_sec+end.tv_usec/1000000.0) - (start.tv_sec+start.tv_usec/1000000.0);
    printf("TecnicoFS completed in %.4f seconds.\n", timeSpent);
}

//main
int main(int argc, char* argv[]) {
    parseArgs(argc, argv);
    processInput();
    fs = new_tecnicofs();
    threadPool();
    print_tecnicofs_tree(outputFileName, fs);
    free_tecnicofs(fs);
    exit(EXIT_SUCCESS);
}
