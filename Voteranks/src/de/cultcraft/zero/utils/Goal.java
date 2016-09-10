package de.cultcraft.zero.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import de.cultcraft.zero.voteranks.VoteRanks;


public class Goal {

	private ArrayList<String> broadcast = new ArrayList<String>();
	private int votes;
	private Goaltype type;
	private ArrayList<String> personalMessage = new ArrayList<String>();
	private ArrayList<ItemStack> item = new ArrayList<ItemStack>();
	private ArrayList<String> command = new ArrayList<String>();
	private ArrayList<ItemStack> books = new ArrayList<ItemStack>();
	private String id = null;
	/**
	 * Creates a filled Goal
	 * @param votes
	 * @param type
	 * @param personalMessage
	 * @param broadcast
	 * @param item
	 * @param command
	 * @param book
	 */
	public Goal(int votes,Goaltype type,ArrayList<String> personalMessage,ArrayList<String> broadcast,ArrayList<ItemStack> item,ArrayList<String> command,ArrayList<ItemStack> book,String id){
		this.books = book;
		this.votes = votes;
		this.type = type;
		this.broadcast = broadcast;
		this.personalMessage = personalMessage;
		this.command = command;
		this.item = item;
		this.id = id;
	}
	/**
	 * Creates an empty Goal
	 */
	public Goal(){		
	}
	public static Goal GoalsFromString(String[] line) throws NumberFormatException, Exception{
		
		Goal goal = new Goal();
		
		if (line[0].contains("votes=")){
			goal.setType(Goaltype.SAME);
			goal.setVotes(Integer.parseInt(line[0].split("=")[1]));			
		}else if(line[0].contains("votes>")){
			goal.setType(Goaltype.BIGGER);
			goal.setVotes(Integer.parseInt(line[0].split(">")[1]));					
		}else if(line[0].contains("votes<")){
			goal.setType(Goaltype.SMALLER);
			goal.setVotes(Integer.parseInt(line[0].split("<")[1]));				
		}else if(line[0].contains("votes%")){
			goal.setType(Goaltype.MODULO);
			goal.setVotes(Integer.parseInt(line[0].split("%")[1]));	
		}
		
		for (int x = 0; x < line.length; x++){			
			 if ((line[x].contains("Message=")) || (line[x].contains("message="))) {
				 goal.addPersonalMessage(ChatManager.ColorIt(line[x].split("=")[1]));
			 }else if ((line[x].contains("broadcast=")) || (line[x].contains("Broadcast="))) {
				 goal.addBroadcast(ChatManager.ColorIt(line[x].split("=")[1]));
			 }else if ((line[x].contains("Give=")) || (line[x].contains("give="))) {
				 String goalpartid = line[x].split("=")[1];
				 //Give=264:0,1,null,-1,null,-1,-1,-1
				 if(goalpartid.equalsIgnoreCase("randomitem")){
					 goal.addItem(new RandomItemStack());
				 }else{
					String[] subdata = goalpartid.split(",");	             
					ItemStack stack = getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7]));
					goal.addItem(stack);
				 }
	          } else if ((line[x].contains("command=")) || (line[x].contains("Command="))) {
	              goal.addCommand(line[x].split("=")[1]);	            
			 } else if ((line[x].contains("Book=")) || (line[x].contains("book="))) {
	              ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
	              BookMeta bmeta = (BookMeta)book.getItemMeta();
	              if ((line[x].split("=")[1].contains("[file]")) || (line[x].split("=")[1].contains("[File]"))) {
	                book.setItemMeta(getBookMetafromPath(bmeta, line[x].split("=")[1].split("]")[1]));
	              } else {
	                book.setItemMeta(getBookMetafromList(bmeta, line[x].split("=")[1]));
	              }
	              goal.addBook(book);
	        } else if ((line[x].contains("id=")) || (line[x].contains("Id="))) {
	        	goal.setId(line[x].split("=")[1]);
	        }
		}	
		return goal;
	}
	
	private static BookMeta getBookMetafromPath(BookMeta bmeta, String path) {
		    List list = loadStringListFromFile(path);
		    for (int i = 0; i < list.size(); i++) {
		      if (((String)list.get(i)).contains("author:"))
		        bmeta.setAuthor(ChatManager.ColorIt(replaceUmlaute(((String)list.get(i)).split("author:")[1])));
		      else if (((String)list.get(i)).contains("title:"))
		        bmeta.setTitle(ChatManager.ColorIt(replaceUmlaute(((String)list.get(i)).split("title:")[1])));
		      else if (((String)list.get(i)).contains("description:"))
		        bmeta.setLore(replaceUmlaute(((String)list.get(i)).split("description:")[1].split(";")));
		      else {
		        bmeta.addPage(new String[] { replaceUmlaute((String)list.get(i)) });
		      }
		    }
		    return bmeta;
		  }
		  private static BookMeta getBookMetafromList(BookMeta bmeta, String data) {
		    if (data.split(",").length == 4) {
		      bmeta.setAuthor(data.split(",")[0].split("author:")[1]);
		      bmeta.setTitle(data.split(",")[1].split("title:")[1]);
		      bmeta.setLore(Arrays.asList(new String[] { data.split(",")[2].split("description:")[1] }));
		      String[] pages = data.split(",")[3].replace("[newpage]", "newpage").split("newpage");
		      for (int i = 0; i < pages.length; i++)
		        bmeta.addPage(new String[] { replaceUmlaute(pages[i]) });
		    }
		    else {
		      bmeta.setAuthor(ChatManager.ColorIt("&4Server"));
		      bmeta.setTitle(ChatManager.ColorIt("&4Book from the Server!"));
		      bmeta.addPage(new String[] { data });
		    }
		    return bmeta;
		  }
	
	  private static ItemStack getItemStack(int id, int subid, int amount, Enchantment ent, int enchantmentlvl, String name, int red, int green, int blue) throws Exception{
		  ItemStack stack = new ItemStack(id, amount);
		  if((ent != null) && (id != -1)) {
			  try {
				  stack.addUnsafeEnchantment(ent, enchantmentlvl);
			 }catch (IllegalArgumentException e) {
			      System.out.println(ent + " can't be on " + stack.getItemMeta().getDisplayName());
			 }
		  }

		  if (subid != -1) {
			  stack.setDurability((short)subid);
		  }
		  if (((name != null ? 1 : 0) & (name.equalsIgnoreCase("null") ? 0 : 1)) != 0) {
			  ItemMeta meta = stack.getItemMeta();
			  meta.setDisplayName(ChatManager.ColorIt(name));
			  stack.setItemMeta(meta);
		  }
		  if ((red != -1) &&  (green != -1) && (blue != -1)) {
			  LeatherArmorMeta lam = (LeatherArmorMeta)stack.getItemMeta();
			  lam.setColor(Color.fromBGR(blue, green, red));
			  if(((name != null ? 1 : 0) & (name.equalsIgnoreCase("null") ? 0 : 1)) != 0) {
				  lam.setDisplayName(ChatManager.ColorIt(name));
			  }
			  stack.setItemMeta(lam);
		  }

		  return stack;
	  }
	  
	  public static ItemStack getItemStackFromString(String subdata[]) throws NumberFormatException, Exception{
		  return getItemStack(Integer.parseInt(subdata[0].split(":")[0]), Integer.parseInt(subdata[0].split(":")[1]), Integer.parseInt(subdata[1]), Enchantment.getByName(subdata[2]), Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7]));
	  }
	  private static List<String> loadStringListFromFile(String path) {
		    File file = new File(path);
		    List list = new ArrayList();
		    if (file.exists())
		      try
		      {
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
	  
	  private static String replaceUmlaute(String msg) {
		  return ChatManager.ColorIt(msg).replace("[newline] ", "\n").replace("[newline]", "\n");
	  }
	  
	  private static List<String> replaceUmlaute(String[] list) {
		  List listreturn = new ArrayList();
		  for (int i = 0; i < list.length; i++) {
			  listreturn.add(replaceUmlaute(list[i]));
		  }
		  return listreturn;
	  }  
	  
	public ArrayList<String> getBroadcast() {
		return broadcast;
	}
	public void addBroadcast(String broadcast) {
		this.broadcast.add(broadcast);
	}
	public int getVotes() {
		return votes;
	}
	public void setVotes(int votes) {
		this.votes = votes;
	}
	public Goaltype getType() {
		return type;
	}
	public void setType(Goaltype type) {
		this.type = type;
	}
	public ArrayList<String> getPersonalMessage() {
		return personalMessage;
	}
	public void addPersonalMessage(String personalMessage) {
		this.personalMessage.add(personalMessage);
	}
	public ArrayList<ItemStack> getItem() {
		return item;
	}
	public void addItem(ItemStack item) {
		this.item.add(item);
	}
	public ArrayList<String> getCommand() {
		return command;
	}
	public void addCommand(String command) {
		this.command.add(command);
	}
	public ArrayList<ItemStack> getBook() {
		return books;
	}
	public void addBook(ItemStack book) {
		this.books.add(book);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean hasAccess(Player p){
		if(id != null){
			if(!p.hasPermission("voteranks.except." + getId())){
				/* The User has the right to trigger the  Goal*/
				return true;
			}else{
				/* The User hasn't the right to trigger the  Goal*/
				return false;
			}
		}
		return true;
	}
	@Override
	public String toString() {
		return "Goal [votes=" + votes + ", type=" + type + ", getBroadcast()="
				+ getBroadcast() + ", getVotes()=" + getVotes()
				+ ", getType()=" + getType() + ", getPersonalMessage()="
				+ getPersonalMessage() + ", getItem()=" + getItem()
				+ ", getCommand()=" + getCommand() + ", getBook()=" + getBook()
				+ "]";
	}	
}

