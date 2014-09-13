import model.*;

import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
	private static final double STRIKE_ANGLE = 1.0D * PI / 180.0D;
	private Hockeyist isForward = null;
	private Hockeyist isMiddle = null;
	private Hockeyist isGuard = null;
	private Player opponentPlayer = null;

	static int isDebugFull = 100;
	static boolean isDebugMove = true;
	double opponentGateX=0, opponentGateY=0;
	
	@Override
	public void move(Hockeyist self, World world, Game game, Move move) {
		if (!(self.getRemainingCooldownTicks() == 0)
				|| !(self.getRemainingKnockdownTicks() == 0))
			return;

		opponentPlayer = world.getOpponentPlayer();
		opponentGateX = 0.5D * (opponentPlayer.getNetBack() +
		 opponentPlayer.getNetFront());
		opponentGateY = 0.5D * (opponentPlayer.getNetBottom() +
	     opponentPlayer.getNetTop());

		defineRoles(self, world);

		if (STRIKEorNotSTRIKE(self, world, game, move))
			return;
		if (TAKE_PUCKEorNotTAKE_PUCK(self, world, game, move))
			return;
		if (isGuard == self) {

		} else if (isForward == self) {

		} else if (isMiddle == self) {

		} else
			System.out.println("Paniс!!!!");


		// if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
		// if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
		//
		// double netX = 0.5D * (opponentPlayer.getNetBack() +
		// opponentPlayer.getNetFront());
		// double netY = 0.5D * (opponentPlayer.getNetBottom() +
		// opponentPlayer.getNetTop());
		// netY += (self.getY() < netY ? 0.5D : -0.5D) *
		// game.getGoalNetHeight();
		//
		// double angleToNet = self.getAngleTo(netX, netY);
		// move.setTurn(angleToNet);
		//
		// if (abs(angleToNet) < STRIKE_ANGLE) {
		// move.setAction(ActionType.SWING);
		// }
		// } else {
		// Hockeyist nearestOpponent = getNearestOpponent(self.getX(),
		// self.getY(), world);
		// if (nearestOpponent != null) {
		// if (self.getDistanceTo(nearestOpponent) > game.getStickLength()) {
		// move.setSpeedUp(1.0D);
		// } else if (abs(self.getAngleTo(nearestOpponent)) < 0.5D *
		// game.getStickSector()) {
		// move.setAction(ActionType.STRIKE);
		// }
		// move.setTurn(self.getAngleTo(nearestOpponent));
		// }
		// }
		// } else {
		// move.setSpeedUp(1.0D);
		// move.setTurn(self.getAngleTo(world.getPuck()));
		 move.setAction(ActionType.TAKE_PUCK);
		// }
	}

	/**
	 * @return Попробовать захватить шайбу
	 */
	private boolean TAKE_PUCKEorNotTAKE_PUCK(Hockeyist self, World world, Game game,
			Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()
				||world.getPuck().getOwnerHockeyistId() == self.getId()) {
			return false;// Уже у наших!
		}
		// Есть ли в зоне удара противники?
		for (Hockeyist hockeyist : world.getHockeyists()) {
			if (hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE
					|| hockeyist.getState() == HockeyistState.KNOCKED_DOWN
					|| hockeyist.getState() == HockeyistState.RESTING
					|| self.getDistanceTo(hockeyist) > game.getStickLength()
					|| abs(self.getAngleTo(hockeyist) - self.getAngle()) > 0.5 * game
							.getStickSector()

			) {
				continue;
			}
			move.setAction(ActionType.STRIKE);
			return true;
		}

		move.setSpeedUp(1.0D);
		move.setTurn(self.getAngleTo(world.getPuck()));
		move.setAction(ActionType.TAKE_PUCK);
		return true;
	}

	/**
	 * @return Бить или не бить, вот в чем вопрос.....
	 */
	private boolean STRIKEorNotSTRIKE(Hockeyist self, World world, Game game,
			Move move) {
		if (self.getState() != HockeyistState.SWINGING
				|| self.getStamina() < game.getStrikeStaminaBaseCost()) {
			//move.setAction(ActionType.SWING);
			return false;
		}
		// Есть ли в зоне удара противники?
		for (Hockeyist hockeyist : world.getHockeyists()) {
			if (hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE
					|| hockeyist.getState() == HockeyistState.KNOCKED_DOWN
					|| hockeyist.getState() == HockeyistState.RESTING
					|| self.getDistanceTo(hockeyist) > game.getStickLength()
					|| abs(self.getAngleTo(hockeyist) - self.getAngle()) > 0.5 * game
							.getStickSector()

			) {
				continue;
			}
			move.setAction(ActionType.STRIKE);
			return true;
		}

		// Hockeyist nearestOpponent = null;
		// double nearestOpponentRange = 0.0D;
		//
		// for (Hockeyist hockeyist : world.getHockeyists()) {
		// if (hockeyist.isTeammate() || hockeyist.getType() ==
		// HockeyistType.GOALIE
		// || hockeyist.getState() == HockeyistState.KNOCKED_DOWN
		// || hockeyist.getState() == HockeyistState.RESTING) {
		// continue;
		// }
		//
		// double opponentRange = hypot(x - hockeyist.getX(), y -
		// hockeyist.getY());
		//
		// if (nearestOpponent == null || opponentRange < nearestOpponentRange)
		// {
		// nearestOpponent = hockeyist;
		// nearestOpponentRange = opponentRange;
		// }
		// }
		move.setAction(ActionType.SWING);
		return true;
	}

	/**
	 * @return Определяет кто какую роль играет
	 */
	void defineRoles(Hockeyist self, World world) {
		isForward = null;
		isMiddle = null;
		isGuard = null;

		double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer
				.getNetFront());
		double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
				.getNetTop());
		Hockeyist hl[] = world.getHockeyists();

		for (Hockeyist hockeyist : world.getHockeyists()) {

			if (hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE)
				continue;
			if (null == isForward
					|| hockeyist.getDistanceTo(netX, netY) < isForward
							.getDistanceTo(netX, netY))
				isForward = hockeyist;
			if (null == isGuard
					|| hockeyist.getDistanceTo(netX, netY) > isGuard
							.getDistanceTo(netX, netY))
				isGuard = hockeyist;

		}

		for (Hockeyist hockeyist : world.getHockeyists()) {

			if (!hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE)
				continue;
			if (isForward != hockeyist && isGuard != hockeyist)
				isMiddle = hockeyist;

		}
	}

	private static Hockeyist getNearestOpponent(double x, double y, World world) {
		Hockeyist nearestOpponent = null;
		double nearestOpponentRange = 0.0D;

		for (Hockeyist hockeyist : world.getHockeyists()) {
			if (hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE
					|| hockeyist.getState() == HockeyistState.KNOCKED_DOWN
					|| hockeyist.getState() == HockeyistState.RESTING) {
				continue;
			}

			double opponentRange = hypot(x - hockeyist.getX(),
					y - hockeyist.getY());

			if (nearestOpponent == null || opponentRange < nearestOpponentRange) {
				nearestOpponent = hockeyist;
				nearestOpponentRange = opponentRange;
			}
		}

		return nearestOpponent;
	}
}
