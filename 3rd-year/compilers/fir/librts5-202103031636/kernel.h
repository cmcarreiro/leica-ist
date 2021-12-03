#ifndef __RTS_KERNEL_H__
#define __RTS_KERNEL_H__

// These are basic file-related syscalls: see kernel.asm
// The declarations used here are Linux-friendly without C library typedefs (but compatible).
// For more information (manual pages): man 2 name-of-call-without-sys_-prefix
// Example: man 2 read
long int sys_read(int fd, void *buffer, unsigned long count);
long int sys_write(int fd, const void *buffer, unsigned long count);

// These two declarations are actually unnecessary (they are not used by the RTS itself).
int sys_open(const char *pathname, int flags, unsigned int mode);
int sys_close(int fd);

#endif
