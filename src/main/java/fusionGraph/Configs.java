package fusionGraph;

import java.io.File;
import java.util.Map;
import dataMining.distance.graphDistance.GraphDistanceType;
import fusionGraph.dataset.DatasetFacade;
import util.FileUtils;
import util.Params;

public class Configs extends Params {

    public static final int LIST_SIZE_GENERATION = 1000;
    public static final int LIST_SIZE_EVALUATION = 10;

    public Configs(Map<String, String> params) {
        super(params);
    }

    public int listSizeFusion(){
    	return getInt("listSizeFusion", 10);
	}
    public int listSizeFusionLimit(){
    	return getInt("listSizeFusionLimit", 1000);
	}

	public static File getDatasetFolder(String name){
		return new File("/home/icaro/projects/fusionGraph/datasets/"+name);
	}

	static File resultsFolder(String dataset){
		return new File("/home/icaro/projects/fusionGraph/results/"+dataset);
	}

	public static File getFeaturesFolder(String dataset){
		return FileUtils.get(getDatasetFolder(dataset), "features");
	}

	public static File getFeaturesFile(String dataset, String descriptor){
		return FileUtils.get(getFeaturesFolder(dataset), descriptor);
	}

	public static File getDatasetFoldsDir(String dataset){
		return new File(getDatasetFolder(dataset),"foldDistributions");
	}

	public static File queryRanksParentFolder(String dataset){
		return new File(resultsFolder(dataset), "ranks");
	}

	public static File responseRanksParentFolder(String dataset){
		if(DatasetFacade.isQuerySetAndResponseSetEquals(dataset))
			return queryRanksParentFolder(dataset);
		return new File(resultsFolder(dataset), "ranks_responsesVSresponses");
	}

	public static File ranksEvaluationFolder(String dataset){
		return new File(resultsFolder(dataset), "ranksEvaluation");
	}

	public static File responseFusionGraphsFolder(String queryFusionGraphsFolder) {
		return responseFusionGraphsFolder(new File(queryFusionGraphsFolder));
	}
	public static File responseFusionGraphsFolder(File queryFusionGraphsFolder) {
		return new File(queryFusionGraphsFolder.getParentFile(), queryFusionGraphsFolder.getName() + "_responsesVSresponses");
	}

    public static File getDescriptorBasedRanksFolder(File ranksParentFolder, String descriptor) {
        return new File(ranksParentFolder, "descriptorBased_" + descriptor);
    }

    public String[] descriptors() {
        return assertArray("descriptors");
    }

    public GraphDistanceType fusionGraphComparator() {
        return GraphDistanceType.get(get("similarityFusionGraph", "WGU"));
    }

    public boolean normalizeLinear() {
        return getBoolean("normalizeLinear", true);
    }

    public int rerankOptionAtEval() {
        return getInt("rerankOptionAtEval", 0);
    }

    public int rerankOptionAtFusion() {
        return getInt("rerankOptionAtFusion", 2);
    }
    public void disableDefaultRerankOptionAtFusion() {
    	setIfAbsent("rerankOptionAtFusion", 0);
    }

    public int L(String dataset, boolean methodSupportsFullRanks){
        if( !DatasetFacade.isFullRankRequired(dataset) )
            return listSizeFusion();
        if(methodSupportsFullRanks)
            return -1;
        return listSizeFusionLimit();
    }

    public String embed(){
    	return assertParam("embed");
    }

    public static Configs parse(String... args) {
        return new Configs(parseParams(args));
    }

	public int incremental(){
		return getInt("incremental", 2);
	}
}
