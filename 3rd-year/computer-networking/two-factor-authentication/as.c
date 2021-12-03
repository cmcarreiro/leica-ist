#include "as.h"

void printArgs() {
	printf("---------- ARGS ----------\n");
	printf("ASIP: %s\n", asip);
	printf("    ASPORT: %s\n", asport);
	printf("VERBOSE? %s\n", verbose ? "True" : "False");
	printf("--------------------------\n");
}

void panic(char* message, int errcode) {
	printf("ERROR: %s\n CODE: %d\n", message, errcode);
	closeServer();
	exit(errcode);
}

void usage(int errcode) {
	printf("USAGE: ./as [-p ASport] [-v]\n");
	exit(errcode);
}

void parseArgs(int argc, char* argv[]) {
	// Set defaults
	strcpy(asip, "localhost");
	strcpy(asport, "58035");
	verbose = FALSE;

	int opt;
	while ((opt = getopt(argc, argv, "p:v")) != -1) {
		switch (opt) {
		case 'p':
			if (*optarg == '-') usage(opt);
			strcpy(asport, optarg);
			break;
		case 'v':
			verbose = TRUE;
			break;
		case '?':
			usage(opt);
			break;
		}
	}
}

void closeServer() {
	if (verbose)
		printf("[END] Closing server.\n");
	if (tcp_fd) close(tcp_fd);
	if (udp_fd) close(udp_fd);
	for (int i = 0; i < MAX_CLIENTS; i++)
		if (fdlist[i] != -1) close(fdlist[i]);
	exit(0);
}

