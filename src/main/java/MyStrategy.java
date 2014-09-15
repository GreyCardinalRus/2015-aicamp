import model.*;
import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
	private static final double STRIKE_ANGLE = 2.0D * PI / 180.0D;
	private Hockeyist isForward = null;
	private Hockeyist isMiddle = null;
	private Hockeyist isGuard = null;
	private Player opponentPlayer = null;

	final static int DIST2STRIKE = 300;
	static int isDebugFull = 100;
	static boolean isDebugMove = true;

	private double opponentGateX = 0, opponentGateY = 0,
			areaForStrikeToGateX = 0, areaForStrikeToGateY = 0;
	private Hockeyist oponentGOALIE = null;
	private Hockeyist nearestOpponent = null;
	private boolean opponentForStrike = false;
	private Hockeyist dangerOponent = null;
	private boolean puckOnOpponentSize = false;

	double guardPointX = 0, guardPointY = 0;

	@Override
	public void move(Hockeyist self, World world, Game game, Move move) {
		if (!(self.getRemainingCooldownTicks() == 0)
				|| !(self.getRemainingKnockdownTicks() == 0))
			return;

		calculateCommonVars(self, world, game, move);

		defineRoles(self, world);

		if (doItPuckOwner(self, world, game, move))
			return;

		if (STRIKEorNotSTRIKE(self, world, game, move))
			return;
		if (TAKE_PUCKEorNotTAKE_PUCK(self, world, game, move))
			return;

		if (self.getId() == isGuard.getId()) {
			if (doItGuard(self, world, game, move))
				return;
		} else if (self.getId() == isForward.getId()) {
			if (doItforward(self, world, game, move))
				return;
		} else if (self.getId() == isMiddle.getId()) {
			if (doItMiddle(self, world, game, move))
				return;
		} else {
			if (isDebugFull > 0) {
				isDebugFull--;
				System.out.println("" + world.getTick() + " Paniс!!!!"
						+ " self=" + self.getId() + " isGuard="
						+ isGuard.getId() + " isForward=" + isForward.getId());
			}
			if (doItCommon(self, world, game, move))
				return;

		}
		// move.setAction(ActionType.NONE);
		// if(abs(self.getSpeedX()+self.getSpeedY())>0) move.setSpeedUp(-1.0D);
	}

	/**
	 * @return Действия владеющего шайбой
	 */
	private boolean doItPass(Hockeyist self, World world, Game game, Move move,
			Hockeyist passTo) {

		double angleToPass = self.getAngleTo(passTo);
		boolean passTrue = true;

		for (Hockeyist hockeyist : world.getHockeyists()) {
			if (hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE
					|| hockeyist.getState() == HockeyistState.KNOCKED_DOWN
					|| hockeyist.getState() == HockeyistState.RESTING) {
				continue;
			}

			if (sin(self.getAngleTo(hockeyist)) * self.getDistanceTo(hockeyist) < hockeyist
					.getRadius())
				passTrue = false;
		}
		if (passTrue) {
			move.setSpeedUp(1.0D);
			move.setPassAngle(self.getAngleTo(passTo));
			move.setAction(ActionType.PASS);
		} else {
			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(passTo.getX(), world.getHeight()
					- passTo.getY()));
			move.setAction(ActionType.NONE);
		}
		return false;
	}

	/**
	 * @return Действия владеющего шайбой
	 */
	private boolean doItPuckOwner(Hockeyist self, World world, Game game,
			Move move) {
		if (self.getStamina() >= game.getStrikeStaminaBaseCost()
				&& opponentForStrike) {
			if (self.getState() != HockeyistState.SWINGING) {// STabs(angleToNet)
																// >
																// STRIKE_ANGLE
																// * 3) {
				move.setAction(ActionType.SWING); // return true;
			} else // if(self.getStamina() <= game.getStrikeStaminaBaseCost())
			{
				move.setAction(ActionType.STRIKE); // return true;
			}
			return true;
		}
		if (world.getPuck().getOwnerHockeyistId() != self.getId())
			return false;
		if (hypot(isForward.getX() - areaForStrikeToGateX, isForward.getY()
				- areaForStrikeToGateY) < hypot(self.getX()
				- areaForStrikeToGateX, self.getY() - areaForStrikeToGateY))
		// форвард ближе чем я! Отдам ему пас!
		{
			doItPass(self, world, game, move, isForward);

			// System.out.println("pass");
			return true;
			// Мы в своей половине
		} else if (abs(self.getX() - oponentGOALIE.getX()) > world.getWidth() * 0.4d) {

			return myMoveTo(self, world, game, move, areaForStrikeToGateX,
					areaForStrikeToGateY, true);
			// }
		} else if (self.getAngleTo(opponentGateX, opponentGateY) > STRIKE_ANGLE) {
			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(opponentGateX, opponentGateY));
			return true;
		}

		else {
			// мы в зоне удара!
			double angleToNet = self.getAngleTo(opponentGateX, opponentGateY);
			move.setSpeedUp(1.0D);
			move.setTurn(angleToNet);
			if (self.getState() != HockeyistState.SWINGING) {// STabs(angleToNet)
																// >
																// STRIKE_ANGLE
																// * 3) {
				move.setAction(ActionType.SWING); // return true;
			} else // if(self.getStamina() <= game.getStrikeStaminaBaseCost())
			{
				move.setAction(ActionType.STRIKE); // return true;
			}
			if (self.getSwingTicks() > 0) {
				System.out.println("   " + move.getSpeedUp() + " "
						+ move.getTurn() + " " + move.getAction());
				System.out.println(self.getStamina() + "~"
						+ self.getSwingTicks() + " a=" + angleToNet + " SX="
						+ (int) self.getX() + " SY=" + (int) self.getY()
						+ " GGX=" + (int) opponentGateX + " GGY="
						+ (int) opponentGateY + " areaFSTGX="
						+ (int) areaForStrikeToGateX + " areaFSTGY="
						+ (int) areaForStrikeToGateY + " GPX="
						+ (int) guardPointX + " GPY=" + (int) guardPointY);
			}
			return true;

		}
	}

	/**
	 * @return Действия Защитника
	 */
	private boolean doItGuard(Hockeyist self, World world, Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
			// Шайба у наших, подходим ближе к центру своей половины
			return myMoveTo(self, world, game, move, guardPointX, guardPointY,
					false);

			// } else if (world.getPuck().getOwnerPlayerId() != -1) {
			// // Шайба у противника прижимаемся
			// return myMoveTo(self, world, game, move, guardPointX,
			// guardPointY, false);

		} else if (!puckOnOpponentSize) {
			return myMoveTo(self, world, game, move, world.getPuck(), true);
		} else {
			return myMoveTo(self, world, game, move, guardPointX, guardPointY,
					false);
		}
	}

	/**
	 * @return Двинем по нашенски!
	 */
	private boolean myMoveTo(Hockeyist self, World world, Game game, Move move,
			Unit unit, boolean isHard) {
		double newX = unit.getX(), newY = unit.getY();
		if (unit.getId() == world.getPuck().getId()) { // calc traectori!
														// newX+=unit.getSpeedX()*50;newY+=unit.getSpeedY()*50;
		}
		// else return myMoveTo(self, world, game,
		// move,unit.getX(),unit.getY());
		return myMoveTo(self, world, game, move, newX, newY, isHard);
	}

	/**
	 * @return Двинем по нашенски!
	 */
	private boolean myMoveTo(Hockeyist self, World world, Game game, Move move,
			double moveToX, double moveToY, boolean isHard) {
		if (hypot(self.getX() - moveToX, self.getY() - moveToY) < self
				.getRadius() * 2.0D) {
			move.setTurn(self.getAngleTo(opponentGateX, opponentGateY));
			if (hypot(self.getSpeedX(), self.getSpeedY()) > 0.5d)
				move.setSpeedUp(-1.0D);
			return true;
		}
		// Мы на левой или правой половине поля?
		if (isHard) {
			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(moveToX, moveToY));
		} else {
			double needTurn = self.getAngleTo(moveToX, moveToY);
			if (PI / 2 > abs(needTurn)) {
				move.setSpeedUp(1.0D);
				move.setTurn(needTurn);
			} else if (PI / 2 < needTurn) {
				move.setTurn(needTurn - PI);
				move.setSpeedUp(-1.0D);
			} else if (-PI / 2 > needTurn) {
				move.setTurn(needTurn + PI);
				move.setSpeedUp(-1.0D);
			}
		}
		return true;
	}

	/**
	 * @return Действия Нападающего
	 */
	private boolean doItforward(Hockeyist self, World world, Game game,
			Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
			// Шайба у наших, подходим ближе к центру своей половины
			return myMoveTo(self, world, game, move,
					1.0D * (areaForStrikeToGateX), areaForStrikeToGateY, false);

		} else {
			if (puckOnOpponentSize) {
				return myMoveTo(self, world, game, move, world.getPuck(), false);

			} else {
				return myMoveTo(self, world, game, move,
						1.0D * (areaForStrikeToGateX + 0 * world.getWidth()),
						areaForStrikeToGateY, false);

			}
		}
	}

	/**
	 * @return Действия золотой середины
	 */
	private boolean doItMiddle(Hockeyist self, World world, Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
			if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
				Player opponentPlayer = world.getOpponentPlayer();

				double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer
						.getNetFront());
				double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
						.getNetTop());
				netY += (self.getY() < netY ? 0.5D : -0.5D)
						* game.getGoalNetHeight();

				double angleToNet = self.getAngleTo(netX, netY);
				move.setTurn(angleToNet);

				if (abs(angleToNet) < STRIKE_ANGLE) {
					move.setAction(ActionType.SWING);
				}
			} else {
				Hockeyist nearestOpponent = getNearestOpponent(self.getX(),
						self.getY(), world);
				if (nearestOpponent != null) {
					if (self.getDistanceTo(nearestOpponent) > game
							.getStickLength()) {
						move.setSpeedUp(1.0D);
					} else if (abs(self.getAngleTo(nearestOpponent)) < 0.5D * game
							.getStickSector()) {
						move.setAction(ActionType.STRIKE);
					}
					move.setTurn(self.getAngleTo(nearestOpponent));
				}
			}
		} else {
			return myMoveTo(self, world, game, move, world.getPuck(), false);

		}
		return false;
	}

	/**
	 * @return Действия типические типические
	 */
	private boolean doItCommon(Hockeyist self, World world, Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
			if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
				Player opponentPlayer = world.getOpponentPlayer();

				double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer
						.getNetFront());
				double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
						.getNetTop());
				netY += (self.getY() < netY ? 0.5D : -0.5D)
						* game.getGoalNetHeight();

				double angleToNet = self.getAngleTo(netX, netY);
				move.setTurn(angleToNet);

				if (abs(angleToNet) < STRIKE_ANGLE) {
					move.setAction(ActionType.SWING);
					return true;
				}
			} else {
				Hockeyist nearestOpponent = getNearestOpponent(self.getX(),
						self.getY(), world);
				if (nearestOpponent != null) {
					if (self.getDistanceTo(nearestOpponent) > game
							.getStickLength()) {
						move.setSpeedUp(1.0D);
					} else if (abs(self.getAngleTo(nearestOpponent)) < 0.5D * game
							.getStickSector()) {
						move.setAction(ActionType.STRIKE);
						return true;
					}
					move.setTurn(self.getAngleTo(nearestOpponent));
				}
			}
		} else {
			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(world.getPuck()));
			move.setAction(ActionType.TAKE_PUCK);
			return true;
		}
		return false;
	}

	/**
	 * @return Вычислим "глобальные" переменные
	 */
	private void calculateCommonVars(Hockeyist self, World world, Game game,
			Move move) {

		opponentPlayer = world.getOpponentPlayer();
		nearestOpponent = getNearestOpponent(self.getX(), self.getY(), world);

		opponentForStrike = false;
		dangerOponent = null;
		for (Hockeyist hockeyist : world.getHockeyists()) {
			if (!hockeyist.isTeammate()
					&& hockeyist.getType() == HockeyistType.GOALIE)
				oponentGOALIE = hockeyist;

			if (hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE
					|| hockeyist.getState() == HockeyistState.KNOCKED_DOWN
					|| hockeyist.getState() == HockeyistState.RESTING) {
				continue;
			}
			if (self.getDistanceTo(hockeyist) <= game.getStickLength()
					&& abs(self.getAngleTo(hockeyist)) < 0.5D * game
							.getStickSector())
				opponentForStrike = true;
			if (hockeyist.getDistanceTo(self) <= game.getStickLength()
					&& abs(hockeyist.getAngleTo(self)) < 0.5D * game
							.getStickSector())
				dangerOponent = hockeyist;

		}

		opponentGateX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer
				.getNetFront());
		opponentGateY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
				.getNetTop());
		opponentGateY += (self.getY() < opponentGateY ? 0.5D : -0.5D)
				* game.getGoalNetHeight();

		puckOnOpponentSize = (opponentPlayer.getNetBack() < world.getWidth() / 2 ? world
				.getPuck().getX() < world.getWidth() / 2 : world.getPuck()
				.getX() > world.getWidth() / 2);
		areaForStrikeToGateX = world.getWidth() / 2;// opponentGateX
		// + (opponentGateX > world.getWidth() / 2 ? -DIST2STRIKE
		// : DIST2STRIKE);
		areaForStrikeToGateY = opponentGateY
				+ 1.1D
				* (self.getY() < (0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
						.getNetTop())) ? -1.0D : 1.0D)
				* game.getGoalNetHeight();

		guardPointX = world.getWidth()
				* (self.getX() < oponentGOALIE.getX() ? 0.15D : 0.85D);
		guardPointY = world.getHeight() / 2;

		if (isDebugFull > 0) {
			// isDebugFull--;
			// System.out.println("SX"+(int)self.getX()+" SY="+(int)self.getY()+" GGX="+(int)opponentGateX+" GGY="+(int)opponentGateY+" areaFSTGX="+(int)areaForStrikeToGateX+" areaFSTGY="+(int)areaForStrikeToGateY+" GPX="+(int)guardPointX+" GPY="+(int)guardPointY);
			// System.out.println("CY"+(int)(0.5D *
			// (opponentPlayer.getNetBottom() +
			// opponentPlayer.getNetTop()))+" SY="+(int)self.getY()+" GGY="+(int)opponentGateY+" areaFSTGY="+(int)areaForStrikeToGateY+" GPY="+(int)guardPointY);
		}

	}

	/**
	 * @return Попробовать захватить шайбу
	 */
	private boolean TAKE_PUCKEorNotTAKE_PUCK(Hockeyist self, World world,
			Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()
				|| world.getPuck().getOwnerHockeyistId() == self.getId()) {
			return false;// Уже у наших!
		}

		if (self.getDistanceTo(world.getPuck()) <= game.getStickLength()
				|| abs(self.getAngleTo(world.getPuck()) - self.getAngle()) <= 0.5 * game
						.getStickSector()) {
			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(world.getPuck()));
			move.setAction(ActionType.TAKE_PUCK);
			return true;
		}

		return false;
	}

	/**
	 * @return Бить или не бить, вот в чем вопрос.....
	 */
	private boolean STRIKEorNotSTRIKE(Hockeyist self, World world, Game game,
			Move move) {
		if (self.getStamina() < game.getStrikeStaminaBaseCost()) {
			// move.setAction(ActionType.SWING);
			return false;
		}
		if (world.getPuck().getOwnerHockeyistId() != self.getId()
				&& self.getState() == HockeyistState.SWINGING) {
			move.setAction(ActionType.CANCEL_STRIKE);
			return true;

		}
		if (self.getSwingTicks() >= game.getMaxEffectiveSwingTicks()) {
			move.setAction(ActionType.STRIKE);
			return true;
		}
		if (self.getState() == HockeyistState.SWINGING) {
			move.setAction(ActionType.SWING);
			return true;
		}
		return false;
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

		for (Hockeyist hockeyist : world.getHockeyists()) {

			if (!hockeyist.isTeammate()
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

	/**
	 * @return Определяет ближашего противника
	 */
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
