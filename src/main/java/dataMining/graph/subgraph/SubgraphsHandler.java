package dataMining.graph.subgraph;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import dataMining.SamplePathResolver;
import dataMining.SamplePathResolverSimple;
import dataMining.distance.DistanceMeasurer;
import dataMining.graph.GraphDataset;
import dataMining.graph.GraphSample;
import util.ExceptionUtils;
import util.FileUtils;
import util.ListCollector;
import util.Logs;
import util.TimeWatcher;

public abstract class SubgraphsHandler {

	protected final DistanceMeasurer<SampleSubgraph> subgraphDistance;

	public SubgraphsHandler(DistanceMeasurer<SampleSubgraph> subgraphDistance) {
		this.subgraphDistance = subgraphDistance;
	}

	public DistanceMeasurer<SampleSubgraph> getSubgraphDistance(){
		return subgraphDistance;
	}

	public SamplePathResolver getSubgraphsDir(File graphsFolder){
		return new SamplePathResolverSimple(new File(graphsFolder.getPath()+"_subgraphs"));
	}

	public void extractAndSaveSamplesSubgraphs(File samplesFolder, File outputDir, int incremental) {
		extractAndSaveSamplesSubgraphs(new SamplePathResolverSimple(samplesFolder), outputDir, incremental);
	}
    public void extractAndSaveSamplesSubgraphs(SamplePathResolver samplesFolder, File outputDir, int incremental) {
        extractAndSaveSamplesSubgraphs(samplesFolder, new SamplePathResolverSimple(outputDir), incremental);
    }
	public void extractAndSaveSamplesSubgraphs(SamplePathResolver samplesFolder, SamplePathResolver outputDir, int incremental) {
		Preconditions.checkState(samplesFolder.exists(), samplesFolder + " must be an existing folder");
		outputDir.initialize(incremental > 0);
		if(incremental < 1)
			Logs.finest("Extracting subgraphs: "+outputDir);
		else
			Logs.finest("Checking subgraphs: "+outputDir);
		AtomicLong completed = new AtomicLong(), existing = new AtomicLong();
        TimeWatcher logPool = new TimeWatcher();
		samplesFolder.forEachFile(true, graphFile -> {
            File destineFile = null;
            boolean run = false;
            try {
		        long id = GraphDataset.getGraphSampleId(graphFile);
		        destineFile = getSampleSubgraphsFile(id, outputDir);
		        if(incremental < 1){
		        	run = true;
		        }else{
		        	if(!destineFile.exists()){
		        		run = true;
		        	}else if(incremental >= 2){
	        			if(destineFile.length() == 0){
		        			Logs.fine("empty subgraph file, maybe due to problem. recreating! "+destineFile);
		        			FileUtils.deleteQuietly(destineFile);
		        			run = true;
	        			}
		        	}
		        }
		        if(run){
                    extractAndSaveSampleSubgraphs(graphFile, destineFile);
                    completed.incrementAndGet();
                } else {
                    existing.incrementAndGet();
                }
                if(logPool.checkSecondsSpent()){
                    if(incremental > 0)
                        Logs.finest("now: " + completed + " graphs processed; " + existing + " existing");
                    else
                        Logs.finest("now: " + completed + " graphs processed");
                }
            }catch (Exception e){
            	if(run)
            		FileUtils.deleteQuietly(destineFile);
                throw ExceptionUtils.asRuntimeException(e);
            }
		});
		Logs.finest("subgraphs: " + completed + " graphs processed; " + existing + " existing");
	}

    public File getSampleSubgraphsFile(long id, SamplePathResolver subgraphsDir) {
        return subgraphsDir.getSampleFile(id, String.valueOf(id));
    }

