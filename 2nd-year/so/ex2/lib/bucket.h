/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#ifndef BUCKET_H
#define BUCKET_H

#include "../sync.h"
#include "bst.h"

typedef struct bucket {
    node* root;
    syncMech bucketLock;
} bucket;

bucket* new_bucket();
void free_bucket(bucket* bucket);

#endif
