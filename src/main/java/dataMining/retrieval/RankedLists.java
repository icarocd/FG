package dataMining.retrieval;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.common.base.Preconditions;
import util.DataStructureUtils;
import util.FileUtils;
import util.Logs;
import util.Pair;

public class RankedLists {

	private SortedMap<Long,RankedList> ids_ranks;

	public RankedLists(SortedMap<Long, RankedList> ids_ranks) {
        this.ids_ranks = ids_ranks;
    }

    /**
	 * PS: the ranks ordering follows the query IDs ordering, which is sorted by the ids.
	 */
	public List<Long>[] getRanks(){
		return DataStructureUtils.toArray(ids_ranks.values(), List.class, rank -> rank.getIDs());
	}

	/**
	 * PS: the query ids are returned in order.
	 */
	public Long[] getQueryIDs(){
		return DataStructureUtils.toArray(ids_ranks.keySet(), Long.class);
	}

	public void forEach(BiConsumer<? super Long,? super RankedList> action) {
		ids_ranks.forEach(action);
	}

	public static SortedMap<Long,List<Long>> loadFromFile(File ranksFile, int maxRankSize, String separator, int offset, Function<String,Long> mapperToID){
		SortedMap<Long,List<Long>> ranks = new TreeMap<>();
		FileUtils.forEachLine(ranksFile, line -> {
			String[] pieces = line.split(separator);
			int endPosExclusive = maxRankSize < 0 ? pieces.length : Math.min(offset + maxRankSize, pieces.length);
			List<Long> rank;
			if(mapperToID != null)
				rank = Arrays.stream(pieces, offset, endPosExclusive).map(el -> mapperToID.apply(el)).collect(Collectors.toList());
			else
				rank = Arrays.stream(pieces, offset, endPosExclusive).map(el -> Long.valueOf(el)).collect(Collectors.toList());
			ranks.put(rank.get(0), rank);
		});
		return ranks;
	}

	public static RankedLists loadFromFolder(File folder, int rankSizeLimit, int rerank, int rankSizeRerankingLimit) {
		Preconditions.checkArgument(folder.isDirectory(), "path either does not exist or it is a file (method not yet implemented for ranks as a file): "+folder);
		Logs.finer("loading ranks from " + folder);
		if(rerank > 0){
			LinkedHashMap<Long,Map<Long,Pair<Integer,Float>>> ids_ranks = new LinkedHashMap(); //a rank here is a map of <id,<index,weight>>
			for(File rankedListFile : folder.listFiles()){
	        	LinkedHashMap<Long,Pair<Integer,Float>> rank = new LinkedHashMap();
	        	RankedList.load(rankedListFile, rankSizeRerankingLimit, (rank_index,rank_id,rank_weight) -> rank.put(rank_id, Pair.get(rank_index, rank_weight)));
	        	ids_ranks.put(Long.valueOf(rankedListFile.getName()), rank);
	        }
			return new RankedLists(rerank(ids_ranks, rerank==2, rankSizeLimit));
		}else{
		    SortedMap<Long,RankedList> ids_ranks = new TreeMap();
			for(File rankedListFile : folder.listFiles())
	        	ids_ranks.put(Long.valueOf(rankedListFile.getName()), RankedList.load(rankedListFile, rankSizeLimit));
			return new RankedLists(ids_ranks);
		}
	}

	private static SortedMap<Long,RankedList> rerank(Map<Long,Map<Long,Pair<Integer,Float>>> id_rank, boolean considerReciprocal, int finalMaxListEvaluationSize){
		Logs.finer("starting reranking");
		//normaliza conforme: 2017 P.R., Pedronette, 'Unsupervised manifold learning through reciprocal kNN graph and Connected Components for image retrieval tasks'
        BiFunction<Integer,Integer,Float> normalizer;
		if(considerReciprocal)
			normalizer = (pos,anotherPos) -> (float)(pos + anotherPos + Math.max(pos, anotherPos));
        else
        	normalizer = (pos,anotherPos) -> (float)(pos + anotherPos);
    	TreeMap<Long,RankedList> newRanks = new TreeMap();
    	id_rank.forEach((id, rank) -> {
    		List<Pair<Long,Float>> newRank = new ArrayList(rank.size());
			rank.forEach((rankedElement,index_weight) -> {
				int pos = index_weight.getA() + 1;

				Map<Long,Pair<Integer,Float>> inverseRank = id_rank.get(rankedElement);
				Pair<Integer,Float> pos_weight = inverseRank.get(id);
				Integer inversePos = pos_weight != null ? pos_weight.getA()+1 : inverseRank.size()+1;

				newRank.add(new Pair<>(rankedElement, normalizer.apply(pos, inversePos)));
        	});
			DataStructureUtils.stableSort(newRank, Pair.createComparatorByB());
			if(finalMaxListEvaluationSize >= 0)
				DataStructureUtils.removeAtEnd(newRank, newRank.size() - finalMaxListEvaluationSize);
			newRanks.put(id, new RankedList(newRank));
        });
    	return newRanks;
	}

	public static void writeToFile(SortedMap<Long,List<Long>> ranks, File ranksFile){
		Logs.fine("Saving ranks at "+ranksFile);
		FileUtils.mkDirsForFile(ranksFile);
		try(PrintStream out = FileUtils.createPrintStreamToFile(ranksFile)){
			ranks.forEach((queryId,rank) -> {
				int offset = queryId.equals(rank.get(0)) ? 1 : 0;
				out.print(queryId);
				for(int i = offset; i < rank.size(); i++){
					out.print(" ");
					out.print(rank.get(i));
				}
				out.println();
			});
		}
	}
}
