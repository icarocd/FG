package util.graph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class GraphConverter {

    public static ListenableGraph asListenableGraph(Graph graph) {
        if (graph instanceof ListenableGraph) {
            return (ListenableGraph) graph;
        }

        if(graph instanceof DirectedGraph){
            if (graph instanceof WeightedGraph) {
                return new ListenableDirectedWeightedGraph((WeightedGraph) graph);
            }
            return new ListenableDirectedGraph((DirectedGraph) graph);
        }

        if(graph instanceof WeightedGraph){
            return new ListenableUndirectedWeightedGraph((WeightedGraph) graph);
        }
        return new ListenableUndirectedGraph((UndirectedGraph) graph);
    }
}
