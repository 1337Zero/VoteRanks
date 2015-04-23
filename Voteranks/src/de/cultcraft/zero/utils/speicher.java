package de.cultcraft.zero.utils;

import de.cultcraft.zero.voteranks.VoteRanks;

import java.util.ArrayList;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class speicher
{
  private String PermSystem = "standart";
  private String DbSystem = "sqlite";

  private ArrayList<Player> cleardbcmd = new ArrayList();
  private static Permission perms = null;
  private VoteRanks main = null;

  private RegisteredServiceProvider rsp = null;
  private ArrayList<Thread_CloseSidebar> closed = new ArrayList();

  public String getDbSystem() {
    return this.DbSystem;
  }

  public void setDbSystem(String dbSystem) {
    this.DbSystem = dbSystem;
  }
  public speicher(VoteRanks main) {
    this.main = main;
  }

  public String getPermSystem() {
    return this.PermSystem;
  }
  public void setPermSystem(String permSystem) {
    this.PermSystem = permSystem;
  }
  public void addCleardbcmd(Player p) {
    this.cleardbcmd.add(p);
  }
  public void removeCleardbcmd(Player p) {
    this.cleardbcmd.remove(p);
  }
  public boolean hasPlayerCmdRemoveCmd(String player) {
    for (int i = 0; i < this.cleardbcmd.size(); i++) {
      if (((Player)this.cleardbcmd.get(i)).getName().equalsIgnoreCase(player)) {
        return true;
      }
    }
    return false;
  }
  public boolean hasPermission(Player p, String Permission) {
	  if(this.getPermSystem().equalsIgnoreCase("vault")){
		  if (this.rsp == null) {
		      this.rsp = this.main.getServer().getServicesManager().getRegistration(Permission.class);
		    }
		    perms = (Permission)this.rsp.getProvider();
		   
		    return perms.has(p, Permission);
	  }else{
		  return p.hasPermission(Permission);
	  }
  }

  public ArrayList<Thread_CloseSidebar> getClosed() {
    return this.closed;
  }
  public void addnewThread(Thread_CloseSidebar closed) {
    Thread_CloseSidebar thread = getThread(closed.getPlayer().getName());
    if (thread == null) {
      this.closed.add(closed);
    } else {
      thread.setClosed(true);
      this.closed.add(closed);
    }
  }

  private Thread_CloseSidebar getThread(String Player) { for (int i = 0; i < this.closed.size(); i++) {
      if (((Thread_CloseSidebar)this.closed.get(i)).getPlayer().getName().equalsIgnoreCase(Player)) {
        return (Thread_CloseSidebar)this.closed.get(i);
      }
    }
    return null;
  }
}