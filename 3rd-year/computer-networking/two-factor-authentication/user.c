#include "user.h"

void printArgs() {
	printf("---------- ARGS ----------\n");
	printf("ASIP: %s\n", asip);
	printf("    ASPORT: %s\n", asport);
	printf("FSIP: %s\n", fsip);
	printf("    FSPORT: %s\n", fsport);
	printf("--------------------------\n");
}

void panic(char* message, int errcode) {
	printf("ERROR: %s\n CODE: %d\n", message, errcode);
	closeAS();
	exit(errcode);
}

void usage(int errcode) {
	printf("USAGE: ./user [-n ASIP] [-p ASport] [-m FSIP] [-q FSport] \n");
	exit(errcode);
}

void parseArgs(int argc, char* argv[]) {
	int opt;
	// Set defaults
	strcpy(asip, "localhost");
	strcpy(asport, "58035");
	strcpy(fsip, "localhost");
	strcpy(fsport, "59035");
	while ((opt = getopt(argc, argv, "n:p:m:q:")) != -1) {
		// Checks if it's an opt followed by another opt
		switch (opt) {
		case 'n':
			if (*optarg == '-') usage(opt);
			strcpy(asip, optarg);
			break;
		case 'p':
			if (*optarg == '-') usage(opt);
			strcpy(asport, optarg);
			break;
		case 'm':
			if (*optarg == '-') usage(opt);
			strcpy(fsip, optarg);
			break;
		case 'q':
			if (*optarg == '-') usage(opt);
			strcpy(fsport, optarg);
			break;
		case '?':
			usage(opt);
			break;
		}
	}
}

