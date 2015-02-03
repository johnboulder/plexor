package com.wherethisgo.plexor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerBuffer;
import com.google.android.gms.games.Players;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;

public class CreateGameDialogActivity extends BaseGameActivity
{
	// For our intents to be sent to the calling activity
	final static int    RC_SELECT_PLAYERS    = 10000;
	//	final static int RC_LOOK_AT_MATCHES = 10001;
	//	final static int RC_CREATE_GAME = 10010;
	final static String EXTRA_OPPONENT_NAME  = "com.wherethisgo.plexor.opponent_name";
	final static String EXTRA_MOVE_TIMELIMIT = "com.wherethisgo.plexor.move_timelimit";
	final static String EXTRA_RANKED_BOOLEAN = "com.wherethisgo.plexor.ranked";

	// Form Data
	private ArrayList<String> invitees;
	private boolean isRanked           = false;
	private int     timeLimitSelection = 0;
	private Context context;
	/* TODO confirm what these two values are actually supposed to represent, and what they're doing is what we expect.
	 * As I understand, they're intended to be used to determine if the player selected an automatch player in the invite
	 * players UI. And if the player did, the room that's configured automatches our player to some random other player  */
	private int minAutoMatchPlayersRemaining = 0;
	private int maxAutoMatchPlayersRemaining = 0;

	public CreateGameDialogActivity()
	{

	}

	/**
	 * Handle radio button input
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// TODO look for default settings saved somewhere
		setContentView(R.layout.dialog_create_game);

		context = getApplicationContext();

		// Setup confirm dialog button
		findViewById(R.id.dialog_button_create_game).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (timeLimitSelection == 0)
				{
					// TODO Display a toast to the user that timelimit selection is invalid
					Toast toast = Toast.makeText(context, "Invalid timelimit selection", Toast.LENGTH_SHORT);
					toast.show();
					return;
				}

				// Gather form info in the intent
				Intent gameSettings = new Intent();
				gameSettings.putExtra(Games.EXTRA_PLAYER_IDS, invitees);
				gameSettings.putExtra(EXTRA_MOVE_TIMELIMIT, getSpinnerSelection());
				gameSettings.putExtra(EXTRA_RANKED_BOOLEAN, getRankedSelection());
				gameSettings.putExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, minAutoMatchPlayersRemaining);
				gameSettings.putExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, maxAutoMatchPlayersRemaining);

				// Pass info to calling activity (i.e. MainActivity)
				setResult(Activity.RESULT_OK, gameSettings);

				// Close dialog
				finish();
			}
		});

		// Setup cancel dialog button
		findViewById(R.id.dialog_button_create_game_cancel).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// We don't even bother calling setResult in this instance because all it means is that some
				// extra processing will occur when there isn't actually anything to process.
				// Update: Rethinking this previous statement. Possbility that there's a reason for returning this.
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		// Setup invite players dialog button
		findViewById(R.id.dialog_button_create_game_invite_players).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// Open the invite players UI
				// Must select at most 1 player and at least 1 player
				Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 1, true);
				startActivityForResult(intent, RC_SELECT_PLAYERS);
			}
		});

		((AdapterView<SpinnerAdapter>) findViewById(R.id.dialog_create_game_spinner_move_timelimit)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				timeLimitSelection = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				// TODO find out if when nothing is selected, the spinner changes its selected position
				System.err.println("####################################");
				System.err.println("timeLimitSelection: " + timeLimitSelection);
				System.err.println("####################################");
			}

		});

		Spinner spinner = (Spinner) findViewById(R.id.dialog_create_game_spinner_move_timelimit);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dialog_move_timelimits, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
	}

	/**
	 * Handle radio button input
	 */
	@Override
	public void onActivityResult(int request, int response, Intent data)
	{
		// Returned from 'Select players to Invite' dialog
		if (request == RC_SELECT_PLAYERS)
		{
			TextView invitedPlayersTV = (TextView) this.findViewById(R.id.dialog_text_view_invited_players);

			if (response != Activity.RESULT_OK)
			{
				// Hide
				this.findViewById(R.id.dialog_text_view_invited_players_shadow).setVisibility(View.GONE);
				invitedPlayersTV.setVisibility(View.GONE);

				// Get invite button
				Button inviteButton = (Button) findViewById(R.id.dialog_button_create_game_invite_players);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inviteButton.getLayoutParams();

				// Check if the bottom margin has been set to 0dp
				if (params.bottomMargin == 0)
				{
					// Set it back to 20dp
					params.bottomMargin = 20;
					inviteButton.setLayoutParams(params);
				}
				return;
			}

			/*TODO Create a custom UI for finding opponents. For this reason, I'm including the following code that I found
			 * public abstract Intent getSelectOpponentsIntent (GoogleApiClient apiClient, int minPlayers, int maxPlayers)
			 * startActivityForResult(Intent, int)
			 * 
			 * https://developer.android.com/reference/com/google/android/gms/games/multiplayer/realtime/RealTimeMultiplayer.html#getSelectOpponentsIntent(com.google.android.gms.common.api.GoogleApiClient, int, int)*/

			minAutoMatchPlayersRemaining = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
			maxAutoMatchPlayersRemaining = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

			// Get the invitee list TODO find a way to display the proper name. Currently only displays the number id of the person
			invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

			// Remove bottom margin from invite button
			Button inviteButton = (Button) findViewById(R.id.dialog_button_create_game_invite_players);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inviteButton.getLayoutParams();
			params.bottomMargin = 0;
			inviteButton.setLayoutParams(params);

			// Display the opponents id
			findViewById(R.id.dialog_text_view_invited_players_shadow).setVisibility(View.VISIBLE);
			invitedPlayersTV.setVisibility(View.VISIBLE);

			// Check if a real player was selected or not
			if (invitees.size() > 3)
			{
                /*TODO currently this causes the game to crash*/
				Games.Players.loadPlayer(getApiClient(), invitees.get(0)).setResultCallback(new ResultCallback<Players.LoadPlayersResult>()
				{
					@Override
					public void onResult(Players.LoadPlayersResult result)
					{
						// Calls processResult which sets the text of the TextView invitedPlayersTV to the invited player's display name
						processResult(result);
					}
				});
			} else
			{
				invitedPlayersTV.setText("Random Automatch Player");
			}

		}
	}

