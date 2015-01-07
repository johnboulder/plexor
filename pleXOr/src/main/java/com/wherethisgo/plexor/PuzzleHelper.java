package com.wherethisgo.plexor;

//import java.util.Stack;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
//import android.graphics.drawable.ColorDrawable;
//import android.content.Intent;
//import android.support.v4.app.DialogFragment;

public class PuzzleHelper extends Activity
{

	final String X = "X";
	final String O = "O";
	Drawable DEFAULT_COLOR;
	EditText ViewArray[][] = new EditText[9][9];
	private Button button_solve;
	private Button button_clear;
	//private Stack<EditText> invalidCells;

	private boolean playerIsX = true;
	//click listener, checks for playerX or O.
	public OnClickListener onClickListener = new OnClickListener()
	{
		@Override
		public void onClick(final View v)
		{
			if (playerIsX)
			{
				for (int i = 0; i < 9; i++)
				{
					for (int j = 0; j < 9; j++)
					{
						if (v == ViewArray[i][j])
						{
							if (ViewArray[i][j].toString() == "")
							{
								ViewArray[i][j].setText(X);
							} else
							{
								return;
							}
						}
					}
				}

			} else
			{
				for (int i = 0; i < 9; i++)
				{
					for (int j = 0; j < 9; j++)
					{
						if (v == ViewArray[i][j])
						{
							if (ViewArray[i][j].toString() == "")
							{
								ViewArray[i][j].setText(O);
							} else
							{
								return;
							}
						}
					}
				}
			}
		}
	};
	//global variable used for identifying unassigned positions
	private int Row = -1, Column = -1;
	private int grid[][] = new int[9][9];

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Show the Up button in the action bar.
		//setupActionBar();

		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				ViewArray[i][j].setOnClickListener(onClickListener);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

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

	public void EnableBlock(int blockNum)
	{

	}

	/**
	 * TODO  don't know if I actually need this
	 */
	private void UpdateGrid()
	{
		for (int i = 0; i < 9; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				grid[i][j] = StrToInt(ViewArray[i][j].getText().toString());
			}
		}
	}

	//converts a string to an integer value. used to throw a flag (value 0) if the entry is invalid
	private String IntToStr(int value)
	{
		String newValue = "0";

		switch (value)
		{
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
	 * TODO refactor so that this possibly checks for rows containing 3 of any kind in a row or column
	 * consider giving class for such operations
	 */
	private boolean NoConflicts(int row, int col, int entry)
	{
		for (int i = 0; i < 9; i++)
		{
			if (grid[row][i] == entry)
			{
				return false;
			}

			if (grid[i][col] == entry)
			{
				return false;
			}
		}

		return true;
	}

	private class GenericTextWatcher implements TextWatcher
	{
		private View view;

		private GenericTextWatcher(View view)
		{
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
		{
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
		{
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		public void afterTextChanged(Editable editable)
		{

		}
	}

}
