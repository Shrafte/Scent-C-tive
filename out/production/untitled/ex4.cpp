#include <iostream>
#include <fstream>
#include "gamecomm.h"
#include <time.h>
#include <stdlib.h>
#include <vector>

using namespace std;

const int maxturn = 1;
const int minturn = -1;
const int blank = 0;
const int VS = -1000000;
const int VL = 1000000;

struct board
{
	int m[8][8];
	int r, c, turn, h;
	board(int n[][8], int row = 8, int column = 8, int t = 1)
	{
		for (int k = 0; k < 8; k++)
			for (int l = 0; l < 8; l++)
				m[k][l] = n[k][l];
		r = row;
		c = column;
		turn = t;
	}
};

typedef board *state_t;
typedef int stateType;

void swap(state_t &a, state_t &b)
{
	state_t tmp = a;
	a = b;
	b = tmp;
	return;
}

int other(int k)
{
	return k * -1;
}

bool legal(state_t s, int row, int column, int turn)
{

	if (s->m[row][column] != 0)
		return false;

	int r, c;

	// check up the column - is there a capture from above?
	for (r = row - 1; r >= 0 && s->m[r][column] == other(turn); r--)
		;
	if (r < row - 1 && r >= 0 && s->m[r][column] == turn)
	{
		return true;
	}
	// check down the column
	for (r = row + 1; r < 8 && s->m[r][column] == other(turn); r++)
		;
	if (r > row + 1 && r < 8 && s->m[r][column] == turn)
	{
		return true;
	}
	// check to the left in the row
	for (c = column - 1; c >= 0 && s->m[row][c] == other(turn); c--)
		;
	if (c < column - 1 && c >= 0 && s->m[row][c] == turn)
	{
		return true;
	}
	// check to the right in the row
	for (c = column + 1; c < 8 && s->m[row][c] == other(turn); c++)
		;
	if (c > column + 1 && c < 8 && s->m[row][c] == turn)
	{
		return true;
	}
	// check NW
	for (c = column - 1, r = row - 1; c >= 0 && r >= 0 && s->m[r][c] == other(turn); c--, r--)
		;
	if (c < column - 1 && c >= 0 && r >= 0 && s->m[r][c] == turn)
	{
		return true;
	}
	// check NE
	for (c = column + 1, r = row - 1; c < 8 && r > 0 && s->m[r][c] == other(turn); c++, r--)
		;
	if (c > column + 1 && c < 8 && r >= 0 && s->m[r][c] == turn)
	{
		return true;
	}
	// check SE
	for (c = column + 1, r = row + 1; c < 8 && r < 8 && s->m[r][c] == other(turn); c++, r++)
		;
	if (c > column + 1 && c < 8 && r < 8 && s->m[r][c] == turn)
	{
		return true;
	}

	// check SW
	for (c = column - 1, r = row + 1; c >= 0 && r < 8 && s->m[r][c] == other(turn); c--, r++)
		;
	if (c < column - 1 && c >= 0 && r < 8 && s->m[r][c] == turn)
	{
		return true;
	}

	return false;
}

// checks if the given state is a terminal state
bool isterminal(state_t state)
{
	int row, column;

	for (row = 0; row < 8; row++)
	{
		for (column = 0; column < 8; column++)
		{
			if (legal(state, row, column, maxturn))
			{
				return false;
			}
		}
	}
	return true;
}

void printboard(int m[][8])
{
	for (int r = 0; r < 8; r++)
	{
		for (int c = 0; c < 8; c++)
		{
			if (m[r][c] == minturn)
				cout << 'b';
			else if (m[r][c] == maxturn)
				cout << 'w';
			else
				cout << '-';
			cout << " ";
		}
		cout << endl;
	}
	cout << endl;
}

