package de.cultcraft.zero.utils;

public class PlayerData {

	private String Username = "";
	private int votes = 0;
	private int Rank = 0;
	private String lastvote = "";
	private String UUID = "";
	
	public PlayerData(String name,int votes,int rank,String lastvote,String UUID){
		this.Username = name;
		this.votes = votes;
		this.Rank = rank;
		this.lastvote = lastvote;
		this.UUID = UUID;
	}
	
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public int getVotes() {
		return votes;
	}
	public void setVotes(int votes) {
		this.votes = votes;
	}
	public int getRank() {
		return Rank;
	}
	public void setRank(int rank) {
		Rank = rank;
	}
	public String getLastvote() {
		return lastvote;
	}
	public void setLastvote(String lastvote) {
		this.lastvote = lastvote;
	}
	public String toString(){
		return Username + ";" + votes + ";" + Rank + ";" + lastvote;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}
	
	
}
