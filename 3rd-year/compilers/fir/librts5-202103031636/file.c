#include "kernel.h"
#include "file.h"
#include "string.h"

/**
 * Print C-style string to output channel specified by descriptor.
 * @param fd output file descriptor
 * @param str string to be written
 */
void fprints(int fd, const char *str) {
  if (str == 0) return; // NULL pointer
  sys_write(fd, str, strlen(str));
}

/**
 * Read single byte (char) from input channel specified by descriptor.
 * @param input file descriptor
 */
char freadb(int fd) {
  char character = '\0';
  sys_read(fd, (void *)&character, 1UL);
  return character;
}
