package dataMining.retrieval;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongPredicate;
import org.apache.commons.lang3.mutable.MutableInt;
import com.google.common.base.Preconditions;
import dataMining.Sample;
import util.DataStructureUtils;
import util.Pair;

public class RankQualityMeasurerFactory {

	public static <T extends Sample> RankQualityMeasurer create(Pair<T,RankedList>[] ranks, Integer expectedRankSizes){
    	//IMPORTANT: here, the rank pairs must be sorted by the query ids, so that the results can be latter compared in paired manner
        //extrai maps, para indexar a qtd de amostras por classe, e a classe de cada amostra
        Map<String,MutableInt> numSamplesByClass = new HashMap<>();
        Map<Long,String> classBySampleId = new HashMap<>();
        for(Pair<T,RankedList> sampleRank : ranks){
            T sample = sampleRank.getA();
            DataStructureUtils.incrementMapValue(numSamplesByClass, sample.getLabel());
            classBySampleId.put(sample.getId(), sample.getLabel());
        }

        IntFunction<LongPredicate> queryRelevanceCheckerProvider = queryIndex -> {
			String queryClass = ranks[queryIndex].getA().getFirstLabel();
			return responseId -> queryClass.equals(classBySampleId.get(responseId));
		};
		IntFunction<Integer> getterMaxRelevants = queryIndex -> {
			String queryClass = ranks[queryIndex].getA().getFirstLabel();
			return numSamplesByClass.get(queryClass).getValue();
		};
		return new RankQualityMeasurer(ranks.length, queryIndex -> ranks[queryIndex].getA().getId(), queryIndex -> ranks[queryIndex].getB().getIDs(), queryRelevanceCheckerProvider, getterMaxRelevants, expectedRankSizes);
    }

	public static RankQualityMeasurer create(RankedLists lists, Function<String,? extends Number> label_maxResponses, Function<Long,String> queryId_label, Function<Long,String> responseId_label, Integer expectedRankSizes){
		Long[] queryIds = lists.getQueryIDs();
		List<Long>[] ranks = lists.getRanks();

		IntFunction<Long> idGetter = index -> queryIds[index];
		IntFunction<List<Long>> rankGetter = index -> ranks[index];
		IntFunction<LongPredicate> queryRelevanceCheckerProvider = queryRelevanceCheckerProvider(idGetter, queryId_label, responseId_label);
		IntFunction<Integer> getterMaxRelevants = getterMaxRelevants(idGetter, queryId_label, label_maxResponses);
		return new RankQualityMeasurer(ranks.length, idGetter, rankGetter, queryRelevanceCheckerProvider, getterMaxRelevants, expectedRankSizes);
	}
	public static IntFunction<LongPredicate> queryRelevanceCheckerProvider(IntFunction<Long> idGetter, Function<Long,String> querySampleClassById, Function<Long,String> trainSampleClassById){
		IntFunction<LongPredicate> queryRelevanceCheckerProvider = queryIndex -> {
			String queryClass = querySampleClassById.apply(idGetter.apply(queryIndex));
			return responseId -> queryClass.equals(trainSampleClassById.apply(responseId));
		};
		return queryRelevanceCheckerProvider;
	}
	public static IntFunction<Integer> getterMaxRelevants(IntFunction<Long> idGetter, Function<Long,String> querySampleClassById, Function<String,? extends Number> numTrainSamplesByClass){
		IntFunction<Integer> getterMaxRelevants = queryIndex -> {
            String queryClass = querySampleClassById.apply(idGetter.apply(queryIndex));
		    return numTrainSamplesByClass.apply(queryClass).intValue();
		};
		return getterMaxRelevants;
	}

    public static RankQualityMeasurer create(long id, List<Long> theRank, String queryClass, Function<String,? extends Number> numTrainSamplesByClass, Function<Long,String> trainSampleClassById, Integer expectedRankSizes) {
    	IntFunction<LongPredicate> queryRelevanceCheckerProvider = queryIndex -> {
			return responseId -> queryClass.equals(trainSampleClassById.apply(responseId));
		};
		IntFunction<Integer> getterMaxRelevants = queryIndex -> numTrainSamplesByClass.apply(queryClass).intValue();
        return new RankQualityMeasurer(1, index -> id, index -> theRank, queryRelevanceCheckerProvider, getterMaxRelevants, expectedRankSizes);
    }

	public static long[] queryIdsSorted(File ranksFolder){
		Preconditions.checkArgument(ranksFolder.isDirectory(), ranksFolder + " must be an existing folder");
    	return Arrays.stream(ranksFolder.list()).mapToLong(name -> Long.parseLong(name)).sorted().toArray();
	}

	public static IntFunction<List<Long>> queryRankGetter(File ranksFolder, IntFunction<Long> queryIdGetter, int finalMaxListEvaluationSize){
		return queryIdx -> RankedList.loadEntriesAsList(new File(ranksFolder, queryIdGetter.apply(queryIdx).toString()), finalMaxListEvaluationSize);
	}
}
