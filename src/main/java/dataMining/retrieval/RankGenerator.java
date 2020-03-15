package dataMining.retrieval;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import dataMining.Sample;
import dataMining.distance.DistancesIO;
import dataMining.distance.SampleDistanceMeasurer;
import util.Logs;
import util.MathUtils;
import util.Pair;
import util.TimeWatcher;

public class RankGenerator {

    /** generated ranks considering samples as both queries and responses */
    public static <T extends Sample> Pair<T,RankedList>[] generateRanks(ArrayList<T> samples, SampleDistanceMeasurer<T> similarityFunction, int rankSizeLimit,
		boolean samplesCanBeDestroyed, boolean normalize, File outputDir)
    {
        final int numRanks = samples.size();
        Preconditions.checkArgument(numRanks > 0);

        Logs.fine("Creating " + numRanks + " ranks, using similarity "+similarityFunction+", limit "+rankSizeLimit+(outputDir==null?"":", on "+outputDir)+" ...");
        TimeWatcher timeWatcher = new TimeWatcher();

        //sort the samples so the ranks and therefore the quality measurements are comparable:
        Collections.sort(samples, (a,b) -> Long.compare(a.getId(), b.getId()));

        Pair<T,RankedList>[] ranks = new Pair[numRanks];
        for (int i = 0; i < numRanks; i++)
            ranks[i] = new Pair<>(samples.get(i), new RankedList(rankSizeLimit, true));

        if(samplesCanBeDestroyed){ //destroy original dataset object to save some memory:
            samples.clear();
            samples.trimToSize();
            samples = null;
        }

        TimeWatcher logPooler = new TimeWatcher(0);
        for (int i = 0; i < numRanks; i++) {
            Pair<T,RankedList> rank = ranks[i];
            T sample = rank.getA();
            if(logPooler.checkSecondsSpent(30))
                Logs.finest("Creating ranks... now on index " + i);

            //como as listas sao montadas para amostras que sao tanto queries quanto retornos de consulta (i.e. dev x dev), colocamos a propria amostra como retorno da lista dela e tambem evitamos recomputo de d(A,B) e d(B,A) supondo medida simétrica
            rank.getB().add(sample.getId(), 1);
            MathUtils.forRange(i + 1, numRanks, true, j -> {
                Pair<T,RankedList> anotherRank = ranks[j];
                T anotherSample = anotherRank.getA();
                float similarity = similarityFunction.getSimilarity(sample, anotherSample);
                rank.getB().add(anotherSample.getId(), similarity);
                anotherRank.getB().add(sample.getId(), similarity);
            });
        }
        Logs.finer("Ranks created after " + timeWatcher);

        if(normalize){
            for(Pair<T,RankedList> sampleAndRankedList : ranks)
                sampleAndRankedList.getB().normalize();
        }

        if(outputDir != null){
            for(Pair<T,RankedList> rank : ranks)
                rank.getB().saveToFolder(rank.getA().getId(), outputDir);
        }

        return ranks;
    }

    public static void generateRanks(File metricesDir, boolean similaritiesOtherwiseDistances, int rankSizeLimit, boolean normalizeAsDecreasing, float min, float max, File outputDir) {
    	Map<Long,RankedList> ranksById = new HashMap<>();
    	float metricToItself = similaritiesOtherwiseDistances ? 1 : 0;
    	DistancesIO.forEachEntry(metricesDir, (idA,idB,metric) -> {
    		RankedList rankA = ranksById.get(idA);
    		RankedList rankB = ranksById.get(idB);
    		if(rankA == null) {
    			rankA = new RankedList(rankSizeLimit, similaritiesOtherwiseDistances);
    			rankA.add(idA, metricToItself);
    			ranksById.put(idA, rankA);
    		}
    		if(rankB == null) {
    			rankB = new RankedList(rankSizeLimit, similaritiesOtherwiseDistances);
    			rankB.add(idB, metricToItself);
    			ranksById.put(idB, rankB);
    		}
    		rankA.add(idB, metric);
    		rankB.add(idA, metric);
    	});

    	if(normalizeAsDecreasing)
    		ranksById.values().forEach(rank -> rank.normalizeDecreasing(min, max));

    	for(Entry<Long,RankedList> entry : ranksById.entrySet()) {
    		entry.getValue().saveToFolder(entry.getKey(), outputDir);
    	}
    }

    /** generated ranks considering different queries and responses */
    public static <T extends Sample> void generateRanks(Stream<T> querySamples, Iterable<T> responseSamples, SampleDistanceMeasurer<T> similarity, int rankSizeLimit, boolean normalize, File outputDir) {
        Logs.fine("Creating ranks, using similarity "+similarity+", limit "+rankSizeLimit+", on "+outputDir);
        TimeWatcher timeWatcher = new TimeWatcher(), logPooler = new TimeWatcher();
        AtomicLong count = new AtomicLong();
        querySamples.parallel().forEach(querySample -> {
            RankedList rank = generateRank(querySample, responseSamples, similarity, rankSizeLimit);
            if(normalize)
                rank.normalize();
            rank.saveToFolder(querySample.getId(), outputDir);
            count.incrementAndGet();
            if(logPooler.checkSecondsSpent(30)) Logs.finest(count+" ranks created so far");
        });
        Logs.finer("Ranks created after " + timeWatcher);
    }

    public static <T extends Sample> RankedList generateRank(T querySample, Iterable<T> responseSamples, SampleDistanceMeasurer<T> similarity, int rankSizeLimit) {
        RankedList rank = new RankedList(rankSizeLimit, true);
        for(T responseSample : responseSamples)
            rank.add(responseSample.getId(), similarity.getSimilarity(querySample, responseSample));
        return rank;
    }
}
