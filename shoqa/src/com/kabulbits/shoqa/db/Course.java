package com.kabulbits.shoqa.db;

public class Course {
	
	public int id;
	public int year;
	public int grade;
	public int shift;
	public String name;
	
	public Course(int id, int grade, String name){
		this.id = id;
		this.grade = grade;
		this.name = name;
	}
	public Course(int id, int year, int grade, String name){
		this.id = id;
		this.year = year;
		this.grade = grade;
		this.name = name;
	}
	
	public Course(int id, int year, int grade, int shift, String name){
		this.id = id;
		this.year = year;
		this.grade = grade;
		this.shift = shift;
		this.name = name;
	}
	
	public String toString(){
		return String.format("%d - %s", this.grade, this.name);
	}
	
	public boolean equals(Object o){
		if (o instanceof Course){
			return this.id == ((Course)o).id;
		}
		return false;
	}
}
