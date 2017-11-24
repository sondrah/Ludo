package no.ntnu.imt3281.ludo.server;

import java.util.Vector;

public class User {

	private static int userID;
	private String username;
	private String password;
	private Vector <Integer> friendlist;
	// TOKEN?

	public User() {
		super();
	}
	
	public User(String uname, String pwd) {
		super();
		setUsername(uname);
		setPassword(pwd);
		friendlist = new Vector<>();
		
	}

	public void setPassword(String pwd) {
		this.password = pwd;
	}

	public void setUsername(String uname) {
		this.username = uname;
	}
	
	public int getID() {
		return userID;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public Vector<Integer> getFriendlist() {
		return friendlist;
	}

}
