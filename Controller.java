
public class Controller extends Object implements Runnable{
	private ShuttleStation ss = null;
	
	public String getName(){return "Controller";}
	public boolean con_running = true;
	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	public Controller(ShuttleStation ss)
	{
		this.ss = ss;

		new Thread(this).start();
	}
	
	public void openRechargeStation()
	{
		ss.OpenRechargeStation();
	}
	
	public void reFillReservoir()
	{
		ss.ReFillReservoir();
	}

	
	public void run()
	{
		int napping = 0;
		while(con_running)
		{
			// wait to open recharge station
			napping = 10 + 1; // random from 11
			try {
				Thread.sleep(napping*100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			msg("open the recharge station");
			this.openRechargeStation(); // open the recharge station
			
			synchronized(this.ss.waitingRechargeGroup) {
				if(this.ss.waitingRechargeGroup.getGroupShuttles().size() == 0)
				{
					synchronized(this){
						while(this.ss.waitingRechargeGroup.getGroupShuttles().size() == 0){
							try {
								this.ss.waitingRechargeGroup.notifyAllInGroup();
								msg("waiting Shuttle Group");
								this.wait(); // waiting shuttle group
								break;
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					msg("there is new shuttle group");
				}
			}
			
			while(ss.rechargeStationOpen && this.ss.waitingRechargeGroup.allFinishRefill() == false)
			{ 
				synchronized(this){
					while(this.ss.waitingControllerRefill.size()==0){
						try {
							msg("wait to refill");
							this.wait(); // wait to move shuttle group to landing area or refill
							break;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				// refilling time
				msg("refilling the reservoir");
				napping = (int)(Math.random()*5 + 1); // random from 1 to 5
				try {
					Thread.sleep(napping*10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				msg("refilled the reservoir to 200");
				this.reFillReservoir(); // refilled the reservoir

						
			}
			
			if(this.ss.waitingRechargeGroup.allFinishRefill() == true)
			{
				// notify all Shuttle in group to wake up
				msg("ShuttleGroup finish refill, controller notifies all Shuttles in group to wake up");
				while(ss.waitingControllerSignalToMove.size()>0)
				{
					synchronized(ss.waitingControllerSignalToMove.elementAt(0))
					{
						ss.waitingControllerSignalToMove.elementAt(0).notify();
					}
					ss.waitingControllerSignalToMove.remove(0);
				}
				ss.rechargeStationOpen = false; // close recharge station
				msg("recharge station closed");
			}
		}
		
	}

}
