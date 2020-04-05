package de.cultcraft.zero.utils;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class ZAItem {


	//public static AmountHandler amounthandler;
	//public static EnchantmentHandler enchantMenthandler;
	//public static PriceHandler pricehandler;
		
	
	public static Map<String, Object> itemToMap(ItemStack stack){		
		Map<String, Object> sitem = stack.serialize();
		return sitem;
	}
	
	public static ItemStack MapToItem(Map<String, Object> item){
		return ItemStack.deserialize(item);
	}

	public static void setHandler(){
		//amounthandler = new AmountHandler();
		//enchantMenthandler = new EnchantmentHandler();
		//pricehandler = new PriceHandler();
	}
	/*public static void setupIconMenus(){			
		for(String key : ZeroAuction.submenuconfigs.keySet()){			
			
			FileConfiguration temp = ZeroAuction.submenuconfigs.get(key);
			int size = (temp.getList("selectable_items").size()/9)*9;
			
			if(size < temp.getList("selectable_items").size()){
				size = ((temp.getList("selectable_items").size()/9)+1)*9;
			}	

			//Create submenus for buymenu -> redirecting to an amounthandler class
			//IconMenu tempmenu  = new IconMenu(temp.getString("Settings.description"), size, amounthandler, ZeroAuction.instance);
			ItemStack icon = new ItemStack(Material.valueOf(temp.getString("Settings.item_icon")));
			
			//Bearbeite Icon des Submenus
			if(temp.contains("Settings.enchantments")){
				for(String x : temp.getStringList("Settings.enchantments")){				
					if(icon.getType().equals(Material.TIPPED_ARROW)) {
						if(PotionType.valueOf(x.split(";")[0]) != null){
							setArrowEnchantmentType(icon, PotionType.valueOf(x.split(";")[0]), Boolean.valueOf(x.split(";")[1]), Boolean.valueOf(x.split(";")[2]));
						}else{
							System.out.println("Missconfiguration of ArrowType:" + icon.getType() + " (" + x + "), unknown ArrowType");
						}
					}else if(icon.getType().equals(Material.POTION) || icon.getType().equals(Material.SPLASH_POTION)){
						if(PotionType.valueOf(x.split(";")[0]) != null){
							setPotionEnchantmentType(icon, PotionType.valueOf(x.split(";")[0]),  Boolean.valueOf(x.split(";")[1]), Boolean.valueOf(x.split(";")[2]));
						}else{
							System.out.println("Missconfiguration of PotionType:" + icon.getType() + " (" + x + "), unknown PotionType");
						}
					}else{
						if(Enchantment.getByKey(NamespacedKey.minecraft(x.split(";")[0].toLowerCase())) != null){
							setBookEnchantmentType(icon, Enchantment.getByKey(NamespacedKey.minecraft(x.split(";")[0].toLowerCase())), Integer.valueOf(x.split(";")[1]));							
						}else{
							System.out.println("Missconfiguration of Enchantment:" + icon.getType() + " (" + x + "), unknown Enchantment");
						}
					}
				}
			}
			//Füll mit items
			if(temp.contains("selectable_items")){
				int cnt = 0;
				for(Object s : temp.getList("selectable_items")){
					//{APPLE=[PROTECTION_ENVIRONMENTAL,1, DURABILITY,4]}
					//nodename = Material
					String nodename = s.toString().split("\\{")[1].split("=")[0];
					ItemStack item = new ItemStack(Material.valueOf(nodename));
					if(s.toString().split("\\[").length > 1){
						String[] ent = s.toString().split("\\[")[1].split("\\]")[0].split(",");
						for(String x : ent){
							//typ;lvl
							if(item.getType().equals(Material.TIPPED_ARROW)) {
								if(PotionType.valueOf(x.split(";")[0]) != null){
									setArrowEnchantmentType(item, PotionType.valueOf(x.split(";")[0]), Boolean.valueOf(x.split(";")[1]), Boolean.valueOf(x.split(";")[2]));
								}else{
									System.out.println("Missconfiguration of ArrowType:" + icon.getType() + " (" + x + "), unknown ArrowType");
								}
							}else if(item.getType().equals(Material.POTION) || item.getType().equals(Material.SPLASH_POTION)){
								if(PotionType.valueOf(x.split(";")[0]) != null){
									setPotionEnchantmentType(item, PotionType.valueOf(x.split(";")[0]),  Boolean.valueOf(x.split(";")[1]), Boolean.valueOf(x.split(";")[2]));
								}else{
									System.out.println("Missconfiguration of PotionType:" + icon.getType() + " (" + x + "), unknown PotionType");
								}
							}else{
								if(Enchantment.getByKey(NamespacedKey.minecraft(x.split(";")[0].toLowerCase())) != null){
									setBookEnchantmentType(item, Enchantment.getByKey(NamespacedKey.minecraft(x.split(";")[0].toLowerCase())), Integer.valueOf(x.split(";")[1]));							
								}else{
									System.out.println("Missconfiguration of Enchantment:" + icon.getType() + " (" + x + "), unknown Enchantment");
								}
							}							
						}
					}	
					tempmenu.setOption(cnt++, item,	nodename,new String[0]);
				}
			}else{
				System.out.println("no items provided");
			}				
			
			BuyMenu.menues.put(temp.getString("Settings.name"), tempmenu);
			//BuyMenu.setOption(temp.getInt("Settings.position"), new ItemStack(Material.valueOf(temp.getString("Settings.item_icon")), 1), temp.getString("Settings.name"),  temp.getString("Settings.lore").split("<break>"));
		}
		//BuyMenu.setOption(26, new ItemStack(Material.SIGN, 1), "Exit", "Schließt das Fenster");
		//BuyMenu.setUpPreCompiledInventory();
	}*/
	
	public static ItemStack setEnchantmentType(ItemStack x,Enchantment e,int lvl){
		if(!x.getType().equals(Material.ENCHANTED_BOOK)){
			x.addEnchantment(e, lvl);
		}else{
			return setBookEnchantmentType(x, e, lvl);
		}
		return x;
	}
	public static ItemStack setBookEnchantmentType(ItemStack x,Enchantment e,int lvl){
		if(x.getType().equals(Material.ENCHANTED_BOOK)){
			EnchantmentStorageMeta  meta = (EnchantmentStorageMeta ) x.getItemMeta();
			meta.addEnchant(e, lvl, false);
			x.setItemMeta(meta);
		}else{
			return setEnchantmentType(x, e, lvl);
		}
		return x;
	}
	public static ItemStack setArrowEnchantmentType(ItemStack x,PotionType e,boolean extented,boolean upgraded){
		if(x.getType().equals(Material.TIPPED_ARROW) || x.getType().equals(Material.SPLASH_POTION)){
			PotionMeta meta = (PotionMeta)x.getItemMeta();
			PotionData pdata = new PotionData(e,extented,upgraded);
			
			meta.setBasePotionData(pdata);
			x.setItemMeta(meta);
		}
		return x;
	}
	public static ItemStack setPotionEnchantmentType(ItemStack x,PotionType e,boolean extented,boolean upgraded){
		if(x.getType().equals(Material.POTION)){
			PotionMeta meta = (PotionMeta)x.getItemMeta();
			PotionData pdata = new PotionData(e,extented,upgraded);
			meta.setBasePotionData(pdata);
			x.setItemMeta(meta);
		}
		return x;
	}
}
