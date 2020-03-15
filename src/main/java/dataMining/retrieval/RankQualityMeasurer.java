package dataMining.retrieval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import util.Logs;
import util.MathUtils;
import util.Pair;
import util.Quadruple;
import util.TimeWatcher;

public class RankQualityMeasurer {

    private final int numRanks;
    private final IntFunction<Long> queryIdGetter;
    private final IntFunction<List<Long>> rankGetter; //given a query index, provides the rank
	private final IntFunction<LongPredicate> queryRelevanceCheckerProvider; ////given a query index, provides a relevance checker based on responseId
	private final IntFunction<Integer> getterMaxRelevants; //given a query index, provides the maximum recall value
	private final Integer expectedRankSizes;

    public RankQualityMeasurer(int numRanks, IntFunction<Long> queryIdGetter, IntFunction<List<Long>> rankGetter, IntFunction<LongPredicate> queryRelevanceCheckerProvider, IntFunction<Integer> getterMaxRelevants, Integer expectedRankSizes) {
        this.numRanks = numRanks;
        this.queryIdGetter = queryIdGetter;
        this.rankGetter = rankGetter;
        this.queryRelevanceCheckerProvider = queryRelevanceCheckerProvider;
        this.getterMaxRelevants = getterMaxRelevants;
        this.expectedRankSizes = expectedRankSizes;
    }

    public QualityQueries measureQualityOfRankings(int rankSize) {
        TimeWatcher timeWatcher = new TimeWatcher();
        QualityQueries qualityQueries = new QualityQueries(numRanks);

        long lastId = Long.MAX_VALUE;
        TimeWatcher progressWatcher = new TimeWatcher();
        for (int i = 0; i < numRanks; i++) {
        	long queryId = queryIdGetter.apply(i);
        	if(i > 0 && lastId > queryId)
        		throw new IllegalArgumentException("queryIds and ranks must by paired and in order by the query ids, so that the results can be latter compared in paired manner");
        	List<Long> rank = rankGetter.apply(i);
        	LongPredicate relevanceChecker = queryRelevanceCheckerProvider.apply(i);
            int maxRelevantResponses = getterMaxRelevants.apply(i);

            int relevantsRetrieved = 0;
            int relevantsRetrievedAt4 = 0;
            float sumPrecisionsFromRelevantIndices = 0;
            for (int k = 0; k < rankSize; k++) {
                if( relevanceChecker.test(rank.get(k)) ){ //at each recall delta increment
                    relevantsRetrieved++;
                    if(k < 4)
                    	relevantsRetrievedAt4++;
                    float precisionAtK = (float)relevantsRetrieved / (k+1);
                    sumPrecisionsFromRelevantIndices += precisionAtK;
                }
            }

            qualityQueries.precisions[i] = (float)relevantsRetrieved / rankSize;
            qualityQueries.recalls[i] = (float)relevantsRetrieved / maxRelevantResponses;
            qualityQueries.averagePrecisions[i] = sumPrecisionsFromRelevantIndices / Math.min(maxRelevantResponses, rankSize); /* sumPrecisionsFromRelevantIndices / numRelevants; */
			qualityQueries.NDCGs[i] = ndcg(k -> relevanceChecker.test(rank.get(k)), rankSize, maxRelevantResponses);
			qualityQueries.NS[i] = rankSize >= 4 ? relevantsRetrievedAt4 : Float.NaN;

			lastId = queryId;
			if(progressWatcher.checkSecondsSpent()) Logs.fine("progress: measureQualityOfRankings concluded " + (i+1) + " of " + numRanks);
        }
        qualityQueries.mAP = mAP();
        Logs.finer("Rank qualities measured, for rankSize of "+rankSize+", after " + timeWatcher);
        return qualityQueries;
    }

	private float ndcg(IntPredicate relevanceCheckerByRankIndex, int rankSize, int numTrainSamplesFromClass) {
        return MathUtils.ndcg(numTrainSamplesFromClass, rankSize, relevanceCheckerByRankIndex);
    }

    public Float mAP(){
    	return mAP(expectedRankSizes, new Iterator<Quadruple<Long,List,Integer,LongPredicate>>() {
    		int idx = 0;
			public boolean hasNext(){
				return idx < numRanks;
			}
			public Quadruple<Long,List,Integer,LongPredicate> next(){
				Quadruple<Long,List,Integer,LongPredicate> p = new Quadruple<>(
					queryIdGetter.apply(idx),
					rankGetter.apply(idx),
					getterMaxRelevants.apply(idx),
					queryRelevanceCheckerProvider.apply(idx)
				);
				idx++;
				return p;
			}
		});
	}

