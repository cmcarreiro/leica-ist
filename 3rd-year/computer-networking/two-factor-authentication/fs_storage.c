#include "fs_storage.h"

int deleteFolder(char* path) {
	char command[PATHSIZE];
	sprintf(command, "rm -rf %s", path);
	system(command);
}

int initializeStorage() {
	int errcode;
	strcpy(USER_DIR, "USERS_FS");
	errcode = mkdir(USER_DIR, S_IRWXU);
	if(errcode == -1 && errno != EEXIST)
		return ERROR;
	return OKAY;
}
int createFileFS(int uid, char *fname, p_FileContents source) {
	char path[PATHSIZE];
	int errcode;
	// If the directory already exists, this fails, which is fine.
	sprintf(path, "./%s/%05d", USER_DIR, uid);
	errcode = mkdir(path, S_IRWXU);
	if(errcode == -1 && errno != EEXIST)
		return ERROR;
	sprintf(path, "./%s/%05d/%s", USER_DIR, uid, fname);
	return createFile(path, source);
}
int deleteFileFS(int uid, char *fname) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d/", USER_DIR, uid);
	DIR *dir = opendir(path);
	if(dir)
		closedir(dir);
	else if(ENOENT == errno)
		return NO_DIRECTORY;
	else
		return ERROR;
	sprintf(path, "./%s/%05d/%s", USER_DIR, uid, fname);
	return deleteFile(path);
}
int getFileContentsFS(int uid, char *fname, p_FileContents dest) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d/", USER_DIR, uid);
	DIR *dir = opendir(path);
	if(dir)
		closedir(dir);
	else if(ENOENT == errno)
		return NO_DIRECTORY;
	else
		return ERROR;
	sprintf(path, "./%s/%05d/%s", USER_DIR, uid, fname);
	return getFileContents(path, dest);
}

int countFiles(int uid) {
	char path[PATHSIZE];
	int count;
	DIR *directory;
	struct dirent *dir;
	sprintf(path, "./%s/%05d", USER_DIR, uid);
	directory = opendir(path);
	if(directory) {
		while(dir = readdir(directory)) {
			if(!strcmp(dir->d_name, ".") || !strcmp(dir->d_name, ".."))
				continue;
			count++;
		}
		closedir(directory);
	}
	else
		return NO_DIRECTORY;
	return count;
}

int listDir(int uid, char *dest_string) {
	char path[PATHSIZE];
	char temp[2048] = "";
	char filepath[PATHSIZE];
	char fileinfo[PATHSIZE];
	FILE *file;
	long filesize;
	int num_files = 0;
	DIR *directory;
	struct dirent *dir;
	strcpy(dest_string, "");
	// Get the user folder path
	sprintf(path, "./%s/%05d", USER_DIR, uid);
	directory = opendir(path);
	if(directory) {
		while(dir = readdir(directory)) {
			if(!strcmp(dir->d_name, ".") || !strcmp(dir->d_name, ".."))
				continue;
			num_files++;
			// We need it in this format
			// filename filesize
			// So we need to figure out the size
			sprintf(filepath, "./%s/%s", path, dir->d_name);
			file = fopen(filepath, "r");
			if(file) {
				fseek(file, 0L, SEEK_END);
				filesize = ftell(file);
				fclose(file);
			} else
				filesize = -1;
			sprintf(fileinfo, " %s %ld", dir->d_name, filesize);
			strcat(temp, fileinfo);
		}
		closedir(directory);
	} else
		return NO_DIRECTORY;
	if(num_files == 0)
		return NO_FILES;
	sprintf(dest_string, "%d%s", num_files, temp);
	return OKAY;
}
int removeDir(int uid) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d", USER_DIR, uid);
	return deleteFolder(path);
}
