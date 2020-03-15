package dataMining.retrieval;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Scanner;
import com.google.common.base.Preconditions;
import util.ExceptionUtils;
import util.FileUtils;
import util.Logs;
import util.MathUtils;
import util.StringUtils;

public class QualityQueries {
    public float[] precisions;
    public float[] recalls;
    public float[] averagePrecisions;
    public float[] NDCGs;
    public float[] NS; //number of relevant items retrieved until the 4th result element
    public Float mAP;

    public QualityQueries(int numQueries) {
        precisions = new float[numQueries];
        recalls = new float[numQueries];
        averagePrecisions = new float[numQueries];
        NDCGs = new float[numQueries];
        NS = new float[numQueries];
    }
    private QualityQueries(float[] precisions, float[] recalls, float[] averagePrecisions, float[] NDCGs, float[] NS, Float mAP) {
		this.precisions = precisions;
		this.recalls = recalls;
		this.averagePrecisions = averagePrecisions;
		this.NDCGs = NDCGs;
		this.NS = NS;
		this.mAP = mAP;
	}

	public int getNumQueries(){
        return precisions.length;
    }

    public float NDCG() {
    	return MathUtils.mean(NDCGs);
    }

    public float NDCG(int queryIndex) {
        return NDCGs[queryIndex];
    }

    public float meanAveragePrecision() {
        return MathUtils.mean(averagePrecisions);
    }

    private float meanPrecision() {
        return MathUtils.mean(precisions);
    }

    private float meanRecall() {
        return MathUtils.mean(recalls);
    }

    private float meanFMeasure() {
        return MathUtils.mean(MathUtils.harmonicMeans(precisions, recalls));
    }

    public float meanNSScore(){
		return MathUtils.mean(NS);
	}

    public void save(File outputFile) {
        FileUtils.mkDirsForFile(outputFile);
        Logs.fine("Saving measures: " + outputFile);
        try(Writer out = FileUtils.createWriterToFile(outputFile)){
        	save(out);
        }catch(IOException e){ throw ExceptionUtils.asRuntimeException(e); }
    }
    public Writer save(OutputStream out, boolean includeDetail) {
    	return save(new OutputStreamWriter(out), includeDetail);
    }
    public Writer save(Writer out) {
    	return save(out, true);
    }
    public Writer save(Writer out, boolean includeDetail) {
        try{
			DecimalFormat decimalFormatter = StringUtils.getDecimalFormatter();
			out.write("#Line 1 indicates numQueries. Line 2 contains: NDCG@,AP@,mean P@,mean recall@,mean F-measure@,MAP,N-S");
			if(includeDetail)
				out.write(". Following lines: precisions,recalls,averagePrecisions,NDCGs,N-Ss");
			out.write("\n");
			out.write(String.valueOf(precisions.length));
			out.write("\n");
			out.write(decimalFormatter.format(NDCG()));
			out.write(",");
			out.write(decimalFormatter.format(meanAveragePrecision()));
			out.write(",");
			out.write(decimalFormatter.format(meanPrecision()));
			out.write(",");
			out.write(decimalFormatter.format(meanRecall()));
			out.write(",");
			out.write(decimalFormatter.format(meanFMeasure()));
			out.write(",");
			out.write(mAP != null ? decimalFormatter.format(mAP) : "not_computed");
			out.write(",");
			out.write(decimalFormatter.format(meanNSScore()));
			if(includeDetail){
				out.write("\n");
				printArray(out, decimalFormatter, precisions);
				printArray(out, decimalFormatter, recalls);
				printArray(out, decimalFormatter, averagePrecisions);
				printArray(out, decimalFormatter, NDCGs);
				printArray(out, decimalFormatter, NS);
			}
			out.flush();  // important!
			return out;
		}catch(IOException e){ throw ExceptionUtils.asRuntimeException(e); }
    }

    private void printArray(Writer out, DecimalFormat decimalFormatter, float[] values) throws IOException {
        for(int i = 0; i < values.length; i++){
            if(i > 0)
                out.write(",");
            out.write(decimalFormatter.format(values[i]));
        }
        out.write("\n");
    }

	public static QualityQueries parse(File evalFile){
		try(Scanner scanner = FileUtils.createScannerFromFile(evalFile)){
			scanner.nextLine(); //skip header
			int numQueries = Integer.parseInt(scanner.nextLine());
			String[] measures = scanner.nextLine().split(",");
			Preconditions.checkState(measures.length == 7);
			String mAP = measures[5];
			float[] precisions = readFloats(scanner.nextLine(), numQueries);
			float[] recalls = readFloats(scanner.nextLine(), numQueries);
			float[] averagePrecisions = readFloats(scanner.nextLine(), numQueries);
			float[] NDCGs = readFloats(scanner.nextLine(), numQueries);
			float[] NS = readFloats(scanner.nextLine(), numQueries);
			return new QualityQueries(precisions, recalls, averagePrecisions, NDCGs, NS, "not_computed".equals(mAP) ? null : Float.parseFloat(mAP));
		}
	}
	private static float[] readFloats(String line, int numQueries){
		String[] pieces = line.split(",");
		Preconditions.checkState(pieces.length == numQueries);
		return MathUtils.asFloatArrayPrimitive(pieces);
	}
}