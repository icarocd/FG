package fusionGraph.dataset;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.IntFunction;
import java.util.function.LongPredicate;
import org.apache.commons.lang3.mutable.MutableInt;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import dataMining.retrieval.QualityQueries;
import dataMining.retrieval.RankQualityMeasurer;
import dataMining.retrieval.RankQualityMeasurerFactory;
import dataMining.retrieval.RankedLists;
import fusionGraph.Configs;
import util.Logs;
import util.Pair;

public class DatasetFacade {

	//cache:
    static String dataset;
    static Pair<Map<String,MutableInt>,SortedMap<Long,String>> labeledDatasetInfo;

	public static synchronized Set<Long> getResponseIDsInOrder(String dataset) {
		if(isQuerySetAndResponseSetEquals(dataset) && isLabeled(dataset)){
			SortedMap<Long,String> ids_classes = getLabeledDatasetInfo(dataset).getB();
			return ids_classes.keySet();
		}
		throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}

    public static synchronized Pair<Map<String,MutableInt>,SortedMap<Long,String>> getLabeledDatasetInfo(String dataset){
        if(labeledDatasetInfo == null || !dataset.equals(DatasetFacade.dataset)){
            if("MPEG7".equals(dataset) || "Brodatz".equals(dataset) || "Soccer".equals(dataset) || "UW".equals(dataset))
                labeledDatasetInfo = new DistanceMatricesLabeledDataset(dataset).loadInfo();
            else throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
        }
        DatasetFacade.dataset = dataset;
        return labeledDatasetInfo;
    }

    public static Map<Long,String> getLabelsIfApplicable(String dataset){
		if(!isLabeled(dataset))
			return null;
		return getLabeledDatasetInfo(dataset).getB();
	}

    /**
     * Returns a BiMap from ids to indices and vice-versa, or null if the dataset already uses ids starting from 0 and without gaps
     */
    public static BiMap<Long,Integer> bimapperIDToIndex(String dataset){
		if("MPEG7".equalsIgnoreCase(dataset) || "Brodatz".equalsIgnoreCase(dataset) || "Soccer".equalsIgnoreCase(dataset) || "UW".equalsIgnoreCase(dataset) || "UKBench".equalsIgnoreCase(dataset))
			return null;
		throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}

	public static BiMap<Integer,Long> bimapperIndexToID(String dataset){
		BiMap<Long,Integer> bimap_id_index = bimapperIDToIndex(dataset);
		return bimap_id_index != null ? bimap_id_index.inverse() : null;
	}

	public static boolean isLabeled(String dataset){
		if("MPEG7".equals(dataset) || "Brodatz".equals(dataset) || "Soccer".equals(dataset) || "UW".equals(dataset) || "UKBench".equals(dataset))
			return true;
		throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}
	public static float getMostRepresentativeMeasure(String dataset, QualityQueries q){
		if("UKBench".equals(dataset))
			return q.meanNSScore();
		if("MPEG7".equals(dataset) || "Brodatz".equals(dataset) || "Soccer".equals(dataset) || "UW".equals(dataset))
			return q.NDCG();
		throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}
	public static boolean usesNDCG10OtherwiseNS(String dataset) {
		if("UKBench".equals(dataset))
			return false;
		if("MPEG7".equals(dataset) || "Brodatz".equals(dataset) || "Soccer".equals(dataset) || "UW".equals(dataset))
			return true;
		throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}
	public static boolean isFullRankRequired(String dataset){
	    if("MPEG7".equals(dataset) || "Brodatz".equals(dataset) || "Soccer".equals(dataset) || "UW".equals(dataset) || "UKBench".equals(dataset))
	        return false;
	    throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}
	public static boolean isQuerySetAndResponseSetEquals(String dataset){
		if("MPEG7".equals(dataset) || "Brodatz".equals(dataset) || "Soccer".equals(dataset) || "UW".equals(dataset) || "UKBench".equals(dataset))
			return true;
		throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}

	public static int getRankSizeLimitGeneration(String dataset){
		return isFullRankRequired(dataset) ? -1 : Configs.LIST_SIZE_GENERATION;
	}

	/**
	 * PS: usamos este metodo tambem pra saber limite dos tamanhos de ranks finais a gerar, pois precisa ser ate o tamanho maximo em que a avaliacao olha
	 */
	public static int getRankSizeLimitEvaluation(String dataset){
		 return isFullRankRequired(dataset) ? -1 : Configs.LIST_SIZE_EVALUATION;
	}

	static void generateRanks(String dataset){
		generateRanks(dataset, getRankSizeLimitGeneration(dataset));
	}
	static void generateRanks(String dataset, int rankSize){
		if("MPEG7".equals(dataset) || "Brodatz".equals(dataset) || "Soccer".equals(dataset) || "UW".equals(dataset))
			new DistanceMatricesLabeledDataset(dataset).generateRanks(rankSize);
		else throw new UnsupportedOperationException("dataset not yet supported: " + dataset);
	}

