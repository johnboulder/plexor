package com.wherethisgo.plexor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 *
 */
public class MatchLocal extends MainActivity implements OnTurnBasedMatchUpdateReceivedListener
{

	final String LETTER_O = "O";
	final String LETTER_X = "X";
	// private String grid[][]= new String [9][9];
	EditText ViewArray[][] = new EditText[9][9];
	Drawable DEFAULT_COLOR;
	/**
	 * End Game related variables
	 */

	// private boolean GridDiggable = true;
	Random rand = new Random();
	/**
	 * Game related variables
	 */
	private Block   board[][];
	private String  serializedBoard[]/* = new String[81] */;
	private Block   greaterBoard/* = new Block() */;
	private Block   currentBlock/* = board[1][1] */; // sets the starting block
	// private String currentPlayer/* = "0"*/;
	private Integer currentBlockCol/* = 1 */;
	private Integer currentBlockRow/* = 1 */;
	private Integer selectedRow = null;
	private Integer selectedCol = null;
	private Integer lastRow     = null;
	private Integer lastCol     = null;
	private boolean nextTurnSelectABlock = false;
	private boolean gameFinished         = false;
	private String player, firstPlayer, secondPlayer;
	private boolean multiplayerMatch = false;
	private Context        context;
	// This is the current match we're in; null if not loaded
	private TurnBasedMatch mMatch;
	private       boolean         isDoingTurn     = false;
	/**
	 *
	 */
	private final OnTouchListener onTouchListener = new OnTouchListener()
	{
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			int action = event.getActionMasked();
			if (!gameWon() && v.isEnabled() && !gameFinished && action == MotionEvent.ACTION_DOWN && isDoingTurn)
			{
				for (int i = 0; i < 9; i++)
				{
					for (int j = 0; j < 9; j++)
					{
						if (v == ViewArray[i][j])
						{
							if (selectedCol != null && selectedRow != null)
							{
								clearSquareValue(selectedRow - currentBlockRow * 3, selectedCol - currentBlockCol * 3);
								selectedRow = null;
								selectedCol = null;

								if (nextTurnSelectABlock)
								{
									nextTurnSelectABlock = false;
								}

							}

							if (nextTurnSelectABlock)
							{
								/* TODO
								 * reveal a title that tells the user to select a block.*/
								/*Enables the current block and disables all others*/

								// Check to see that the block selected is not won yet
								if (!board[i / 3][j / 3].getWinStatus())
								{
									lockVisuals(i / 3, j / 3);
									/* set nextTurnSelectABlock to false so this doesn't happen again*/
									nextTurnSelectABlock = false;
								}
								break;
							}
							// If setSquareValue returns false i.e. a value couldn't be placed in that position
							else if (!setSquareValue(i - currentBlockRow * 3, j - currentBlockCol * 3, player))
							{
								/*
								 * TODO output an alert or some visual information that tells the player they can't
								 * place a value there.
								 */
								Toast toast = Toast.makeText(MatchLocal.this, "setSquareValueOfBlock: Attempted to place value at square in block, but failed", Toast.LENGTH_SHORT);
								toast.show();
								return true;
							}
							else
							{
								/* TODO not sure whether we should make this call or, just call something that
								 * disables all the blocks so the first player can't make a move again.
								 */
								updateVisuals();

								selectedRow = i;
								selectedCol = j;
								// if (player == firstPlayer) player = secondPlayer;
								// else player = firstPlayer;

							}
						}
					}
				}
			}
			else if (!gameFinished && gameWon())
			{

				String winner = greaterBoard.getWinner();
				//				for (int i = 0; i < 3; i++)
				//					for (int j = 0; j < 3; j++)
				//					{
				//						setCurrentBlockValue(i, j, winner);
				//					}
				gameFinished = true;
				return true;
			}

			// Always returns true so the touch is consumed.
			return true;
		}
	};
	private       String          matchName       = null;
	private       PlexorTurn      matchData       = null;
	// boolean playerIsX = true;

	/**
	 *
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Call the superclass (required)
		super.onCreate(savedInstanceState);
		// Set the view to the game board xml (required)
		setContentView(R.layout.activity_game_board);

		context = getApplicationContext();

		// Populate the array of textboxes that represent the board visually
		ViewArray[0][0] = (EditText) findViewById(R.id.TextBoxR1C1);
		ViewArray[0][1] = (EditText) findViewById(R.id.TextBoxR1C2);
		DEFAULT_COLOR = ViewArray[0][1].getBackground();

		ViewArray[0][2] = (EditText) findViewById(R.id.TextBoxR1C3);
		ViewArray[0][3] = (EditText) findViewById(R.id.TextBoxR1C4);
		ViewArray[0][4] = (EditText) findViewById(R.id.TextBoxR1C5);
		ViewArray[0][5] = (EditText) findViewById(R.id.TextBoxR1C6);
		ViewArray[0][6] = (EditText) findViewById(R.id.TextBoxR1C7);
		ViewArray[0][7] = (EditText) findViewById(R.id.TextBoxR1C8);
		ViewArray[0][8] = (EditText) findViewById(R.id.TextBoxR1C9);

		ViewArray[1][0] = (EditText) findViewById(R.id.TextBoxR2C1);
		ViewArray[1][1] = (EditText) findViewById(R.id.TextBoxR2C2);
		ViewArray[1][2] = (EditText) findViewById(R.id.TextBoxR2C3);
		ViewArray[1][3] = (EditText) findViewById(R.id.TextBoxR2C4);
		ViewArray[1][4] = (EditText) findViewById(R.id.TextBoxR2C5);
		ViewArray[1][5] = (EditText) findViewById(R.id.TextBoxR2C6);
		ViewArray[1][6] = (EditText) findViewById(R.id.TextBoxR2C7);
		ViewArray[1][7] = (EditText) findViewById(R.id.TextBoxR2C8);
		ViewArray[1][8] = (EditText) findViewById(R.id.TextBoxR2C9);

		ViewArray[2][0] = (EditText) findViewById(R.id.TextBoxR3C1);
		ViewArray[2][1] = (EditText) findViewById(R.id.TextBoxR3C2);
		ViewArray[2][2] = (EditText) findViewById(R.id.TextBoxR3C3);
		ViewArray[2][3] = (EditText) findViewById(R.id.TextBoxR3C4);
		ViewArray[2][4] = (EditText) findViewById(R.id.TextBoxR3C5);
		ViewArray[2][5] = (EditText) findViewById(R.id.TextBoxR3C6);
		ViewArray[2][6] = (EditText) findViewById(R.id.TextBoxR3C7);
		ViewArray[2][7] = (EditText) findViewById(R.id.TextBoxR3C8);
		ViewArray[2][8] = (EditText) findViewById(R.id.TextBoxR3C9);

		ViewArray[3][0] = (EditText) findViewById(R.id.TextBoxR4C1);
		ViewArray[3][1] = (EditText) findViewById(R.id.TextBoxR4C2);
		ViewArray[3][2] = (EditText) findViewById(R.id.TextBoxR4C3);
		ViewArray[3][3] = (EditText) findViewById(R.id.TextBoxR4C4);
		ViewArray[3][4] = (EditText) findViewById(R.id.TextBoxR4C5);
		ViewArray[3][5] = (EditText) findViewById(R.id.TextBoxR4C6);
		ViewArray[3][6] = (EditText) findViewById(R.id.TextBoxR4C7);
		ViewArray[3][7] = (EditText) findViewById(R.id.TextBoxR4C8);
		ViewArray[3][8] = (EditText) findViewById(R.id.TextBoxR4C9);

		ViewArray[4][0] = (EditText) findViewById(R.id.TextBoxR5C1);
		ViewArray[4][1] = (EditText) findViewById(R.id.TextBoxR5C2);
		ViewArray[4][2] = (EditText) findViewById(R.id.TextBoxR5C3);
		ViewArray[4][3] = (EditText) findViewById(R.id.TextBoxR5C4);
		ViewArray[4][4] = (EditText) findViewById(R.id.TextBoxR5C5);
		ViewArray[4][5] = (EditText) findViewById(R.id.TextBoxR5C6);
		ViewArray[4][6] = (EditText) findViewById(R.id.TextBoxR5C7);
		ViewArray[4][7] = (EditText) findViewById(R.id.TextBoxR5C8);
		ViewArray[4][8] = (EditText) findViewById(R.id.TextBoxR5C9);

		ViewArray[5][0] = (EditText) findViewById(R.id.TextBoxR6C1);
		ViewArray[5][1] = (EditText) findViewById(R.id.TextBoxR6C2);
		ViewArray[5][2] = (EditText) findViewById(R.id.TextBoxR6C3);
		ViewArray[5][3] = (EditText) findViewById(R.id.TextBoxR6C4);
		ViewArray[5][4] = (EditText) findViewById(R.id.TextBoxR6C5);
		ViewArray[5][5] = (EditText) findViewById(R.id.TextBoxR6C6);
		ViewArray[5][6] = (EditText) findViewById(R.id.TextBoxR6C7);
		ViewArray[5][7] = (EditText) findViewById(R.id.TextBoxR6C8);
		ViewArray[5][8] = (EditText) findViewById(R.id.TextBoxR6C9);

		ViewArray[6][0] = (EditText) findViewById(R.id.TextBoxR7C1);
		ViewArray[6][1] = (EditText) findViewById(R.id.TextBoxR7C2);
		ViewArray[6][2] = (EditText) findViewById(R.id.TextBoxR7C3);
		ViewArray[6][3] = (EditText) findViewById(R.id.TextBoxR7C4);
		ViewArray[6][4] = (EditText) findViewById(R.id.TextBoxR7C5);
		ViewArray[6][5] = (EditText) findViewById(R.id.TextBoxR7C6);
		ViewArray[6][6] = (EditText) findViewById(R.id.TextBoxR7C7);
		ViewArray[6][7] = (EditText) findViewById(R.id.TextBoxR7C8);
		ViewArray[6][8] = (EditText) findViewById(R.id.TextBoxR7C9);

		ViewArray[7][0] = (EditText) findViewById(R.id.TextBoxR8C1);
		ViewArray[7][1] = (EditText) findViewById(R.id.TextBoxR8C2);
		ViewArray[7][2] = (EditText) findViewById(R.id.TextBoxR8C3);
		ViewArray[7][3] = (EditText) findViewById(R.id.TextBoxR8C4);
		ViewArray[7][4] = (EditText) findViewById(R.id.TextBoxR8C5);
		ViewArray[7][5] = (EditText) findViewById(R.id.TextBoxR8C6);
		ViewArray[7][6] = (EditText) findViewById(R.id.TextBoxR8C7);
		ViewArray[7][7] = (EditText) findViewById(R.id.TextBoxR8C8);
		ViewArray[7][8] = (EditText) findViewById(R.id.TextBoxR8C9);

		ViewArray[8][0] = (EditText) findViewById(R.id.TextBoxR9C1);
		ViewArray[8][1] = (EditText) findViewById(R.id.TextBoxR9C2);
		ViewArray[8][2] = (EditText) findViewById(R.id.TextBoxR9C3);
		ViewArray[8][3] = (EditText) findViewById(R.id.TextBoxR9C4);
		ViewArray[8][4] = (EditText) findViewById(R.id.TextBoxR9C5);
		ViewArray[8][5] = (EditText) findViewById(R.id.TextBoxR9C6);
		ViewArray[8][6] = (EditText) findViewById(R.id.TextBoxR9C7);
		ViewArray[8][7] = (EditText) findViewById(R.id.TextBoxR9C8);
		ViewArray[8][8] = (EditText) findViewById(R.id.TextBoxR9C9);

		/* Set onClick listeners for all the text boxes of the gameboard*/
		findViewById(R.id.button_confirm_move).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				confirmMove();
			}
		});

		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				ViewArray[i][j].setOnTouchListener(onTouchListener);
				ViewArray[i][j].setFocusable(false);
			}
		}

		/* Init game variables */
		board = new Block[3][3];
		serializedBoard = new String[81];
		greaterBoard = new Block();

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				board[i][j] = new Block();
			}
		}

		firstPlayer = LETTER_X;
		secondPlayer = LETTER_O;

		// TODO implement stuff in ActiveGamesList to handle the passing of this extra

		// Initialize the matchData variable depending on whether it's online or local
		// Is this an online multiplayer game?
		if (getIntent().hasExtra(EXTRA_MATCH_DATA))
		{
			mMatch = getIntent().getParcelableExtra(EXTRA_MATCH_DATA);
			matchData = PlexorTurn.unpersist(mMatch.getData());
			matchData.multiplayerMatch = Globals.turnData.multiplayerMatch;
		}
		// The game is not online, so it must be local
		else
		{
			matchData = Globals.turnData;
		}

		// Has anyone made a move yet?
		if (matchData.turnCounter == 0)
		{
			// Noone has made a move yet, so we need to initialize the match with starting values
			initGame();

			if(multiplayerMatch)
			{
				// Since this is the first turn of the match, we need to set the value of matchData.firstPlayer
				// To do this we must first connect to google.

				// Connect to google
				new AsyncTask<Void, Void, Void>()
				{
					@Override
					protected Void doInBackground(Void... params)
					{
						ConnectionResult cr = getApiClient().blockingConnect();
						//dismissSpinner();
						if (!cr.isSuccess())
						{
							showWarning("Connection Failed", "Could not connect to google api client");
						}
						String playerId = Games.Players.getCurrentPlayerId(getApiClient());
						String myParticipantId = mMatch.getParticipantId(playerId);
						matchData.firstPlayer = myParticipantId;
						return null;
					}

					@Override
					protected void onPostExecute(Void result)
					{
						dismissSpinner();
					}

				}.execute();
			}
		}

		// Someone has mad a move, so there are existing values that we'll initialize the game with
		else
		{
			multiplayerMatch = matchData.multiplayerMatch;

			currentBlockRow = matchData.lastMoveX / 3;
			currentBlockCol = matchData.lastMoveY / 3;

			deSerializeBoard(matchData.serializedBoard);

			for (int i = 0; i < 3; i++)
			{
				for (int j = 0; j < 3; j++)
				{
					Block localBlock = board[i][j];
					localBlock.checkForWin();

					if (localBlock.getWinStatus())
					{
						setBlockValue(i, j, localBlock.getWinner(), localBlock);
						//setBlockValue(i, j, localBlock);
					}
				}
			}

			currentBlock = board[currentBlockRow][currentBlockCol];

			lockVisuals(matchData.lastMoveX - currentBlockRow * 3, matchData.lastMoveY - currentBlockCol * 3);

			if (currentBlock.getWinStatus())
			{
				nextTurnSelectABlock = true;
				enableAllBlocks();
			}

			if (multiplayerMatch)
			{
				updateMatch();
			}
			else
			{
				player = matchData.firstPlayer;
			}

		}
	}

	private void updateOnMove()
	{

		currentBlockRow = matchData.lastMoveX / 3;
		currentBlockCol = matchData.lastMoveY / 3;

		deSerializeBoard(matchData.serializedBoard);

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				Block localBlock = board[i][j];
				localBlock.checkForWin();

				if (localBlock.getWinStatus())
				{
					setBlockValue(i, j, localBlock.getWinner(), localBlock);
					//setBlockValue(i, j, localBlock);
				}
			}
		}

		currentBlock = board[currentBlockRow][currentBlockCol];

		lockVisuals(matchData.lastMoveX - currentBlockRow * 3, matchData.lastMoveY - currentBlockCol * 3);

		if (currentBlock.getWinStatus())
		{
			nextTurnSelectABlock = true;
			enableAllBlocks();
		}

		updateMatch();
	}

	/**
	 * Called only on the first turn of a game. Used to initialize everything to a starting value;
	 *
	 */
	private void initGame()
	{
		if (getIntent().hasExtra("matchName"))
		{
			matchName = getIntent().getExtras().getString("matchName");
			matchData.matchName = matchName;
		}
		else
		{
			// TODO refactor to comply with use of unique ids for game names
			Calendar c = Calendar.getInstance();
			Date date = c.getTime();
			String name = date.toString();
			matchName = "Multiplayer "+name;
			matchData.matchName = matchName;
		}

		// It's the first turn, so turnData will have a value.
		multiplayerMatch = matchData.multiplayerMatch;

		player = firstPlayer;

		currentBlockRow = 1;
		currentBlockCol = 1;

		currentBlock = board[currentBlockRow][currentBlockCol];
		lockVisuals(currentBlockRow, currentBlockCol);

		// It's the first turn so the player is certainly doing their turn
		isDoingTurn = true;
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (lastRow != null && lastCol != null)
		{
			// Some basic turn data
			matchData.serializedBoard = serializeBoard();
			// TODO check for initializations
			matchData.lastMoveX = lastRow;
			matchData.lastMoveY = lastCol;
			// Put the player whose turn it is to go in the firstPlayer variable
			matchData.firstPlayer = player;

			ArrayList<PlexorTurn> matchList;

			String matchListPath = getFilesDir().getPath() + File.separator + "matches" + File.separator + "matchList.plx";
			File gamesListFile = new File(matchListPath);

			// Read the matchList from file so we can update it
			try
			{
				InputStream file = new FileInputStream(gamesListFile);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				matchList = (ArrayList<PlexorTurn>) input.readObject();
				input.close();
				buffer.close();
				file.close();
			}
			catch (IOException | ClassNotFoundException ex)
			{
				ex.printStackTrace();
				// Create the folder we need
				if (!(new File(getFilesDir().getPath() + File.separator + "matches").mkdir()))
				{
					Log.e("plexor", "matches folder created");
				}
				matchList = new ArrayList<>();
			}

			// Update the matchList
			for (PlexorTurn i : matchList)
			{
				if (i.matchName.equals(matchData.matchName))
				{
					matchList.remove(i);
					break;
				}
			}

			matchList.add(matchData);

			// Write the list of games to a file
			try
			{
				File matchListFile = new File(matchListPath);
				OutputStream file = new FileOutputStream(matchListFile);
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
				output.writeObject(matchList);
				output.close();
				buffer.close();
				file.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		// We're leaving the activity, so turnData should be cleared.
		Globals.turnData = null;
	}

	/**
	 *
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play_puzzles, menu);
		return true;
	}

	private void dismissSpinner()
	{
		findViewById(R.id.progressLayout).setVisibility(View.GONE);
	}

	/**
	 *
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * TODO set this up so that a player is chosen to play first based on a dice
	 * roll perhaps? consider making a simple version of rock paper scissors
	 * where one player chooses, then the other player chooses
	 */
	private void rollForFirst()
	{

		// playerIsX = true;
	}

	/**
	 * TODO
	 * <p/>
	 * Performs certain actions that are necessary to perform relating to the visual representation
	 * Enables a particular block corresponding to the square that a value was placed in
	 *
	 * @param row - (0-2) WITH RESPECT TO THE BLOCK IT WAS PLACED IN, row of the square where a value was last placed
	 * @param col - (0-2) WITH RESPECT TO THE BLOCK IT WAS PLACED IN, col of the square where a value was last placed
	 */
	private void lockVisuals(int row, int col)
	{
		// Just in case the visuals aren't fully updated
		updateVisuals();

		// TODO Some unnecessary processing occurs between these two methods.
		// Should be able to do the same thing with one method call
		enableBlock(getBlock(row, col));

		if (!setCurrentBlock(row, col))
		{
			// TODO create a method that highlights the available blocks and allows the player to select one
			// Or just enable all, but for this there needs to be proper handling of the value placement in the blocks.
			// for(int i = 0; i<9; i++)
			// for (int j = 0; j<9; j++)
			// ViewArray[i][j].setEnabled(true);
		}
	}

	/**
	 *
	 */
	public void enableBlock(int blockNum)
	{
		/**
		 * TODO create a complimentary function for disabling specific blocks
		 * rather than just disabling the entire board.
		 */
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if (ViewArray[i][j].isEnabled())
				{
					ViewArray[i][j].setEnabled(false);
				}
			}
		}

		switch (blockNum)
		{
			case 1:
				for (int i = 0; i < 3; i++)
				{
					for (int j = 0; j < 3; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;

			case 2:
				for (int i = 0; i < 3; i++)
				{
					for (int j = 3; j < 6; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
			case 3:
				for (int i = 0; i < 3; i++)
				{
					for (int j = 6; j < 9; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
			case 4:
				for (int i = 3; i < 6; i++)
				{
					for (int j = 0; j < 3; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
			case 5:
				for (int i = 3; i < 6; i++)
				{
					for (int j = 3; j < 6; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
			case 6:
				for (int i = 3; i < 6; i++)
				{
					for (int j = 6; j < 9; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
			case 7:
				for (int i = 6; i < 9; i++)
				{
					for (int j = 0; j < 3; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
			case 8:
				for (int i = 6; i < 9; i++)
				{
					for (int j = 3; j < 6; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
			case 9:
				for (int i = 6; i < 9; i++)
				{
					for (int j = 6; j < 9; j++)
					{
						ViewArray[i][j].setEnabled(true);
					}
				}
				break;
		}
	}

	/**
	 * Updates the current visual representation of the game board. - Enables a
	 * particular block after a value is placed.
	 */
	private void updateVisuals()
	{
		for (int i = 0; i < 3; i++) // loops for each block row
		{
			for (int j = 0; j < 3; j++) // loops for each row in the array
			{
				// loops for each block per row/column again since we can't make
				// a loop from 0-8 (each block is labeled from 0-2 in columns)
				for (int k = 0; k < 3; k++)
				{
					String localBlock[][] = board[i][k].getBoard();
					for (int l = 0; l < 3; l++) // hits 0,1,2 - 9 times each
					{
						ViewArray[j + i * 3][l + k * 3].setText(localBlock[j][l].equals(Block.empty) ? "#" : localBlock[j][l]);
					}

				}
			}
		}

	}

	public void confirmMove()
	{
		/* TODO
		 * We want this method to
		 * - Confirm our move placement with a popup dialog
		 * - Send the move data to the server
		 */
		if (selectedRow != null && selectedCol != null)
		{
			matchData.turnCounter+=1;
			currentBlock.checkForWin();

			if (currentBlock.getWinStatus())
			{
				setCurrentBlockValue();
			}

			lockVisuals(selectedRow - currentBlockRow * 3, selectedCol - currentBlockCol * 3);

			if (multiplayerMatch)
			{
				// Some basic turn data
				matchData.serializedBoard = serializeBoard();

				// TODO confirm that selectedRow and selectedCol are not null at this point.
				matchData.lastMoveX = selectedRow;
				matchData.lastMoveY = selectedCol;

				/* gets the id of the next participant. next participant is NULL on first move, and will have the participant ID on every other move*/
				String nextParticipant = getNextParticipantID();
				GoogleApiClient mApiClient = getApiClient();

				Games.TurnBasedMultiplayer.takeTurn(mApiClient, mMatch.getMatchId(), matchData.persist(), nextParticipant /*The ID of the player who's turn is next*/).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>()
				{
					@Override
					public void onResult(TurnBasedMultiplayer.UpdateMatchResult result)
					{
						processResult(result);
					}
				});
			}
			else
			{
				lastRow = selectedRow;
				lastCol = selectedCol;

				selectedRow = null;
				selectedCol = null;

				if (player.equals(firstPlayer))
				{
					player = secondPlayer;
				}
				else
				{
					player = firstPlayer;
				}
			}
		}
		else
		{
			/*
			 * TODO
			 * output a toast saying that the player has to select a square
			 */
		}
	}

	/**
	 *
	 */
	public String getNextParticipantID()
	{
		String playerId = Games.Players.getCurrentPlayerId(getApiClient());
		String myParticipantId = mMatch.getParticipantId(playerId);

		ArrayList<String> participantIds = mMatch.getParticipantIds();

		int desiredIndex = -1;

		for (int i = 0; i < participantIds.size(); i++)
		{
			if (participantIds.get(i).equals(myParticipantId))
			{
				desiredIndex = i + 1;
			}
		}

		if (desiredIndex < participantIds.size())
		{
			return participantIds.get(desiredIndex);
		}

		if (mMatch.getAvailableAutoMatchSlots() <= 0)
		{
			// You've run out of automatch slots, so we start over.
			return participantIds.get(0);
		}

		else
		{
			// You have not yet fully automatched, so null will find a new
			// person to play against.
			return null;
		}
	}

	/**
	 * Takes the row and column of some square in the 81 square board and
	 * returns the block number THAT IT CAME FROM in the greater board
	 */
	private int getBlock(int row, int col)
	{
		int temp = col + 1;
		int value = temp + (row * 3);
		return value;
	}

	/**
	 *
	 */
	private String serializeBoard()
	{
		String localBoard = "";
		for (int i = 0; i < 3; i++)
		// loops for each block row
		{
			for (int j = 0; j < 3; j++)
			// loops for each row in the array
			// loops for each block per row/column again since we can't make a loop from 0-8 (each block is labeled from 0-2 in columns)
			{
				for (int k = 0; k < 3; k++)
				{
					String localBlock[][] = board[i][k].getBoard();
					for (int l = 0; l < 3; l++) // hits 0,1,2 - 9 times each
					{
						localBoard = localBoard.concat(localBlock[j][l]);
					}

				}
			}
		}
		return localBoard;
	}

	/**
	 * This loop, along with the other loops that are similar to it in structure, is confusing as fuck.
	 * Don't try to understand it (although I have included comments if you want to try), you'll only get
	 * confused thinking about it. Believe me. I wrote it, and every time I come back to try and understand
	 * it, I sit staring at my computer screen for an hour.
	 */
	private void deSerializeBoard(String serializedBoard)
	{
		/*Take the serializedBoard string and convert it to a Queue*/
		//Queue<String> serializedBoardQueue = new LinkedList<>(Arrays.asList(serializedBoard.split("")));
		/* Loops for each row in the greaterBoard (3 times - 0,
		 *                                                   1,
		 *                                                   2)
		 */
		int count = 0;
		for (int i = 0; i < 3; i++)
		// Loops for each row in each block (9 times - 0,1,2, 0,1,2, 0,1,2)
		{
			for (int j = 0; j < 3; j++)
				// Loops for each column in the greaterBoard, since they change every 3 iterations.
				/* (27 times - 0,1,2,
				 *             0,1,2,
				 *             0,1,2
				 *             0,1,2,
				 *             0,1,2,
				 *             0,1,2
				 *             0,1,2,
				 *             0,1,2,
				 *             0,1,2 )*/
			{
				for (int k = 0; k < 3; k++)
				{
					// Loops for each column in each block (81 times)
					for (int l = 0; l < 3; l++) // hits 0,1,2 - 27 times
					{
						// if the char at the block is not the empty value, set the value equal to the character at that position
						if (!String.valueOf(serializedBoard.charAt(count)).equals(Block.empty))
						{
							setSquareValueOfBlock(board[i][k], j, l, String.valueOf(serializedBoard.charAt(count)));
						}
						count++;
					}

				}
			}
		}
	}

	/**
	 * Given a block, sets the value of the square at the given coordinates within the block. Intended for use updating the match.
	 * Does not call Block.checkForWin(), or setBlockValue(). This should only be done once the entire board is initialized.
	 */
	private boolean setSquareValueOfBlock(Block block, int row, int col, String value)
	{
		try
		{
			block.setSquare(row, col, value);
			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			Toast toast = Toast.makeText(this, "setSquareValueOfBlock: Attempted to place value at square in block, but failed", Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}
	}

	// This is the main function that gets called when players choose a match
	// from the inbox, or else create a match and want to start it.
	/*TODO add handling for the bitmask that is passed from the roomConfig call on line 186 in mainActivity*/
	public void updateMatch()
	{
		/**
		 * What we need this TODO
		 * -Initialize all the backend stuff that can't be initialized from local values
		 */

		int status = mMatch.getStatus();
		int turnStatus = mMatch.getTurnStatus();

		switch (status)
		{
			case TurnBasedMatch.MATCH_STATUS_CANCELED:
				showWarning("Canceled!", "This game was canceled!");
				return;
			case TurnBasedMatch.MATCH_STATUS_EXPIRED:
				showWarning("Expired!", "This game is expired.  So sad!");
				return;
			case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
				showWarning("Waiting for auto-match...", "We're still waiting for an automatch partner.");
				return;
			case TurnBasedMatch.MATCH_STATUS_COMPLETE:
				if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE)
				{
					showWarning("Complete!", "This game is over; someone finished it, and so did you!  There is nothing to be done.");
					break;
				}

				// Note that in this state, you must still call "Finish" yourself, so we allow this to continue.
				showWarning("Complete!", "This game is over; someone finished it!  You can only close it now.");
		}

		// OK, it's active. Check on turn status.
		switch (turnStatus)
		{
			case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
				isDoingTurn = true;

				initializeMatchOnUpdate();

				return;
			case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
			{
				isDoingTurn = false;

				initializeMatchOnUpdate();

				return;
			}
			//TODO investigate when this turn status is given
			case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
				showWarning("Good inititative!", "Still waiting for invitations.\n\nBe patient!");
		}
	}

	public void processResult(TurnBasedMultiplayer.UpdateMatchResult result)
	{
		mMatch = result.getMatch();

		if (!checkStatusCode(mMatch, result.getStatus().getStatusCode()))
		{
			return;
		}
		if (mMatch.canRematch())
		{
			//askForRematch();
		}

		/*TODO Error occurring here when a move is confirmed where getTurnStatus is returning that it is this player's turn,
		 * when in fact it should not be because the first move was cast and it should be the next player's turn*/
		isDoingTurn = (mMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

		if (isDoingTurn)
		{
			updateMatch();
			return;
		}
	}

	private void initializeMatchOnUpdate()
	{

		if ( matchData.secondPlayer == null )
		{
			new AsyncTask<Void, Void, Void>()
			{

				@Override
				protected Void doInBackground(Void... params)
				{
					ConnectionResult cr = getApiClient().blockingConnect();
					//dismissSpinner();
					if (!cr.isSuccess())
					{
						showWarning("Connection Failed", "Could not connect to google api client");
					}
					/*TODO solve java.lang.IllegalStateException: GoogleApiClient must be connected*/
					String playerId = Games.Players.getCurrentPlayerId(getApiClient());
					String myParticipantId = mMatch.getParticipantId(playerId);

					String firstPlayerParticipantId = matchData.firstPlayer;
					if (!firstPlayerParticipantId.equals(myParticipantId))
					{
						matchData.secondPlayer = myParticipantId;
					}

					return null;
				}

				@Override
				protected void onPostExecute(Void result)
				{
					dismissSpinner();
				}

			}.execute();

		}

		// Determines and sets the current player
		determineCurrentPlayer();

		/* Represents the row and column of the currently selected block within the 3x3 greaterBoard.
		 * Modulo the last move positions by 3 to find the current blockRow and column*/
		currentBlockRow = matchData.lastMoveX % 3;
		currentBlockCol = matchData.lastMoveY % 3;

		nextTurnSelectABlock = matchData.nextTurnSelectABlock;

		deSerializeBoard(matchData.serializedBoard);
		updateGreaterBoard();

		if (nextTurnSelectABlock)
		{
			/*TODO
			 * reveal a title that tells the user to select a block.*/
			enableAllBlocks();
			//highlightSelectableBlocks();
		}
		/* nextTurnSelectABlock is false, so we have a current block to now update the visuals with*/
		else
		{
			// Sets the starting block
			currentBlock = board[currentBlockRow][currentBlockCol];
			/*Enables the current block and disables all others*/
			lockVisuals(currentBlockRow, currentBlockCol);
		}
	}

	/**
	 * Determines which player it is right now that's making the move.
	 * So we know what visual thing the player will be placing on the board when they touch the screen.
	 */
	private void determineCurrentPlayer()
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				ConnectionResult cr = getApiClient().blockingConnect();
				//dismissSpinner();
				if (!cr.isSuccess())
				{
					showWarning("Connection Failed", "Could not connect to google api client");
				}

//				String playerId = Games.Players.getCurrentPlayerId(getApiClient());
//				String myParticipantId = mMatch.getParticipantId(playerId);
//
//				ArrayList<String> participantIds = mMatch.getParticipantIds();
//
//				for (int i = 0; i < participantIds.size(); i++)
//				{
//					if (participantIds.get(i).equals(myParticipantId))
//					{
//						if(i == 0)
//						{
//							player = firstPlayer;
//						}
//						else
//						{
//							player = secondPlayer;
//						}
//						return null;
//					}
//				}

				String playerId = Games.Players.getCurrentPlayerId(getApiClient());
				String myParticipantId = mMatch.getParticipantId(playerId);

				String firstPlayerParticipantId = matchData.firstPlayer;
				if (firstPlayerParticipantId.equals(myParticipantId))
				{
					player = firstPlayer;
				}
				else
				{
					player = secondPlayer;
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				dismissSpinner();
			}

		}.execute();
	}

	private void updateGreaterBoard()
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				board[i][j].checkForWin();

				if (board[i][j].getWinStatus())
				{
					setBlockValue(i, j, board[i][j]);
				}
			}
		}
	}

	@Override
	public void onTurnBasedMatchReceived(TurnBasedMatch match)
	{
		mMatch = match;
		matchData = PlexorTurn.unpersist(mMatch.getData());
		updateOnMove();

		Toast.makeText(this, "Match Updated", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTurnBasedMatchRemoved(String matchId)
	{
		Toast.makeText(this, "A match was removed.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSignInSucceeded()
	{
		Games.TurnBasedMultiplayer.registerMatchUpdateListener(getApiClient(), this);
		return;
	}

	/**
	 * #########################################################################
	 * #########################################################################
	 */
	/**
	 * #########################################################################
	 * #########################################################################
	 */
	/**
	 * #########################################################################
	 * #########################################################################
	 */
	/**
	 * #########################################################################
	 * #########################################################################
	 */
	/**
	 * #########################################################################
	 * #########################################################################
	 */
	/**
	 * #########################################################################
	 * #########################################################################
	 */

	/**
	 *
	 */
	private Block getCurrentBlock()
	{
		return currentBlock;
	}

	/**
	 * Used for switching to the next block the game is to be played in
	 */
	private boolean setCurrentBlock(int row, int col)
	{
		/* If the block we're changing to is not won*/
		if (!board[row][col].getWinStatus())
		{
			currentBlock = board[row][col];
			currentBlockCol = col;
			currentBlockRow = row;
			return true;
		}

		nextTurnSelectABlock = true;
		enableAllBlocks();
		/* If the block we're changing to is won*/
		return false;
	}

	// sets our current square to the value
	// checks if a win has occured in the block
	// if it has, call the set block method to set the greater block to the
	// winner's symbol

	/**
	 *
	 */
	private boolean setSquareValue(int row, int col, String value)
	{
		try
		{
			currentBlock.setSquare(row, col, value);
			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			return false;
		}
	}

	/**
	 * Enables every block on the board
	 */
	private void enableAllBlocks()
	{
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if (!ViewArray[i][j].isEnabled())
				{
					ViewArray[i][j].setEnabled(true);
				}
			}
		}
	}

	/**
	 * Sets the value of the block passed to it. Uses the greaterBoard variable and
	 * sets all the square values at the current row and column to the winner of the block
	 */
	private boolean setBlockValue(int row, int col, String winner, Block block)
	{
		try
		{
			greaterBoard.setSquare(row, col, block.getWinner());
			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			return false;
		}
	}

	/**
	 *
	 */
	private boolean clearSquareValue(int row, int col)
	{
		currentBlock.clearSquare(row, col);

		return true;
	}

	/**
	 * Sets the value of the current block. Uses the greaterBoard variable and sets the value at
	 * the current row and column to the winner in the block
	 */
	private boolean setBlockValue(int localBlockRow, int localBlockCol, Block localBlock)
	{
		try
		{
			greaterBoard.setSquare(localBlockRow, localBlockCol, localBlock.getWinner());

			String winner = localBlock.getWinner();

			for (int i = 0; i < 3; i++)
			{
				for (int j = 0; j < 3; j++)
				{
					//ViewArray[j + i * 3][l + k * 3].setText(localBlock[j][l] != Block.empty ? localBlock[j][l] : "#");
					localBlock.clearSquare(i, j);
					localBlock.setSquare(i, j, winner);
				}
			}

			/* The following creates a simple animation which appears as a spiral when a block is won */
			ViewArray[3 * localBlockRow][3 * localBlockCol].setText(winner);
			ViewArray[3 * localBlockRow][1 + 3 * localBlockCol].setText(winner);
			ViewArray[3 * localBlockRow][2 + 3 * localBlockCol].setText(winner);

			ViewArray[1 + 3 * localBlockRow][2 + 3 * localBlockCol].setText(winner);
			ViewArray[2 + 3 * localBlockRow][2 + 3 * localBlockCol].setText(winner);
			ViewArray[2 + 3 * localBlockRow][1 + 3 * localBlockCol].setText(winner);

			ViewArray[2 + 3 * localBlockRow][3 * localBlockCol].setText(winner);
			ViewArray[1 + 3 * localBlockRow][3 * localBlockCol].setText(winner);
			ViewArray[1 + 3 * localBlockRow][1 + 3 * localBlockCol].setText(winner);

			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			return false;
		}
	}

	/**
	 * Sets the value of the current block. Uses the greaterBoard variable and sets the value at
	 * the current row and column to the winner in the block
	 */
	private boolean setCurrentBlockValue()
	{
		try
		{
			greaterBoard.setSquare(currentBlockRow, currentBlockCol, currentBlock.getWinner());

			String winner = currentBlock.getWinner();

			for (int i = 0; i < 3; i++)
			{
				for (int j = 0; j < 3; j++)
				{
					//ViewArray[j + i * 3][l + k * 3].setText(localBlock[j][l] != Block.empty ? localBlock[j][l] : "#");
					currentBlock.clearSquare(i, j);
					currentBlock.setSquare(i, j, winner);
				}
			}

			/* The following creates a simple animation which appears as a spiral when a block is won */
			ViewArray[3 * currentBlockRow][3 * currentBlockCol].setText(winner);
			ViewArray[3 * currentBlockRow][1 + 3 * currentBlockCol].setText(winner);
			ViewArray[3 * currentBlockRow][2 + 3 * currentBlockCol].setText(winner);

			ViewArray[1 + 3 * currentBlockRow][2 + 3 * currentBlockCol].setText(winner);
			ViewArray[2 + 3 * currentBlockRow][2 + 3 * currentBlockCol].setText(winner);
			ViewArray[2 + 3 * currentBlockRow][1 + 3 * currentBlockCol].setText(winner);

			ViewArray[2 + 3 * currentBlockRow][3 * currentBlockCol].setText(winner);
			ViewArray[1 + 3 * currentBlockRow][3 * currentBlockCol].setText(winner);
			ViewArray[1 + 3 * currentBlockRow][1 + 3 * currentBlockCol].setText(winner);

			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			return false;
		}
	}

	private boolean gameWon()
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
	private boolean determineFirstPlayer()
	{
		// TODO get the user to input even or odd guesses
		Random rand = new Random();
		int i = rand.nextInt() % 2;

		if (i == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

}