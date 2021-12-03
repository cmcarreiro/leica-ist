/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

//socket stream server

#define _GNU_SOURCE //must be defined before any .h files

#include <stdio.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <unistd.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "fs.h"
#include "lib/constants.h"
#include "lib/timer.h"
#include "lib/sync.h"



//arguments from terminal
char* socket_name;
char* output_file;
int num_buckets;

//socket used to communicate with client
int serv_sockfd;

//filesystem
tecnicofs* fs;

//used to calculate elapsed time
TIMER_T startTime;
TIMER_T stopTime;

//thread information
pthread_t tid[MAX_THREADS];
int numThreads;


void displayUsage (char* app_name){
    printf("Usage: %s socket_name output_file number_buckets\n", app_name);
    exit(EXIT_FAILURE);
}

void parseArgs (int argc, char* argv[]){
    if (argc != 4) {
        fprintf(stderr, "Invalid format:\n");
        displayUsage(argv[0]);
    }
    socket_name = argv[1];
    output_file = argv[2];
    num_buckets = atoi(argv[3]);
    if (num_buckets < 1) {
        fprintf(stderr, "Invalid number of buckets\n");
        displayUsage(argv[0]);
    }
}

void errorParse(int lineNumber){
    fprintf(stderr, "Error: line %d invalid\n", lineNumber);
    exit(EXIT_FAILURE);
}

FILE* openOutputFile() {
    FILE *fp;
    fp = fopen(output_file, "w");
    if (fp == NULL) {
        perror("Error opening output file");
        exit(EXIT_FAILURE);
    }
    return fp;
}

void commandC(int sockfd, char* buff, uid_t client_uid) {
    permission ownerPerm, othersPerm;
    int retVal;
    char* name = (char*) malloc(sizeof(char) * MAX_NAME);
    sscanf(buff, "c %s %1d%1d", name, (int*)&ownerPerm, (int*)&othersPerm);
    retVal = create_fs(fs, name, client_uid, ownerPerm, othersPerm);
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "%d", retVal);
    write(sockfd, buff, strlen(buff));
    free(name);
}

void commandD(int sockfd, char* buff, uid_t client_uid) {
    int retVal;
    char* name = (char*) malloc(sizeof(char) * MAX_NAME);
    sscanf(buff, "d %s", name);
    retVal = delete_fs(fs, name, client_uid);
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "%d", retVal);
    write(sockfd, buff, strlen(buff));
    free(name);
}

void commandR(int sockfd, char* buff, uid_t client_uid) {
    int retVal;
    char * nameOrig = (char*) malloc(sizeof(char) * MAX_NAME);
    char * nameDest = (char*) malloc(sizeof(char) * MAX_NAME);
    sscanf(buff, "r %s %s", nameOrig, nameDest);
    retVal = change_name_fs(fs, nameOrig, nameDest, client_uid);
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "%d", retVal);
    write(sockfd, buff, strlen(buff));
    free(nameOrig);
    free(nameDest);
}

void commandO(int sockfd, char* buff, int fd_vec[][2], int* numOpenFiles, uid_t client_uid) {
    int retVal, i;
    permission openMode;
    char* name = (char*) malloc(sizeof(char) * MAX_NAME);
    sscanf(buff, "o %s %d", name, (int*)&openMode);
    retVal = open_file_fs(fs, name, openMode, client_uid, *numOpenFiles);
    if(retVal > -1) {
        for(i=0; i<MAX_FDS; i++) {
            if(fd_vec[i][0] == -1) {
                break;
            }
        }
        fd_vec[i][0] = retVal;
        fd_vec[i][1] = openMode;
        *numOpenFiles += 1;
    }
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "%d", retVal);
    write(sockfd, buff, strlen(buff));
    free(name);
}

void commandX(int sockfd, char* buff, int fd_vec[][2], int *numOpenFiles) {
    int retVal, fd, i;
    sscanf(buff, "x %d", &fd);
    retVal = TECNICOFS_ERROR_FILE_NOT_OPEN;
    for(i=0; i<MAX_FDS; i++) {
        if(fd_vec[i][0] == fd) {
          fd_vec[i][0] = -1;
          fd_vec[i][1] = -1;
          *numOpenFiles -= 1;
          retVal = 0;
          break;
        }
    }
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "%d", retVal);
    write(sockfd, buff, strlen(buff));
}

void commandL(int sockfd, char* buff, int fd_vec[][2]) {
    int retVal, i, inumber, fd, len;
    sscanf(buff, "l %d %d", &fd, &len);
    char* readBuff = (char*) malloc(sizeof(char) * len);
    inumber = -1;
    for(i=0; i<MAX_FDS; i++) {
        if(fd_vec[i][0] == fd) {
            inumber = fd_vec[i][0];
            if(fd_vec[i][1] < 2)
                retVal = TECNICOFS_ERROR_INVALID_MODE;
            break;
        }
    }
    if(inumber == -1)
        retVal = TECNICOFS_ERROR_FILE_NOT_OPEN;
    else if(retVal != TECNICOFS_ERROR_INVALID_MODE)
        retVal = read_file_fs(fs, fd, readBuff, len);
    bzero(buff, MAX_MSG);
    if(retVal < 0)
        snprintf(buff, MAX_MSG, "%d", retVal);
    else
        snprintf(buff, MAX_MSG, "%d %s", retVal, readBuff);
    write(sockfd, buff, strlen(buff));
    free(readBuff);
}

