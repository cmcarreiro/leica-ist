#include "as_storage.h"

int deleteFolder(char* path) {
	char command[PATHSIZE];
	sprintf(command, "rm -rf %s", path);
	system(command);
}

int initializeStorage() {
	int errcode;
	errcode = mkdir(USER_DIR, S_IRWXU);
	if (errcode == -1 && errno != EEXIST)
		return ERROR;
	return OKAY;
}

int regUser(int uid, char* password) {
	int errcode;
	char path[PATHSIZE];
	FILE* user_file;
	// Attempt to create a directory for that user
	sprintf(path, "./%s", USER_DIR);
	errcode = mkdir(path, S_IRWXU);
	if (errcode == -1 && errno != EEXIST)
		return ERROR;
	sprintf(path, "./%s/%05d", USER_DIR, uid);
	errcode = mkdir(path, S_IRWXU);
	if (errcode == -1 && errno != EEXIST)
		return ERROR;
	// Check if an user already exists. If so, compare passwords.
	sprintf(path, "./%s/%05d/%05d_pass.txt", USER_DIR, uid, uid);
	if (access(path, F_OK) != -1) {
		return checkPassword(uid, password);
	}
	user_file = fopen(path, "w+");
	if (!user_file)
		return ERROR;
	fprintf(user_file, "%s\n", password);
	fclose(user_file);
	return OKAY;
}

int unregUser(int uid) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d", USER_DIR, uid);
	return deleteFolder(path);
}

int regPD(int uid, char* ip, char* port) {
	char path[PATHSIZE];
	FILE* pd_file;
	sprintf(path, "./%s/%05d/%05d_reg.txt", USER_DIR, uid, uid);
	if (access(path, F_OK) != -1)
		return ALREADY_EXISTS;
	pd_file = fopen(path, "w+");
	if (!pd_file)
		return ERROR;
	fprintf(pd_file, "%s\n%s\n", ip, port);
	fclose(pd_file);
	return OKAY;
}

int getPD(int uid, char* dest_ip, char* dest_port) {
	char path[PATHSIZE];
	char file_contents[FILESIZE];
	FILE* pd_file;
	sprintf(path, "./%s/%05d/%05d_reg.txt", USER_DIR, uid, uid);
	if (access(path, F_OK) == -1)
		return NO_PD;
	pd_file = fopen(path, "r");
	if (!pd_file)
		return ERROR;
	fscanf(pd_file, "%s\n%s\n", dest_ip, dest_port);
	fclose(pd_file);
	return OKAY;
}

void unregPD(int uid) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d/%05d_reg.txt", USER_DIR, uid, uid);
	if (access(path, F_OK) != -1)
		remove(path);
}

int loginUser(int uid, char* password) {
	char path[PATHSIZE];
	char stored_password[9];
	FILE* open_file;
	sprintf(path, "./%s/%05d/%05d_login.txt", USER_DIR, uid, uid);
	if (access(path, F_OK) != -1)
		return ALREADY_EXISTS;
	int status = checkPassword(uid, password);
	if (status != OKAY)
		return status;
	sprintf(path, "./%s/%05d/%05d_login.txt", USER_DIR, uid, uid);
	// Creating an empty file
	open_file = fopen(path, "w+");
	if (!open_file)
		return ERROR;
	fclose(open_file);
	return OKAY;
}

int checkPassword(int uid, char* password) {
	char path[PATHSIZE];
	char stored_password[9];
	FILE* open_file;
	sprintf(path, "./%s/%05d/%05d_pass.txt", USER_DIR, uid, uid);
	if (access(path, F_OK) == -1)
		return USER_NOT_REGISTERED;
	open_file = fopen(path, "r");
	if (!open_file)
		return ERROR;
	fgets(stored_password, 9, open_file);
	fclose(open_file);
	if (strcmp(stored_password, password))
		return WRONG_PASSWORD;
	return OKAY;
}

void logoutUser(int uid) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d/%05d_login.txt", USER_DIR, uid, uid);
	if (access(path, F_OK) != -1)
		remove(path);
}

int regRID(int uid, int rid, int vc, char fop, char* fname) {
	char path[PATHSIZE];
	char content[64];
	FILE* rid_file;
	memset(path, '\0', PATHSIZE);
	sprintf(path, "./%s/%05d/%05d_rid_%04d.txt", USER_DIR, uid, uid, rid);
	if (access(path, F_OK) != -1)
		return ALREADY_EXISTS;
	if(fname[0] != '\0') {
		sprintf(content, "%04d\n%c\n%s\n", vc, fop, fname);
	}
	else
		sprintf(content, "%04d\n%c\n", vc, fop);
	rid_file = fopen(path, "w+");
	if (!rid_file)
		return ERROR;
	fwrite(content, 1, strlen(content), rid_file);
	fclose(rid_file);
	return OKAY;
}

int getRID(int uid, int rid, char *fop_dest, char *fname_dest) {
	char path[PATHSIZE];
	int result;
	FILE* rid_file;
	sprintf(path, "./%s/%05d/%05d_rid_%04d.txt", USER_DIR, uid, uid, rid);
	if (access(path, F_OK) == -1)
		return NO_RID;
	rid_file = fopen(path, "r");
	if (!rid_file)
		return ERROR;
	int status = fscanf(rid_file, "%d\n%s\n%s", &result, fop_dest, fname_dest);
	fclose(rid_file);
	if(status == 2)
		memset(fname_dest, '\0', sizeof(fname_dest));
	return result;
}

void unregRID(int uid, int rid) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d/%05d_rid_%04d.txt", USER_DIR, uid, uid, rid);
	if (access(path, F_OK) != -1)
		remove(path);
}

int regTID(int uid, int tid, char fop, char* fname) {
	char path[PATHSIZE];
	char content[32];
	FILE* tid_file;
	sprintf(path, "./%s/%05d/%05d_tid_%04d.txt", USER_DIR, uid, uid, tid);
	if (access(path, F_OK) != -1)
		return ALREADY_EXISTS;
	if (fname[0] != '\0')
		sprintf(content, "%c\n%s\n", fop, fname);
	else
		sprintf(content, "%c\n", fop);
	tid_file = fopen(path, "w+");
	if (!tid_file)
		return ERROR;
	fwrite(content, 1, strlen(content), tid_file);
	fclose(tid_file);
	return OKAY;
}

int getTID(int uid, int tid, char* fop_dest, char* fname_dest) {
	char path[PATHSIZE];
	FILE* tid_file;
	sprintf(path, "./%s/%05d/%05d_tid_%04d.txt", USER_DIR, uid, uid, tid);
	if (access(path, F_OK) == -1)
		return NO_TID;
	tid_file = fopen(path, "r");
	if (!tid_file)
		return ERROR;
	int status = fscanf(tid_file, "%s\n%s\n", fop_dest, fname_dest);
	fclose(tid_file);
	if (status == 1)
		memset(fname_dest, '\0', sizeof(fname_dest));
	return OKAY;
}

void unregTID(int uid, int tid) {
	char path[PATHSIZE];
	sprintf(path, "./%s/%05d/%05d_tid_%04d.txt", USER_DIR, uid, uid, tid);
	if (access(path, F_OK) != -1)
		remove(path);
}