void connectAS() {
	int errcode;
	struct addrinfo hints, * addr;
	as_fd = socket(AF_INET, SOCK_STREAM, 0);
	if (as_fd == -1)
		panic("Failed to create the AS socket.", as_fd);
	bzero(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	errcode = getaddrinfo(asip, asport, &hints, &addr);
	if (errcode != 0)
		panic("Failed to getaddrinfo for the AS.", errcode);
	errcode = connect(as_fd, addr->ai_addr, addr->ai_addrlen);
	if (errcode == -1)
		panic("Failed to establish a connection to the AS.", errcode);
	freeaddrinfo(addr);
}

void closeAS() {
	if (as_fd != -1) close(as_fd);
}

void connectFS() {
	int errcode;
	struct addrinfo hints, * addr;
	fs_fd = socket(AF_INET, SOCK_STREAM, 0);
	if (fs_fd == -1)
		panic("Failed to create the FS socket.", fs_fd);
	bzero(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	errcode = getaddrinfo(fsip, fsport, &hints, &addr);
	if (errcode != 0)
		panic("Failed to getaddrinfo for the FS.", errcode);
	errcode = connect(fs_fd, addr->ai_addr, addr->ai_addrlen);
	if (errcode == -1)
		panic("Failed to establish a connection to the FS.", errcode);
	freeaddrinfo(addr);
}

void closeFS() {
	if (fs_fd != -1) close(fs_fd);
}

void sendMessage(int fd, char* message, long messagelen) {
	//printf("[sendMessage] to %d: \"%s\"\n", fd, message);
	long sent = 0, written;
	if (messagelen == MSG)
		messagelen = strlen(message);
	while (sent < messagelen) {
		written = write(fd, &message[sent], messagelen - sent);
		if (written == -1)
			panic("Failed to send TCP message.", fd);
		sent += written;
	}
}

void sendFile(int fd, int tid, char* fname, p_FileContents file) {
	char start[64];
	sprintf(start, "UPL %05d %04d %s %ld ", uid, tid, fname, file->byte_size);
	int startlen = strlen(start);
	long messagelen = startlen + file->byte_size + 1; // final \n
	char* message = (char*)malloc(sizeof(char) * messagelen);
	strcpy(message, start);
	for (int i = startlen; i < messagelen - 1; i++)
		message[i] = file->byte_content[i - startlen];
	message[messagelen - 1] = '\n';
	sendMessage(fd, message, messagelen);
	free(message);
}

char* receiveMessage(int fd) {
	char buffer[1024];
	char* retval = (char*)malloc(0);
	char command[4];
	int received;

	bzero(buffer, sizeof(buffer));

	//We attempt to make a first read
	received = read(fd, buffer, sizeof(buffer));
	if(received == -1) {
		printf("ERROR: %s\n", strerror(errno));
		panic("Failed to receive a TCP message!", received);
	}
	retval = (char*)realloc(retval, received);
	if(!retval)
		panic("Failed to reallocate space for the received message...", received);
	for (int i = 0; i < received; i++)
		retval[i] = buffer[i];
	sscanf(retval, "%s ", command);
	if(strcmp(command, "RRT"))
		return retval;
	// Logic for receiving files
	return receiveFile(fd, retval, received);
}
char* receiveFile(int fd, char* sofar, int received_sofar) {
	char buffer[1024];
	char status[4];
	long file_size;
	char temp[64];
	int temp_len;
	long read_left;
	int received;
	int i;
	if(sscanf(sofar, "RRT %s %ld ", status, &file_size) != 2)
		return sofar; //badly formatted?
	sprintf(temp, "RTT %s %ld ", status, file_size);
	temp_len = strlen(temp); // points to the file start
	read_left = temp_len + file_size + 1 - received_sofar; // TOTAL SIZE +1 - already received
	while(read_left > 0) {
		received = read(fd, buffer, 1024);
		if(received == -1)
			panic("Failed to receive a TCP file!", received);
		sofar = (char*)realloc(sofar, received_sofar + received);
		if(!sofar)
			panic("Failed to reallocate space for the received file...", received_sofar + received);
		for (i = 0; i < received; i++)
			sofar[received_sofar + i] = buffer[i];
		received_sofar += received;
		read_left -= received;
	}
	return sofar;
}

void processMessage(char* message) {
	char code[4];
	char status[6];
	sscanf(message, "%s ", code);
	if (!strcmp(code, "RLO")) {
		sscanf(message, "RLO %s", status);
		if (!strcmp(status, "OK")) printf("You are now logged in.\n");
		else if (!strcmp(status, "NOK")) printf("UID is ok but password is incorrect.\n");
		else if (!strcmp(status, "ERR")) printf("UID does not exist.\n");
		else printf("BUFFER %s", message);
	}
	else if (!strcmp(code, "RRQ")) {
		sscanf(message, "RRQ %s", status);
		if (!strcmp(status, "OK")) printf("Request successful.\n");
		else if (!strcmp(status, "ELOG")) printf("Successful login not previously done.\n");
		else if (!strcmp(status, "EPD")) printf("Message could not be sent by the AS to the PD.\n");
		else if (!strcmp(status, "EUSER")) printf("UID is incorrect.\n");
		else if (!strcmp(status, "EFOP")) printf("Fop is invalid.\n");
		else if (!strcmp(status, "ERR")) printf("Incorrectly formatted REQ message.\n");
	}
	else if (!strcmp(code, "RAU")) {
		// Authentication successful
		sscanf(message, "RAU %04d", &tid);
		if (tid == 0) printf("Authentication not successful.\n");
		else printf("Authenticated! (TID=%04d).\n", tid);
	}
	else if (!strcmp(code, "RLS")) {
		// Authentication successful
		int num_files;
		sscanf(message, "RLS %s", status);
		if (!strcmp(status, "EOF")) printf("No files are available.\n");
		else if (!strcmp(status, "NOK")) printf("UID does not exist.\n");
		else if (!strcmp(status, "INV")) printf("TID rejected by the AS.\n");
		else if (!strcmp(status, "ERR")) printf("LST request not correctly formulated.\n");
		else {
			message[strcspn(message, "\n")] = ' '; //replace final \n with ' '
			sscanf(message, "RLS %d", &num_files);
			strtok(message, " ");	//RLS
			strtok(NULL, " ");		//num_files
			char* fname;
			char* fsize;
			for (int i = 1; i <= num_files; i++) {
				fname = strtok(NULL, " ");
				fsize = strtok(NULL, " ");
				printf("%d - (%s bytes) %s\n", i, fsize, fname);
			}
		}
	}
	else if (!strcmp(code, "RRT")) {
		int errcode;
		long fsize;
		int pointer;
		char buffer[128];
		sscanf(message, "RRT %s %ld", status, &fsize);
		if (!strcmp(status, "OK")) {
			p_FileContents file = newFileContents();
			if (!file)
				panic("Failed to malloc space for the file contents.", -1);
			errcode = setupFileContents(file, fsize);
			if (errcode == ERROR)
				panic("Failed to malloc space for the file contents (bytes).", errcode);
			// Figure out where the data starts
			sprintf(buffer, "RRT %s %ld ", status, fsize);
			pointer = strlen(buffer);
			// Copy that data to the FileContents
			for (int i = pointer; i < (fsize + pointer); i++) {
				file->byte_content[i - pointer] = message[i];
			}
			// Save the file to the disk
			errcode = createFile(RETRIEVE_FNAME, file);
			if (errcode == ERROR) {
				freeFileContents(file);
				panic("Failed to save the file to disk.", errcode);
			}
			if (errcode == FILE_EXISTS) {
				printf("A file with that name already exists here!\n");
				freeFileContents(file);
			}
			else {
				printf("Successfully retrieved the file %s\n", RETRIEVE_FNAME);
				freeFileContents(file);
			}
		}
		else if (!strcmp(status, "EOF")) printf("File is not available.\n");
		else if (!strcmp(status, "NOK")) printf("UID does not exist.\n");
		else if (!strcmp(status, "INV")) printf("TID rejected by the AS.\n");
		else if (!strcmp(status, "ERR")) printf("RTV request not correctly formulated.\n");
	}
	else if (!strcmp(code, "RUP")) {
		sscanf(message, "RUP %s", status);
		if (!strcmp(status, "OK")) printf("Upload successful.\n");
		else if (!strcmp(status, "NOK")) printf("UID does not exist.\n");
		else if (!strcmp(status, "DUP")) printf("File already registered.\n");
		else if (!strcmp(status, "FULL")) printf("User has already uploaded 15 files.\n");
		else if (!strcmp(status, "INV")) printf("TID rejected by the AS.\n");
		else if (!strcmp(status, "ERR")) printf("UPL request not correctly formulated.\n");
	}
	else if (!strcmp(code, "RDL")) {
		sscanf(message, "RDL %s", status);
		if (!strcmp(status, "OK")) printf("File deleted successfully.\n");
		else if (!strcmp(status, "EOF")) printf("File is not available.\n");
		else if (!strcmp(status, "NOK")) printf("UID does not exist.\n");
		else if (!strcmp(status, "INV")) printf("TID rejected by the AS.\n");
		else if (!strcmp(status, "ERR")) printf("DEL request not correctly formulated.\n");
	}
	else if (!strcmp(code, "RRM")) {
		sscanf(message, "RRM %s", status);
		if (!strcmp(status, "OK")) printf("Remove command successful.\n");
		else if (!strcmp(status, "NOK")) printf("UID does not exist.\n");
		else if (!strcmp(status, "INV")) printf("TID rejected by the AS.\n");
		else if (!strcmp(status, "ERR")) printf("REM request not correctly formulated.\n");
	}
	else {
		printf("Received an unknown message: %s\n", message);
	}
}

void processInput() {
	char cmd[10];
	char message[128];
	char* buffer;
	// Last request id stored
	int rid;
	char vc[5];
	char fop;
	char fname[25];

	while (1) {
		bzero(message, sizeof(buffer));
		scanf("%s", cmd);
		if (!strcmp(cmd, "login")) {
			scanf(" %d %s", &uid, pass);
			sprintf(message, "LOG %05d %s\n", uid, pass);
			sendMessage(as_fd, message, MSG);
			buffer = receiveMessage(as_fd);
			processMessage(buffer);
			free(buffer);
		}
		else if (!strcmp(cmd, "req")) {
			scanf(" %c", &fop);
			rid = rand() % 10000;
			if (fop == 'R' || fop == 'U' || fop == 'D') {
				scanf(" %s", fname);
				sprintf(message, "REQ %05d %04d %c %s\n", uid, rid, fop, fname);
			}
			else if (fop == 'L' || fop == 'X') sprintf(message, "REQ %05d %04d %c\n", uid, rid, fop);
			sendMessage(as_fd, message, MSG);
			buffer = receiveMessage(as_fd);
			processMessage(buffer);
			free(buffer);
		}
		else if (!strcmp(cmd, "val")) {
			scanf(" %s", vc);
			sprintf(message, "AUT %05d %04d %s\n", uid, rid, vc);
			sendMessage(as_fd, message, MSG);
			buffer = receiveMessage(as_fd);
			processMessage(buffer);
			free(buffer);
		}
		else if (!strcmp(cmd, "l") || !strcmp(cmd, "list")) {
			sprintf(message, "LST %05d %04d\n", uid, tid);
			connectFS();
			sendMessage(fs_fd, message, MSG);
			buffer = receiveMessage(fs_fd);
			closeFS();
			processMessage(buffer);
			free(buffer);
		}
		else if (!strcmp(cmd, "r") || !strcmp(cmd, "retrieve")) {
			scanf(" %s", RETRIEVE_FNAME);
			sprintf(message, "RTV %05d %04d %s\n", uid, tid, RETRIEVE_FNAME);
			connectFS();
			sendMessage(fs_fd, message, MSG);
			buffer = receiveMessage(fs_fd);
			closeFS();
			processMessage(buffer);
			free(buffer);
		}
		else if (!strcmp(cmd, "u") || !strcmp(cmd, "upload")) {
			int errcode;
			scanf(" %s", fname);
			p_FileContents file = newFileContents();
			if (!file)
				panic("Failed to malloc space for the file.", -1);
			errcode = getFileContents(fname, file);
			if (errcode == ERROR)
				panic("Failed to load the file.", errcode);
			if (errcode == FILE_DOESNT_EXIST) {
				printf("Couldn't find the file %s\n", fname);
				continue;
			}
			connectFS();
			sendFile(fs_fd, tid, fname, file);
			buffer = receiveMessage(fs_fd);
			closeFS();
			processMessage(buffer);
			free(buffer);
			freeFileContents(file);
		}
		else if (!strcmp(cmd, "d") || !strcmp(cmd, "delete")) {
			scanf(" %s", fname);
			sprintf(message, "DEL %05d %04d %s\n", uid, tid, fname);
			connectFS();
			sendMessage(fs_fd, message, MSG);
			buffer = receiveMessage(fs_fd);
			closeFS();
			processMessage(buffer);
			free(buffer);
		}
		else if (!strcmp(cmd, "x") || !strcmp(cmd, "remove")) {
			sprintf(message, "REM %05d %04d\n", uid, tid);
			connectFS();
			sendMessage(fs_fd, message, MSG);
			buffer = receiveMessage(fs_fd);
			closeFS();
			processMessage(buffer);
			free(buffer);
		}
		else if (!strcmp(cmd, "exit")) {
			closeAS();
			exit(0);
		}
		else {
			printf("Unknown command (%s).\n", cmd);
			// skip the rest of the line
			int ch;
			while((ch = getchar()) != '\n' && (ch != EOF));
		}
	}
}

int main(int argc, char* argv[]) {
	parseArgs(argc, argv);
	//printArgs();

	/* Intializes random number generator */
	time_t t;
	srand((unsigned)time(&t));


	connectAS();
	processInput();
}
