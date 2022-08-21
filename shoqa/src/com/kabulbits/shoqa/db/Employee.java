package com.kabulbits.shoqa.db;

import java.util.Date;

public class Employee 
{
	public int id;
	public String name;
	public String lname;
	public String fname;
	public String gfname;
	public String idcard;
	public String phone;
	public String mainAddress;
	public String currAddress;
	public String educField;
	public String graduPlace;
	public String graduYear;
	public String serviceDuration;
	public String previousJob;
	public String ngoExpr;
	public String educSeminars;
	public String nationalLangs;
	public String internLangs;
	public String abroudTours;
	public String provinceTours;
	public String crimes;
	public String punishments;
	
	public int educLevel;
	public int empType;
	public boolean isTeacher;
	
	public Date birthDate;
	public Date employDate;
	public Date leaveDate;
	
	public byte image [];

	public Employee() {
		this.id = 0;
		this.name = "";
	}
	
	public Employee(int id)
	{
		this.id = id;
	}

	public Employee(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public void fill(String data [])
	{
		int i = 0;
		
		this.name = data[i++];
		this.lname = data[i++];
		this.fname = data[i++];
		this.gfname = data[i++];
		this.idcard = data[i++];
		this.phone = data[i++];
		this.mainAddress = data[i++];
		this.currAddress = data[i++];
		this.educField = data[i++];
		this.graduPlace = data[i++];
		this.graduYear = data[i++];
		this.serviceDuration = data[i++];
		this.previousJob = data[i++];
		this.ngoExpr = data[i++];
		this.educSeminars = data[i++];
		this.nationalLangs = data[i++];
		this.internLangs = data[i++];
		this.abroudTours = data[i++];
		this.provinceTours = data[i++];
		this.crimes = data[i++];
		this.punishments = data[i++];
	}
	
	public String [] values()
	{
		return new String [] {
				this.name,
				this.lname,
				this.fname,
				this.gfname,
				this.idcard,
				this.phone,
				this.mainAddress,
				this.currAddress,
				this.educField,
				this.graduPlace,
				this.graduYear,
				this.serviceDuration,
				this.previousJob,
				this.ngoExpr,
				this.educSeminars,
				this.nationalLangs,
				this.internLangs,
				this.abroudTours,
				this.provinceTours,
				this.crimes,
				this.punishments,
		};
	}

	public String toString() {
		return String.valueOf(this.name);
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof Employee){
			return ((Employee)o).id == this.id;
		}
		return false;
	}
}