void initServer() {
	int errcode;
	struct addrinfo hints, * addr;
	//UDP socket
	udp_fd = socket(AF_INET, SOCK_DGRAM, 0);
	if (udp_fd == -1)
		panic("Failed to initialize UDP socket.", udp_fd);
	memset(&hints, 0, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_DGRAM;
	hints.ai_flags = AI_PASSIVE;
	errcode = getaddrinfo(NULL, asport, &hints, &addr);
	if (errcode != 0)
		panic("Failed to getaddrinfo for UDP.", errcode);
	errcode = bind(udp_fd, addr->ai_addr, addr->ai_addrlen);
	if (errcode == -1)
		panic("Failed to bind the UDP socket.", errcode);
	if (verbose) {
		printf("[INIT] UDP server initialized @ %s:%s\n", asip, asport);
	}
	// TCP socket
	tcp_fd = socket(AF_INET, SOCK_STREAM, 0);							//socket()
	if (tcp_fd == -1)
		panic("Failed to initialize TCP socket.", tcp_fd);
	memset(&hints, 0, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;
	errcode = getaddrinfo(NULL, asport, &hints, &addr);					//getaddrinfo()
	if (errcode != 0)
		panic("Failed to getaddrinfo for TCP.", errcode);
	errcode = bind(tcp_fd, addr->ai_addr, addr->ai_addrlen);			//bind()
	if (errcode == -1)
		panic("Failed to bind the TCP socket.", errcode);
	errcode = listen(tcp_fd, MAX_CLIENTS);								//listen()
	if (errcode == -1)
		panic("Failed to get TCP server to listen.", errcode);
	if (verbose) {
		printf("[INIT] TCP server initialized @ %s:%s\n", asip, asport);
		printf("[INIT] TCP server is listening...\n");
	}
	freeaddrinfo(addr);
}

void receiveUDP(char* dest) {
	int errcode;
	int from_len = sizeof(callback_udp);
	bzero(dest, BUFF_SIZE);
	errcode = recvfrom(udp_fd, dest, BUFF_SIZE, 0, (struct sockaddr *)&callback_udp, &from_len);
	if (errcode <= 0)
		panic("Failed to receive UDP message.", errcode);
	if (verbose) {
		char ip[128];
		inet_ntop(AF_INET, &callback_udp, ip, sizeof(ip));
		int port = ntohs(callback_udp.sin_port);
		printf("[RECV] (UDP) Received from %s:%d - \"%s\"\n", ip, port, dest);
	}
}

void replyUDP(char* message) {
	int bytes_sent;
	int message_size = strlen(message);
	bytes_sent = sendto(udp_fd, message, message_size, 0, (struct sockaddr *)&callback_udp, sizeof(callback_udp));
	if (bytes_sent != message_size)
		panic("Failed to send a message.", bytes_sent);
	if (verbose) {
		char ip[128];
		inet_ntop(AF_INET, &callback_udp, ip, sizeof(ip));
		int port = ntohs(callback_udp.sin_port);
		printf("[SEND] (UDP) Replied back to %s:%d - \"%s\"\n", ip, port, message);
	}
}

int sendVlcUDP(int uid, int vc, char fop, char* fname) {
	char ip[128], port[9];
	struct addrinfo hints, * addr;
	int errcode;
	errcode = getPD(uid, ip, port);
	if (errcode == ERROR)
		panic("Failed to get a PD's info from disk!", uid);
	if(errcode == NO_PD)
		return NO_PD;
	// get the pd's addrinfo
	bzero(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_DGRAM;
	errcode = getaddrinfo(ip, port, &hints, &addr);
	if (errcode != 0)
		panic("Failed to get a PD's addrinfo!", uid);
	// send him the vlc
	char buffer[BUFF_SIZE];
	bzero(buffer, BUFF_SIZE);
	if (fname[0] == '\0')
		sprintf(buffer, "VLC %05d %04d %c\n", uid, vc, fop);
	else
		sprintf(buffer, "VLC %05d %04d %c %s\n", uid, vc, fop, fname);
	int bytes_sent;
	int message_size = strlen(buffer);
	bytes_sent = sendto(udp_fd, buffer, message_size, 0, addr->ai_addr, addr->ai_addrlen);
	if (bytes_sent != message_size)
		panic("Failed to send a message.", bytes_sent);
	if (verbose)
		printf("[VAL] (UDP) Sent VLC %d to %04d's PD @ %s:%s - \"%s\"\n", vc, uid, ip, port, buffer);
	freeaddrinfo(addr);
	// receive the response
	bzero(buffer, sizeof(buffer));
	receiveUDP(buffer);
	return OKAY;
}

void processUDP(char* message) {
	char command[4];
	int uid;
	int errcode;
	sscanf(message, "%s ", command);
	// PD messages
	if (!strcmp(command, "REG")) {
		char pass[9], ip[128], port[6];
		sscanf(message, "REG %d %s %s %s\n", &uid, pass, ip, port);
		errcode = regUser(uid, pass);
		if (errcode == ERROR)
			panic("Something went wrong saving a user to disk!", uid);
		if (errcode == WRONG_PASSWORD) {
			replyUDP("RRG NOK\n");
			return;
		}
		errcode = regPD(uid, ip, port);
		if (errcode == ERROR)
			panic("Something went wrong saving a PD to disk!", uid);
		if (errcode == ALREADY_EXISTS) {
			replyUDP("RRG NOK\n");
			return;
		}
		replyUDP("RRG OK\n");
	}
	else if (!strcmp(command, "UNR")) {
		char pass[9];
		sscanf(message, "UNR %d %s\n", &uid, pass);
		errcode = checkPassword(uid, pass);
		if (errcode == ERROR)
			panic("Something went wrong verifying a password!", uid);
		if (errcode == WRONG_PASSWORD) {
			replyUDP("RUN NOK\n");
			return;
		}
		unregPD(uid);
		replyUDP("RUN OK\n");
	}
	// FS messages
	else if (!strcmp(command, "VLD")) {
		int tid;
		char fop[2], fname[25];
		sscanf(message, "VLD %d %d\n", &uid, &tid);
		errcode = getTID(uid, tid, fop, fname);
		if (errcode == ERROR)
			panic("Something went wrong getting a TID from disk!", uid);
		memset(message, 0, sizeof(message));
		if (errcode == NO_TID) {
			sprintf(message, "CNF %05d %04d E\n", uid, tid);
		}
		else if (fname[0] != '\0'){
			sprintf(message, "CNF %05d %04d %s %s\n", uid, tid, fop, fname);
		}
		else {
			sprintf(message, "CNF %05d %04d %s\n", uid, tid, fop);
		}
		unregTID(uid, tid);
		// Check if it's a remove
		if(!strcmp(fop, "X")) {
			unregUser(uid);
		}
		replyUDP(message);
	}
	else
		replyUDP("ERR\n");
}

char* receiveTCP(int fd) {
	char buffer[BUFF_SIZE];
	char* retval = (char*)malloc(0);
	long len = 0;
	int received;

	bzero(buffer, BUFF_SIZE);
	while ((received = read(fd, buffer, BUFF_SIZE)) > 0) {
		// Add space to the return value
		retval = (char*)realloc(retval, len + received);
		if (!retval)
			panic("Failed to reallocate space for the received message...", len + received);
		// Copy the newly read to the end of the return value
		for (int i = 0; i < received; i++) {
			retval[len + i] = buffer[i];
		}
		len += received;
		if (retval[len - 1] == '\n') {
			retval = (char*)realloc(retval, len + 1);
			retval[(len + 1) - 1] = '\0'; //finish string
			break;
		}
		bzero(buffer, BUFF_SIZE);
	}
	if (received == -1) {
		free(retval);
		panic("Failed to receive a TCP message.", fd);
	}
	if (len == 0) {
		free(retval);
		if(verbose) {
			int uid = getUID(fd);
			printf("[CON] User with UID %05d disconnected.\n", uid);
		}
		setUID(-1, fd);
		removeSocket(fd);
		return NULL;
	}
	if (verbose) {		
		struct sockaddr_in from;
		int fromlen = sizeof(from);
		getsockname(fd, (struct sockaddr*)&from, &fromlen);
		printf("[RECV] (TCP) Received from %s:%d - \"%s\"\n", inet_ntoa(from.sin_addr), ntohs(from.sin_port), retval);
	}
	return retval;
}

void sendTCP(int fd, char* message) {
	int sent = 0, written;
	int messagelen = strlen(message);
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

void processTCP(int fd, char* message) {
	char command[4];
	int uid, errcode;
	char buffer[128];
	sscanf(message, "%s ", command);
	if (!strcmp(command, "LOG")) {
		char pass[9];
		sscanf(message, "LOG %d %s\n", &uid, pass);
		errcode = loginUser(uid, pass);
		if (errcode == ERROR)
			panic("Failed to get a password from the disk.", uid);
		if (errcode == USER_NOT_REGISTERED || errcode == ALREADY_EXISTS)
			sprintf(buffer, "RLO ERR\n");
		else if (errcode == WRONG_PASSWORD)
			sprintf(buffer, "RLO NOK\n");
		else {
			sprintf(buffer, "RLO OK\n");
			setUID(uid, fd);
		}
		sendTCP(fd, buffer);
	}
	else if (!strcmp(command, "REQ")) {
		int exp_uid = getUID(fd);
		if(exp_uid == -1)
			sprintf(buffer, "RRQ ELOG\n");
		else {
			int rid, vc;
			int errcode;
			char fop, fname[25];
			
			memset(fname, '\0', sizeof(fname));
			sscanf(message, "REQ %d %d %c %s\n", &uid, &rid, &fop, fname);
			if(exp_uid != uid)
				sprintf(buffer, "RRQ EUSER\n");
			else {
				vc = rand() % 10000;
				errcode = sendVlcUDP(uid, vc, fop, fname);
				if(errcode == NO_PD)
					sprintf(buffer, "RRQ EPD\n");
				else {
					errcode = regRID(uid, rid, vc, fop, fname);
					if (errcode == ERROR)
						panic("Failed to store a RID.", uid);
					if (errcode == ALREADY_EXISTS)
						sprintf(buffer, "RRQ ERR\n"); // RANDOMLY GENERATED TWO OF THE SAME RIDS?
					else
						sprintf(buffer, "RRQ OK\n");
				}
			}
		}
		sendTCP(fd, buffer);
	}
	else if (!strcmp(command, "AUT")) {
		int rid, vc_user;
		int vc_as, tid;
		char fop[2], fname[25];
		sscanf(message, "AUT %d %d %d\n", &uid, &rid, &vc_user);
		vc_as = getRID(uid, rid, fop, fname);
		if(vc_as == ERROR)
			panic("Failed to read a TID for a user.", uid);
		if (vc_as == NO_TID || vc_user != vc_as)
			sprintf(buffer, "RAU 0\n");
		else {
			tid = rand() % 10000;
			sprintf(buffer, "RAU %04d\n", tid);
			regTID(uid, tid, fop[0], fname);
			unregRID(uid, rid);
		}
		sendTCP(fd, buffer);
	}
	free(message);
}

void setUID(int uid, int fd) {
	int i;
	for (i = 0; i < MAX_CLIENTS; i++) {
		if (fdlist[i] == fd)
			break;
	}
	if (uid == -1) // Removing a user
		logoutUser(uidlist[i]);
	uidlist[i] = uid;
}

int getUID(int fd) {
	int i;
	for(i = 0; i < MAX_CLIENTS; i++) {
		if (fdlist[i] == fd)
			return uidlist[i];
	}
	return -1;
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

int main(int argc, char* argv[]) {
	parseArgs(argc, argv);
	if (verbose)
		printArgs();
	signal(SIGINT, closeServer);
	initServer();
	initializeStorage();

	/* Intializes random number generator */
	time_t t;
	srand((unsigned)time(&t));

	fd_set readfds;
	int max, fd, retval;
	char buffer[256];

	while (1) {
		bzero(buffer, BUFF_SIZE);
		FD_ZERO(&readfds);
		FD_SET(udp_fd, &readfds);
		FD_SET(tcp_fd, &readfds);
		max = tcp_fd > udp_fd ? tcp_fd : udp_fd;
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
			if (FD_ISSET(udp_fd, &readfds)) {
				receiveUDP(buffer);
				processUDP(buffer);
			}
			else if (FD_ISSET(tcp_fd, &readfds)) {
				// Master socket listening
				struct sockaddr_in address;
				socklen_t addrlen;
				int new_socket = accept(tcp_fd, (struct sockaddr*)&address, &addrlen);
				if (new_socket == -1)
					panic("Failed to accept a connection.", new_socket);
				if (verbose)
					printf("[CON] NEW TCP CONNECTION ACCEPTED @ %s:%d\n", inet_ntoa(address.sin_addr), ntohs(address.sin_port));
				addSocket(new_socket);
			}
			// Any other socket here
			else {
				for (int i = 0; i < MAX_CLIENTS; i++) {
					fd = fdlist[i];
					if (FD_ISSET(fd, &readfds)) {
						char* message = receiveTCP(fd);
						if (message != NULL) processTCP(fd, message);
					}
				}
			}
		}
	}
}
