package fusionGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import com.google.common.base.Preconditions;
import dataMining.SamplePathResolver;
import dataMining.SamplePathResolverSimple;
import dataMining.distance.graphDistance.GraphDistanceType;
import dataMining.distance.graphDistance.GraphSampleDistanceMeasurer;
import dataMining.graph.GraphDataset;
import dataMining.graph.GraphSample;
import dataMining.retrieval.RankGenerator;
import dataMining.retrieval.RankedList;
import fusionGraph.dataset.DatasetFacade;
import util.FileUtils;
import util.LazySupplier;
import util.Logs;
import util.StringUtils;
import util.TimeWatcher;
import util.graph.LabeledMeasurableGraph;

public class RankAggregation {

    private final String[] descriptors;
    private int rerankOptionAtFusion;
    final int L;
    private final boolean normalizeLinear;
    private final FusionGraphCreator fusionCreator;
    private final String fusionConfigName;
	public final File queryFusionGraphsFolder;

    public RankAggregation(String dataset, Configs params) {
    	this(new File(Configs.resultsFolder(dataset), "ranks-aggregated"), params.L(dataset, false), params);
    }
    public RankAggregation(File parentFusionGraphsDir, int L, Configs params) {
    	this.L = L;
    	descriptors = params.descriptors();
    	rerankOptionAtFusion = params.rerankOptionAtFusion();

    	normalizeLinear = params.normalizeLinear();

    	fusionCreator = new FusionGraphCreator();

    	fusionConfigName = StringUtils.stripStart(
            DatasetFacade.getRerankAppendName(rerankOptionAtFusion)
            + (normalizeLinear ? "_normalizeLinear" : "")
            + fusionCreator.getSetupInfoSufix()
            + "_descriptors="+StringUtils.join(descriptors,'+')
            + "_L="+L, "_");
    	queryFusionGraphsFolder = new File(parentFusionGraphsDir, "FG_"+fusionConfigName);
    }

