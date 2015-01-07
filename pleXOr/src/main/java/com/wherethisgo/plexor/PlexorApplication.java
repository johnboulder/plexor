package com.wherethisgo.plexor;

import android.app.Application;
import android.content.Context;

public class PlexorApplication extends Application
{
	private static Context context;

	public static Context getAppContext()
	{
		return PlexorApplication.context;
	}

	public void onCreate()
	{
		super.onCreate();
		PlexorApplication.context = getApplicationContext();
	}
}
