#include "file_contents.h"

int existsFile(char *path) {
	if(access(path, F_OK) != -1)
		return FILE_EXISTS;
	return FILE_DOESNT_EXIST;
}

p_FileContents newFileContents() {
	p_FileContents new = (p_FileContents) malloc(sizeof(struct FileContents));
	if(!new)
		return NULL;
	new->byte_content = NULL;
	new->byte_size = 0;
	return new;
}
int setupFileContents(p_FileContents pfc, long size) {
	pfc->byte_content = (char*) malloc(size * sizeof(char));
	if(!pfc->byte_content)
		return ERROR;
	pfc->byte_size = size;
	return OKAY;
}
void freeFileContents(p_FileContents pfc) {
	if(pfc->byte_content)
		free(pfc->byte_content);
	free(pfc);
}

int getFileContents(char* path, p_FileContents dest) {
	FILE *file;
	if (existsFile(path) == FILE_DOESNT_EXIST)
		return FILE_DOESNT_EXIST;
	file = fopen(path, "rb");
	// Get the length of the file
	fseek(file, 0, SEEK_END);
	if (setupFileContents(dest, ftell(file)) != OKAY) {
		fclose(file);
		return ERROR;
	}
	rewind(file);
	// Read that length
	fread(dest->byte_content, 1, dest->byte_size, file);
	fclose(file);
	return OKAY;
}

int createFile(char *path, p_FileContents source) {
	FILE *file;
	if (existsFile(path) == FILE_EXISTS)
		return FILE_EXISTS;
	file = fopen(path, "wb");
	if(!file)
		return ERROR;
	fwrite(source->byte_content, 1, source->byte_size, file);
	fclose(file);
	return OKAY;
}

int deleteFile(char *path) {
	if (existsFile(path) == FILE_DOESNT_EXIST)
		return FILE_DOESNT_EXIST;
	remove(path);
	return OKAY;
}
