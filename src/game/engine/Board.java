package game.engine;
import game.engine.dataloader.*;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;

import java.io.IOException;
import java.util.*;

import game.engine.monsters.*;
import game.engine.cards.*;

import java.util.ArrayList;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.monsters.Monster;
import game.engine.dataloader.*;
import game.engine.exceptions.InvalidCSVFormat;
public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> OriginalCardsCopy;
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		OriginalCardsCopy = originalCards;
		cards = new ArrayList<Card>();
		setCardsByRarity();
		reloadCards();
	}
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return OriginalCardsCopy;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}
	
	private int[] indexToRowCol(int index){
		int row = index / Constants.BOARD_ROWS;
		int col  = index % Constants.BOARD_COLS;
		
		if(row % 2 == 1){
			col = Constants.BOARD_COLS - 1 -col;
		}
		
		int[] pair = {row, col};
		return pair;
	}
	
	public void initializeBoard(ArrayList<Cell> specialCells){
		ArrayList<DoorCell> doors = new ArrayList<>();
		ArrayList<ConveyorBelt> belts = new ArrayList<>();
		ArrayList<ContaminationSock> socks = new ArrayList<>();
		for (Cell c : specialCells) {
			if (c instanceof DoorCell) {
				doors.add((DoorCell) c);
			} else if (c instanceof ConveyorBelt) {
				belts.add((ConveyorBelt) c);
			} else if (c instanceof ContaminationSock) {
				socks.add((ContaminationSock) c);
			}
		}
		
		for (int i = 0; i < Constants.BOARD_SIZE; i++) {
			if (i % 2 == 0) {
				setCell(i, new Cell("Rest " + i));
			} else {
				setCell(i, doors.remove(0));
			}
		}

		for (int idx : Constants.CARD_CELL_INDICES) {
			setCell(idx, new CardCell("Card " + idx));
		}
		for (int idx : Constants.CONVEYOR_CELL_INDICES) {
			setCell(idx, belts.remove(0));
		}
		for (int idx : Constants.SOCK_CELL_INDICES) {
			setCell(idx, socks.remove(0));
		}

		ArrayList<Monster> stationed = getStationedMonsters();
		for (int k = 0; k < Constants.MONSTER_CELL_INDICES.length && k < stationed.size(); k++) {
			int idx = Constants.MONSTER_CELL_INDICES[k];
			Monster m = stationed.get(k);
			m.setPosition(idx);
			MonsterCell mc = new MonsterCell(m.getName(), m);
			mc.setMonster(m);
			setCell(idx, mc);
		}
	}
	
	private Cell getCell(int index) {
		int[] rc = indexToRowCol(index);
		return boardCells[rc[0]][rc[1]];
	}

	private void setCell(int index, Cell cell) {
		int[] rc = indexToRowCol(index);
		boardCells[rc[0]][rc[1]] = cell;
	}
	
	private void setCardsByRarity() {
		ArrayList<Card> expanded = new ArrayList<>();
		for (Card c : originalCards) {
			for (int i = 0; i < c.getRarity(); i++) {
				expanded.add(c);
			}
		}
		originalCards = expanded;
	}

	public static void reloadCards() {
		cards = new ArrayList<>(originalCards);
		Collections.shuffle(cards);
	}

	public static Card drawCard() {
		if (cards.isEmpty()) {
			reloadCards();
		}
		return cards.remove(0);
	}

	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException{
		int oldPosition = currentMonster.getPosition();
		currentMonster.move(roll);
		
		getCell(currentMonster.getPosition()).onLand(currentMonster, opponentMonster); 
	
		if(getCell(currentMonster.getPosition()).isOccupied() && currentMonster.getPosition() == opponentMonster.getPosition()){
			currentMonster.setPosition(oldPosition);
			throw new InvalidMoveException();
		}

		if(currentMonster.isConfused()){
			currentMonster.decrementConfusion();
			opponentMonster.decrementConfusion();
		}
		updateMonsterPositions(currentMonster, opponentMonster);
	}
	
	private void updateMonsterPositions(Monster player, Monster opponent){
		for(int i = 0 ;i < Constants.BOARD_SIZE; i++){
			getCell(i).setMonster(null);
		}
		
		Cell playerCell = getCell(player.getPosition());
		playerCell.setMonster(player);
		
		Cell opponentCell = getCell(opponent.getPosition());
		opponentCell.setMonster(opponent);
	}

}
