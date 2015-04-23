package de.cultcraft.zero.utils;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
			}
			
			if(allow == true){				
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
						  p.getInventory().addItem(goal.getItem().get(i));
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
