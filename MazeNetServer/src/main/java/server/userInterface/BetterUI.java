package server.userInterface;
 
import generated.BoardType.Row;
import generated.CardType;
import generated.MoveMessageType;
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
 
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.Timer;
 
import server.Board;
import server.Card;
import server.Player;
import server.Position;
 
@SuppressWarnings("serial")
public class BetterUI extends JFrame implements UI {
 
	private static class ImageRessources {
		private static HashMap<String, Image> images = new HashMap<String, Image>();
 
		public static Image getImage(String name) {
			if (images.containsKey(name)) {
				return images.get(name);
			} else {
				URL u = ImageRessources.class
						.getResource("/server/userInterface/resources/" + name
								+ ".png");
				Image img = null;
				try {
					img = ImageIO.read(u);
					images.put(name, img);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return img;
			}
		}
	}
 
	private class UIBoard extends JPanel {
		Board b;
		Image images[][] = new Image[7][7];
		Card c[][] = new Card[7][7];
 
		public void setBoard(Board b) {
			if (b == null) {
				this.b = null;
				return;
			}
			this.b = (Board) b.clone();
			int y = 0, x = 0;
			for (Row r : b.getRow()) {
				x = 0;
				for (CardType ct : r.getCol()) {
					Card card = new Card(ct);
					c[y][x] = card;
					images[y][x] = ImageRessources.getImage(card.getShape()
							.toString() + card.getOrientation().value());
					if (c[y][x].getTreasure() != null) {
						ImageRessources.getImage(c[y][x].getTreasure().value());
					}
					x++;
				}
				y++;
			}
		}
 
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (b == null)
				return;
			int width = this.getWidth();
			int height = this.getHeight();
			width = height = Math.min(width, height);
			width = height -= width % 7;
			int pixelsPerField = width / 7;
 
			for (int y = 0; y < 7; y++) {
				for (int x = 0; x < 7; x++) {
					int topLeftY = pixelsPerField * y;
					int topLeftX = pixelsPerField * x;
					if (animProps != null) {
						if (animProps.vertikal
								&& x == animProps.shiftPosition.getCol()) {
							topLeftY += animProps.direction
									* (pixelsPerField * animationState / animationFrames);
						} else if (!animProps.vertikal
								&& y == animProps.shiftPosition.getRow()) {
							topLeftX += animProps.direction
									* (pixelsPerField * animationState / animationFrames);
						}
					}
 
					g.drawImage(images[y][x], topLeftX, topLeftY,
							pixelsPerField, pixelsPerField, null);
					if (c[y][x] != null) {
 
						if (c[y][x].getTreasure() != null) {
							g.drawImage(ImageRessources.getImage(c[y][x]
									.getTreasure().value()), topLeftX
									+ pixelsPerField / 4, topLeftY
									+ pixelsPerField / 4, pixelsPerField / 2,
									pixelsPerField / 2, null);
						}
						//Zeichnen der SpielerPins
						for (Integer playerID : c[y][x].getPin().getPlayerID()) {
							g.setColor(colorForPlayer(playerID));
							g.fillOval(
									topLeftX + pixelsPerField / 4
											+ pixelsPerField / 4
											* ((playerID - 1) / 2), topLeftY
											+ pixelsPerField / 4
											+ pixelsPerField / 4
											* ((playerID - 1) % 2),
									pixelsPerField / 4, pixelsPerField / 4);
 
							g.setColor(Color.WHITE);
							g.drawOval(
									topLeftX + pixelsPerField / 4
											+ pixelsPerField / 4
											* ((playerID - 1) / 2), topLeftY
											+ pixelsPerField / 4
											+ pixelsPerField / 4
											* ((playerID - 1) % 2),
									pixelsPerField / 4, pixelsPerField / 4);
							centerStringInRect((Graphics2D) g, "" + playerID,
									topLeftX + pixelsPerField / 4
											+ pixelsPerField / 4
											* ((playerID - 1) / 2), topLeftY
											+ pixelsPerField / 4
											+ pixelsPerField / 4
											* ((playerID - 1) % 2),
									pixelsPerField / 4, pixelsPerField / 4);
						}
					} else {
						System.out.println("Card at " + x + " " + y
								+ " is null");
					}
				}
			}
			// Zeichnen der eingeschobenen karte in der animation
			if (animProps != null) {
				int topLeftY = pixelsPerField
						* (animProps.shiftPosition.getRow() - (animProps.vertikal ? animProps.direction
								: 0));
				int topLeftX = pixelsPerField
						* (animProps.shiftPosition.getCol() - (!animProps.vertikal ? animProps.direction
								: 0));
				if (animProps.vertikal) {
					topLeftY += animProps.direction
							* (pixelsPerField * animationState / animationFrames);
				} else {
					topLeftX += animProps.direction
							* (pixelsPerField * animationState / animationFrames);
				}
				Card card = new Card(b.getShiftCard());
				g.drawImage(
						ImageRessources.getImage(card.getShape().toString()
								+ card.getOrientation().value()), topLeftX,
						topLeftY, pixelsPerField, pixelsPerField, null);
				if (card.getTreasure() != null) {
					g.drawImage(ImageRessources.getImage(card.getTreasure()
							.value()), topLeftX + pixelsPerField / 4, topLeftY
							+ pixelsPerField / 4, pixelsPerField / 2,
							pixelsPerField / 2, null);
				}
				g.setColor(Color.YELLOW);
				g.drawRect(topLeftX, topLeftY, pixelsPerField, pixelsPerField);
			}
		}
 
