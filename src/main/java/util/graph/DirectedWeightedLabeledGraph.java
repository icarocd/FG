package util.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import dataMining.distance.graphDistance.MaximumCommonSubgraphCreator;
import util.DataStructureUtils;
import util.MathUtils;
import util.ObjectUtils;

/**
 * Grafo direcionado, em que: <BR>
 * - há suporte a pesos de nó e de aresta
 * - há suporte a rótulos de aresta
 * - há suporte a multiplas arestas por nós de origem-destino, desde que sob label diferente
 */
public class DirectedWeightedLabeledGraph extends DirectedWeightedMultigraph<String, LabeledWeightedEdge> implements LabeledMeasurableGraph {

    private Map<String,MutableDouble> vertexesWeights;

    private DirectedWeightedLabeledGraph() {
        super(new ClassBasedEdgeFactory<String, LabeledWeightedEdge>(LabeledWeightedEdge.class));
    }

    public DirectedWeightedLabeledGraph(boolean weighted) {
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

	/**
	 * @return true if graph became empty (0 vertices)
	 */
	public boolean prune(Collection<String> verticesToDiscard, boolean reconnectOrphanEdges) {
		for (String term : verticesToDiscard) {
            if (containsVertex(term)) {

            	//antes da remocao do termo, refaz as ligacoes dos nos de entrada do termo removido, aos nós de saida do termo removido:
	            if(reconnectOrphanEdges){
	            	Set<LabeledWeightedEdge> incomingEdges = incomingEdgesOf(term);
		            Set<LabeledWeightedEdge> outgoingEdges = outgoingEdgesOf(term);

		            for (LabeledWeightedEdge outgoingEdge : outgoingEdges) {
		                String outgoingTerm = (String) outgoingEdge.getTarget();
		                for (LabeledWeightedEdge incomingEdge : incomingEdges) {
		                    String incomingTerm = (String) incomingEdge.getSource();
	                        if (!outgoingTerm.equals(incomingTerm)) {
	                            double inducedWeight = (incomingEdge.getWeight() + outgoingEdge.getWeight()) / 2.0;
	                            LabeledWeightedEdge existingEdge = getEdge(incomingTerm, outgoingTerm);
	                            if (existingEdge == null) {
	    	                        LabeledWeightedEdge edge = addEdge(incomingTerm, outgoingTerm);
	                                setEdgeWeight(edge, inducedWeight);
	    	                    } else {
	    	                    	setEdgeWeight(existingEdge, (existingEdge.getWeight() + inducedWeight) / 2.0);
	    	                    }
		                    }
		                }
		            }
	            }

	            removeVertex(term, true);
	            if(vertexesWeights.isEmpty()) //if graph ended up with no vertices, interrupt term pruning (no need to continue)
	                return true;
	        }
        }

		//limpa caches locais do grafo, que ficam defasados apos remocao de nó!
		ObjectUtils.writeField(this, "unmodifiableVertexSet", null);
		ObjectUtils.writeField(this, "unmodifiableEdgeSet", null);

		return false;
	}

	/**
     * @return true if graph became empty (0 vertices)
     */
    public boolean pruneWorstWeightedTerms(int maxNodesToRetain) {
        if(vertexesWeights.size() > maxNodesToRetain){
            Set<String> verticesToDiscard;
            {
                Set<String> bestTerms = DataStructureUtils.getMapKeysSortedByValue(vertexesWeights, false, maxNodesToRetain);

                verticesToDiscard = new LinkedHashSet<>(vertexSet());
                verticesToDiscard.removeAll(bestTerms);
            }
            if (!verticesToDiscard.isEmpty()) {
                return prune(verticesToDiscard, true);
            }
        }
        return false;
    }

    @Override
    public float getMaximumCommonSubgraphSizeTo(MeasurableGraph graphB, boolean useWeightsIfApplicable) {
    	return MaximumCommonSubgraphCreator.getMaximumCommonSubgraphSize(this, (DirectedWeightedLabeledGraph)graphB, useWeightsIfApplicable);
    }
}