	public void aggregateRanks(String dataset, int incremental){
        File queryRanksParentFolder = Configs.queryRanksParentFolder(dataset);
        File responseRanksParentFolder = Configs.responseRanksParentFolder(dataset);
        boolean useSameQueriesAndResponses = queryRanksParentFolder.equals(responseRanksParentFolder);
        int nQueries;
        LazySupplier<Map<Long,List<RankedList>>> getter_queryIds_ranks;
        {
        	Function<String,File> getterDescriptorRanksFolderQuery = d -> Configs.getDescriptorBasedRanksFolder(queryRanksParentFolder, d);
        	File firstDescriptorRanksDir = getterDescriptorRanksFolderQuery.apply(descriptors[0]);
        	Preconditions.checkState(firstDescriptorRanksDir.isDirectory(), "it must be an existing dir: "+firstDescriptorRanksDir);
    		nQueries = FileUtils.countDirFiles(firstDescriptorRanksDir, false);
        	getter_queryIds_ranks = LazySupplier.create(() -> loadRanksForAggregation(dataset, getterDescriptorRanksFolderQuery));
        }
        if(useSameQueriesAndResponses){
			aggregateRanksAsGraphs(nQueries, getter_queryIds_ranks, DatasetFacade.getLabelsIfApplicable(dataset), getter_queryIds_ranks, queryFusionGraphsFolder, incremental);
        }else{
            if(rerankOptionAtFusion != 0)
                throw new UnsupportedOperationException("reranking not yet supported for different response and query sets");
            if(DatasetFacade.isLabeled(dataset))
                throw new UnsupportedOperationException("to be implemented. it requires labels for different response and query sets...");
            Map<Long,List<RankedList>> responseIds_ranks = loadRanksForAggregation(dataset, d -> Configs.getDescriptorBasedRanksFolder(responseRanksParentFolder, d));
            LazySupplier<Map<Long,List<RankedList>>> getter_responseIds_ranks = LazySupplier.create(() -> responseIds_ranks);
            aggregateRanksAsGraphs(nQueries, getter_queryIds_ranks, null, getter_responseIds_ranks, queryFusionGraphsFolder, incremental);
            //gerar tambem pra response vs response, pois é usado posteriormente no rerankeamento dos ranks das consultas
            int nResponses = responseIds_ranks.size();
            aggregateRanksAsGraphs(nResponses, getter_responseIds_ranks, null, getter_responseIds_ranks, Configs.responseFusionGraphsFolder(queryFusionGraphsFolder), incremental);
        }
    }
	private Map<Long,List<RankedList>> loadRanksForAggregation(String dataset, Function<String,File> getterDescriptorRanksFolder){
		 return RanksForAggregationLoader.loadRanksForAggregation(dataset, getterDescriptorRanksFolder, descriptors, L, normalizeLinear, rerankOptionAtFusion);
	}
	public void aggregateRanksAsGraphs(int nQueries, LazySupplier<Map<Long,List<RankedList>>> queryIds_ranks, Map<Long,String> queryIds_labels,
		LazySupplier<Map<Long, List<RankedList>>> getter_responseIds_ranks, File outputDir_, int incremental)
	{
		SamplePathResolver outputDir = new SamplePathResolverSimple(outputDir_);
        if(outputDir.exists() && outputDir.countFiles() >= nQueries){
        	Logs.fine("fusion graphs exist, skipping. "+outputDir);
        	return;
        }
        Function<Long,List<RankedList>> responseIds_ranks = getter_responseIds_ranks.get()::get;
    	Logs.info("Generating fusion graphs: "+outputDir);
        outputDir.initialize(incremental > 0);
        AtomicInteger progressCount = new AtomicInteger(); TimeWatcher progressWatcher = new TimeWatcher();
        TimeWatcher totalTime = new TimeWatcher();
        queryIds_ranks.get().entrySet().parallelStream().forEach(id_ranks -> {
            Long id = id_ranks.getKey();
            File outputFile = GraphDataset.getGraphSampleFile(outputDir, id);
            if(incremental < 1 || !outputFile.exists()){
            	String label = queryIds_labels==null ? null : queryIds_labels.get(id);
				LabeledMeasurableGraph fg = fusionCreator.create(id, id_ranks.getValue(), responseIds_ranks);
            	GraphDataset.writeSample(new GraphSample(id, label, fg), outputFile);
            }
            progressCount.incrementAndGet();
            if(progressWatcher.checkSecondsSpent())
            	Logs.finest("now: "+progressCount+" fusion graphs generated");
        });
        Logs.info(progressCount+" fusion graphs generated, ater " + totalTime);
    }

    public void rankFromFusedGraphsByQuerying(String dataset, Configs params) {
        final GraphDistanceType similarityFusionGraph = params.fusionGraphComparator();
        final File queryGraphBasedRerankedRanksFolder = getFusedRanksFolder(Configs.queryRanksParentFolder(dataset), similarityFusionGraph);
        if( DatasetFacade.isQuerySetAndResponseSetEquals(dataset) ){
            ArrayList<GraphSample> fusionGraphs = GraphDataset.loadFromFolder(queryFusionGraphsFolder).getSamples();
            RankGenerator.generateRanks(fusionGraphs, new GraphSampleDistanceMeasurer(similarityFusionGraph),
                DatasetFacade.getRankSizeLimitEvaluation(dataset), true, true, queryGraphBasedRerankedRanksFolder);
        }else{
            Stream<GraphSample> queryFusionGraphs = Arrays.stream(queryFusionGraphsFolder.listFiles()).map(f -> GraphDataset.loadSampleFromFile(f));
            ArrayList<GraphSample> responseFusionGraphs = GraphDataset.loadFromFolder(Configs.responseFusionGraphsFolder(queryFusionGraphsFolder)).getSamples();
            RankGenerator.generateRanks(queryFusionGraphs, responseFusionGraphs, new GraphSampleDistanceMeasurer(similarityFusionGraph),
                DatasetFacade.getRankSizeLimitEvaluation(dataset), true, queryGraphBasedRerankedRanksFolder);
        }
        DatasetFacade.evaluateRanks(dataset, queryGraphBasedRerankedRanksFolder, params.rerankOptionAtEval(), false);
    }

    public File getFusedRanksFolder(File ranksParentFolder, GraphDistanceType fusionGraphComparator){
    	 return new File(ranksParentFolder, "FG_"+fusionGraphComparator.name()+"_"+fusionConfigName);
	}
}
