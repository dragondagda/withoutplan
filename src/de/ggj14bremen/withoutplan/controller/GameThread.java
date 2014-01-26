package de.ggj14bremen.withoutplan.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.SystemClock;
import android.util.Log;
import de.ggj14bremen.withoutplan.R;
import de.ggj14bremen.withoutplan.event.CellClicked;
import de.ggj14bremen.withoutplan.model.Board;
import de.ggj14bremen.withoutplan.model.Cell;
import de.ggj14bremen.withoutplan.model.Figure;
import de.ggj14bremen.withoutplan.model.Settings;
import de.ggj14bremen.withoutplan.model.Figure.Orientation;
import de.ggj14bremen.withoutplan.model.GameBoard;
import de.ggj14bremen.withoutplan.model.GameState;
import de.ggj14bremen.withoutplan.model.WPColor;
import de.ggj14bremen.withoutplan.util.Generator;

public class GameThread extends Thread implements Game{

	private static final int PAUSE_TIME = 1500;

	private final long SLEEP_TIME = 100L;
	
	private boolean running;
	
	private GameState state;
	
	private GameBoard board;
	
	private boolean reset = false;
	
	private List<Figure> figures;
	
	private int[] figureTurn;
	
	private int figureStep;
	
	private TimeScoreInfo timeScoreInfo;
	
	private int boardSizeX;
	
	private int boardSizeY;
	
	private int amountFigures;
	
	private long stepTime;
	
	private int enemyLife;
	
	private boolean showedMoveTarget;
	
	private boolean showedOrientation;
	
	/**
	 * should be changed to true if end of state 
	 * is reached through timer or through event.
	 */
	private boolean next;
	
	private int round;
	
	private int roundsAfterSpeedup;
	
	public GameThread(){
		running = true;
		boardSizeX = Settings.getBoardSizeX();
		boardSizeY = Settings.getBoardSizeY();
		enemyLife = Settings.getEnemyLife();
		roundsAfterSpeedup = Settings.getRoundsAfterSpeedUp();
		amountFigures = Settings.getAmountFigures();
		stepTime = Settings.getStepTime();
		board = new GameBoard(boardSizeX, boardSizeY);
		next = false;
		figures = new ArrayList<Figure>();
		figureStep = 0;
		this.state = GameState.INIT;
		figureTurn = new int[amountFigures];
		this.timeScoreInfo = new TimeScoreInfo(stepTime, 0);
		round = 0;
		showedMoveTarget = false;
		showedOrientation = false;
		this.start();
	}
	
	private void reinit(){
		this.reset = false;
		running = true;
		boardSizeX = Settings.getBoardSizeX();
		boardSizeY = Settings.getBoardSizeY();
		amountFigures = Settings.getAmountFigures();
		stepTime = Settings.getStepTime();
		enemyLife = Settings.getEnemyLife();
		roundsAfterSpeedup = Settings.getRoundsAfterSpeedUp();
		board = new GameBoard(boardSizeX, boardSizeY);
		next = false;
		figures = new ArrayList<Figure>();
		figureStep = 0;
		this.state = GameState.INIT;
		figureTurn = new int[amountFigures];
		this.timeScoreInfo = new TimeScoreInfo(stepTime, 0);
		showedMoveTarget = false;
		showedOrientation = false;
		round = 0;
		this.randomizeFigureTurn();
	}

	private void randomizeFigureTurn() {
		int index = 0;
		final Set<Integer> alreadyInserted = new HashSet<Integer>();
		while(index<amountFigures){
			final int element = Generator.randomIntBetween(0, amountFigures-1);
			if(!alreadyInserted.contains(Integer.valueOf(element))){
				alreadyInserted.add(Integer.valueOf(element));
				figureTurn[index] = element;
				index++;
			}
		}
	}
	
	private long time;
	private long lastSecondsRemaining = 0L;
	
