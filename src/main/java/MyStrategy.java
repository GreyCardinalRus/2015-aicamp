import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


import model.Bonus;
import model.BonusType;
import model.Car;
import model.CarType;
import model.Game;
import model.Move;
import model.World;


import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
	//private static boolean debugInfo2File = false;
	private static boolean isDebugMove = true;
	private static boolean isTraceMove = true ;
	private static int ReverceMode[] = {0,0}, DreeftMode[] = {0,0} ;
	private static double driftAngle[] = {0,0},prevTXR[] = {0,0},prevTYR[] = {0,0};
	private static boolean mapSaved = true;
	private static int nextTX[] = {0,0}, nextTY[] = {0,0};
	//private static int numTemplate = -1;
	private static int prevTX[] = {0,0},prevTY[] = {0,0},prevMX[] = {0,0},prevMY[] = {0,0};

	////private static double myLastRealX=0,myLastRealY=0;
	/// проблемне 3 и 6
	@Override
	public void move(Car self, World world, Game game, Move move) {
		GreyCardinalMove( self, world, game, move);
		//WhiteHarrierMove( self, world, game, move);
	}
	void WhiteHarrierMove( Car self, World world, Game game, Move move) {

	    double currSpeed = Math.sqrt(Math.pow(self.getSpeedX(),2)+Math.pow(self.getSpeedY(),2));

        //TileType [][] tiles = world.getTilesXY();

        //int [][] waypoints = world.getWaypoints();

        if (currSpeed > 30)
        {
            move.setEnginePower(0.0D);
            move.setWheelTurn(1.0D);
            
        }
        else
        {
            move.setEnginePower(1.0D);
        }
        if (self.getAngle() > -Math.PI/4)
        {
        	move.setBrake(true);
        }
        if (self.getSpeedX() < 15 )
        {
            move.setEnginePower(1.0D);
        	move.setBrake(false);
        }
        
        
        if(self.getAngle() == 0)
        {
            move.setEnginePower(1.0D);
        }

        if (world.getTick() > game.getInitialFreezeDurationTicks()) {
            move.setUseNitro(true);
        }


}

	void GreyCardinalMove(Car self, World world, Game game, Move move)
	{
		MyMoveToNexCorner(self, world, game, move);
		// MyMoveToBonus(self, world, game, move);
		UseSpillOil(self, world, game, move);
		UseThrowProjectile(self, world, game, move);

		//if (move.getEnginePower() == 0)
		//	move.setEnginePower(1);
			if (!mapSaved) {
			mapSaved = true;
			// aStar.printRoute();
			try {
				File outfile = new File("map.csv");
				FileWriter wrt;
				outfile.createNewFile();
				wrt = new FileWriter(outfile);
				for (int i = 0; i < world.getWidth(); i++) {
					for (int j = 0; j < world.getHeight(); j++)

						wrt.append(world.getTilesXY()[j][i] + "\t");
					wrt.append("\n");
				}
				wrt.append("\n");
				wrt.append("\n");
				for (int i = 0; i < world.getWaypoints().length; i++) {
					// for (int j = 0; j < world.getHeight(); j++)
					wrt.append(world.getWaypoints()[i][0] + "\t");
					wrt.append(world.getWaypoints()[i][1] + "\t");
					wrt.append("\n");
				}

				wrt.flush();
				wrt.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}		
	}
	void UseSpillOil(Car self, World world, Game game, Move move) {
		if (self.getOilCanisterCount() == 0 || self.getRemainingOilCooldownTicks() > 0
				|| abs(move.getWheelTurn()) < 0.5)
			return;
		boolean spillOil = false;
		int myTargetX = (int) ((self.getX() )/ game.getTrackTileSize()),
				myTargetY = (int) ((self.getY() ) / game.getTrackTileSize());
		for (Car car : world.getCars()) {
			if (car.isTeammate()||car.getDurability()==0||car.isFinishedTrack()||1D>hypot(car.getSpeedX(), car.getSpeedY())
					||(myTargetX != (int) ((car.getX() )/ game.getTrackTileSize())&&myTargetY != (int) ((car.getY() )/ game.getTrackTileSize())))
				continue;
			if ((abs(self.getAngleTo(car)) > 0.90 * PI && self.getDistanceTo(car) < 2D * game.getTrackTileSize()
					&& self.getDistanceTo(car) > 0.9D * game.getTrackTileSize())
					|| (abs(self.getAngleTo(car)) > 0.95 * PI
							&& self.getDistanceTo(car) < 1D * game.getTrackTileSize()))
				spillOil = true;

		}
		move.setSpillOil(spillOil);

	}

	void UseThrowProjectile(Car self, World world, Game game, Move move) {
		if (self.getProjectileCount() == 0 || self.getRemainingProjectileCooldownTicks() > 0)
			return;
		int myTargetX = (int) ((self.getX() )/ game.getTrackTileSize()),
				myTargetY = (int) ((self.getY() ) / game.getTrackTileSize());		
		boolean throwProjectile = false;
		for (Car car : world.getCars()) {
			if (car.isTeammate()||car.getDurability()==0||car.isFinishedTrack()
					||(myTargetX != (int) ((car.getX() )/ game.getTrackTileSize())&&myTargetY != (int) ((car.getY() )/ game.getTrackTileSize()))
					)
				continue;
			if (abs(self.getAngleTo(car)) < 0.010D * PI && self.getDistanceTo(car) < 2 * game.getTrackTileSize())
				throwProjectile = true;
		}
		move.setThrowProjectile(throwProjectile);
	}

//	private boolean cellFree(int x, int y, World world) {
//		if (x < 0 || y < 0 || x >= world.getWidth() || y >= world.getHeight())
//			return false;
//		if (world.getTilesXY()[x][y] == TileType.EMPTY
//				//||world.getTilesXY()[x][y] == TileType.RIGHT_BOTTOM_CORNER
//)
//			return false;
//		return true;
//	}

	void MyMoveToNexCorner(Car self, World world, Game game, Move move) {
 		if(world.getTick() < 180) return;
 		int carId=(self.getType()==CarType.BUGGY?1:0);
  		boolean useNitro = (world.getTick() > 180&&self.getRemainingNitroCooldownTicks()==0)?true:false;
		int nexpointindex=self.getNextWaypointIndex();
		int targetXCP, targetYCP;
		int cornerTileOffset = (int)(game.getTrackTileSize()*0.25);//((speedModule > 5)?((speedModule > 10)?((speedModule > 15)?((speedModule > 20) ? ((speedModule > 25) ? 0.10:0.15):0.20) : 0.25): 0.30): 0.35));
		int nextWaypointX = 0;
		int nextWaypointY = 0;

		int targetX=world.getWaypoints()[nexpointindex][0];
		int targetY=world.getWaypoints()[nexpointindex][1];
		int myTargetX = (int) ((self.getX() )/ game.getTrackTileSize()),
				myTargetY = (int) ((self.getY() ) / game.getTrackTileSize());
		double speedModule = hypot(self.getSpeedX(), self.getSpeedY());	
		// Сворачиваем точки - если они на одной прямой -а то тупит не зная что это прямая  на 9 тупит!!!
		if(nexpointindex<world.getWaypoints().length-1){
		  if(myTargetX==targetX&&targetX==world.getWaypoints()[nexpointindex+1][0]){
			  if(myTargetY>targetY&&targetY>world.getWaypoints()[nexpointindex+1][1])
				  while(nexpointindex<world.getWaypoints().length-1&&targetX==world.getWaypoints()[nexpointindex+1][0]&&world.getWaypoints()[nexpointindex][1]>world.getWaypoints()[nexpointindex+1][1])
				  nexpointindex++;
			  else if(myTargetY<targetY&&targetY<world.getWaypoints()[nexpointindex+1][1])
				  while(nexpointindex<world.getWaypoints().length-1&&targetX==world.getWaypoints()[nexpointindex+1][0]&&world.getWaypoints()[nexpointindex][1]<world.getWaypoints()[nexpointindex+1][1])
					  nexpointindex++;
		  }else
		  if(myTargetY==targetY&&targetY==world.getWaypoints()[nexpointindex+1][1]){
			  
			  if(myTargetX>targetY&&targetX>world.getWaypoints()[nexpointindex+1][0])
				  while(nexpointindex<world.getWaypoints().length-1&&targetY==world.getWaypoints()[nexpointindex+1][1]&&world.getWaypoints()[nexpointindex][0]>world.getWaypoints()[nexpointindex+1][0])
				  nexpointindex++;
				  else 	if(myTargetX<targetY&&targetX<world.getWaypoints()[nexpointindex+1][0])			  
					  while(nexpointindex<world.getWaypoints().length-1&&targetY==world.getWaypoints()[nexpointindex+1][1]&&world.getWaypoints()[nexpointindex][0]<world.getWaypoints()[nexpointindex+1][0])
					  nexpointindex++;

		  }
		}
	//	if(isDebugMove&&(targetX!=world.getWaypoints()[nexpointindex][0]||targetY!=world.getWaypoints()[nexpointindex][1]))
	//		System.out.println(" change "+targetX+","+targetY+" to "+world.getWaypoints()[nexpointindex][0]+","+world.getWaypoints()[nexpointindex][1]);
		targetX=world.getWaypoints()[nexpointindex][0]*5+3;
		targetY=world.getWaypoints()[nexpointindex][1]*5+3;
		myTargetX=(int) ((self.getX() ) / game.getTrackTileSize()*5);
		myTargetY=(int) ((self.getY() ) / game.getTrackTileSize()*5);
		AStar aStar = new AStar(world.getWidth()*5, world.getHeight()*5);
		// Заполним карту как-то клетками, учитывая преграду
		// map08 gluck
		fillMapCarRacing2015(world, aStar,game,self);
		aStar.calculateRoute(new Cell(myTargetX, myTargetY), new Cell(targetX, targetY));
		//aStar.printRoute();
		if (aStar.nextCell() != null )//&& !(myTargetX ==targetX&&targetX == (int)(aStar.nextCell().x/3) || myTargetY ==targetY&&targetY ==(int)(aStar.nextCell().y/3)) )
			{
			//nextTX = targetX; nextTY = targetY;
			if (isDebugMove&&(targetX !=aStar.nextCell().x
					&& targetY !=aStar.nextCell().y)
			)
				{
				if(isTraceMove) aStar.printRoute();
//////	 			System.out.println("From targetX " + nextTX +"," + targetY +" to " + (((int)(aStar.nextCell().x/3)==myTargetX)?myTargetX+aStar.nextCell().x-myTargetX*3-1:(int)(aStar.nextCell().x/3)) +"," + (((int)(aStar.nextCell().y/3)==myTargetY)?myTargetY+aStar.nextCell().y-myTargetY*3-1:(int)(aStar.nextCell().y/3)));
			}
		targetX =aStar.nextCell().x;
		targetY =aStar.nextCell().y;
		}
		targetXCP=targetX;targetYCP=targetY;	
		double dt=self.getDistanceTo((0.5+targetX)*game.getTrackTileSize()/5,(0.5+targetY)*game.getTrackTileSize()/5);
		double at=self.getAngleTo((0.5+targetX)*game.getTrackTileSize()/5,(0.5+targetY)*game.getTrackTileSize()/5);
		if(DreeftMode[carId]>0){
			DreeftMode[carId]--;
			move.setWheelTurn(driftAngle[carId]*5 );
			if(speedModule>15d) move.setBrake(true);
			move.setEnginePower(0);//(DreeftMode>40)||DreeftMode< 10?1:0);
			if(isDebugMove)//&&(nextRealX!=nextWaypointX||nextRealY!=nextWaypointY))
			{
//				
//				nextRealX=nextWaypointX;
//		 		nextRealY=nextWaypointY;
			System.out.println(" DreeftMode MP ("+myTargetX+ ") "+(int)self.getX()+" ("+myTargetY+ ") "+(int)self.getY() +"  NP ("+targetX+ ","+targetY+ ") driftAngle="+driftAngle[carId]+" "+targetXCP+","+targetYCP);//+" DTWP "+(int)distanceToWaypoint+" power="+(int)(100*self.getEnginePower())+" speed "+(int)speedModule+" "+world.getTilesXY()[targetX][targetY]+" "+" "+angleToWaypoint+" "+move.isUseNitro()+ " distToBonus = "+distToBonus+" at = "+at +" dt = "+dt);
			}
			return;
		}	
//		int nextTX=targetX,nextTY=targetY;
		if(	abs(at)<0.6
				&&(false
			//||speedModule>05D&&dt<=0.1*game.getTrackTileSize()
//			||speedModule>10D&&dt<=0.2*game.getTrackTileSize()
//			||speedModule>10D&&dt<=0.3*game.getTrackTileSize()
//			||speedModule>10D&&dt<=0.4*game.getTrackTileSize()
//			||speedModule>10D&&dt<=0.5*game.getTrackTileSize()
//			||speedModule>10D&&dt<=0.6*game.getTrackTileSize()
//			||speedModule>10D&&dt<=0.5*game.getTrackTileSize()
// 			||speedModule>13D&&dt<=0.65*game.getTrackTileSize()
//			||speedModule>14D&&dt<=0.9*game.getTrackTileSize()
//			||speedModule>15D&&dt<=0.7*game.getTrackTileSize()
			||speedModule>17D&&dt<=0.9*game.getTrackTileSize()
//			||speedModule>18D&&dt<=1.0*game.getTrackTileSize()
			||speedModule>20D&&dt<=1.0  *game.getTrackTileSize()
//			||speedModule>22D&&dt<=1.3*game.getTrackTileSize()
//			||speedModule>23D&&dt<=1.2  *game.getTrackTileSize()
//			||speedModule>30D&&dt<=1.9*game.getTrackTileSize()//+
//			||speedModule>35D&&dt<=2.1 *game.getTrackTileSize()
			))
		{
		    if(nexpointindex==world.getWaypoints().length-1) nexpointindex=0;
		    else nexpointindex++;
//		    int oldX=targetX,oldY=targetY;
		    if (aStar.nextCell() == null ||(abs(aStar.nextCell().x-aStar.getFinish().x)<2&&abs(aStar.nextCell().y-aStar.getFinish().y)<2)){
		    	targetX=world.getWaypoints()[nexpointindex][0]*5+3; targetY=world.getWaypoints()[nexpointindex][1]*5+3;
		    	aStar.calculateRoute(new Cell(myTargetX, myTargetY), new Cell(targetX, targetY));
				if (aStar.nextCell() != null )//&& !(myTargetX ==targetX&&targetX == (int)(aStar.nextCell().x/3) || myTargetY ==targetY&&targetY ==(int)(aStar.nextCell().y/3)) )
				{
					targetX =aStar.nextCell().nextPoint.nextPoint.x;
					targetY =aStar.nextCell().nextPoint.nextPoint.y;
				}
		    }
		    else {
		    	targetX =aStar.nextCell().nextPoint.x;
				targetY =aStar.nextCell().nextPoint.y;
		    }
		    //DreeftMode = 50;
			nextWaypointX = (int)((targetX + 0.1D) * game.getTrackTileSize()/5);
			nextWaypointY = (int)((targetY + 0.1D) * game.getTrackTileSize()/5);
			//cornerTileOffset=(int)(game.getTrackTileSize()*0.3);//(int)(game.getTrackTileSize()*((speedModule > 5)?((speedModule > 10)?((speedModule > 15)?((speedModule > 20) ? ((speedModule > 25) ? 0.10:0.15):0.20) : 0.25): 0.30): 0.35));
			switch (world.getTilesXY()[(int)(targetX/5)][(int)(targetY/5)]) {
			case LEFT_TOP_CORNER:
				nextWaypointX += cornerTileOffset;
				nextWaypointY += cornerTileOffset;
				break;
			case RIGHT_TOP_CORNER:
				nextWaypointX -= cornerTileOffset;
				nextWaypointY += cornerTileOffset;
				break;
			case LEFT_BOTTOM_CORNER:
				nextWaypointX += cornerTileOffset;
				nextWaypointY -= cornerTileOffset;
				break;
			case RIGHT_BOTTOM_CORNER:
				nextWaypointX -= cornerTileOffset;
				nextWaypointY -= cornerTileOffset;
				break;
			default:
			}

		    driftAngle[carId] = self.getAngleTo(nextWaypointX, nextWaypointY);	
		   // DreeftMode = (abs(driftAngle)<0.2)?0:10;
		   
		}
		 else driftAngle[carId]=0;
		// System.out.println(""+nextTX+" "+targetX+" "+nextTY+" "+targetY);
		if (nextTX[carId] != targetX || nextTY[carId] != targetY) {
			// System.out.println(""+targetX+" "+targetY);
			nextTX[carId] = targetX;
			nextTY[carId] = targetY;
		}
		
		nextWaypointX = (int)((targetX + 0.1D) * game.getTrackTileSize()/5);
		nextWaypointY = (int)((targetY + 0.1D) * game.getTrackTileSize()/5);

		cornerTileOffset=0;//(int)(game.getTrackTileSize()*((speedModule > 5)?((speedModule > 10)?((speedModule > 15)?((speedModule > 20) ? ((speedModule > 25) ? 0.17:0.20):0.22) : 0.25): 0.27): 0.30));
		switch (world.getTilesXY()[(int)(targetX/5)][(int)(targetY/5)]) {
		case LEFT_TOP_CORNER:
			nextWaypointX += cornerTileOffset;
			nextWaypointY += cornerTileOffset;
			break;
		case RIGHT_TOP_CORNER:
			nextWaypointX -= cornerTileOffset;
			nextWaypointY += cornerTileOffset;
			break;
		case LEFT_BOTTOM_CORNER:
			nextWaypointX += cornerTileOffset;
			nextWaypointY -= cornerTileOffset;
			break;
		case RIGHT_BOTTOM_CORNER:
			nextWaypointX -= cornerTileOffset;
			nextWaypointY -= cornerTileOffset;
			break;
		default:
		}

   		double distToBonus = world.getWidth()*2*game.getTrackTileSize();
   		double angleToBonus=0;
   		double distanceToWaypoint = self.getDistanceTo(nextWaypointX, nextWaypointY);
   		double angleToNextWaypoint = self.getAngleTo(nextWaypointX, nextWaypointY);
		for (Bonus bonus : world.getBonuses()) {
			if (self.getDurability() > 0.95D && bonus.getType() == BonusType.REPAIR_KIT)
				continue;
			angleToBonus = abs(self.getAngleTo(bonus) - self.getAngleTo(nextWaypointX,nextWaypointY));
			if(abs((((int)((bonus.getX())/ game.getTrackTileSize()) + 0.5D) * game.getTrackTileSize())-bonus.getX())>0.20* game.getTrackTileSize()
					&&abs((((int)((bonus.getY())/ game.getTrackTileSize()) + 0.5D) * game.getTrackTileSize())-bonus.getY())>0.20* game.getTrackTileSize()) continue;
			if (angleToBonus > (0.1
					+ 1.7 * game.getTrackTileSize()/(distanceToWaypoint+((self.getDurability() < 0.5D &&bonus.getType() == BonusType.REPAIR_KIT)?1:3)*game.getTrackTileSize() )) )
				continue;
			if(distToBonus < self.getDistanceTo(bonus)||distanceToWaypoint<game.getTrackTileSize()||self.getDistanceTo(bonus)>(distanceToWaypoint-0.5*game.getTrackTileSize())) continue;
			if(myTargetX!=(int) (bonus.getX() / game.getTrackTileSize())&&myTargetY!=(int) (bonus.getY()/ game.getTrackTileSize())) continue;
				nextWaypointX = (int)bonus.getX();
				nextWaypointY = (int)bonus.getY();
				//if(isDebugMove) System.out.println(abs(self.getAngleTo(bonus) - self.getAngleTo(nextWaypointX,nextWaypointY)));
				distToBonus = self.getDistanceTo(bonus);

		}
//		nextWaypointX = (int)game.getBonusSize()*((int)nextWaypointX/(int)game.getBonusSize());nextWaypointY = (int)game.getBonusSize()*((int)nextWaypointY/(int)game.getBonusSize());
		distanceToWaypoint = self.getDistanceTo(nextWaypointX, nextWaypointY);	
		if(distToBonus>distanceToWaypoint) {angleToBonus=0;distToBonus=0;}
		//angleToWaypoint = (angleToBonus==0)?self.getAngleTo(nextWaypointX, nextWaypointY):(angleToWaypoint+2*angleToBonus)/3;	
		double angleToWaypoint =  self.getAngleTo(nextWaypointX, nextWaypointY);

		move.setWheelTurn(angleToWaypoint );//* distanceToWaypoint/game.getTrackTileSize() );// *
		move.setEnginePower(0.5+ distanceToWaypoint / game.getTrackTileSize()+((distToBonus>1d)?1:0));	
		if(abs(move.getWheelTurn())<0.01*PI) move.setWheelTurn(0.0); 		
																			// PI);

		// if(!toBonus&&speedModule>3D&&self.getDistanceTo(nextWaypointX,
		// nextWaypointY)<game.getTrackTileSize())
		// move.setEnginePower(-1);
		move.setBrake(false);
//		if (speedModule * speedModule * abs(angleToWaypoint) > 5D * 2.5D * PI)
//			move.setEnginePower(0.5d*move.getEnginePower());//move.setBrake(true);
//		move.setEnginePower(abs(1-0.5d*abs(angleToWaypoint))*move.getEnginePower());

		//
		// ReverceMode
//		if(ReverceMode>0&&ReverceMode<100&&(abs(move.getWheelTurn())==0||(int)speedModule==0)) ReverceMode=0;

		if (self.getDurability()>0 &&ReverceMode[carId] <= -20 && move.getEnginePower() > 0 && speedModule <3D && world.getTick() > 200){
			//myLastRealX=self.getX();myLastRealY=self.getY();
			ReverceMode[carId] += 150;//+50*abs(distanceToWaypoint/game.getTrackTileSize());
		}
			
		if (self.getDurability()>0 &&(ReverceMode[carId] > -100||speedModule < 3D)&& world.getTick() > 200) {
			ReverceMode[carId]--;
			//if(ReverceMode>100||self.getDistanceTo(nextRealX,nextRealY)>game.getTrackTileSize())
			//	ReverceMode=0;
			//move.setEnginePower(1);
			// move.setWheelTurn(-1*move.getWheelTurn());
			// move.setBrake(true);
		}
		if(ReverceMode[carId]<0&&speedModule>1d) 
		ReverceMode[carId]=0;
	//	if(ReverceMode>0&&self.getDistanceTo(myLastRealX,myLastRealY)>0.25d*game.getTrackTileSize()) 
	//12	ReverceMode=0;

		if (ReverceMode[carId] > 40) {
			
			// move.setEnginePower(-1.0D);
			move.setWheelTurn(-10 * move.getWheelTurn());
//			move.setBrake(true);
			//if(ReverceMode <150&&speedModule < 5){move.setEnginePower(1.0D); ReverceMode=50;}
		}
		if (ReverceMode[carId] > 50) {

			move.setEnginePower(-1.0D);
			// move.setWheelTurn(-1*move.getWheelTurn());
			move.setBrake(false);
		}
	//	if(ReverceMode<100&&(int)speedModule==0) {move.setEnginePower(1);}
		if(ReverceMode[carId]>0&&abs(angleToWaypoint)>1D&&distToBonus==0 ) move.setEnginePower(0.3*move.getEnginePower());
		if(myTargetX!=targetX&&myTargetY!=targetY) move.setWheelTurn(50*move.getWheelTurn());
		DreeftMode[carId] = (1==abs(myTargetX-targetX)&&1==abs(myTargetY-targetY)&&distToBonus==0&&speedModule>15d&&ReverceMode[carId]<1&&abs(driftAngle[carId]-at)>0.5)?15:0;
		
        if(abs(angleToWaypoint)>1&&speedModule>5d) move.setEnginePower(0);
		move.setBrake(((distToBonus>0)?false:speedModule>19D&&dt<=1.5  *game.getTrackTileSize()||speedModule>35D&&dt<=2.0  *game.getTrackTileSize()
				||speedModule>10D&&(
						abs(angleToNextWaypoint) > 0.9//world.getTilesXY()[targetX][targetY]==TileType.LEFT_BOTTOM_CORNER&&world.getTilesXY()[myTargetX][myTargetY]==TileType.RIGHT_TOP_CORNER
						||false)));		
		move.setEnginePower(((move.isBrake())?0:move.getEnginePower()));
		move.setUseNitro(!move.isBrake() && abs(angleToNextWaypoint) < 0.2 && useNitro
				&&(speedModule < 10 &&  distanceToWaypoint > 4* game.getTrackTileSize()
				||speedModule < 20 &&  distanceToWaypoint > 5 * game.getTrackTileSize()));		
		//  if(isDebugMove&&(prevTX!=targetX||prevTY!=targetY||prevMX!=myTargetX||prevMY!=myTargetY||prevTXR!=nextWaypointX||prevTYR!=nextWaypointY))//&&(nextRealX!=nextWaypointX||nextRealY!=nextWaypointY))
		{
			System.out.println(""+ReverceMode[carId]+" MP ("+myTargetX+", "+myTargetY+ ") " +" NP ("+targetX+", "+targetY+ ") "+" power="+(int)(100*self.getEnginePower())+" speed ="+(int)speedModule+"\t n="+move.isUseNitro()+" b="+move.isBrake()+"\t DTWP "+(int)distanceToWaypoint +"\t"+(int)(100*angleToWaypoint)+"\t distToBonus = "+(int)distToBonus+"\t  "+(int)(100*angleToBonus)+"\t at = "+(int)(100*at) +" dt = "+(int)dt+"\t "+" from "+(int)self.getX()+", "+(int)self.getY()+" to "+nextWaypointX+", "+nextWaypointY);// +" "+world.getTilesXY()[targetX][targetY]);
		}
		prevTX[carId]=targetX;prevTY[carId]=targetY;prevMX[carId]=myTargetX;prevMY[carId]=myTargetY; prevTXR[carId]=nextWaypointX;prevTYR[carId]=nextWaypointY;
	}

	private void fillMapCarRacing2015(World world, AStar aStar, Game game,Car self) {
//		for (Car car : world.getCars()) {
//			//if (car.equals(s))	continue;
//			aStar.cellList.add(new Cell((int)(car.getX()/game.getTrackTileSize()*5),(int)(car.getY()/game.getTrackTileSize()*5), false));
//		}
		
		for (int x = 0; x < world.getWidth(); x++) {
			for (int y = 0; y < world.getHeight(); y++) {
				switch (world.getTilesXY()[x][y]) {
				case VERTICAL:
					fillMap(aStar, x, y,new boolean[]{	false,true,true,true,false,
														false,true,true,true,false,
														false,true,true,true,false,	
														false,true,true,true,false,
														false,true,true,true,false});

					break;
				case HORIZONTAL:
					fillMap(aStar, x, y,new boolean[]{	false,false,false,false,false,
														true,true,true,true,true,
														true,true,true,true,true,
														true,true,true,true,true,
														false,false,false,false,false});

					break;
				case LEFT_TOP_CORNER:
					fillMap(aStar, x, y,new boolean[]{	false,false,false,false,false,
														false,true,true,true,true,
														false,true,true,true,true,
														false,true,true,true,true,
														false,true,true,true,false});

					break;
				case RIGHT_TOP_CORNER:
					fillMap(aStar, x, y,new boolean[]{	false,false,false,false,false,
														true,true,true,true,false,
														true,true,true,true,false,
														true,true,true,true,false,
														false,true,true,true,false});

					break;
				case LEFT_BOTTOM_CORNER:
					fillMap(aStar, x, y,new boolean[]{	false,true,true,true,false,
														false,true,true,true,true,
														false,true,true,true,true,
														false,true,true,true,true,
														false,false,false,false,false});

					break;
				case RIGHT_BOTTOM_CORNER:
					fillMap(aStar, x, y,new boolean[]{	false,true,true,true,false,
														true,true,true,true,false,
														true,true,true,true,false,
														true,true,true,true,false,
														false,false,false,false,false});

					break;
				case LEFT_HEADED_T:
					fillMap(aStar, x, y,new boolean[]{	false,true,true,true,false,
														true,true,true,true,false,
														true,true,true,true,false,
														true,true,true,true,false,
														false,true,true,true,false});

					break;
				case RIGHT_HEADED_T:
					fillMap(aStar, x, y,new boolean[]{	false,true,true,true,false,
														false,true,true,true,true,
														false,true,true,true,true,
														false,true,true,true,true,
														false,true,true,true,false});

					break;
				case TOP_HEADED_T:
					fillMap(aStar, x, y,new boolean[]{	false,true,true,true,false,
														true,true,true,true,true,
														true,true,true,true,true,
														true,true,true,true,true,
														false,false,false,false,false});

					break;
				case BOTTOM_HEADED_T:
					fillMap(aStar, x, y,new boolean[]{	false,false,false,false,false,
														true,true,true,true,true,
														true,true,true,true,true,
														true,true,true,true,true,
														false,true,true,true,false});

					break;
				case CROSSROADS:
				case UNKNOWN
:
					fillMap(aStar, x, y,new boolean[]{	false,true,true,true,false,
														true,true,true,true,true,
														true,true,true,true,true,
														true,true,true,true,true,
														false,true,true,true,false});

					break;
				default:
					fillMap(aStar, x, y,new boolean[]{	false,false,false,false,false,
														false,false,false,false,false,
														false,false,false,false,false,
														false,false,false,false,false,
														false,false,false,false,false});
					break;	
				}
				
			}
		}
		for (Car car : world.getCars()) {
			if (car.getId()==self.getId())	continue;
			aStar.cellList.add(new Cell((int)(car.getX()/game.getTrackTileSize()*5),(int)(car.getY()/game.getTrackTileSize()*5), true));
			aStar.cellList.add(new Cell((int)(car.getX()/game.getTrackTileSize()*5)+1,(int)(car.getY()/game.getTrackTileSize()*5), true));
			aStar.cellList.add(new Cell((int)(car.getX()/game.getTrackTileSize()*5)-1,(int)(car.getY()/game.getTrackTileSize()*5), true));
			aStar.cellList.add(new Cell((int)(car.getX()/game.getTrackTileSize()*5),(int)(car.getY()/game.getTrackTileSize()*5)+1, true));
			aStar.cellList.add(new Cell((int)(car.getX()/game.getTrackTileSize()*5),(int)(car.getY()/game.getTrackTileSize()*5)-1 , true));
		}

	}
	private void fillMap(AStar aStar,int x, int y,boolean map[]) {
		for (int i=0;i<5;i++) for (int j=0;j<5;j++) aStar.cellList.add(new Cell(x*5+j, y*5+i, !map[i*5+j]));
	}
}

