package de.cultcraft.zero.Database;

import de.cultcraft.zero.utils.PlayerData;
import de.cultcraft.zero.utils.ZAItem;
import de.cultcraft.zero.voteranks.VoteRanks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class DbTask {
	private FileConfiguration config = null;
	private MySqlDB db = null;
	private SqliteDb sdb = null;
	// private speicher speicher;
	private SimpleDateFormat format = new SimpleDateFormat("dd:MM:yyyy");

	public static String dbSystem = "sqlite";

	public DbTask(FileConfiguration config) {
		this.config = config;
		initdb();
	}

	private void initdb() {
		if (this.config.getBoolean("Settings.mysql"))
			try {
				this.db = new MySqlDB(this.config.getString("Settings.mysql-database-url"),
						this.config.getString("Settings.mysql-user"), this.config.getString("Settings.mysql-password"));
				CreateDb();
				DbTask.dbSystem = "mysql";
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
		if (DbTask.dbSystem.equalsIgnoreCase("mysql")) {
			try {
				ResultSet rs = getResultSet("SHOW FIELDS FROM `Votes`;");
				if (rs != null) {
					if (!rs.getString(1).equalsIgnoreCase("UUID")) {
						ExexuteQuery("ALTER TABLE Votes ADD UUID VARCHAR(64)FIRST;");
						VoteRanks.instance.log.info(
								"[" + VoteRanks.instance.getDescription().getName() + "] " + "Added a UUID table");
					}
					VoteRanks.instance.log.info(
							"[" + VoteRanks.instance.getDescription().getName() + "] " + "Your database is up-2-date");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			// Diffrent command cause not exit in sqlite
			try {
				ResultSet rs = getResultSet("PRAGMA table_info(Votes);");
				if (rs != null) {

					if (!rs.getString(2).equalsIgnoreCase("UUID")) {
						ExexuteQuery("ALTER TABLE Votes ADD UUID VARCHAR(64);");
						ExexuteQuery("CREATE TABLE 'Votes_b' ('UUID' VARCHAR(64),'User' VARCHAR(30),'votes' int,'lastvote' VARCHAR(30));");
						ExexuteQuery("insert into 'Votes_b' Select UUID,User,Votes,lastvote From 'Votes';");
						ExexuteQuery("DROP table 'Votes';");
						ExexuteQuery("ALTER TABLE 'Votes_b' RENAME TO 'Votes';");

						// insert into "Votes_b" Select UUID,User,Votes,lastvote From "Votes"
						// DROP table "Votes"
						// ALTER TABLE "Votes_b" RENAME TO "Votes";
						VoteRanks.instance.log.info("[" + VoteRanks.instance.getDescription().getName() + "] " + "Added a UUID table");
					}
					VoteRanks.instance.log.info("[" + VoteRanks.instance.getDescription().getName() + "] " + "Your database is up-2-date");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int storeItem(ItemStack stack) {
		try {
			return storeStack( stack);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private int storeStack(ItemStack stack) throws SQLException, IOException {
		Map<String, Object> seritem = ZAItem.itemToMap(stack);
		// Store item Data
		int iid = getNewItemID();
		for (String key : seritem.keySet()) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
			dataOutput.writeObject(seritem.get(key));
			dataOutput.close();
			String serobj = Base64Coder.encodeLines(outputStream.toByteArray());
			PreparedStatement insert = getPreparedStmnt("INSERT INTO `itemdata`(`name`,iid, `value`) VALUES (?,?,?)");
			insert.setString(1, key);
			insert.setInt(2, iid);
			insert.setString(3, serobj);
			insert.executeUpdate();			
		}
		return iid;
	}

	/**
	 * Returns the last iid of the db
	 * @return
	 */
	public int getNewItemID() {		
		try {
			PreparedStatement ps = getPreparedStmnt("SELECT COUNT(DISTINCT iid) FROM itemdata");			
			ResultSet rs = ps.executeQuery();
			rs.next();
			return rs.getInt(1)+1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public ResultSet exexuteQueryWithGenKeys(String query) throws SQLException {
		if (this.db != null) {
			return this.db.executeStmtWithGeneratedKeys(query);
		} else {
			throw new NotImplementedException("Sqlite doenst like to return generated keys!");
		}
	}

	public void storeItems(List<ItemStack> stacks) throws SQLException, IOException {
		for (ItemStack stack : stacks) {
			storeStack(stack);
		}
	}

	/**
	 * 
	 * @param iid
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public ItemStack loadItem(int iid) throws SQLException, ClassNotFoundException, IOException {
		// *
		PreparedStatement ps = getPreparedStmnt("SELECT * FROM `itemdata` WHERE `iid` = ?");
		ps.setInt(1, iid);
		ResultSet rs = ps.executeQuery();
		
		// idid/iid/name/value
		HashMap<String, Object> data = new HashMap<String, Object>();
		while(rs.next()) {
			// Alle Zeilen durchgehen
			Object value = null;
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(rs.getString(4)));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			value = dataInput.readObject();
			dataInput.close();
			data.put(rs.getString(3), value);
		}
		rs.close();
		ItemStack stack = ZAItem.MapToItem(data);
		return stack;
	}

	private boolean isResetToday(int day, int month, int year) {
		String rdate = this.format.format(new Date());
		int rday = Integer.parseInt(rdate.split(":")[0]);
		int rmonth = Integer.parseInt(rdate.split(":")[1]);
		int ryear = Integer.parseInt(rdate.split(":")[2]);
		if ((rday == day) && (rmonth == month) && (ryear == year)) {
			return true;
		}
		return false;
	}

	private Date getDate(Date d, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(5, days);
		return cal.getTime();
	}

	public void lookUpResetDate() {
		if (VoteRanks.config.getBoolean("Settings.auto-db-reset")) {
			ResultSet count = getResultSet("SELECT COUNT(Date) FROM `Resets` where `done` = 0");
			try {
				if (count.getInt(1) == 1) {
					ResultSet rs = getResultSet("SELECT * FROM `Resets` where `done` = 0");
					// this.d = getDate(new Date(),
					// VoteRanks.config.getInt("Settings.auto-db-reset-time-days"));
					String date = rs.getString(1);
					int day = Integer.parseInt(date.split(":")[0]);
					int month = Integer.parseInt(date.split(":")[1]);
					int year = Integer.parseInt(date.split(":")[2]);
					if (isResetToday(day, month, year)) {
						VoteRanks.instance.log.warning(
								"[" + VoteRanks.instance.getDescription().getName() + "] " + "reseting database...");
						clearAllVotes();

						PreparedStatement updateResets = getPreparedStmnt(
								"UPDATE `Resets` SET `done` = 1 WHERE `DATE` = ?");
						updateResets.setString(1, date);
						updateResets.executeUpdate();

						Date d = getDate(new Date(), VoteRanks.config.getInt("Settings.auto-db-reset-time-days"));
						PreparedStatement insert = getPreparedStmnt("INSERT INTO `Resets` VALUES(?,0)");
						insert.setString(1, this.format.format(d));
						insert.executeUpdate();
						VoteRanks.instance.log.warning(
								"[" + VoteRanks.instance.getDescription().getName() + "] " + "database reseted!");
					} else {
						VoteRanks.instance.log.warning("[" + VoteRanks.instance.getDescription().getName() + "] "
								+ "database reset is at " + day + "." + month + "." + year);
					}
				} else {
					VoteRanks.instance.log.warning("[" + VoteRanks.instance.getDescription().getName() + "] "
							+ "No reset-Date found setting up a new one ...");
					Date d = getDate(new Date(), VoteRanks.config.getInt("Settings.auto-db-reset-time-days"));
					// ExexuteQuery("INSERT INTO `Resets` VALUES('" + this.format.format(d) + "',0"
					// + ")");
					PreparedStatement insert = getPreparedStmnt("INSERT INTO `Resets` VALUES(?,0)");
					insert.setString(1, this.format.format(d));
					insert.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			VoteRanks.instance.log.info(
					"[" + VoteRanks.instance.getDescription().getName() + "] " + "auto-database-reset is disabled");
		}
	}

	private void CreateDb() {
		System.out.println("Erstelle Datenbanken...");
		try {
			if (this.db != null) {
				this.db.ExecuteStmt("CREATE TABLE IF NOT EXISTS Votes (UUID VARCHAR(64), User VARCHAR(30),votes int, lastvote VARCHAR(30));");
				this.db.ExecuteStmt("CREATE TABLE IF NOT EXISTS Resets (Date VARCHAR(30) ,done int);");
				this.db.ExecuteStmt("CREATE TABLE IF NOT EXISTS itemdata (idid INT AUTO_INCREMENT,iid INT,name text,value text,PRIMARY KEY (idid));");
				this.db.ExecuteStmt("CREATE TABLE IF NOT EXISTS Vote_backup (vbid INT AUTO_INCREMENT,UUID VARCHAR(64),username VARCHAR(30),votes int,backupdate VARCHAR(30),PRIMARY KEY (vbid));");

			} else {
				this.sdb.executeStmt("CREATE TABLE IF NOT EXISTS Votes (UUID VARCHAR(64), User VARCHAR(30),votes int, lastvote VARCHAR(30));");
				this.sdb.executeStmt("CREATE TABLE IF NOT EXISTS Resets (Date VARCHAR(30) ,done int);");
				this.sdb.executeStmt("CREATE TABLE IF NOT EXISTS itemdata (idid INT AUTO_INCREMENT,iid INT,name text,value text,PRIMARY KEY (idid));");
				this.sdb.executeStmt("CREATE TABLE IF NOT EXISTS Vote_backup (vbid INT AUTO_INCREMENT,UUID VARCHAR(64),username VARCHAR(30),votes int,backupdate VARCHAR(30),PRIMARY KEY (vbid));");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PreparedStatement getPreparedStmnt(String sql) throws SQLException {
		if (this.db != null) {
			return this.db.prepareStmnt(sql);
		} else {
			return this.sdb.conn.prepareStatement(sql);
		}
	}

	public ResultSet getResultSet(String query) {
		try {
			if (this.db != null) {
				return this.db.executeRs(query);
			}
			return this.sdb.executeQry(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void clearAllVotes() {
		if(VoteRanks.config.getBoolean("Settings.backup-on-delete")) {
			backupXTopVotes();
		}
		ExexuteQuery("UPDATE `Votes` set `votes` = 0");
	}
	
	private void backupXTopVotes() {
		int topamount = VoteRanks.config.getInt("Settings.backup-top-x");
		//CREATE TABLE IF NOT EXISTS Vote_backup (vbid INT AUTO_INCREMENT,UUID VARCHAR(64),username VARCHAR(30),votes int,backupdate VARCHAR(30),PRIMARY KEY (idid));
		ResultSet rs = this.getResultSet("SELECT * FROM Votes ORDER BY `votes` DESC LIMIT 0," + topamount);		
		try {
			PreparedStatement insert = getPreparedStmnt("INSERT INTO Vote_backup (UUID,username,votes,backupdate) VALUES (?,?,?,?)");
			while(rs.next()) {
				insert.setString(1, rs.getString("UUID"));
				insert.setString(2, rs.getString("User"));
				insert.setInt(3, rs.getInt("votes"));
				insert.setString(4, format.format(new Date()));
				insert.addBatch();
			}
			insert.executeBatch();
			rs.close();
		}catch(SQLException ex) {
			ex.printStackTrace();
		}		
	}

	public void ExexuteQuery(String query) {
		try {
			if (this.db != null)
				this.db.ExecuteStmt(query);
			else
				this.sdb.executeStmt(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void Disable() {
		if (this.db != null)
			this.db.closeConnection();
		else
			this.sdb.closeConnection();
	}

	public void updatePlayer(String name, UUID uniqueId) {
		try {
			PreparedStatement ps = getPreparedStmnt("UPDATE `Votes` SET `User` = ? where `UUID` = ?");
			ps.setString(1, name);
			ps.setString(2, uniqueId.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createUserInDataBase(String player) {
		try {
			if (!UserExistinDatabase(player)) {
				Date d = new Date();
				if (Bukkit.getServer().getPlayer(player) != null) {
					// this.dbtask.ExexuteQuery("INSERT INTO Votes VALUES ('" + + "','" + player +
					// "',0,'" + this.format.format(this.d) + "')");
					PreparedStatement insert = getPreparedStmnt("INSERT INTO Votes VALUES (?,?,0,?)");
					insert.setString(1, Bukkit.getServer().getPlayer(player).getUniqueId().toString());
					insert.setString(2, player);
					insert.setString(3, this.format.format(d));
					insert.executeUpdate();
				} else if (Bukkit.getOfflinePlayer(player) != null) {
					PreparedStatement insert = getPreparedStmnt("INSERT INTO Votes VALUES (?,?,0,?)");
					insert.setString(1, Bukkit.getOfflinePlayer(player).getUniqueId().toString());
					insert.setString(2, player);
					insert.setString(3, this.format.format(d));
					insert.executeUpdate();
				}
				if (this.config.getBoolean("Settings.debug-mode")) {
					System.out.println(
							"Fuer " + player + " wurde erfolgreich ein Eintrag in der Vote-Datenbank erstellt");
				}
			} else if (this.config.getBoolean("Settings.debug-mode")) {
				System.out.println("User " + player + " existiert in der Datenbank");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public PlayerData getPlayerData(String Player) {
		if (DbTask.dbSystem.equalsIgnoreCase("sqlite"))
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT * FROM `Votes` WHERE `User` = ?");
				ps.setString(1, Player);
				ResultSet data = ps.executeQuery();
				data.next();
				PlayerData back = new PlayerData(data.getString(2), data.getInt(3), 0, data.getString(4),data.getString(1));
				PreparedStatement ps2 = getPreparedStmnt("SELECT COUNT(User) FROM `Votes` WHERE `votes` > ?");
				ps2.setInt(1, data.getInt(3));
				ResultSet count = ps2.executeQuery();
				count.next();
				back.setRank(count.getInt(1) + 1);
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		else {
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT * FROM `Votes` WHERE `User` = ?");
				ps.setString(1, Player);
				ResultSet data = ps.executeQuery();
				data.next();
				PreparedStatement ps2 = getPreparedStmnt("SELECT COUNT(User) FROM `Votes` WHERE `votes` > ?");
				ps2.setInt(1, data.getInt(3));
				ResultSet count = ps2.executeQuery();
				count.next();
				return new PlayerData(data.getString(2), data.getInt(3), count.getInt(1) + 1, data.getString(4),data.getString(1));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
		return null;
	}

	public ArrayList<PlayerData> getTopTen() {
		ArrayList<PlayerData> back = new ArrayList<PlayerData>();
		if (DbTask.dbSystem.equalsIgnoreCase("sqlite")) {
			try {
				int rank = 1;
				PreparedStatement ps = getPreparedStmnt("SELECT * FROM `Votes` ORDER BY `votes` DESC LIMIT 0,10");
				ResultSet data = ps.executeQuery();
				data.next();
				do {
					if (!back.toString().contains(data.getString(2))) {
						back.add(new PlayerData(data.getString(2), data.getInt(3), rank, data.getString(4),
								data.getString(1)));
						rank++;
					}
				}while (data.next());
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else {
			try {
				int rank = 1;
				PreparedStatement ps = getPreparedStmnt("SELECT * FROM `Votes` ORDER BY `votes` DESC LIMIT 0,10");
				ResultSet data = ps.executeQuery();
				data.next();

				// ResultSet data = this.db.executeRs("SELECT * FROM `Votes` ORDER BY `votes`
				// DESC LIMIT 0,10");
				do {
					back.add(new PlayerData(data.getString(2), data.getInt(3), rank, data.getString(4),
							data.getString(1)));
					rank++;
				} while (data.next());
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
		return null;
	}
	public ArrayList<PlayerData> getTopTenBackup(String para) {
		ArrayList<PlayerData> back = new ArrayList<PlayerData>();
		if (DbTask.dbSystem.equalsIgnoreCase("sqlite")) {
			try {
				int rank = 1;
				PreparedStatement ps = getPreparedStmnt("SELECT * FROM `Vote_backup` WHERE backupdate like %?% ORDER BY `votes` DESC LIMIT 0,10");
				para = "%" + para + "%";
				ps.setString(1, para);
				
				ResultSet data = ps.executeQuery();
				data.next();
				do {
					if (!back.toString().contains(data.getString(2))) {
						back.add(new PlayerData(data.getString(3), data.getInt(4), rank, data.getString(5),
								data.getString(2)));
						rank++;
					}
				}while (data.next());
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else {
			try {
				int rank = 1;
				PreparedStatement ps = getPreparedStmnt("SELECT * FROM `Vote_backup` WHERE backupdate like ? ORDER BY `votes` DESC LIMIT 0,10");
				para = "%" + para + "%";
				ps.setString(1, para);
				ResultSet data = ps.executeQuery();
				data.next();

				// ResultSet data = this.db.executeRs("SELECT * FROM `Votes` ORDER BY `votes`
				// DESC LIMIT 0,10");
				do {
					//name,votes,rank,lastvote,uuid
					back.add(new PlayerData(data.getString(3), data.getInt(4), rank, data.getString(5),
							data.getString(2)));
					rank++;
				} while (data.next());
				return back;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[ERROR] Konnte keine Verbindung zu der Sqlite oder der Mysql db erstellen!");
		return null;
	}

	public int getRank(int votes) {
		if (DbTask.dbSystem.equalsIgnoreCase("sqlite"))
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `votes` > ?");
				ps.setInt(1, votes);
				ResultSet data = ps.executeQuery();
				data.next();
				return data.getInt(1);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		else {
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `votes` > ?");
				ps.setInt(1, votes);
				ResultSet data = ps.executeQuery();
				data.next();
				return data.getInt(1);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 1;
	}

	public int getSameRank(int votes) {
		if (DbTask.dbSystem.equalsIgnoreCase("sqlite"))
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `votes` = ?");
				ps.setInt(1, votes);
				ResultSet data = ps.executeQuery();
				data.next();
				return data.getInt(1) - 1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		else {
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `votes` = ?");
				ps.setInt(1, votes);
				ResultSet data = ps.executeQuery();

				// ResultSet data = this.db.executeRs("SELECT COUNT(User) FROM `Votes` where
				// `votes` = " + votes);
				data.next();
				if (data.getInt(1) - 2 < 0) {
					return 1;
				}
				return data.getInt(1) - 1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return 0;
	}

	public boolean UserExistinDatabase(String player) {
		if (DbTask.dbSystem.equalsIgnoreCase("sqlite"))
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `User` = ?");
				ps.setString(1, player);
				ResultSet data = ps.executeQuery();
				data.next();
				if (data.getInt(1) == 0) {
					return false;
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		else {
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `User` = ?");
				ps.setString(1, player);
				ResultSet data = ps.executeQuery();
				data.next();
				if (data.getInt(1) == 0) {
					return false;
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean UserExistinDatabase(UUID uuid) {
		if (DbTask.dbSystem.equalsIgnoreCase("sqlite"))
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `UUID` = ?");
				ps.setString(1, uuid.toString());

				ResultSet data = ps.executeQuery();
				data.next();
				if (data.getInt(1) == 0) {
					return false;
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		else {
			try {
				PreparedStatement ps = getPreparedStmnt("SELECT COUNT(User) FROM Votes where `UUID` = ?");
				ps.setString(1, uuid.toString());
				ResultSet data = ps.executeQuery();
				data.next();
				if (data.getInt(1) == 0) {
					return false;
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void setVotes(int amount, String username, String lastvote) {
		try {
			PreparedStatement ps = getPreparedStmnt("UPDATE `Votes` SET `votes` = ?,`lastvote` = ? where `User` = ?");
			ps.setInt(1, amount);
			ps.setString(2, lastvote);
			ps.setString(3, username);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
}