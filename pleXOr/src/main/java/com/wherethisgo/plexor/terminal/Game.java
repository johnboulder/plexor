package com.wherethisgo.plexor.terminal;

import java.util.Random;
import java.util.Scanner;


public class Game
{
	private Block   board[][];
	private String  serializedBoard[]/* = new String[81]*/;
	private Block   greaterBoard/* = new Block()*/;
	private Block   currentBlock/* = board[1][1]*/; //sets the starting block
	private String  currentPlayer/* = "0"*/;
	private Integer currentBlockCol/* = 1*/;
	private Integer currentBlockRow/* = 1*/;
	private Boolean xIsEven/* = false*/, oIsEven/* = false*/;

	Game()
	{
		board = new Block[3][3];
		serializedBoard = new String[81];
		greaterBoard = new Block();
		this.currentPlayer = "0";
		currentBlockCol = 1;
		currentBlockRow = 1;
		xIsEven = false;
		oIsEven = false;
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				board[i][j] = new Block();
			}
		}

		currentBlock = board[1][1]; //sets the starting block
	}

	//TODO create an iterateable linked list that keeps track of the moves
	public static void main(String args[])
	{
		String player, firstPlayer, secondPlayer;
		Game game = new Game();

		//		if(game.determineFirstPlayer() && game.xIsEven)
		//		{
		//			firstPlayer = "X";
		//			secondPlayer = "O";
		//		}
		//		else
		//		{
		//			firstPlayer = "O";
		//			secondPlayer = "X";
		//		}

		firstPlayer = "X";
		secondPlayer = "O";

		player = firstPlayer;
		Scanner reader = new Scanner(System.in);

		while (!game.gameWon())
		{
			int col, row;

			// alert player that it is their move
			System.out.println("Player " + player + "'s turn");
			System.out.print("Input col: ");
			// get user input
			col = reader.nextInt();
			System.out.print("Input row: ");
			// get user input
			row = reader.nextInt();

			// Loop while player placement choice is invalid
			// When the placement is valid
			// - Set the square
			// - Check for a win in the block
			// - Update everything
			// - Determine the next block.
			// - Handle setting the next block
			while (!game.setSquareValue(row, col, player))
			{
				System.out.println("That position is already taken.");
				System.out.print("Input col: ");
				col = reader.nextInt();
				System.out.print("Input row: ");
				row = reader.nextInt();
			}
			game.printBoard();

			if (player == firstPlayer)
			{
				player = secondPlayer;
			} else
			{
				player = firstPlayer;
			}

			while (!game.setCurrentBlock(row, col))
			{
				// Start process of giving player the ability to select the next block
				System.out.println("Player " + player + " must select the next block");
				System.out.print("Input col: ");
				col = reader.nextInt();
				System.out.print("Input row: ");
				row = reader.nextInt();
			}
		}
		reader.close();


	}

	public void printBoard()
	{
		for (int i = 0; i < 3; i++) // loops for each block row
		{
			for (int j = 0; j < 3; j++) // loops for each row in the array
			{
				System.out.print("|");
				// loops for each block per row/column again since we can't make a loop from 0-8 (each block is labeled from 0-2 in columns)
				for (int k = 0; k < 3; k++)
				{
					String localBlock[][] = board[i][k].getBoard();
					for (int l = 0; l < 3; l++) // hits 0,1,2 - 9 times each
					{
						System.out.print(localBlock[j][l] + " ");
					}

					System.out.print("|");
				}
				System.out.println();
			}
			System.out.print("|--------------------|");
			System.out.println();
		}

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				System.out.print(greaterBoard.getSquare(i, j));
			}

			System.out.println();
		}
	}

	public Block getCurrentBlock()
	{
		return currentBlock;
	}

	public void serializeBoard()
	{

	}

	/**
	 * Used for switching to the next block the game is to be played in
	 */
	public boolean setCurrentBlock(int row, int col)
	{
		if (!board[row][col].getWinStatus())
		{
			currentBlock = board[row][col];
			currentBlockCol = col;
			currentBlockRow = row;
			return true;
		}

		return false;
	}

	// sets our current square to the value
	// checks if a win has occured in the block
	// if it has, call the set block method to set the greater block to the winner's symbol
	public boolean setSquareValue(int row, int col, String value)
	{
		try
		{
			currentBlock.setSquare(row, col, value);
			currentBlock.checkForWin();

			if (currentBlock.getWinStatus())
			{
				setBlockValue();
			}

			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			return false;
		}
	}

	/**
	 * Sets the value of the current block.
	 * Uses the greaterBoard variable and sets the value at the current row and column to the winner in the block
	 */
	public boolean setBlockValue()
	{
		try
		{
			greaterBoard.setSquare(currentBlockRow, currentBlockCol, currentBlock.getWinner());
			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			return false;
		}
	}

	//called after a player places an X or an O
	public void checkBlocksForWin()
	{
		String winner = currentBlock.checkForWin();
		if (winner != null)
		{
			try
			{
				greaterBoard.setSquare(currentBlockRow, currentBlockCol, winner);
			}
			catch (CannotPlaceValueException cpve)
			{
				System.out.println("Choose another position.");
			}
		}
	}

	public boolean gameWon()
	{
		if (greaterBoard.checkForWin() == "X" || greaterBoard.checkForWin() == "O")
		{
			return true;
		}

		return false;
	}

	/**
	 * Returns true for even
	 */
	public boolean determineFirstPlayer()
	{
		//TODO get the user to input even or odd guesses
		Random rand = new Random();
		int i = rand.nextInt() % 2;

		if (i == 0)
		{
			return true;
		} else
		{
			return false;
		}
	}
}