class Cell {
	/**
	 * Создает клетку с координатами x, y.
	 * 
	 * @param blocked
	 *            является ли клетка непроходимой
	 */
	public Cell(int x, int y, boolean blocked) {
		this.x = x;
		this.y = y;
		this.blocked = blocked;
	}

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
		this.blocked = false;
	}

	/**
	 * Функция вычисления манхеттенского расстояния от текущей клетки до finish
	 * 
	 * @param finish
	 *            конечная клетка
	 * @return расстояние
	 */
	public int mandist(Cell finish) {
		return 10 * (Math.abs(this.x - finish.x) + Math.abs(this.y - finish.y));
	}

	/**
	 * Вычисление стоимости пути до соседней клетки finish
	 * 
	 * @param finish
	 *            соседняя клетка
	 * @return 10, если клетка по горизонтали или вертикали от текущей, 14, если
	 *         по диагонали (это типа 1 и sqrt(2) ~ 1.44)
	 */
	public int price(Cell finish) {
		if (this.x == finish.x || this.y == finish.y) {
			return 10;
		} else {
			return 14;
		}
	}

	/**
	 * Устанавливает текущую клетку как стартовую
	 */
	public void setAsStart() {
		this.start = true;
	}

	/**
	 * Устанавливает текущую клетку как конечную
	 */
	public void setAsFinish() {
		this.finish = true;
	}

	/**
	 * Сравнение клеток
	 * 
	 * @param second
	 *            вторая клетка
	 * @return true, если координаты клеток равны, иначе - false
	 */
	public boolean equals(Cell second) {
		return (this.x == second.x) && (this.y == second.y);
	}

	/**
	 * Красиво печатаем * - путь (это в конце) + - стартовая или конечная # -
	 * непроходимая . - обычная
	 * 
	 * @return строковое представление клетки
	 */
	public String toString() {
		if (this.road) {
			return " * ";
		}
		if (this.start || this.finish) {
			return " + ";
		}
		if (this.blocked) {
			return " # ";
		}
		return " . ";
	}

	public int x = -1;
	public int y = -1;
	public Cell parent = this;
	public Cell nextPoint = this;
	public boolean blocked = false;
	public boolean start = false;
	public boolean finish = false;
	public boolean road = false;
	public int F = 0;
	public int G = 0;
	public int H = 0;
}

