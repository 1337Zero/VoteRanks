package de.cultcraft.zero.listener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.utils.ChatManager;
import de.cultcraft.zero.utils.PlayerData;
import de.cultcraft.zero.utils.Thread_CloseSidebar;
import de.cultcraft.zero.utils.speicher;
import de.cultcraft.zero.voteranks.VoteRanks;

public class VoteCommandExecuter implements CommandExecutor{

	private DbTask db = null;
	private FileConfiguration config;
	private ChatManager ch = new ChatManager();
	private speicher speicher = null;
	private Date  d;
	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy[hh:mm:ss]");
	private VoteRanks main;
	
	public VoteCommandExecuter(DbTask db,FileConfiguration config,speicher speicher,VoteRanks main){
		this.db = db;
		this.config = config;
		this.speicher = speicher;
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String args,String[] para) {
		if(sender instanceof Player){
			if(args.equalsIgnoreCase("votelist")){
				if(config.getBoolean("Settings.use-sidebar-4-votelist")){
					Player p = (Player)sender;
					try {
						SideBarVoteList(p);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}				
				if(config.getBoolean("Settings.use-chat-4-votelist")){
					try {
						CommandVotelist(((Player)sender));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}else if(args.equalsIgnoreCase("votes")){
				if(config.getBoolean("Settings.use-sidebar-4-votes")){
					Player p = (Player)sender;
					try {
						SideBarVotes(p, para);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(config.getBoolean("Settings.use-chat-4-votes")){
					try {
						CommandVotes(sender, para);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}				
			}else if(args.equalsIgnoreCase("setvotes")){
				return CommandSetVotes(sender,para);
			}else if(args.equalsIgnoreCase("clearvotes")){
				return CommandClearVotes(sender);
			}else if(args.equalsIgnoreCase("clearaccept")){
				CommandClearVotesAccept(sender);
			}else if(args.equalsIgnoreCase("cleardeny")){
				return CommandClearDeny(sender);
			}else if(args.equalsIgnoreCase("vote")){
				return CommandVote(((Player)sender));
			}else if(args.equalsIgnoreCase("voteversion")){		
				sender.sendMessage(ch.ColorIt("&6[" + main.getDescription().getName() + "]&a Version: " + main.getDescription().getVersion()));	
			}else if(args.equalsIgnoreCase("addVote")){
				return addVote(((Player)sender), para);
			}else if(args.equalsIgnoreCase("savebook")){
				try {
					savebookasTxt(sender);
				} catch (IOException e) {
					sender.sendMessage(ch.ColorIt("[VoteRanks] &4OOPS, there is something wrong!"));
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	private boolean CommandVotelist(Player p) throws SQLException{	
		 ArrayList<PlayerData> data = db.getTopTen();
			    if(data != null){
			    	 p.sendMessage(ch.ColorIt(config.getString("Messages.votelist-header").replace("<size>", data.size() + "")));
					    for(int i = 0; i < data.size();i++){
					    	p.sendMessage(ch.ColorIt(config.getString("Messages.votelist-place-format").replace("<player>", data.get(i).getUsername()).replace("<rank>", data.get(i).getRank() + "").replace("<votes>", data.get(i).getVotes() + "")));    	
					    }	
			    }		   	
		return true;
	}	
	private boolean CommandSeeVotes(PlayerData playerData,Player p,String search4) throws SQLException{		
		if(playerData != null){
			int votes = playerData.getVotes();
			int rank = playerData.getRank();
			int samevotes = db.getSameRank(votes);
			
			if(search4.equalsIgnoreCase(p.getName())){
				if(samevotes == 1){					
						p.sendMessage(ch.ColorIt(config.getString("Messages.votes-see-format").replace("<rank>", rank  + "").replace("<player>", search4).replace("<votes>", votes + "")));
				}else{
						p.sendMessage(ch.ColorIt(config.getString("Messages.votes-see-format-more-than-one").replace("<rank>",rank + "").replace("<player>", search4).replace("<votes>", votes + "").replace("<same rank>", samevotes  + "")));
					}
			}else{	
				if(samevotes == 1){	
					p.sendMessage(ch.ColorIt(config.getString("Messages.votes-see-format-others").replace("<rank>", rank  + "").replace("<player>", search4).replace("<votes>", votes + "")));
				}else{
					p.sendMessage(ch.ColorIt(config.getString("Messages.votes-see-format-others-more-than-one").replace("<rank>",rank + "").replace("<player>", search4).replace("<votes>", votes + "").replace("<same rank>", samevotes + "")));
				}
			}
		}		
		return true;
	}
	private boolean CommandClearVotes(CommandSender sender){
		
		Player p = ((Player)sender);
		if(speicher.hasPermission(p,"VoteRank.clearvotes")){
			speicher.addCleardbcmd(p);
			p.sendMessage(ch.ColorIt(config.getString("Messages.clearvotes-sure")));
		}		
		return true;
	}
	private boolean CommandClearVotesAccept(CommandSender sender){
		if(sender instanceof Player){
			Player p = (Player)sender;
			if(speicher.hasPlayerCmdRemoveCmd(p.getName())){
					p.sendMessage(ch.ColorIt(config.getString("Messages.clearvotes-working")));
					db.ExexuteQuery("UPDATE `Votes` set `votes` = 0");
					p.sendMessage(ch.ColorIt(config.getString("Messages.clearvotes-done")));
					return true;
			}
		}		
		return true;
	}
	private boolean CommandVotes(CommandSender sender,String[] para){
		Player p = (Player)sender;
		String search4 = p.getName();
		if(para.length == 1){			
			
				if(db.UserExistinDatabase(para[0])){
					search4 = para[0];
				}else{
					p.sendMessage(ch.ColorIt(config.getString("Messages.votes-player-not-found").replace("<player>", para[0])));
				}			
		}				
		try {
			CommandSeeVotes(db.getPlayerData(search4,p.getUniqueId()),p,search4);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	private boolean CommandSetVotes(CommandSender sender,String[] para){
		Player p = (Player)sender;
		if(speicher.hasPermission(p,"VoteRank.setvotes")){
			if(para.length == 2){
				
					if(!db.UserExistinDatabase(para[0])){
						p.sendMessage(ch.ColorIt(config.getString("Messages.votes-player-not-found").replace("<player>", para[0])));
					}else{
						try{
							d = new Date();
							int votes = Integer.parseInt(para[1]);
							db.ExexuteQuery("UPDATE `Votes` SET `votes` = " + votes + " where `User` = '" + para[0] + "'" );
							db.ExexuteQuery("UPDATE `Votes` SET `lastvote` = '" + format.format(d) + "' where `User` = '" + para[0] + "'" );
							p.sendMessage(ch.ColorIt(config.getString("Messages.setvotes-success").replace("<player>", para[0]).replace("<votes>", votes + "")));
						}catch(Exception e){
							p.sendMessage(ch.ColorIt(config.getString("Messages.setvotes-not-int")));
						}
					}				
				}else{
					p.sendMessage(ch.ColorIt(config.getString("Messages.setvotes-2less-para")));
				}
		}
		return true;
	}
	private boolean CommandClearDeny(CommandSender sender){
		if(sender instanceof Player){
			Player p = (Player)sender;
			if(speicher.hasPlayerCmdRemoveCmd(p.getName())){						
				speicher.removeCleardbcmd(p);
				p.sendMessage(ch.ColorIt(config.getString("Messages.clearvotes-deny")));
			}
		}
		return true;
	}
	private void SideBarVoteList(Player p) throws Exception{
	//   User/votes/lastvote		
		ArrayList<PlayerData> data = db.getTopTen();
		
	    Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votelist", "dummy");
	    String header = "&aVote-Bestenliste";
	    
	    if((ch.ColorIt(config.getString("Messages.sidebar-votelist-header").replace("<size>", data.size() + "")).length() < 32)){
	    	header = ch.ColorIt(config.getString("Messages.sidebar-votelist-header").replace("<size>", data.size() + ""));
	    }
	    
	    localObjective.setDisplayName(ch.ColorIt(header));
	    localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
	    
	    for(int i = 0; i < data.size();i++){
	    	Score score = localObjective.getScore(Bukkit.getOfflinePlayer(data.get(i).getUsername()));
	    	score.setScore(data.get(i).getVotes());	    	
	    }	    
	    p.setScoreboard(localObjective.getScoreboard());
	    Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR, ch.ColorIt(header));
	    tcs.runTaskLater(main, (config.getInt("Settings.sidebar-show-time-seconds")*20));
	    speicher.addnewThread(tcs);	    
	}
	private void SideBarVotes(Player p,String[] para) throws Exception{
		//   User/votes/lastvote
		String search4 = p.getName();
		Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votes", "dummy");
		if(para.length == 1){
				if(db.UserExistinDatabase(para[0])){
					search4 = para[0];					
						PlayerData data = db.getPlayerData(search4,Bukkit.getOfflinePlayer(search4).getUniqueId());
						if(data != null){
							int votes = data.getVotes();
							int rank = data.getRank();
							int samerank = db.getSameRank(votes);							
								if(samerank == 1){									
									Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-rank-name"))));
									Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-votes-name"))));
									rankscore.setScore(rank);
									votescore.setScore(votes);								
								}else{									
									Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-rank-name"))));
									Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-votes-name"))));
									Score otherscore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-same-rank-name"))));	
									rankscore.setScore(rank);					
									votescore.setScore(votes);
									otherscore.setScore(samerank);									
								}	
						}						
				}
				localObjective.setDisplayName(ch.ColorIt("&6" + search4 + ":"));
			    localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				p.setScoreboard(localObjective.getScoreboard());
				Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR, ch.ColorIt(ch.ColorIt("&6" + search4 + ":")));
				tcs.runTaskLater(main, (config.getInt("Settings.sidebar-show-time-seconds")*20));
				speicher.addnewThread(tcs);				
		}else{
			try {
				PlayerData data = db.getPlayerData(p.getName(),p.getUniqueId());
				if(data != null){
					int votes = data.getVotes();
					int rank = data.getRank();
					int samerank = db.getSameRank(votes);					
						if(samerank == 1){								
							Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-rank-name"))));
							Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-votes-name"))));	
							rankscore.setScore(rank);
							votescore.setScore(votes);
						}else{
							Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-rank-name"))));
							Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-votes-name"))));
							Score otherscore = localObjective.getScore(Bukkit.getOfflinePlayer(ch.ColorIt(config.getString("Messages.sidebar-same-rank-name"))));
							rankscore.setScore(rank);
							votescore.setScore(votes);
							otherscore.setScore(samerank);
						}
						localObjective.setDisplayName(ch.ColorIt("&6" + p.getName() + ":"));
					    localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
						p.setScoreboard(localObjective.getScoreboard());
						Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR, ch.ColorIt(ch.ColorIt("&6" + p.getName() + ":")));
						tcs.runTaskLater(main, (config.getInt("Settings.sidebar-show-time-seconds")*20));
						speicher.addnewThread(tcs);						
				}				
		}catch(Exception e){			
			e.printStackTrace();
		}			
		}
		}
	private boolean CommandVote(Player p){	
		if(config.getBoolean("Settings.command_vote")){		
			List<?> votelist = config.getList("votesits");	
			for(int i = 0; i < votelist.size();i++){
				p.sendMessage(ch.ColorIt1(votelist.get(i).toString()));
			}		
		}	
		return true;
	}
	private boolean addVote(Player p,String[] para){		
		if(speicher.hasPermission(p, "Voterank.addvote")){
			if(para.length == 1){
				Vote vote = new Vote();
				vote.setAddress("127.0.0.1");
				vote.setServiceName("localvote");
				vote.setTimeStamp(System.currentTimeMillis() + "");
				vote.setUsername(para[0]);
				Bukkit.getPluginManager().callEvent(new VotifierEvent(vote));
			}else if(para.length == 2){
				int amount = 1;
				try{
					amount = Integer.parseInt(para[1]);
				}catch(NumberFormatException e){	
					p.sendMessage(ch.ColorIt("&6[VoteRanks]&4'" + para[1] + "' is not a number!"));
				}
				for(int i = 0; i < amount;i++){
					Vote vote = new Vote();
					vote.setAddress("127.0.0.1");
					vote.setServiceName("localvote");
					vote.setTimeStamp(System.currentTimeMillis() + "");
					vote.setUsername(para[0]);
					Bukkit.getPluginManager().callEvent(new VotifierEvent(vote));
				}
			}
		}
		return true;
	}
	private void savebookasTxt(CommandSender sender) throws IOException{
		if(sender instanceof Player){
			Player p = (Player)sender;
		if(speicher.hasPermission(p,"VoteRank.savebook")){			
				
			File dir = new File("plugins/VoteRanks/Books");		
			
			if(!dir.exists()){
				dir.mkdir();
			}
				if(p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)){				
					int count = dir.list().length;
					File file = new File(dir + "/book" + count + ".txt");
					if(!file.exists()){
						file.createNewFile();
					}
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF8"));				
					BookMeta meta = (BookMeta)p.getItemInHand().getItemMeta();
					writer.write("author:" + meta.getAuthor());
					writer.newLine();
					writer.write("title:" + meta.getTitle());
					writer.newLine();
					writer.write("description:");
					if(meta.getLore() != null){
						for(int i = 0; i < meta.getLore().size();i++){
							writer.write(meta.getLore().get(i) + ";");
						}
					}					
					writer.newLine();
					for(int i = 1; i <= meta.getPageCount();i++){
						System.out.println(meta.getPage(i).toString() + " " + i );
						writer.write(ch.encode(meta.getPage(i).toString().replace("\n", "[newline]")));
						writer.newLine();
					}
					writer.flush();
					writer.close();
					sender.sendMessage(ch.ColorIt("[VoteRanks] &asuccesfully written to: " + dir + "/book" + count + ".txt").replace("/", "\\"));
				}else{
					sender.sendMessage(ch.ColorIt("&4You don't have a book in your hand!"));
				}				
			}
		}else{
			sender.sendMessage("[Voteranks] You have to be a user to do that!");
		}
	}
}