		private void centerStringInRect(Graphics2D g2d, String s, int x, int y,
				int height, int width) {
			Rectangle size = g2d.getFontMetrics().getStringBounds(s, g2d)
					.getBounds();
			float startX = (float) (width / 2 - size.getWidth() / 2);
			float startY = (float) (height / 2 - size.getHeight() / 2);
			g2d.drawString(s, startX + x - size.x, startY + y - size.y);
		}
 
	}
 
	int currentPlayer;
 
	private class StatsPanel extends JPanel {
		boolean initiated = false;
		TreeMap<Integer, JLabel> statLabels = new TreeMap<Integer, JLabel>();
		TreeMap<Integer, JLabel> currentPlayerLabels = new TreeMap<Integer, JLabel>();
		TreeMap<Integer, JLabel> treasureImages = new TreeMap<Integer, JLabel>();
 
		public void update(List<Player> stats, int current) {
			if (initiated) {
				currentPlayerLabels.get(currentPlayer).setText("");
				currentPlayer = current;
				currentPlayerLabels.get(currentPlayer).setText(">");
				for (Player p : stats) {
					statLabels.get(p.getID()).setText("" + p.treasuresToGo());
					treasureImages.get(p.getID()).setIcon(
							new ImageIcon(ImageRessources.getImage(p
									.getCurrentTreasure().value())));
				}
 
			} else {
				//Beim ersten mal erzeugen wir die GUI.
				initiated = true;
				GridBagConstraints gc = new GridBagConstraints();
				gc.gridx = GridBagConstraints.RELATIVE;
				gc.anchor = GridBagConstraints.WEST;
				this.setLayout(new GridBagLayout());
				for (Player p : stats) {
					gc.gridy = p.getID();
					JLabel currentPlayerLabel = new JLabel();
					currentPlayerLabels.put(p.getID(), currentPlayerLabel);
 
					JLabel playerIDLabel = new JLabel("" + p.getID());
					JLabel playerNameLabel = new JLabel(p.getName());
					playerNameLabel.setForeground(colorForPlayer(p.getID()));
 
					JLabel statLabel = new JLabel("" + p.treasuresToGo());
					statLabels.put(p.getID(), statLabel);
 
					JLabel treasureImage = new JLabel(new ImageIcon(
							ImageRessources.getImage(p.getCurrentTreasure()
									.value())));
					treasureImages.put(p.getID(), treasureImage);
 
					gc.ipadx = 5;
					this.add(currentPlayerLabel, gc);
					gc.ipadx = 0;
					this.add(playerIDLabel, gc);
					this.add(playerNameLabel, gc);
					this.add(treasureImage, gc);
					this.add(statLabel, gc);
				}
				currentPlayer = current;
				currentPlayerLabels.get(currentPlayer).setText(">");
			}
		}
	}
 
