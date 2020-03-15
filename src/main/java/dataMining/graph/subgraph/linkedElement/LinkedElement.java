package dataMining.graph.subgraph.linkedElement;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import dataMining.distance.DistanceMeasurer;
import dataMining.graph.subgraph.SampleSubgraph;
import util.DataStructureUtils;
import util.graph.MeasurableGraph;

public class LinkedElement extends SampleSubgraph {

	public static final Comparator<LinkedElement> COMPARATOR_BY_VERTEX_WEIGHT = ((o1,o2) -> Float.compare(o1.elementWeight, o2.elementWeight));

	private final String element;
    protected float elementWeight;
    protected final Map<String,Float> edgesWeights; //incident vertices and its correspondent edge's weight
    protected final Map<String,Float> neighborsWeights;

    public LinkedElement(String element, float elementWeight, Map<String, Float> edgesWeights, Map<String, Float> neighborsWeights) {
        this.element = element;
        this.elementWeight = elementWeight;
        this.edgesWeights = edgesWeights;
        this.neighborsWeights = neighborsWeights;
    }

    public String getElement() {
        return element;
    }

    public float getElementWeight() {
        return elementWeight;
    }

    @Override
    public boolean isWeighted() {
        return neighborsWeights != null;
    }

    @Override
    public int getNumVertices() {
    	return 1 + edgesWeights.size();
    }

    @Override
    public int getNumEdges() {
    	return edgesWeights.size();
    }

    @Override
    public double getSumEdgesWeights() {
    	return DataStructureUtils.getSumMapValueDouble(edgesWeights);
    }

    public void replaceWeights(float newWeight) {
    	elementWeight = newWeight;
    	edgesWeights.replaceAll((k,v) -> newWeight);
    	neighborsWeights.replaceAll((k,v) -> newWeight);
    }

    public Set<String> incidentElements() {
        return edgesWeights.keySet();
    }

    @Override
    public double getSumNodesWeights() {
    	double sum = elementWeight;
    	for(Float w : neighborsWeights.values())
    		sum += w;
    	return sum;
    }

    private Float getNeighborWeight(String neighbor) {
		return neighborsWeights.get(neighbor);
	}

    @Override
    public float calculateDistance(DistanceMeasurer<SampleSubgraph> distanceFunction, SampleSubgraph subgraph2) {
        if (this == subgraph2)
            return 0;
        return distanceFunction.getDistance(this, subgraph2);
    }

    @Override
	public float getMaximumCommonSubgraphSizeTo(MeasurableGraph graph2, boolean useWeightsIfApplicable) {
    	LinkedElement g2 = (LinkedElement)graph2;
    	boolean considerWeights = useWeightsIfApplicable && isWeighted();

        if(!considerWeights){
        	if(element.equals(g2.element)) //neste caso, cada vizinho comum contribui com tanto 1 nó comum a mais quanto uma aresta comum a mais
        		return 1 + 2 * DataStructureUtils.intersectionCount(edgesWeights.keySet(), g2.edgesWeights.keySet());
    		int commonNodes = DataStructureUtils.intersectionCount(edgesWeights.keySet(), g2.edgesWeights.keySet());
    		boolean edges1ContainsElement2 = edgesWeights.keySet().contains(g2.element);
    		boolean edges2ContainsElement1 = g2.edgesWeights.keySet().contains(element);
    		if(edges1ContainsElement2)
    			commonNodes++;
    		if(edges2ContainsElement1){
    			commonNodes++;
    			if(edges1ContainsElement2) //quando ambos true, existe aresta no MCS entre T1 e T2
    				return commonNodes + 1;
    		}
    		return commonNodes;
        } else { //tamanho do MCS será a soma dos pesos dos seus nós e arestas, onde o peso de um nó é o mínimo dentre os nos grafos origem (idem para aresta)
        	float size;
        	if(element.equals(g2.element)){
        		size = Math.min(elementWeight, g2.elementWeight);
        		for(Entry<String,Float> edgeWeight : edgesWeights.entrySet()){
        			String neighbor = edgeWeight.getKey();
        			Float g2NeighborEdgeWeight = g2.edgesWeights.get(neighbor);
        			if(g2NeighborEdgeWeight != null){
        				size += Math.min(edgeWeight.getValue(), g2NeighborEdgeWeight);
        				size += Math.min(getNeighborWeight(neighbor), g2.getNeighborWeight(neighbor));
        			}
        		}
        	}else{
        		size = 0;
        		for(Entry<String,Float> edgeWeight : edgesWeights.entrySet()){
        			String neighbor = edgeWeight.getKey();
        			Float g2NeighborEdgeWeight = g2.edgesWeights.get(neighbor);
        			if(g2NeighborEdgeWeight != null)
        				size += Math.min(getNeighborWeight(neighbor), g2.getNeighborWeight(neighbor));
        		}
        		Float weightElement1InGraph2 = g2.neighborsWeights.get(element);
        		Float weightElement2InGraph1 = neighborsWeights.get(g2.element);
        		if(weightElement1InGraph2 != null)
        			size += Math.min(elementWeight, weightElement1InGraph2);
        		if(weightElement2InGraph1 != null){
        			size += Math.min(g2.elementWeight, weightElement2InGraph1);
        			if(weightElement1InGraph2 != null) //quando ambos true, existe aresta no MCS entre T1 e T2
        				return size + Math.min(edgesWeights.get(g2.element), g2.edgesWeights.get(element));
        		}
        	}
        	return size;
        }
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result + Float.floatToIntBits(elementWeight);
        result = prime * result + ((edgesWeights == null) ? 0 : edgesWeights.hashCode());
        result = prime * result + ((neighborsWeights == null) ? 0 : neighborsWeights.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof LinkedElement))
            return false;
        LinkedElement other = (LinkedElement) obj;
        if (!Objects.equals(element, other.element))
            return false;
        if (Float.floatToIntBits(elementWeight) != Float.floatToIntBits(other.elementWeight))
            return false;
        if (!Objects.equals(edgesWeights, other.edgesWeights))
            return false;
        if (!Objects.equals(neighborsWeights, other.neighborsWeights))
            return false;
        return true;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
