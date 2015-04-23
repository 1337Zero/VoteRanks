package de.cultcraft.zero.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.utils.ChatManager;
import de.cultcraft.zero.utils.PlayerData;
import de.cultcraft.zero.utils.Thread_CloseSidebar;
import de.cultcraft.zero.utils.speicher;
import de.cultcraft.zero.voteranks.VoteRanks;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class VoteCommandExecuter
  implements CommandExecutor
{
  private DbTask db = null;
  private FileConfiguration config;
  private ChatManager ch = new ChatManager();
  private speicher speicher = null;
  private Date d;
  private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy[hh:mm:ss]");
  private VoteRanks main;

  public VoteCommandExecuter(DbTask db, FileConfiguration config, speicher speicher, VoteRanks main)
  {
    this.db = db;
    this.config = config;
    this.speicher = speicher;
    this.main = main;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String args, String[] para)
  {
    if ((sender instanceof Player))
      if (args.equalsIgnoreCase("votelist")) {
        if (this.config.getBoolean("Settings.use-sidebar-4-votelist")) {
          Player p = (Player)sender;
          try {
            SideBarVoteList(p);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        if (this.config.getBoolean("Settings.use-chat-4-votelist"))
          try {
            CommandVotelist((Player)sender);
          } catch (SQLException e) {
            e.printStackTrace();
          }
      }
      else if (args.equalsIgnoreCase("votes")) {
        if (this.config.getBoolean("Settings.use-sidebar-4-votes")) {
          Player p = (Player)sender;
          try {
            SideBarVotes(p, para);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        if (this.config.getBoolean("Settings.use-chat-4-votes"))
          try {
            CommandVotes(sender, para);
          } catch (Exception e) {
            e.printStackTrace();
          }
      } else {
        if (args.equalsIgnoreCase("setvotes"))
          return CommandSetVotes(sender, para);
        if (args.equalsIgnoreCase("clearvotes"))
          return CommandClearVotes(sender);
        if (args.equalsIgnoreCase("clearaccept")) {
          CommandClearVotesAccept(sender); } else {
          if (args.equalsIgnoreCase("cleardeny"))
            return CommandClearDeny(sender);
          if (args.equalsIgnoreCase("vote"))
            return CommandVote((Player)sender);
          if (args.equalsIgnoreCase("voteversion")) {
            sender.sendMessage(this.ch.ColorIt("&6[" + this.main.getDescription().getName() + "]&a Version: " + this.main.getDescription().getVersion())); } else {
            if (args.equalsIgnoreCase("addVote"))
              return addVote((Player)sender, para);
            if (args.equalsIgnoreCase("savebook"))
              try {
                savebookasTxt(sender);
              } catch (IOException e) {
                sender.sendMessage(this.ch.ColorIt("[VoteRanks] &4OOPS, there is something wrong!"));
                e.printStackTrace();
              }
          }
        }
      }
    return true;
  }
  private boolean CommandVotelist(Player p) throws SQLException {
    ArrayList data = this.db.getTopTen();
    if (data != null) {
      p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votelist-header").replace("<size>", data.size() + "")));
      for (int i = 0; i < data.size(); i++) {
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votelist-place-format").replace("<player>", ((PlayerData)data.get(i)).getUsername()).replace("<rank>", "" + ((PlayerData)data.get(i)).getRank()).replace("<votes>", "" + ((PlayerData)data.get(i)).getVotes())));
      }
    }
    return true;
  }
  private boolean CommandSeeVotes(PlayerData playerData, Player p, String search4) throws SQLException {
    if (playerData != null) {
      int votes = playerData.getVotes();
      int rank = playerData.getRank();
      int samevotes = this.db.getSameRank(votes);

      if (search4.equalsIgnoreCase(p.getName())) {
        if (samevotes == 1)
          p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votes-see-format").replace("<rank>","" + rank).replace("<player>", search4).replace("<votes>","" + votes)));
        else {
          p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votes-see-format-more-than-one").replace("<rank>", "" +rank).replace("<player>", search4).replace("<votes>", "" +votes).replace("<same rank>", "" +samevotes)));
        }
      }
      else if (samevotes == 1)
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votes-see-format-others").replace("<rank>", "" +rank).replace("<player>", search4).replace("<votes>","" + votes)));
      else {
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votes-see-format-others-more-than-one").replace("<rank>", "" +rank).replace("<player>", "" +search4).replace("<votes>", "" + votes).replace("<same rank>", "" +samevotes)));
      }
    }

    return true;
  }

  private boolean CommandClearVotes(CommandSender sender) {
    Player p = (Player)sender;
    if (this.speicher.hasPermission(p, "VoteRank.clearvotes")) {
      this.speicher.addCleardbcmd(p);
      p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.clearvotes-sure")));
    }
    return true;
  }
  private boolean CommandClearVotesAccept(CommandSender sender) {
    if ((sender instanceof Player)) {
      Player p = (Player)sender;
      if (this.speicher.hasPlayerCmdRemoveCmd(p.getName())) {
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.clearvotes-working")));
        this.db.ExexuteQuery("UPDATE `Votes` set `votes` = 0");
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.clearvotes-done")));
        return true;
      }
    }
    return true;
  }
  private boolean CommandVotes(CommandSender sender, String[] para) {
    Player p = (Player)sender;
    String search4 = p.getName();
    if (para.length == 1)
    {
      if (this.db.UserExistinDatabase(para[0]))
        search4 = para[0];
      else
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votes-player-not-found").replace("<player>", para[0])));
    }
    try
    {
      CommandSeeVotes(this.db.getPlayerData(search4), p, search4);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return true;
  }
  private boolean CommandSetVotes(CommandSender sender, String[] para) {
    Player p = (Player)sender;
    if (this.speicher.hasPermission(p, "VoteRank.setvotes")) {
      if (para.length == 2)
      {
        if (!this.db.UserExistinDatabase(para[0]))
          p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.votes-player-not-found").replace("<player>", para[0])));
        else
          try {
            this.d = new Date();
            int votes = Integer.parseInt(para[1]);
            this.db.ExexuteQuery("UPDATE `Votes` SET `votes` = " + votes + " where `User` = '" + para[0] + "'");
            this.db.ExexuteQuery("UPDATE `Votes` SET `lastvote` = '" + this.format.format(this.d) + "' where `User` = '" + para[0] + "'");
            p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.setvotes-success").replace("<player>", para[0]).replace("<votes>","" + votes)));
          } catch (Exception e) {
            p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.setvotes-not-int")));
          }
      }
      else {
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.setvotes-2less-para")));
      }
    }
    return true;
  }
  private boolean CommandClearDeny(CommandSender sender) {
    if ((sender instanceof Player)) {
      Player p = (Player)sender;
      if (this.speicher.hasPlayerCmdRemoveCmd(p.getName())) {
        this.speicher.removeCleardbcmd(p);
        p.sendMessage(this.ch.ColorIt(this.config.getString("Messages.clearvotes-deny")));
      }
    }
    return true;
  }

  private void SideBarVoteList(Player p) throws Exception {
    ArrayList data = this.db.getTopTen();

    Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votelist", "dummy");
    String header = "&aVote-Bestenliste";

    if (this.ch.ColorIt(this.config.getString("Messages.sidebar-votelist-header").replace("<size>","" + data.size())).length() < 32) {
      header = this.ch.ColorIt(this.config.getString("Messages.sidebar-votelist-header").replace("<size>","" + data.size()));
    }

    localObjective.setDisplayName(this.ch.ColorIt(header));
    localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

    for (int i = 0; i < data.size(); i++) {
      Score score = localObjective.getScore(Bukkit.getOfflinePlayer(((PlayerData)data.get(i)).getUsername()));
      score.setScore(((PlayerData)data.get(i)).getVotes());
    }
    p.setScoreboard(localObjective.getScoreboard());
    Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR, this.ch.ColorIt(header));
    tcs.runTaskLater(this.main, this.config.getInt("Settings.sidebar-show-time-seconds") * 20);
    this.speicher.addnewThread(tcs);
  }

  private void SideBarVotes(Player p, String[] para) throws Exception {
    String search4 = p.getName();
    Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votes", "dummy");
    if (para.length == 1) {
      if (this.db.UserExistinDatabase(para[0])) {
        search4 = para[0];
        PlayerData data = this.db.getPlayerData(search4);
        if (data != null) {
          int votes = data.getVotes();
          int rank = data.getRank();
          int samerank = this.db.getSameRank(votes);
          if (samerank == 1) {
            Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
            Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
            rankscore.setScore(rank);
            votescore.setScore(votes);
          } else {
            Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
            Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
            Score otherscore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-same-rank-name"))));
            rankscore.setScore(rank);
            votescore.setScore(votes);
            otherscore.setScore(samerank);
          }
        }
      }
      localObjective.setDisplayName(this.ch.ColorIt("&6" + search4 + ":"));
      localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
      p.setScoreboard(localObjective.getScoreboard());
      Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR, this.ch.ColorIt(this.ch.ColorIt("&6" + search4 + ":")));
      tcs.runTaskLater(this.main, this.config.getInt("Settings.sidebar-show-time-seconds") * 20);
      this.speicher.addnewThread(tcs);
    } else {
      try {
        PlayerData data = this.db.getPlayerData(p.getName());
        if (data != null) {
          int votes = data.getVotes();
          int rank = data.getRank();
          int samerank = this.db.getSameRank(votes);
          if (samerank == 1) {
            Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
            Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
            rankscore.setScore(rank);
            votescore.setScore(votes);
          } else {
            Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
            Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
            Score otherscore = localObjective.getScore(Bukkit.getOfflinePlayer(this.ch.ColorIt(this.config.getString("Messages.sidebar-same-rank-name"))));
            rankscore.setScore(rank);
            votescore.setScore(votes);
            otherscore.setScore(samerank);
          }
          localObjective.setDisplayName(this.ch.ColorIt("&6" + p.getName() + ":"));
          localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
          p.setScoreboard(localObjective.getScoreboard());
          Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR, this.ch.ColorIt(this.ch.ColorIt("&6" + p.getName() + ":")));
          tcs.runTaskLater(this.main, this.config.getInt("Settings.sidebar-show-time-seconds") * 20);
          this.speicher.addnewThread(tcs);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private boolean CommandVote(Player p) { if (this.config.getBoolean("Settings.command_vote")) {
      List votelist = this.config.getList("votesits");
      for (int i = 0; i < votelist.size(); i++) {
        p.sendMessage(this.ch.ColorIt1(votelist.get(i).toString()));
      }
    }
    return true; }

  private boolean addVote(Player p, String[] para) {
    if (this.speicher.hasPermission(p, "Voterank.addvote")) {
      if (para.length == 1) {
        Vote vote = new Vote();
        vote.setAddress("127.0.0.1");
        vote.setServiceName("localvote");
        vote.setTimeStamp("" + System.currentTimeMillis());
        vote.setUsername(para[0]);
        Bukkit.getPluginManager().callEvent(new VotifierEvent(vote));
      } else if (para.length == 2) {
        int amount = 1;
        try {
          amount = Integer.parseInt(para[1]);
        } catch (NumberFormatException e) {
          p.sendMessage(this.ch.ColorIt("&6[VoteRanks]&4'" + para[1] + "' is not a number!"));
        }
        for (int i = 0; i < amount; i++) {
          Vote vote = new Vote();
          vote.setAddress("127.0.0.1");
          vote.setServiceName("localvote");
          vote.setTimeStamp("" +System.currentTimeMillis());
          vote.setUsername(para[0]);
          Bukkit.getPluginManager().callEvent(new VotifierEvent(vote));
        }
      }
    }
    return true;
  }
  private void savebookasTxt(CommandSender sender) throws IOException {
    if ((sender instanceof Player)) {
      Player p = (Player)sender;
      if (this.speicher.hasPermission(p, "VoteRank.savebook"))
      {
        File dir = new File("plugins/VoteRanks/Books");

        if (!dir.exists()) {
          dir.mkdir();
        }
        if (p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
          int count = dir.list().length;
          File file = new File(dir + "/book" + count + ".txt");
          if (!file.exists()) {
            file.createNewFile();
          }
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
          BookMeta meta = (BookMeta)p.getItemInHand().getItemMeta();
          writer.write("author:" + meta.getAuthor());
          writer.newLine();
          writer.write("title:" + meta.getTitle());
          writer.newLine();
          writer.write("description:");
          if (meta.getLore() != null) {
            for (int i = 0; i < meta.getLore().size(); i++) {
              writer.write((String)meta.getLore().get(i) + ";");
            }
          }
          writer.newLine();
          for (int i = 1; i <= meta.getPageCount(); i++) {
            System.out.println(meta.getPage(i).toString() + " " + i);
            writer.write(this.ch.encode(meta.getPage(i).toString().replace("\n", "[newline]")));
            writer.newLine();
          }
          writer.flush();
          writer.close();
          sender.sendMessage(this.ch.ColorIt("[VoteRanks] &asuccesfully written to: " + dir + "/book" + count + ".txt").replace("/", "\\"));
        } else {
          sender.sendMessage(this.ch.ColorIt("&4You don't have a book in your hand!"));
        }
      }
    } else {
      sender.sendMessage("[Voteranks] You have to be a user to do that!");
    }
  }
}