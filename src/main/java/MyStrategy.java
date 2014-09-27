import model.*;
import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
	private static final double STRIKE_ANGLE = 2.0D * PI / 180.0D;
	private Hockeyist isForward = null;
	private Hockeyist isMiddle = null;
	private Hockeyist isGuard = null;
	private Player opponentPlayer = null;

	private double mySpeed = 0.9D;
	private static double correctAngleK = 0;

	final static int DIST2STRIKE = 100;
	static int isDebugFull = 100;
	static boolean isDebugMove = true;

	private double opponentGateX = 0, opponentGateY = 0,
			areaForStrikeToGateXP = 0, areaForStrikeToGateYP = 0,
			areaForStrikeToGateXS = 0, areaForStrikeToGateYS = 0;
	private Hockeyist opponentGOALIE = null;
	private Hockeyist nearestOpponent = null;
	private boolean opponentForStrike = false;
	private Hockeyist dangerOponent = null;
	private boolean puckOnOpponentSide = false;
	private boolean puckOnMySide = false;

	double guardPointX = 0, guardPointY = 0;

	@Override
	public void move(Hockeyist self, World world, Game game, Move move) {
		// if (!(self.getRemainingCooldownTicks() == 0)
		// || !(self.getRemainingKnockdownTicks() == 0))
		// return;

		calculateCommonVars(self, world, game, move);

		defineRoles(self, world);

		if (self.getState() == HockeyistState.SWINGING) {
			// System.out.println(world.getTick()+" SWINGING"+" SX="+(int)self.getX()+" SY="+(int)self.getY()+" GGX="+(int)opponentGateX+" GGY="+(int)opponentGateY+" areaFSTGX="+(int)areaForStrikeToGateX+" areaFSTGY="+(int)areaForStrikeToGateY+" GPX="+(int)guardPointX+" GPY="+(int)guardPointY);
			if (opponentForStrike) {
				move.setAction(ActionType.STRIKE);
			}
			if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
				move.setAction(ActionType.STRIKE);
			} else {
				move.setAction(ActionType.CANCEL_STRIKE);
			}
			return;
		}
		if (doItPuckOwner(self, world, game, move))
			return;

		if (TAKE_PUCKEorNotTAKE_PUCK(self, world, game, move))
			return;

		if (world.getPuck().getOwnerHockeyistId() != self.getId()
				&& opponentForStrike)// &&self.getState() !=
										// HockeyistState.SWINGING) {
		{
			move.setAction(ActionType.STRIKE);
			move.setAction(ActionType.SWING);
			return;
		}

		if (STRIKEorNotSTRIKE(self, world, game, move))
			return;

		if (self.getId() == isGuard.getId()) {
			// if (doItforward(self, world, game, move))
			// return;
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
	 * @return Действия что бы отдать пас
	 */
	private boolean doItPass(Hockeyist self, World world, Game game, Move move,
			Hockeyist passTo, boolean passis) {
		if (passis && 0.5D * world.getWidth() > (opponentGateX - self.getX())) {
			double angleToPass = self.getAngleTo(passTo);
			boolean passTrue = true;

			for (Hockeyist hockeyist : world.getHockeyists()) {
				if (hockeyist.isTeammate()
                    || hockeyist.getType() == HockeyistType.GOALIE
						|| hockeyist.getState() == HockeyistState.KNOCKED_DOWN
						|| hockeyist.getState() == HockeyistState.RESTING) {
					continue;
				}

				if (sin(self.getAngleTo(hockeyist) - angleToPass)
						* self.getDistanceTo(hockeyist) < game.getStickLength()
						&& self.getDistanceTo(hockeyist) > 2 * self.getRadius())// game.getStickLength())
					passTrue = false;
			}
			if (passTrue) {
				// move.setSpeedUp(mySpeed);
				move.setPassAngle(angleToPass);
				move.setAction(ActionType.PASS);
				// System.out.println(world.getTick()+" pass");
				return true;
			} else {
				// move.setSpeedUp(mySpeed);
				// move.setTurn(self.getAngleTo(passTo.getX(), world.getHeight()
				// - passTo.getY()));
				return false;//return myMoveTo(self, world, game, move, opponentGateX,
						//opponentGateY, true, self.getRadius());
				// move.setAction(ActionType.NONE);
			}
		} else {
			move.setTurn(self.getAngleTo(0, world.getWidth() / 2));
			move.setAction(ActionType.PASS);
			return true;
		}
		// return false;
	}

	/**
	 * @return Действия владеющего шайбой
	 */
	private boolean doItPuckOwner(Hockeyist self, World world, Game game,
			Move move) {

		if (world.getPuck().getOwnerHockeyistId() != self.getId()) {

			return false;
		}
		if (null == opponentGOALIE) {
		}
		if (0.5D * world.getWidth() > (opponentGateX - self.getX())
				&& null != opponentGOALIE
				&& ((hypot(isForward.getX() - areaForStrikeToGateXP,
						(isForward.getY() - areaForStrikeToGateYP) / 3) < hypot(
						self.getX() - areaForStrikeToGateXP,
						(self.getY() - areaForStrikeToGateYP) / 3)) || hypot(
						isForward.getX() - areaForStrikeToGateXS,
						(isForward.getY() - areaForStrikeToGateYS) / 3) < hypot(
						self.getX() - areaForStrikeToGateXP,
						(self.getY() - areaForStrikeToGateYP) / 3)))
		// форвард ближе чем я! Отдам ему пас!
		{
			if // (passTrue)
			(doItPass(self, world, game, move, isForward, true))
				return true;
			// Мы в своей половине
		} // else
		// double
		// newX=0.5D*world.getWidth()+0.2D*world.getWidth()*(opponentGateX>0.5D*world.getWidth()?1:-1),newY=0.5D*world.getHeight()+0.3D*world.getHeight()*(self.getY()>0.5D*world.getHeight()?-1:1);
		// System.out.println(""+world.getTick()+" "+(int)self.getX()+" "+(int)self.getY()+" afsx="+(int)areaForStrikeToGateXP+" afxy="+(int)areaForStrikeToGateYP+" newX="+(int)newX+" newY="+(int)newY);
		// if (abs(self.getX() - opponentGateX)>0.4D*world.getWidth()) {
		// System.out.println("nw");
		// return myMoveTo(self, world, game, move, newX,
		// newY, true,self.getRadius());
		// }
		//
		// else
		if (null != opponentGOALIE
				&& (abs(self.getX() - areaForStrikeToGateXP) > self.getRadius() * 4 || abs(self
						.getY() - areaForStrikeToGateYP) > self.getRadius() * 2)) {

			return myMoveTo(self, world, game, move, areaForStrikeToGateXP,
					areaForStrikeToGateYP, false, self.getRadius());
		} else {
			// мы в зоне удара!
			double angleToNet = self.getAngleTo(opponentGateX, opponentGateY);

			if (abs(angleToNet) > STRIKE_ANGLE) {
				move.setSpeedUp(0.0D);
				move.setTurn(angleToNet);
			} else if (self.getState() != HockeyistState.SWINGING) {
				// System.out.println(hypot(self.getX() - areaForStrikeToGateXP,
				// self.getY()
				// - areaForStrikeToGateYP)+"<"+ self.getRadius()*2);
				move.setAction(ActionType.SWING); // return true;
			} else {
				move.setAction(ActionType.STRIKE); // return true;
				// System.out.println(angleToNet+" SX="+(int)self.getX()+" SY="+(int)self.getY()+" GGX="+(int)opponentGateX+" GGY="+(int)opponentGateY+" areaFSTGX="+(int)areaForStrikeToGateX+" areaFSTGY="+(int)areaForStrikeToGateY+" GPX="+(int)guardPointX+" GPY="+(int)guardPointY);

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
					false, self.getRadius()*2);

			// } else if (world.getPuck().getOwnerPlayerId() != -1) {
			// // Шайба у противника прижимаемся
			// return myMoveTo(self, world, game, move, guardPointX,
			// guardPointY, false);

	//	} else if (puckOnMySide && world.getPuck().getOwnerPlayerId() != -1
	//			&& world.getPuck().getOwnerPlayerId() != self.getPlayerId()) {
	//		return myMoveTo(self, world, game, move, guardPointX, guardPointY,
	//				false, self.getRadius());
			// Идем между шайбой и точкой защиты
			// return myMoveTo(self, world, game, move,
			// (world.getPuck().getX()+guardPointX)/2,(world.getPuck().getY()+guardPointY)/2,
			// false,game.getStickLength());
		} else if (puckOnMySide) {
			return myMoveTo(self, world, game, move, world.getPuck(), true,
					game.getStickLength());
		} else {
			return myMoveTo(self, world, game, move, guardPointX, guardPointY,
					false, self.getRadius());
		}
	}

	/**
	 * @return Двинем по нашенски!
	 */
	private boolean myMoveTo(Hockeyist self, World world, Game game, Move move,
			Unit unit, boolean isHard, double nearArea) {
		double newX = unit.getX(), newY = unit.getY();
		if (unit.getId() == world.getPuck().getId()
				&& self.getId() == isForward.getId()) { // calc traectori!
			newX += unit.getSpeedX() * correctAngleK;
			newY += unit.getSpeedY() * correctAngleK;
			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(newX, newY));
			return true;
		} else {

		}
		// else return myMoveTo(self, world, game,
		// move,unit.getX(),unit.getY());
		return myMoveTo(self, world, game, move, newX, newY, isHard, nearArea);
	}

	/**
	 * @return Двинем по нашенски!
	 */
	private boolean myMoveTo(Hockeyist self, World world, Game game, Move move,
			double moveToX, double moveToY, boolean isHard, double nearArea) {
		if (hypot(self.getX() - moveToX, self.getY() - moveToY) < nearArea) {
			if (self.getId() == isGuard.getId()) {
				move.setTurn(self.getAngleTo(world.getPuck()));
			} else
				move.setTurn(self.getAngleTo(areaForStrikeToGateXP,
						areaForStrikeToGateYP));
			// if (hypot(self.getSpeedX(), self.getSpeedY()) > 0.5d)
			move.setSpeedUp(0.0D);
			return true;
		}
		// Мы на левой или правой половине поля?
		// isHard=true;
		if (isHard) {
			move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(moveToX, moveToY));
		} else if (world.getPuck().getOwnerHockeyistId() == self.getId()
				&& (abs(self.getX() - opponentGateX) > world.getWidth() / 3)) {
			move.setTurn(self.getAngleTo(moveToX, moveToY));// move.setTurn(self.getAngleTo(areaForStrikeToGateXP,
															// areaForStrikeToGateYP));
			move.setSpeedUp(1.0D);
			// System.out.println("GoTo Gate! "+world.getTick());
		} else {
			double needTurn = self.getAngleTo(moveToX, moveToY);
			if (0.5D * PI > abs(needTurn)) {
				move.setSpeedUp(mySpeed);
				move.setTurn(needTurn);
			} else if (0.5D * PI < needTurn) {
				move.setTurn(needTurn - PI);
				move.setSpeedUp(-mySpeed);
			} else if (-0.5D * PI > needTurn) {
				move.setTurn(needTurn + PI);
				move.setSpeedUp(-mySpeed);
			}
		}
		if (abs(move.getTurn()) > (PI / 20))
			move.setSpeedUp(0.0D);
		else
			move.setSpeedUp(move.getSpeedUp());// * 5
		// * self.getDistanceTo(moveToX, moveToY) / world.getHeight());
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
					1.0D * (areaForStrikeToGateXP), areaForStrikeToGateYP,
					false, 2.0D *self.getRadius());

		} else {
			// if (puckOnOpponentSide) {
			return myMoveTo(self, world, game, move, world.getPuck(), false,
					game.getStickLength());

			// } else {
			// return myMoveTo(self, world, game, move,
			// 1.0D * (areaForStrikeToGateX + 0 * world.getWidth()),
			// areaForStrikeToGateY, false,self.getRadius());

			// }
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
			return myMoveTo(self, world, game, move, world.getPuck(), false,
					game.getStickLength());

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

		double OHX = 0, OHY = 0;
		int OHQ = 0;

		opponentForStrike = false;
		dangerOponent = null;
		opponentGOALIE = null;
		for (Hockeyist hockeyist : world.getHockeyists()) {
			if (!hockeyist.isTeammate()
					&& hockeyist.getType() == HockeyistType.GOALIE)
				opponentGOALIE = hockeyist;

			if (hockeyist.isTeammate()
					|| hockeyist.getType() == HockeyistType.GOALIE
					|| hockeyist.getState() == HockeyistState.KNOCKED_DOWN
					|| hockeyist.getState() == HockeyistState.RESTING) {
				continue;
			}
			OHX += hockeyist.getX();
			OHY += hockeyist.getY();
			OHQ++;
			if (self.getDistanceTo(hockeyist) <= 0.8D * game.getStickLength()
					&& abs(self.getAngleTo(hockeyist)) < 0.5D * game
							.getStickSector())
				opponentForStrike = true;
			if (hockeyist.getDistanceTo(self) <= game.getStickLength()
					&& abs(hockeyist.getAngleTo(self)) < 0.5D * game
							.getStickSector())
				dangerOponent = hockeyist;

		}
		if (0 == OHQ)
			OHQ = 1;
		OHX /= OHQ;
		OHY /= OHQ;
		opponentGateX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer
				.getNetFront());
		opponentGateY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
				.getNetTop());
