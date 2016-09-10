package de.cultcraft.zero.voteranks;

import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.listener.VoteCommandExecuter;
import de.cultcraft.zero.listener.VoteListener;
import de.cultcraft.zero.utils.Goal;
import de.cultcraft.zero.utils.VoteWorker;
import de.cultcraft.zero.utils.WorkTask;
import de.cultcraft.zero.utils.speicher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class VoteRanks extends JavaPlugin
{
  private Logger log = Bukkit.getLogger();
  private VoteListener listener = null;
  public static FileConfiguration config = null;
  private DbTask dbtask = null;
  private VoteCommandExecuter cmd;
  private speicher speicher = null;
  private Date d;
  private SimpleDateFormat format = new SimpleDateFormat("dd:MM:yyyy");
  public static VoteWorker task;

  public void onEnable() {
    this.log.info("[" + getDescription().getName() + "] " + "Starte " + getDescription().getName() + " Version " + getDescription().getVersion());
    this.log.info("[" + getDescription().getName() + "] " + "Lade config ... ");
    this.config = LoadConfig();
    this.log.info("[" + getDescription().getName() + "] " + "Lade Worktask ... ");    
    
    task = new VoteWorker(loadGoals());
    task.runTaskTimer(this, 100, config.getLong("Settings.timer"));
    
    this.log.info("[" + getDescription().getName() + "] " + "Lade Speicher ... ");
    this.speicher = new speicher(this);
    this.log.info("[" + getDescription().getName() + "] " + "Lade Datenbank ... ");
    this.dbtask = new DbTask(this.config, this.speicher);
    this.log.info("[" + getDescription().getName() + "] " + "Lade Listener ... ");
    this.listener = new VoteListener(this.dbtask, this.config);
    getServer().getPluginManager().registerEvents(this.listener, this);
    this.log.info("[" + getDescription().getName() + "] " + "Lade Commandlistener ... ");
    this.cmd = new VoteCommandExecuter(this.dbtask, this.config, this.speicher, this);
    getCommand("votelist").setExecutor(this.cmd);
    getCommand("votes").setExecutor(this.cmd);
    getCommand("setvotes").setExecutor(this.cmd);
    getCommand("clearvotes").setExecutor(this.cmd);
    getCommand("clearaccept").setExecutor(this.cmd);
    getCommand("cleardeny").setExecutor(this.cmd);
    getCommand("vote").setExecutor(this.cmd);
    getCommand("voteversion").setExecutor(this.cmd);
    getCommand("addvote").setExecutor(this.cmd);
    getCommand("savebook").setExecutor(this.cmd);
    this.log.info("[" + getDescription().getName() + "] " + "Suche Vault ... ");
    if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
      this.log.info("[" + getDescription().getName() + "] " + "Vault wurde gefunden! ");
      this.speicher.setPermSystem("Vault");
    } else {
      this.log.info("[" + getDescription().getName() + "] " + "Vault wurde nicht gefunden! ");
    }
    this.log.warning("[" + getDescription().getName() + "] " + "searching for a auto-reset date... ");
    LookUpResetDate();
    
    this.log.info("[" + getDescription().getName() + "] " + "looking up db-version..."); 
        
    if(speicher.getDbSystem().equalsIgnoreCase("mysql")){
    	try{
            ResultSet rs = dbtask.getResultSet1("SHOW FIELDS FROM `Votes`;");
            if(rs != null){
            	if(!rs.getString(1).equalsIgnoreCase("UUID")){
            		dbtask.ExexuteQuery("ALTER TABLE Votes ADD UUID VARCHAR(64)FIRST;");
            	    this.log.info("[" + getDescription().getName() + "] " + "Added a UUID table"); 
            	}
        	    this.log.info("[" + getDescription().getName() + "] " + "Your database is up-2-date"); 
            }
        }catch(SQLException e){
        	e.printStackTrace();
        }
    }else{
    	//Diffrent command cause not exit in sqlite
    	try{
            ResultSet rs = dbtask.getResultSet1("PRAGMA table_info(Votes);");
            if(rs != null){
            	            	
            	if(!rs.getString(2).equalsIgnoreCase("UUID")){
            		dbtask.ExexuteQuery("ALTER TABLE Votes ADD UUID VARCHAR(64);");
            		dbtask.ExexuteQuery("CREATE TABLE 'Votes_b' ('UUID' VARCHAR(64),'User' VARCHAR(30),'votes' int,'lastvote' VARCHAR(30));");
            		dbtask.ExexuteQuery("insert into 'Votes_b' Select UUID,User,Votes,lastvote From 'Votes';");
            		dbtask.ExexuteQuery("DROP table 'Votes';");
            		dbtask.ExexuteQuery("ALTER TABLE 'Votes_b' RENAME TO 'Votes';");
            		//insert into "Votes_b" Select UUID,User,Votes,lastvote From "Votes"
            		//DROP table "Votes"
            		//ALTER TABLE "Votes_b" RENAME TO "Votes";
            		
            		
            	    this.log.info("[" + getDescription().getName() + "] " + "Added a UUID table"); 
            	}
        	    this.log.info("[" + getDescription().getName() + "] " + "Your database is up-2-date"); 
            }
        }catch(SQLException e){
        	e.printStackTrace();
        }    	
    	
    }
    
    
    this.log.info("[" + getDescription().getName() + "] " + "geladen! ");    
  }

  public void onDisable() {
    this.log.info("[" + getDescription().getName() + "] " + "Beende " + getDescription().getName() + " Version " + getDescription().getVersion());
    this.dbtask.Disable();
  }
  public FileConfiguration LoadConfig() {
    FileConfiguration config = getConfig();
    config.options().copyDefaults(true);
    saveConfig();
    return getConfig();
  }
  public void LookUpResetDate() {
    if (this.config.getBoolean("Settings.auto-db-reset")) {
      ResultSet count = this.dbtask.getResultSet1("SELECT COUNT(Date) FROM `Resets` where `done` = 0");
      try {
        if (count.getInt(1) == 1) {
          ResultSet rs = this.dbtask.getResultSet1("SELECT * FROM `Resets` where `done` = 0");
          this.d = getDate(new Date(), this.config.getInt("Settings.auto-db-reset-time-days"));
          String date = rs.getString(1);
          int day = Integer.parseInt(date.split(":")[0]);
          int month = Integer.parseInt(date.split(":")[1]);
          int year = Integer.parseInt(date.split(":")[2]);
          if (isResetToday(day, month, year)) {
            this.log.warning("[" + getDescription().getName() + "] " + "reseting database...");
            this.dbtask.ExexuteQuery("UPDATE `Votes` set `votes` = 0");
            this.dbtask.ExexuteQuery("UPDATE `Resets` SET `done` = 1 WHERE `DATE` = '" + date + "'");
            Date d = getDate(new Date(), this.config.getInt("Settings.auto-db-reset-time-days"));
            this.dbtask.ExexuteQuery("INSERT INTO `Resets` VALUES('" + this.format.format(d) + "',0" + ")");
            this.log.warning("[" + getDescription().getName() + "] " + "database reseted!");
          } else {
            this.log.warning("[" + getDescription().getName() + "] " + "database reset is at " + day + "." + month + "." + year);
          }
        } else {
          this.log.warning("[" + getDescription().getName() + "] " + "No reset-Date found setting up a new one ...");
          Date d = getDate(new Date(), this.config.getInt("Settings.auto-db-reset-time-days"));
          this.dbtask.ExexuteQuery("INSERT INTO `Resets` VALUES('" + this.format.format(d) + "',0" + ")");
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      this.log.info("[" + getDescription().getName() + "] " + "auto-database-reset is disabled");
    }
  }

  private boolean isResetToday(int day, int month, int year) { String rdate = this.format.format(new Date());
    int rday = Integer.parseInt(rdate.split(":")[0]);
    int rmonth = Integer.parseInt(rdate.split(":")[1]);
    int ryear = Integer.parseInt(rdate.split(":")[2]);
    if ((rday == day) && 
      (rmonth == month) && 
      (ryear == year)) {
      return true;
    }

    return false; }

  private Date getDate(Date d, int days) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.add(5, days);
    return cal.getTime();
  }
  private ArrayList<Goal> loadGoals(){
	  List<String> goals = (List<String>) this.config.getList("Goals");
	  ArrayList<Goal>  makedGoals = new ArrayList<Goal>();
	  for(int i = 0; i < goals.size();i++){
		  try{
			  Goal goal = Goal.GoalsFromString(goals.get(i).split(";"));
			  makedGoals.add(goal);
		  }catch(Exception e){
			  e.printStackTrace();
			    this.log.info("[" + getDescription().getName() + "] " + " An Error occured while loading " + goals.get(i));
		  }	  
	  }
	  this.log.info("[" + getDescription().getName() + "] " + " Loaded " + makedGoals.size() + " Goals!");
	  return makedGoals;
  }
  
}