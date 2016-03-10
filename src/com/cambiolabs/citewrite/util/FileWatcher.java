package com.cambiolabs.citewrite.util;

import java.io.File;
import java.util.TimerTask;

public abstract class FileWatcher extends TimerTask 
{
	public static long delay = 1000*60; //wait a minute
	public static long poll = 1000*60; //check every minute
	private long timeStamp;
	private File file;

	public FileWatcher(File file) 
	{
		this.file = file;
		this.timeStamp = file.lastModified();
	}

	public final void run() 
	{
		long ts = file.lastModified();

		if( this.timeStamp != ts ) 
		{
			System.out.println("File Changed: " + this.file.getName());
			this.timeStamp = ts;
			onChange(file);
		}
	}

	protected abstract void onChange(File file);
}
