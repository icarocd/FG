package dataMining.distance.graphDistance;

import util.graph.MeasurableGraph;

public enum GraphDistanceType {
    MCS {
        public float calculateSimilarity(MeasurableGraph graphA, MeasurableGraph graphB) {
            return mcsSimilarity(graphA, graphB, true);
        }
    },
    MCSNOTWEIGHTED {
        public float calculateSimilarity(MeasurableGraph graphA, MeasurableGraph graphB) {
            return mcsSimilarity(graphA, graphB, false);
        }
    },
    WGU {
        public float calculateSimilarity(MeasurableGraph graphA, MeasurableGraph graphB) {
        	return wguSimilarity(graphA, graphB, true);
        }
    },
    WGUNOTWEIGHTED {
        public float calculateSimilarity(MeasurableGraph graphA, MeasurableGraph graphB) {
            return wguSimilarity(graphA, graphB, false);
        }
    };

    protected float mcsSimilarity(MeasurableGraph graphA, MeasurableGraph graphB, boolean useWeightsIfApplicable) {
        if(graphA.equals(graphB))
            return 1F;
        float mcsSize = graphA.getMaximumCommonSubgraphSizeTo(graphB, useWeightsIfApplicable);
        if(mcsSize == 0F)
            return 0F;
        float graphSizeA = graphA.getSize(useWeightsIfApplicable);
        float graphSizeB = graphB.getSize(useWeightsIfApplicable);
        return mcsSize / Math.max(graphSizeA, graphSizeB);
    }

    protected float wguSimilarity(MeasurableGraph graphA, MeasurableGraph graphB, boolean useWeightsIfApplicable) {
        if(graphA.equals(graphB))
            return 1F;
        float mcsSize = graphA.getMaximumCommonSubgraphSizeTo(graphB, useWeightsIfApplicable);
        if(mcsSize == 0F)
            return 0F;
        float graphSizeA = graphA.getSize(useWeightsIfApplicable);
        float graphSizeB = graphB.getSize(useWeightsIfApplicable);
        return (mcsSize / (graphSizeA + graphSizeB - mcsSize));
    }

    public abstract float calculateSimilarity(MeasurableGraph graphA, MeasurableGraph graphB);

    public final float calculateDistance(MeasurableGraph graphA, MeasurableGraph graphB){
    	return 1F - calculateSimilarity(graphA, graphB);
    }

    public static GraphDistanceType get(String name) {
        for(GraphDistanceType t : values())
            if(t.name().equalsIgnoreCase(name))
                return t;
        return null;
    }

    public static GraphDistanceType[] getByNames(String[] names) {
        GraphDistanceType[] graphTypes = new GraphDistanceType[names.length];
        for(int i = 0; i < names.length; i++)
            graphTypes[i] = get(names[i]);
        return graphTypes;
    }
}
