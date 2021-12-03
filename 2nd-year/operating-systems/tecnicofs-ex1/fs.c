/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#include "fs.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

void initLockFS(tecnicofs* fs) {
	#ifdef MUTEX
		if(pthread_mutex_init(&fs->mutex, NULL)) {
			fprintf(stderr, "Error: could not initialize mutex\n");
            exit(EXIT_FAILURE);
		}
	#elif RWLOCK
		if(pthread_rwlock_init(&fs->rwlock, NULL)) {
			fprintf(stderr, "Error: could not initialize rwlock\n");
            exit(EXIT_FAILURE);
		}
	#endif
}

void lockFSWrite(tecnicofs* fs) {
	#ifdef MUTEX
		if(pthread_mutex_lock(&fs->mutex)) {
			fprintf(stderr, "Error: could not lock mutex\n");
            exit(EXIT_FAILURE);
		}
	#elif RWLOCK
		if(pthread_rwlock_wrlock(&fs->rwlock)) {
			fprintf(stderr, "Error: could not writelock rwlock\n");
            exit(EXIT_FAILURE);
		}
	#endif
}

void lockFSRead(tecnicofs* fs) {
	#ifdef MUTEX
		if(pthread_mutex_lock(&fs->mutex)) {
			fprintf(stderr, "Error: could not lock mutex\n");
            exit(EXIT_FAILURE);
		}
	#elif RWLOCK
		if(pthread_rwlock_rdlock(&fs->rwlock)) {
			fprintf(stderr, "Error: could not readlock rwlock\n");
            exit(EXIT_FAILURE);
		}
	#endif
}

void unlockFS(tecnicofs* fs) {
	#ifdef MUTEX
		if(pthread_mutex_unlock(&fs->mutex)) {
			fprintf(stderr, "Error: could not unlock mutex\n");
            exit(EXIT_FAILURE);
		}
	#elif RWLOCK
		if(pthread_rwlock_unlock(&fs->rwlock)) {
			fprintf(stderr, "Error: could not unlock rwlock\n");
            exit(EXIT_FAILURE);
		}
	#endif
}

void destroyLockFS(tecnicofs* fs) {
	#ifdef MUTEX
		if(pthread_mutex_destroy(&fs->mutex)) {
			fprintf(stderr, "Error: could not destroy mutex\n");
            exit(EXIT_FAILURE);
		}
	#elif RWLOCK
		if(pthread_rwlock_destroy(&fs->rwlock)) {
			fprintf(stderr, "Error: could not destroy rwlock\n");
            exit(EXIT_FAILURE);
		}
	#endif
}

int obtainNewInumber(tecnicofs* fs) {
	lockFSWrite(fs);
	int newInumber = ++(fs->nextINumber);
	unlockFS(fs);
	return newInumber;
}

tecnicofs* new_tecnicofs(){
	tecnicofs*fs = malloc(sizeof(tecnicofs));
	if (!fs) {
		perror("failed to allocate tecnicofs");
		exit(EXIT_FAILURE);
	}
	fs->bstRoot = NULL;
	fs->nextINumber = 0;
	initLockFS(fs);
	return fs;
}

void free_tecnicofs(tecnicofs* fs){
	destroyLockFS(fs);
	free_tree(fs->bstRoot);
	free(fs);
}

void create(tecnicofs* fs, char *name, int inumber){
	lockFSWrite(fs);
	fs->bstRoot = insert(fs->bstRoot, name, inumber);
	unlockFS(fs);
}

void delete(tecnicofs* fs, char *name){
	lockFSWrite(fs);
	fs->bstRoot = remove_item(fs->bstRoot, name);
	unlockFS(fs);
}

int lookup(tecnicofs* fs, char *name){
	lockFSRead(fs);
	node* searchNode = search(fs->bstRoot, name);
	unlockFS(fs);
	if (searchNode) return searchNode->inumber;
	return 0;
}

void print_tecnicofs_tree(char *outputFileName, tecnicofs *fs){
	FILE *outputFile;
	outputFile = fopen(outputFileName, "w");
	print_tree(outputFile, fs->bstRoot);
	fclose(outputFile);
}
