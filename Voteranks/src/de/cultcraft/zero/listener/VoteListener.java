package de.cultcraft.zero.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.utils.ChatManager;
import de.cultcraft.zero.utils.PlayerData;
import de.cultcraft.zero.utils.WorkTask;
import de.cultcraft.zero.voteranks.VoteRanks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;

public class VoteListener implements Listener {
	private DbTask dbtask;
	private FileConfiguration config;
	private Date d;
	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy[hh:mm:ss]");

	public VoteListener(DbTask dbtask, FileConfiguration config) {
		this.dbtask = dbtask;
		this.config = config;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnVoteEvent(VotifierEvent event) {
		if (dbtask.UserExistinDatabase(event.getVote().getUsername())) {
			dbtask.createUserInDataBase(event.getVote().getUsername());
		}
		PerformVote(event.getVote());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		// Look if UUID and Name are up-2-date
		if (dbtask.UserExistinDatabase(event.getPlayer().getUniqueId())) {
			if (!dbtask.UserExistinDatabase(event.getPlayer().getName())) {
				dbtask.updatePlayer(event.getPlayer().getName(), event.getPlayer().getUniqueId());
			}
		} else if (dbtask.UserExistinDatabase(event.getPlayer().getName())) {
			dbtask.updatePlayer(event.getPlayer().getName(), event.getPlayer().getUniqueId());
		}

		if (!this.dbtask.UserExistinDatabase(event.getPlayer().getName())
				&& this.config.getBoolean("Settings.firstjoined-book")) {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMeta bmeta = (BookMeta) book.getItemMeta();
			book.setItemMeta(getBookMetafromPath(bmeta, this.config.getString("Settings.firstjoined-path")));
			event.getPlayer().getInventory().addItem(new ItemStack[] { book });
		}
		dbtask.createUserInDataBase(event.getPlayer().getName());
		ShowVotesOnTab(event.getPlayer());
	}

	private BookMeta getBookMetafromPath(BookMeta bmeta, String path) {
		List<String> list = loadStringListFromFile(path);
		for (int i = 0; i < list.size(); i++) {
			if (((String) list.get(i)).contains("author:"))
				bmeta.setAuthor(ChatManager.ColorIt(replaceUmlaute(((String) list.get(i)).split("author:")[1])));
			else if (((String) list.get(i)).contains("title:"))
				bmeta.setTitle(ChatManager.ColorIt(replaceUmlaute(((String) list.get(i)).split("title:")[1])));
			else if (((String) list.get(i)).contains("description:"))
				bmeta.setLore(replaceUmlaute(((String) list.get(i)).split("description:")[1].split(";")));
			else {
				bmeta.addPage(new String[] { replaceUmlaute((String) list.get(i)) });
			}
		}
		return bmeta;
	}

	private List<String> loadStringListFromFile(String path) {
		File file = new File(path);
		List<String> list = new ArrayList<String>();
		if (file.exists())
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
				String readed = "";
				readed = reader.readLine();
				while (readed != null) {
					list.add(ChatManager.ColorIt(readed));
					readed = reader.readLine();
				}
				reader.close();
			} catch (FileNotFoundException e) {
				System.out.println("File " + path + " not found!");
				e.printStackTrace();
				System.out.println("File " + path + " not found!");
			} catch (IOException e) {
				System.out.println("Cant read the file");
				e.printStackTrace();
			}
		else {
			list.add(ChatManager.ColorIt("&4Something went wrong, please report this to an admin"));
		}
		return list;
	}

	private String replaceUmlaute(String msg) {
		return ChatManager.ColorIt(msg).replace("[newline] ", "\n").replace("[newline]", "\n");
	}

