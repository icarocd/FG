package dataMining.graph;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import com.google.common.collect.Iterators;
import dataMining.Dataset;
import dataMining.Sample;
import dataMining.SamplePathResolver;
import dataMining.SamplePathResolverSimple;
import util.DataStructureUtils;
import util.FileUtils;
import util.Logs;
import util.Pair;
import util.StringUtils;
import util.TimeWatcher;
import util.graph.DirectedWeightedLabeledGraph;
import util.graph.LabeledMeasurableGraph;
import util.graph.LabeledWeightedEdge;

public class GraphDataset extends Dataset<GraphSample> {

    public GraphDataset() {
    }

    public GraphDataset(ArrayList<GraphSample> samples) {
        super(samples);
    }

    public void normalizeWeights(float min, float max) {
        normalizeWeights(min, max, samples);
	}
	public static void normalizeWeights(float min, float max, ArrayList<GraphSample>... datasets) {
	    Logs.finer("Normalizing graph weights by dividing values for their max within each graph");
        for(ArrayList<GraphSample> samples : datasets) {
    	    for (GraphSample s : samples) {
                s.getGraph().normalizeWeights(min, max);
            }
        }
    }

    public void logStats() {
	    TreeSet<String> terms = new TreeSet<>();
	    int sumVertices = 0;
	    for(GraphSample s : samples){
	    	Set<String> graphTerms = s.getGraph().vertexSet();
            terms.addAll(graphTerms);
	    	sumVertices += graphTerms.size();
	    }
	    Logs.info("terms (" + terms.size() +"): " + terms);
	    Logs.info("Avg. #vertices per graph: " + (float)sumVertices/size());
	}

	public void writeToFolder(SamplePathResolver resultFolder) {
		logSampleCountByClass();

        TimeWatcher watcher = new TimeWatcher();

        resultFolder.initialize(false);

    	for(GraphSample sample : samples)
    		writeSampleOnFolder(sample, resultFolder);

        Logs.fine("Dataset saved on folder " + resultFolder + ". Time elapsed: "+ watcher);
    }

	public static void writeSampleOnFolder(GraphSample graphSample, File resultFolder) {
		writeSampleOnFolder(graphSample, new SamplePathResolverSimple(resultFolder));
	}
	public static void writeSampleOnFolder(GraphSample graphSample, SamplePathResolver resultFolder) {
        writeSample(graphSample, getGraphSampleFile(resultFolder, graphSample.getId()));
	}
	public static void writeSample(GraphSample graphSample, File outputFile) {
		writeSample(graphSample, outputFile, true);
	}
	public static void writeSample(GraphSample graphSample, File outputFile, boolean compress) {
//  file format without compression:
//  id
//  labels
//  num_vertices num_edges flag_weighted
//  vertex_name vertex_weight [1 line per vertex]
//  edge_source edge_target edge_weight edge_label [1 line per edge]
//
//	file format with compression:
//  id
//  labels
//  num_vertices num_edges flag_weighted C
//  vertex_name vertex_weight [1 line per vertex]
//  edge_source [edge_target edge_weight edge_label]*
	    DecimalFormat formatter = StringUtils.getDecimalFormatter(8);
	    try(PrintStream out = FileUtils.createPrintStreamToFile(outputFile)){
            out.println(graphSample.getId());
            if(graphSample.getLabels() == null)
                out.println();
            else
                out.println(StringUtils.join(graphSample.getLabels(), '\t'));
            LabeledMeasurableGraph g = graphSample.getGraph();
            Set<String> vertices = g.vertexSet();
            out.print(vertices.size());
            out.print("\t");
            out.print(g.getNumEdges());
            out.print("\t");
            out.print(g.isWeighted() ? '1' : '0');
            if(compress){
            	out.print("\t");
            	out.print("C");
            }
            out.print("\n");
            for(String vertex : vertices){
                out.print(vertex);
                out.print("\t");
                Double w = g.getVertexWeight(vertex);
                if(w == null || w.isNaN()) throw new IllegalStateException("invalid vertex weight for ["+vertex+"] on GraphSample ["+graphSample.getId()+"]");
                out.print(formatter.format(w));
                out.print("\n");
            }
            if(compress){
            	for(String vertex : vertices){
            		Set<LabeledWeightedEdge> outgoingEdges = g.outgoingEdgesOf(vertex);
            		if(!outgoingEdges.isEmpty()){
            			out.print(vertex);
            			for(LabeledWeightedEdge edge : outgoingEdges){
            				out.print("\t");
            				out.print(edge.getTarget());
                    		out.print("\t");
                    		double w = edge.getWeight();
                    		if(Double.isNaN(w)) throw new IllegalStateException("invalid edge weight for ["+edge+"] on GraphSample ["+graphSample.getId()+"]");
                    		out.print(formatter.format(w));
                    		out.print("\t");
                    		out.print(edge.getLabel());
            			}
            			out.print("\n");
            		}
            	}
            }else{
            	for(LabeledWeightedEdge edge : g.edgeSet()){
            		out.print(edge.getSource());
            		out.print("\t");
            		out.print(edge.getTarget());
            		out.print("\t");
            		double w = edge.getWeight();
            		if(Double.isNaN(w)) throw new IllegalStateException("invalid edge weight for ["+edge+"] on GraphSample ["+graphSample.getId()+"]");
            		out.print(formatter.format(w));
            		out.print("\t");
            		out.print(edge.getLabel());
            		out.print("\n");
            	}
            }
        }
    }
	public static Set<String> loadSampleLabelsFromFile(File file) {
        try(Scanner in = FileUtils.createScannerFromFile(file)){
        	in.nextLine(); //id
            String s = in.nextLine();
            if(!s.isEmpty())
                return DataStructureUtils.asSet(s.split("\t"));
            return new LinkedHashSet<>(0);
        }
    }
	public static GraphSample loadSampleFromFile(File file) {
        try(Scanner in = FileUtils.createScannerFromFile(file)){
            long id = Long.parseLong(in.nextLine());
            Set<String> labels = null;
            {
                String s = in.nextLine();
                if(!s.isEmpty())
                    labels = DataStructureUtils.asSet(s.split("\t"));
            }
            String[] tmp = in.nextLine().split("\t");
            int nVertices = Integer.parseInt(tmp[0]);
            int nEdges = Integer.parseInt(tmp[1]);
            boolean weighted = Integer.parseInt(tmp[2])==1;
            boolean compressed = tmp.length > 3 && tmp[3].equals("C");
            DirectedWeightedLabeledGraph g = new DirectedWeightedLabeledGraph(weighted);
            for (int i = 1; i <= nVertices; i++) {
                tmp = in.nextLine().split("\t");
                String vertex = tmp[0];
                double weight = Double.parseDouble(tmp[1]);
                g.addVertex(vertex, weight);
            }
            if(compressed){
            	while(in.hasNextLine()){
            		tmp = StringUtils.splitPreserveAllTokens(in.nextLine(), '\t');
            		String source = tmp[0];
            		for(int i = 1; i < tmp.length; i += 3){
        				String target = tmp[i];
        				double weight = Double.parseDouble(tmp[i+1]);
        				String label = tmp[i+2];
        				LabeledWeightedEdge edge = g.addEdge(source, target);
        				edge.setWeight(weight);
        				edge.setLabel(label);
					}
            	}
            }else{
            	for (int i = 1; i <= nEdges; i++) {
            		tmp = StringUtils.splitPreserveAllTokens(in.nextLine(), '\t');
            		String source = tmp[0];
            		String target = tmp[1];
            		double weight = Double.parseDouble(tmp[2]);
            		String label = tmp[3];
            		LabeledWeightedEdge edge = g.addEdge(source, target);
            		edge.setWeight(weight);
            		edge.setLabel(label);
            	}
            }
            return new GraphSample(id, labels, g);
        }catch (RuntimeException e) {
            Logs.severe("Error while reading GraphSample from file " + file);
            throw e;
        }
    }

