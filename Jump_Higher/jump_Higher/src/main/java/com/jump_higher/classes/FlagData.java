package com.jump_higher.classes;
/**
 * @author Stav Bodik
 * This class used to hold user country flag data such as country name, and image source number.
 */
 public class FlagData{
	 private String countryName;
	 private String shortCountryName; // Israel - IL
	 private int imageSRC;
	
	 public FlagData(String countryName, String shortCountryName, int imageSRC) {
		super();
		this.countryName = countryName;
		this.imageSRC = imageSRC;
		this.shortCountryName=shortCountryName;
	}
	 
	 public String getCountryName() {
		return countryName;
	}
	 
	 public String getShortCountryName() {
		return shortCountryName;
	}
	 
	 public int getImageSRC() {
		return imageSRC;
	}
	 
 }