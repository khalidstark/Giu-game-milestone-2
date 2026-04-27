package game.engine.cards;

import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.*;

public class EnergyStealCard extends Card implements CanisterModifier {
	private int energy;
	
	public void modifyCanisterEnergy(Monster monster, int canisterValue){
		
	}
	
	public void performAction(Monster player, Monster opponent){
		
	}

	public EnergyStealCard(String name, String description, int rarity, int energy) {
		super(name, description, rarity, true);
		this.energy = energy;
	}
	
	public int getEnergy() {
		return energy;
	}
	
}
