#include <stdio.h>

int main() {
    int num_cidadaos, num_supermercados;
    if(!scanf("%d %d", &num_cidadaos, &num_supermercados))
		return -1;
	if(!scanf("%d %d", &num_cidadaos, &num_supermercados))
		return -1;
	printf("%d\n", num_cidadaos <= num_supermercados ? num_cidadaos : num_supermercados);
	return 0;
}