package game.engine.cells;

import game.engine.Constants;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier{

	public ContaminationSock(String name, int effect){
		super(name, -Math.abs(effect));
	}

	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		monster.alterEnergy(canisterValue);
	}

	public void transport(Monster monster) {
		monster.setPosition(monster.getPosition() + getEffect());
		if(!monster.isShielded())
			modifyCanisterEnergy(monster, -Constants.SLIP_PENALTY);
		else 
			monster.setShielded(false);
	}
}
