package de.cultcraft.zero.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import de.cultcraft.zero.Database.DbTask;
import de.cultcraft.zero.utils.ChatManager;
import de.cultcraft.zero.utils.PlayerData;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class VoteListener implements Listener{

	private DbTask dbtask;
	private FileConfiguration config;
	private ChatManager ch = new ChatManager();
	private Date d;
	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy[hh:mm:ss]");
	
	
	public VoteListener(DbTask dbtask,FileConfiguration config) {
		this.dbtask = dbtask;
		this.config = config;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnVoteEvent(VotifierEvent event){	
		CreateUserInDataBase(event.getVote().getUsername());
		PerformVote(event.getVote());

	}	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoinEvent(PlayerJoinEvent event){
		ShowVotesOnTab(event.getPlayer());	
		if(!dbtask.UserExistinDatabase(event.getPlayer().getName())){
			if(config.getBoolean("Settings.firstjoined-book")){
				ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
				BookMeta bmeta = (BookMeta) book.getItemMeta();
				book.setItemMeta(getBookMetafromPath(bmeta, config.getString("Settings.firstjoined-path")));	
				event.getPlayer().getInventory().addItem(book);
			}			
		}
		CreateUserInDataBase(event.getPlayer().getName());	
	}
	
	private void CreateUserInDataBase(String player){			
			if(!dbtask.UserExistinDatabase(player)){
			//   User/votes/lastvote
				d = new Date();
				if(Bukkit.getServer().getPlayer(player) != null){
					System.out.println("INSERT INTO Votes VALUES ('" + Bukkit.getServer().getPlayer(player).getUniqueId() + "','" + player + "',0,'" + format.format(d) +"')");
					dbtask.ExexuteQuery("INSERT INTO Votes VALUES ('" + Bukkit.getServer().getPlayer(player).getUniqueId() + "','" + player + "',0,'" + format.format(d) +"')");					
				}else if(Bukkit.getOfflinePlayer(player) != null){
					dbtask.ExexuteQuery("INSERT INTO Votes VALUES ('" + Bukkit.getOfflinePlayer(player).getPlayer().getUniqueId() + "','" + player + "',0,'" + format.format(d) +"')");
				}else{
					System.out.println("ERROR can#t add Player to db");
				}				
				if(config.getBoolean("Settings.debug-mode")){
					System.out.println("Fuer " + player + " wurde erfolgreich ein Eintrag in der Vote-Datenbank erstellt");
				}
				}else{
					if(config.getBoolean("Settings.debug-mode")){
						System.out.println("User " + player + " existiert in der Datenbank");
					}
				}		
		}
	private void PerformVote(Vote vote){
	//   User/votes/lastvote
		CreateUserInDataBase(vote.getUsername());
		if(vote.getUsername().length() > 0){		
			PlayerData data = dbtask.getPlayerData(vote.getUsername(),Bukkit.getOfflinePlayer(vote.getUsername()).getPlayer().getUniqueId());
			if(data != null){
				d = new Date();
				dbtask.ExexuteQuery("UPDATE `Votes` SET `votes` = " + (data.getVotes() + 1) + " where `User` = '" + vote.getUsername() + "'" );
				dbtask.ExexuteQuery("UPDATE `Votes` SET `lastvote` = '" + format.format(d) + "' where `User` = '" + vote.getUsername() + "'" );
				AktuUserRank(vote.getUsername(), (data.getVotes() + 1),data.getRank());
				if(Bukkit.getPlayer(vote.getUsername()) != null){
					ShowVotesOnTab(Bukkit.getPlayer(vote.getUsername()));
				}
			}
			
			if(config.getBoolean("Settings.debug-mode")){
				System.out.println(vote.getUsername() + " hat auf " + vote.getServiceName() + " um " + vote.getTimeStamp() + " gevotet");
			}
			if(config.getBoolean("Settings.debug-mode")){
				System.out.println("User: " + vote.getUsername());
				System.out.println("IP: " + vote.getAddress());
				System.out.println("Service: " + vote.getServiceName());
				System.out.println("Zeit: " + vote.getTimeStamp());
				System.out.println("Votes: " + (data.getVotes() + 1));
				System.out.println("Rang: " + data.getRank());
			}
			
		}else{
			if(config.getBoolean("Settings.debug-mode")){
				System.out.println("[WARNUNG] Jemand hat versucht mit unvollständigen Informationen zu voten!");
				System.out.println("-----");
				System.out.println("User: '" + vote.getUsername() + "'");
				System.out.println("IP: '" + vote.getAddress() + "'");
				System.out.println("SERVICE: '" + vote.getServiceName() + "'");
				System.out.println("TIMESTAMP: '" + vote.getTimeStamp() + "'");
				System.out.println("-----");
			}
		}
	}
	private void AktuUserRank(String Player,int votes,int oldrank){
		int rank = dbtask.getRank(votes);
		if(rank == 0){
			rank = 1;
		}
		if(oldrank > rank){
			if(config.getBoolean("Settings.broadcast-rank-change")){
				Bukkit.getServer().broadcastMessage(ch.ColorIt(config.getString("Messages.rank-change-msg").replace("<player>", Player).replace("<rank>", (rank + 1) + "")));
			}			
		}
		LookupGoals(votes,Bukkit.getPlayer(Player),Player);	
	}
	private void LookupGoals(int votes,Player p,String playername){
		if(p != null){
			try{
				List<?> goals = config.getList("Goals");
				for(int i = 0; i < goals.size();i++){
					String goal = goals.get(i).toString();
					String[] goalpart = goal.split(";");
					if(goalpart[0].contains("votes=")){
						
						if(Integer.parseInt(goalpart[0].split("=")[1]) == votes){	
							for(int x = 0; x < goalpart.length;x++){	
								if(goalpart[x].contains("Message=") || goalpart[x].contains("message=")){
									if(p.isOnline()){
										p.sendMessage(ch.ColorIt(goalpart[x].split("=")[1].replace("<player>", p.getName()).replace("<votes>", votes + "")));	
									}									
								}else if(goalpart[x].contains("broadcast=") || goalpart[x].contains("Broadcast=")){							
									Bukkit.getServer().broadcastMessage(ch.ColorIt(goalpart[x].split("=")[1].replace("<player>", p.getName()).replace("<votes>", votes + "")));							
								}else if(goalpart[x].contains("Give=") || goalpart[x].contains("give=")){									
									if(p.isOnline()){
										String goalpartid = goalpart[x].split("=")[1];		
										String[] subdata = goalpartid.split(",");
										p.getInventory().addItem(getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7])));
									}else{
										String goalpartid = goalpart[x].split("=")[1];	
										String[] subdata = goalpartid.split(",");
										Bukkit.getServer().getOfflinePlayer(playername).getPlayer().getInventory().addItem(getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7])));
									}
								}else if(goalpart[x].contains("command=") || goalpart[x].contains("Command=")){							
									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), goalpart[x].split("=")[1].replace("<player>", p.getName()));							
								}else if(goalpart[x].contains("Book=") || goalpart[x].contains("book=")){
									ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
									BookMeta bmeta = (BookMeta) book.getItemMeta();
									if(goalpart[x].split("=")[1].contains("[file]") || goalpart[x].split("=")[1].contains("[File]")){
										book.setItemMeta(getBookMetafromPath(bmeta, goalpart[x].split("=")[1].split("]")[1]));									
									}else{
										book.setItemMeta(getBookMetafromList(bmeta, goalpart[x].split("=")[1]));
										System.out.println("Unfomiert " + goalpart[x]);
									}								
									p.getInventory().addItem(book);
								}
							}
						}
					}else if(goalpart[0].contains("votes<")){
						if(Integer.parseInt(goalpart[0].split("<")[1]) > votes){	
							for(int x = 0; x < goalpart.length;x++){	
								if(goalpart[x].contains("Message=") || goalpart[x].contains("message=")){
									if(p.isOnline()){
										p.sendMessage(ch.ColorIt(goalpart[x].split("=")[1].replace("<player>", p.getName()).replace("<votes>", votes + "")));	
									}									
								}else if(goalpart[x].contains("broadcast=") || goalpart[x].contains("Broadcast=")){							
									Bukkit.getServer().broadcastMessage(ch.ColorIt(goalpart[x].split("=")[1].replace("<player>", p.getName()).replace("<votes>", votes + "")));							
								}else if(goalpart[x].contains("Give=") || goalpart[x].contains("give=")){								
									if(p.isOnline()){
										String goalpartid = goalpart[x].split("=")[1];	
										String[] subdata = goalpartid.split(",");
										p.getInventory().addItem(getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7])));
									}else{
										String goalpartid = goalpart[x].split("=")[1];	
										String[] subdata = goalpartid.split(",");
										Bukkit.getServer().getOfflinePlayer(playername).getPlayer().getInventory().addItem(getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7])));
									}
								}else if(goalpart[x].contains("command=") || goalpart[x].contains("Command=")){							
									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), goalpart[x].split("=")[1].replace("<player>", p.getName()));							
								}else if(goalpart[x].contains("Book=") || goalpart[x].contains("book=")){
									ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
									BookMeta bmeta = (BookMeta) book.getItemMeta();
									if(goalpart[x].split("=")[1].contains("[file]") || goalpart[x].split("=")[1].contains("[File]")){
										book.setItemMeta(getBookMetafromPath(bmeta, goalpart[x].split("=")[1].split("]")[1]));									
									}else{
										bmeta.addPage(ch.ColorIt(goalpart[x].split("=")[1]));
										book.setItemMeta(bmeta);
									}								
									p.getInventory().addItem(book);
								}
							}
						}
					}else if(goalpart[0].contains("votes>")){
						if(Integer.parseInt(goalpart[0].split(">")[1]) < votes){	
							for(int x = 0; x < goalpart.length;x++){	
								if(goalpart[x].contains("Message=") || goalpart[x].contains("message=")){
									if(p.isOnline()){
										p.sendMessage(ch.ColorIt(goalpart[x].split("=")[1].replace("<player>", p.getName()).replace("<votes>", votes + "")));	
									}									
								}else if(goalpart[x].contains("broadcast=") || goalpart[x].contains("Broadcast=")){							
									Bukkit.getServer().broadcastMessage(ch.ColorIt(goalpart[x].split("=")[1].replace("<player>", p.getName()).replace("<votes>", votes + "")));							
								}else if(goalpart[x].contains("Give=") || goalpart[x].contains("give=")){
									
									if(p.isOnline()){
										String goalpartid = goalpart[x].split("=")[1];	
										String[] subdata = goalpartid.split(",");
										p.getInventory().addItem(getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7])));
									}else{				
										String goalpartid = goalpart[x].split("=")[1];		
										String[] subdata = goalpartid.split(",");
										Bukkit.getServer().getOfflinePlayer(playername).getPlayer().getInventory().addItem(getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7])));
									}
								}else if(goalpart[x].contains("command=") || goalpart[x].contains("Command=")){							
									Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), goalpart[x].split("=")[1].replace("<player>", p.getName()));							
								}else if(goalpart[x].contains("Book=") || goalpart[x].contains("book=")){
									ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
									BookMeta bmeta = (BookMeta) book.getItemMeta();
									if(goalpart[x].split("=")[1].contains("[file]") || goalpart[x].split("=")[1].contains("[File]")){
										book.setItemMeta(getBookMetafromPath(bmeta, goalpart[x].split("=")[1].split("]")[1]));									
									}else{
										bmeta.addPage(ch.ColorIt(goalpart[x].split("=")[1]));
										book.setItemMeta(bmeta);
									}								
									p.getInventory().addItem(book);
								}
							}
						}
					}else{
						if(config.getBoolean("Settings.debug-mode")){
							System.out.println("Ein Fehler ist aufgetreten beim Laden des Ziels:'" + goal + "'");
							System.out.println("Keine Vote-Anzahl gefunden!");
						}
					}
				}
			}catch(Exception e){
				Bukkit.getLogger().log(Level.WARNING, "[Voteranks] there is something wrong with your config");
				e.printStackTrace();
			}
		}		
	}
	private void ShowVotesOnTab(Player p){
		if(config.getBoolean("Settings.show-votes-on-tab")){
			if( p.getScoreboard().getObjective("Votetablist") != null){
				p.getScoreboard().getObjective("Votetablist").unregister();
			}
			if(p.getScoreboard().getObjective(DisplaySlot.PLAYER_LIST) != null){
				p.getScoreboard().clearSlot(DisplaySlot.PLAYER_LIST);
			}			
			Objective localObjective = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("Votetablist", "dummy");
			localObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		    
			Player[] players = Bukkit.getOnlinePlayers();			  
			for(int i = 0; i < players.length;i++){
				PlayerData data = dbtask.getPlayerData(players[i].getName(),p.getUniqueId());	
			    Score score = localObjective.getScore(Bukkit.getOfflinePlayer(players[i].getName()));
				score.setScore(data.getVotes());	
			}			
			for(int i = 0; i < players.length;i++){
				players[i].setScoreboard(localObjective.getScoreboard());
			}
			
		} 
	}
	private ItemStack getItemStack(int id,int subid,int amount,Enchantment ent,int enchantmentlvl,String name,int red,int green,int blue) throws Exception{
		@SuppressWarnings("deprecation")
		ItemStack stack = new ItemStack(id,amount);	
			if(ent != null){
				if(id != -1){
					try{
						stack.addUnsafeEnchantment(ent, enchantmentlvl);
					}catch(IllegalArgumentException e){
						System.out.println(ent + " can't be on " +  stack.getItemMeta().getDisplayName());
					}
				}			
			}
			if(subid != -1){
				stack.setDurability((short) subid);
			}
			if(name != null & !name.equalsIgnoreCase("null")){	
				ItemMeta meta = stack.getItemMeta();
		        meta.setDisplayName(ch.ColorIt(name));
		        stack.setItemMeta(meta);
			}			
			if(red != -1){
				if(green != -1){
					if(blue != -1){
						LeatherArmorMeta lam = (LeatherArmorMeta)stack.getItemMeta();
						lam.setColor(Color.fromBGR(blue, green, red));
						if(name != null & !name.equalsIgnoreCase("null")){						
					        lam.setDisplayName(ch.ColorIt(name));
						}
			            stack.setItemMeta(lam);
					}
				}
			}						
		return stack;
	}
	private List<String> loadStringListFromFile(String path){
		File file = new File(path);
		List<String> list = new ArrayList<String>();
		if(file.exists()){
			try{
				//BufferedReader reader = new BufferedReader(new FileReader(file));
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF8"));
				String readed = "";
				readed = reader.readLine();
				while(readed != null){
					list.add(ch.ColorIt(readed));
					readed = reader.readLine();
				}	
				reader.close();
			}catch(FileNotFoundException e){
				System.out.println("File " + path + " not found!");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Cant read the file");
				e.printStackTrace();
			}			
		}else{			
			list.add(ch.ColorIt("&4Something went wrong, please report this to an admin"));
		}
		return list;
	}
	private BookMeta getBookMetafromPath(BookMeta bmeta,String path){		
		List<String> list = loadStringListFromFile(path);
		List<String> lorelist = new ArrayList<String>();
		lorelist.add("No description here");
		bmeta.setAuthor("Noone has written this ;D");
		bmeta.setTitle("No Title found");
		bmeta.setLore(lorelist);
		for(int i = 0; i < list.size();i++){
			if(list.get(i).contains("author:")){
				bmeta.setAuthor(ch.ColorIt(replaceUmlaute(list.get(i).split("author:")[1])));
			}else if(list.get(i).contains("title:")){
				bmeta.setTitle(ch.ColorIt(replaceUmlaute(list.get(i).split("title:")[1])));
			}else if(list.get(i).contains("description:")){
				bmeta.setLore(replaceUmlaute(list.get(i).split("description:")[1].split(";")));
			}else{
				bmeta.addPage(replaceUmlaute(list.get(i)));
			}
		}		
		return bmeta;
	}
	private BookMeta getBookMetafromList(BookMeta bmeta,String data){
		if(data.split(",").length == 4){
			bmeta.setAuthor(data.split(",")[0].split("author:")[1]);
			bmeta.setTitle(data.split(",")[1].split("title:")[1]);
			bmeta.setLore(Arrays.asList(new String[] {data.split(",")[2].split("description:")[1]}));
			String[] pages = data.split(",")[3].replace("[newpage]", "newpage").split("newpage");			
			for(int i = 0; i < pages.length;i++){
				bmeta.addPage(replaceUmlaute(pages[i]));
			}	
		}else{
			bmeta.setAuthor(ch.ColorIt("&4Server"));
			bmeta.setTitle(ch.ColorIt("&4Book from the Server!"));
			bmeta.addPage(data);
		}				
		return bmeta;
	}
	private String replaceUmlaute(String msg){
		return ch.ColorIt(msg).replace("[newline] ", "\n").replace("[newline]", "\n");
	}
	private List<String> replaceUmlaute(String[] list){	
		List<String> listreturn = new ArrayList<String>();
		for(int i = 0; i < list.length;i++){
			listreturn.add(replaceUmlaute(list[i]));
		}		
		return listreturn;
	}
	}
	
	
