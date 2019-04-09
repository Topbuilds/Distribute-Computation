import java.util.Vector;
public class ShuttleGroup extends Object{
	private Vector<Shuttle> vShuttles = new Vector<Shuttle>();
	public int numPerGroup = 0;
	public String getName(){return "ShuttleGroup";}
	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	
	public ShuttleGroup(int numRecharge)
	{
		numPerGroup = numRecharge;
	}
	
	public Vector<Shuttle> getGroupShuttles() {return vShuttles;}
	
	public synchronized boolean addShuttle(Shuttle s)
	{
		if(vShuttles.size()<numPerGroup)
		{
			vShuttles.add(s);
			return true;
		}
		else
			return false;
	}

	public synchronized boolean removeShuttle(Shuttle s)
	{
		if(vShuttles.size()>0)
		{
			vShuttles.removeElement(s);
			return true;
		}
		else
			return false;
	}
	
	public boolean findShuttle(int ID)
	{
		for(int i=0; i<vShuttles.size(); i++)
		{
			if(vShuttles.elementAt(i).ID == ID)
				return true;
		}
		
		return false;
	}
	
	public void notifyAllInGroup()
	{
		this.msg("group size: " + vShuttles.size());
		for(int i=0; i<vShuttles.size(); i++)
		{
			synchronized(vShuttles.elementAt(i)){
				vShuttles.elementAt(i).notify();
				vShuttles.elementAt(i).msg("waking up");
			}
		}
	}
	
	public synchronized boolean allFinishRefill()
	{
		boolean result = true;
		
		for(int i=0; i<vShuttles.size(); i++)
		{
			//msg(vShuttles.elementAt(i).getName() + ": " + Integer.toString(vShuttles.elementAt(i).tank));
			if(vShuttles.elementAt(i).tank != 100)
				result = false;
		}
		
		return result;
	}
}
