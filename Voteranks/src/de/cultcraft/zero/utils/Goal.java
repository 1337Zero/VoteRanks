package de.cultcraft.zero.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
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
	// private ArrayList<ItemStack> books = new ArrayList<ItemStack>();
	private HashMap<String, ItemStack> books = new HashMap<String, ItemStack>();
	private String id = null;

	private ArrayList<String> usedItemList = new ArrayList<String>();
	private ArrayList<String> usedCommandList = new ArrayList<String>();

	private ArrayList<Integer> dbItems = new ArrayList<Integer>();

	/**
	 * Creates a filled Goal
	 * 
	 * @param votes
	 * @param type
	 * @param personalMessage
	 * @param broadcast
	 * @param item
	 * @param command
	 * @param book
	 */
	public Goal(int votes, Goaltype type, ArrayList<String> personalMessage, ArrayList<String> broadcast,
			ArrayList<ItemStack> item, ArrayList<String> command, HashMap<String, ItemStack> books, String id) {
		this.books = books;
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
	public Goal() {
	}

	public static Goal GoalsFromString(String[] line) throws NumberFormatException, Exception {

		Goal goal = new Goal();

		if (line[0].contains("votes=")) {
			goal.setType(Goaltype.SAME);
			goal.setVotes(Integer.parseInt(line[0].split("=")[1]));
		} else if (line[0].contains("votes>")) {
			goal.setType(Goaltype.BIGGER);
			goal.setVotes(Integer.parseInt(line[0].split(">")[1]));
		} else if (line[0].contains("votes<")) {
			goal.setType(Goaltype.SMALLER);
			goal.setVotes(Integer.parseInt(line[0].split("<")[1]));
		} else if (line[0].contains("votes%")) {
			goal.setType(Goaltype.MODULO);
			goal.setVotes(Integer.parseInt(line[0].split("%")[1]));
		}

		for (int x = 0; x < line.length; x++) {
			if ((line[x].contains("Message=")) || (line[x].contains("message="))) {
				goal.addPersonalMessage(ChatManager.ColorIt(line[x].split("=")[1]));
			} else if ((line[x].contains("broadcast=")) || (line[x].contains("Broadcast="))) {
				goal.addBroadcast(ChatManager.ColorIt(line[x].split("=")[1]));
			} else if ((line[x].contains("Give=")) || (line[x].contains("give="))) {
				String goalpartid = line[x].split("=")[1];
				// Give=264:0,1,null,-1,null,-1,-1,-1
				String[] subdata = goalpartid.split(",");
				if (subdata.length == 8) {
					Enchantment enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(subdata[2]));
					ItemStack stack = getItemStack(Material.valueOf(subdata[0]), Integer.parseInt(subdata[1]),
							enchantment, Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]),
							Integer.parseInt(subdata[6]), Integer.parseInt(subdata[7]));
					goal.addItem(stack);
				} else {
					if (subdata[0].trim().equalsIgnoreCase("randomitem")) {
						goal.addRandomItemList("randomitemlist");
					} else {
						if (VoteRanks.config.getList(subdata[0].trim()) != null) {
							goal.addRandomItemList(subdata[0]);
						} else {
							System.out.println("[Vote-Rank] ERROR while searching for list '" + subdata[0]
									+ " it's not in your config!");
							throw new NoSuchFieldError(subdata[0] + " is not in your config!");
						}
					}
				}
			} else if (line[x].contains("GiveDb=") || line[x].contains("giveDb=") || line[x].contains("givedb=")
					|| line[x].contains("Givedb=")) {
				int itemID = Integer.parseInt(line[x].split("=")[1]);
				goal.addItem(VoteRanks.dbtask.loadItem(itemID));
				goal.dbItems.add(itemID);
			} else if ((line[x].contains("command=")) || (line[x].contains("Command="))) {
				if (!isNumeric(line[x].split("=")[1])) {
					goal.addCommand(line[x].split("=")[1]);
				} else if (line[x].split("=")[1].equalsIgnoreCase("randomcommandlist")) {
					goal.addCommand("randomcommand");
					goal.addRandomCommandList("randomcommandlist");
				}
			} else if ((line[x].contains("Book=")) || (line[x].contains("book="))) {
				ItemStack book = bookStackFromFile(line[x]);
				goal.addBook(line[x].split("=")[1].split("]")[1], book);
			} else if ((line[x].contains("id=")) || (line[x].contains("Id="))) {
				goal.setId(line[x].split("=")[1]);
			}
		}
		return goal;
	}

	private static boolean isNumeric(String str) {
		return str.matches("[\\d]*");
	}

	public static String getRandomCommand() {
		List<?> votelist = VoteRanks.config.getList("randomcommandlist");
		Random r = new Random();
		return votelist.get(r.nextInt(votelist.size())).toString();
	}

	public static ItemStack getBookFromFile(String path) {
		File f = new File(path);

		if (!f.exists()) {
			return null;
		} else {
			return bookStackFromFile("book=[file]" + path);
		}
	}

	private static ItemStack bookStackFromFile(String line) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bmeta = (BookMeta) book.getItemMeta();
		if ((line.split("=")[1].contains("[file]")) || (line.split("=")[1].contains("[File]"))) {
			book.setItemMeta(getBookMetafromPath(bmeta, line.split("=")[1].split("]")[1]));
		} else {
			book.setItemMeta(getBookMetafromList(bmeta, line.split("=")[1]));
		}
		return book;
	}

	private static BookMeta getBookMetafromPath(BookMeta bmeta, String path) {
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

	private static BookMeta getBookMetafromList(BookMeta bmeta, String data) {
		if (data.split(",").length == 4) {
			bmeta.setAuthor(data.split(",")[0].split("author:")[1]);
			bmeta.setTitle(data.split(",")[1].split("title:")[1]);
			bmeta.setLore(Arrays.asList(new String[] { data.split(",")[2].split("description:")[1] }));
			String[] pages = data.split(",")[3].replace("[newpage]", "newpage").split("newpage");
			for (int i = 0; i < pages.length; i++)
				bmeta.addPage(new String[] { replaceUmlaute(pages[i]) });
		} else {
			bmeta.setAuthor(ChatManager.ColorIt("&4Server"));
			bmeta.setTitle(ChatManager.ColorIt("&4Book from the Server!"));
			bmeta.addPage(new String[] { data });
		}
		return bmeta;
	}

	private static ItemStack getItemStack(Material id, int amount, Enchantment ent, int enchantmentlvl, String name,
			int red, int green, int blue) throws Exception {
		ItemStack stack = new ItemStack(id, amount);
		if ((ent != null)) {
			try {
				stack.addUnsafeEnchantment(ent, enchantmentlvl);
			} catch (IllegalArgumentException e) {
				System.out.println(ent + " can't be on " + stack.getItemMeta().getDisplayName());
			}
		}
		if (((name != null ? 1 : 0) & (name.equalsIgnoreCase("null") ? 0 : 1)) != 0) {
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatManager.ColorIt(name));
			stack.setItemMeta(meta);
		}
		if ((red != -1) && (green != -1) && (blue != -1)) {
			LeatherArmorMeta lam = (LeatherArmorMeta) stack.getItemMeta();
			lam.setColor(Color.fromBGR(blue, green, red));
			if (((name != null ? 1 : 0) & (name.equalsIgnoreCase("null") ? 0 : 1)) != 0) {
				lam.setDisplayName(ChatManager.ColorIt(name));
			}
			stack.setItemMeta(lam);
		}
		return stack;
	}

	public static ItemStack getItemStackFromString(String subdata[]) throws NumberFormatException, Exception {
		Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(subdata[2].toLowerCase()));
		return getItemStack(Material.valueOf(subdata[0]), Integer.parseInt(subdata[1]), enchantment,
				Integer.parseInt(subdata[3]), subdata[4], Integer.parseInt(subdata[5]), Integer.parseInt(subdata[6]),
				Integer.parseInt(subdata[7]));
	}

	private static List<String> loadStringListFromFile(String path) {
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

	private static String replaceUmlaute(String msg) {
		return ChatManager.ColorIt(msg).replace("[newline] ", "\n").replace("[newline]", "\n");
	}

	private static List<String> replaceUmlaute(String[] list) {
		List<String> listreturn = new ArrayList<String>();
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

	public HashMap<String, ItemStack> getBook() {
		return books;
	}

	public void addBook(String path, ItemStack book) {
		this.books.put(path, book);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean hasAccess(Player p) {
		if (id != null) {
			if (!p.hasPermission("voteranks.except." + getId())) {
				/* The User has the right to trigger the Goal */
				return true;
			} else {
				/* The User hasn't the right to trigger the Goal */
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Goal [votes=" + votes + ", type=" + type + ", getBroadcast()=" + getBroadcast() + ", getVotes()="
				+ getVotes() + ", getType()=" + getType() + ", getPersonalMessage()=" + getPersonalMessage()
				+ ", getItem()=" + getItem() + ", getCommand()=" + getCommand() + ", getBook()=" + getBook() + "]";
	}

	public boolean addRandomItemList(String list) {
		if (VoteRanks.config.getList(list) != null) {
			this.usedItemList.add(list);
			this.addItem(new RandomItemStack(list));
			return true;
		} else {
			this.usedItemList.add(list);
			System.out.println("randomitemList " + list + "not found!");
			return false;
		}
	}

	public boolean addRandomCommandList(String list) {
		if (VoteRanks.config.getList(list) != null) {
			this.usedCommandList.add(list);
			return true;
		} else {
			this.usedCommandList.add(list);
			return false;
		}
	}

	public String toConfigString() {
		StringBuilder builder = new StringBuilder();

		// selector
		builder.append("votes" + this.type.typeToString() + this.getVotes() + ";");
		// votes=1;
		// Message=Your first vote!;
		// broadcast=<player> has 1 vote;
		// Give=264:0,1,null,-1,null,-1,-1,-1;
		// giveDb=1

		for (String s : this.getPersonalMessage()) {
			builder.append("message=" + s + ";");
		}
		for (String s : this.getBroadcast()) {
			builder.append("broadcast=" + s + ";");
		}
		for (String s : this.getCommand()) {
			builder.append("command=" + s + ";");
		}
		for (String s : this.getBook().keySet()) {
			builder.append("book=" + s + ";");
		}
		for (String s : this.usedCommandList) {
			builder.append("command=" + s + ";");
		}
		for (String s : this.usedItemList) {
			builder.append("give=" + s + ";");
		}
		for (Integer s : this.dbItems) {
			builder.append("dbitem=" + s + ";");
		}

		return builder.toString();
	}

	public ArrayList<Integer> getDbItems() {
		return dbItems;
	}

	public ArrayList<String> getUsedItemList() {
		return usedItemList;
	}

	public ArrayList<String> getUsedCommandList() {
		return usedCommandList;
	}
}