class Table<T extends Cell> {
	/**
	 * Создаем карту игры с размерами width и height
	 */
	public Table(int width, int height) {
		this.width = width;
		this.height = height;
		this.table = new Cell[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				table[i][j] = new Cell(0, 0, false);
			}
		}
	}

	/**
	 * Добавить клетку на карту
	 */
	public void add(Cell cell) {
		table[cell.x][cell.y] = cell;
	}

	/**
	 * Получить клетку по координатам x, y
	 * 
	 * @return клетка, либо фейковая клетка, которая всегда блокирована (чтобы
	 *         избежать выхода за границы)
	 */
	@SuppressWarnings("unchecked")
	public T get(int x, int y) {
		if (x < width && x >= 0 && y < height && y >= 0) {
			return (T) table[x][y];
		}
		// а разве так можно делать в Java? оО но работает оО
		return (T) (new Cell(0, 0, true));
	}

	/**
	 * Печать всех клеток поля. Красиво.
	 */
	public void printp() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				System.out.print(this.get(j, i));
			}
			System.out.println();
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}

	public int width;
	public int height;
	private Cell[][] table;
}

class AStar {
	private int width = 0;
	private int height = 0;
	public Table<Cell> cellList = null;

	private Cell start = null;
	private Cell nextCell = null;
	private Cell finish = null;
	private boolean noroute = false;