	@Override
	public void run(){
		this.randomizeFigureTurn();
		while(running){
			
			boolean skipSwitch = false;
			
			if(reset){
				this.reinit();
			}else{
				if(this.state == GameState.MOVE || this.state == GameState.ORIENTATE){
					long newTime = SystemClock.elapsedRealtime();
					if(this.timeScoreInfo.reduceStepTime(newTime-time)){
						this.nextState(true);
						skipSwitch = true;
						this.board.moveFigure(this.getCurrentFigure(), this.getCurrentFigure().getX(), this.getCurrentFigure().getY());
						this.board.orientateFigure(this.getCurrentFigure(), this.getCurrentFigure().getOrientation());
					}
					final long secondsRemaining = timeScoreInfo.getStepTime()/1000;
					if(secondsRemaining!=lastSecondsRemaining){
						if(secondsRemaining==0L){
			    			 Sounds.playSound(R.raw.timer);
			    		 }else if(secondsRemaining<=3L){
			    			 Sounds.playSound(R.raw.timer_2);
			    		 }
			    		 lastSecondsRemaining = secondsRemaining;
					}
					time = newTime;
				}
				
				if(!skipSwitch){
				
					switch(this.state){
					case INIT:
						this.initFigures();
						final int amountEnemies = Generator.randomIntBetween(1, 3);
						this.timeScoreInfo.setInfoText("Figures created!");
						Sounds.playSound(R.raw.player_spawn);
						this.sleepFor(PAUSE_TIME);
						this.spawnEnemies(amountEnemies);
						this.next = true;
						this.timeScoreInfo.setInfoText("Enemies spawned");
						this.sleepFor(PAUSE_TIME);
						this.timeScoreInfo.setInfoText("- ROUND "+ ++round + " -" );
						time = SystemClock.elapsedRealtime();
						break;
					case MOVE:
						if(!showedMoveTarget){
							this.board.showMoveTarget(this.getCurrentFigure());
							this.timeScoreInfo.setInfoText("Turn of Figure " + (this.figureTurn[this.figureStep]+1));
							showedMoveTarget = true;
						}
						break;
					case ORIENTATE:
						if(!showedOrientation){
							this.board.showOrientation(this.getCurrentFigure());
							showedOrientation = true;
						}
						break;
					case ANALYSIS:
						this.analyseRound();
						this.next = true;
						// TODO show info of analysis result
						this.timeScoreInfo.setInfoText("Analysed board!");
						this.sleepFor(PAUSE_TIME);
						break;
					case SPAWN:
						this.spawnEnemies(Generator.randomIntBetween(0, 2));
						// TODO show info of spawned enemies
						this.sleepFor(PAUSE_TIME/2);
						this.timeScoreInfo.setInfoText("Enemies spawned");
						this.sleepFor(PAUSE_TIME/2);
						this.next = true;
						break;
					case END:
						// TODO show end monitor
						this.running = false;
					}
					
				}
			}
			sleepFor(SLEEP_TIME);
			
			if(!skipSwitch && next){
				this.nextState(false);
				this.next = false;
			}
		}
	}

