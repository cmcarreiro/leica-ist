/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#include "fs.h"
#include "lib/bst.h"
#include "lib/bucket.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "sync.h"

// Returns 1st hashcode according to sorting criteria
int first_hc(int hashCode1, int hashCode2) {
    return hashCode1<hashCode2 ? hashCode1:hashCode2;
}

// Returns 2nd hashcode according to sorting criteria
int last_hc(int hashCode1, int hashCode2) {
    return hashCode1<hashCode2 ? hashCode2:hashCode1;
}

/* Simple hash function for strings.
 * Receives a string and resturns its hash value
 * which is a number between 0 and n-1
 * In case the string is null, returns -1 */
int hash(char* name, int n) {
	if (!name)
		return -1;
	return (int) name[0] % n;
}


int obtainNewInumber(tecnicofs* fs) {
	int newInumber = ++(fs->nextINumber);
	return newInumber;
}

tecnicofs* new_tecnicofs(int numBuckets) {
	int i;
	tecnicofs* fs = (tecnicofs*) malloc(sizeof(tecnicofs) + numBuckets*sizeof(bucket*));
	if (!fs) {
		perror("failed to allocate tecnicofs");
		exit(EXIT_FAILURE);
	}
	for(i=0; i<numBuckets; i++)
		fs->hashTable[i] = new_bucket();
	fs->nextINumber = 0;
	fs->numBuckets = numBuckets;
	return fs;
}

void free_tecnicofs(tecnicofs* fs) {
	int i;
	for(i=0; i<(fs->numBuckets); i++)
		free_bucket(fs->hashTable[i]);
	free(fs);
}

void create(tecnicofs* fs, char *name, int inumber, int hashCode) {
    fs->hashTable[hashCode]->root = insert(fs->hashTable[hashCode]->root, name, inumber);
}

void call_create(tecnicofs* fs, char *name, int inumber) {
	int hashCode = hash(name, fs->numBuckets);
	sync_wrlock(&(fs->hashTable[hashCode]->bucketLock));
    create(fs, name, inumber, hashCode);
	sync_unlock(&(fs->hashTable[hashCode]->bucketLock));
}

void delete(tecnicofs* fs, char *name, int hashCode) {
    fs->hashTable[hashCode]->root = remove_item(fs->hashTable[hashCode]->root, name);
}

void call_delete(tecnicofs* fs, char *name) {
	int hashCode = hash(name, fs->numBuckets);
	sync_wrlock(&(fs->hashTable[hashCode]->bucketLock));
    delete(fs, name, hashCode);
	sync_unlock(&(fs->hashTable[hashCode]->bucketLock));
}

int lookup(tecnicofs* fs, char* name, int hashCode) {
    int inumber = 0;
	node* searchNode = search(fs->hashTable[hashCode]->root, name);
	if (searchNode) {
		inumber = searchNode->inumber;
	}
    return inumber;
}

int call_lookup(tecnicofs* fs, char* name) {
	int hashCode = hash(name, fs->numBuckets);
    int inumber = 0;
	sync_rdlock(&(fs->hashTable[hashCode]->bucketLock));
    inumber = lookup(fs, name, hashCode);
	sync_unlock(&(fs->hashTable[hashCode]->bucketLock));
	return inumber;
}

void call_move(tecnicofs* fs, char* name1, char* name2) {
	int hashCode1 = hash(name1, fs->numBuckets);
	int hashCode2 = hash(name2, fs->numBuckets);

	if(hashCode1 == hashCode2) {
        sync_wrlock(&(fs->hashTable[hashCode1]->bucketLock));
        int inumber = 0;
		if((inumber=lookup(fs, name1, hashCode1))
            && !lookup(fs, name2, hashCode2)) {
			delete(fs, name1, hashCode1);
			create(fs, name2, inumber, hashCode2);
        }
        sync_unlock(&(fs->hashTable[hashCode1]->bucketLock));
        return;
	}

	int first = first_hc(hashCode1, hashCode2);
	int last = last_hc(hashCode1, hashCode2);

	sync_wrlock(&(fs->hashTable[first]->bucketLock));
	sync_wrlock(&(fs->hashTable[last]->bucketLock));

	int inumber = 0;
    if((inumber=lookup(fs, name1, hashCode1))
        && !lookup(fs, name2, hashCode2)) {
        delete(fs, name1, hashCode1);
        create(fs, name2, inumber, hashCode2);
    }
    sync_unlock(&(fs->hashTable[last]->bucketLock));
	sync_unlock(&(fs->hashTable[first]->bucketLock));
    return;
}

void print_tecnicofs_trees(FILE* fp, tecnicofs* fs) {
	int i;
	for(i=0; i<(fs->numBuckets); i++)
		if(fs->hashTable[i]->root != NULL)
			print_tree(fp, fs->hashTable[i]->root);
}
