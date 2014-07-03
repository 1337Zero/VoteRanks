package de.cultcraft.zero.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import de.cultcraft.zero.utils.PlayerData;
import de.cultcraft.zero.utils.speicher;

public class DbTask {
	
	private FileConfiguration config = null;
	private MySqlDB db = null;
	private SqliteDb sdb = null;
	private speicher speicher;
	
	public DbTask(FileConfiguration config,speicher speicher){
		this.config = config;
		this.speicher = speicher;
		initdb();
	}	
	private void initdb(){
		
		if(config.getBoolean("Settings.mysql")){		
			try {
				db = new MySqlDB(config.getString("Settings.mysql-database-url"), config.getString("Settings.mysql-user"), config.getString("Settings.mysql-password"));
				CreateDb();
				speicher.setDbSystem("mysql");
				System.out.println("MySql Datenbank geladen!");	
			} catch (SQLException e) {
				System.out.println("Laden der MySql-Datenbank fehlgeschlagen!");
				try {
					sdb = new SqliteDb(config.getString("Settings.path-2-sqllitedb"));
					CreateDb();
					System.out.println("SqlLite Db geladen!");
				} catch (Exception e1) {
					System.out.println("Laden der Sql-Lite db fehlgeschlagen!");
					e1.printStackTrace();
				}				
				e.printStackTrace();
			}					
		}else{			
			try {
				sdb = new SqliteDb(config.getString("Settings.path-2-sqllitedb"));
				CreateDb();
				System.out.println("SqlLite Db geladen!");				
			} catch (Exception e1) {
				System.out.println("Laden der Sql-Lite db fehlgeschlagen!");
				e1.printStackTrace();
			}
		}
	}
	private void CreateDb(){
		System.out.println("Erstelle Datenbanken...");
		try{
		if(db != null){
		//Votes   User/votes/lastvote
		//Resets   Date/done
			
			db.ExecuteStmt("CREATE TABLE IF NOT EXISTS Votes (UUID VARCHAR(64), User VARCHAR(30),votes int, lastvote VARCHAR(30));");
			db.ExecuteStmt("CREATE TABLE IF NOT EXISTS Resets (Date VARCHAR(30) ,done int);");
			
			boolean updatet = false;
			
			try {
				ResultSet data = db.executeRs("SHOW FIELDS FROM `Votes`");
				do{
					if(data.getString(1).equalsIgnoreCase("UUID")){
						updatet = true;
					}
				}while(data.next());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(!updatet){
				db.ExecuteStmt("ALTER TABLE Votes ADD COLUMN UUID VARCHAR (64) FIRST");
			}			
		}else{		
			sdb.executeStmt("CREATE TABLE IF NOT EXISTS Votes (UUID VARCHAR(64), User VARCHAR(30),votes int, lastvote VARCHAR(30));");
			sdb.executeStmt("CREATE TABLE IF NOT EXISTS Resets (Date VARCHAR(30) ,done int);");							
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ResultSet getResultSet1(String query){
		try {
		if(db != null){
			return db.executeRs(query);
		}else{
			return sdb.executeQry(query);			
		}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public void ExexuteQuery(String query){
		try {
			if(db != null){
				db.ExecuteStmt(query);
			}else{
				sdb.executeStmt(query);
			}
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	}
	public void Disable(){
		if(db != null){
			db.closeConnection();
		}else{
			sdb.closeConnection();
		}
	}

	public PlayerData getPlayerData(String Player, UUID UUID) {
		PlayerData uuiddata = getPlayerDataPerUUID(UUID);
		if(uuiddata == null){
			if (speicher.getDbSystem().equalsIgnoreCase("sqlite")) {
				try {
					ResultSet data = sdb.executeQry("SELECT * FROM `Votes` WHERE `User` = '"+ Player + "'");
					PlayerData back = new PlayerData(data.getString(2),data.getInt(3), 0, data.getString(4), data.getString(1));
					ResultSet count = sdb.executeQry("SELECT COUNT(User) FROM `Votes` WHERE `votes` > "	+ data.getInt(3));
					back.setRank((count.getInt(1) + 1));
					return back;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				try {
					ResultSet data = db.executeRs("SELECT * FROM `Votes` WHERE `User` = '" + Player + "'");
					ResultSet count = db.executeRs("SELECT COUNT(User) FROM `Votes` WHERE `votes` > "+ data.getInt(3));
					return new PlayerData(data.getString(2), data.getInt(3),(count.getInt(1) + 1), data.getString(4),data.getString(1));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
			return null;
		}
		return null;
	}
	private PlayerData getPlayerDataPerUUID(UUID UUID){
		if(UUID == null){
			return null;
		}
		if(speicher.getDbSystem().equalsIgnoreCase("sqlite")){
			try {
				ResultSet data = sdb.executeQry("SELECT * FROM `Votes` WHERE `UUID` = '" + UUID + "'");
				PlayerData back = new PlayerData(data.getString(2), data.getInt(3), 0, data.getString(4),data.getString(1));
				ResultSet count = sdb.executeQry("SELECT COUNT(User) FROM `Votes` WHERE `votes` > " + data.getInt(3));
				back.setRank((count.getInt(1) + 1));
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			try {
				ResultSet data = db.executeRs("SELECT * FROM `Votes` WHERE `UUID` = '" + UUID + "'");
				ResultSet count = db.executeRs("SELECT COUNT(User) FROM `Votes` WHERE `votes` > " + data.getInt(3));
				return new PlayerData(data.getString(2),data.getInt(3),(count.getInt(1) + 1),data.getString(4),data.getString(1));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
		return null;
	}
	public ArrayList<PlayerData> getTopTen(){
		ArrayList<PlayerData> back = new ArrayList<PlayerData>();
		if(speicher.getDbSystem().equalsIgnoreCase("sqlite")){
			try {
				int rank = 1;
				ResultSet data = sdb.executeQry("SELECT * FROM `Votes` ORDER BY `votes` DESC LIMIT 0,10");				
				do{
					if(!back.toString().contains(data.getString(1))){
						back.add(new PlayerData(data.getString(2),data.getInt(3),rank,data.getString(4),data.getString(1)));
						rank++;
					}					
				}while(data.next());				
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{			
			try {
				int rank = 1;
				ResultSet data = db.executeRs("SELECT * FROM `Votes` ORDER BY `votes` DESC LIMIT 0,10");
				do{
					back.add(new PlayerData(data.getString(2),data.getInt(3),rank,data.getString(4),data.getString(1)));
					rank++;
				}while(data.next());
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
		System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
		return null;
	}
	public int getRank(int votes){
		if(speicher.getDbSystem().equalsIgnoreCase("sqlite")){
			try {
				ResultSet data = sdb.executeQry("SELECT COUNT(User) FROM Votes where `votes` > " + votes);
				return data.getInt(1);
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}else{
			try {
				ResultSet data = db.executeRs("SELECT COUNT(User) FROM Votes where `votes` > " + votes);
				return data.getInt(1);
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}
		return 1;
	}
	public int getSameRank(int votes){		
		if(speicher.getDbSystem().equalsIgnoreCase("sqlite")){
			try {
				ResultSet data = sdb.executeQry("SELECT COUNT(User) FROM `Votes` where `votes` = " + votes);
				if(data.getInt(1) - 2 < 0){
					return 1;
				}else{
					return (data.getInt(1) - 1);
				}				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			try {
				ResultSet data = db.executeRs("SELECT COUNT(User) FROM `Votes` where `votes` = " + votes);				
				if(data.getInt(1) - 2 < 0){
					return 1;
				}else{
					return (data.getInt(1) - 1);					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	public boolean UserExistinDatabase(String Player){
		
		if(speicher.getDbSystem().equalsIgnoreCase("sqlite")){
			try {
				ResultSet data = sdb.executeQry("SELECT COUNT(User) FROM Votes where `User` = '" + Player + "'");
				if(data.getInt(1) == 0){
					return false;
				}else{
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			try {
				ResultSet data = db.executeRs("SELECT COUNT(User) FROM Votes where `User` = '" + Player + "'");
				if(data.getInt(1) == 0){
					return false;
				}else{
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;		
	}
 
	

}
