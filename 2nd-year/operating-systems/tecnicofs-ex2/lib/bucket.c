/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#include "bucket.h"
#include "../sync.h"
#include <stdlib.h>

bucket* new_bucket() {
    bucket* new_bucket = (bucket*) malloc(sizeof(bucket));
    if (!new_bucket) {
		perror("failed to allocate bucket");
		exit(EXIT_FAILURE);
	}
    new_bucket->root = NULL;
    sync_init(&(new_bucket->bucketLock));
    return new_bucket;
}

void free_bucket(bucket* bucket) {
    free_tree(bucket->root);
	sync_destroy(&(bucket->bucketLock));
	free(bucket);
}
