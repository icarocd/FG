package fusionGraph.dataset;

import java.io.File;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.mutable.MutableInt;
import dataMining.retrieval.RankedList;
import fusionGraph.Configs;
import util.FileUtils;
import util.Logs;
import util.StringUtils;

public abstract class DistanceMatricesDataset {

	protected final String name;

	public DistanceMatricesDataset(String name) {
		this.name = name;
	}

	public void generateRanks(int rankSizeLimit){
		generateRanks(rankSizeLimit, null, null, Configs.queryRanksParentFolder(name));
	}
	protected void generateRanks(int rankSizeLimit, Set<Long> queryIdsFilter, Set<Long> responseIdsFilter, File ranksParentFolder){
		Function<String,File> descriptorNameToRanksFolder = d -> Configs.getDescriptorBasedRanksFolder(ranksParentFolder, d);
		for(File distancesFile : getDistancesDir().listFiles()){
			File ranksFolder = descriptorNameToRanksFolder.apply(distancesFile.getName());
			if(ranksFolder.exists()) {
				Logs.info("ranks exist, skipping. "+ranksFolder);
				continue;
			}
			Logs.info("generating ranks at "+ranksFolder);
			ranksFolder.mkdirs();
			MutableInt currentId = new MutableInt();
			FileUtils.forEachLine(distancesFile, distsLine -> {
				long id = currentId.intValue();
				if( queryIdsFilter == null || queryIdsFilter.contains(id) ){
					double[] dists = StringUtils.splitDoubles(distsLine.trim(), " ");
					RankedList rank = new RankedList(rankSizeLimit, false);
					for(long responseId = 0; responseId < dists.length; responseId++){
						if(responseIdsFilter == null || responseIdsFilter.contains(responseId)){
							rank.add(responseId, (float)dists[(int)responseId]);
						}
					}
					rank.saveToFolder(id, ranksFolder);
				}
				currentId.increment();
			});
		}
	}

	public File getDistancesDir(){
		return new File(getDatasetFolder(), "distance_matrices");
	}

	public File getDatasetFolder(){
		return Configs.getDatasetFolder(name);
	}
}
