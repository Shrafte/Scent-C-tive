#include <iostream>

int main() {
    int x = 0;
    int y = 0;
    int z = 0;

    for (int i = 0; i < 10; i++) {
        x += i;
    }

    // dead code:
    y = x + 1;
    z = y + 1;

    // dead code:
    int a = 0;
    a = a + 1;

    // dead code:
    int b = 0;
    b = b + 1;

    return 0;
}