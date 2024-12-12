#include<stdio.h>
const int _a = 3+2+-+2%2/1;
const int a=3+(1*2)/2*_a;
const int arrayA[2] = {1,2};
const int array_b[2] = {1,arrayA[0]};
int array_c[2] = {1,a};
int array_d[2] = {1,2};
int main(){
	int x;
	x=getint();
	printf("what you enter in is:%d\n",x);
	printf("_a=%d\n",_a);
	printf("array_c[0] is:%d\n",array_c[0]);
	printf("array_d[0] is:%d\n",array_d[0]);
	printf("array_d[1] is:%d\n",array_d[1]);
	int i=2;
	int ext;
	for(;i<x;){
		ext=x%i;
		if(ext==0){
			x=x/i;
			printf("%d ",i);
		}
		else{
			i=i+1;
		}

	}	printf("%d",x);
	printf("\nOver");
	return 0;
}