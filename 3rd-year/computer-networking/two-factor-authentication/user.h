#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include "file_contents.h"

#define MSG -1

struct addrinfo* res;
char buffer[1024];
// AS info
int as_fd = -1;
char asip[128];
char asport[6];
// FS info
int fs_fd = -1;
char fsip[128];
char fsport[6];
// User info
int uid;
char pass[9];
char RETRIEVE_FNAME[25]; // used to store the name of the file to save with retrieve
int tid; // used to store the last tid received

// Aux functions
void panic(char* message, int errcode);
void usage(int errocde);
void parseArgs(int argc, char* argv[]);
// Connection functions
void connectAS();
void closeAS();
void connectFS();
void closeFS();
void sendMessage(int fd, char* message, long messagelen);
void sendFile(int fd, int tid, char* fname, p_FileContents file);
char* receiveMessage(int fd);
char* receiveFile(int fd, char* sofar, int received_sofar);
// Processing
void processMessage(char* message);
void processInput();