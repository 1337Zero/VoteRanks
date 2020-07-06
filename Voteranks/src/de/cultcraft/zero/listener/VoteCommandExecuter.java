package de.cultcraft.zero.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.utils.ChatManager;
import de.cultcraft.zero.utils.Goal;
import de.cultcraft.zero.utils.PlayerData;
import de.cultcraft.zero.utils.RandomItemStack;
import de.cultcraft.zero.utils.Thread_CloseSidebar;
import de.cultcraft.zero.utils.VoteWorker;
import de.cultcraft.zero.voteranks.VoteRanks;
import net.milkbowl.vault.chat.Chat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class VoteCommandExecuter implements CommandExecutor {
	private DbTask db = null;
	private FileConfiguration config;
	private Date d;
	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy[hh:mm:ss]");
	private VoteRanks main;
	private ArrayList<Player> cleardbcmd = new ArrayList<Player>();
	private ArrayList<Thread_CloseSidebar> closed = new ArrayList<Thread_CloseSidebar>();

	public VoteCommandExecuter(DbTask db, FileConfiguration config, VoteRanks main) {
		this.db = db;
		this.config = config;
		this.main = main;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String args, String[] para) {
		if (sender instanceof Player) {
			//votelist
			if (args.equalsIgnoreCase("votelist")) {
				if (this.config.getBoolean("Settings.use-sidebar-4-votelist")) {
					Player p = (Player) sender;
					try {
						SideBarVoteList(p);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (this.config.getBoolean("Settings.use-chat-4-votelist")) {
					try {
						CommandVotelist((Player) sender);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}					
				//votes
			}else if (args.equalsIgnoreCase("votetopb")) {
				if(para.length == 1) {
					return CommandVotelistb((Player) sender, para[0]);
				}
				
			} else if (args.equalsIgnoreCase("votes")) {
				if (this.config.getBoolean("Settings.use-sidebar-4-votes")) {
					Player p = (Player) sender;
					try {
						SideBarVotes(p, para);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (this.config.getBoolean("Settings.use-chat-4-votes")) {
					try {
						CommandVotes(sender, para);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (args.equalsIgnoreCase("setvotes")) {
				return CommandSetVotes(sender, para);
			}else if (args.equalsIgnoreCase("clearvotes")) {
				return CommandClearVotes(sender);
			}else if (args.equalsIgnoreCase("clearaccept")) {
				CommandClearVotesAccept(sender);
			} else if (args.equalsIgnoreCase("cleardeny")) {
				return CommandClearDeny(sender);
			}else if (args.equalsIgnoreCase("vote")) {
				return CommandVote((Player) sender);
			}else if (args.equalsIgnoreCase("voteversion")) {
				sender.sendMessage(ChatManager.ColorIt("&6[" + this.main.getDescription().getName() + "]&a Version: " + this.main.getDescription().getVersion()));
			} else if (args.equalsIgnoreCase("addVote")) {
				return addVote((Player) sender, para);
			}else if (args.equalsIgnoreCase("savebook")) {
				try {
					savebookasTxt(sender);
				} catch (IOException e) {
					sender.sendMessage(ChatManager.ColorIt("[VoteRanks] Error saving the book"));
					e.printStackTrace();
				}
			}else if (args.equalsIgnoreCase("storeStack")) {
				storeItemStackCommand((Player)sender);
			}else if (args.equalsIgnoreCase("editGoal")) {
				commandeditGoal((Player)sender,para);
			}else if (args.equalsIgnoreCase("votereload")) {
				reloadVoteRanks((Player)sender);
			}
		}
		return true;
	}

	private void reloadVoteRanks(Player sender) {
		VoteRanks.instance.reloadConfig();
		VoteRanks.task = new VoteWorker(VoteRanks.instance.loadGoals());
	}

	private void commandeditGoal(Player sender, String[] para) {
		//command  para[0]	  para[1]		Nachricht
		//editGoal id		  subcommand	[...]
		if (!sender.hasPermission("VoteRank.editgoal")) {
			sender.sendMessage(ChatManager.ColorIt(config.getString("Messages.no-permissions").replace("<command>", "clearvotes")));
			return;
		}
		if(para.length >= 2) {
			String message = getStringFromParameterAtPosition(2, para);
			if(!para[1].matches("[\\d]*")) {
				sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[1])));
				return;
			}
			int id = Integer.parseInt(para[1]);
			if(para[0].equalsIgnoreCase("addMessage")) {
				///editgoal addMessage 0 Hallo :D a b c d e
				VoteWorker.goals.get(id).getPersonalMessage().add(message);				
				sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-message-added").replace("<message>", message).replace("<id>", id+"")));
			}else if(para[0].equalsIgnoreCase("addBroadcast")) {
				//editGoal id addBroadcast Broadcast
				VoteWorker.goals.get(id).getBroadcast().add(message);
				sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-broadcast-added").replace("<broadcast>", message).replace("<id>", id+"")));
			}else if(para[0].equalsIgnoreCase("addGiveList")) {
				//editGoal id addGiveList ListName
				if(!VoteWorker.goals.get(id).addRandomItemList(message)) {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-randomitemlist-added").replace("<list>", message).replace("<id>", id+"")));
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-list-not-existing").replace("<list>", message)));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-randomitemlist-added").replace("<list>", message).replace("<id>", id+"")));
				}
			}else if(para[0].equalsIgnoreCase("addCommandList")) {
				//editGoal id addCommandList Command
				if(VoteWorker.goals.get(id).addRandomCommandList(message)) {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-randomcommandlist-added").replace("<list>", message).replace("<id>", id+"")));
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-list-not-existing").replace("<list>", message)));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-randomcommandlist-added").replace("<list>", message).replace("<id>", id+"")));
				}
			}else if(para[0].equalsIgnoreCase("addCommand")) {
				//editGoal id addCommand Commando
				VoteWorker.goals.get(id).getCommand().add(message);
				sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-command-added").replace("<command>", message).replace("<id>", id+"")));	
			}else if(para[0].equalsIgnoreCase("addBook")) {
				//editGoal id addBook Path
				ItemStack stack = Goal.getBookFromFile(message);
				
				if(stack != null) {
					VoteWorker.goals.get(id).getBook().put(message,stack);
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-book-added").replace("<path>", message).replace("<id>", id+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-book-not-found").replace("<path>", message).replace("<id>", id+"")));
				}					
			}else if(para[0].equalsIgnoreCase("adddbitem")) {
				//editGoal id adddbitem id
				if(para[2].matches("[\\d]*")) {
					VoteWorker.goals.get(id).getDbItems().add(Integer.parseInt(para[2]));
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-dbitem-added").replace("<dbid>", para[2]).replace("<id>", id+"")));	
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("setMessage")) {
				if(para[2].matches("[\\d]*")) {
					int mid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setMessage mID <text ...>
					VoteWorker.goals.get(id).getPersonalMessage().set(mid,message);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-message-set").replace("<message>", message).replace("<id>", id+"").replace("<mid>", mid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("setBroadcast")) {
				if(para[2].matches("[\\d]*")) {
					int bid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setBroadcast bID <text ...>
					VoteWorker.goals.get(id).getBroadcast().set(bid,message);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-broadcast-set").replace("<message>", message).replace("<id>", id+"").replace("<bid>", bid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}			
			}else if(para[0].equalsIgnoreCase("setGiveList")) {
				if(para[2].matches("[\\d]*")) {
					int lid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setGiveList lID <text ...>
					VoteWorker.goals.get(id).getUsedItemList().set(lid,message);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-givelist-set").replace("<message>", message).replace("<id>", id+"").replace("<lid>", lid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("setCommandList")) {
				if(para[2].matches("[\\d]*")) {
					int lid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setCommandList lID <text ...>
					VoteWorker.goals.get(id).getUsedCommandList().set(lid,message);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-commandlist-set").replace("<message>", message).replace("<id>", id+"").replace("<lid>", lid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("setCommand")) {
				if(para[2].matches("[\\d]*")) {
					int cid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setCommandList lID <text ...>
					VoteWorker.goals.get(id).getCommand().set(cid,message);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-commandlist-set").replace("<message>", message).replace("<id>", id+"").replace("<cid>", cid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}			
			}else if(para[0].equalsIgnoreCase("setBook")) {
				//editGoal id addBook Path							
				if(para[2].matches("[\\d]*")) {
					int bid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					ItemStack stack = Goal.getBookFromFile(message);	
					if(stack != null) {
						VoteWorker.goals.get(id).getBook().put(this.getBookPathByID(bid, id),stack);
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-book-set").replace("<path>", message).replace("<id>", id+"").replace("<bid>", bid+"")));
					}else {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-book-not-found").replace("<path>", message).replace("<id>", id+"")));
					}
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("setdbitem")) {
				//editGoal id adddbitem id
				if(para[2].matches("[\\d]*")) {
					int dbItemid = Integer.parseInt(para[2]);
					VoteWorker.goals.get(id).getDbItems().set(dbItemid,Integer.parseInt(para[3]));
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-dbitem-set").replace("<dbid>", para[2]).replace("<id>", id+"").replace("<dbicnid>", id+"")));	
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("removeMessage")) {
				if(para[2].matches("[\\d]*")) {
					int mid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal setMessage gid mID <text ...>
					VoteWorker.goals.get(id).getPersonalMessage().remove(mid);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-message-removed").replace("<message>", message).replace("<id>", id+"").replace("<mid>", mid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("removeBroadcast")) {
				if(para[2].matches("[\\d]*")) {
					int bid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setBroadcast bID <text ...>
					VoteWorker.goals.get(id).getBroadcast().remove(bid);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-broadcast-removed").replace("<message>", message).replace("<id>", id+"").replace("<bid>", bid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}			
			}else if(para[0].equalsIgnoreCase("removeGiveList")) {
				if(para[2].matches("[\\d]*")) {
					int lid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setGiveList lID <text ...>
					VoteWorker.goals.get(id).getUsedItemList().remove(lid);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-givelist-removed").replace("<message>", message).replace("<id>", id+"").replace("<lid>", lid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("removeCommandList")) {
				if(para[2].matches("[\\d]*")) {
					int lid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setCommandList lID <text ...>
					VoteWorker.goals.get(id).getUsedCommandList().remove(lid);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-commandlist-removed").replace("<message>", message).replace("<id>", id+"").replace("<lid>", lid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("removeCommand")) {
				if(para[2].matches("[\\d]*")) {
					int cid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					//editgoal gid setCommandList lID <text ...>
					VoteWorker.goals.get(id).getCommand().remove(cid);				
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-commandlist-removed").replace("<message>", message).replace("<id>", id+"").replace("<cid>", cid+"")));
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}			
			}else if(para[0].equalsIgnoreCase("removeBook")) {
				//editGoal id addBook Path							
				if(para[2].matches("[\\d]*")) {
					int bid = Integer.parseInt(para[2]);
					message = getStringFromParameterAtPosition(3, para);
					ItemStack stack = Goal.getBookFromFile(message);	
					if(stack != null) {
						VoteWorker.goals.get(id).getBook().remove(this.getBookPathByID(bid, id));
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-book-removed").replace("<path>", message).replace("<id>", id+"").replace("<bid>", bid+"")));
					}else {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-book-not-found").replace("<path>", message).replace("<id>", id+"")));
					}
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("removedbitem")) {
				//editGoal id adddbitem id
				if(para[2].matches("[\\d]*")) {
					int dbItemid = Integer.parseInt(para[2]);
					VoteWorker.goals.get(id).getDbItems().remove(dbItemid);
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-dbitem-removed").replace("<dbid>", para[2]).replace("<id>", id+"").replace("<dbicnid>", id+"")));	
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[2])));
				}
			}else if(para[0].equalsIgnoreCase("show")) {
				if(para[1].matches("[\\d]*")) {
					if(Integer.parseInt(para[1]) > (VoteWorker.goals.size()-1)) {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.out-of-range").replace("<max>", (VoteWorker.goals.size()-1)+"").replace("<number>", para[1])));
						return;
					}
					Goal goal = VoteWorker.goals.get(Integer.parseInt(para[1]));					
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-neededVotes").replace("<votes>", goal.getVotes()+"").replace("<type>", goal.getType().typeToString())));
					if(goal.getPersonalMessage().size() > 0) {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-messages")));					
						int cnt = 0;
						for(String mess : goal.getPersonalMessage()) {
							sender.sendMessage(cnt++ + ": " + mess);
						}
					}
					if(goal.getBroadcast().size() > 0) {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-broadcast")));	
						int cnt = 0;
						for(String mess : goal.getBroadcast()) {
							sender.sendMessage(cnt++ + ": " + mess);
						}
					}
					if(goal.getCommand().size() > 0) {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-commands")));	
						int cnt = 0;
						for(String mess : goal.getCommand()) {
							sender.sendMessage(cnt++ + ": " + mess);
						}
					}
					if(goal.getBook().size() > 0) {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-books")));	
						int cnt = 0;
						for(String mess : goal.getBook().keySet()) {
							sender.sendMessage(cnt++ + ": " + mess);
						}
					}				
					if(goal.getItem().size() > 0) {
						sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.goal-items")));		
						int cnt = 0;
						for(ItemStack stack : goal.getItem()) {
							if(!(stack instanceof RandomItemStack)) {
								ItemMeta meta = stack.getItemMeta();
								if(meta != null) {
									sender.sendMessage(cnt++ + ": " + meta.getDisplayName());
								}else {
									sender.sendMessage(cnt++ + ": " + stack.getType());
								}
							}else {
								RandomItemStack rstack = (RandomItemStack) stack;
								sender.sendMessage(cnt++ + ": random Stack from " + rstack.getList());
							}
						}
					}
				}else {
					sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.not-a-number").replace("<string>", para[1])));
				}
			}
		}else if(para.length == 0) {
			for(String s : VoteRanks.config.getString("Messages.editgoal-types").split("<newline>")) {
				sender.sendMessage(ChatManager.ColorIt(s));	
			}
		}else if(para[0].equalsIgnoreCase("show")) {
			int id = 0;
			for(Goal goal: VoteWorker.goals) {
				sender.sendMessage(id++ + ": " + goal.toConfigString().replace("§", "&"));
			}
		}else if(para[0].equalsIgnoreCase("save")) {
			ArrayList<String> goalAsString = new ArrayList<String>();
			for(Goal goal: VoteWorker.goals) {
				goalAsString.add(goal.toConfigString());
			}
			VoteRanks.config.set("Goals", goalAsString);
			VoteRanks.instance.saveConfig();
			sender.sendMessage(ChatManager.ColorIt(VoteRanks.config.getString("Messages.config-saved")));
		}else {
			sender.sendMessage(ChatManager.ColorIt(""));
		}
	}
	private String getBookPathByID(int id,int goalID) {
		Goal goal = VoteWorker.goals.get(goalID);		
		int cnt = 0;
		for(String mess : goal.getBook().keySet()) {
			if(cnt++ == id) {
				return mess;
			}
		}
		return null;
	}
	private String getStringFromParameterAtPosition(int startPos,String[] para) {
		StringBuilder builder = new StringBuilder();
		
		for(int i = startPos; i < para.length;i++) {
			builder.append(para[i] + " ");
		}
		
		return builder.toString();
	}

	private boolean CommandVotelist(Player p) throws SQLException {
		ArrayList<PlayerData> data = this.db.getTopTen();
		if (data != null) {
			p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.votelist-header").replace("<size>", data.size() + "")));
			for (int i = 0; i < data.size(); i++) {
				p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.votelist-place-format")
					.replace("<player>", ((PlayerData) data.get(i)).getUsername())
					.replace("<rank>", "" + ((PlayerData) data.get(i)).getRank())
					.replace("<votes>", "" + ((PlayerData) data.get(i)).getVotes())));
			}
		}
		return true;
	}

	private boolean CommandVotelistb(Player p,String para) {
		if (p.hasPermission("VoteRank.votelistb")) {
			ArrayList<PlayerData> data = this.db.getTopTenBackup(para);
			if (data != null) {
				p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.votelist-header").replace("<size>", data.size() + "")));
				for (int i = 0; i < data.size(); i++) {
					p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.votelist-place-format")
						.replace("<player>", ((PlayerData) data.get(i)).getUsername())
						.replace("<rank>", "" + ((PlayerData) data.get(i)).getRank())
						.replace("<votes>", "" + ((PlayerData) data.get(i)).getVotes())));
				}
			}
		}else if (config.getBoolean("Settings.show-no-permission-msg")) {
			p.sendMessage(ChatManager.ColorIt(config.getString("Messages.no-permissions").replace("<command>", "clearvotes")));
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
					p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.votes-see-format")
							.replace("<rank>", "" + rank).replace("<player>", search4).replace("<votes>", "" + votes)));
				else {
					p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.votes-see-format-more-than-one")
							.replace("<rank>", "" + rank).replace("<player>", search4).replace("<votes>", "" + votes)
							.replace("<same rank>", "" + samevotes)));
				}
			} else if (samevotes == 1)
				p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.votes-see-format-others")
						.replace("<rank>", "" + rank).replace("<player>", search4).replace("<votes>", "" + votes)));
			else {
				p.sendMessage(
						ChatManager.ColorIt(this.config.getString("Messages.votes-see-format-others-more-than-one")
								.replace("<rank>", "" + rank).replace("<player>", "" + search4)
								.replace("<votes>", "" + votes).replace("<same rank>", "" + samevotes)));
			}
		} else {
			p.sendMessage(
					ChatManager.ColorIt(config.getString("Messages.player-not-found").replace("<player>", search4)));
		}

		return true;
	}

	private boolean CommandClearVotes(CommandSender sender) {
		Player p = (Player) sender;
		if (sender.hasPermission("VoteRank.clearvotes")) {
			cleardbcmd.add(p);
			p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.clearvotes-sure")));
		} else {
			if (config.getBoolean("Settings.show-no-permission-msg")) {
				p.sendMessage(ChatManager
						.ColorIt(config.getString("Messages.no-permissions").replace("<command>", "clearvotes")));
			}
		}
		return true;
	}

	private boolean CommandClearVotesAccept(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			if (this.cleardbcmd.contains(p)) {
				p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.clearvotes-working")));
				this.db.clearAllVotes();
				p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.clearvotes-done")));
				return true;
			}
		}
		return true;
	}

	private boolean CommandVotes(CommandSender sender, String[] para) {
		Player p = (Player) sender;
		String search4 = p.getName();
		if (para.length == 1) {
			if (this.db.UserExistinDatabase(para[0]))
				search4 = para[0];
			else
				p.sendMessage(ChatManager.ColorIt(
						this.config.getString("Messages.votes-player-not-found").replace("<player>", para[0])));
		}
		try {
			CommandSeeVotes(this.db.getPlayerData(search4), p, search4);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean CommandSetVotes(CommandSender sender, String[] para) {
		Player p = (Player) sender;
		if (sender.hasPermission("VoteRank.setvotes")) {
			if (para.length == 2) {
				if (!this.db.UserExistinDatabase(para[0]))
					p.sendMessage(ChatManager.ColorIt(
							this.config.getString("Messages.votes-player-not-found").replace("<player>", para[0])));
				else
					try {
						this.d = new Date();
						int votes = Integer.parseInt(para[1]);
						this.db.setVotes(votes,para[0],this.format.format(this.d));
						p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.setvotes-success")
								.replace("<player>", para[0]).replace("<votes>", "" + votes)));
					} catch (Exception e) {
						p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.setvotes-not-int")));
					}
			} else {
				p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.setvotes-2less-para")));
			}
		} else {
			if (config.getBoolean("Settings.show-no-permission-msg")) {
				p.sendMessage(ChatManager
						.ColorIt(config.getString("Messages.no-permissions").replace("<command>", "setvotes")));
			}
		}
		return true;
	}

	private boolean CommandClearDeny(CommandSender sender) {
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			if (this.cleardbcmd.contains(p)) {
				cleardbcmd.remove(p);
				p.sendMessage(ChatManager.ColorIt(this.config.getString("Messages.clearvotes-deny")));
			}
		}
		return true;
	}

	private void SideBarVoteList(Player p) throws Exception {
		ArrayList<PlayerData> data = this.db.getTopTen();

		@SuppressWarnings("deprecation")
		Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votelist",
				"dummy");
		String header = "&aVote-Bestenliste";

		if (ChatManager
				.ColorIt(this.config.getString("Messages.sidebar-votelist-header").replace("<size>", "" + data.size()))
				.length() < 32) {
			header = ChatManager.ColorIt(
					this.config.getString("Messages.sidebar-votelist-header").replace("<size>", "" + data.size()));
		}

		localObjective.setDisplayName(ChatManager.ColorIt(header));
		localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (int i = 0; i < data.size(); i++) {
			@SuppressWarnings("deprecation")
			Score score = localObjective.getScore(Bukkit.getOfflinePlayer(((PlayerData) data.get(i)).getUsername()));
			score.setScore(((PlayerData) data.get(i)).getVotes());
		}
		p.setScoreboard(localObjective.getScoreboard());
		Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR, ChatManager.ColorIt(header));
		tcs.runTaskLater(this.main, this.config.getInt("Settings.sidebar-show-time-seconds") * 20);
		this.addnewThread(tcs);
	}

	@SuppressWarnings("deprecation")
	private void SideBarVotes(Player p, String[] para) throws Exception {
		String search4 = p.getName();
		Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votes",
				"dummy");
		if (para.length == 1) {
			if (this.db.UserExistinDatabase(para[0])) {
				search4 = para[0];
				PlayerData data = this.db.getPlayerData(search4);
				if (data != null) {
					int votes = data.getVotes();
					int rank = data.getRank();
					int samerank = this.db.getSameRank(votes);
					if (samerank == 1) {
						Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
						Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
						rankscore.setScore(rank);
						votescore.setScore(votes);
					} else {
						Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
						Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
						Score otherscore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-same-rank-name"))));
						rankscore.setScore(rank);
						votescore.setScore(votes);
						otherscore.setScore(samerank);
					}
				}
			} else {
				p.sendMessage(ChatManager
						.ColorIt(config.getString("Messages.player-not-found").replace("<player>", para[0])));
			}
			localObjective.setDisplayName(ChatManager.ColorIt("&6" + search4 + ":"));
			localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
			p.setScoreboard(localObjective.getScoreboard());
			Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR,
					ChatManager.ColorIt(ChatManager.ColorIt("&6" + search4 + ":")));
			tcs.runTaskLater(this.main, this.config.getInt("Settings.sidebar-show-time-seconds") * 20);
			this.addnewThread(tcs);
		} else {
			try {
				PlayerData data = this.db.getPlayerData(p.getName());
				if (data != null) {
					int votes = data.getVotes();
					int rank = data.getRank();
					int samerank = this.db.getSameRank(votes);
					if (samerank == 1) {
						Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
						Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
						rankscore.setScore(rank);
						votescore.setScore(votes);
					} else {
						Score rankscore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-rank-name"))));
						Score votescore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-votes-name"))));
						Score otherscore = localObjective.getScore(Bukkit.getOfflinePlayer(
								ChatManager.ColorIt(this.config.getString("Messages.sidebar-same-rank-name"))));
						rankscore.setScore(rank);
						votescore.setScore(votes);
						otherscore.setScore(samerank);
					}
					localObjective.setDisplayName(ChatManager.ColorIt("&6" + p.getName() + ":"));
					localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
					p.setScoreboard(localObjective.getScoreboard());
					Thread_CloseSidebar tcs = new Thread_CloseSidebar(p, DisplaySlot.SIDEBAR,
							ChatManager.ColorIt(ChatManager.ColorIt("&6" + p.getName() + ":")));
					tcs.runTaskLater(this.main, this.config.getInt("Settings.sidebar-show-time-seconds") * 20);
					this.addnewThread(tcs);
				} else {
					p.sendMessage(ChatManager
							.ColorIt(config.getString("Messages.player-not-found").replace("<player>", p.getName())));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean CommandVote(Player p) {
		if (this.config.getBoolean("Settings.command_vote")) {
			List<?> votelist = this.config.getList("votesits");
			for (int i = 0; i < votelist.size(); i++) {
				p.sendMessage(ChatManager.ColorIt1(votelist.get(i).toString()));
			}
		}
		return true;
	}

	private boolean addVote(Player p, String[] para) {
		if (p.hasPermission("Voterank.addvote")) {
			if (para.length == 1) {
				Vote vote = new Vote("localvote", para[0], "127.0.0.1", "" + System.currentTimeMillis());
				Bukkit.getPluginManager().callEvent(new VotifierEvent(vote));
			} else if (para.length == 2) {
				int amount = 1;
				try {
					amount = Integer.parseInt(para[1]);
				} catch (NumberFormatException e) {
					p.sendMessage(ChatManager.ColorIt("&6[VoteRanks]&4'" + para[1] + "' is not a number!"));
				}
				for (int i = 0; i < amount; i++) {
					Vote vote = new Vote("localvote", para[0], "127.0.0.1", "" + System.currentTimeMillis());
					Bukkit.getPluginManager().callEvent(new VotifierEvent(vote));
				}
			}
		} else {
			if (config.getBoolean("Settings.show-no-permission-msg")) {
				p.sendMessage(ChatManager
						.ColorIt(config.getString("Messages.no-permissions").replace("<command>", "addvote")));
			}
		}
		return true;
	}

	private void savebookasTxt(CommandSender sender) throws IOException {
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			if (p.hasPermission("VoteRank.savebook")) {
				File dir = new File("plugins/VoteRanks/Books");

				if (!dir.exists()) {
					dir.mkdir();
				}
				if (p.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
					int count = dir.list().length;
					File file = new File(dir + "/book" + count + ".txt");
					if (!file.exists()) {
						file.createNewFile();
					}
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
					BookMeta meta = (BookMeta) p.getInventory().getItemInMainHand().getItemMeta();
					writer.write("author:" + meta.getAuthor());
					writer.newLine();
					writer.write("title:" + meta.getTitle());
					writer.newLine();
					writer.write("description:");
					if (meta.getLore() != null) {
						for (int i = 0; i < meta.getLore().size(); i++) {
							writer.write((String) meta.getLore().get(i) + ";");
						}
					}
					writer.newLine();
					for (int i = 1; i <= meta.getPageCount(); i++) {
						writer.write(ChatManager.encode(meta.getPage(i).toString().replace("\n", "[newline]")));
						writer.newLine();
					}
					writer.flush();
					writer.close();
					sender.sendMessage(ChatManager
							.ColorIt("[VoteRanks] &asuccesfully written to: " + dir + "/book" + count + ".txt")
							.replace("/", "\\"));
				} else {
					sender.sendMessage(ChatManager.ColorIt("&4You don't have a book in your hand!"));
				}
			} else {
				if (config.getBoolean("Settings.show-no-permission-msg")) {
					p.sendMessage(ChatManager
							.ColorIt(config.getString("Messages.no-permissions").replace("<command>", "savebook")));
				}
			}
		} else {
			sender.sendMessage("[Voteranks] You have to be a user to do that!");
		}
	}

	public void addnewThread(Thread_CloseSidebar closed) {
		Thread_CloseSidebar thread = getThread(closed.getPlayer().getName());
		if (thread == null) {
			this.closed.add(closed);
		} else {
			thread.setClosed(true);
			this.closed.add(closed);
		}
	}

	private Thread_CloseSidebar getThread(String Player) {
		for (int i = 0; i < this.closed.size(); i++) {
			if (((Thread_CloseSidebar) this.closed.get(i)).getPlayer().getName().equalsIgnoreCase(Player)) {
				return (Thread_CloseSidebar) this.closed.get(i);
			}
		}
		return null;
	}
	private void storeItemStackCommand(Player player) {
		if (!player.hasPermission("VoteRank.storestack")) {
			player.sendMessage(ChatManager.ColorIt(config.getString("Messages.no-permissions").replace("<command>", "clearvotes")));
			return;
		}
		if(player.getInventory().getItemInMainHand() != null) {
			int id = db.storeItem(player.getInventory().getItemInMainHand());
			String message = VoteRanks.config.getString("Messages.item-stored");
			message = message.replace("<id>", id+"");
			player.sendMessage(ChatManager.ColorIt(message));
		}		
	}
	
	
	
	
	
	
}