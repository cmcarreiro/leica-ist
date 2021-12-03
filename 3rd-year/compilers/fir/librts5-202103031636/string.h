#ifndef __RTS_STRING_H__
#define __RTS_STRING_H__

int strlen(const char *str);

void prints(const char *s);
void printsp(unsigned long count);
void println();

char readb();
const char *readln(char *buffer, unsigned long size);

#endif