	private void sleepFor(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Log.e("GameThread", e.getMessage());
		}
	}

	private void initFigures() {
		for(int i=0; i<amountFigures; i++){
			final Figure figure = new Figure();
			figure.setColor(WPColor.values()[i%3]);
			figure.setOrientation(Orientation.values()[Generator.randomIntBetween(0, 3)]);
			figures.add(figure);
			boolean cellNotFound = true;
			while(cellNotFound){
				final int x = Generator.randomIntBetween(0, boardSizeX-1);
				final int y = Generator.randomIntBetween(0, boardSizeY-1);
				if(this.board.getCell(x, y).getFigure()==null){
					this.board.moveFigure(figure, x, y);
					cellNotFound = false;
				}
			}
			Log.i("FigureINFO", "color: " + figure.getColor() + "; orientation: " + figure.getOrientation() + "; x: " + figure.getX() + "; y: " + figure.getY());
		}
	}

	private void nextState(boolean timerFinished) {
		switch(this.state){
		case INIT:
			this.state = GameState.MOVE;
			this.timeScoreInfo.setInfoText("");
			this.timeScoreInfo.setTimeShowed(true);
			break;
		case MOVE:
			if(timerFinished){
				afterOrientate();
			}else{
				this.state = GameState.ORIENTATE;
			}
			this.showedMoveTarget = false;
			break;
		case ORIENTATE:
			afterOrientate();
			this.showedOrientation = false;
			break;
		case ANALYSIS:
			this.state = GameState.SPAWN;
			this.timeScoreInfo.setInfoText("- ROUND "+ ++round + " -" );
			// TODO if game ended this.state = GameState.END
			break;
		case SPAWN:
			this.state = GameState.MOVE;
			time = SystemClock.elapsedRealtime();
			this.timeScoreInfo.setTimeShowed(true);
			break;
		case END:
			// TODO show end monitor
			this.running = false;
		}
	}

	private void afterOrientate() {
		final boolean allFiguresMoved = nextFigure();
		if(allFiguresMoved){
			this.state= GameState.ANALYSIS;
			this.timeScoreInfo.setTimeShowed(false);
		}else{
			this.state = GameState.MOVE;
		}
		final int subtractedTime = this.round/roundsAfterSpeedup;
		this.timeScoreInfo.setStepTime(stepTime - (subtractedTime*1000));
	}
	
	private Figure getCurrentFigure(){
		final int index = figureTurn[figureStep];
		return figures.get(index);
	}
	
	private boolean nextFigure(){
		final boolean allFiguresMoved;
		if(figureStep+1>=amountFigures){
			figureStep = 0;
			randomizeFigureTurn();
			allFiguresMoved = true;
		}else{
			figureStep++;
			allFiguresMoved = false;
		}
		
		return allFiguresMoved;
	}

	private void analyseRound() {
		final Cell[][] cells = this.board.getCells();
		
		for(int i=0;i<cells.length;i++){
			for(int j=0;j<cells[i].length;j++){
				final Cell cell = cells[i][j];
				if(cell.hasEnemy()){
					if(!cell.getWatchingFigures().isEmpty()){
						final Set<WPColor> colors = new HashSet<WPColor>();
						for(final Figure figure : cell.getWatchingFigures()){
							colors.add(figure.getColor());
						}
						Log.i("ANALYZE", "checkPotentialKill - x:" + i +";y:"+j);
						this.checkPotentialKill(cells, i, j, colors);
					}
				}
			}
		}
		
		boolean darkerSound = false;
		boolean blackoutSound = false;
		for(int i=0;i<cells.length;i++){
			for(int j=0;j<cells[i].length;j++){
				final Cell cell = cells[i][j];
				if(cell.hasEnemy() && cell.getEnemy().isAlive()){
					cell.getEnemy().increaseAge();
					if(cell.getEnemy().isAlive()){
						darkerSound = true;
					}else{
						blackoutSound = true;
					}
				}
			}
		}
		
		if(blackoutSound){
			Sounds.playSound(R.raw.fail);
		}else if(darkerSound){
			Sounds.playSound(R.raw.counter_fade);
		}
	}

	private void checkPotentialKill(Cell[][] cells, int x, int y,
			Set<WPColor> colors) {
		for(final WPColor color : colors){
			Log.i("ANALYZE", x+"/"+y+" Color: " + color);
		}
		boolean playSound = false;
		for(int i=x-1;i<=x+1;i++){
			for(int j=y-1;j<=y+1;j++){
				if(this.lookForFigure(cells, i, j)){
					Log.i("ANALYZE", "Figur - x:" + i + ", y: " + j);
					final Figure figure = cells[i][j].getFigure();
					if(colors.contains(figure.getColor().getContrary())){
						Log.i("ANALYZE", "Remove enemy x:" + i + ", y:" + j);
						this.board.removeEnemy(i,j);
						this.timeScoreInfo.addScore();
						playSound = true;
					}
				}
			}
		}
		if(playSound)
			Sounds.playSound(R.raw.destroy);
	}
	

	private boolean lookForFigure(Cell[][] cells, int x, int y) {
		final boolean result;
		if(x<0 || x>=boardSizeX || y<0 || y>=boardSizeY){
			result = false;
		}else{
			result = (cells[x][y].getFigure() != null);
		}
		return result;
	}

	private void spawnEnemies(int amountEnemies) {
		boolean soundForSpawn = false;
		for(int i=0; i<amountEnemies;i++){
			boolean cellNotFound = true;
			while(cellNotFound){
				final int x = Generator.randomIntBetween(0, boardSizeX-1);
				final int y = Generator.randomIntBetween(0, boardSizeY-1);
				final Cell cell = this.board.getCell(x, y);
				if(cell.getFigure()== null && cell.getEnemy()==null){
					this.board.spawnEnemy(x, y, enemyLife);
					cellNotFound = false;
				}
			}
			soundForSpawn = true;
		}
		if(soundForSpawn){
			Sounds.playSound(R.raw.spawn);
		}
	}

	@Override
	public Board getBoard() {
		return this.board;
	}

	@Override
	public TimeScoreInfo getTimeScoreInfo() {
		return this.timeScoreInfo;
	}

	@Override
	public void dispatchEvent(CellClicked event) {
		switch(this.state){
		case MOVE:
			if(this.board.getCell(event.getX(), event.getY()).isWalkable()){
				this.board.moveFigure(this.getCurrentFigure(), event.getX(), event.getY());
				Sounds.playSound(R.raw.movement_5);
				this.nextState(false);
			}
			break;
		case ORIENTATE:
			if(this.board.getCell(event.getX(), event.getY()).isVisible()){
				final Orientation orientation;
				boolean soundOrientate = true;
				if(event.getX()<this.getCurrentFigure().getX()){
					orientation = Orientation.LEFT;
				}else if(event.getX()>this.getCurrentFigure().getX()){
					orientation = Orientation.RIGHT;
				}else if(event.getY()<this.getCurrentFigure().getY()){
					orientation = Orientation.BOTTOM;
				}else if(event.getY()>this.getCurrentFigure().getY()){
					orientation = Orientation.TOP;
				}else{
					orientation = this.getCurrentFigure().getOrientation();
					soundOrientate = false;
				}
				this.board.orientateFigure(this.getCurrentFigure(), orientation);
				if(soundOrientate){
					Sounds.playSound(R.raw.orientation_3);
				}
				this.nextState(false);
			}
			break;
			default:
				// not allowed
		}
	}

	public void reset()
	{
		reset = true;
	}
}
