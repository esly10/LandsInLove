package com.cambiolabs.citewrite.data;

public class Version
{
	public int major = 0;
	public int minor = 0;
	public int build = 0;
	
	public Version(int major, int minor, int build)
	{
		this.major = major;
		this.minor = minor;
		this.build = build;
	}
	
	public Version(String ver) throws Exception
	{
		String[] parts = ver.split("\\.");
		if(parts.length != 3)
		{
			throw new Exception("Invalid version format.");
		}
		
		try
		{
			major = Integer.parseInt(parts[0]);
			minor = Integer.parseInt(parts[1]);
			build = Integer.parseInt(parts[2]);
		}
		catch(NumberFormatException e)
		{
			throw new Exception("Invalid version format.");
		}
	}
	
	public int compare(Version version)
	{
		if(this.major > version.major)
		{
			return 1;
		}
		else if(this.major < version.major)
		{
			return -1;
		}
		
		//majors are equal
		if(this.minor > version.minor)
		{
			return 1;
		}
		else if(this.minor < version.minor)
		{
			return -1;
		}
		
		//minors are equal
		if(this.build > version.build)
		{
			return 1;
		}
		else if(this.build < version.build)
		{
			return -1;
		}
		
		return 0;
	}
	
	public boolean isCompatible(Version version)
	{
		return (this.compare(version) == 0);
	}
	
	public boolean isMajorOrEqual(Version version)
	{
		return (this.compare(version) >= 0);
	}
	
	@Override
	public String toString()
	{
		return this.major + "." + this.minor + "." + this.build;
	}
}
