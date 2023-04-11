// long param, continue, break
#include <iostream>
using namespace std;

void sumAll(int numOne, int numTwo, int numThree, int numfour, int numFive, int a) {
    int sum = numOne + numTwo + numThree + numfour + numFive;
    cout << "Sum is: " << sum << endl;
}

int main() {
    int answer;

    for (int i = 0; i < 10; i++) {
        if (i == 4) {
            break;
        }
        cout << i << endl;
    }

    for (int i = 0; i < 10; i++) {
        if (i == 4) {
            continue;
        }
        cout << i << endl;
    }

    sumAll(1, 2, 3, 4, 5, 6);

    return 0;
}