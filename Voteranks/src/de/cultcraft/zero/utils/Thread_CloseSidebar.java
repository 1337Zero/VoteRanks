package de.cultcraft.zero.utils;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

public class Thread_CloseSidebar extends BukkitRunnable{

	private Player p;
	private DisplaySlot display;
	private String name;
	private boolean closed = false;
	
	public Thread_CloseSidebar(Player p,DisplaySlot display,String name){
		this.p = p;
		this.display = display;
		this.name = name;
	}
	
	public void run(){		
		if(!closed){
			if( p.getScoreboard().getObjective(name) != null){
				p.getScoreboard().getObjective(name).unregister();
			}
			if(p.getScoreboard().getObjective(display) != null){
				p.getScoreboard().clearSlot(display);
			} 	
		}			
	}
	public Player getPlayer(){
		return p;
	}
	public void setClosed(boolean lol){
		this.closed = lol;
	}

}
