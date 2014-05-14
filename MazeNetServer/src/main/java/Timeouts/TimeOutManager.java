package Timeouts;

import java.util.HashMap;
import java.util.Timer;

import networking.Connection;
import server.Game;
import config.Settings;

public class TimeOutManager extends Timer {

	private LoginTimeOut lto;
	private HashMap<Integer, SendMessageTimeout> smt;

	public TimeOutManager() {
		super("TimeOuts", true);
		this.smt = new HashMap<Integer, SendMessageTimeout>();
	}

	public void startLoginTimeOut(Game currentGame) {
		lto = new LoginTimeOut(currentGame);
		this.schedule(lto, Settings.LOGINTIMEOUT);
	}

	public void stopLoginTimeOut() {
		lto.cancel();
	}

	public void startSendMessageTimeOut(int playerId, Connection c) {
		smt.put(playerId, new SendMessageTimeout(c));
		this.schedule(smt.get(playerId), Settings.SENDTIMEOUT);
	}

	public void stopSendMessageTimeOut(int playerId) {
		if (smt.containsKey(playerId))
			smt.get(playerId).cancel();
	}
}
