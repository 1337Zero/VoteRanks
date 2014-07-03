package de.cultcraft.zero.voteranks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.listener.VoteCommandExecuter;
import de.cultcraft.zero.listener.VoteListener;
import de.cultcraft.zero.utils.speicher;

public class VoteRanks extends JavaPlugin{
	
	private Logger log = Bukkit.getLogger();
	private VoteListener listener = null;
	private FileConfiguration config = null;
	private DbTask dbtask = null;
	private VoteCommandExecuter cmd;
	private speicher speicher = null;
	private Date d;
	private SimpleDateFormat format = new SimpleDateFormat("dd:MM:yyyy");
	
	public void onEnable(){		
		//Git
		log.info("[" + getDescription().getName() + "] " + "Starte " + getDescription().getName() + " Version " + getDescription().getVersion());	
		log.info("[" + getDescription().getName() + "] " + "Lade config ... ");
		config = LoadConfig();
		log.info("[" + getDescription().getName() + "] " + "Lade Speicher ... ");
		speicher = new speicher(this);
		log.info("[" + getDescription().getName() + "] " + "Lade Datenbank ... ");
		dbtask = new DbTask(config, speicher);
		log.info("[" + getDescription().getName() + "] " + "Lade Listener ... ");
		listener = new VoteListener(dbtask, config);
		getServer().getPluginManager().registerEvents(listener, this);	
		log.info("[" + getDescription().getName() + "] " + "Lade Commandlistener ... ");
		cmd =  new VoteCommandExecuter(dbtask, config,speicher,this);		
		getCommand("votelist").setExecutor(cmd);
		getCommand("votes").setExecutor(cmd);
		getCommand("setvotes").setExecutor(cmd);
		getCommand("clearvotes").setExecutor(cmd);
		getCommand("clearaccept").setExecutor(cmd);
		getCommand("cleardeny").setExecutor(cmd);
		getCommand("vote").setExecutor(cmd);
		getCommand("voteversion").setExecutor(cmd);
		getCommand("addvote").setExecutor(cmd);
		getCommand("savebook").setExecutor(cmd);
		log.info("[" + getDescription().getName() + "] " + "Suche Vault ... ");		
		if(Bukkit.getPluginManager().getPlugin("Vault") != null){
			log.info("[" + getDescription().getName() + "] " + "Vault wurde gefunden! ");
			speicher.setPermSystem("Vault");
		}else{
			log.info("[" + getDescription().getName() + "] " + "Vault wurde nicht gefunden! ");
		}
		log.warning("[" + getDescription().getName() + "] " + "searching for a auto-reset date... ");
		LookUpResetDate();
		log.info("[" + getDescription().getName() + "] " + "geladen! ");		
	}
	public void onDisable(){
		log.info("[" + getDescription().getName() + "] " + "Beende " + getDescription().getName() + " Version " + getDescription().getVersion());		
		dbtask.Disable();
	}
	public FileConfiguration LoadConfig() {
		FileConfiguration config = this.getConfig();		
		config.options().copyDefaults(true);
		saveConfig();
		return this.getConfig();
	}
	public void LookUpResetDate(){
		if(config.getBoolean("Settings.auto-db-reset")){
			ResultSet count = dbtask.getResultSet1("SELECT COUNT(Date) FROM `Resets` where `done` = 0");			
			try {
				if(count.getInt(1) == 1){
					ResultSet rs = dbtask.getResultSet1("SELECT * FROM `Resets` where `done` = 0");
					d = getDate(new Date(), config.getInt("Settings.auto-db-reset-time-days"));					
					String date = rs.getString(1);
					int day = Integer.parseInt(date.split(":")[0]);
					int month = Integer.parseInt(date.split(":")[1]);
					int year = Integer.parseInt(date.split(":")[2]);	
					if(isResetToday(day,month,year)){							
								log.warning("[" + getDescription().getName() + "] " +"reseting database...");
								dbtask.ExexuteQuery("UPDATE `Votes` set `votes` = 0");								
								dbtask.ExexuteQuery("UPDATE `Resets` SET `done` = 1 WHERE `DATE` = '" + date + "'");	
								Date d = getDate(new Date(),config.getInt("Settings.auto-db-reset-time-days"));
								dbtask.ExexuteQuery("INSERT INTO `Resets` VALUES('" + format.format(d) + "',0" + ")");
								log.warning("[" + getDescription().getName() + "] " +"database reseted!");				
					}else{
						log.warning("[" + getDescription().getName() + "] " +"database reset is at " + day + "." + month + "." + year);		
					}
				}else{
					log.warning("[" + getDescription().getName() + "] " +"No reset-Date found setting up a new one ...");
					Date d = getDate(new Date(),config.getInt("Settings.auto-db-reset-time-days"));			
					dbtask.ExexuteQuery("INSERT INTO `Resets` VALUES('" + format.format(d) + "',0" + ")");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			log.info("[" + getDescription().getName() + "] " + "auto-database-reset is disabled");
		}
	}
	private boolean isResetToday(int day,int month,int year){
		String rdate = format.format(new Date());
		int rday = Integer.parseInt(rdate.split(":")[0]);
		int rmonth = Integer.parseInt(rdate.split(":")[1]);
		int ryear = Integer.parseInt(rdate.split(":")[2]);					
		if(rday == day){						
			if(rmonth == month){							
				if(ryear == year){					
					return true;
				}
			}
		}
		return false;
	}
	private Date getDate(Date d,int days){	
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, days);		
		return cal.getTime();
	}

}