    /** Computes MAP, as the Area under Precision-Recall Curve, as the sum of trapezoid areas.
	 * Each iterator item is a <id,responses,maxPositives,relevanceChecker>, where:
	 *     responses is a list of, either, 1: pairs <rankPosition,responseId>, where rankPosition starts by 0, or 2: responseIds;
	 *     maxPositives: the maximum of true positive results for the query in the dataset;
	 *     relevanceChecker: a checker thas returns a flag according to whether the responseId is relevant to the query of not.
	 * IMPORTANT: Here, it is assumed that the response list does not bring the query id itself.
	 * PS: MAP differs from AP@K from this class, due to the cut-off use in AP@K. MAP requires the analysis of the full dataset instead.
	 */
	public static Float mAP(Integer expectedRankSizes, Iterator<Quadruple<Long,List,Integer,LongPredicate>> searchIterator){
		float sumAP = 0;
		int nQueries = 0;
		int minRankSize = Integer.MAX_VALUE, maxRankSize = -1;
		while( searchIterator.hasNext() ){
			Quadruple<Long,List,Integer,LongPredicate> search = searchIterator.next();
			long queryID = search.getA();
			List results_ = search.getB();
			int maxPositives = search.getC();
			LongPredicate relevanceChecker = search.getD();
			if( !results_.isEmpty() ){
				List<Integer> tpRanks = new ArrayList();  // ranks of true positives (not including the query)
				int rankShift = 0;  // apply this shift to ignore null results

				minRankSize = Math.min(minRankSize, results_.size());
				maxRankSize = Math.max(maxRankSize, results_.size());
				if(results_.get(0) instanceof Pair){
					List<Pair<Integer,Long>> results = results_;
					//sort results by increasing rank:
					Collections.sort(results, Pair.createComparatorByA());

					for(Pair<Integer,Long> rank_returned : results){
						int rankPosition = rank_returned.getA();
						long returnedID = rank_returned.getB();
						if(returnedID == queryID)
							rankShift--;
						else if(relevanceChecker.test(returnedID))
							tpRanks.add(rankPosition + rankShift);
					}
				}else{
					List<Long> results = results_;
					for(int rankPosition = 0; rankPosition < results.size(); rankPosition++){
						long returnedID = results.get(rankPosition);
						if(returnedID == queryID)
							rankShift--;
						else if(relevanceChecker.test(returnedID))
							tpRanks.add(rankPosition + rankShift);
					}
				}

				sumAP += apFromSearch(tpRanks, maxPositives);
			}
			nQueries++;
		}
		if(expectedRankSizes != null && (expectedRankSizes.intValue() != minRankSize || expectedRankSizes.intValue() != maxRankSize)){
			Logs.warn("MAP could not be reliably computed, due to the ocurrence of ranks of unexpected size: "
				+nQueries+" queries, expected size "+expectedRankSizes+", found min "+minRankSize+" and max "+maxRankSize+".");
			return null;
		}
		return sumAP / nQueries;
	}

	/** Compute the average precision (area sum of trapezoids in PR-plot) of one search.
	 * tpRanks             = ordered list of ranks of true positives
	 * nMaxPositiveResults = total number of positives in dataset */
	private static float apFromSearch(List<Integer> tpRanks, int maxPositiveResults){
		// All trapezoids have an x-size of:
		final double recallStep = 1.0/maxPositiveResults;

		float ap = 0;  // accumulate trapezoids in PR-plot

		// ntp = nb of true positives so far
		// rank = nb of retrieved items so far
		for(int countPositives = 0; countPositives < tpRanks.size(); countPositives++){
			int rankPosition = tpRanks.get(countPositives);

			// y-size on left side of trapezoid:
			float precisionLeft = rankPosition==0 ? 1 : countPositives/(float)rankPosition;

			// y-size on right side of trapezoid (ntp and rank are increased by one):
			float precisionRight = (countPositives+1)/(float)(rankPosition+1);

			ap += (precisionRight + precisionLeft) * recallStep / 2F;
		}
		return ap;
	}
}
