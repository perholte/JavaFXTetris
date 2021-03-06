package logic;

import java.util.ArrayList; 
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import coordinates.Coordinates;
import shape.Shape;

public class Board {

	private int posX, posY, startPosX, startPosY;
	private Shape currentShape;
	private Shape nextShape;
	private int score;
	private boolean gameOver = false;

	//hver block blir representert ved fargen sin som en string
	private List<List<String>> board = new ArrayList<>();

	public Board(int columnLength, int rowLength) {
		if (columnLength < 1 || rowLength < 1) {
			throw new IllegalArgumentException("columnLength and rowLength should be larger than 0");
		}

		startPosY = 0;
		startPosX = columnLength / 2;
		posX = startPosX;
		posY = startPosY;
		nextShape = NextShapeGenerator.getNextShape(columnLength);

		while (board.size() < rowLength) {
			List<String> list = new ArrayList<>();
			while (list.size() < columnLength) {
				list.add(null);
			}
			board.add(list);
		}
	}

	public boolean moveRight() {
		return moveX(1, false);
	}

	public boolean moveLeft() {
		return moveX(-1, false);
	}

	private boolean moveX(int distance, boolean isOverlapping) {
		if (!isOverlapping) {
			removeTrace();
		}

		boolean isSpaceAvailable = checkSpaceX(posX + distance);
		if (isSpaceAvailable) {
			posX += distance;
		}

		placeBlock();
		return isSpaceAvailable;
	}

	public void moveDown() {
		moveDown(1, false);
	}

	private void moveDown(int distance, boolean isOverlapping) {		
		if (!isOverlapping) {
			removeTrace();
		}

		if (!checkSpaceY(posY + distance)) {
			placeBlock();
			insertNewBlock();
		} else {
			posY += distance;
			score += distance;
		}
		placeBlock();
	}

	public void hardDrop() {
		removeTrace();
		int i = posY;
		while (i < getRowLength()) {
			i++;
			if (!checkSpaceY(i)) {
				i--;
				break;
			}
		}
		score += (i - posY) * 10;

		posY = i;
		placeBlock();
		insertNewBlock();
	}

	private Boolean checkSpaceY(int y) {
		try {
			boolean spaceBelow = Coordinates.getCoorinatesForShape(currentShape, posX, y, getColumnLength()).stream()
				.map(coo -> getTile(coo.getX(), coo.getY()) == null || coo.getY() == getRowLength())
				.reduce((a,b) -> a && b)
				.get();     
			if (spaceBelow == false) throw new IllegalStateException();
		} catch (IndexOutOfBoundsException | IllegalStateException | IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	private Boolean checkSpaceX(int x) {
		try {
			Collection<Coordinates> allCoordinates = Coordinates.getCoorinatesForShape(currentShape, x, posY, getColumnLength());
		
			boolean isSpaceAvailable = allCoordinates.stream()
				.map(coo -> getTile(coo.getX(), coo.getY()) == null)
				.reduce((a,b) -> a && b)
				.get();
		
			Collection<Integer> xValues = allCoordinates.stream().map(Coordinates::getX).collect(Collectors.toList());
			boolean presentInBothBorders = xValues.contains(0) && xValues.contains(getColumnLength() - 1);
		
			if (isSpaceAvailable == false || presentInBothBorders || x <= -1 || x >= getColumnLength()) {
				throw new IllegalStateException();
			}
		} catch (IndexOutOfBoundsException | IllegalStateException | IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	private void removeTrace() {
		updateColor(false);
	}

	private void placeBlock() {
		updateColor(true);
	}

	private void updateColor(boolean color) {
		Coordinates.getCoorinatesForShape(currentShape, posX, posY, getColumnLength()).stream()
			.filter(a -> a.getX() >= 0 && a.getX() < getColumnLength() && a.getY() >= 0 && a.getY() < getRowLength())
			.forEach(coo -> board.get(coo.getY())
				.set(coo.getX(),
					color ? currentShape.color : null ));
	}

	public Shape getNextShape() {
		return nextShape;
	}

	public void insertNewBlock() {
		checkForClearedLines();
		posX = startPosX;
		posY = startPosY;
		currentShape = nextShape;
		nextShape = NextShapeGenerator.getNextShape(getColumnLength());

		if (!Coordinates.getCoorinatesForShape(currentShape, posX, posY, getColumnLength()).stream()
				.map(coo -> getTile(coo.getX(), coo.getY()) == null || coo.getY() == getColumnLength())
				.reduce((a,b) -> a && b)
				.get()) {
			gameOver = true;
		};

		placeBlock();
	}

	private void checkForClearedLines() {
		int numberOfClearedLines = 0;
		for (int i = 0; i < getRowLength(); i++) {
			List<String> row = board.get(i);
			if (row.stream().map(color -> color != null).reduce((b,c) -> b && c).get())  {
				board.remove(i);
				
				List<String> list = new ArrayList<>();
				while (list.size() < getColumnLength()) {
					list.add(null);
				}
				board.add(0, list);
				
				numberOfClearedLines++;
				score += numberOfClearedLines * 100;
			}
		}
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int getColumnLength() {
		return board.get(0).size();
	}

	public int getRowLength() {
		return board.size();
	}

	public String getTile(int posX, int posY) {
		if (posX < 0 || posX >= getColumnLength() || posY < 0 || posY >= getRowLength()) {
			throw new IllegalArgumentException("posX and posY must be inside the grid");
		}
		return board.get(posY).get(posX);
	}

	public List<List<String>> getBoard() {
		return board;
	}

	public int getScore() {
		return score;
	}

	public boolean rotateShape() {
		removeTrace();
		try { 
			currentShape.rotateRight();
			if(!checkSpaceX(posX) || !checkSpaceY(posY)) {
				throw new IllegalStateException();
			} else {
				placeBlock();
			}
		} catch (IndexOutOfBoundsException | IllegalStateException e) {
			if (checkSpaceX(posX + 1) && posX < getColumnLength() - 1) {
				moveX(1, true);
			} else if (checkSpaceX(posX - 1) && posX >= 1) {
				moveX(-1, true);
			} else if(checkSpaceY(posY + 1) && checkSpaceX(posX)) {
				moveDown(1, true);
			} else if (checkSpaceX(posX + 2) && posX < getColumnLength() - 2) {
				moveX(2, true);
			} else if (checkSpaceX(posX - 2) && posX >= 2) {
				moveX(-2, true);
			} else if(checkSpaceY(posY + 2) && checkSpaceX(posX)) {
				moveDown(2, true);
			} else {
				currentShape.rotateLeft();
				placeBlock();
				return false;
			}
		}
		return true;
	}
}
