package de.cultcraft.zero.utils;

import java.util.ArrayList;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import de.cultcraft.zero.voteranks.VoteRanks;

public class speicher {
	private String PermSystem = "standart";
	private String DbSystem = "sqlite";
	
	private ArrayList<Player> cleardbcmd = new ArrayList<Player>();
	private static Permission perms = null;
	private VoteRanks main = null;	
	@SuppressWarnings("rawtypes")	
	private RegisteredServiceProvider rsp = null;
	private ArrayList<Thread_CloseSidebar> closed = new ArrayList<Thread_CloseSidebar>();
	
	public String getDbSystem() {
		return DbSystem;
	}

	public void setDbSystem(String dbSystem) {
		DbSystem = dbSystem;
	}	
	public speicher(VoteRanks main){
		this.main = main;	
	}
	
	public String getPermSystem() {
		return PermSystem;
	}
	public void setPermSystem(String permSystem) {
		PermSystem = permSystem;
	}
	public void addCleardbcmd(Player p) {
		cleardbcmd.add(p);
	}
	public void removeCleardbcmd(Player p) {
		cleardbcmd.remove(p);
	}
	public boolean hasPlayerCmdRemoveCmd(String player){
		for(int i = 0; i < cleardbcmd.size();i++){
			if(cleardbcmd.get(i).getName().equalsIgnoreCase(player)){
				return true;
			}
		}
		return false;
	}
	public boolean hasPermission(Player p,String Permission){
		if(rsp == null){
			rsp = main.getServer().getServicesManager().getRegistration(Permission.class);
		}		
	    perms = (Permission)rsp.getProvider();
		if(PermSystem.equalsIgnoreCase("standart")){
			return p.hasPermission(Permission);
		}else{
			return perms.has(p, Permission);
		}
	}
	public ArrayList<Thread_CloseSidebar> getClosed(){		
		return closed;
	}
	public void addnewThread(Thread_CloseSidebar closed){
			Thread_CloseSidebar thread = getThread(closed.getPlayer().getName());
			if(thread == null){
				this.closed.add(closed);				
			}else{
				thread.setClosed(true);	
				this.closed.add(closed);
		}		
	}
	private Thread_CloseSidebar getThread(String Player){
		for(int i = 0; i < closed.size();i++){
			if(closed.get(i).getPlayer().getName().equalsIgnoreCase(Player)){
				return closed.get(i);
			}
		}
		return null;
	}	
}
