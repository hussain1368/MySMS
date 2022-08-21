package com.kabulbits.shoqa.db;

public class Subject 
{
	public int id;
	public int position;
	public String name;
	
	public Subject(int id, int pos, String name) {
		this.id = id;
		this.position = pos;
		this.name = name;
	}
	
	public Subject(int id, String name){
		this.id = id;
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Subject){
			return ((Subject)obj).id == this.id;
		}
		return false;
	}
}