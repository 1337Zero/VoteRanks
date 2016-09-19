package de.cultcraft.zero.utils;

import org.bukkit.inventory.ItemStack;

public class RandomItemStack extends ItemStack{

	private String list;

	public RandomItemStack(String list){
		this.list = list;
	}
	public String getList() {
		return list;
	}
	public void setList(String list) {
		this.list = list;
	}

	
}
