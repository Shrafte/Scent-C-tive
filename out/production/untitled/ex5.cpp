#include <iostream>
#include <cstdlib>
#include <array>
#include <time.h>
#include <bits/stdc++.h>
using namespace std;


int subSum(int n, int arr[]) {
    int W = 1000; // sum we are trying to add to
    int subset[n+1][W+1]; // 2D array to hold summed values

    for (int w = 0; w <= W; w++) { // initalizing values
        subset[0][w] = 0;
    }

    for (int i = 1; i <= n; i++) { // filling subset table
        for (int w = 1; w <= W; w++) {
            if (arr[i] > w) {
                subset[i][w] = subset[i - 1][w];
            } else {
                subset[i][w] = max(subset[i - 1][w], arr[i] + subset[i - 1][w - arr[i]]);
            }
        }
    }


    return subset[n][W]; // return closest value to target sum
};

int subSumApprox(int n, int arr[]) {
    sort(arr, arr + n, greater<int>()); // sort array descending order
    int sum = 1000; // target sum
    int curr = 0; // current sum of numbers at a time


    for (int i = 0; i < n; i++) { // add numbers in descending order until one will make sum go over 1000, return current sum
        if ((curr + arr[i]) > sum) {
            return curr;
        } else {
            curr = curr + arr[i];
        }
    }



    return curr;
};


int main() {
    int arr[20]; // array to hold random numbers

    srand (time(NULL)); // to initialize random seed

    for (int i = 0; i < 20; i++) { // filling array with random numbers <= 600
        arr[i] = (rand() % 600) + 1;
    }

    int num = subSum(20, arr);
    int numApprox = subSumApprox(20, arr);

    cout << "Dynamic: " << num << endl;
    cout << "Greedy: " << numApprox << endl;



    return 0;
}