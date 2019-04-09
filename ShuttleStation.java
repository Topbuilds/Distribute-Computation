import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
public class ShuttleStation extends Object {
	private Controller con = null;
	private Supervisor sup = null;
	private int reservoir = 200;
	private int availRecharge = 0;
	private int availTrack = 0;
	private ReentrantLock rec_lock = new ReentrantLock();
	private ReentrantLock reser_lock = new ReentrantLock();

	public boolean rechargeStationOpen = false;
	public int numShuttle = 0;
	public Vector<Shuttle> waitingRechargeShuttles = new Vector<Shuttle>();
	public ShuttleGroup waitingRechargeGroup = null;
	public Vector<Shuttle> waitingControllerRefill = new Vector<Shuttle>();
	public Vector<Shuttle> waitingControllerSignalToMove = new Vector<Shuttle>();
	public Vector<Shuttle> waitingTrackShuttles = new Vector<Shuttle>();
	public Vector<Shuttle> waitingSupervisorShuttles = new Vector<Shuttle>();
	
	public Controller getController() {return this.con;}
	public Supervisor getSupervisor() {return this.sup;}
	public boolean getRechargeStationStatus() {return this.rechargeStationOpen;}
	
	public String getName(){return "ShuttleStation";}
	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	
	public ShuttleStation(int numRecharge, int numTrack, int numShuttle) 
	{
		// create the Supervisor, Controller (with self-starting threads)
		this.sup = new Supervisor(this);
		this.con = new Controller(this);
		this.waitingRechargeGroup = new ShuttleGroup(numRecharge);
		
		// init number of Recharge and Track
		this.availRecharge = numRecharge;
		this.availTrack = numTrack;	
		this.numShuttle = numShuttle;
		
	}
	
	public void finishedCruise()
	{	
		numShuttle--;
		if(numShuttle<=0)
		{
			msg("All shuttles finished cruise. Today is end.");
			this.con.con_running = false;
			this.sup.sup_running = false;
		}
		
	}
	
	public void moveToRecharge(Shuttle s) 
	{
		synchronized(s)
		{
			while(cannotMoveToRechargeNow(s))
				while(true)
				{
					try{		
					//synchronized(con){this.con.notify();}
					s.msg("wait to move to recharge");
					s.wait(); break;}
					catch (InterruptedException e) {
						// race condition check
						if(!cannotMoveToRechargeNow(s))
							break; 
						else
							continue; }		
				}
		}		
		

		this.rec_lock.lock();
		this.availRecharge--;
		this.rec_lock.unlock();
		
		s.msg("moved to recharge. Available recharge: " + Integer.toString(availRecharge));
	}
	
	private synchronized boolean cannotMoveToRechargeNow(Shuttle s)
	{
		boolean status;
		if(this.availRecharge <= 0)
		{
			this.waitingRechargeShuttles.addElement(s);
			status = true;
		}
		else
			status = false;

		return status;
	}
	
	public void OpenRechargeStation() 
	{
		this.rechargeStationOpen = true;
		
		
		this.waitingRechargeGroup.notifyAllInGroup();
				
	}
	
	public void ReFillReservoir() 
	{
		this.reservoir = 200;
		
		while(this.waitingControllerRefill.size()>0)
		{
			synchronized(waitingControllerRefill.elementAt(0))
			{
				waitingControllerRefill.elementAt(0).notify();
			}
			waitingControllerRefill.removeElementAt(0);
		}
	}
	
	private boolean notOpenRechargeNow(Shuttle s)
	{
		boolean status;
		if(this.rechargeStationOpen == false)
		{
			synchronized(waitingRechargeGroup){
				if(!this.waitingRechargeGroup.findShuttle(s.ID))
				{
					s.msg("add to waitingRechargeGroup");
					this.waitingRechargeGroup.addShuttle(s);
				}
			}
			status = true;
		}
		else
			status = false;
		
		return status;
	}
	
