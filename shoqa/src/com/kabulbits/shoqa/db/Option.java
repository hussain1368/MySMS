package com.kabulbits.shoqa.db;

public class Option {
	
	public int key;
	public int type = 0;
	public String value;

	public Option(int key)
	{
		this(key, "");
	}
	
	public Option(String value)
	{
		this(0, value);
	}
	
	public Option(int key, String value)
	{
		this.key = key;
		this.value = value;
	}
	
	public Option(int key, int type, String value)
	{
		this.key = key;
		this.type = type;
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(obj instanceof Option)
		{
			Option option = (Option) obj;
			return option.key == this.key;
		}
		return false;
	}
}
