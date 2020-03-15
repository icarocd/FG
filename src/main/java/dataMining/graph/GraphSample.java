package dataMining.graph;

import java.util.Set;
import dataMining.Sample;
import util.DataStructureUtils;
import util.graph.LabeledMeasurableGraph;

public class GraphSample extends Sample {

    private LabeledMeasurableGraph graph;

    public GraphSample(long id, String label, LabeledMeasurableGraph graph) {
        this(id, DataStructureUtils.asSetUnit(label), graph);
    }
    public GraphSample(long id, Set<String> labels, LabeledMeasurableGraph graph) {
        super(id, labels);
        this.graph = graph;
    }

    public LabeledMeasurableGraph getGraph() {
        return graph;
    }

    public boolean containsVertex(String vertex) {
    	return graph.containsVertex(vertex);
    }

    public boolean containsEdge(String source, String target) {
    	return graph.containsEdge(source, target);
    }
}
