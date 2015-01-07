/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wherethisgo.plexor;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Updated and adapted for pleXOr by @author John Stockwell
 * Originally made by @author wolff
 */
public class PlexorTurn implements Serializable
{

	public static final String TAG = "EBTurn";

	// Contains a string comprised of 81 characters. x for first player, o for second player, and e for empty space
	public String serializedBoard = "";
	public int lastMoveX;
	public int lastMoveY;
	public int turnCounter;
	public boolean nextTurnSelectABlock = false;
	public String  firstPlayer          = null;
	public String  secondPlayer         = null;
	public String  matchName            = null;

	public PlexorTurn()
	{
	}

	public PlexorTurn(String name)
	{
		matchName = name;
	}

	// Creates a new instance of SkeletonTurn.
	static public PlexorTurn unpersist(byte[] byteArray)
	{

		if (byteArray == null)
		{
			Log.d(TAG, "Empty array---possible bug.");
			return new PlexorTurn();
		}

		String st = null;
		try
		{
			st = new String(byteArray, "UTF-16");
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
			return null;
		}

		Log.d(TAG, "====UNPERSIST \n" + st);

		PlexorTurn retVal = new PlexorTurn();

		try
		{
			JSONObject obj = new JSONObject(st);

			if (obj.has("serializedBoard"))
			{
				retVal.serializedBoard = obj.getString("serializedBoard");
			}
			if (obj.has("matchName"))
			{
				retVal.matchName = obj.getString("matchName");
			}
			if (obj.has("turnCounter"))
			{
				retVal.turnCounter = obj.getInt("turnCounter");
			}
			if (obj.has("lastMoveX"))
			{
				retVal.lastMoveX = obj.getInt("lastMoveX");
			}
			if (obj.has("lastMoveY"))
			{
				retVal.lastMoveY = obj.getInt("lastMoveY");
			}
			if (obj.has("firstPlayer"))
			{
				retVal.firstPlayer = obj.getString("firstPlayer");
			}
			if (obj.has("secondPlayer"))
			{
				retVal.secondPlayer = obj.getString("secondPlayer");
			}
			if (obj.has("nextMoveSelectBlock"))
			{
				retVal.nextTurnSelectABlock = obj.getBoolean("nextTurnSelectABlock");
			}

		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

	// This is the byte array we will write out to the TBMP API.
	public byte[] persist()
	{
		JSONObject retVal = new JSONObject();

		try
		{
			retVal.put("serializedBoard", serializedBoard);
			retVal.put("matchName", matchName);
			retVal.put("turnCounter", turnCounter);
			retVal.put("lastMoveX", lastMoveX);
			retVal.put("lastMoveY", lastMoveY);
			retVal.put("firstPlayer", firstPlayer);
			retVal.put("secondPlayer", secondPlayer);
			retVal.put("nextTurnSelectABlock", nextTurnSelectABlock);
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String st = retVal.toString();

		Log.d(TAG, "==== PERSISTING\n" + st);

		return st.getBytes(Charset.forName("UTF-16"));
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		stream.writeObject(serializedBoard);
		stream.writeObject(matchName);
		stream.writeInt(lastMoveX);
		stream.writeInt(lastMoveY);
		stream.writeInt(turnCounter);
		stream.writeBoolean(nextTurnSelectABlock);
		stream.writeObject(firstPlayer);
		stream.writeObject(secondPlayer);
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		serializedBoard = (String) stream.readObject();
		matchName = (String) stream.readObject();
		lastMoveX = stream.readInt();
		lastMoveY = stream.readInt();
		turnCounter = stream.readInt();
		nextTurnSelectABlock = stream.readBoolean();
		firstPlayer = (String) stream.readObject();
		secondPlayer = (String) stream.readObject();
	}
}
