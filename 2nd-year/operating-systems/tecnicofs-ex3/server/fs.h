/*
Sistemas Operativos 19/20
Catarina Carreiro   92438
Cristiano Clemente  92440
*/

#ifndef FS_H
#define FS_H
#include "lib/bst.h"
#include "lib/sync.h"
#include "../tecnicofs-api-constants.h"

#define FREE_INODE -1
#define INODE_TABLE_SIZE 50

typedef struct tecnicofs {
    node* bstRoot;
    int nextINumber;
    syncMech bstLock;
} tecnicofs;

typedef struct inode_t {
    uid_t owner;
    permission ownerPermissions;
    permission othersPermissions;
    char* fileContent;
} inode_t;

int obtainNewInumber(tecnicofs* fs);
tecnicofs* new_tecnicofs();
void free_tecnicofs(tecnicofs* fs);
int create_fs(tecnicofs* fs, char *name, uid_t owner, permission ownerPerm, permission othersPerm);
int delete_fs(tecnicofs* fs, char *name, uid_t owner);
int lookup_fs(tecnicofs* fs, char *name);
int change_name_fs(tecnicofs* fs, char *nameOrig, char* nameDest, uid_t owner);
int open_file_fs(tecnicofs* fs, char *name, permission openMode, uid_t client_uid, int numOpenFiles);
int read_file_fs(tecnicofs* fs, int inumber, char* readBuff, int len);
int write_file_fs(tecnicofs* fs, int fd, char* writeBuff, int len);
void print_tecnicofs_tree(FILE * fp, tecnicofs *fs);

#endif /* FS_H */