// flip pieces after move is made on new board
void flipPieces(state_t &s, int row, int col, int turn)
{
	int r, c, i, j;

	// up
	for (r = row - 1; r >= 0 && s->m[r][col] == other(turn); r--)
		;
	if (r >= 0 && s->m[r][col] == turn)
	{
		for (i = row - 1; i > r; i--)
		{
			s->m[i][col] = turn;
		}
	}

	// down
	for (r = row + 1; r < 8 && s->m[r][col] == other(turn); r++)
		;
	if (r < 8 && s->m[r][col] == turn)
	{
		for (i = row + 1; i < r; i++)
		{
			s->m[i][col] = turn;
		}
	}

	// left
	for (c = col - 1; c >= 0 && s->m[row][c] == other(turn); c--)
		;
	if (c >= 0 && s->m[row][c] == turn)
	{
		for (i = col - 1; i > c; i--)
		{
			s->m[row][i] = turn;
		}
	}

	// right
	for (c = col + 1; c < 8 && s->m[row][c] == other(turn); c++)
		;
	if (c < 8 && s->m[row][c] == turn)
	{
		for (i = col + 1; i < c; i++)
		{
			s->m[row][i] = turn;
		}
	}

	// NW
	for (c = col - 1, r = row - 1; c >= 0 && r >= 0 && s->m[r][c] == other(turn); c--, r--)
		;
	if (c >= 0 && r >= 0 && s->m[r][c] == turn)
	{
		for (i = col - 1, j = row - 1; i > c && j > r; i--, j--)
		{
			s->m[j][i] = turn;
		}
	}

	// NE
	for (c = col + 1, r = row - 1; c < 8 && r >= 0 && s->m[r][c] == other(turn); c++, r--)
		;
	if (c < 8 && r >= 0 && s->m[r][c] == turn)
	{
		for (i = col + 1, j = row - 1; i < c && j > r; i++, j--)
		{
			s->m[j][i] = turn;
		}
	}

	// SE
	for (c = col + 1, r = row + 1; c < 8 && r < 8 && s->m[r][c] == other(turn); c++, r++)
		;
	if (c < 8 && r < 8 && s->m[r][c] == turn)
	{
		for (i = col + 1, j = row + 1; i < c && j < r; i++, j++)
		{
			s->m[j][i] = turn;
		}
	}

	// SW
	for (c = col - 1, r = row + 1; c >= 0 && r < 8 && s->m[r][c] == other(turn); c--, r++)
		;
	if (c >= 0 && r < 8 && s->m[r][c] == turn)
	{
		for (i = col - 1, j = row + 1; i > c && j < r; i--, j++)
		{
			s->m[j][i] = turn;
		}
	}
}

// creates a new board if theres a legal move
void expand(state_t state, vector<state_t> &successor, int turn)
{
	printboard(state->m);
	int row, col;

	for (row = 0; row < 8; row++)
	{
		for (col = 0; col < 8; col++)
		{
			if (legal(state, row, col, turn))
			{
				cout << "Legal move at position: (" << row << "," << col << ")" << endl;
				board *newBoard = new board(state->m, row, col, turn);
				newBoard->m[row][col] = turn;
				flipPieces(newBoard, row, col, turn);
				successor.push_back(newBoard);
			}
		}
	}
	cout << endl
		 << endl;
}

// counts num of legal moves for player with turn
int numLegalMoves(state_t s, int turn)
{
	int count = 0;
	for (int i = 0; i < 8; i++)
	{
		for (int j = 0; j < 8; j++)
		{
			if (legal(s, i, j, turn))
			{
				count++;
			}
		}
	}
	return count;
}

// counts corners occupied by the player
int numCorners(state_t s, int turn)
{
	int count = 0;
	if (s->m[0][0] == turn)
	{
		count++;
	}
	if (s->m[0][7] == turn)
	{
		count++;
	}
	if (s->m[7][0] == turn)
	{
		count++;
	}
	if (s->m[7][7] == turn)
	{
		count++;
	}
	return count;
}

