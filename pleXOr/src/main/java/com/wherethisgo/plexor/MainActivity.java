package com.wherethisgo.plexor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameActivity;

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

public class MainActivity extends BaseGameActivity
{
	public static final String TAG                = "DrawingActivity";
	// For our intents
	public final static int    RC_LOOK_AT_MATCHES = 10001;
	public final static int    RC_CREATE_GAME     = 10002;
	public static final int    RC_OPEN_GAME       = 10003;
	public static final int    RC_INBOX           = 10004;
	// For activities with arguments
	public final static String EXTRA_MATCH_DATA   = "com.wherethisgo.plexor.match_data";
	String matchListPath = null;
	// How long to show toasts.
	private AlertDialog mAlertDialog;
	private ArrayList<PlexorTurn> matchList = null;
	// This is the current match data after being unpersisted.
	// Do not retain references to match data once you have
	// taken an action on the match, such as takeTurn()
	// public SkeletonTurn matchData;
	// private TextView mDisplay;
	// private AtomicInteger msgId = new AtomicInteger();
	// private SharedPreferences prefs;

	// Stuff for panningView animation
	private ImageView backgroundUL;
	private ImageView backgroundU;
	private ImageView backgroundL;
	private ImageView background;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		// Background animation
		background = (ImageView) findViewById(R.id.activity_background);
		backgroundUL = (ImageView) findViewById(R.id.activity_backgroundUL);
		backgroundU = (ImageView) findViewById(R.id.activity_backgroundU);
		backgroundL = (ImageView) findViewById(R.id.activity_backgroundL);

		background.setPadding(1,1,1,1);
		backgroundUL.setPadding(1,1,1,1);
		backgroundU.setPadding(1,1,1,1);
		backgroundL.setPadding(1,1,1,1);

		int distanceMultiple = 10;
		int delta = (100*distanceMultiple);
		int location[] = new int[2];
		background.getLocationOnScreen(location);

		TranslateAnimation animation = new TranslateAnimation(location[0], location[0]+(delta), location[1], location[1]+(delta));
		animation.setDuration(10000);
		animation.setFillAfter(false);
		animation.setRepeatMode(Animation.RESTART);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setInterpolator(new LinearInterpolator());

		backgroundUL.setX(-delta);
		backgroundUL.setY(-delta);

		backgroundL.setX(-delta);

		backgroundU.setY(-delta);

		background.startAnimation(animation);
		backgroundUL.startAnimation(animation);
		backgroundL.startAnimation(animation);
		backgroundU.startAnimation(animation);

		// Splash Screen fade out
		ImageView splash = (ImageView) findViewById(R.id.logoSplash);
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setFillAfter(true);
		fadeOut.setStartOffset(3000);
		fadeOut.setDuration(1000);
		//splash.setAnimation(fadeOut);

		getGameHelper().setMaxAutoSignInAttempts(0);

		File appDirectory = new File(getFilesDir(), "");
		/* TODO ensure that on release of this game, the array list of games is encrypted so it cannot
		* be manipulated
		*
		* We have to make sure that those files that are saved on the device are not used to update any multiplayer data
		* TODO make the matchListPath variable global so all classes can access it
		*/
		matchListPath = getFilesDir().getPath() + File.separator + "matches" + File.separator + "matchList.plx";
		final File gamesListFile = new File(matchListPath);

		// FOR TESTING ///////////////////////////////////////
		//File fileToDelete = new File(matchListPath);
		//fileToDelete.delete();
		// FOR TESTING ///////////////////////////////////////

		// Initialize matchList with th matchList file
		// check to see if the existing games ArrayList was written to disk. If it was, read it and set the variable
		// If it wasn't, create a new one, and use it to store new games

		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
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
				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{

			}

		}.execute();

		context = getApplicationContext();

		// Setup signin button
		findViewById(R.id.button_exit).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				if (getApiClient() != null && getApiClient().isConnected())
				{
					signOut();
					findViewById(R.id.button_play_multiplayer).setBackgroundResource(R.drawable.plexor_button);
					findViewById(R.id.button_play_computer).setBackgroundResource(R.drawable.plexor_button);
					findViewById(R.id.button_inbox).setBackgroundResource(R.drawable.plexor_button);
				}
				else
				{
					finish();
				}

