package com.wherethisgo.plexor.terminal;


public class Block
{

	private String  board[][];
	private Boolean won;
	private String  winner;

	Block()
	{
		board = new String[3][3];
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				board[i][j] = "0";
			}
		}

		won = false;
		winner = null;
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

	public String getSquare(final int row, final int col)
	{
		return board[row][col];
	}

	public boolean squareAvailable(int row, int col)
	{
		if (board[row][col] != "0")
		{
			return false;
		}

		return true;
	}

}
