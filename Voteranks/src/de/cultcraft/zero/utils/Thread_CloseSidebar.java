package de.cultcraft.zero.utils;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

public class Thread_CloseSidebar extends BukkitRunnable
{
  private Player p;
  private DisplaySlot display;
  private String name;
  private boolean closed = false;

  public Thread_CloseSidebar(Player p, DisplaySlot display, String name) {
    this.p = p;
    this.display = display;
    this.name = name;
  }

  public void run() {
    if (!this.closed) {
      if (this.p.getScoreboard().getObjective(this.name) != null) {
        this.p.getScoreboard().getObjective(this.name).unregister();
      }
      if (this.p.getScoreboard().getObjective(this.display) != null)
        this.p.getScoreboard().clearSlot(this.display);
    }
  }

  public Player getPlayer() {
    return this.p;
  }
  public void setClosed(boolean lol) {
    this.closed = lol;
  }
}