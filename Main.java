import javax.swing.JFrame;			//Imports for stuff
import javax.swing.JOptionPane;

import java.awt.Canvas;					
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;			//Graphics libs import
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Main extends JFrame {
	public static void main(String args[]) {
		new Main();
	}
	
	public Main() {
		super("Minesweeper");
		super.setSize(300, 300);
		super.add(new MineSweeper(25, 25));
		super.setVisible(true);
	}
}

class MineSweeper extends Canvas {
	/**
	 * TODO:
	 * 		Implement buffer strategy instead of canvas to prevent flicker
	 * 		Stats pane with restart button and number of mines left
	 */
	
	ArrayList<ArrayList<Cell>> board;
	boolean flagging;
	int mines;
	
	public MineSweeper() {
		this(25, 25);
	}
	
	public MineSweeper(int width, int height) {
		board = new ArrayList<ArrayList<Cell>>();
		flagging = false;
		mines = 0;
		
		for(int x = 0; x<width; x++) {
			board.add(new ArrayList<Cell>());
			for(int y = 0; y<height; y++) {
				board.get(x).add(new Cell(x, y));
				if(board.get(x).get(y).isMine()) {
					mines++;
				}
			}
		}
		
		for(int x = 0; x<board.size(); x++) {
			for(int y = 0; y<board.get(x).size(); y++) {
				Cell c = board.get(x).get(y);
				if(y>0) {
					c.addNeighbor(board.get(x).get(y-1));
				}
				if(y>0 && x>0) {
					c.addNeighbor(board.get(x-1).get(y-1));
				}
				if(x>0) {
					c.addNeighbor(board.get(x-1).get(y));
				}
				if(y<height-1 && x>0) {
					c.addNeighbor(board.get(x-1).get(y+1));
				}
				if(y<height-1) {
					c.addNeighbor(board.get(x).get(y+1));
				}
				if(y<height-1 && x<width-1) {
					c.addNeighbor(board.get(x+1).get(y+1));
				}
				if(x<width-1) {
					c.addNeighbor(board.get(x+1).get(y));
				}
				if(y>0 && x<width-1) {
					c.addNeighbor(board.get(x+1).get(y-1));
				}
			}
		}
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int x = (int)((double)e.getX()*width/(getWidth()-(getWidth()%width)));
				int y = (int)((double)e.getY()*height/(getHeight()-(getHeight()%height)));
				
				Cell n = board.get(x).get(y);
				if(flagging) {
					n.flag();
					if(n.isMine()) {
						if(n.isFlagged()) {
							mines--;
						} else {
							mines++;
						}
					}
				} else if (!n.isFlagged()) {
					n.trigger();
					if(n.isMine()) {
						for(int ix = 0; ix<board.size(); ix++) {
							for(int iy = 0; iy<board.get(ix).size(); iy++) {
								board.get(ix).get(iy).trigger();
							}
						}
						repaint();
						JOptionPane.showConfirmDialog(null,
								"Game Over!",
								"You lost!", JOptionPane.PLAIN_MESSAGE);
					}
				}
				if (mines==0) {
					JOptionPane.showConfirmDialog(null,
							"Game Over!",
							"You won!", JOptionPane.PLAIN_MESSAGE);
				}
				repaint();
			}
		});
		super.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyChar()=='f') {
					flagging = !flagging;
					repaint();
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {}

			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
		
	}

	public void paint(Graphics window) {
		for(int x = 0; x<board.size(); x++) {
			for(int y = 0; y<board.get(x).size(); y++) {
				Cell n = board.get(x).get(y);
				n.draw(window, getWidth()/board.size(), getHeight()/board.get(y).size());
			}
		}
		if(flagging) {
			window.setColor(Color.RED);
			window.drawString("Flag mode", 0, 10);
		}
	}

	class Cell {
		int surrounding;
		boolean mine;
		boolean triggered;
		boolean flag;
		int x, y;
		ArrayList<Cell> neighbors;
		
		public Cell(int x, int y) {
			this(10, x, y);
		}
		
		public Cell(int percentChanceOfBeingMine, int x, int y) {
			this.surrounding = 0;
			this.triggered = false;
			this.x = x;
			this.y = y;
			this.mine = (Math.random()*100)<=percentChanceOfBeingMine;
			this.neighbors = new ArrayList<Cell>();
			this.flag = false;
		}
		
		public void addNeighbor(Cell neighbor) {
			this.neighbors.add(neighbor);
			if(neighbor.isMine()) {
				this.surrounding++;
			}
		}
		
		public void trigger() {
			if(!this.triggered) {
				this.triggered = true;
				if(!this.isMine()) {
					for(int x = 0; x<this.neighbors.size(); x+=2) {
						Cell n = neighbors.get(x);
						if(!n.isMine() && this.surrounding==0 && !n.isFlagged()) {
							n.trigger();
						}
					}
				}
			}
		}
		
		public void flag() {
			this.flag = !this.flag;
		}
		
		public boolean isFlagged() {
			return this.flag;
		}
		
		public boolean isMine() {
			return this.mine;
		}
		
		public void draw(Graphics window, int width, int height) {
			if(!this.triggered) {
				window.setColor(Color.GRAY);
				window.fill3DRect(this.x*width, this.y*height, width, height, true);
				if(this.flag) {
					window.setColor(Color.BLACK);
					window.drawLine((this.x*width), (this.y*height), (this.x*width)+width, (this.y*height)+height);
					window.drawLine((this.x*width)+width, (this.y*height), (this.x*width), (this.y*height)+height);
				}
			} else {
				switch(this.surrounding) {
				case 1:
					window.setColor(Color.RED);
					break;
				case 2:
					window.setColor(new Color(255, 102, 0));
					break;
				case 3:
					window.setColor(new Color(0, 100, 0));
					break;
				case 4:
					window.setColor(Color.BLUE);
					break;
				case 5:
					window.setColor(new Color(128, 0, 128));
					break;
				case 6:
					window.setColor(Color.MAGENTA);
					break;
				case 7:
					window.setColor(Color.PINK);
					break;
				case 8:
					window.setColor(Color.BLACK);
					break;
				}
				if(this.surrounding!=0 || this.isMine()) {
					String str = this.surrounding+"";
					if(this.isMine()) {
						window.setColor(Color.BLACK);
						str = "M";
					}
					FontMetrics f = window.getFontMetrics();
					int x = ((width-f.stringWidth(str))/2)+this.x*width;
					int y = ((height-f.getHeight())/2) + f.getAscent()+this.y*height;
					window.drawString(str, x, y);
				}
			}
		}
	}
}
