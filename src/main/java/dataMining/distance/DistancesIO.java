package dataMining.distance;
import java.io.Closeable;
import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import util.FileUtils;
import util.Logs;
import util.StringUtils;
import util.TimeWatcher;
import util.TriConsumer;

public class DistancesIO implements Closeable {

	private final DecimalFormat decimalFormatter = StringUtils.getDecimalFormatter();
	private final PrintStream out;

	public DistancesIO(File outputFile) {
		out = FileUtils.createPrintStreamToFile(outputFile);
	}

	public synchronized void addDistance(Long idB, double distance) {
		out.println(idB + " " + decimalFormatter.format(distance));
	}

	@Override
	public void close() {
		out.close();
	}

	public static void getDistancesForSample(File dir, int numSamples, long queryId) {
        Logs.fine("loading distances for id "+queryId);
        TimeWatcher time = new TimeWatcher();
        Map<Long,Float> distances = new HashMap<>();
        FileUtils.forEachFileWithinFolder_interruptable(dir, false, distancesFile -> {
            long id_file = Long.parseLong(distancesFile.getName());
            try( Scanner scanner = FileUtils.createScannerFromFile(distancesFile) ){
                if(queryId == id_file){
                    while( scanner.hasNext() ){
                        long id_file_inside = scanner.nextLong();
                        float dist = Float.parseFloat(scanner.next());
                        distances.put(id_file_inside, dist);
                    }
                    if(distances.size() >= numSamples - 1)
                        return false;
                }else{
                    while( scanner.hasNext() ){
                        long id_file_inside = scanner.nextLong();
                        float dist = Float.parseFloat(scanner.next());
                        if(queryId == id_file_inside){
                            distances.put(id_file, dist);
                            if(distances.size() >= numSamples - 1)
                                return false;
                            break;
                        }
                    }
                }
            }
            return true;
        });
        Logs.fine(distances.size()+" distances loaded for id "+queryId + ", after "+time);
    }

	/**
	 * Traverses all distance entries and provides them to a consumer.
	 * WARNINGS:
	 * 1: given d(A,B) and d(B,A), only one of them is returned, without any guarantee of which it will be.
	 * 2: all d(A,A) will not be provided, so assume them 0.
	 */
	public static void forEachEntry(File dir, TriConsumer<Long,Long,Float> consumer) {
		AtomicLong count = new AtomicLong();
		FileUtils.forEachFileWithinFolder(dir, false, distancesFile -> {
			long id_file = Long.parseLong(distancesFile.getName());
            try( Scanner scanner = FileUtils.createScannerFromFile(distancesFile) ){
                while( scanner.hasNext() ){
                    long id_file_inside = scanner.nextLong();
                    float dist = Float.parseFloat(scanner.next());
                    consumer.accept(id_file, id_file_inside, dist);
                }
            }
            Logs.finest(distancesFile+" loaded. count: "+count.incrementAndGet());
		});
	}
}
