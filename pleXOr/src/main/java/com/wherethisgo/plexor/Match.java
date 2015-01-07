package com.wherethisgo.plexor;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * TODO
 * -Create some way of keeping track of turn timelimits. Currently the information is being passed to the match but we're not doing anything with it
 * -Implement ranked matches
 * -General efficiency and structural improvements- This class has a lot of things that are done in a lazy manner, this needs to be remedied.
 */
public class Match extends MainActivity
{
	final String LETTER_O = "O";
	final String LETTER_X = "X";
	public PlexorTurn mTurnData;
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
	private String  serializedBoard/* = new String[81] */;
	//TODO Consider eliminating the use of greaterBoard in some instances entirely. Block keeps track of its block winners already.
	private Block   greaterBoard/* = new Block() */;
	private Block   currentBlock/* = board[1][1] */; // sets the starting block
	// private String currentPlayer/* = "0"*/;
	private Integer currentBlockCol/* = 1 */;
	private Integer currentBlockRow/* = 1 */;
	private Integer selectedCol;
	private Integer selectedRow;
	// Is the player currently performing a move
	private boolean isDoingTurn = false;
	private boolean nextTurnSelectABlock = false;
	// This is the current match we're in; null if not loaded
	private TurnBasedMatch mMatch;
	// Local convenience pointers
	private TextView mDataView;
	private TextView mTurnTextView;
	private String player, firstPlayer, secondPlayer;

	// boolean playerIsX = true;
	/**
	 *
	 */
	private final OnTouchListener onTouchListener = new OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			v.performClick();
			/*
			 * make a variable for the game that can call gameWon() like in
			 * our text version of this
			 */
			if (!gameWon() && v.isEnabled() && isDoingTurn)
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
								/*Enables the current block and disables all others*/
								lockVisuals(i / 3, j / 3);
								/* set nextTurnSelectABlock to false so this doesn't happen again*/
								nextTurnSelectABlock = false;

