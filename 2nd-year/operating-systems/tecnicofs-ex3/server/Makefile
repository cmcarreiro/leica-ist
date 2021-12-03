# Sistemas Operativos 19/20
# Catarina Carreiro   92438
# Cristiano Clemente  92440

CFLAGS =-Wall -std=gnu99 -I../ -g
LDFLAGS=-lm -pthread

.PHONY: all clean

all: tecnicofs

tecnicofs: lib/bst.o lib/sync.o fs.o tecnicofs.o
	gcc $(CFLAGS) $(LDFLAGS) -o tecnicofs tecnicofs.o fs.o lib/bst.o lib/sync.o

tecnicofs.o: tecnicofs.c
	gcc $(CFLAGS) -o tecnicofs.o -c tecnicofs.c

fs.o: fs.c fs.h
	gcc $(CFLAGS) -o fs.o -c fs.c

lib/bst.o: lib/bst.c lib/bst.h
	gcc $(CFLAGS) -o lib/bst.o -c lib/bst.c

#lib/inodes.o: lib/inodes.c lib/inodes.h
#	gcc $(CFLAGS) -o lib/inodes.o -c lib/inodes.c

lib/sync.o: lib/sync.c lib/sync.h
	gcc $(CFLAGS) -o lib/sync.o -c lib/sync.c

clean:
	@echo Cleaning...
	rm -f lib/*.o *.o tecnicofs
