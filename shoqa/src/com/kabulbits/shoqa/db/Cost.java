package com.kabulbits.shoqa.db;

public class Cost {
	
	public int id;
	public int value;
	public String name;
	
	public Cost(int id, int value, String name){
		this.id = id;
		this.value = value;
		this.name = name;
	}

	public String toString() {
		return this.name;
	}

	public boolean equals(Object obj) {
		if(obj instanceof Cost){
			return ((Cost)obj).id == this.id;
		}
		return false;
	}
}
