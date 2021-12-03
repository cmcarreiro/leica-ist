#include "file.h"
#include "string.h"

/**
 * Trivial C-style string length function.
 */
int strlen(const char *str) {
  int length = 0;
  while (str[length] != '\0')
    length++;
  return length;
}

/**
 * Print C-style string to stdout (descriptor 1).
 * @param str string to be written
 */
void prints(const char *str) {
  fprints(1, str);
}

/**
 * Simple new-line output function.
 */
void println() {
  prints("\n");
}

/**
 * Print spaces.
 * @param count the number of spaces to print.
 */
void printsp(unsigned long count) {
  while (count-- != 0)
    prints(" ");
}

/**
 * Read single byte (char) from stdin (descriptor 0).
 */
char readb() {
  return freadb(0);
}

/* DAVID: probably buggy!!!! */
const char *readln(char *buffer, unsigned long size) {
  if (size == 0) return 0; // error
  for (unsigned long ix = 0; ix < size; ix++) {
    buffer[ix] = '\0';
    char c = readb();
    if (c == 0)
      return 0; /* error */
    if (c == '\n')
      return buffer;
    buffer[ix] = c;
  }
  buffer[size - 1] = '\0'; /* paranoia */
  return buffer;
}
