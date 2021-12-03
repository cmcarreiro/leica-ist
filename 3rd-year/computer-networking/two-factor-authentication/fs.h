#ifndef FS_H
#define FS_H

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <signal.h>
#include "fs_storage.h"
#include "file_contents.h"

#define MAX_CLIENTS 256
#define MESSAGESIZE 128
#define MSG -1 //Used to flag a message as not a file
#define TRUE 1
#define FALSE 0
#define VALID 0
#define INVALID -1

int verbose = FALSE;
int as_fd;
struct sockaddr as_addr;
char asip[128];
char asport[6];
int fs_fd;
struct sockaddr fs_addr;
char fsport[6];
int fdlist[MAX_CLIENTS] = {[0 ... 255] = -1};

// Aux functions
void panic(char *message, int errcode);
void usage(int errocde);
void parseArgs(int argc, char* argv[]);

// UDP (with AS)
void startConnectionAS();
int validateOperation(int uid, int tid, char fop, char *fname);
void closeConnectionAS();

// TCP (with User)
void startConnectionTCP();
void addSocket(int fd);
void removeSocket(int fd);
void sendTCP(int fd, char *message, long messagelen);
void sendFile(int fd, p_FileContents file);
char *receiveTCP(int fd);
char* receiveFile(int fd, char* sofar, int received_sofar);
void closeConnectionTCP();

// FS functions
void closeServer();
void requestList(int fd, int uid, int tid);
void retrieveFile(int fd, int uid, int tid, char *fname);
void uploadFile(int fd, int uid, int tid, char *fname, p_FileContents contents);
void removeFile(int fd, int uid, int tid, char *fname);
void removeUser(int fd, int uid, int tid);

#endif // FS_H