	private boolean checkIfEnoughFuel(Shuttle s)
	{
		boolean result = true;
		this.reser_lock.lock();
			if(this.reservoir < (100 - s.tank))
				result = false;

		this.reser_lock.unlock();
		
		return result;
	}
	public void reFillTank(Shuttle s)
	{
		// check if recharge station open
		synchronized(s)
		{
			while(notOpenRechargeNow(s))
				while(true) // wait to be notified, not interrupted
					try{s.msg("wait recharge open");s.wait(); break;
					}
					catch (InterruptedException e) {
						// race condition check
						if(!notOpenRechargeNow(s))
							break; 
						else
							continue; }		
		}
		s.msg("start to refill");
		// check if reservoir has enough capacity
		while(!checkIfEnoughFuel(s))
		{
			s.msg("wait controller to refill the recharge reservoir");
			
			synchronized(con)
			{
				this.waitingControllerRefill.addElement(s);
				this.con.notify(); // notify controller to assist
			}
			
			synchronized(s)
			{			
				while(true) // wait to be notified, not interrupted
					try
					{   if(!checkIfEnoughFuel(s))
							s.wait(); 
						break;
					}
					catch (InterruptedException e) {
						// race condition check
						if(checkIfEnoughFuel(s))
							break; 
						else
							continue; }		
			}
			
		}
		// shuttle s refill
		this.reser_lock.lock();		
		this.reservoir = this.reservoir - (100 - s.tank);
		s.tank = 100;	
		this.reser_lock.unlock();
		
		s.msg("refilled the tank to 100");
		msg("reservoir:" + Integer.toString(this.reservoir));
		
		finishRefillTank(s);
	}
	
	private synchronized boolean notlastFinishRefill(Shuttle s)
	{
		boolean status;
		synchronized(this.waitingControllerSignalToMove)
		{
			this.waitingControllerSignalToMove.addElement(s);
		}
		
		if(this.waitingRechargeGroup.allFinishRefill() == false)
		{			
			status = true;
		}
		else
		{
			synchronized(con)
				{this.con.notify();} // last finish refill Shuttle notifies controller
			status = false;
		}
		return status;
	}
	
	public void finishRefillTank(Shuttle s)
	{
		synchronized(s)
		{
			
			while(notlastFinishRefill(s))
				while(true) // wait to be notified, not interrupted
					try{s.msg("finished refill, need to wait controller signal to move to landing/takeoff area");
					
					s.wait(); break;}
					catch (InterruptedException e) {
						// race condition check
						if(!notlastFinishRefill(s))
							break; 
						else
							continue; }
			
		}
		
		// move to landing area for next taking off
		s.msg("controller signaled to move to landing/takeoff area");

		
		synchronized(waitingRechargeGroup){
			this.waitingRechargeGroup.removeShuttle(s);
		
			if(this.waitingRechargeGroup.getGroupShuttles().size() == 0)
			{
				msg("ShuttleGroup finished recharge, recharge staions are available now");
				this.rec_lock.lock();
				this.availRecharge = this.waitingRechargeGroup.numPerGroup; // reset availRecharge number
				this.rec_lock.unlock();
				
				msg("available Recharges: " + this.waitingRechargeGroup.numPerGroup);
				
				while(this.waitingRechargeShuttles.size()>0)
				{
					synchronized(waitingRechargeShuttles.elementAt(0))
					{
						waitingRechargeShuttles.elementAt(0).notify();
						waitingRechargeShuttles.elementAt(0).msg("notified to recharge station");
					}
					waitingRechargeShuttles.removeElementAt(0);
				}
			}
		}
	}
	
	public void landing(Shuttle s) 
	{
		// shuttle landed
		s.msg("landed.");
	}
	

	
	public void takeOff(Shuttle s)
	{
		synchronized(s)
		{
			while(cannotTakeOffNow(s))
				while(true) // wait to be notified, not interrupted
					try{s.msg("wait supervisor to author take off");s.wait(); break;}
					catch (InterruptedException e) {
						// race condition check
						if(!cannotTakeOffNow(s))
							break; 
						else
							continue; }
		}
		
		
		
		if(this.waitingSupervisorShuttles.size()>0)
		{
			synchronized(waitingSupervisorShuttles.elementAt(0))
			{
				waitingSupervisorShuttles.elementAt(0).notify();
			}
			waitingSupervisorShuttles.removeElementAt(0);
		}

		
		synchronized(this.waitingRechargeGroup){
			if(this.waitingRechargeGroup.getGroupShuttles().size() == 0)
			{
				if(this.waitingTrackShuttles.size()>0)
				{
					synchronized(waitingTrackShuttles.elementAt(0))
					{
						waitingTrackShuttles.elementAt(0).notify();
					}
					waitingTrackShuttles.removeElementAt(0);
				}
				
			}
		}
		
		s.msg("take off again");
	}
	
	private synchronized boolean cannotTakeOffNow(Shuttle s)
	{	
		boolean status;
		if(this.sup.busy == true)
		{
			//s.msg("need to wait supervisor to signal to take off");
			this.waitingSupervisorShuttles.addElement(s);
			status = true;
		}
		else
		{	
			status = false;
		}
		
		return status;
	}
}
