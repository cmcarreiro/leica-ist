# Sistemas Operativos 19/20
# Catarina Carreiro   92438
# Cristiano Clemente  92440


CC   = gcc
LD   = gcc
CFLAGS =-Wall -std=gnu99 -I../ -g
LDFLAGS=-lm -pthread

.PHONY: all clean run

all: tecnicofs

tecnicofs: lib/bst.o fs.o main.o
	$(LD) $(CFLAGS) $(LDFLAGS) -o tecnicofs-nosync lib/bst.o fs-nosync.o main-nosync.o
	$(LD) $(CFLAGS) $(LDFLAGS) -o tecnicofs-mutex lib/bst.o fs-mutex.o main-mutex.o
	$(LD) $(CFLAGS) $(LDFLAGS) -o tecnicofs-rwlock lib/bst.o fs-rwlock.o main-rwlock.o

lib/bst.o: lib/bst.c lib/bst.h
	$(CC) $(CFLAGS) -o lib/bst.o -c lib/bst.c

fs.o: fs.c fs.h lib/bst.h
	$(CC) $(CFLAGS) -o fs-nosync.o -c fs.c
	$(CC) $(CFLAGS) -DMUTEX -o fs-mutex.o -c fs.c
	$(CC) $(CFLAGS) -DRWLOCK -o fs-rwlock.o -c fs.c

main.o: main.c fs.h lib/bst.h
	$(CC) $(CFLAGS) -o main-nosync.o -c main.c
	$(CC) $(CFLAGS) -DMUTEX -o main-mutex.o -c main.c
	$(CC) $(CFLAGS) -DRWLOCK -o main-rwlock.o -c main.c

clean:
	@echo Cleaning...
	rm -f lib/*.o *.o tecnicofs-nosync tecnicofs-mutex tecnicofs-rwlock

run: tecnicofs
	./tecnicofs
