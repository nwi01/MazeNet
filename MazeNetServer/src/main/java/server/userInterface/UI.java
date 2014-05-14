package server.userInterface;

import generated.MoveMessageType;

import java.util.List;

import server.Board;
import server.Player;

public interface UI {
	public void displayMove(MoveMessageType mm, Board b,long millis);
	public void updatePlayerStatistics(List<Player> stats,Integer current);
	public void init(Board b);
}
