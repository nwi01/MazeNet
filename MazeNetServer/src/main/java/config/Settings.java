package config;

import server.userInterface.BetterUI;
import server.userInterface.GraphicalUI;
import server.userInterface.UI;

@SuppressWarnings("unused")
public class Settings {
	// TODO falls alles nur Static => Konstruktor private machen
	public final static int port = 5123;
	public static int DEFAULT_PLAYERS = 1;
	public final static int MOVEDELAY = (int) (2 * 1000); // 0,5 sec

	public final static long LOGINTIMEOUT = 1 * 60 * 1000;// 1min
	public final static int LOGINTRIES = 3;// maximal 3 Loginversuche
	public static final int MOVETRIES = 3; // maximale Versuche einen gueltigen
											// zug zu machen
	public static final long SENDTIMEOUT = 1 * 60 * 1000;// 1min

	public static final boolean TESTBOARD = false; // Das Testbrett ist immer
													// gleich
	public static final long TESTBOARD_SEED = 0; // Hiermit lassen sich die
													// Testfälle Anpassen
													// (Pseudozufallszahlen)
	public static UI USERINTERFACE = new BetterUI();
	// public static UI USERINTERFACE = new GraphicalUI(); // Hierüber kann das
	// Usserinterface
	// bestimmt werden
}
