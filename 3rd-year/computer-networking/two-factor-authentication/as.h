#ifndef AS_H
#define AS_H

#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h> //memset
#include <stdio.h>
#include <signal.h>
#include <time.h>
#include "as_storage.h"

#define FALSE 0
#define TRUE 1
#define BUFF_SIZE 256
#define MAX_CLIENTS 256

// AS info
int verbose = FALSE;
char asip[128]; //localhost
char asport[6]; // 58035 by default
// communications
int udp_fd, tcp_fd;
struct sockaddr_in callback_udp;
int fdlist[MAX_CLIENTS] = { [0 ... 255] = -1 };
int uidlist[MAX_CLIENTS] = { [0 ... 255] = -1 }; // Used to logout users

// Aux functions
void panic(char* message, int errcode);
void usage(int errocde);
void parseArgs(int argc, char* argv[]);

// Server functions
void initServer();
void closeServer();
void receiveUDP(char* dest);
void replyUDP(char* message);
int sendVlcUDP(int uid, int vc, char fop, char* fname);
void processUDP(char* message);
void setUID(int uid, int fd);
int getUID(int fd);
void addSocket(int fd);
void removeSocket(int fd);
void sendTCP(int fd, char* message);
char* receiveTCP(int fd);

#endif // AS_H