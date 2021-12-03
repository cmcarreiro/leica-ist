#include "fs.h"

void panic(char* message, int errcode) {
	printf("ERROR: %s\n CODE: %d\n", message, errcode);
	closeServer();
	exit(errcode);
}

void printArgs() {
	printf("---------- ARGS ----------\n");
	printf("FSPORT %s\n", fsport);
	printf("ASIP: %s\n", asip);
	printf("    ASPORT: %s\n", asport);
	printf("VERBOSE? %s\n", verbose ? "True" : "False");
	printf("--------------------------\n");
}

void usage(int errcode) {
	printf("USAGE: ./fs [-q FSport] [-n ASIP] [-p ASport] [-v]\n");
	exit(errcode);
}

void parseArgs(int argc, char* argv[]) {
	int opt;
	// Set defaults
	strcpy(fsport, "59035");
	strcpy(asip, "localhost");
	strcpy(asport, "58035");
	while ((opt = getopt(argc, argv, "vq:n:p:")) != -1) {
		switch (opt) {
		case 'q':
			if (*optarg == '-') usage(opt);
			strcpy(fsport, optarg);
			break;
		case 'n':
			if (*optarg == '-') usage(opt);
			strcpy(asip, optarg);
			break;
		case 'p':
			if (*optarg == '-') usage(opt);
			strcpy(asport, optarg);
			break;
		case 'v':
			verbose = TRUE;
			break;
		case '?':
			usage(opt);
		}
	}
}

