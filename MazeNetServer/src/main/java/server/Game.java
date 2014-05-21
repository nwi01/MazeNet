package server;

import generated.ErrorType;
import generated.MoveMessageType;
import generated.TreasureType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import networking.Connection;
import server.userInterface.UI;
import tools.Debug;
import tools.DebugLevel;
import Timeouts.TimeOutManager;
import config.Settings;

public class Game extends Thread {

	/**
	 * beinhaltet die Spieler, die mit dem Server verbunden sind und die durch
	 * die ID zugreifbar sind
	 */
	private HashMap<Integer, Player> spieler;
	private ServerSocket s;
	private TimeOutManager timeOutMan;
	private Board spielBrett;
	private Integer winner = -1;// Default wert -1 solange kein Gewinner
								// feststeht
	private UI userinterface;
	private int playerCount;

	public Game() {
		Debug.addDebugger(System.out, DebugLevel.DEFAULT);
		winner = -1;
		spieler = new HashMap<Integer, Player>();
		timeOutMan = new TimeOutManager();
		try {
			s = new ServerSocket(config.Settings.port);
		} catch (IOException e) {
			System.err
					.println("Port bereits belegt, bitte schließen Sie alle Serverinstanzen");
		}
	}

	/**
	 * Auf TCP Verbindungen warten und den Spielern die Verbindung ermoeglichen
	 */
	public void init(int players) {
		try {
			int i = 1;
			boolean accepting = true;
			timeOutMan.startLoginTimeOut(this);

			while (accepting && i <= players) {
				try {
					// TODO Was wenn ein Spieler beim Login rausfliegt
					Debug.print("Waiting for Player (" + i + "/" + players
							+ ")", DebugLevel.DEFAULT);
					Socket mazeClient = s.accept();
					Connection c = new Connection(mazeClient, this, i);
					spieler.put(i, c.login(i));
				} catch (SocketException e) {
					Debug.print("...Waiting for Player timed out!",
							DebugLevel.DEFAULT);
				}
				++i;
			}
			// Warten bis die Initialisierung durchgelaufen ist
			boolean spielbereit = false;
			while (!spielbereit) {
				spielbereit = true;
				for (Integer id : spieler.keySet()) {
					Player p = spieler.get(id);
					if (!p.isInitialized()) {
						spielbereit = false;
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			timeOutMan.stopLoginTimeOut();
			// Spielbrett generieren
			spielBrett = new Board();

			// Verteilen der Schatzkarten
			ArrayList<TreasureType> tcp = new ArrayList<TreasureType>();
			tcp.add(TreasureType.SYM_01);
			tcp.add(TreasureType.SYM_02);
			tcp.add(TreasureType.SYM_03);
			tcp.add(TreasureType.SYM_04);
			tcp.add(TreasureType.SYM_05);
			tcp.add(TreasureType.SYM_06);
			tcp.add(TreasureType.SYM_07);
			tcp.add(TreasureType.SYM_08);
			tcp.add(TreasureType.SYM_09);
			tcp.add(TreasureType.SYM_10);
			tcp.add(TreasureType.SYM_11);
			tcp.add(TreasureType.SYM_12);
			tcp.add(TreasureType.SYM_13);
			tcp.add(TreasureType.SYM_14);
			tcp.add(TreasureType.SYM_15);
			tcp.add(TreasureType.SYM_16);
			tcp.add(TreasureType.SYM_17);
			tcp.add(TreasureType.SYM_18);
			tcp.add(TreasureType.SYM_19);
			tcp.add(TreasureType.SYM_20);
			tcp.add(TreasureType.SYM_21);
			tcp.add(TreasureType.SYM_22);
			tcp.add(TreasureType.SYM_23);
			tcp.add(TreasureType.SYM_24);
			Collections.shuffle(tcp);
			int anzCards = tcp.size() / spieler.size();
			i = 0;
			for (Integer player : spieler.keySet()) {
				ArrayList<TreasureType> cardsPerPlayer = new ArrayList<TreasureType>();
				for (int j = i * anzCards; j < (i + 1) * anzCards; j++) {
					cardsPerPlayer.add(tcp.get(j));
				}
				spieler.get(player).setTreasure(cardsPerPlayer);
				++i;
			}
		} catch (IOException e) {
			System.err.println("Fehler beim Verbindungsaufbau (game.init()):");
			System.err.println(e.getMessage());
		}

	}

	public void closeServerSocket() {
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<Player> playerToList() {
		List<Player> erg = new ArrayList<Player>();
		for (Integer id : spieler.keySet()) {
			erg.add(spieler.get(id));
		}
		return erg;
	}

	public void singleTurn(Integer currPlayer) {
		/**
		 * Connection.awaitMove checken ->Bei Fehler illegalMove->liefert neuen
		 * Zug
		 */
		userinterface.updatePlayerStatistics(playerToList(), currPlayer);

		TreasureType t = spieler.get(currPlayer).getCurrentTreasure();
		spielBrett.setTreasure(t);

		Debug.print("Spielbrett vor Zug von Spieler " + currPlayer,
				DebugLevel.VERBOSE);
		Debug.print(spielBrett.toString(), DebugLevel.VERBOSE);

		MoveMessageType move = spieler.get(currPlayer).getConToClient()
				.awaitMove(spieler, this.spielBrett, 0);
		if (move != null) {
			if (spielBrett.proceedTurn(move, currPlayer)) {
				// foundTreasure gibt zurueck wieviele
				// Schaetze noch zu finden sind
				if (spieler.get(currPlayer).foundTreasure() == 0) {
					winner = currPlayer;
				}
			}
			userinterface.displayMove(move, spielBrett, Settings.MOVEDELAY);

		} else {
			Debug.print("Keinen Move erhalten!", DebugLevel.DEFAULT);
		}
	}

	public Board getBoard() {
		return spielBrett;
	}

	/**
	 * Aufraeumen nach einem Spiel
	 */
	public void cleanUp() {
		if (winner > 0) {
			for (Integer playerID : spieler.keySet()) {
				Player s = spieler.get(playerID);
				s.getConToClient().sendWin(winner,
						spieler.get(winner).getName(), spielBrett);
			}
			userinterface.updatePlayerStatistics(playerToList(), winner);
			Debug.print(spieler.get(winner).getName() + "(" + winner
					+ ") hat das Spiel gewonnen.", DebugLevel.DEFAULT);
			JOptionPane.showMessageDialog(null, spieler.get(winner).getName()
					+ "(" + winner + ") hat das Spiel gewonnen.");
		} else {
			// Iterator<Integer> playerID = spieler.keySet().iterator();
			// while (playerID.hasNext() ) {
			// Player s = spieler.get(playerID.next());
			// s.getConToClient().disconnect(ErrorType.NOERROR);
			// }

			while (spieler.size() > 0) {
				Player s = spieler.get(spieler.keySet().iterator().next());
				s.getConToClient().disconnect(ErrorType.NOERROR);

			}
		}
		closeServerSocket();
	}

	public boolean somebodyWon() {
		return winner != -1;

	}

	public static void main(String[] args) {
		Game currentGame = new Game();
		currentGame.parsArgs(args);
		currentGame.userinterface = Settings.USERINTERFACE;
		currentGame.run();
	}

	public void setUserinterface(UI userinterface) {
		this.userinterface = userinterface;
	}

	public void parsArgs(String args[]) {
		playerCount = Settings.DEFAULT_PLAYERS;
		for (String arg : args) {
			String playerFlag = "-n";
			if (arg.startsWith(playerFlag)) {
				playerCount = Integer
						.valueOf(arg.substring(playerFlag.length()));
			}
		}

	}

	public void run() {

		init(playerCount);

		userinterface.init(spielBrett);
		Integer currPlayer = 1;
		userinterface.updatePlayerStatistics(playerToList(), currPlayer);

		while (!somebodyWon()) {
			Debug.print("Aktueller Spieler: " + currPlayer, DebugLevel.VERBOSE);
			singleTurn(currPlayer);
			currPlayer = nextPlayer(currPlayer);
		}
		cleanUp();
	}

	private Integer nextPlayer(Integer currPlayer) {
		Iterator<Integer> iDIterator = spieler.keySet().iterator();
		Integer id = -1;
		while (iDIterator.hasNext()) {
			id = iDIterator.next();
			if (id == currPlayer) {
				break;
			}
		}
		if (iDIterator.hasNext()) {
			return iDIterator.next();
		} else {
			// Erste ID zurueckgeben,
			return spieler.keySet().iterator().next();
		}
	}

	public void removePlayer(int id) {
		this.spieler.remove(id);
		Debug.print(
				"[INFO]: Spieler mit ID " + id + " hat das Spiel verlassen",
				DebugLevel.DEFAULT);
	}

	public void stopGame() {
		winner = -2;
	}
}
