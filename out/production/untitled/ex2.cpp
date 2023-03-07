#include <iostream>
using namespace std;

int sum(int a, int b, int c, int d, int e, int f, int g) {
    int sum = a + b + c + d + e + f + g;
    return sum;
}

int main() {
    int answer;

    cout << "calculating sum" << endl;;
    answer = sum(1,2,3,4,5,6,7);
    cout << "sum is "<< answer << endl;

    return 0;
}