package com.wherethisgo.plexor;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

public class OpenStatsDialogActivity extends BaseGameActivity
{

	public static final int REQUEST_ACHIEVEMENTS = 10001;
	public static final int REQUEST_LEADERBOARDS = 10002;
	public static final String LEADERBOARD_ID = "CgkIgOOA37MWEAIQBw";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_stats_dialog);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_open_stats_dialog, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onLeaderboardClicked(View v)
	{
		startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(),LEADERBOARD_ID ), REQUEST_LEADERBOARDS);
	}

	public void onAchievementsClicked(View v)
	{
		startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIEVEMENTS);
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