	UIBoard uiboard = new UIBoard();
	StatsPanel statPanel = new StatsPanel();
 
	public BetterUI() {
		super("Better MazeNet UI");
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				uiboard, statPanel);
		this.add(splitPane, BorderLayout.CENTER);
		this.pack();
		this.setSize(800, 700);

		splitPane.setDividerLocation(0.8);
 
	}
 
	private class AnimationProperties {
		public final boolean vertikal;
		public final Position shiftPosition;
		public final int direction;
 
		public AnimationProperties(Position shiftPosition) {
			this.shiftPosition = shiftPosition;
			if (shiftPosition.getCol() == 6 || shiftPosition.getCol() == 0) {
				vertikal = false;
				direction = shiftPosition.getCol() == 0 ? 1 : -1;
			} else if (shiftPosition.getRow() == 6
					|| shiftPosition.getRow() == 0) {
				vertikal = true;
				direction = shiftPosition.getRow() == 0 ? 1 : -1;
			} else {
				throw new IllegalArgumentException("Can not shift like that");
			}
 
		}
	}
 
	private static final boolean animateMove = true;
	private static final boolean animateShift = true;
	private static final int animationFrames = 10;
	private int animationState = 0;
 
	private class ShiftAnimationTimerOperation implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			animationState++;
			uiboard.repaint();
			if (animationState == animationFrames) {
				animationState = 0;
				animationTimer.stop();
				animationTimer = null;
				animProps = null;
				synchronized (animationFinished) {
					animationFinished.notify();
				}
 
			}
		}
	}
 
	private static class Pathfinding {
		public static int[][] findShortestPath(Board b, Position startPos,
				Position endPos) {
			
			//Dijkstra
			boolean[][] visited = new boolean[7][7];
			int[][] weglen = new int[7][7];
			int[][] pfad = new int[7][7];
			for (int y = 0; y < 7; ++y) {
				for (int x = 0; x < 7; ++x) {
					weglen[y][x] = Integer.MAX_VALUE;
				}
			}
 
			int currentX = startPos.getCol();
			int currentY = startPos.getRow();
			weglen[currentY][currentX] = 0;
			while (true) {
				visited[currentY][currentX] = true;
				if (currentX > 0
						&& b.getCard(currentY, currentX).getOpenings().isLeft()
						&& b.getCard(currentY, currentX - 1).getOpenings()
								.isRight()) {
					if (weglen[currentY][currentX - 1] > weglen[currentY][currentX] + 1) {
						weglen[currentY][currentX - 1] = weglen[currentY][currentX] + 1;
						pfad[currentY][currentX - 1] = currentY * 7 + currentX;
					}
				}
				if (currentY > 0
						&& b.getCard(currentY, currentX).getOpenings().isTop()
						&& b.getCard(currentY - 1, currentX).getOpenings()
								.isBottom()) {
					if (weglen[currentY - 1][currentX] > weglen[currentY][currentX] + 1) {
						weglen[currentY - 1][currentX] = weglen[currentY][currentX] + 1;
						pfad[currentY - 1][currentX] = currentY * 7 + currentX;
					}
				}
 
				if (currentX < 6
						&& b.getCard(currentY, currentX).getOpenings()
								.isRight()
						&& b.getCard(currentY, currentX + 1).getOpenings()
								.isLeft()) {
					if (weglen[currentY][currentX + 1] > weglen[currentY][currentX] + 1) {
						weglen[currentY][currentX + 1] = weglen[currentY][currentX] + 1;
						pfad[currentY][currentX + 1] = currentY * 7 + currentX;
					}
				}
				if (currentY < 6
						&& b.getCard(currentY, currentX).getOpenings()
								.isBottom()
						&& b.getCard(currentY + 1, currentX).getOpenings()
								.isTop()) {
					if (weglen[currentY + 1][currentX] > weglen[currentY][currentX] + 1) {
						weglen[currentY + 1][currentX] = weglen[currentY][currentX] + 1;
						pfad[currentY + 1][currentX] = currentY * 7 + currentX;
					}
				}
 
				{
					int currentMinWegLen = Integer.MAX_VALUE;
					for (int y = 6; y >= 0; --y) {
						for (int x = 6; x >= 0; --x) {
							if (!visited[y][x]
									&& weglen[y][x] < currentMinWegLen) {
								currentMinWegLen = weglen[y][x];
								currentX = x;
								currentY = y;
							}
						}
					}
					if (currentMinWegLen == Integer.MAX_VALUE)
						break;
				}
			}
			currentX = endPos.getCol();
			currentY = endPos.getRow();
			int anzahlWegpunkte = weglen[currentY][currentX] + 1;
			//Weg ist ein Array von x und y werten
			int weg[][] = new int[anzahlWegpunkte][2];
			int i = anzahlWegpunkte - 1;
			while (i > 0) {
				weg[i--] = new int[] { currentX, currentY };
				int buf = pfad[currentY][currentX];
				currentX = buf % 7;
				currentY = buf / 7;
			}
			weg[0] = new int[] { currentX, currentY };
			return weg;
		}
	}
 
	private class MoveAnimationTimerOperation implements ActionListener {
		int[][] points;
 
		public MoveAnimationTimerOperation(Board b, Position startPos,
				Position endPos) {
			points = Pathfinding.findShortestPath(b, startPos, endPos);
			uiboard.c[endPos.getRow()][endPos.getCol()].getPin().getPlayerID()
			.remove(new Integer(currentPlayer));
			uiboard.c[startPos.getRow()][startPos.getCol()].getPin().getPlayerID()
			.add(new Integer(currentPlayer));
		}
 
		int i = 0;
 
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (i + 1 == points.length) {
				synchronized (animationFinished) {
					animationTimer.stop();
					animationTimer = null;
					animationFinished.notify();
				}
				return;
			}
			uiboard.c[points[i][1]][points[i][0]].getPin().getPlayerID()
					.remove(new Integer(currentPlayer));
			i++;
			uiboard.c[points[i][1]][points[i][0]].getPin().getPlayerID()
					.add(new Integer(currentPlayer));
			uiboard.repaint();
 
		}
	}
 
	Object animationFinished = new Object();
	Timer animationTimer;
	AnimationProperties animProps = null;
 
	@Override
	public void displayMove(MoveMessageType mm, Board b,long millis) {
		if (animateShift) {
			uiboard.b.setShiftCard(mm.getShiftCard());
			animationTimer = new Timer(150, new ShiftAnimationTimerOperation());
			animProps = new AnimationProperties(new Position(
					mm.getShiftPosition()));
			synchronized (animationFinished) {
				animationTimer.start();
				try {
					animationFinished.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Position oldPlayerPos = new Position(
				uiboard.b.findPlayer(currentPlayer));
		uiboard.setBoard(b);
		if (animateMove) {
			//Falls unser Spieler sich selbst verschoben hat.
			AnimationProperties props = new AnimationProperties(new Position(
					mm.getShiftPosition()));
			if (props.vertikal) {
				if (oldPlayerPos.getCol() == props.shiftPosition.getCol()) {
					oldPlayerPos
							.setRow((7 + oldPlayerPos.getRow() + props.direction) % 7);
				}
			} else {
				if (oldPlayerPos.getRow() == props.shiftPosition.getRow()) {
					oldPlayerPos
							.setCol((7 + oldPlayerPos.getCol() + props.direction) % 7);
				}
			}
			animationTimer = new Timer(250, new MoveAnimationTimerOperation(
					uiboard.b, oldPlayerPos, new Position(mm.getNewPinPos())));
			synchronized (animationFinished) {
				animationTimer.start();
				try {
					animationFinished.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			uiboard.repaint();
		}
	}
 
	@Override
	public void updatePlayerStatistics(List<Player> stats, Integer current) {
		statPanel.update(stats, current);
	}
 
	@Override
	public void init(Board b) {
		uiboard.setBoard(b);
		uiboard.repaint();
		this.setVisible(true);
	}
 
	private static Color colorForPlayer(int playerID) {
		switch (playerID) {
		case 0:
			return Color.yellow;
		case 1:
			return Color.GREEN;
		case 2:
			return Color.BLACK;
		case 3:
			return Color.RED;
		case 4:
			return Color.BLUE;
		default:
			throw new IllegalArgumentException(
					"UI is not prepared for this playerId");
		}
	}
 
}