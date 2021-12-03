all: pd user as fs

pd: pd.c pd.h
	gcc pd.c -o pd

user: user.c user.h file_contents.c file_contents.h
	gcc file_contents.c user.c -o user

as: as.c as.h as_storage.c as_storage.h
	gcc -o as as_storage.c as.c 

fs: fs.c fs.h fs_storage.c fs_storage.h file_contents.c file_contents.h
	gcc -o fs file_contents.c fs_storage.c fs.c

clean:
	rm pd user as fs
