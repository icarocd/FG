package util;

import java.util.logging.Level;

public class MemoryUtils {

	public static void log() {
		Logs.finest(buildLog());
	}
	public static void log(long max, long inUse) {
		Logs.finest(buildLog(max, inUse));
	}
	public static String buildLog(){
		Runtime r = Runtime.getRuntime();
		long inUse = inUse(r);
        long max = r.maxMemory();
    	// max (total allocable):           r.maxMemory()
        // total (currently allocated):     r.totalMemory()
        // free (from current allocation):  instance.freeMemory()
        // inUse:                           total - free
        // max free:                        max - inUse = max - total + free
		return buildLog(max, inUse);
	}
	public static String buildLog(long max, long inUse) {
        return new StringBuilder("Memory use (MB): ").append(MathUtils.bytesToMB(inUse)).append(" of ").append(MathUtils.bytesToMB(max)).toString();
	}

	private static long inUse(Runtime runtime) {
		return runtime.totalMemory() - runtime.freeMemory();
	}

	private static long getMaxFreeMemoryInMB(long max, long inUse) {
		return MathUtils.bytesToMB(max - inUse);
	}

	public static boolean isEnoughFreeMemory(long thresholdInMB){
		Runtime r = Runtime.getRuntime();
        long max = r.maxMemory();
		long inUse = inUse(r);
		long maxFreeMB = getMaxFreeMemoryInMB(max, inUse);

		boolean enough = maxFreeMB >= thresholdInMB;
		if(!enough) //since memory is a crucial factor, we prefer to log its status when it becomes not enough
			log(max, inUse);
		return enough;
	}

    public static void main(String[] args) {
        Logs.init(Level.FINEST);
        log();
        System.out.println(isEnoughFreeMemory(2048));
    }
}
