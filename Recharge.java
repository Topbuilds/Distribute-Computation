
public class Recharge extends Object implements Runnable{
	private ShuttleStation ss = null;
	private int ID;
	public int reservoir = 200;
	public boolean charging = false;
	public String getName(){return "Recharger" + Integer.toString(this.ID);}
	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	
	public Recharge(ShuttleStation ss, int ID)
	{
		this.ss = ss;
		this.ID = ID;
		
		new Thread(this).start();
	}
	
	public void run()
	{
		
	}
}
