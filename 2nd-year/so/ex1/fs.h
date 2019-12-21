/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#ifndef FS_H
#define FS_H
#include "lib/bst.h"
#include <pthread.h>

typedef struct tecnicofs {
    node* bstRoot;
    int nextINumber;
    #ifdef MUTEX
        pthread_mutex_t mutex;
    #elif RWLOCK
        pthread_rwlock_t rwlock;
    #endif
} tecnicofs;

void initLockFS(tecnicofs* fs);
void lockFSWrite(tecnicofs* fs);
void lockFSRead(tecnicofs* fs);
void unlockFS(tecnicofs* fs);
void destroyLockFS(tecnicofs* fs);

int obtainNewInumber(tecnicofs* fs);
tecnicofs* new_tecnicofs();
void free_tecnicofs(tecnicofs* fs);
void create(tecnicofs* fs, char *name, int inumber);
void delete(tecnicofs* fs, char *name);
int lookup(tecnicofs* fs, char *name);
void print_tecnicofs_tree(char *outputFileName, tecnicofs *fs); //FILE * fp

#endif /* FS_H */
