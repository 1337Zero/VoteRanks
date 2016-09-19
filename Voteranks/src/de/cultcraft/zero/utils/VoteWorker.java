package de.cultcraft.zero.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.cultcraft.zero.voteranks.VoteRanks;

public class VoteWorker extends BukkitRunnable{

	private ArrayList<WorkTask> worktasks = new ArrayList<WorkTask>();
	private ArrayList<Goal> goals = new ArrayList<Goal>();
	
	public VoteWorker(ArrayList<Goal> goals) {
		this.goals = goals;
	}
	
	@Override
	public void run() {
		if(worktasks.size() > 0){		
			for(int i = 0; i < worktasks.size();i++){
				LookupGoalsAndPerform(worktasks.get(i).getVotes(), worktasks.get(i).getP());					
			}	
			worktasks.clear();
		}
	}
	public void addTask(WorkTask task){
		worktasks.add(task);
	}
	
	private void LookupGoalsAndPerform(int votes,Player p){
		for(Goal goal : goals){
			boolean allow = false;
			if(goal.getType().equals(Goaltype.BIGGER)){
				if(votes > goal.getVotes()){
					allow = true;
				}
			}else if(goal.getType().equals(Goaltype.SMALLER)){
				if(votes < goal.getVotes()){
					allow = true;
				}				
			}else if(goal.getType().equals(Goaltype.SAME)){
				if(votes == goal.getVotes()){
					allow = true;
				}			
			}else if(goal.getType().equals(Goaltype.MODULO)){
				if(votes % goal.getVotes() == 0){
					allow = true;
				}			
			}
			
			if(allow == true){	
				if(goal.hasAccess(p)){
					allow = false;
					//Broadcasts
					for(int i = 0; i < goal.getBroadcast().size();i++){
						Bukkit.getServer().broadcastMessage(goal.getBroadcast().get(i).replace("<player>", p.getName()).replace("<votes>", "" +votes));
					}
					
					//Messages to the Player
					if(p.isOnline()){
						for(int i = 0; i < goal.getPersonalMessage().size();i++){
							  p.sendMessage(goal.getPersonalMessage().get(i).replace("<player>", p.getName()).replace("<votes>", "" +votes));
						}
					}				
					//Commands
					for(int i = 0; i < goal.getCommand().size();i++){
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), goal.getCommand().get(i).replace("<player>", p.getName()));
					}
					
					if(p.isOnline()){
						//Items
						for(int i = 0; i < goal.getItem().size();i++){
							if(goal.getItem().get(i) instanceof RandomItemStack){
								p.getInventory().addItem(getRandomItem(((RandomItemStack) goal.getItem().get(i)), goal));
							}else{
								p.getInventory().addItem(goal.getItem().get(i));
							}						  
						}
						//Books
						for(int i = 0; i < goal.getBook().size();i++){
							  p.getInventory().addItem(goal.getBook().get(i));
						}
					}	
				}								
			}
		}		
	}
	private ItemStack getRandomItem(RandomItemStack rstack,Goal g){
		List<String> randomitem = (List<String>) VoteRanks.config.getList(rstack.getList());	
		Random r = new Random();
		try {
			int random = r.nextInt(randomitem.size());
			ItemStack s = Goal.getItemStackFromString(randomitem.get(random).split(","));
			return s;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		System.out.println("[Vote-Rank] ERROR while giving itemstack of goal " + g + " giving air instead!" );
		return new ItemStack(Material.AIR);
	}
}
