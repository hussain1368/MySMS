package com.kabulbits.shoqa.db;

import java.awt.image.BufferedImage;
import java.util.Date;

public class Student {
	
	public int id;
	public int code;
	public int docNo;
	public Date docDate;
	public String name;
	public String lname;
	public String fname;
	public String gfname;
	public String motherLang;
	public String idcard;
	public String phone;
	public String fatherJob;
	public String motherJob;
	public String bloodGroup;
	public String gender;
	public Date birthDate;
	public String birthPlace;
	public int mainProv;
	public int mainDist;
	public String mainVill;
	public int currProv;
	public int currDist;
	public String currVill;
	public String state;
	public int official;
	public int grade;
	public int regYear;
	public int gradYear;
	public float avg;
	public int memID;
	
	public byte [] image;
	public BufferedImage bufImage;

	public Student(int id)
	{
		this.id = id;
	}
	
	public Student(int id, String name, float avg, BufferedImage image)
	{
		this.id = id;
		this.name = name;
		this.avg = avg;
		this.bufImage = image;
	}
	
	public String [] array(){
		return new String [] {
				String.valueOf(this.code),
				String.valueOf(this.docNo),
				String.valueOf(this.name),
				String.valueOf(this.lname),
				String.valueOf(this.fname),
				String.valueOf(this.gfname),
				String.valueOf(this.motherLang),
				String.valueOf(this.idcard),
				String.valueOf(this.phone),
				String.valueOf(this.fatherJob),
				String.valueOf(this.motherJob),
		};
	}
	
	public void fill(String [] data)
	{
		try {
			this.code = Integer.parseInt(data[0]);
			this.docNo = Integer.parseInt(data[1]);
		} catch (NumberFormatException e) {
			this.code = 0;
			this.docNo = 0;
		}
		
		int i = 2;
		this.name = data[i++];
		this.lname = data[i++];
		this.fname = data[i++];
		this.gfname = data[i++];
		this.motherLang = data[i++];
		this.idcard = data[i++];
		this.phone = data[i++];
		this.fatherJob = data[i++];
		this.motherJob = data[i++];
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}


