	private void extractAndSaveSampleSubgraphs(File graphFile, File destineFile) {
		GraphSample sample = GraphDataset.loadSampleFromFile(graphFile);
		Iterator<SampleSubgraph> subgraphs = extractSubgraphsIterator(sample);
		PrintStream out = null;
		try{
			out = new PrintStream(destineFile);
			while(subgraphs.hasNext())
				out.append(getSubgraphAsStringLine(subgraphs.next())).append('\n');
			out.close();
		} catch (Exception e) {
			IOUtils.closeQuietly(out);
			FileUtils.deleteQuietly(destineFile);
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	public void loadSubgraphs(File inputFile, int maxLoads, Consumer<SampleSubgraph> collector) {
	    LineIterator lineIterator = null;
	    try{
	    	lineIterator = FileUtils.lineIteratorOfFile(inputFile);
	        PeekingIterator<String> peekingLineIterator = Iterators.peekingIterator(lineIterator);
            consumeMetadataFromFile(peekingLineIterator); //faz a leitura pular as linhas iniciais de metadados, se houver
            if (maxLoads < 0) { // unlimited
                while(peekingLineIterator.hasNext())
                    collector.accept(getSubgraphFromStringLine(peekingLineIterator.next()));
            } else {
                while(peekingLineIterator.hasNext() && maxLoads-- > 0)
                    collector.accept(getSubgraphFromStringLine(peekingLineIterator.next()));
            }
	    }catch(Exception e){
	    	throw new RuntimeException("error while reading subgraphs: "+inputFile, e);
	    }finally{
            LineIterator.closeQuietly(lineIterator);
        }
	}
	public List<SampleSubgraph> loadSubgraphs(File inputFile, int maxLoads) {
	    ListCollector<SampleSubgraph> subgraphs = new ListCollector<>();
	    loadSubgraphs(inputFile, maxLoads, subgraphs);
	    return subgraphs.getElements();
	}
	public List<SampleSubgraph> loadSubgraphs(long id, SamplePathResolver subgraphsDir) {
        return loadSubgraphs(getSampleSubgraphsFile(id, subgraphsDir), -1);
    }

    public Map<String,String> loadMetadataFromSubgraphsFile(File subgraphsFile) {
	    LineIterator lineIterator = FileUtils.lineIteratorOfFile(subgraphsFile);
	    try{
            return consumeMetadataFromFile(Iterators.peekingIterator(lineIterator));
        }finally{
            lineIterator.close();
        }
    }
	private Map<String, String> consumeMetadataFromFile(PeekingIterator<String> peekingLineIterator) {
	    LinkedHashMap<String,String> metadata = new LinkedHashMap<>();
	    while(peekingLineIterator.hasNext()){
	        if(!peekingLineIterator.peek().startsWith("#"))
	            break;
            String[] pieces = peekingLineIterator.next().substring(1).split("=");
            metadata.put(pieces[0], pieces[1]);
        }
	    return metadata;
    }

    public List<SampleSubgraph> extractSubgraphs(GraphSample sample){
    	return IteratorUtils.toList(extractSubgraphsIterator(sample));
    }
	public abstract Iterator<SampleSubgraph> extractSubgraphsIterator(GraphSample sample);

    protected abstract CharSequence getSubgraphAsStringLine(SampleSubgraph subgraph);

    protected abstract SampleSubgraph getSubgraphFromStringLine(String sampleSubgraphAsString);

    public void append(SampleSubgraph sampleSubgraph, Writer writer) {
        append(getSubgraphAsStringLine(sampleSubgraph), writer);
    }
    public void append(CharSequence sampleSubgraphAsString, Writer writer) {
        try {
            writer.append(sampleSubgraphAsString).append('\n');
        } catch (IOException e) {
        	throw ExceptionUtils.asRuntimeException(e);
        }
    }

    /**
     * @param subgraphs
     */
    public void retainComplexSubgraphs(List<SampleSubgraph> subgraphs) {
        throw new UnsupportedOperationException("not supported yet");
    }

    /**
     * @param subgraphs
     * @param maxElements
     */
    public <T extends SampleSubgraph> List<T> reduceToMostImportant(List<T> subgraphs, int maxElements) {
        throw new UnsupportedOperationException("not supported yet");
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