	private static RankQualityMeasurer createRankQualityMeasurer(String dataset, File ranksToEval, int rerank){
		int finalMaxListEvaluationSize = getRankSizeLimitEvaluation(dataset);
    	if(rerank > 0)
    		return createRankQualityMeasurer(dataset, RankedLists.loadFromFolder(ranksToEval, finalMaxListEvaluationSize, rerank, getRankSizeLimitGeneration(dataset)));
		if( ranksToEval.isDirectory() ) //se nao for fazer reranking, e é diretorio, obtem um measurer que nao carrega os ranks em memoria
			return createRankQualityMeasurerOnDemand(dataset, ranksToEval, finalMaxListEvaluationSize);
		return createRankQualityMeasurer(dataset, RankedLists.loadFromFile(ranksToEval, finalMaxListEvaluationSize, "\\s+", 0, null));
	}
	/** Cria um quality measurer sem depender de carregar em memoria todos os ranks. */
    private static RankQualityMeasurer createRankQualityMeasurerOnDemand(String dataset, File ranksFolder, int finalMaxListEvaluationSize){
    	Preconditions.checkArgument(ranksFolder.isDirectory());
		long[] queryIds = RankQualityMeasurerFactory.queryIdsSorted(ranksFolder);
		IntFunction<Long> idGetter = queryIdx -> queryIds[queryIdx];
		IntFunction<List<Long>> rankGetter = RankQualityMeasurerFactory.queryRankGetter(ranksFolder, idGetter, finalMaxListEvaluationSize);
		return new RankQualityMeasurer(queryIds.length, idGetter, rankGetter, queryRelevanceCheckerProvider(dataset, idGetter), getterMaxRelevants(dataset, idGetter), getExpectedRankSizes(dataset));
    }
	private static RankQualityMeasurer createRankQualityMeasurer(String dataset, RankedLists lists){
		return createRankQualityMeasurer(dataset, lists.getQueryIDs(), lists.getRanks());
	}
	public static RankQualityMeasurer createRankQualityMeasurer(String dataset, SortedMap<Long, List<Long>> ranks) {
        return createRankQualityMeasurer(dataset, ranks.keySet().toArray(new Long[ranks.size()]), ranks.values().toArray(new List[ranks.size()]));
    }
    private static RankQualityMeasurer createRankQualityMeasurer(String dataset, Long[] queryIDs, List<Long>[] ranks){
		IntFunction<Long> idGetter = index -> queryIDs[index];
		return new RankQualityMeasurer(queryIDs.length, idGetter, queryIndex -> ranks[queryIndex], queryRelevanceCheckerProvider(dataset, idGetter), getterMaxRelevants(dataset, idGetter), getExpectedRankSizes(dataset));
	}

	private static IntFunction<LongPredicate> queryRelevanceCheckerProvider(String dataset, IntFunction<Long> idGetter){
		if(isLabeled(dataset)){
			Pair<Map<String,MutableInt>,SortedMap<Long,String>> labeledInfo = getLabeledDatasetInfo(dataset);
			return RankQualityMeasurerFactory.queryRelevanceCheckerProvider(idGetter, labeledInfo.getB()::get, labeledInfo.getB()::get);
		}
		throw new UnsupportedOperationException("dataset not yet supported: "+dataset);
	}
	private static IntFunction<Integer> getterMaxRelevants(String dataset, IntFunction<Long> idGetter){
		if( isLabeled(dataset) ){
			Pair<Map<String,MutableInt>,SortedMap<Long,String>> labeledInfo = getLabeledDatasetInfo(dataset);
			return RankQualityMeasurerFactory.getterMaxRelevants(idGetter, labeledInfo.getB()::get, labeledInfo.getA()::get);
		}
		throw new UnsupportedOperationException("dataset not yet supported: "+dataset);
	}
	private static Integer getExpectedRankSizes(@SuppressWarnings("unused") String dataset){
		//PS: it depends on your dataset.
    	return null;
	}

	public static QualityQueries evaluateRanks(String dataset, File ranksToEval, int rerank, boolean skipIfExist){
		File measuresFile = new File(Configs.ranksEvaluationFolder(dataset), ranksToEval.getName() + getRerankAppendName(rerank));
    	if(skipIfExist && measuresFile.exists())
    		return null;

    	RankQualityMeasurer measurer = createRankQualityMeasurer(dataset, ranksToEval, rerank);

        //PS: here, we must indicate the cut-off for evaluation, regardless the dataset requires MAP or not. Requiring MAP must influence only on the rank generation and loading, but not here.
        QualityQueries quality = measurer.measureQualityOfRankings(Configs.LIST_SIZE_EVALUATION);

        quality.save(measuresFile);
        Logs.info("evaluated "+ranksToEval+", into " +measuresFile);

        Logs.info("results:\n" + quality.save(new StringWriter(), false).toString());

        return quality;
	}

	public static String getRerankAppendName(int rerankOptionAtFusion){
		if(rerankOptionAtFusion == 1) return "_rerank";
		if(rerankOptionAtFusion == 2) return "_rerankRec";
		return "";
	}
}
