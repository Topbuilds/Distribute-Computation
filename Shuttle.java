import java.util.Random;
public class Shuttle extends Object implements Runnable{
	private ShuttleStation ss = null;
	public int ID = 0;
	public int tank = 0;
	public int numCruises = 0;
	public String getName(){return "Shuttle" + Integer.toString(this.ID);}
	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	public Shuttle(){}
	public Shuttle(ShuttleStation ss, int ID, int numCruises)
	{
		this.ss = ss;
		this.ID = ID;
		this.numCruises = numCruises;
		// Generate a random number between 50 and 100
		Random rand = new Random();
		tank = rand.nextInt(50) + 50;
		
		new Thread(this).start();
	}
	
	public void run()
	{
		int napping = 0;
		while(true)
		{
			napping = (int)(Math.random()*10 + 1); // random from 1 to 10
			msg("crusing");
			// crusing
			try {
				Thread.sleep(napping*10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			tank = (int)(Math.random()*50 + 1); // random from 1 to 50
			msg("tank:"+ tank);
			// landing
			this.ss.landing(this);
			
			this.numCruises--;
			msg("Remain cruises times:" + this.numCruises);
			
			if(this.numCruises == 0)
			{
				msg("finished today's cruises." );
				this.ss.finishedCruise();
				break;
			}
			
			// shuttle is going to move to recharge
			this.ss.moveToRecharge(this);
			
			// shuttle is going to refull tank
			this.ss.reFillTank(this);
			
			// wait to next take off
			napping = (int)(Math.random()*10 + 1); // random from 1 to 10
			
			try {
				Thread.sleep(napping*10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			// takeoff
			msg("going to take off area");
			this.ss.takeOff(this);
		}
	}


}
