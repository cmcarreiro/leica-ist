#ifndef PD_H
#define PD_H

#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>
#define BUFF_SIZE 512

// GLOBALS
int fd;
struct sockaddr as_addr, as_callback;
char buffer[BUFF_SIZE];
char pdip[128];
char pdport[6];
char asip[128];
char asport[6];
int uid = -1;
char pass[9];

// Aux functions
void panic(char* message, int errcode);
void usage(int errcode);
// Socket functions
void startConnection();
void sendMessage(char* message, struct sockaddr dest_addr);
void receiveMessage();
void closeConnection();
// Information parsing
void parseArgs(int argc, char* argv[]);
void processBuffer();

#endif // PD_H
