package com.jump_higher.classes;

import com.jump_higher.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Stav Bodik
 * This class used to hold logged in user account and gaming information .
 */
public class User {
	
	private String userName;
	private String password;
	private String email;
	private int userID=-1;
	private FlagData flagData;
	private int rank=0;
	
	private String bestTime="00:00:00";
	private int bestLevel=1;
	
	private String reachedTime="00:00:00";
	private int reachedLevel=0;

	private Bitmap profileImage;
	
	public User(String userName, String password, String email, int userID, FlagData flagData,Context context) {
		super();
		this.userName = userName;
		this.password = password;
		this.email = email;
		this.userID = userID;
		this.flagData = flagData;
		this.profileImage=BitmapFactory.decodeResource(context.getResources(), R.drawable.anonymprofil);
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setProfileImage(Bitmap profileImage) {
		this.profileImage = profileImage;
	}
	
	public Bitmap getProfileImage() {
		return profileImage;
	}
	
	
	public int getBestLevel() {
		return bestLevel;
	}
	
	public void setBestLevel(int bestLevel) {
		this.bestLevel = bestLevel;
	}
	public String getReachedTime() {
		return reachedTime;
	}
	
	public void setReachedTime(String reachedTime) {
		try{
		this.reachedTime = reachedTime.substring(0, 8);
		}catch(Exception e){e.printStackTrace();}
	}
	
	public int getReachedLevel() {
		return reachedLevel;
	}
	
	public void setReachedLevel(int reachedLevel) {
		this.reachedLevel = reachedLevel;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	
	public FlagData getFlagData() {
		return flagData;
	}
	
	public void setFlagData(FlagData flagData) {
		this.flagData = flagData;
	}
	
	public int getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public String getBestTime() {
		return bestTime;
	}
	
	public void setBestTime(String bestTime,String from) {
		this.bestTime = bestTime;
	}
	
	public void setLevel(int level) {
		this.bestLevel = level;
	}
	
	public int getLevel() {
		return bestLevel;
	}
	
	
	
	
}
