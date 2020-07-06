package de.cultcraft.zero.voteranks;

import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.listener.VoteCommandExecuter;
import de.cultcraft.zero.listener.VoteListener;
import de.cultcraft.zero.utils.Goal;
import de.cultcraft.zero.utils.VoteWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


public class VoteRanks extends JavaPlugin {

	public Logger log = Bukkit.getLogger();
	private VoteListener listener = null;
	public static FileConfiguration config = null;
	public static DbTask dbtask = null;
	private VoteCommandExecuter cmd;
	public static VoteWorker task;
	public static VoteRanks instance;

	public void onEnable() {
		VoteRanks.instance = this;
		this.log.info("[" + getDescription().getName() + "] " + "Starte " + getDescription().getName() + " Version "+ getDescription().getVersion());
		this.log.info("[" + getDescription().getName() + "] " + "Lade config ... ");
		VoteRanks.config = LoadConfig();
		this.log.info("[" + getDescription().getName() + "] " + "Lade Datenbank ... ");
		VoteRanks.dbtask = new DbTask(VoteRanks.config);
		this.log.info("[" + getDescription().getName() + "] " + "Lade Worktask ... ");

		task = new VoteWorker(loadGoals());
		task.runTaskTimer(this, 100, config.getLong("Settings.timer"));

		this.log.info("[" + getDescription().getName() + "] " + "Lade Speicher ... ");
		this.log.info("[" + getDescription().getName() + "] " + "Lade Listener ... ");
		this.listener = new VoteListener(VoteRanks.dbtask, VoteRanks.config);
		getServer().getPluginManager().registerEvents(this.listener, this);
		this.log.info("[" + getDescription().getName() + "] " + "Lade Commandlistener ... ");
		this.cmd = new VoteCommandExecuter(VoteRanks.dbtask, VoteRanks.config, this);
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
		getCommand("storeStack").setExecutor(this.cmd);
		getCommand("editGoal").setExecutor(this.cmd);
		getCommand("votetopb").setExecutor(this.cmd);
		getCommand("votereload").setExecutor(this.cmd);
		this.log.info("[" + getDescription().getName() + "] " + "Suche Vault ... ");
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			this.log.info("[" + getDescription().getName() + "] " + "Vault wurde gefunden! ");
		} else {
			this.log.info("[" + getDescription().getName() + "] " + "Vault wurde nicht gefunden! ");
		}
		this.log.warning("[" + getDescription().getName() + "] " + "searching for a auto-reset date... ");
		dbtask.lookUpResetDate();

		this.log.info("[" + getDescription().getName() + "] " + "looking up db-version...");

		this.log.info("[" + getDescription().getName() + "] " + "geladen! ");
	}

	public void onDisable() {
		this.log.info("[" + getDescription().getName() + "] " + "Beende " + getDescription().getName() + " Version "
				+ getDescription().getVersion());
		VoteRanks.dbtask.Disable();
	}

	public FileConfiguration LoadConfig() {
		FileConfiguration config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		return getConfig();
	}
	
	

	public ArrayList<Goal> loadGoals() {
		@SuppressWarnings("unchecked")
		List<String> goals = (List<String>) VoteRanks.config.getList("Goals");
		ArrayList<Goal> makedGoals = new ArrayList<Goal>();
		for (int i = 0; i < goals.size(); i++) {
			try {
				Goal goal = Goal.GoalsFromString(goals.get(i).split(";"));
				makedGoals.add(goal);
			} catch (Exception e) {
				e.printStackTrace();
				this.log.info("[" + getDescription().getName() + "] " + " An Error occured while loading " + goals.get(i));
			}
		}
		this.log.info("[" + getDescription().getName() + "] " + " Loaded " + makedGoals.size() + " Goals!");
		return makedGoals;
	}

}