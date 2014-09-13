import model.*;

import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
	private static final double STRIKE_ANGLE = 1.0D * PI / 180.0D;
	private Hockeyist isForward = null;
	private Hockeyist isMiddle = null;
	private Hockeyist isGuard = null;
	private Player opponentPlayer = null;
	final int DIST2STRIKE = 100;
	static int isDebugFull = 10;
	static boolean isDebugMove = true;
	double opponentGateX=0, opponentGateY=0, areaForStrikeToGateX=0, areaForStrikeToGateY=0;
	
	@Override
	public void move(Hockeyist self, World world, Game game, Move move) {
		//if (!(self.getRemainingCooldownTicks() == 0)
		//		|| !(self.getRemainingKnockdownTicks() == 0))
		//	return;

		calculateCommonVars(self, world, game, move);

		defineRoles(self, world);

		if (STRIKEorNotSTRIKE(self, world, game, move))
			return;
		if (TAKE_PUCKEorNotTAKE_PUCK(self, world, game, move))
			return;

		if (self.getId() ==isGuard.getId()) {
			doItGuard(self, world, game, move);
		} else if (self.getId() == isForward.getId()) {
			doItforward(self, world, game, move);
		} else if (self.getId()== isMiddle.getId()) {
			doItMiddle(self, world, game, move);
		} else
		{
			if(isDebugFull>0 ){
				isDebugFull--;
				System.out.println(""+ world.getTick()+" Paniс!!!!"+" self="+self.getId()+" isGuard="+isGuard.getId()+" isForward="+isForward.getId());
			}
	        doItCommon(self, world, game, move);

		}
//		if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
//			move.setSpeedUp(1.0D);
//			move.setTurn(self.getAngleTo(areaForStrikeToGateX,
//					areaForStrikeToGateY));
//			move.setAction(ActionType.TAKE_PUCK);// .SWING);
//		} else {
//			move.setSpeedUp(1.0D);
//			move.setTurn(self.getAngleTo(world.getPuck()));
//			move.setAction(ActionType.TAKE_PUCK);
//		}
	}


	/**
	 * @return Действия типические типические
	 */
	private void doItGuard(Hockeyist self, World world, Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
		    if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
		        Player opponentPlayer = world.getOpponentPlayer();

		        double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer.getNetFront());
		        double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer.getNetTop());
		        netY += (self.getY() < netY ? 0.5D : -0.5D) * game.getGoalNetHeight();

		        double angleToNet = self.getAngleTo(netX, netY);
		        move.setTurn(angleToNet);

		        if (abs(angleToNet) < STRIKE_ANGLE) {
		            move.setAction(ActionType.SWING);
		        }
		    } else {
		        Hockeyist nearestOpponent = getNearestOpponent(self.getX(), self.getY(), world);
		        if (nearestOpponent != null) {
		            if (self.getDistanceTo(nearestOpponent) > game.getStickLength()) {
		                move.setSpeedUp(1.0D);
		            } else if (abs(self.getAngleTo(nearestOpponent)) < 0.5D * game.getStickSector()) {
		                move.setAction(ActionType.STRIKE);
		            }
		            move.setTurn(self.getAngleTo(nearestOpponent));
		        }
		    }
		} else {
		    move.setSpeedUp(1.0D);
		    move.setTurn(self.getAngleTo(world.getPuck()));
		    move.setAction(ActionType.TAKE_PUCK);
		}
	}

	/**
	 * @return Действия типические типические
	 */
	private void doItforward(Hockeyist self, World world, Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
		    if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
		        Player opponentPlayer = world.getOpponentPlayer();

		        double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer.getNetFront());
		        double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer.getNetTop());
		        netY += (self.getY() < netY ? 0.5D : -0.5D) * game.getGoalNetHeight();

		        double angleToNet = self.getAngleTo(netX, netY);
		        move.setTurn(angleToNet);

		        if (abs(angleToNet) < STRIKE_ANGLE) {
		            move.setAction(ActionType.SWING);
		        }
		    } else {
		        Hockeyist nearestOpponent = getNearestOpponent(self.getX(), self.getY(), world);
		        if (nearestOpponent != null) {
		            if (self.getDistanceTo(nearestOpponent) > game.getStickLength()) {
		                move.setSpeedUp(1.0D);
		            } else if (abs(self.getAngleTo(nearestOpponent)) < 0.5D * game.getStickSector()) {
		                move.setAction(ActionType.STRIKE);
		            }
		            move.setTurn(self.getAngleTo(nearestOpponent));
		        }
		    }
		} else {
		    move.setSpeedUp(1.0D);
		    move.setTurn(self.getAngleTo(world.getPuck()));
		    move.setAction(ActionType.TAKE_PUCK);
		}
	}

	/**
	 * @return Действия типические типические
	 */
	private void doItMiddle(Hockeyist self, World world, Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
		    if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
		        Player opponentPlayer = world.getOpponentPlayer();

		        double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer.getNetFront());
		        double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer.getNetTop());
		        netY += (self.getY() < netY ? 0.5D : -0.5D) * game.getGoalNetHeight();

		        double angleToNet = self.getAngleTo(netX, netY);
		        move.setTurn(angleToNet);

		        if (abs(angleToNet) < STRIKE_ANGLE) {
		            move.setAction(ActionType.SWING);
		        }
		    } else {
		        Hockeyist nearestOpponent = getNearestOpponent(self.getX(), self.getY(), world);
		        if (nearestOpponent != null) {
		            if (self.getDistanceTo(nearestOpponent) > game.getStickLength()) {
		                move.setSpeedUp(1.0D);
		            } else if (abs(self.getAngleTo(nearestOpponent)) < 0.5D * game.getStickSector()) {
		                move.setAction(ActionType.STRIKE);
		            }
		            move.setTurn(self.getAngleTo(nearestOpponent));
		        }
		    }
		} else {
		    move.setSpeedUp(1.0D);
		    move.setTurn(self.getAngleTo(world.getPuck()));
		    move.setAction(ActionType.TAKE_PUCK);
		}
	}

	
	/**
	 * @return Действия типические типические
	 */
	private void doItCommon(Hockeyist self, World world, Game game, Move move) {
		if (world.getPuck().getOwnerPlayerId() == self.getPlayerId()) {
		    if (world.getPuck().getOwnerHockeyistId() == self.getId()) {
		        Player opponentPlayer = world.getOpponentPlayer();

		        double netX = 0.5D * (opponentPlayer.getNetBack() + opponentPlayer.getNetFront());
		        double netY = 0.5D * (opponentPlayer.getNetBottom() + opponentPlayer.getNetTop());
		        netY += (self.getY() < netY ? 0.5D : -0.5D) * game.getGoalNetHeight();

		        double angleToNet = self.getAngleTo(netX, netY);
		        move.setTurn(angleToNet);

		        if (abs(angleToNet) < STRIKE_ANGLE) {
		            move.setAction(ActionType.SWING);
		        }
		    } else {
		        Hockeyist nearestOpponent = getNearestOpponent(self.getX(), self.getY(), world);
		        if (nearestOpponent != null) {
		            if (self.getDistanceTo(nearestOpponent) > game.getStickLength()) {
		                move.setSpeedUp(1.0D);
		            } else if (abs(self.getAngleTo(nearestOpponent)) < 0.5D * game.getStickSector()) {
		                move.setAction(ActionType.STRIKE);
		            }
		            move.setTurn(self.getAngleTo(nearestOpponent));
		        }
		    }
		} else {
		    move.setSpeedUp(1.0D);
		    move.setTurn(self.getAngleTo(world.getPuck()));
		    move.setAction(ActionType.TAKE_PUCK);
		}
	}

	/**
	 * @return Вычислим "глобальные" переменные
	 */
	private void calculateCommonVars(Hockeyist self, World world, Game game,
			Move move) {
		
		opponentPlayer = world.getOpponentPlayer();
		opponentGateX = 0.5D * (opponentPlayer.getNetBack() +
		 opponentPlayer.getNetFront());
		opponentGateY = 0.5D * (opponentPlayer.getNetBottom() +
	     opponentPlayer.getNetTop());
		opponentGateY += (self.getY() < opponentGateY ? 0.5D : -0.5D) *
		 game.getGoalNetHeight();
		
		
		areaForStrikeToGateX=opponentGateX+(opponentGateX>world.getWidth()/2?-DIST2STRIKE:DIST2STRIKE);
		areaForStrikeToGateY=opponentGateY+(self.getY() < opponentGateY ? -1.0D : 1.0D) *
				 game.getGoalNetHeight();
//		if(isDebugFull>0)
//		{
//			isDebugFull--;
//			System.out.println("GoalGateX="+opponentGateX+" GoalGateY="+opponentGateY+" areaFSTGX="+areaForStrikeToGateX+" areaFSTGY="+areaForStrikeToGateY);
//		}
       
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
			if( self.getStamina() >= game.getStrikeStaminaBaseCost()) {
				//move.setAction(ActionType.SWING);
				move.setAction(ActionType.STRIKE);
				return true;
			}
			return false;
		}
		if( self.getDistanceTo(world.getPuck()) <= game.getStickLength()
		|| abs(self.getAngleTo(world.getPuck()) - self.getAngle()) < 0.5 * game
				.getStickSector()){
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
		if (//self.getState() != HockeyistState.SWINGING
				//|| 
				self.getStamina() < game.getStrikeStaminaBaseCost()) {
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
        if(world.getPuck().getOwnerHockeyistId() != self.getId()&&self.getState() == HockeyistState.SWINGING)
        {
			move.setAction(ActionType.CANCEL_STRIKE);
			return true;

        }
        if(self.getSwingTicks()>=game.getMaxEffectiveSwingTicks()){
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
		//move.setAction(ActionType.SWING);
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
