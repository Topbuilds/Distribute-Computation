
public class ShuttleStationMon extends Object {
	public static void main(String[] args) throws InterruptedException {
		int numShuttle = 7;
		int numRecharge = 3;
		int numTrack = 3;
		int numCruiseEachShuttle = 3;
		int runTime = 30; // seconds
		System.out.println("ShuttleStationMon: numShuttle=" + numShuttle
		         + ", numRecharge=" + numRecharge
		         + ", numTrack=" + numTrack + ", numCruiseEachShuttle=" + numCruiseEachShuttle);
		
		// create ShuttleStation
		ShuttleStation ss = new ShuttleStation(numRecharge, numTrack, numShuttle);
		
		for(int i=0;i<numShuttle; i++)
		{
			new Shuttle(ss, i, numCruiseEachShuttle);
		}
		
		
	    //System.out.println("All threads started");
	    
	    // let them run for a while
	    Thread.sleep(runTime*1000);
	    System.out.println(
	       "time to stop the threads and exit");
	    System.exit(0);
	}

}
