#include<stdio.h>
const int constIntArray[3] = {10, 20, 30};
const char constCharArray[5] = {'A', 'B', 'C', 'D', 'E'};
const char constCharArray2[5] = "abc";
int intArray[5];
char charArray[5];

int func_with_param(int a, char b, int arr[], char str[]) {
    int sum = a + b + arr[0] + str[0];
    return sum;
}

int main() {
    intArray[0] = constIntArray[0];
    intArray[1] = constIntArray[1];
    intArray[2] = constIntArray[2];
    intArray[3] = intArray[0] + intArray[1];
    intArray[4] = intArray[3] + intArray[2];

    // Using unary '-' operator
    intArray[0] = -intArray[0];

    // Using unary '+' operator
    intArray[0] = +intArray[0];

    // Using '/' and '%'
    intArray[1] = intArray[3] / intArray[2];
    intArray[2] = intArray[3] % intArray[2];


    // Copy constCharArray to charArray and modify to sum over 128
    charArray[0] = constCharArray[0] + constCharArray[1] + constCharArray[2] + constCharArray[3] + constCharArray[4];

    int result = func_with_param(intArray[0], charArray[0], intArray, charArray);
    return 0;
}