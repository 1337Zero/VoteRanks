package de.cultcraft.zero.Database;

import de.cultcraft.zero.utils.PlayerData;
import de.cultcraft.zero.utils.speicher;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class DbTask
{
  private FileConfiguration config = null;
  private MySqlDB db = null;
  private SqliteDb sdb = null;
  private speicher speicher;

  public DbTask(FileConfiguration config, speicher speicher)
  {
    this.config = config;
    this.speicher = speicher;
    initdb();
  }

  private void initdb() {
    if (this.config.getBoolean("Settings.mysql"))
      try {
        this.db = new MySqlDB(this.config.getString("Settings.mysql-database-url"), this.config.getString("Settings.mysql-user"), this.config.getString("Settings.mysql-password"));
        CreateDb();
        this.speicher.setDbSystem("mysql");
        System.out.println("MySql Datenbank geladen!");
      } catch (SQLException e) {
        System.out.println("Laden der MySql-Datenbank fehlgeschlagen!");
        try {
          this.sdb = new SqliteDb(this.config.getString("Settings.path-2-sqllitedb"));
          CreateDb();
          System.out.println("SqlLite Db geladen!");
        } catch (Exception e1) {
          System.out.println("Laden der Sql-Lite db fehlgeschlagen!");
          e1.printStackTrace();
        }
        e.printStackTrace();
      }
    else
      try {
        this.sdb = new SqliteDb(this.config.getString("Settings.path-2-sqllitedb"));
        CreateDb();
        System.out.println("SqlLite Db geladen!");
      } catch (Exception e1) {
        System.out.println("Laden der Sql-Lite db fehlgeschlagen!");
        e1.printStackTrace();
      }
  }

  private void CreateDb() {
    System.out.println("Erstelle Datenbanken...");
    try {
      if (this.db != null){
        this.db.ExecuteStmt("CREATE TABLE IF NOT EXISTS Votes (UUID VARCHAR(64), User VARCHAR(30),votes int, lastvote VARCHAR(30));");
        this.db.ExecuteStmt("CREATE TABLE IF NOT EXISTS Resets (Date VARCHAR(30) ,done int);");

       /* boolean updatet = false;
        try
        {
          ResultSet data = this.db.executeRs("SHOW FIELDS FROM `Votes`");
          do {
            if (data.getString(1).equalsIgnoreCase("UUID"))
              updatet = true;
          }
          while (data.next());
        } catch (SQLException e) {
          e.printStackTrace();
        }
        if (!updatet)
          this.db.ExecuteStmt("ALTER TABLE Votes ADD COLUMN UUID VARCHAR (64) FIRST");*/
      }
      else {
        this.sdb.executeStmt("CREATE TABLE IF NOT EXISTS Votes (UUID VARCHAR(64), User VARCHAR(30),votes int, lastvote VARCHAR(30));");
        this.sdb.executeStmt("CREATE TABLE IF NOT EXISTS Resets (Date VARCHAR(30) ,done int);");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public ResultSet getResultSet1(String query) {
    try { if (this.db != null) {
        return this.db.executeRs(query);
      }
      return this.sdb.executeQry(query);
    } catch (SQLException e)
    {
      e.printStackTrace();
    }return null;
  }

  public void ExexuteQuery(String query) {
    try {
      if (this.db != null)
        this.db.ExecuteStmt(query);
      else
        this.sdb.executeStmt(query);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void Disable() { if (this.db != null)
      this.db.closeConnection();
    else
      this.sdb.closeConnection(); }

  public PlayerData getPlayerData(String Player)
  {
    if (this.speicher.getDbSystem().equalsIgnoreCase("sqlite"))
      try {
        ResultSet data = this.sdb.executeQry("SELECT * FROM `Votes` WHERE `User` like '%" + Player + "%'");
        PlayerData back = new PlayerData(data.getString(2), data.getInt(3), 0, data.getString(4), data.getString(1));
        ResultSet count = this.sdb.executeQry("SELECT COUNT(User) FROM `Votes` WHERE `votes` > " + data.getInt(3));
        back.setRank(count.getInt(1) + 1);
        return back;
      } catch (SQLException e) {
        e.printStackTrace();
      }
    else {
      try {
        ResultSet data = this.db.executeRs("SELECT * FROM `Votes` WHERE `User` like '%" + Player + "%'");
        ResultSet count = this.db.executeRs("SELECT COUNT(User) FROM `Votes` WHERE `votes` > " + data.getInt(3));
        return new PlayerData(data.getString(2), data.getInt(3), count.getInt(1) + 1, data.getString(4), data.getString(1));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
    return null;
  }
  public ArrayList<PlayerData> getTopTen() {
    ArrayList<PlayerData> back = new ArrayList<PlayerData>();
    
    if (this.speicher.getDbSystem().equalsIgnoreCase("sqlite"))
      try {
        int rank = 1;
        ResultSet data = this.sdb.executeQry("SELECT * FROM `Votes` ORDER BY `votes` DESC LIMIT 0,10");
        do
          if (!back.toString().contains(data.getString(2))) {
        	  back.add(new PlayerData(data.getString(2), data.getInt(3), rank, data.getString(4), data.getString(1)));
        	  rank++;
          }
        while (data.next());
        
        return back;
      } catch (SQLException e) {
        e.printStackTrace();
      }
    else {
      try {
        int rank = 1;
        ResultSet data = this.db.executeRs("SELECT * FROM `Votes` ORDER BY `votes` DESC LIMIT 0,10");
        do {
          back.add(new PlayerData(data.getString(2), data.getInt(3), rank, data.getString(4), data.getString(1)));
          rank++;
        }while (data.next());
        return back;
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
    return null;
  }
  public int getRank(int votes) {
    if (this.speicher.getDbSystem().equalsIgnoreCase("sqlite"))
      try {
        ResultSet data = this.sdb.executeQry("SELECT COUNT(User) FROM Votes where `votes` > " + votes);
        return data.getInt(1);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    else {
      try {
        ResultSet data = this.db.executeRs("SELECT COUNT(User) FROM Votes where `votes` > " + votes);
        return data.getInt(1);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return 1;
  }
  public int getSameRank(int votes) {
    if (this.speicher.getDbSystem().equalsIgnoreCase("sqlite"))
      try {
        ResultSet data = this.sdb.executeQry("SELECT COUNT(User) FROM `Votes` where `votes` = " + votes);
        if (data.getInt(1) - 2 < 0) {
          return 1;
        }
        return data.getInt(1) - 1;
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    else {
      try {
        ResultSet data = this.db.executeRs("SELECT COUNT(User) FROM `Votes` where `votes` = " + votes);
        if (data.getInt(1) - 2 < 0) {
          return 1;
        }
        return data.getInt(1) - 1;
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return 0;
  }

  public boolean UserExistinDatabase(String Player) {
	    if (this.speicher.getDbSystem().equalsIgnoreCase("sqlite"))
	      try {
	        ResultSet data = this.sdb.executeQry("SELECT COUNT(User) FROM Votes where `User` like '" + Player + "'");
	        if (data.getInt(1) == 0) {
	          return false;
	        }
	        return true;
	      }
	      catch (SQLException e) {
	        e.printStackTrace();
	      }
	    else {
	      try {
	        ResultSet data = this.db.executeRs("SELECT COUNT(User) FROM Votes where `User` like '" + Player + "'");
	        if (data.getInt(1) == 0) {
	          return false;
	        }
	        return true;
	      }
	      catch (SQLException e) {
	        e.printStackTrace();
	      }
	    }
	    return false;
	  }
  public boolean UserExistinDatabase(UUID uuid) {
	    if (this.speicher.getDbSystem().equalsIgnoreCase("sqlite"))
	      try {
	        ResultSet data = this.sdb.executeQry("SELECT COUNT(User) FROM Votes where `UUID` = '" + uuid.toString() + "'");
	        if (data.getInt(1) == 0) {
	          return false;
	        }
	        return true;
	      }
	      catch (SQLException e) {
	        e.printStackTrace();
	      }
	    else {
	      try {
	        ResultSet data = this.db.executeRs("SELECT COUNT(User) FROM Votes where `UUID` = '" + uuid.toString() + "'");
	        if (data.getInt(1) == 0) {
	          return false;
	        }
	        return true;
	      }
	      catch (SQLException e) {
	        e.printStackTrace();
	      }
	    }
	    return false;
	  }
}