    public static File getGraphSampleFile(SamplePathResolver folder, long id) {
	    return folder.getSampleFile(id, getGraphSampleFilename(id));
    }
    public static File getGraphSampleFile(String folder, long id) {
        return new File(folder, getGraphSampleFilename(id));
    }
    public static String getGraphSampleFilename(long id) {
        return id + ".graphSample";
    }
    public static long getGraphSampleId(File graphSampleFile) {
        return Sample.getIdFromFile_(graphSampleFile);
    }

    public static GraphDataset loadFromFolder(String folder) {
        return loadFromFolder(new File(folder));
    }
    public static GraphDataset loadFromFolder(File folder) {
        return loadFromFolder(new SamplePathResolverSimple(folder));
    }
    public static GraphDataset loadFromFolder(SamplePathResolver pathResolver) {
	    Logs.finest("Loading GraphDataset " + pathResolver);
	    TimeWatcher timeWatcher = new TimeWatcher();
	    ArrayList<GraphSample> samples = new ArrayList<>();
	    forEachSampleInFolder(pathResolver, false, s -> samples.add(s));
	    Collections.sort(samples, Sample.COMPARATOR_BY_ID);
	    Logs.finest("GraphDataset was read after " + timeWatcher);
	    return new GraphDataset(samples);
	}
    public static int countFromFolder(File folder) {
        return countFromFolder(new SamplePathResolverSimple(folder));
    }
    public static int countFromFolder(SamplePathResolver pathResolver) {
	    return pathResolver.countFiles();
	}
    public static void forEachSampleInFolder(File folder, boolean parallel, Consumer<GraphSample> collector) {
    	forEachSampleInFolder(new SamplePathResolverSimple(folder), parallel, collector);
    }
    public static void forEachSampleInFolder(SamplePathResolver pathResolver, boolean parallel, Consumer<GraphSample> collector) {
    	pathResolver.forEachFile(parallel, sampleFile -> collector.accept(loadSampleFromFile(sampleFile)));
    }

    public static Iterator<GraphSample> iterateFromFolder(File folder) {
    	return iterateFromFolder(folder, null);
    }
    public static Iterator<GraphSample> iterateFromFolder(File folder, Consumer<GraphSample> postAction) {
    	return iterateFromFolder(new SamplePathResolverSimple(folder), postAction);
    }
    public static Iterator<GraphSample> iterateFromFolder(SamplePathResolver pathResolver, Consumer<GraphSample> postAction) {
        Logs.finest("Creating iterator for GraphDataset from " + pathResolver);
		Iterator<File> iterator = pathResolver.iterator();
		return Iterators.transform(iterator,
			postAction == null ?
        	(sampleFile -> loadSampleFromFile(sampleFile))
        	: (sampleFile -> {
    			GraphSample s = loadSampleFromFile(sampleFile);
    			postAction.accept(s);
    			return s;
    		})
		);
    }

	public static List<GraphSample> loadSubset(String samplesDir, List<Pair<String,String>> sampleFilenamesAndLabels) {
	    List<GraphSample> samples = new ArrayList<>();
		for (Pair<String,String> sampleFilenameAndLabel : sampleFilenamesAndLabels) {
			long id = Long.parseLong(sampleFilenameAndLabel.getA());
			try {
		        samples.add(loadSampleFromFile(getGraphSampleFile(samplesDir, id)));
		    } catch (Exception e) {
		        throw new RuntimeException("Unexpected error while loading GraphSample " + id, e);
		    }
	    }
		return samples;
	}
}
