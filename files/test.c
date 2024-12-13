#include<stdio.h>
int a[10];
const int b[2]={1,2};
char aa[10];
const char bb[10]="hello";
int func1(int a[],int b,char c[],char d){
    printf("%d,%d,%c,%c\n",a[1],b,c[1],d);
    a[0]=d;
    a[1]=b;
    c[1]=d;
    return 0;
}
int func2(int a[],int b,char c[],char d){
    func1(a,b,c,d);
    printf("%d,%d,%c,%c\n",a[1],b,c[1],d);
    return 0;
}
int main()
{
    int c[10];
    char cc[10];
    const int d[2]={1,2};
    const char dd[3]="1";
    func2(a,a[1],aa,aa[1]);
    func2(c,c[1],cc,cc[1]);
    func2(d,d[1],dd,dd[1]);

    return 0;
}