package com.cambiolabs.citewrite.util;

import java.util.TimerTask;

public abstract class CodesWatcher extends TimerTask 
{
	public static boolean changed = false;

	public CodesWatcher() 
	{
	}

	public final void run() 
	{
		if(CodesWatcher.changed) 
		{
			CodesWatcher.changed = false;
			onChange();
		}
	}
	
	public static void update()
	{
		if(CodesWatcher.changed == false)
		{
			CodesWatcher.changed = true;
		}
	}

	protected abstract void onChange();
}
