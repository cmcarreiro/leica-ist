/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#include "fs.h"
#include "lib/bst.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "lib/sync.h"

inode_t inode_table[INODE_TABLE_SIZE];
pthread_mutex_t inode_table_lock;

//==============================================================================
// inode table functions
//==============================================================================
void lock_inode_table(){
    if(pthread_mutex_lock(&inode_table_lock) != 0){
        perror("Failed to acquire the i-node table lock.\n");
        exit(EXIT_FAILURE);
    }
}

void unlock_inode_table(){
    if(pthread_mutex_unlock(&inode_table_lock) != 0){
        perror("Failed to release the i-node table lock.\n");
        exit(EXIT_FAILURE);
    }
}

/*
 * Initializes the i-nodes table and the mutex.
 */
void inode_table_init(){
    if(pthread_mutex_init(&inode_table_lock, NULL) != 0){
        perror("Failed to initialize inode table mutex.\n");
        exit(EXIT_FAILURE);
    }
    for(int i = 0; i < INODE_TABLE_SIZE; i++){
        inode_table[i].owner = FREE_INODE;
        inode_table[i].fileContent = NULL;
    }
}

/*
 * Releases the allocated memory for the i-nodes tables
 * and destroys the mutex.
 */
void inode_table_destroy(){
    for(int i = 0; i < INODE_TABLE_SIZE; i++){
        if(inode_table[i].owner!=FREE_INODE && inode_table[i].fileContent)
            free(inode_table[i].fileContent);
    }

    if(pthread_mutex_destroy(&inode_table_lock) != 0){
        perror("Failed to destroy inode table mutex.\n");
        exit(EXIT_FAILURE);
    }
}

/*
 * Creates a new i-node in the table with the given information.
 * Input:
 *  - owner: uid of the user that created the file
 *  - ownerPerm: permissions of the owner
 *  - othersPerm: permissions of all other users
 * Returns:
 *  inumber: identifier of the new i-node, if successfully created
 *       -1: if an error occurs
 */
int inode_create(uid_t owner, permission ownerPerm, permission othersPerm){
    lock_inode_table();
    for(int inumber = 0; inumber < INODE_TABLE_SIZE; inumber++){
        if(inode_table[inumber].owner == FREE_INODE){
            inode_table[inumber].owner = owner;
            inode_table[inumber].ownerPermissions = ownerPerm;
            inode_table[inumber].othersPermissions = othersPerm;
            inode_table[inumber].fileContent = NULL;
            unlock_inode_table();
            return inumber;
        }
    }
    unlock_inode_table();
    return -1;
}

/*
 * Deletes the i-node.
 * Input:
 *  - inumber: identifier of the i-node
 * Returns:
 *   0: if successful
 *  -1: if an error occurs
 */
int inode_delete(int inumber){
    lock_inode_table();
    if((inumber < 0) || (inumber > INODE_TABLE_SIZE) || (inode_table[inumber].owner == FREE_INODE)){
        printf("inode_delete: invalid inumber");
        unlock_inode_table();
        return -1;
    }

    inode_table[inumber].owner = FREE_INODE;
    if(inode_table[inumber].fileContent){
        free(inode_table[inumber].fileContent);
    }
    unlock_inode_table();
    return 0;
}

/*
 * Copies the contents of the i-node into the arguments.
 * Only the fields referenced by non-null arguments are copied.
 * Input:
 *  - inumber: identifier of the i-node
 *  - owner: pointer to uid_t
 *  - ownerPerm: pointer to permission
 *  - othersPerm: pointer to permission
 *  - fileContent: pointer to a char array with size >= len
 * Returns:
 *    len of content read:if successful
 *   -1: if an error occurs
 */
int inode_get(int inumber, uid_t *owner, permission *ownerPerm, permission *othersPerm, char* fileContents, int len){
    lock_inode_table();
    if((inumber < 0) || (inumber > INODE_TABLE_SIZE) || (inode_table[inumber].owner == FREE_INODE)){
        //printf("inode_getValues: invalid inumber %d\n", inumber); //returns error code instead of printing
        unlock_inode_table();
        return TECNICOFS_ERROR_FILE_NOT_FOUND;
    }

    if(len < 0){
        printf("inode_getValues: invalid len %d\n", len);
        unlock_inode_table();
        return -1;
    }

    if(owner)
        *owner = inode_table[inumber].owner;

    if(ownerPerm)
        *ownerPerm = inode_table[inumber].ownerPermissions;

    if(othersPerm)
        *othersPerm = inode_table[inumber].othersPermissions;

    if(fileContents && len > 0 && inode_table[inumber].fileContent){
        //if(len > ((int) strlen(inode_table[inumber].fileContent)))
        //    len = ((int) strlen(inode_table[inumber].fileContent) + 1);
        strncpy(fileContents, inode_table[inumber].fileContent, len-1); //changed to len-1
        fileContents[len-1] = '\0'; //changed to len-1
        unlock_inode_table();
        return strlen(fileContents);
    }

    unlock_inode_table();
    return 0;
}

/*
 * Updates the i-node file content.
 * Input:
 *  - inumber: identifier of the i-node
 *  - fileContent: pointer to the string with size >= len
 *  - len: length to copy
 * Returns:
 *    0:if successful
 *   -1: if an error occurs
 */
