package game.engine.cards;

import game.engine.monsters.Monster;

public class SwapperCard extends Card {

	public void performAction(Monster player, Monster opponent){
		if(player.compareTo(opponent) < 0){
			int temp = player.getPosition();
			player.setPosition(opponent.getPosition());
			opponent.setPosition(temp);
		}
	}
	
	public SwapperCard(String name, String description, int rarity) {
		super(name, description, rarity, true);
	}
	
}
