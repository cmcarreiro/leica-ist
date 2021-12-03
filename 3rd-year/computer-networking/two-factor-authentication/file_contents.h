#ifndef FILE_CONTENTS_H
#define FILE_CONTENTS_H

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <ftw.h>
#include <sys/stat.h>
#include <errno.h>

#define OKAY 0
#define ERROR -1
#define FILE_DOESNT_EXIST -20
#define FILE_EXISTS -30

//Aux functions
int existsFile(char *path);
// Dealing with file bytes
typedef struct FileContents{
    char* byte_content;
    long int byte_size;
} *p_FileContents;
p_FileContents newFileContents();
int setupFileContents(p_FileContents pfc, long size);
void freeFileContents(p_FileContents pfc);
// File management
int getFileContents(char *path, p_FileContents dest);
int createFile(char *path, p_FileContents source);
int deleteFile(char *path);

#endif // FILE_CONTENTS_H
