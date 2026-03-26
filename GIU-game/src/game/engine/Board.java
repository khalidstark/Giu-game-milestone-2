package game.engine;
import game.engine.dataloader.*;
import game.engine.cells.*;
import game.engine.Constants;

import java.io.IOException;
import java.util.*;

import game.engine.monsters.*;
import game.engine.cards.*;
import game.engine.dataloader.*;

public class Board {
	
	private final Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters;
	private static  ArrayList<Card> originalCards;
	private static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) throws IOException{
		boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<>();
		cards = new ArrayList<>();
		originalCards = readCards;
	}
	
	public Cell[][] getBoardCells(){
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters(){
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters){
		Board.stationedMonsters = stationedMonsters;
	}
	
	public static ArrayList<Card> getOriginalCards(){
		return originalCards;
	}
	
	public static ArrayList<Card> getCards(){
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards){
		Board.cards = cards;
	}
}
