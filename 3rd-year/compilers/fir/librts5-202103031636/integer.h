#ifndef __RTS_INTEGER_H__
#define __RTS_INTEGER_H__

/**
 * Numbers represented as strings.
 * Assumes that the string is clean (may have starting spaces).
 * Negative numbers are supported.
 */
int atoi(const char *s);

const char *itoa(int value, char *str, unsigned long length);
void printi(int i);

int readi();

#endif
