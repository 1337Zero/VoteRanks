package de.cultcraft.zero.utils;

public class PlayerData
{
  private String Username = "";
  private int votes = 0;
  private int Rank = 0;
  private String lastvote = "";
  private String UUID = "";

  public PlayerData(String name, int votes, int rank, String lastvote, String UUID) {
    this.Username = name;
    this.votes = votes;
    this.Rank = rank;
    this.lastvote = lastvote;
    this.UUID = UUID;
  }

  public String getUsername() {
    return this.Username;
  }
  public void setUsername(String username) {
    this.Username = username;
  }
  public int getVotes() {
    return this.votes;
  }
  public void setVotes(int votes) {
    this.votes = votes;
  }
  public int getRank() {
    return this.Rank;
  }
  public void setRank(int rank) {
    this.Rank = rank;
  }
  public String getLastvote() {
    return this.lastvote;
  }
  public void setLastvote(String lastvote) {
    this.lastvote = lastvote;
  }
  public String toString() {
    return this.Username + ";" + this.votes + ";" + this.Rank + ";" + this.lastvote;
  }

  public String getUUID() {
    return this.UUID;
  }

  public void setUUID(String uUID) {
    this.UUID = uUID;
  }
}