	private void processResult(Players.LoadPlayersResult result)
	{
		//TODO doesn't work...
		PlayerBuffer pb = result.getPlayers();
		Player player = pb.get(0);
		pb.close();

		TextView invitedPlayersTV = (TextView) this.findViewById(R.id.dialog_text_view_invited_players);
		invitedPlayersTV.setText(player.getDisplayName());

	}

	/**
	 * Handle radio button input
	 */
	public void onRadioButtonClicked(View v)
	{
		// Is the button now checked?
		boolean checked = ((RadioButton) v).isChecked();
		// Check which radio button was clicked
		switch (v.getId())
		{
			case R.id.dialog_create_game_radio_unranked:
			{
				if (checked)
				{
					isRanked = false;
				}
				break;
			}
			case R.id.dialog_create_game_radio_ranked:
			{
				if (checked)
				{
					// Display a toast
					Toast toast = Toast.makeText(context, "We're sorry, but ranked matches are not available at this time.", Toast.LENGTH_SHORT);
					toast.show();
					// Set isRanked to false
					isRanked = false;
					// Uncheck the radio button and re-check the other radio button
					((RadioButton) v).setChecked(false);
					((RadioButton) findViewById(R.id.dialog_create_game_radio_unranked)).setChecked(true);
				}
				break;
			}
		}
	}

	/**
	 * Handle radio button input
	 */
	private boolean getRankedSelection()
	{
		return isRanked;
	}

	/**
	 * Handle radio button input
	 */
	private int getSpinnerSelection()
	{
		// TODO Auto-generated method stub
		return timeLimitSelection;
	}

	/**
	 * Handle radio button input
	 */
	@Override
	public void onSignInFailed()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Handle radio button input
	 */
	@Override
	public void onSignInSucceeded()
	{
		// TODO Auto-generated method stub

	}

}
