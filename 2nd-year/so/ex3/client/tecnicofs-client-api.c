/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

//socket stream client

#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/un.h>
#include "tecnicofs-client-api.h"
#include <assert.h>

//socket used to communicate with server
int sockfd;
char buff[MAX_MSG];

int tfsMount(char* address) {
    int servlen, retVal;
    struct sockaddr_un serv_addr;
    if(sockfd) return TECNICOFS_ERROR_OPEN_SESSION;
    if((sockfd=socket(AF_UNIX, SOCK_STREAM, 0)) < 0) //create socket
        perror("client: can't open stream socket");
    bzero((char*)&serv_addr, sizeof(serv_addr));
    serv_addr.sun_family = AF_UNIX;       //socket type
    strcpy(serv_addr.sun_path, address);  //socket name
    servlen = strlen(serv_addr.sun_path) + sizeof(serv_addr.sun_family);
    if(connect(sockfd, (struct sockaddr*) &serv_addr, servlen) < 0) //connect to server
        perror("client: can't connect to server");
    return 0;
}

int tfsUnmount() {
    if(!sockfd) return TECNICOFS_ERROR_NO_OPEN_SESSION;
    write(sockfd, "e", 1);
    close(sockfd);
    exit(0);
}

int tfsCreate(char* filename, permission ownerPermissions, permission othersPermissions) {
    int retVal;
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "c %s %d%d", filename, ownerPermissions, othersPermissions);
    write(sockfd, buff, strlen(buff));
    bzero(buff, MAX_MSG);
    read(sockfd, buff, MAX_MSG);
    sscanf(buff, "%d", &retVal);
    return retVal;
}

int tfsDelete(char* filename) {
    int retVal;
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "d %s", filename);
    write(sockfd, buff, strlen(buff));
    bzero(buff, MAX_MSG);
    read(sockfd, buff, MAX_MSG);
    sscanf(buff, "%d", &retVal);
    return retVal;
}

int tfsRename(char* filenameOld, char *filenameNew) {
    int retVal;
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "r %s %s", filenameOld, filenameNew);
    write(sockfd, buff, strlen(buff));
    bzero(buff, MAX_MSG);
    read(sockfd, buff, MAX_MSG);
    sscanf(buff, "%d", &retVal);
    return retVal;
}

int tfsOpen(char* filename, permission mode) {
    int retVal;
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "o %s %d", filename, mode);
    write(sockfd, buff, strlen(buff));
    bzero(buff, MAX_MSG);
    read(sockfd, buff, MAX_MSG);
    sscanf(buff, "%d", &retVal);
    return retVal;
}

int tfsClose(int fd) {
    int retVal;
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "x %d", fd);
    write(sockfd, buff, strlen(buff));
    bzero(buff, MAX_MSG);
    read(sockfd, buff, MAX_MSG);
    sscanf(buff, "%d", &retVal);
    return retVal;
}

int tfsRead(int fd, char* outBuff, int len) {
    int retVal;
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "l %d %d", fd, len);
    write(sockfd, buff, strlen(buff));
    bzero(buff, MAX_MSG);
    read(sockfd, buff, MAX_MSG);
    sscanf(buff, "%d", &retVal);
    if(retVal >= 0)
        sscanf(buff, "%d %s", &retVal, outBuff);
    return retVal;
}

int tfsWrite(int fd, char* inBuff, int len) {
    int retVal;
    bzero(buff, MAX_MSG);
    snprintf(buff, MAX_MSG, "w %d %s %d", fd, inBuff, len);
    write(sockfd, buff, strlen(buff));
    bzero(buff, MAX_MSG);
    read(sockfd, buff, MAX_MSG);
    sscanf(buff, "%d", &retVal);
    return retVal;
}
