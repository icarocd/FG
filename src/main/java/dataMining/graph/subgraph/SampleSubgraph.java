package dataMining.graph.subgraph;

import dataMining.distance.DistanceMeasurer;
import util.graph.MeasurableGraph;

public abstract class SampleSubgraph implements MeasurableGraph {

    public abstract float calculateDistance(DistanceMeasurer<SampleSubgraph> distanceFunction, SampleSubgraph sampleSubgraph);
}
