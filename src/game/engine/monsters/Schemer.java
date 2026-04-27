package game.engine.monsters;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	
	public void setEnergy(int energy) {
        int delta = energy - getEnergy();
        super.setEnergy(getEnergy() + delta + Constants.SCHEMER_STEAL);
	}
	
	private int stealEnergyFrom(Monster target) {
		 int steal = Math.min(Constants.SCHEMER_STEAL, target.getEnergy());
		 target.setEnergy(target.getEnergy() - steal);
	     return steal;
	 }
	
 	public void executePowerupEffect(Monster opponentMonster) {
 		int sum = stealEnergyFrom(opponentMonster);
 			for( Monster m: Board.getStationedMonsters()) {
 				int now = stealEnergyFrom(m);
 				if (now<Constants.SCHEMER_STEAL)
 				sum+=stealEnergyFrom(m);
 			}	
 			this.setEnergy(this.getEnergy()+sum);
 		
	 
 	}	
}