	public void setStartCell(int x, int y) {
		// Стартовая и конечная
		cellList.get(x, y).setAsStart();
		start = cellList.get(x, y);
		start.F = 10000;

	}

	public void setFinishCell(int x, int y) {
		// Стартовая и конечная
		cellList.get(x, y).setAsFinish();
		setFinish(cellList.get(x, y));
	}

	public Cell nextCell() {
		return nextCell;
	}

	AStar(int width, int height) {
		this.height = height;
		this.width = width;
		cellList = new Table<Cell>(this.width, this.height);

	}

	public void calculateRoute(Cell start, Cell end) {
		boolean found = false;
		nextCell = null;
		noroute = false;
		setStartCell(start.x, start.y);
		setFinishCell(end.x, end.y); // Фух, начинаем

		LinkedList<Cell> openList = new LinkedList<Cell>();
		LinkedList<Cell> closedList = new LinkedList<Cell>();
		LinkedList<Cell> tmpList = new LinkedList<Cell>();
		// 1) Добавляем стартовую клетку в открытый список.
		openList.push(start);

		// 2) Повторяем следующее:
		while (!found && !noroute) {
			// a) Ищем в открытом списке клетку с наименьшей стоимостью F.
			// Делаем ее текущей клеткой.
			Cell min = openList.getFirst();
			for (Cell cell : openList) {
				// тут я специально тестировал, при < или <= выбираются разные
				// пути,
				// но суммарная стоимость G у них совершенно одинакова. Забавно,
				// но так и должно быть.
				if (cell.F < min.F)
					min = cell;
			}

			// b) Помещаем ее в закрытый список. (И удаляем с открытого)
			closedList.push(min);
			openList.remove(min);
			// System.out.println(openList);

			// c) Для каждой из соседних 8-ми клеток ...
			tmpList.clear();
//			tmpList.add(cellList.get(min.x, min.y - 1));
//			tmpList.add(cellList.get(min.x + 1, min.y));
//			tmpList.add(cellList.get(min.x, min.y + 1));
//			tmpList.add(cellList.get(min.x - 1, min.y));
			tmpList.add(cellList.get(min.x - 1, min.y - 1));
			tmpList.add(cellList.get(min.x, min.y - 1));
			tmpList.add(cellList.get(min.x + 1, min.y - 1));
			tmpList.add(cellList.get(min.x + 1, min.y));
			tmpList.add(cellList.get(min.x + 1, min.y + 1));
			tmpList.add(cellList.get(min.x, min.y + 1));
			tmpList.add(cellList.get(min.x - 1, min.y + 1));
			tmpList.add(cellList.get(min.x - 1, min.y));

			for (Cell neightbour : tmpList) {
				// Если клетка непроходимая или она находится в закрытом списке,
				// игнорируем ее. В противном случае делаем следующее.
				if (neightbour.blocked || closedList.contains(neightbour))
					continue;

				// Если клетка еще не в открытом списке, то добавляем ее туда.
				// Делаем текущую клетку родительской для это клетки.
				// Расчитываем стоимости F, G и H клетки.
				if (!openList.contains(neightbour)) {
					openList.add(neightbour);
					neightbour.parent = min;
					neightbour.H = neightbour.mandist(getFinish());
					neightbour.G = neightbour.price(min);
					neightbour.F = neightbour.H + neightbour.G;
					continue;
				}
				// Если клетка уже в открытом списке, то проверяем, не дешевле
				// ли будет путь через эту клетку. Для сравнения используем
				// стоимость G.
				if (neightbour.F < min.F // + neightbour.price(min)
				) {
					// Более низкая стоимость G указывает на то, что путь будет
					// дешевле. Эсли это так, то меняем родителя клетки на
					// текущую клетку и пересчитываем для нее стоимости G и F.
					neightbour.parent = min.parent; // вот тут я честно хз, надо
													// ли min.parent или нет,
													// вроде надо
					neightbour.H = neightbour.mandist(getFinish());
					neightbour.G = neightbour.price(min);
					neightbour.F = neightbour.H + neightbour.G;
				}

				// Если вы сортируете открытый список по стоимости F, то вам
				// надо отсортировать свесь список в соответствии с изменениями.
			}

			// d) Останавливаемся если:
			// Добавили целевую клетку в открытый список, в этом случае путь
			// найден.
			// Или открытый список пуст и мы не дошли до целевой клетки. В этом
			// случае путь отсутствует.

			if (openList.contains(getFinish())) {
				found = true;
			}

			if (openList.isEmpty()) {
				noroute = true;
			}
		}

		// 3) Сохраняем путь. Двигаясь назад от целевой точки, проходя от каждой
		// точки к ее родителю до тех пор, пока не дойдем до стартовой точки.
		// Это и будет наш путь.
		if (!noroute) {
			Cell rd = getFinish().parent;
			while (!rd.equals(start)) {
				rd.road = true;
				nextCell = rd;
				rd = rd.parent;
				rd.nextPoint = nextCell;
				if (rd == null)
					break;
			}
			// Найдем теперь саму дальнюю
			if(nextCell.x==nextCell.nextPoint.x){
				while (!nextCell.equals(getFinish())&&!nextCell.equals(nextCell.nextPoint)) {
					if (nextCell.nextPoint == null||nextCell.nextPoint.x!=nextCell.x)
						break;
					nextCell = nextCell.nextPoint;
				}
			}
			else
			{
				while (!nextCell.equals(getFinish())&&!nextCell.equals(nextCell.nextPoint)) {
					if (nextCell.nextPoint == null||nextCell.nextPoint.y!=nextCell.y)
						break;
					nextCell = nextCell.nextPoint;
				}
				
			}
			// cellList.printp();
		} else {
			// System.out.println("NO ROUTE");
		}

	}