void startConnectionTCP() {
	int errcode;
	struct addrinfo hints;
	struct addrinfo* addr;
	fs_fd = socket(AF_INET, SOCK_STREAM, 0);
	if (fs_fd == -1)
		panic("Failed to initialize TCP socket.", -1);
	bzero(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;
	errcode = getaddrinfo(NULL, fsport, &hints, &addr);
	if (errcode != 0)
		panic("Failed to getaddrinfo for TCP.", errcode);
	errcode = bind(fs_fd, addr->ai_addr, addr->ai_addrlen);
	if (errcode == -1)
		panic("Failed to bind the TCP socket.", errcode);
	errcode = listen(fs_fd, MAX_CLIENTS);
	if (errcode == -1)
		panic("Failed to get TCP server to listen.", errcode);
	if (verbose)
		printf("[INIT] TCP server up. Listening on port %s...\n", fsport);
	freeaddrinfo(addr);
}

void addSocket(int fd) {
	for (int i = 0; i < MAX_CLIENTS; i++) {
		if (fdlist[i] == -1) {
			fdlist[i] = fd;
			return;
		}
	}
	panic("Too many connections!", -1);
}

void removeSocket(int fd) {
	for (int i = 0; i < MAX_CLIENTS; i++) {
		if (fdlist[i] == fd) {
			fdlist[i] = -1;
			return;
		}
	}
}

char* receiveTCP(int fd) {
	char buffer[1024];
	char* retval = (char*)malloc(0);
	char command[4];
	int received;

	bzero(buffer, sizeof(buffer));
	
	if(fd == -1)
		return NULL;
	//We attempt to make a first read
	received = read(fd, buffer, sizeof(buffer));
	if(received == -1) {
		printf("ERROR: %s\n", strerror(errno));
		panic("Failed to receive a TCP message!", received);
	}
	if (received == 0) { // Someone disconnected?
		removeSocket(fd);
		return NULL;
	}
	retval = (char*)realloc(retval, received);
	if(!retval)
		panic("Failed to reallocate space for the received message...", received);
	for (int i = 0; i < received; i++)
		retval[i] = buffer[i];
	sscanf(retval, "%s ", command);
	if(strcmp(command, "UPL")) {
		if (verbose) {
			struct sockaddr_in from;
			int fromlen = sizeof(from);
			getsockname(fd, (struct sockaddr*)&from, &fromlen);
			printf("[RECV] Received from %s:%d - \"%s\"\n", inet_ntoa(from.sin_addr), ntohs(from.sin_port), retval);
		}
		return retval;
	}
	// Logic for receiving files
	
	return receiveFile(fd, retval, received);
}
char* receiveFile(int fd, char* sofar, int received_sofar) {
	char buffer[1024];
	int uid, tid;
	char fname[25];
	long file_size;
	char temp[64];
	int temp_len;
	long read_left;
	int received;
	int i;
	if(sscanf(sofar, "UPL %d %d %s %ld ", &uid, &tid, fname, &file_size) != 4)
		return sofar; //badly formatted?
	sprintf(temp, "UPL %05d %04d %s %ld ", uid, tid, fname, file_size);
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
	if (verbose) {
		struct sockaddr_in from;
		int fromlen = sizeof(from);
		getsockname(fd, (struct sockaddr*)&from, &fromlen);
		printf("[RECV] Received a file from %s:%d - %d bytes.\n", inet_ntoa(from.sin_addr), ntohs(from.sin_port), received_sofar - temp_len -1);
	}
	return sofar;
}

void sendTCP(int fd, char* message, long messagelen) {
	long sent = 0, written;
	if (messagelen == MSG)
		messagelen = strlen(message);
	while (sent < messagelen) {
		written = write(fd, &message[sent], messagelen - sent);
		if (written == -1)
			panic("Failed to send TCP message.", fd);
		sent += written;
	}
	if (verbose) {
		struct sockaddr_in from;
		int fromlen = sizeof(from);
		getsockname(fd, (struct sockaddr*)&from, &fromlen);
		printf("[SEND] (TCP) Sent to %s:%d - \"%s\"\n", inet_ntoa(from.sin_addr), ntohs(from.sin_port), message);
	}
}

void sendFile(int fd, p_FileContents file) {
	char start[32];
	sprintf(start, "RRT OK %ld ", file->byte_size);
	int startlen = strlen(start);
	long messagelen = startlen + file->byte_size + 1; // final \n
	char* message = (char*)malloc(sizeof(char) * messagelen);
	strcpy(message, start);
	for (int i = startlen; i < messagelen - 1; i++)
		message[i] = file->byte_content[i - startlen];
	message[messagelen - 1] = '\n';
	sendTCP(fd, message, messagelen);
	free(message);
}

void closeConnectionTCP() {
	if (fs_fd)
		close(fs_fd);
	for (int i = 0; i < MAX_CLIENTS; i++) {
		if (fdlist[i] != -1)
			close(fdlist[i]);
	}
}

void startConnectionAS() {
	int errcode;
	struct addrinfo* addr;
	struct addrinfo hints;
	// Setup the socket	
	as_fd = socket(AF_INET, SOCK_DGRAM, 0);
	if (as_fd == -1)
		panic("Failed to initate UDP socket.", as_fd);
	bzero(&hints, sizeof(hints));

	// Setup the AS addrinfo
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_DGRAM;
	errcode = getaddrinfo(asip, asport, &hints, &addr);
	if (errcode != 0)
		panic("Failed to getaddrinfo for the AS.", errcode);
	as_addr = *addr->ai_addr;
	if (verbose)
		printf("[INIT] UDP communication with AS @ %s:%s setup...\n", asip, asport);
	freeaddrinfo(addr);
}

void closeConnectionAS() {
	if (as_fd)
		close(as_fd);
}

void closeServer() {
	printf("[END] Closing server...\n");
	closeConnectionAS();
	closeConnectionTCP();
}

int validateOperation(int uid, int tid, char fop, char* fname) {
	int errcode;
	char message[MESSAGESIZE];
	// First we send the request to the AS
	sprintf(message, "VLD %05d %04d\n", uid, tid);
	int bytes_sent = sendto(as_fd, message, strlen(message), 0, &as_addr, sizeof(as_addr));
	if (bytes_sent != strlen(message))
		panic("Failed to send a message to the AS.", bytes_sent);
	if (verbose)
		printf("[VAL] Sent to the AS: \"%s\"\n", message);
	// Imediately afterwards we should receive the verification
	struct sockaddr_in from_addr;
	socklen_t len = sizeof(from_addr);
	bzero(message, MESSAGESIZE);
	errcode = recvfrom(as_fd, message, MESSAGESIZE, 0, (struct sockaddr*)&from_addr, &len);
	if (errcode <= 0)
		panic("Failed to receive a message from the AS.", errcode);
	if (verbose) {
		char from_ip[128];
		inet_ntop(AF_INET, &from_addr.sin_addr, from_ip, sizeof(from_ip));
		printf("[VAL] Received from the AS @ %s:%d - \"%s\"\n", from_ip, ntohs(from_addr.sin_port), message);
	}
	// Interpret the message received
	int recvUID, recvTID;
	char recvfop;
	char recvfname[25];
	sscanf(message, "CNF %d %d %c %s", &recvUID, &recvTID, &recvfop, recvfname);
	if (uid != recvUID) {
		if (verbose)
			printf("[VAL] VALIDATION FAILED (wrong uid). got=%05d, expected=%05d\n", recvUID, uid);
		return INVALID;
	}
	if (tid != recvTID) {
		if (verbose)
			printf("[VAL] VALIDATION FAILED (wrong tid). got=%d, expected=%d\n", recvTID, tid);
		return INVALID;
	}
	if (fop != recvfop) {
		if (verbose)
			printf("[VAL] VALIDATION FAILED (wrong fop). got=%c, expected=%c\n", recvfop, fop);
		return INVALID;
	}
	if (fname != NULL) {
		if (strcmp(fname, recvfname)) {
			if (verbose)
				printf("[VAL] VALIDATION FAILED (wrong file name). got=%s, expected=%s\n", recvfname, fname);
			return INVALID;
		}
	}
	printf("[VAL] VALIDATION PASSED!\n");
	return VALID;
}

void requestList(int fd, int uid, int tid) {
	if (verbose)
		printf("[REQ] LIST REQUESTED. uid=%05d, tid=%d\n", uid, tid);
	char temp[576];
	int validation = validateOperation(uid, tid, 'L', NULL);
	if (validation == INVALID) {
		sendTCP(fd, "RLS INV\n", MSG);
		return;
	}
	// Get the info from fs_storage
	int status = listDir(uid, temp);
	if (status == NO_DIRECTORY) {
		sendTCP(fd, "RLS NOK\n", MSG);
		return;
	}
	if (status == NO_FILES) {
		sendTCP(fd, "RLS EOF\n", MSG);
		return;
	}
	// Send the list over
	char final[2048];
	sprintf(final, "RLS %s\n", temp);
	sendTCP(fd, final, MSG);
}

void retrieveFile(int fd, int uid, int tid, char* fname) {
	if (verbose)
		printf("[REQ] FILE REQUESTED. uid=%05d, tid=%d, file=%s\n", uid, tid, fname);
	int validation = validateOperation(uid, tid, 'R', fname);
	if (validation == INVALID) {
		sendTCP(fd, "RRT INV\n", MSG);
		return;
	}
	p_FileContents contents = newFileContents();
	int status = getFileContentsFS(uid, fname, contents);
	if (status == ERROR)
		panic("Failed to retrieve some file contents...", status);
	if (status == NO_DIRECTORY) {
		sendTCP(fd, "RRT NOK\n", MSG);
		return;
	}
	if (status == FILE_DOESNT_EXIST) {
		sendTCP(fd, "RRT EOF\n", MSG);
		return;
	}
	sendFile(fd, contents);
}

void uploadFile(int fd, int uid, int tid, char* fname, p_FileContents contents) {
	if (verbose)
		printf("[REQ] FILE UPLOAD. uid=%05d, tid=%d, file=%s\n", uid, tid, fname);
	int validation = validateOperation(uid, tid, 'U', fname);
	if (validation == INVALID) {
		sendTCP(fd, "RUP INV\n", MSG);
		return;
	}
	if (countFiles(uid) >= 15) {
		sendTCP(fd, "RUP FULL\n", MSG);
		return;
	}
	int status = createFileFS(uid, fname, contents);
	if (status == ERROR)
		panic("Failed to create a file...", status);
	if (status == FILE_EXISTS) {
		sendTCP(fd, "RUP DUP\n", MSG);
		return;
	}
	sendTCP(fd, "RUP OK\n", MSG);
}

void removeFile(int fd, int uid, int tid, char* fname) {
	if (verbose)
		printf("[REQ] FILE DELETED. uid=%05d, tid=%d, file=%s\n", uid, tid, fname);
	int validation = validateOperation(uid, tid, 'D', fname);
	if (validation == INVALID) {
		sendTCP(fd, "RDL INV\n", MSG);
		return;
	}
	int status = deleteFileFS(uid, fname);
	if (status == ERROR)
		panic("Failed to delete a file...", status);
	if (status == NO_DIRECTORY) {
		sendTCP(fd, "RDL NOK\n", MSG);
		return;
	}
	if (status == FILE_DOESNT_EXIST) {
		sendTCP(fd, "RDL EOF\n", MSG);
		return;
	}
	sendTCP(fd, "RDL OK\n", MSG);
}

void removeUser(int fd, int uid, int tid) {
	if (verbose)
		printf("[REQ] USER REMOVAL. uid=%05d, tid=%d\n", uid, tid);
	int validation = validateOperation(uid, tid, 'X', NULL);
	if (validation == INVALID) {
		sendTCP(fd, "REM INV\n", MSG);
		return;
	}
	int status = removeDir(uid);
	if (status == NO_DIRECTORY) {
		sendTCP(fd, "RRM NOK\n", MSG);
		return;
	}
	sendTCP(fd, "RRM OK\n", MSG);
}

int main(int argc, char* argv[]) {
	parseArgs(argc, argv);
	initializeStorage();
	signal(SIGINT, closeServer);
	if (verbose)
		printArgs();
	startConnectionAS();
	startConnectionTCP();

	fd_set readfds;
	int max, fd, retval;
	while (1) {
		FD_ZERO(&readfds);
		FD_SET(fs_fd, &readfds);
		FD_SET(STDIN_FILENO, &readfds);
		max = fs_fd;
		// Add all the connected FDS to the select
		for (int i = 0; i < MAX_CLIENTS; i++) {
			fd = fdlist[i];
			if (fd != -1)
				FD_SET(fd, &readfds);
			if (fd > max)
				max = fd;
		}
		retval = select(max + 1, &readfds, (fd_set*)NULL, (fd_set*)NULL, NULL);
		switch (retval) {
		case 0:
			// Timeout
			break;
		case -1:
			panic("Select error.", -1);
			break;
		default:
			if (FD_ISSET(fs_fd, &readfds)) {
				// Master socket listening
				struct sockaddr_in address;
				socklen_t addrlen;
				int new_socket = accept(fs_fd, (struct sockaddr*)&address, &addrlen);
				if (new_socket == -1)
					panic("Failed to accept a connection.", new_socket);
				if (verbose)
					printf("[CON] NEW TCP CONNECTION ACCEPTED @ %s:%d\n", inet_ntoa(address.sin_addr), ntohs(address.sin_port));
				addSocket(new_socket);
			}
			// Any other socket here
			for (int i = 0; i < MAX_CLIENTS; i++) {
				fd = fdlist[i];
				if (FD_ISSET(fd, &readfds)) {
					char* message = receiveTCP(fd);
					if (message == NULL)
						continue;
					char command[4], fname[25];
					int uid, tid;
					sscanf(message, "%s", command);
					if (!strcmp(command, "LST")) {
						if (sscanf(message, "LST %d %d\n", &uid, &tid) == 2)
							requestList(fd, uid, tid);
						else
							sendTCP(fd, "RLS ERR\n", MSG);
					}
					else if (!strcmp(command, "RTV")) {
						if (sscanf(message, "RTV %d %d %s\n", &uid, &tid, fname) == 3)
							retrieveFile(fd, uid, tid, fname);
						else
							sendTCP(fd, "RRT ERR\n", MSG);
					}
					else if (!strcmp(command, "UPL")) {
						long file_size;
						int str_pointer;
						char temp[64];
						if (sscanf(message, "UPL %d %d %s %ld ", &uid, &tid, fname, &file_size) == 4) {
							p_FileContents contents = newFileContents();
							setupFileContents(contents, file_size);
							// Figure out where the file starts
							sprintf(temp, "UPL %05d %04d %s %ld ", uid, tid, fname, file_size);
							str_pointer = strlen(temp);
							// Copy it
							for (int i = str_pointer; i < (file_size + str_pointer); i++) {
								contents->byte_content[i - str_pointer] = message[i];
							}
							uploadFile(fd, uid, tid, fname, contents);
							freeFileContents(contents);
						}
						else
							sendTCP(fd, "RUP ERR\n", MSG);
					}
					else if (!strcmp(command, "DEL")) {
						if (sscanf(message, "DEL %d %d %s\n", &uid, &tid, fname) == 3)
							removeFile(fd, uid, tid, fname);
						else
							sendTCP(fd, "RDL ERR\n", MSG);
					}
					else if (!strcmp(command, "REM")) {
						if (sscanf(message, "REM %d %d\n", &uid, &tid) == 2)
							removeUser(fd, uid, tid);
						else
							sendTCP(fd, "RRM ERR\n", MSG);
					}
					else
						sendTCP(fd, "ERR\n", MSG);
					free(message);
				}
			}
		}
	}
	return 0;
}
