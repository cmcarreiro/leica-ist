#include "pd.h"

void panic(char* message, int errcode) {
	printf("ERROR: %s\n CODE: %d\n", message, errcode);
	closeConnection();
	exit(errcode);
}

void usage(int errcode) {
	printf("USAGE: ./pd PDIP [-d PDport] [-n ASIP] [-p ASport]\n");
	exit(errcode);
}

void printArgs() {
	printf("---------- ARGS ----------\n");
	printf("PDIP: %s\n", pdip);
	printf("    PDport: %s\n", pdport);
	printf("ASIP: %s\n", asip);
	printf("    ASport: %s\n", asport);
	printf("--------------------------\n");
}

void startConnection() {
	int errcode;
	struct addrinfo* pd, * as;
	struct addrinfo hints;
	// Setup the socket
	fd = socket(AF_INET, SOCK_DGRAM, 0);								//socket()
	if (fd == -1)
		panic("Failed to initate socket.", fd);
	bzero(&hints, sizeof(hints));

	// Setup the AS addrinfo
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_DGRAM;
	errcode = getaddrinfo(asip, asport, &hints, &as);
	if (errcode != 0)
		panic("Failed to getaddrinfo for the AS.", errcode);
	as_addr = *as->ai_addr;
	freeaddrinfo(as);

	// Setup the local server to receive VLCs
	hints.ai_flags = AI_PASSIVE;
	errcode = getaddrinfo(NULL, pdport, &hints, &pd);
	if (errcode != 0)
		panic("Failed to getaddrinfo for the PD.", errcode);
	// Copy the struct sockaddr to our global to send messages later.
	errcode = bind(fd, pd->ai_addr, pd->ai_addrlen);					//bind()
	if (errcode == -1)
		panic("Failed to bind the socket.", errcode);
	freeaddrinfo(pd);
}

void sendMessage(char* message, struct sockaddr dest_addr) {
	//printf("[sendMessage] %s\n", message);
	int bytes_sent;
	int message_size = strlen(message);
	bytes_sent = sendto(fd, message, message_size, 0, &dest_addr, sizeof(dest_addr));
	if (bytes_sent != message_size)
		panic("Failed to send a message.", bytes_sent);
}

void receiveMessage() {
	int errcode;
	int from_len = sizeof(as_callback);
	bzero(buffer, sizeof(buffer));
	errcode = recvfrom(fd, buffer, BUFF_SIZE, 0, &as_callback, &from_len);
	//printf("[receiveMessage] %s\n", buffer);
	if (errcode <= 0)
		panic("Failed to receive a message.", errcode);
}

void closeConnection() {
	if (fd) close(fd);
}

void parseArgs(int argc, char* argv[]) {
	int opt;
	// Set defaults
	if (argc < 2)
		usage(-1);
	strcpy(pdip, argv[1]);
	strcpy(pdport, "57035");
	strcpy(asip, argv[1]);
	strcpy(asport, "58035");
	while ((opt = getopt(argc - 1, &argv[1], ":d:n:p:")) != -1) {
		// Checks if it's an opt with no argument
		if (optarg == NULL)
			usage(opt);
		// Checks if it's an opt followed by another opt
		if (*optarg == '-')
			usage(opt);
		switch (opt) {
		case 'd':
			strcpy(pdport, optarg);
			break;
		case 'n':
			strcpy(asip, optarg);
			break;
		case 'p':
			strcpy(asport, optarg);
			break;
		case '?':
			usage(opt);
		}
	}
}

void processInput() {
	// Setup the select
	fd_set inputs, cpyinputs;
	int retval;
	FD_ZERO(&inputs);
	FD_SET(STDIN_FILENO, &inputs);
	FD_SET(fd, &inputs);

	char cmd[32];
	char message[BUFF_SIZE];

	while (1) {
		cpyinputs = inputs;
		retval = select(FD_SETSIZE, &cpyinputs, (fd_set*)NULL, (fd_set*)NULL, NULL);

		switch (retval) {
		case 0:
			// Timeout
			break;
		case -1:
			panic("Select error.", 1);
			break;
		default:
			if (FD_ISSET(STDIN_FILENO, &cpyinputs)) {
				// INPUT IN STDIN
				scanf("%s", cmd);
				if (!strcmp(cmd, "reg")) {
					// Registration command
					if (uid != -1)
						printf("Registration has already been done. Current user: %05d\n", uid);
					else {
						scanf(" %d %s", &uid, pass);
						sprintf(message, "REG %05d %s %s %s\n", uid, pass, pdip, pdport);
						sendMessage(message, as_addr);
					}
				}
				else if (!strcmp(cmd, "exit")) {
					// Exit command
					if (uid != -1) {
						sprintf(message, "UNR %04d %s\n", uid, pass);
						sendMessage(message, as_addr);
						receiveMessage();
						processBuffer();
					}
					closeConnection();
					exit(0);
				}
				else {
					printf("---------------------- HELP ----------------------\n");
					printf("Invalid command. Please use one of the following:\n");
					printf("\treg UID PASSWORD\n");
					printf("\texit\n");
					printf("--------------------------------------------------\n");
					// skip the rest of the line
					int ch;
					while((ch = getchar()) != '\n' && (ch != EOF));
				}
			}
			else if (FD_ISSET(fd, &cpyinputs)) {
				// MESSAGE IN THE SOCKET
				receiveMessage();
				processBuffer();
			}
			else
				panic("Select has an unknown fd.", -1);
		}
	}
}

void processBuffer() {
	char cmd[4];
	char status[4];
	sscanf(buffer, "%s", cmd);
	if (!strcmp(cmd, "RRG")) {
		// Registration status
		sscanf(buffer, "RRG %s", status);
		if (!strcmp(status, "OK"))
			printf("Registration successful.\n");
		else if (!strcmp(status, "NOK")) {
			uid = -1;
			printf("Registration failed.\n");
		}
	}
	else if (!strcmp(cmd, "RUN")) {
		// Unregistration status
		sscanf(buffer, "RUN %s", status);
		if (!strcmp(status, "OK"))
			printf("Unregister successful.\n");
		else if (!strcmp(status, "NOK"))
			printf("Unregister failed.\n");
	}
	else if (!strcmp(cmd, "VLC")) {
		// Validation code
		int received_uid, vc;
		char fop;
		char filename[32];
		char message[BUFF_SIZE];
		sscanf(buffer, "VLC %d %d %c %s", &received_uid, &vc, &fop, filename);
		if (received_uid != uid) {
			sprintf(message, "RVC %05d NOK\n", received_uid);
			sendMessage(message, as_callback);
		}
		else {
			sprintf(message, "RVC %05d OK\n", uid);
			sendMessage(message, as_callback);
			printf("VC=%04d, ", vc);
			switch (fop) {
			case 'L':
				printf("list\n");
				break;
			case 'X':
				printf("remove user\n");
				break;
			case 'U':
				printf("upload: %s\n", filename);
				break;
			case 'R':
				printf("retrieve: %s\n", filename);
				break;
			case 'D':
				printf("delete: %s\n", filename);
				break;
			default:
				printf("unknown operation (%c)\n", fop);
			}
		}
	}
	else
		printf("Unrecognized message in buffer: \"%s\"\n", buffer);
}

int main(int argc, char* argv[]) {
	parseArgs(argc, argv);
	//printArgs();
	startConnection();
	processInput();
}