	private List<String> replaceUmlaute(String[] list) {
		List<String> listreturn = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			listreturn.add(replaceUmlaute(list[i]));
		}
		return listreturn;
	}

	private void PerformVote(Vote vote) {
		dbtask.createUserInDataBase(vote.getUsername());
		if (vote.getUsername().length() > 0) {
			PlayerData data = this.dbtask.getPlayerData(vote.getUsername());
			if (data != null) {
				this.d = new Date();
				try {
					PreparedStatement stmnt = this.dbtask.getPreparedStmnt("UPDATE Votes SET votes = ? where User = ?");
					stmnt.setInt(1, (data.getVotes() + 1));
					stmnt.setString(2, vote.getUsername());
					stmnt.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					PreparedStatement stmnt = this.dbtask
							.getPreparedStmnt("UPDATE Votes SET votes = ?, lastvote = ? where User = ?");
					stmnt.setInt(1, (data.getVotes() + 1));

					Calendar cal = Calendar.getInstance();
					cal.setTime(d);
					stmnt.setString(2, this.format.format(this.d));
					stmnt.setString(3, vote.getUsername());
					stmnt.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				AktuUserRank(vote.getUsername(), data.getVotes() + 1, data.getRank());
				if (Bukkit.getPlayer(vote.getUsername()) != null) {
					ShowVotesOnTab(Bukkit.getPlayer(vote.getUsername()));
				}
			}

			if (this.config.getBoolean("Settings.debug-mode")) {
				System.out.println(vote.getUsername() + " hat auf " + vote.getServiceName() + " um "
						+ vote.getTimeStamp() + " gevotet");
			}
			if (this.config.getBoolean("Settings.debug-mode")) {
				System.out.println("User: " + vote.getUsername());
				System.out.println("IP: " + vote.getAddress());
				System.out.println("Service: " + vote.getServiceName());
				System.out.println("Zeit: " + vote.getTimeStamp());
				System.out.println("Votes: " + (data.getVotes() + 1));
				System.out.println("Rang: " + data.getRank());
			}

		} else if (this.config.getBoolean("Settings.debug-mode")) {
			System.out.println("[WARNUNG] Jemand hat versucht mit unvollständigen Informationen zu voten!");
			System.out.println("-----");
			System.out.println("User: '" + vote.getUsername() + "'");
			System.out.println("IP: '" + vote.getAddress() + "'");
			System.out.println("SERVICE: '" + vote.getServiceName() + "'");
			System.out.println("TIMESTAMP: '" + vote.getTimeStamp() + "'");
			System.out.println("-----");
		}
	}

	private void AktuUserRank(String Player, int votes, int oldrank) {
		int rank = this.dbtask.getRank(votes);
		if (rank == 0) {
			rank = 1;
		}
		if ((oldrank > rank) && (this.config.getBoolean("Settings.broadcast-rank-change"))) {
			Bukkit.getServer().broadcastMessage(ChatManager.ColorIt(this.config.getString("Messages.rank-change-msg").replace("<player>", Player).replace("<rank>", "" + rank + 1)));
		}
		VoteRanks.task.addTask(new WorkTask(Bukkit.getPlayer(Player), votes));
	}

	private void ShowVotesOnTab(Player p) {
		if (this.config.getBoolean("Settings.show-votes-on-tab")) {
			if (p.getScoreboard().getObjective("Votetablist") != null) {
				p.getScoreboard().getObjective("Votetablist").unregister();
			}
			if (p.getScoreboard().getObjective(DisplaySlot.PLAYER_LIST) != null) {
				p.getScoreboard().clearSlot(DisplaySlot.PLAYER_LIST);
			}
			Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votetablist", "dummy","Votetablist",RenderType.HEARTS);
			localObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

			ArrayList<Player> players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
			for (int i = 0; i < players.size(); i++) {
				PlayerData data = this.dbtask.getPlayerData(players.get(i).getName());
				Score score = localObjective.getScore(players.get(i).getName());
				score.setScore(data.getVotes());
			}
			for (int i = 0; i < players.size(); i++)
				players.get(i).setScoreboard(localObjective.getScoreboard());
		}
	}
}