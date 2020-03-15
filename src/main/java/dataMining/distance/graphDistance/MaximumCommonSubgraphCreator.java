package dataMining.distance.graphDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import util.graph.DirectedWeightedLabeledGraph;
import util.graph.LabeledWeightedEdge;
import util.graph.WeightedLabeledGraph;

public class MaximumCommonSubgraphCreator {

    public static float getMaximumCommonSubgraphSize(DirectedWeightedLabeledGraph graphA, DirectedWeightedLabeledGraph graphB, boolean useWeightsIfApplicable) {
        DirectedWeightedLabeledGraph mcs = getMaximumCommonSubgraph(graphA, graphB, useWeightsIfApplicable);
        return mcs != null ? mcs.getSize(useWeightsIfApplicable) : 0;
    }
    public static DirectedWeightedLabeledGraph getMaximumCommonSubgraph(DirectedWeightedLabeledGraph graphA, DirectedWeightedLabeledGraph graphB, boolean useWeightsIfApplicable) {
        boolean considerWeights = useWeightsIfApplicable && graphA.isWeighted();
        DirectedWeightedLabeledGraph mcs = null;

        //add and collect the commons vertexes:
        List<String> commonVertexes = new ArrayList();
        {
        	Set<String> verticesB = graphB.vertexSet();
        	for(String vertex : graphA.vertexSet()){
				if(verticesB.contains(vertex)){ //if true, it is a common vertex
					if(mcs == null) //lazy graph creation, because in general many graphs have nothing in common and we can avoid creating graphs...
						mcs = new DirectedWeightedLabeledGraph(considerWeights);
					commonVertexes.add(vertex);
    				mcs.addVertex(vertex, considerWeights ? Math.min(graphA.getVertexWeight(vertex), graphB.getVertexWeight(vertex)) : 1);
        		}
        	}
        }
        if(mcs == null) //if no vertexes added, return size 0
            return null;

        for (String commonVertex : commonVertexes) {
            Set<LabeledWeightedEdge> outgoingEdgesInGraphA = graphA.outgoingEdgesOf(commonVertex);
            if(outgoingEdgesInGraphA.isEmpty())
                continue;
            Set<LabeledWeightedEdge> outgoingEdgesInGraphB = graphB.outgoingEdgesOf(commonVertex);
			if(outgoingEdgesInGraphB.isEmpty())
            	continue;
            for(LabeledWeightedEdge edge : outgoingEdgesInGraphA){
                if(outgoingEdgesInGraphB.contains(edge)){ //if true, it is a common edge. IMPORTANTE que o equals e hashCode do edge não levem em consideração o peso de aresta!!!
                    String targetVertex = (String)edge.getTarget();
                    String label = edge.getLabel();
                    LabeledWeightedEdge e = mcs.addEdge(commonVertex, targetVertex);
                    e.setLabel(label);
                    if(considerWeights){
                        double edgeWeightA = edge.getWeight();
                        double edgeWeightB = graphB.getEdgeWeight(commonVertex, targetVertex, label);
                        mcs.setEdgeWeight(e, Math.min(edgeWeightA, edgeWeightB));
                    }
                }
            }
        }

        return mcs;
    }

    public static float getMaximumCommonSubgraphSize(WeightedLabeledGraph graphA, WeightedLabeledGraph graphB, boolean useWeightsIfApplicable) {
        WeightedLabeledGraph mcs = getMaximumCommonSubgraph(graphA, graphB, useWeightsIfApplicable);
        return mcs != null ? mcs.getSize(useWeightsIfApplicable) : 0;
    }
    public static WeightedLabeledGraph getMaximumCommonSubgraph(WeightedLabeledGraph graphA, WeightedLabeledGraph graphB, boolean useWeightsIfApplicable) {
        boolean considerWeights = useWeightsIfApplicable && graphA.isWeighted();
        WeightedLabeledGraph mcs = null;

        //add and collect the commons vertexes:
        List<String> commonVertexes = new ArrayList();
        {
            Set<String> verticesB = graphB.vertexSet();
            for(String vertex : graphA.vertexSet()){
                if(verticesB.contains(vertex)){ //if true, it is a common vertex
                    if(mcs == null) //lazy graph creation, because in general many graphs have nothing in common and we can avoid creating graphs...
                        mcs = new WeightedLabeledGraph(considerWeights);
                    commonVertexes.add(vertex);
                    mcs.addVertex(vertex, considerWeights ? Math.min(graphA.getVertexWeight(vertex), graphB.getVertexWeight(vertex)) : 1);
                }
            }
        }
        if(mcs == null) //if no vertexes added, return size 0
            return null;

        for(LabeledWeightedEdge edgeA : graphA.edgeSet()){
            String source = (String)edgeA.getSource();
            String target = (String)edgeA.getTarget();
            LabeledWeightedEdge edgeB = graphB.getEdge(source, target, edgeA.getLabel());
            if(edgeB != null){
                double weight = considerWeights ? Math.min(edgeA.getWeight(), edgeB.getWeight()) : 1;
                mcs.addEdge(source, target, new LabeledWeightedEdge(source, target, edgeA.getLabel(), weight));
            }
        }

        return mcs;
    }

    public static float getMinimumCommonSupergraphSize(DirectedWeightedLabeledGraph graphA, DirectedWeightedLabeledGraph graphB, boolean useWeightsIfApplicable) {
    	return getMinimumCommonSupergraph(graphA, graphB, useWeightsIfApplicable).getSize(useWeightsIfApplicable);
    }
    public static DirectedWeightedLabeledGraph getMinimumCommonSupergraph(DirectedWeightedLabeledGraph graphA, DirectedWeightedLabeledGraph graphB, boolean useWeightsIfApplicable) {
    	boolean considerWeights = useWeightsIfApplicable && graphA.isWeighted();
    	if(!considerWeights) throw new UnsupportedOperationException("to be implemented");

        DirectedWeightedLabeledGraph minimumCommonSupergraph = new DirectedWeightedLabeledGraph(considerWeights);

        for(String vertex : graphA.vertexSet()){
        	minimumCommonSupergraph.addVertex(vertex, graphA.getVertexWeight(vertex));
        }
        for(String vertex : graphB.vertexSet()){
        	minimumCommonSupergraph.addVertexIfNewOtherwiseMaximizeWeight(vertex, graphB.getVertexWeight(vertex));
        }

        for(LabeledWeightedEdge edge : graphA.edgeSet()){
        	String source = (String)edge.getSource();
        	String target = (String)edge.getTarget();
			minimumCommonSupergraph.addEdge(source, target, new LabeledWeightedEdge(source, target, edge.getLabel(), edge.getWeight()));
        }
        for(LabeledWeightedEdge edge : graphB.edgeSet()){
        	String source = (String)edge.getSource();
        	String target = (String)edge.getTarget();
        	LabeledWeightedEdge existingEdge = minimumCommonSupergraph.getEdge(source, target, edge.getLabel());
			if(existingEdge != null){
				if(existingEdge.getWeight() < edge.getWeight()){
					existingEdge.setWeight(edge.getWeight());
				}
			}else{
				minimumCommonSupergraph.addEdge(source, target, new LabeledWeightedEdge(source, target, edge.getLabel(), edge.getWeight()));
			}
        }

        return minimumCommonSupergraph;
    }
}