//		if (abs(self.getX() - opponentGateX) > 0.5D * world.getWidth()) {
//			opponentGateY += (OHY < opponentGateY ? 0.2D : -0.2D)
//					* world.getHeight() * (null == opponentGOALIE ? 0 : 1);
//
//		} else {
			opponentGateY += (self.getY() < opponentGateY ? 0.5D : -0.5D)
					* game.getGoalNetHeight()
					* (null == opponentGOALIE ? 0 : 1);
//	}

		puckOnOpponentSide = (opponentPlayer.getNetBack() < world.getWidth() / 2 ? world
				.getWidth() * 0.1 < world.getPuck().getX()
				&& world.getPuck().getX() < world.getWidth() * 0.6 : world
				.getWidth() * 0.4 > world.getPuck().getX()
				&& world.getPuck().getX() > world.getWidth() * 0.9);
		puckOnMySide = (opponentPlayer.getNetBack() > world.getWidth() * 0.5 ? world
				.getWidth() * 0.1 < world.getPuck().getX()
				&& world.getPuck().getX() < world.getWidth() * 0.4 : world
				.getWidth() * 0.6 < world.getPuck().getX()
				&& world.getPuck().getX() < world.getWidth() * 0.9);
		areaForStrikeToGateXP = opponentGateX// opponentGateX
				+ 4.0D
				* (opponentGateX > 0.5D*world.getWidth() ? -DIST2STRIKE
						: DIST2STRIKE);
		areaForStrikeToGateXS = areaForStrikeToGateXP;
		double myshiftY=0;
		if(abs(self.getX() - opponentGateX) < 0.6D * world.getWidth()){
			myshiftY=(self.getY() > (0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
					.getNetTop())) ? 1.0D : -1.0D);
		}
		else {
			myshiftY=(OHY < (0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
					.getNetTop())) ? 1.0D : -1.0D);
		}
		areaForStrikeToGateYP = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
				.getNetTop())
				+ 1.0D
				* myshiftY
				* game.getGoalNetHeight()* (null == opponentGOALIE ? 0 : 1);
		areaForStrikeToGateYS = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
				.getNetTop())
				- 1.0D
				* myshiftY
				* game.getGoalNetHeight()* (null == opponentGOALIE ? 0 : 1);

		guardPointX = world.getWidth()
				* (self.getX() < opponentGateX ? 0.15D : 0.85D);
		guardPointY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer
				.getNetTop());//world.getHeight() / 2 + 2 * self.getRadius();

		// if ((OHY < 0.5D * world.getHeight() && areaForStrikeToGateYP <
		// areaForStrikeToGateYS)
		// || (OHY > 0.5D * world.getHeight() && areaForStrikeToGateYP >
		// areaForStrikeToGateYS)) {
		//
		// double ts = 0, opponentGateYR = 0.5D
		// * (opponentPlayer.getNetBottom() + opponentPlayer
		// .getNetTop())
		// - (self.getY() < opponentGateY ? 0.5D : -0.5D)
		// * game.getGoalNetHeight()
		// * (null == opponentGOALIE ? 0 : 1);
		// ts = areaForStrikeToGateYP;
		// areaForStrikeToGateYP = areaForStrikeToGateYS;
		// areaForStrikeToGateYS = ts;
		// ts = opponentGateYR;
		// opponentGateYR = opponentGateY;
		// opponentGateY = ts;
		// }
		// if (isDebugFull > 0) {
		// isDebugFull--;
		// // System.out.println(""+game.getStickLength()+" "+self.getRadius());
		// System.out.println("SX"+(int)self.getX()+" SY="+(int)self.getY()+" GGX="+(int)opponentGateX+" GGY="+(int)opponentGateY+" areaFSTGX="+(int)areaForStrikeToGateXP+" areaFSTGY="+(int)areaForStrikeToGateYP+" GPX="+(int)guardPointX+" GPY="+(int)guardPointY);
		// // System.out.println("CY"+(int)(0.5D *
		// // (opponentPlayer.getNetBottom() +
		// //
		// opponentPlayer.getNetTop()))+" SY="+(int)self.getY()+" GGY="+(int)opponentGateY+" areaFSTGY="+(int)areaForStrikeToGateY+" GPY="+(int)guardPointY);
		// }

	}

	/**
	 * @return Попробовать захватить шайбу
	 */
	private boolean TAKE_PUCKEorNotTAKE_PUCK(Hockeyist self, World world,
			Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
			return false;// Уже у наших!
		}

		if (self.getDistanceTo(world.getPuck()) <= game.getStickLength()
				|| abs(self.getAngleTo(world.getPuck()) - self.getAngle()) <= 0.5 * game
						.getStickSector()) {
			//move.setSpeedUp(1.0D);
			move.setTurn(self.getAngleTo(world.getPuck()));
			if(self.getId()==isGuard.getId()&&
					(10<hypot(world.getPuck().getSpeedX(),world.getPuck().getSpeedY())
							||world.getPuck().getOwnerPlayerId() !=-1&&world.getPuck().getOwnerPlayerId() != self.getPlayerId())){
				move.setAction(ActionType.STRIKE);
			}else{
			move.setAction(ActionType.TAKE_PUCK);
			}
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
	private Hockeyist getNearestOpponent(double x, double y, World world) {
		nearestOpponent = null;
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
