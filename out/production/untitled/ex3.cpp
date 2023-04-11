// blockless if, blockless loop, embed inc/dec, security
#include <iostream>
#include <cstdlib>
using namespace std;


void bloodHound(int water, int bottle, int screenTv, int cupOfCoffee) {
    if (water)
        cout << "hello" << endl;

    if (bottle) {
        cout << "hello" << endl;
    }

    for (screenTv = 0; screenTv < 10; screenTv++)
        cout << "hello" << endl;

    for (cupOfCoffee = 0; cupOfCoffee < screenTv; cupOfCoffee++) {
        cout << "hello" << endl;

    }
}

int main () {
    int num;
    string wordToNum = "2023";
    num = atoi(wordToNum);

    bloodHound(num++,2,3,4);

    return 0;
}