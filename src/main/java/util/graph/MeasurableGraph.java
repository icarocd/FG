package util.graph;

public interface MeasurableGraph {

	public default float getSize(boolean useWeightsIfApplicable) {
	    if (useWeightsIfApplicable && isWeighted()) {
	        //Here graph size is defined as the total of the node frequencies added to the total of the edge frequencies
	        return (float) (getSumNodesWeights() + getSumEdgesWeights());
	    } else {
	        return getNumVertices() + getNumEdges();
	    }
	}

	boolean isWeighted();

    int getNumEdges();

	int getNumVertices();

	double getSumEdgesWeights();

	double getSumNodesWeights();

	float getMaximumCommonSubgraphSizeTo(MeasurableGraph graphB, boolean useWeightsIfApplicable);
}