void commandW(int sockfd, char* buff, int fd_vec[][2]) {
    int i, retVal, fd, len, inumber = -1;
    char* writeBuff = (char*) malloc(sizeof(char) * MAX_MSG);
    sscanf(buff, "w %d %s %d", &fd, writeBuff, &len);
    for(i=0; i<MAX_FDS; i++) {
        if(fd_vec[i][0] == fd) {
            inumber = fd_vec[i][0];
            if(fd_vec[i][1]!=1 && fd_vec[i][1]!=3)
                retVal = TECNICOFS_ERROR_INVALID_MODE;
            break;
        }
    }
    if(inumber == -1)
        retVal = TECNICOFS_ERROR_FILE_NOT_OPEN;
    else if(retVal != TECNICOFS_ERROR_INVALID_MODE)
        retVal = write_file_fs(fs, fd, writeBuff, len);
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "%d", retVal);
    write(sockfd, buff, strlen(buff));
    free(writeBuff);
}

void* threadFunc(void* newsockfd) {
    int sockfd = *(int*)newsockfd;
    char buff[MAX_MSG];
    int numOpenFiles = 0;
    int fd_vec[MAX_FDS][2];
    uid_t client_uid;

    //stop thread from catching SIGINT
    sigset_t signal_mask;
    sigemptyset(&signal_mask);
    sigaddset(&signal_mask, SIGINT);
    if(pthread_sigmask(SIG_BLOCK, &signal_mask, NULL) != 0)
        perror("server: pthread_sigmask failed");

    //init fd_vec
    for(int i=0; i<MAX_FDS; i++) {
        fd_vec[i][0]=-1;
        fd_vec[i][1]=-1;
    }

    //gets client uid
    struct ucred user_cred;
    socklen_t len = sizeof(struct ucred);
    if (getsockopt(sockfd, SOL_SOCKET, SO_PEERCRED, &user_cred, &len) == -1)
        perror("server: couldn't get client uid\n");
    client_uid = user_cred.uid;

    while(1) {
        bzero(buff, MAX_MSG);
        read(sockfd, buff, MAX_MSG);        //read command sent by client from socket
        //printf("command: %s\n", buff);    //print command
        char token;
        sscanf(buff, "%c ", &token);
        switch (token) {
            case 'e':
                return NULL;
                break;
            case 'c':
                commandC(sockfd, buff, client_uid);
                break;
            case 'd':
                commandD(sockfd, buff, client_uid);
                break;
            case 'r':
                commandR(sockfd, buff, client_uid);
                break;
            case 'o':
                commandO(sockfd, buff, fd_vec, &numOpenFiles, client_uid);
                break;
            case 'x':
                commandX(sockfd, buff, fd_vec, &numOpenFiles);
                break;
            case 'l':
                commandL(sockfd, buff, fd_vec);
                break;
            case 'w':;
                commandW(sockfd, buff, fd_vec);
                break;
            default: { //error
                perror("server: unknown command\n");
                return NULL;
            }
        }
    }
    return NULL; //if all goes well, this line should never be executed
}

void mount() {
    int servlen;
    struct sockaddr_un serv_addr;

    if((serv_sockfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0) { //create socket
        perror("server: can't open stream socket\n");
        exit(EXIT_FAILURE);
      }

    unlink(socket_name); //deletes name, in case it already exists
    bzero((char*)&serv_addr, sizeof(serv_addr));
    serv_addr.sun_family = AF_UNIX;           //socket type
    strcpy(serv_addr.sun_path, socket_name);  //socket name
    servlen = strlen(serv_addr.sun_path) + sizeof(serv_addr.sun_family);
    if(bind(serv_sockfd, (struct sockaddr*) &serv_addr, servlen) < 0) {
        perror("server: can't bind local address");
        exit(EXIT_FAILURE);
      }
    if(listen(serv_sockfd, MAX_LISTEN) < 0) {
        perror("server: can't listen");
        exit(EXIT_FAILURE);
    }
}

void unmount() {
    FILE* outputFp = openOutputFile();

    //waits for all threads to finish
    for(int i=0; i<numThreads; i++)
        pthread_join(tid[i], NULL);

    //print elapsed time to stdout
    TIMER_READ(stopTime);
    printf("TecnicoFS completed in %.4f seconds.\n", TIMER_DIFF_SECONDS(startTime, stopTime));

    //print bst to out_file
    print_tecnicofs_tree(outputFp, fs);
    fflush(outputFp);
    fclose(outputFp);

    free_tecnicofs(fs);
    close(serv_sockfd);
    exit(0);
}

int main(int argc, char* argv[]) {
    int newsockfd, clilen;
    struct sockaddr_un cli_addr;
    signal(SIGINT, unmount); //unmount handles SIGINT
    parseArgs(argc, argv);
    fs = new_tecnicofs();
    mount();
    TIMER_READ(startTime);
    while(1) {
        clilen = sizeof(cli_addr);
        newsockfd = accept(serv_sockfd, (struct sockaddr*) &cli_addr, (socklen_t*) &clilen); //creates socket for new client
        if(newsockfd < 0) perror("server: accept error");
        if(pthread_create(&tid[numThreads], NULL, threadFunc, &newsockfd) != 0)
            perror("server: thread creation failed");
        else
            numThreads++;
    }
}
