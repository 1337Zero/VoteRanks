package de.cultcraft.zero.utils;

import org.bukkit.entity.Player;

public class WorkTask {
	
	private Player p;
	private int votes;
	
	public WorkTask(Player p,int votes) {
		this.p = p;
		this.votes = votes;
	}	
	
	public Player getP() {
		return p;
	}
	public void setP(Player p) {
		this.p = p;
	}
	public int getVotes() {
		return votes;
	}
	public void setVotes(int votes) {
		this.votes = votes;
	}
	
}