int inode_set(int inumber, char *fileContents, int len){
    lock_inode_table();
    if((inumber < 0) || (inumber > INODE_TABLE_SIZE) || (inode_table[inumber].owner == FREE_INODE)){
        //printf("inode_setFileContent: invalid inumber"); //returns error code instead of printing
        unlock_inode_table();
        return TECNICOFS_ERROR_FILE_NOT_FOUND;
    }

    if(!fileContents || len < 0 || strlen(fileContents) < len){
        printf("inode_setFileContent: fileContents must be non-null && len > 0 && strlen(fileContents) > len");
        unlock_inode_table();
        return -1;
    }

    if(inode_table[inumber].fileContent)
        free(inode_table[inumber].fileContent);

    inode_table[inumber].fileContent = malloc(sizeof(char) * (len+1));
    strncpy(inode_table[inumber].fileContent, fileContents, len);
    inode_table[inumber].fileContent[len] = '\0';

    unlock_inode_table();
    return 0;
}
//==============================================================================


int obtainNewInumber(tecnicofs* fs) {
	int newInumber = ++(fs->nextINumber);
	return newInumber;
}

tecnicofs* new_tecnicofs(){
	tecnicofs*fs = malloc(sizeof(tecnicofs));
	if (!fs) {
		perror("failed to allocate tecnicofs\n");
		exit(EXIT_FAILURE);
	}
	fs->bstRoot = NULL;
	fs->nextINumber = 0;
	sync_init(&(fs->bstLock));
	inode_table_init();
	return fs;
}

void free_tecnicofs(tecnicofs* fs){
	free_tree(fs->bstRoot);
	sync_destroy(&(fs->bstLock));
    inode_table_destroy();
	free(fs);
}

int create_fs(tecnicofs* fs, char *name, uid_t owner, permission ownerPerm, permission othersPerm) {
    int inumber;
    if(lookup_fs(fs, name) != -1)
        return TECNICOFS_ERROR_FILE_ALREADY_EXISTS;
    if((inumber = inode_create(owner, ownerPerm, othersPerm)) < 0)
        return -1;
    sync_wrlock(&(fs->bstLock));
    fs->bstRoot = insert(fs->bstRoot, name, inumber);
	sync_unlock(&(fs->bstLock));
    return 0;
}

int delete_fs(tecnicofs* fs, char *name, uid_t owner){
    int inumber;
    uid_t file_owner;
    if((inumber = lookup_fs(fs, name)) < 0)
        return TECNICOFS_ERROR_FILE_NOT_FOUND;
    inode_get(inumber, &file_owner, NULL, NULL, NULL, 0);
    if(owner != file_owner)
        return TECNICOFS_ERROR_PERMISSION_DENIED;
    sync_wrlock(&(fs->bstLock));
    fs->bstRoot = remove_item(fs->bstRoot, name);
    sync_unlock(&(fs->bstLock));
    if(inode_delete(inumber) < 0)
        return -1;
    //print_tree(stdout, fs->bstRoot); //***
    return 0;
}

int lookup_fs(tecnicofs* fs, char *name/*, uid_t* owner, permission* ownerPerm, permission* othersPerm, char* fileContents*/) {
	sync_rdlock(&(fs->bstLock));
	int inumber = -1;
	node* searchNode = search(fs->bstRoot, name);
	if(searchNode) {
		inumber = searchNode->inumber;
	}
	sync_unlock(&(fs->bstLock));
	return inumber;
}

int change_name_fs(tecnicofs* fs, char *nameOrig, char* nameDest, uid_t owner) {
    int inumber;
    uid_t file_owner;
    if((inumber = lookup_fs(fs, nameOrig)) < 0)
        return TECNICOFS_ERROR_FILE_NOT_FOUND;
    inode_get(inumber, &file_owner, NULL, NULL, NULL, 0);
    if(owner != file_owner)
        return TECNICOFS_ERROR_PERMISSION_DENIED;
    if((lookup_fs(fs, nameDest)) != -1)
        return TECNICOFS_ERROR_FILE_ALREADY_EXISTS;
    sync_wrlock(&(fs->bstLock));
    fs->bstRoot = remove_item(fs->bstRoot, nameOrig);
    fs->bstRoot = insert(fs->bstRoot, nameDest, inumber);
    sync_unlock(&(fs->bstLock));
    return 0;
}

int open_file_fs(tecnicofs* fs, char *name, permission openMode, uid_t client_uid, int numOpenFiles) {
    int inumber = -1;
    uid_t owner_uid;
    permission ownerPerm, othersPerm;
    if((inumber = lookup_fs(fs, name)) < 0)
        return TECNICOFS_ERROR_FILE_NOT_FOUND;
    inode_get(inumber, &owner_uid, &ownerPerm, &othersPerm, NULL, 0);
    if(client_uid == owner_uid) {
        if(openMode > ownerPerm)
            return TECNICOFS_ERROR_PERMISSION_DENIED;
    } else {
        if(openMode > othersPerm)
            return TECNICOFS_ERROR_PERMISSION_DENIED;
    }
    if(numOpenFiles == MAX_FDS)
        return TECNICOFS_ERROR_MAXED_OPEN_FILES;
    return inumber;
}

int read_file_fs(tecnicofs* fs, int inumber, char* readBuff, int len) {
    return inode_get(inumber, NULL, NULL, NULL, readBuff, len);
}

int write_file_fs(tecnicofs* fs, int inumber, char* writeBuff, int len) {
    int retVal;
    retVal = inode_set(inumber, writeBuff, len);
    return retVal;
}

void print_tecnicofs_tree(FILE * fp, tecnicofs *fs){
	print_tree(fp, fs->bstRoot);
}