// returns goodness score
int eval(state_t s)
{
	int i, j;
	// stores eval scores for each spot on board
	int spotScore[8][8] = {
		{50, -20, 10, 5, 5, 10, -20, 50},
		{-20, -30, -5, -5, -5, -5, -30, -20},
		{10, -5, 1, 1, 1, 1, -5, 10},
		{5, -5, 1, 0, 0, 1, -5, 5},
		{5, -5, 1, 0, 0, 1, -5, 5},
		{10, -5, 1, 1, 1, 1, -5, 10},
		{-20, -30, -5, -5, -5, -5, -30, -20},
		{50, -20, 10, 5, 5, 10, -20, 50}};

	int maxScore = 0;
	int minScore = 0;
	int center = 0; // for if player occupies a center square
	for (i = 0; i < 8; i++)
	{
		for (j = 0; j < 8; j++)
		{
			if (s->m[i][j] == maxturn)
			{
				maxScore += spotScore[i][j];
			}
			else if (s->m[i][j] == minturn)
			{
				minScore += spotScore[i][j];
			}
			if ((i == 3 || i == 4) && (j == 3 || j == 4))
			{
				if (s->m[i][j] == maxturn)
				{
					center += 10;
				}
				else if (s->m[i][j] == minturn)
				{
					center -= 10;
				}
			}
		}
	}
	int legalMoves = numLegalMoves(s, maxturn) - numLegalMoves(s, minturn);
	int ownCorner = numCorners(s, maxturn) - numCorners(s, minturn);
	int totalDiff = maxScore + minScore;
	if (totalDiff != 0)
	{
		totalDiff = (maxScore - minScore) / totalDiff;
	}
	return (10 * legalMoves) + (1000 * ownCorner) + (5 * totalDiff) + maxScore - minScore + center; // total score based on all factors
}

int max(int a, int b)
{
	return a > b ? a : b;
}

// add 2 vars to minimax search: alpha and beta from parent
// At each level, either alpbha or beta from parent is passed, and compared with its counterpart
// at current level, ex., alpha of parent vs beta of current
int alphaBeta(state_t state, int maxDepth, int curDepth, int alpha, int beta, state_t &finalBoard)
{
	if (curDepth == maxDepth || isterminal(state)) // CUTOFF test
	{
		return eval(state); // eval returns the heuristic value of state
	}
	vector<state_t> successor;
	int turn;
	if (curDepth % 2 == 0) // This is a MAX node as MAx has depth of: 0, 2, 4, 6, ...
		turn = maxturn;
	else
		turn = minturn;

	expand(state, successor, turn); // find all successors of state
	if (turn == maxturn)			// This is a MAX node .
	{

		for (int k = 0; k < successor.size(); k++)
		{
			// recursively find the value of each successor
			int curvalue = alphaBeta(successor[k], maxDepth, curDepth + 1, alpha, beta, finalBoard);

			if (curvalue > alpha)
			{
				alpha = curvalue;
				if (curDepth == 0)
				{
					finalBoard = successor[k];
				}
			}

			if (alpha >= beta)
			{
				break;
			}
		}
		return alpha;
	}
	else // A MIN node i.e., turn==minturn
	{

		for (int k = 0; k < successor.size(); k++)
		{
			// recursively find the value of each successor
			int curvalue = alphaBeta(successor[k], maxDepth, curDepth + 1, alpha, beta, finalBoard);

			if (curvalue < beta)
			{
				beta = curvalue;
				if (curDepth == 0)
				{
					finalBoard = successor[k];
				}
			}

			if (alpha >= beta)
			{
				break;
			}
		}
		return beta;
	}
}

int main()
{
	int n[8][8], row, column;
	getGameBoard(n);
	state_t s = new board(n), succ = nullptr;
	state_t finalBoard = s;
	alphaBeta(s, 3, 0, VS, VL, finalBoard);
	cout << "Move being placed at position: (" << finalBoard->r << "," << finalBoard->c << ")" << endl;
	putMove(finalBoard->r, finalBoard->c);
	// system("pause");
}