package com.wherethisgo.plexor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TODO ensure that this class can execute even when the matchList is empty
 */
public class ActiveGamesList extends Activity
{
	ArrayList<PlexorTurn> matchList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_active_games_list);

		final ListView listview = (ListView) findViewById(R.id.listview);

		final ArrayList<String> list = new ArrayList<>();

		String matchListPath = getFilesDir().getPath() + File.separator + "matches" + File.separator + "matchList.plx";
		File gamesListFile = new File(matchListPath);

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
		}

		for (PlexorTurn i : matchList)
		{
			list.add(i.matchName);
		}

		final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);
		// TODO make sure this does what you think it does

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
				// TODO make sure this does what you think it does
				Intent intent = new Intent(ActiveGamesList.this, MatchLocal.class);
				// Add the name to the intent so the match class will have it
				final String item = (String) parent.getItemAtPosition(position);
				// Search for the turnData related to the match name
				for (PlexorTurn i : matchList)
				{
					if (i.matchName.equals(item))
					{
						Globals.turnData = i;
						break;
					}
				}
				startActivity(intent);
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_active_games_list, menu);
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

	private class StableArrayAdapter extends ArrayAdapter<String>
	{

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects)
		{
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i)
			{
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position)
		{
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds()
		{
			return true;
		}

	}
}
