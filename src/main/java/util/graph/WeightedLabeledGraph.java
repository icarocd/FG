package util.graph;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.WeightedMultigraph;
import dataMining.distance.graphDistance.MaximumCommonSubgraphCreator;
import util.DataStructureUtils;
import util.MathUtils;

/**
 * Grafo não direcionado, em que: <BR>
 * - há suporte a pesos de nó e de aresta
 * - há suporte a rótulos de aresta
 * - há suporte a multiplas arestas entre dois nós, desde que sob label diferente
 */
public class WeightedLabeledGraph extends WeightedMultigraph<String, LabeledWeightedEdge> implements LabeledMeasurableGraph {

    private Map<String,MutableDouble> vertexesWeights;

    private WeightedLabeledGraph() {
        super(new ClassBasedEdgeFactory<String, LabeledWeightedEdge>(LabeledWeightedEdge.class));
    }

    public WeightedLabeledGraph(boolean weighted) {
        this();
        if (weighted){
            vertexesWeights = new LinkedHashMap<>();
        }
    }

    public boolean isWeighted() {
		return vertexesWeights != null;
	}

    @Override
	public boolean addVertex(String vertex) {
	    return addVertex(vertex, 1);
	}

	public boolean addVertex(String vertex, double weight) {
	    boolean r = super.addVertex(vertex);
	    if (vertexesWeights != null){
            MutableDouble w = vertexesWeights.get(vertex);
            if(w == null){
                vertexesWeights.put(vertex, new MutableDouble(weight));
            }else{
                w.add(weight);
            }
	    }
	    return r;
	}
	public void addVertexIfNewOtherwiseMaximizeWeight(String vertex, double newWeight){
		MutableDouble weight = vertexesWeights.get(vertex);
		if(weight != null) {
			if(weight.getValue() < newWeight)
				weight.setValue(newWeight);
		}else{
			super.addVertex(vertex);
			vertexesWeights.put(vertex, new MutableDouble(newWeight));
		}
	}

	@Override
	public boolean removeVertex(String v) {
	    boolean removed = super.removeVertex(v);
	    if (removed && vertexesWeights != null)
	        DataStructureUtils.decrementMapValueDouble(vertexesWeights, v);
	    return removed;
	}

	public boolean removeVertex(String v, boolean prune) {
	    if(!prune)
	        return removeVertex(v);

	    boolean removed = super.removeVertex(v);
        if (removed && vertexesWeights != null)
            vertexesWeights.remove(v);
        return removed;
	}

	public int getNumVertices() {
		return vertexSet().size();
	}

	public int getNumEdges() {
		return edgeSet().size();
	}

	@Override
	public double getSumNodesWeights() {
		return DataStructureUtils.getSumMapValueDouble(vertexesWeights);
	}

    /** Retorna a primeira aresta com origem-destino-label especificados */
    public LabeledWeightedEdge getEdge(String source, String target, String label) {
        return DataStructureUtils.findFirst(getAllEdges(source, target), e -> Objects.equals(e.getLabel(), label));
    }

	public Map<String, MutableDouble> getVertexesWeights() {
		return vertexesWeights;
	}

    public Double getVertexWeight(String vertex) {
        if(vertexesWeights == null){
            return 1D;
        }
	    MutableDouble weight = vertexesWeights.get(vertex);
	    return weight == null ? null : weight.doubleValue();
	}

	public Set<String> getKBestWeightedVertices(int k) {
		Set<String> bestTerms = new HashSet<>();
		if(vertexesWeights.size() < k || k < 0){
			bestTerms.addAll(vertexesWeights.keySet());
		}else{
			List<Entry<String, MutableDouble>> verticesSortedByWeight = DataStructureUtils.getMapEntriesSortedByValue(vertexesWeights, false);
			for (int i = 0; i < verticesSortedByWeight.size() && bestTerms.size() < k; i++) {
				String term = verticesSortedByWeight.get(i).getKey();
				bestTerms.add(term);
			}
		}
		return bestTerms;
	}

    public double getSumEdgesWeights() {
	    double sum = 0;
	    for(LabeledWeightedEdge edge : edgeSet())
	        sum += edge.getWeight();
	    return sum;
	}

	public Double getEdgeWeight(String sourceVertex, String targetVertex, String label) {
	    LabeledWeightedEdge e = getEdge(sourceVertex, targetVertex, label);
	    return e==null ? null : e.getWeight();
	}

	public void normalizeWeights() {
		normalizeWeights(0F,  1F);
	}
	public void normalizeWeights(float min, float max) {
		MathUtils.normalize(vertexesWeights.values(), MutableDouble::getValue, MutableDouble::setValue, min, max);
		MathUtils.normalize(edgeSet(), LabeledWeightedEdge::getWeight, LabeledWeightedEdge::setWeight, min, max);
	}

    @Override
    public float getMaximumCommonSubgraphSizeTo(MeasurableGraph graphB, boolean useWeightsIfApplicable) {
    	return MaximumCommonSubgraphCreator.getMaximumCommonSubgraphSize(this, (WeightedLabeledGraph)graphB, useWeightsIfApplicable);
    }
}
