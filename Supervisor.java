
public class Supervisor extends Object implements Runnable{
	private ShuttleStation ss = null;
	public boolean sup_running = true;
	public boolean busy = false;
	public String getName(){return "Supervisor";}
	public static long time = System.currentTimeMillis();
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	public Supervisor(ShuttleStation ss)
	{
		this.ss = ss;
		new Thread(this).start();
	}
	
	public void run()
	{
		int napping = 0;
		while(sup_running)
		{
			synchronized(this){
				while(this.ss.waitingSupervisorShuttles.size()==0){
					try {
						this.busy = false;
						msg("waiting");
						this.wait(); // wait to move shuttle group to landing area or refill
						break;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				this.busy = true;
			}
		}
	}
}
