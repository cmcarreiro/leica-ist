#ifndef FS_STORAGE
#define FS_STORAGE

#define _XOPEN_SOURCE 500
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <dirent.h>
#include <ftw.h>
#include <unistd.h>
#include "file_contents.h"

#define OKAY 0
#define ERROR -1
#define NO_DIRECTORY -2
#define NO_FILES -3
#define PATHSIZE 512
#define USER_DIR "USERS_FS"

// Aux function
int deleteFolder(char* path);

int initializeStorage();
int createFileFS(int uid, char *fname, p_FileContents source);
int deleteFileFS(int uid, char *fname);
int getFileContentsFS(int uid, char *fname, p_FileContents dest);
int countFiles(int uid);
int listDir(int uid, char *dest_string);
int removeDir(int uid);
#endif // FS_STORAGE
