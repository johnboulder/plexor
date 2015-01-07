package com.wherethisgo.plexor;

public class Block
{
	public static String empty = "E";
	private final String  board[][];
	private       Boolean won;
	private       String  winner;

	Block()
	{
		board = new String[3][3];
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				board[i][j] = empty;
			}
		}

		won = false;
		winner = empty;
	}

	public String[][] getBoard()
	{
		return board;
	}

	public boolean getWinStatus()
	{
		return won;
	}

	public String getWinner()
	{
		return winner;
	}

	public String checkForWin()
	{
		if (board[0][0] == "X" && board[0][1] == "X" && board[0][2] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}
		if (board[1][0] == "X" && board[1][1] == "X" && board[1][2] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}
		if (board[2][0] == "X" && board[2][1] == "X" && board[2][2] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}

		if (board[0][0] == "X" && board[1][1] == "X" && board[2][2] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}
		if (board[2][0] == "X" && board[1][1] == "X" && board[0][2] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}

		if (board[0][0] == "X" && board[1][0] == "X" && board[2][0] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}
		if (board[0][1] == "X" && board[1][1] == "X" && board[2][1] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}
		if (board[0][2] == "X" && board[1][2] == "X" && board[2][2] == "X")
		{
			this.won = true;
			this.winner = "X";
			return "X";
		}

		if (board[0][0] == "O" && board[0][1] == "O" && board[0][2] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}
		if (board[1][0] == "O" && board[1][1] == "O" && board[1][2] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}
		if (board[2][0] == "O" && board[2][1] == "O" && board[2][2] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}

		if (board[0][0] == "O" && board[1][1] == "O" && board[2][2] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}
		if (board[2][0] == "O" && board[1][1] == "O" && board[0][2] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}

		if (board[0][0] == "O" && board[1][0] == "O" && board[2][0] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}
		if (board[0][1] == "O" && board[1][1] == "O" && board[2][1] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}
		if (board[0][2] == "O" && board[1][2] == "O" && board[2][2] == "O")
		{
			this.won = true;
			this.winner = "O";
			return "O";
		}
		// Necessary for when a value is removed to reset a win status on the block in case the placement that was undone won the block
		if (this.won)
		{
			this.won = false;
			this.winner = empty;
		}

		return null;
	}

	public void setSquare(final int row, final int col, final String value) throws CannotPlaceValueException
	{
		if (squareAvailable(row, col))
		{
			board[row][col] = value;
		} else
		{
			throw new CannotPlaceValueException("Exception: Value has already been placed at that position");
		}
	}

	public void clearSquare(final int row, final int col)
	{
		board[row][col] = empty;
	}

	public String getSquare(final int row, final int col)
	{
		return board[row][col];
	}

	public boolean squareAvailable(int row, int col)
	{
		if (board[row][col] != empty)
		{
			return false;
		}

		return true;
	}

}
