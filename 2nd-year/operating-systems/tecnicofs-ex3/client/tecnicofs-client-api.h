/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#ifndef TECNICOFS_CLIENT_API_H
#define TECNICOFS_CLIENT_API_H

#define MAX_MSG 50

#include "../tecnicofs-api-constants.h"


int tfsCreate(char* filename, permission ownerPermissions, permission othersPermissions);
int tfsDelete(char* filename);
int tfsRename(char* filenameOld, char *filenameNew);
int tfsOpen(char* filename, permission mode);
int tfsClose(int fd);
int tfsRead(int fd, char* buffer, int len);
int tfsWrite(int fd, char* buffer, int len);
int tfsMount(char* address);
int tfsUnmount();

#endif /* TECNICOFS_CLIENT_API_H */
