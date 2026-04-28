package game.engine.cells;

import game.engine.monsters.Monster;

public class MonsterCell extends Cell{

	private final Monster cellMonster;

	public MonsterCell(String name, Monster cellMonster){
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster(){
		return cellMonster;
	}

	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		if (cellMonster.getRole() == landingMonster.getRole()) {
			landingMonster.executePowerupEffect(opponentMonster);
		} else if (landingMonster.getEnergy() > cellMonster.getEnergy()) {
			int landingEnergy = landingMonster.getEnergy();
			int stationedEnergy = cellMonster.getEnergy();
			cellMonster.setEnergy(landingEnergy);
			landingMonster.alterEnergy(stationedEnergy - landingEnergy);
		}
	}
}
