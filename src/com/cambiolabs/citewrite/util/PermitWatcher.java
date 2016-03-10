package com.cambiolabs.citewrite.util;

import java.util.TimerTask;

public abstract class PermitWatcher extends TimerTask 
{
	public static boolean changed = false;

	public PermitWatcher() 
	{
	}

	public final void run() 
	{
		if(PermitWatcher.changed) 
		{
			PermitWatcher.changed = false;
			onChange();
		}
	}
	
	public static void update()
	{
		if(PermitWatcher.changed == false)
		{
			PermitWatcher.changed = true;
		}
	}

	protected abstract void onChange();
}
