package game.engine.cells;

import game.engine.monsters.Monster;

public class ConveyorBelt extends TransportCell {

	public ConveyorBelt(String name, int effect) {
		super(name, Math.abs(effect));
	}
	
	@Override
	public void transport(Monster monster) {
		monster.setPosition(monster.getPosition() + getEffect());
	}

}
