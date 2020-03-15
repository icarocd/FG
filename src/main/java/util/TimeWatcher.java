package util;

public class TimeWatcher {

    private long startTime;

    public TimeWatcher(long startTime) {
    	this.startTime = startTime;
    }
    public TimeWatcher() {
		start();
	}

    public void start() {
        startTime = System.nanoTime();
    }

    public long getTimeInNanoSecs() {
        return System.nanoTime() - startTime;
    }

    public long getTimeInMiliSecs() {
        long nanoTime = getTimeInNanoSecs();
        return DateUtil.nanosToMilis(nanoTime);
    }

    public long getTimeInMicroSecs() {
        long nanoTime = getTimeInNanoSecs();
        return DateUtil.nanosToMicros(nanoTime);
    }

    public long getTimeInSecs() {
    	return DateUtil.nanosToSecs(getTimeInNanoSecs());
    }

    /**
     * Indicates if spent time >= secs, returning true and reseting timer if that happens
     * Thread-safe due eventual parallel calls.
     */
    public synchronized boolean checkSecondsSpent(int secs) {
        if (getTimeInSecs() >= secs) {
            start();
            return true;
        }
        return false;
    }
    public synchronized boolean checkSecondsSpent(){
    	return checkSecondsSpent(60);
    }

    public float getTimeInMinutes() {
        return getTimeInSecs() / 60F;
    }

    public String getTime() {
    	return DateUtil.getTime(getTimeInNanoSecs());
    }

    public String toString() {
        return getTime();
    }
}
