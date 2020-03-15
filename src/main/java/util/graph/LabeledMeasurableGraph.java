package util.graph;

import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jgrapht.Graph;

public interface LabeledMeasurableGraph extends MeasurableGraph, Graph<String, LabeledWeightedEdge> {

    Double getVertexWeight(String vertex);

    boolean addVertex(String vertex, double weight);

    default void addEdgeOtherwiseWeight(String origin, String destine, double weight) {
    	LabeledWeightedEdge edge = getEdge(origin, destine);
        if(edge == null){
            edge = addEdge(origin, destine);
            edge.setWeight(weight);
        }else{
            edge.addWeight(weight);
        }
    }

    Map<String, MutableDouble> getVertexesWeights();

    Set<LabeledWeightedEdge> incomingEdgesOf(String vertex);

    Set<LabeledWeightedEdge> outgoingEdgesOf(String vertex);

    void normalizeWeights();

    void normalizeWeights(float min, float max);
}
