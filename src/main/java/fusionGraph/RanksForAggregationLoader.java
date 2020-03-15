package fusionGraph;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import dataMining.retrieval.RankedList;
import dataMining.retrieval.RankedLists;
import fusionGraph.dataset.DatasetFacade;
import util.DataStructureUtils;
import util.MemoryUtils;

public class RanksForAggregationLoader {

    public static Map<Long,List<RankedList>> loadRanksForAggregation(String dataset, Function<String,File> getterDescriptorRanksFolder, String[] descriptors, int rankSizeLimit, boolean normalizeToLinearInterval, int rerankOptionAtFusion){
        boolean rerank = rerankOptionAtFusion > 0;
        if(rerank && DatasetFacade.isFullRankRequired(dataset))
            throw new UnsupportedOperationException("not yet implemented");
        int rankSizeRerankingLimit = DatasetFacade.getRankSizeLimitGeneration(dataset);
        return loadRanksForAggregation(rankSizeRerankingLimit, getterDescriptorRanksFolder, descriptors, rankSizeLimit, normalizeToLinearInterval, rerankOptionAtFusion);
    }

	public static Map<Long,List<RankedList>> loadRanksForAggregation(int rankSizeRerankingLimit, Function<String,File> getterDescriptorRanksFolder, String[] descriptors, int rankSizeLimit, boolean normalizeToLinearInterval, int rerankOptionAtFusion){
		Consumer<RankedList> normalizer;
		{
			float normMin = 0.1F, normMax = 1F; //IMPORTANT: normalization interval not in 0 to avoid empty weight contribution for vertices and edges
			normalizer =
				normalizeToLinearInterval
				? rank -> rank.changeWeightsToInterval(normMax, normMin) //tentativa de normalizar evitando possiveis impactos de intervalos muito dispares, do modelo anterior
					: rank -> rank.normalizeDecreasing(normMin, normMax);
		}
		Map<Long,List<RankedList>> ranksById = new HashMap();
		for(String descriptor : descriptors){
			File ranksFolder = getterDescriptorRanksFolder.apply(descriptor);
			RankedLists ranks = RankedLists.loadFromFolder(ranksFolder, rankSizeLimit, rerankOptionAtFusion, rankSizeRerankingLimit);
			ranks.forEach((id,rank) -> {
				normalizer.accept(rank);
				DataStructureUtils.putOnListValue(ranksById, id, rank);
			});
			MemoryUtils.log();
		}
		return ranksById;
	}
}


//parentRanksFolder
//Configs.getDescriptorBasedRanksFolder(parentRanksFolder, descriptor)