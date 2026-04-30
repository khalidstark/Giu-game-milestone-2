package game.engine;
import game.engine.Board;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	 public Game(Role playerRole) throws IOException {
	        this.board = new Board(DataLoader.readCards());
	        this.allMonsters = DataLoader.readMonsters();
	        ArrayList<Monster> copyref = DataLoader.readMonsters();
	        ArrayList<Monster> saved = this.allMonsters;
	        this.allMonsters = copyref;
	        this.player = selectRandomMonsterByRole(playerRole);
	        Role oppositeRole = (playerRole == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
	        this.opponent = selectRandomMonsterByRole(oppositeRole);
	        this.allMonsters = saved;
	        this.current=player;
	        copyref.remove(player);
	        copyref.remove(opponent);
	        Board.setStationedMonsters(copyref);
	        board.initializeBoard(DataLoader.readCells());
	    }
	
	public Board getBoard() {
		return board;
	}
	
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters; 
	}
	
	public Monster getPlayer() {
		return player;
	}
	
	public Monster getOpponent() {
		return opponent;
	}
	
	public Monster getCurrent() {
		return current;
	}
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	 private Monster selectRandomMonsterByRole(Role role) {
	        ArrayList<Monster> candidates = new ArrayList<>();
	        for (int i = 0; i < allMonsters.size(); i++) {
	            if (allMonsters.get(i).getRole() == role) {
	                candidates.add(allMonsters.get(i));
	            }
	        }
	        if (candidates.isEmpty()) return null;
	        Collections.shuffle(candidates);
	        return candidates.get(0);
	    }
	
	private Monster getCurrentOpponent(){
		if(current == player)
			return opponent;
		else 
			return player;
	}
	
	 private int rollDice(){
		 return (int) (Math.random()*6) + 1;
	 }
	 
	 public void usePowerup() throws OutOfEnergyException {
	        if (current.getEnergy() < Constants.POWERUP_COST) {
	            throw new OutOfEnergyException();
	        }
	        current.alterEnergy(-Constants.POWERUP_COST);
	        current.executePowerupEffect(getCurrentOpponent());
	    }

	    public void playTurn() throws InvalidMoveException {
	        if (current.isFrozen()) {
	            current.setFrozen(false);
	            switchTurn();
	            return;
	        }
	        int roll = rollDice();
	        board.moveMonster(current, roll, getCurrentOpponent());
	        switchTurn();
	    }

	    private void switchTurn() {
	        current = getCurrentOpponent();
	    }

	    private boolean checkWinCondition(Monster monster) {
	        return monster.getPosition() == Constants.WINNING_POSITION
	                && monster.getEnergy() >= Constants.WINNING_ENERGY;
	    }

	    public Monster getWinner() {
	        if (checkWinCondition(player)) return player;
	        if (checkWinCondition(opponent)) return opponent;
	        return null;
	    }
}
