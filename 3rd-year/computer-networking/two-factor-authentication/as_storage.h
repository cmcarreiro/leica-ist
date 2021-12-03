#ifndef AS_STORAGE
#define AS_STORAGE

#define _XOPEN_SOURCE 500
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ftw.h>
#include <sys/stat.h>
#include <errno.h>

#define OKAY 0
#define ERROR -1
#define WRONG_PASSWORD -2
#define ALREADY_EXISTS -3
#define USER_NOT_REGISTERED -4
#define NO_TID -5
#define NO_RID -6
#define NO_USER -7
#define NO_PD -8
#define PATHSIZE 128
// TODO FIX FILEZISE?
#define FILESIZE 1024
#define USER_DIR "USERS_AS"

// Aux functions
struct FTW;
int deleteFolder(char* path);
int deleteFolder_aux(const char* fpath, const struct stat* sb, int typeflag, struct FTW* ftwbuf);

int initializeStorage();
int regUser(int uid, char* password);
int unregUser(int uid);
int regPD(int uid, char* ip, char* port);
int getPD(int uid, char* dest_ip, char* dest_port);
void unregPD(int uid);
int checkPassword(int uid, char* password);
int loginUser(int uid, char* password);
void logoutUser(int uid);
int regRID(int uid, int rid, int vc, char fop, char* fname);
int getRID(int uid, int rid, char* fop_dest, char* fname_dest);
void unregRID(int uid, int rid);
int regTID(int uid, int tid, char fop, char* fname);
int getTID(int uid, int tid, char* fop_dest, char* fname_dest);
void unregTID(int uid, int tid);

#endif // AS_STORAGE