								/*TODO change the visibility of the textview saying that the user has to select a block*/
							}
							// If setSquareValue returns false it means a value couldn't be placed in that position
							else if (!setSquareValue(i - currentBlockRow * 3, j - currentBlockCol * 3, player))
							{
								/*
								 * setSquareValue outputs a toast when the value cannot be placed
								 */
								return false;
							} else
							{
								/* TODO not sure whether we should make this call or, just call something that
								 * disables all the blocks so the first player can't make a move again.
								 * */
								updateVisuals();

								selectedRow = i;
								selectedCol = j;
								// if (player == firstPlayer) player = secondPlayer;
								// else player = firstPlayer;

								return true;
							}
						}
					}
				}
			} else if (nextTurnSelectABlock)
			{
				/*
				 * #########################################################################
				 * UPDATE THIS SECTION SO THAT WHEN A TOUCH OCCURS ON A MOVE WHERE IF
				 * nextTurnSelectABlock IS TRUE, THE GAME MAKES YOU CHOOSE A BLOCK, AND THEN
				 * THAT BLOCK BECOMES ENABLED
				 * #########################################################################
				 */
			}
			// TODO do not let gameWon() be called from here. Let the win status only occur after a move is confirmed.
			else if (gameWon())
			{
			}

			return false;
		}
	};
	private Context context;

	public Match()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * TODO
	 * This method should:
	 * - Determine whether the game is being started or being updated
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_board);

		//getGameHelper().setMaxAutoSignInAttempts(0);

		context = getApplicationContext();

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

		// TODO confirm all these variables are properly initialized, remove any unnecessary variables
		firstPlayer = LETTER_X;
		secondPlayer = LETTER_O;

		selectedRow = null;
		selectedCol = null;

		/* Init game variables */
		board = new Block[3][3];
		greaterBoard = new Block();

		/*Initializes the board variable*/
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				board[i][j] = new Block();
			}
		}

		/*Sets the on touch listener for all the edit texts*/
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				ViewArray[i][j].setOnTouchListener(onTouchListener);
			}
		}

		TurnBasedMatch match = getIntent().getParcelableExtra(EXTRA_MATCH_DATA);
		mMatch = match;

		findViewById(R.id.button_confirm_move).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				confirmMove();
			}
		});

		/*if match data isn't null, then this game is an existing game and needs to be updated before any moves can be made*/
		if (match.getData() != null)
		{
			// The match has already been started, so there's nothing to initialize
			updateMatch(match);
		}
		/*It's the first move of this game. So we have to initialize some stuff to starting values*/
		else
		{
            /* These values represent the current block's coordinates within the 3x3 greaterBoard.
			 * Game starts in the middle block.*/
			currentBlockCol = 1;
			currentBlockRow = 1;

			// Sets the starting block
			currentBlock = board[currentBlockRow][currentBlockCol];

			/* Enables the current block and disables all others*/
			// enableBlock(getBlockNumber(currentBlockRow, currentBlockCol));
			lockVisuals(currentBlockCol, currentBlockRow);

			serializedBoard = "";

			player = firstPlayer;

			isDoingTurn = true;
			showSpinner();

			mTurnData = new PlexorTurn();

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
					mTurnData.firstPlayer = myParticipantId;
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

	private void showSpinner()
	{
		findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
	}

	private void dismissSpinner()
	{
		findViewById(R.id.progressLayout).setVisibility(View.GONE);
	}

	/**
	 *
	 */
	public void confirmMove()
	{
		/* TODO
		 * We want this method to
		 * - Confirm our move placement with a popup dialog
		 * - Send the move data to the server
		 */
		lockVisuals(selectedRow - currentBlockRow * 3, selectedCol - currentBlockCol * 3);
		// Some basic turn data
		mTurnData.serializedBoard = serializeBoard();
		// TODO confirm that selectedRow and selectedCol are not null at this point.
		mTurnData.lastMoveX = selectedRow;
		mTurnData.lastMoveY = selectedCol;

		/* gets the id of the next participant. next participant is NULL on first move, and will have the participant ID on every other move*/
		String nextParticipant = getNextParticipantID();

		Games.TurnBasedMultiplayer.takeTurn(getApiClient(), mMatch.getMatchId(), mTurnData.persist(), nextParticipant /*The ID of the player who's turn is next*/).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>()
		{
			@Override
			public void onResult(TurnBasedMultiplayer.UpdateMatchResult result)
			{
				processResult(result);
			}
		});
	}

	/**
	 *
	 */
	public String getNextParticipantID()
	{
		String playerId = Games.Players.getCurrentPlayerId(getApiClient());
		String myId = mMatch.getParticipantId(playerId);

		String firstPlayer = mTurnData.firstPlayer;
		if (myId == firstPlayer)
		{
			return mTurnData.secondPlayer;
		} else
		{
			return firstPlayer;
		}

	}

	// This is the main function that gets called when players choose a match
	// from the inbox, or else create a match and want to start it.
	/*TODO add handling for the bitmask that is passed from the roomConfig call on line 186 in mainActivity*/
	public void updateMatch(TurnBasedMatch match)
	{
		/**
		 * What we need this TODO
		 * -Initialize all the backend stuff that can't be initialized from local values
		 */

		mMatch = match;

		int status = match.getStatus();
		int turnStatus = match.getTurnStatus();

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

		mTurnData = null;
	}

	public void processResult(TurnBasedMultiplayer.UpdateMatchResult result)
	{
		TurnBasedMatch match = result.getMatch();

		if (!checkStatusCode(match, result.getStatus().getStatusCode()))
		{
			return;
		}
		if (match.canRematch())
		{
			//askForRematch();
		}

		/*TODO Error occurring here when a move is confirmed where getTurnStatus is returning that it is this player's turn,
		 * when in fact it should not be because the first move was cast and it should be the next player's turn*/
		isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

		if (isDoingTurn)
		{
			updateMatch(match);
			return;
		}
	}

	private void initializeMatchOnUpdate()
	{
		mTurnData = PlexorTurn.unpersist(mMatch.getData());

		if (mTurnData.secondPlayer == null)
		{
			String playerId = Games.Players.getCurrentPlayerId(getApiClient());
			String myParticipantId = mMatch.getParticipantId(playerId);

			String firstPlayerParticipantId = mTurnData.firstPlayer;
			if (firstPlayerParticipantId != myParticipantId)
			{
				mTurnData.secondPlayer = myParticipantId;
			}
		}

		// Determines and sets the current player
		determineCurrentPlayer();

		/* Represents the row and column of the currently selected block within the 3x3 greaterBoard.
		 * Modulo the last move positions by 3 to find the current blockRow and column*/
		currentBlockRow = mTurnData.lastMoveX % 3;
		currentBlockCol = mTurnData.lastMoveY % 3;

		nextTurnSelectABlock = mTurnData.nextTurnSelectABlock;

		deSerializeBoard(mTurnData.serializedBoard);
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

	private void highlightSelectableBlocks()
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				if (greaterBoard.getSquare(i, j) == null)
				{
					// Make the highlight square visible
				}
			}
		}
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
	 * set this up so that a player is chosen to play first based on a dice
	 * roll perhaps? consider making a simple version of rock paper scissors
	 * where one player chooses, then the other player chooses
	 */
	private void rollForFirst()
	{

		// playerIsX = true;
	}

	/*
	 * Determines which player it is right now that's making the move.
	 * So we know what visual thing the player will be placing on the board when they touch the screen.
	 *
	 */
	private void determineCurrentPlayer()
	{
		/*TODO change this so that global values aren't changed automatically*/
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

				String firstPlayerParticipantId = mTurnData.firstPlayer;
				if (firstPlayerParticipantId == myParticipantId)
				{
					player = firstPlayer;
					return null;
				}

				player = secondPlayer;

				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				dismissSpinner();
			}

		}.execute();
	}

	/**
	 * TODO
	 * Find a better way to do this. A way that doesn't involving disabling the entire board followed by initializing the one block
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
	 * Updates the current visual representation of the game board. - Does not enable a
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
						ViewArray[j + i * 3][l + k * 3].setText(localBlock[j][l] != Block.empty ? localBlock[j][l] : "#");
					}
				}
			}
		}
	}

	/**
	 * TODO
	 * <p/>
	 * Performs certain actions that are necessary to perform relating to the visual representation
	 * before the game board is sent off to the game server.
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
		enableBlock(getBlockNumber(row, col));

		if (!setCurrentBlock(row, col))
		{
			// TODO create a method that highlights the available blocks and allows the player to select one
			// Or just enable all, but for this there needs to be proper handling of the value placement in the blocks.
			// for(int i = 0; i<9; i++)
			// for (int j = 0; j<9; j++)
			// ViewArray[i][j].setEnabled(true);
		}
	}

	private void enableAllBlocks()
	{
		for (int i = 1; i < 10; i++)
		{
			enableBlock(i);
		}
	}

	/**
	 *
	 */
	private String IntToStr(int value)
	{
		String newValue = "0";

		switch (value)
		{
			case 0:
				newValue = "0";
				break;
			case 1:
				newValue = "1";
				break;
			case 2:
				newValue = "2";
				break;
			case 3:
				newValue = "3";
				break;
			case 4:
				newValue = "4";
				break;
			case 5:
				newValue = "5";
				break;
			case 6:
				newValue = "6";
				break;
			case 7:
				newValue = "7";
				break;
			case 8:
				newValue = "8";
				break;
			case 9:
				newValue = "9";
				break;
		}

		return newValue;
	}

	/**
	 *
	 */
	private int StrToInt(String value)
	{
		int newValue = 0;

		if (value.length() == 0)
		{
			newValue = 0;
		} else
		{
			switch (value.charAt(0))
			{
				case '1':
					newValue = 1;
					break;
				case '2':
					newValue = 2;
					break;
				case '3':
					newValue = 3;
					break;
				case '4':
					newValue = 4;
					break;
				case '5':
					newValue = 5;
					break;
				case '6':
					newValue = 6;
					break;
				case '7':
					newValue = 7;
					break;
				case '8':
					newValue = 8;
					break;
				case '9':
					newValue = 9;
					break;
			}
		}
		return newValue;
	}

	/**
	 * Takes the row and column of the block in the 3x3 greater board
	 * returns that block's (1-9) number
	 */
	private int getBlockNumber(int row, int col)
	{
		// if(0<=col && col<=8 && 0<=row && 8<=row)
		// {
		// if(0<=col && col<=2 && 0<=row && row<=2)
		// return 1;
		// else if(3<=col && col<=5 && 0<row && row<3)
		// return 2;
		// else if(6<=col && col<=8 && 0<row && row<3)
		// return 3;
		//
		// else if(0<=col && col<=2 && 3<=row && row<=5)
		// return 4;
		// else if(3<=col && col<=5 && 3<=row && row<=5)
		// return 5;
		// else if(6<=col && col<=8 && 3<=row && row<=5)
		// return 6;
		//
		// else if(0<=col && col<=2 && 6<=row && row<=8)
		// return 7;
		// else if(3<=col && col<=5 && 6<=row && row<=8)
		// return 8;
		// else if(6<=col && col<=8 && 6<=row && row<=8)
		// return 9;
		// }
		// return -1;
		// (1,2) = 3+(3)
		++col;
		int value = col + (row * 3);
		return value;
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
	 * @deprecated originally used for the text based version of the game.
	 * Unnecessary to include here
	 */
	@Deprecated
	public void printBoard()
	{
		for (int i = 0; i < 3; i++) // loops for each block row
		{
			for (int j = 0; j < 3; j++) // loops for each row in the array
			{
				System.out.print("|");
				// loops for each block per row/column again since we can't make
				// a loop from 0-8 (each block is labeled from 0-2 in columns)
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

	/**
	 *
	 */
	private Block getCurrentBlock()
	{
		return currentBlock;
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
						localBoard.concat(localBlock[j][l]);
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
		Queue<String> serializedBoardQueue = new LinkedList<String>(Arrays.asList(serializedBoard.split("")));
		/* Loops for each row in the greaterBoard (3 times - 0,
		 *                                                   1,
		 *                                                   2)*/
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
						//TODO create a catch or some handling for the exception that is thrown when the queue is empty
						//serializedBoard.concat(localBlock[j][l]);
						setSquareValueOfBlock(board[i][k], j, l, serializedBoardQueue.remove());
					}

				}
			}
		}
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

	/**
	 * Used for switching to the next block the game is to be played in
	 */
	private boolean setCurrentBlock(int row, int col)
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

	/**
	 * Given a value and the row and col of some square in currentBlock, sets the value of that square to the given value.
	 */
	private boolean setSquareValue(int row, int col, String value)
	{
		try
		{
			currentBlock.setSquare(row, col, value);
			currentBlock.checkForWin();

			if (currentBlock.getWinStatus())
			{
				nextTurnSelectABlock = true;
				setCurrentBlockValue();
			}

			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			Toast toast = Toast.makeText(context, "Sorry, that square cannot be selected.", Toast.LENGTH_SHORT);
			toast.show();
			return false;
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
			Toast toast = Toast.makeText(context, "setSquareValueOfBlock: Attempted to place value at square in block, but failed", Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}
	}

	/**
	 *
	 */
	private boolean clearSquareValue(int row, int col)
	{
		currentBlock.clearSquare(row, col);
		currentBlock.checkForWin();

		if (!currentBlock.getWinStatus())
		{
			setCurrentBlockValue();
		}

		return true;
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

			nextTurnSelectABlock = true;

			return true;
		}
		catch (CannotPlaceValueException cpve)
		{
			return false;
		}
	}

	private void sleep(int milliseconds)
	{
		try
		{
			Thread.sleep(milliseconds); //1000 milliseconds is one second.
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Sets the value of the current block. Uses the greaterBoard variable and sets the value at
	 * the current row and column to the winner in the block
	 */
	private boolean setBlockValue(int row, int col, Block block)
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
		} else
		{
			return false;
		}
	}

}