//				findViewById(R.id.button_sign_out).setVisibility(View.GONE);
//				findViewById(R.id.sign_out_shadow).setVisibility(View.GONE);
//				findViewById(R.id.button_sign_in).setVisibility(View.VISIBLE);
//				findViewById(R.id.sign_in_shadow).setVisibility(View.VISIBLE);
//				findViewById(R.id.button_create_game).setVisibility(View.GONE);
//				findViewById(R.id.create_game_shadow).setVisibility(View.GONE);
//				findViewById(R.id.button_view_games).setVisibility(View.GONE);
//				findViewById(R.id.view_games_shadow).setVisibility(View.GONE);
//				findViewById(R.id.button_inbox).setVisibility(View.GONE);
//				findViewById(R.id.inbox_shadow).setVisibility(View.GONE);
			}
		});

		findViewById(R.id.button_sign_in_google_play).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// start the asynchronous sign in flow
				// TODO not supposed to allow the user to attempt signin when signin is already occurring
				// set this up to check if signin is occurring already
				beginUserInitiatedSignIn();
			}
		});
	}// END onCreate

	@Override
	public void onStart()
	{
		super.onStart();
	}

	// This function is what gets called when you return from either the Play Games built-in inbox, or else the create game built-in interface.
	@Override
	public void onActivityResult(int request, int response, Intent data)
	{
		// It's VERY IMPORTANT for you to remember to call your superclass. BaseGameActivity will not work otherwise.
		super.onActivityResult(request, response, data);

		if (request == RC_LOOK_AT_MATCHES)
		{
			// Returning from the 'Select Match' dialog

			if (response != Activity.RESULT_OK)
			{
				// user canceled
				return;
			}

			TurnBasedMatch match = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

			if (match != null)
			{
				// updateMatch(match);
			}

			Log.d(TAG, "Match = " + match);
		}
		else if (request == RC_CREATE_GAME)
		{
			/*
			 * -Get game timelimit
			 * -Get ranked or unranked
			 * -Create a bitmask from passed data
			 * -TODO Get invited player name, currently only gets the player's ID
			 */

			if (response != Activity.RESULT_OK)
			{
				// User canceled
				return;
			}

			/*TODO add handling for the bitmask in Match.class*/
			long bitmask = 0;
			// get the invitee list
			final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

			// Values 2-9 are used for these types of matches in the bit mask
			final int moveTimeLimit = data.getIntExtra(CreateGameDialogActivity.EXTRA_MOVE_TIMELIMIT, 7);
			// Values 0-1 are used for these types of matches in the bit mask
			final boolean rankedBool = data.getBooleanExtra(CreateGameDialogActivity.EXTRA_RANKED_BOOLEAN, false);

			if (rankedBool)
			{
				bitmask += 1;
			}

			// Do "+1" to remove the possibility of bitmask being 1. 1 designates a ranked match.
			if (moveTimeLimit != 0)
			{
				bitmask += (moveTimeLimit + 1);
			}

			// Get automatch criteria
			Bundle autoMatchCriteria = null;

			int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
			int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

			if (minAutoMatchPlayers > 0)
			{
				autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, bitmask);
			}
			else
			{
				autoMatchCriteria = null;
			}

			TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder().addInvitedPlayers(invitees).setAutoMatchCriteria(autoMatchCriteria).build();

			//TODO write this game to the games list
			PlexorTurn newGame = new PlexorTurn();
			newGame.turnCounter = 0;
			newGame.multiplayerMatch = true;
			Globals.turnData = newGame;

			// Creates a match on google play
			Games.TurnBasedMultiplayer.createMatch(getApiClient(), tbmc).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>()
			{
				@Override
				public void onResult(TurnBasedMultiplayer.InitiateMatchResult result)
				{
					// Calls processResult which opens the match activity
					processResult(result);

				}
			});
		}
			else if (request == RC_OPEN_GAME)
			{

				if (response != Activity.RESULT_OK)
				{
					// user canceled
					return;
				}

				Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(getApiClient());
				startActivityForResult(intent, RC_LOOK_AT_MATCHES);

				TurnBasedMatch match = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
				Globals.turnData = PlexorTurn.unpersist(match.getData());
				String matchId = match.getMatchId();

				Games.TurnBasedMultiplayer.loadMatch(getApiClient(), matchId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>()
				{
					@Override
					public void onResult(TurnBasedMultiplayer.LoadMatchResult result)
					{
						// Calls processResult which opens the match activity
						processResult(result);

					}
				});
			}
	}// END onActivityResult

	@Override
	protected void onPause()
	{
		// Save any necessary data to a file.
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	/**
	 * TODO
	 */
	public void openMatchLocal(View view)
	{
		// Use the current time as the name for the match until the player changes the name on save
		//TODO setup games to use a uniqueID rather than a time.
		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		String name = date.toString();

		// Initialize turn data for the new local game
		PlexorTurn newGame = new PlexorTurn("Local "+name);
		matchList.add(newGame);
		Globals.turnData = newGame;

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

		Intent intent = new Intent(this, MatchLocal.class);
		// Add the name to the intent so the match class will have it
		//intent.putExtra("matchName", name);
		startActivity(intent);
	}// END openMatchLocal

	// Open the create-game UI. You will get back an onActivityResult
	// and figure out what to do.
	public void onCreateAGameClicked(View view)
	{
		// TODO create a popup side-slider form (possibly) that can be filled in
		// with match settings and then confirmed to create a match

		Intent intent = new Intent(this, CreateGameDialogActivity.class);
		startActivityForResult(intent, RC_CREATE_GAME);
	}// END onCreateAGameClicked

	public void onComputerGameClicked(View view)
	{
		// TODO create a popup side-slider form (possibly) that can be filled in
		// with match settings and then confirmed to create a match

		Globals.computerMatch = true;
		openMatchLocal(view);
	}// END onCreateAGameClicked

	public void onViewGamesClicked(View v)
	{
		Intent intent = new Intent(this, ActiveGamesList.class);
		startActivity(intent);
	}// END onViewGamesClicked

	public void onInboxClicked(View v)
	{
		//Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(getApiClient());
		//startActivityForResult(intent, RC_INBOX);
		//startActivityForResult(intent, RC_INBOX);

		Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(getApiClient());
		startActivityForResult(intent, RC_OPEN_GAME);
	}

	public void onOpenAGameClicked(View view)
	{
		// TODO consult the function calls below
		// Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(getApiClient());
		// startActivityForResult(intent, RC_LOOK_AT_MATCHES);
		//Intent intent = new Intent(this, ViewGamesDialog.class);
		//startActivityForResult(intent, RC_OPEN_GAME);
	}

	private void processResult(TurnBasedMultiplayer.InitiateMatchResult result)
	{
		TurnBasedMatch match = result.getMatch();

		if (!checkStatusCode(match, result.getStatus().getStatusCode()))
		{
			return;
		}

		openMatchView(match);

	}

	private void processResult(TurnBasedMultiplayer.LoadMatchResult result)
	{
		TurnBasedMatch match = result.getMatch();
		//matchData = PlexorTurn.unpersist(mMatch.getData());

		if (!checkStatusCode(match, result.getStatus().getStatusCode()))
		{
			return;
		}

		openMatchView(match);

	}

	public void openMatchView(TurnBasedMatch match)
	{
		Intent intent = new Intent(this, MatchLocal.class);
		// Pass match data to the activity
		intent.putExtra(EXTRA_MATCH_DATA, match);
		startActivity(intent);
	}

	// Generic warning/info dialog
	protected void showWarning(String title, String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle(title).setMessage(message);

		// set dialog message
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				// if this button is clicked, close
				// current activity
			}
		});

		// create alert dialog
		mAlertDialog = alertDialogBuilder.create();

		// Show it
		mAlertDialog.show();
	}

	protected void showErrorMessage(TurnBasedMatch match, int statusCode, int stringId)
	{
		showWarning("Warning", getResources().getString(stringId));
	}

	// Returns false if something went wrong, probably. This should handle
	// more cases, and probably report more accurate results.
	protected boolean checkStatusCode(TurnBasedMatch match, int statusCode)
	{
		switch (statusCode)
		{
			case GamesStatusCodes.STATUS_OK:
				return true;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
				// This is OK; the action is stored by Google Play Services and will
				// be dealt with later.
				//Toast.makeText(this, "Stored action for later.  (Please remove this toast before release.)", TOAST_DELAY).show();
				// NOTE: This toast is for informative reasons only; please remove
				// it from your final application.
				return true;
			case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
				showErrorMessage(match, statusCode, R.string.status_multiplayer_error_not_trusted_tester);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
				showErrorMessage(match, statusCode, R.string.match_error_already_rematched);
				break;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
				showErrorMessage(match, statusCode, R.string.network_error_operation_failed);
				break;
			case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
				showErrorMessage(match, statusCode, R.string.client_reconnect_required);
				break;
			case GamesStatusCodes.STATUS_INTERNAL_ERROR:
				showErrorMessage(match, statusCode, R.string.internal_error);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
				showErrorMessage(match, statusCode, R.string.match_error_inactive_match);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
				showErrorMessage(match, statusCode, R.string.match_error_locally_modified);
				break;
			default:
				showErrorMessage(match, statusCode, R.string.unexpected_status);
				Log.d(TAG, "Did not have warning or string to deal with: " + statusCode);
		}

		return false;
	}

	//@Override
	public void onShowLeaderboardsRequested()
	{
		if (isSignedIn())
		{
			startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()),5001);
		}
		else
		{
			//super.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onSignInFailed()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSignInSucceeded()
	{
		// TODO Auto-generated method stub

		findViewById(R.id.button_play_multiplayer).setBackgroundResource(R.drawable.plexor_button_play_multiplayer);
		findViewById(R.id.button_play_computer).setBackgroundResource(R.drawable.plexor_button_play_computer);
		findViewById(R.id.button_inbox).setBackgroundResource(R.drawable.plexor_button_inbox);

		//findViewById(R.id.button_sign_out).setVisibility(View.VISIBLE);
		//findViewById(R.id.sign_out_shadow).setVisibility(View.VISIBLE);
		//findViewById(R.id.button_sign_in).setVisibility(View.GONE);
		//findViewById(R.id.button_create_game).setVisibility(View.VISIBLE);
		//findViewById(R.id.button_view_games).setVisibility(View.VISIBLE);
		//findViewById(R.id.button_inbox).setVisibility(View.VISIBLE);

	}

}
