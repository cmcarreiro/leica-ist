/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#ifndef FS_H
#define FS_H
#include "lib/bst.h"
#include "lib/bucket.h"
#include "sync.h"

typedef struct tecnicofs {
    int nextINumber;
    int numBuckets;
    bucket* hashTable[];
} tecnicofs;

int obtainNewInumber(tecnicofs* fs);
tecnicofs* new_tecnicofs(int numBuckets);
void free_tecnicofs(tecnicofs* fs);
void call_create(tecnicofs* fs, char* name, int inumber);
void call_delete(tecnicofs* fs, char* name);
int call_lookup(tecnicofs* fs, char* name);
void call_move(tecnicofs* fs, char* name1, char* name2);
void print_tecnicofs_trees(FILE* fp, tecnicofs* fs);

#endif /* FS_H */