	public void printRoute() {
		cellList.printp();
		if (!noroute) {

		} else {
			System.out.println("NO ROUTE");
		}

	}

	public static void main(String[] args) {
		// Создадим все нужные списки
		AStar aStar = new AStar(10, 10);
		Table<Cell> blockList = new Table<Cell>(10, 10);
		// Создадим преграду
		blockList.add(new Cell(3, 2, true));
		blockList.add(new Cell(5, 2, true));
		blockList.add(new Cell(4, 2, true));
		blockList.add(new Cell(4, 3, true));
		blockList.add(new Cell(4, 4, true));
		blockList.add(new Cell(4, 5, true));
		blockList.add(new Cell(4, 6, true));
		blockList.add(new Cell(4, 7, true));
		blockList.add(new Cell(3, 7, true));
		// blockList.add(new Cell(2, 7, true));
		blockList.add(new Cell(1, 7, true));
		blockList.add(new Cell(0, 7, true));
		// Заполним карту как-то клетками, учитывая преграду
		for (int i = 0; i < aStar.width; i++) {
			for (int j = 0; j < aStar.height; j++) {
				aStar.cellList.add(new Cell(j, i, blockList.get(j, i).blocked));
			}
		}

		aStar.calculateRoute(new Cell(2, 4), new Cell(7, 8));
		aStar.printRoute();
		System.out.println(aStar.nextCell().x + " " + aStar.nextCell().y);

	}

	public Cell getFinish() {
		return finish;
	}

	public void setFinish(Cell finish) {
		this.finish = finish;
	}
}