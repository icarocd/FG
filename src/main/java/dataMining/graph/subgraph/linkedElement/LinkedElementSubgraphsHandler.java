package dataMining.graph.subgraph.linkedElement;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.Iterators;
import dataMining.distance.DistanceMeasurer;
import dataMining.graph.GraphSample;
import dataMining.graph.subgraph.SampleSubgraph;
import dataMining.graph.subgraph.SubgraphsHandler;
import util.DataStructureUtils;
import util.MathUtils;
import util.graph.LabeledMeasurableGraph;
import util.graph.LabeledWeightedEdge;
import java.util.Set;

public class LinkedElementSubgraphsHandler extends SubgraphsHandler {

    private final boolean alsoConsidererIncomingEdges;

    public LinkedElementSubgraphsHandler(LinkedElementDistances distanceFunction, boolean alsoConsidererIncomingEdges) {
    	super((DistanceMeasurer)distanceFunction);
        this.alsoConsidererIncomingEdges = alsoConsidererIncomingEdges;
    }

    public LinkedElementDistances getDistanceFunction(){
    	return (LinkedElementDistances)(DistanceMeasurer)getSubgraphDistance();
    }

    @Override
    public Iterator<SampleSubgraph> extractSubgraphsIterator(GraphSample sample){
    	LabeledMeasurableGraph graph = sample.getGraph();
        boolean requireNeighborNodeWeights = isNeighborNodeWeightsRequired();
        return Iterators.transform(graph.vertexSet().iterator(), vertex -> createSubgraph(vertex, graph, requireNeighborNodeWeights));
    }

    private boolean isNeighborNodeWeightsRequired(){
    	return getDistanceFunction().isNeighborNodeWeightsRequired();
	}

	private SampleSubgraph createSubgraph(String vertex, LabeledMeasurableGraph graph, boolean requireNeighborNodeWeights) {
        float vertexWeight = graph.getVertexWeight(vertex).floatValue();

        //extracts weights:
        Set<LabeledWeightedEdge> edges = alsoConsidererIncomingEdges ? graph.edgesOf(vertex) : graph.outgoingEdgesOf(vertex);
        Map<String,Float> edgesWeights = new LinkedHashMap<>(edges.size(), 1);
        Map<String,Float> neighborsWeights = requireNeighborNodeWeights ? new LinkedHashMap<>(edges.size(), 1) : null;
        for(LabeledWeightedEdge edge : edges){
            String neighborVertex = (String) edge.getTarget();
            float edgeWeight = (float) edge.getWeight();
            edgesWeights.put(neighborVertex, edgeWeight);
            if(requireNeighborNodeWeights)
                neighborsWeights.put(neighborVertex, graph.getVertexWeight(neighborVertex).floatValue());
        }

        return new LinkedElement(vertex, vertexWeight, edgesWeights, neighborsWeights);
    }

	@Override
	protected CharSequence getSubgraphAsStringLine(SampleSubgraph subgraph) {
	    boolean requireNeighborNodeWeights = isNeighborNodeWeightsRequired();

		LinkedElement s = (LinkedElement) subgraph;
		StringBuilder stringForm = new StringBuilder(s.getElement()).append(" ").append(s.elementWeight);
        for (Entry<String, Float> edgeElementAndWeight : s.edgesWeights.entrySet()) {
            String neighborElement = edgeElementAndWeight.getKey();
			stringForm.append(" ").append(neighborElement).append(" ").append(edgeElementAndWeight.getValue());
			if(requireNeighborNodeWeights)
			    stringForm.append(" ").append(s.neighborsWeights.get(neighborElement));
        }
        return stringForm;
	}
	@Override
	protected SampleSubgraph getSubgraphFromStringLine(String stringForm) {
		try{
			String[] chunks = stringForm.trim().split("\\s+");
	        int chunkIdx = 0;
	        String element = chunks[chunkIdx++];
	        float elementWeight = MathUtils.asFloat(chunks[chunkIdx++]);
	        Map<String, Float> edgesWeights;
	        Map<String, Float> neighborsWeights;
	        if(isNeighborNodeWeightsRequired()){
	        	int nEdges = (chunks.length - chunkIdx) / 3;
	        	edgesWeights = new LinkedHashMap<>(nEdges, 1);
		        neighborsWeights = new LinkedHashMap<>(nEdges, 1);
		        while(chunkIdx < chunks.length) {
		            String t = chunks[chunkIdx++];
					edgesWeights.put(t, MathUtils.asFloat(chunks[chunkIdx++]));
		            neighborsWeights.put(t, MathUtils.asFloat(chunks[chunkIdx++]));
		        }
	        }else{
	        	int nEdges = (chunks.length - chunkIdx) / 2;
	        	edgesWeights = new LinkedHashMap<>(nEdges, 1);
		        neighborsWeights = null;
		        while(chunkIdx < chunks.length) {
		            String t = chunks[chunkIdx++];
					edgesWeights.put(t, MathUtils.asFloat(chunks[chunkIdx++]));
		        }
	        }
	        return new LinkedElement(element, elementWeight, edgesWeights, neighborsWeights);
		}catch (Exception e){
			throw new RuntimeException("error reading subgraph line: "+stringForm, e);
		}
    }

	@Override
	public void retainComplexSubgraphs(List<SampleSubgraph> subgraphs) {
	    for (Iterator<SampleSubgraph> it = subgraphs.iterator(); it.hasNext();) {
            LinkedElement subgraph = (LinkedElement)it.next();
            if(subgraph.getNumEdges() == 0)
                it.remove();
        }
	}

	@Override
    public <T extends SampleSubgraph> List<T> reduceToMostImportant(List<T> subgraphs_, int maxElements) {
    	if(maxElements < 0 || subgraphs_.size() < maxElements)
    		return subgraphs_;
    	List<LinkedElement> subgraphs = (List<LinkedElement>)subgraphs_;
    	Collections.sort(subgraphs, LinkedElement.COMPARATOR_BY_VERTEX_WEIGHT);
    	subgraphs = DataStructureUtils.subListEnd(subgraphs, maxElements);
    	return (List<T>)subgraphs;
    }
}
