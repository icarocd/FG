package fusionGraph;

import java.util.List;
import java.util.function.Function;
import dataMining.retrieval.RankedList;
import util.Pair;
import util.graph.DirectedWeightedLabeledGraph;
import util.graph.LabeledMeasurableGraph;

public class FusionGraphCreator {

    public LabeledMeasurableGraph create(Long queryId, List<RankedList> ranks, Function<Long,List<RankedList>> responsesRanks){
    	LabeledMeasurableGraph g = createBeforeNormalization(queryId, ranks, responsesRanks);

    	//PS: antes de fundir, precisamos ter certeza que os ranks estejam normalizados. depois, idem.
        g.normalizeWeights();

		return g;
    }
    private LabeledMeasurableGraph createBeforeNormalization(@SuppressWarnings("unused") Long queryId, List<RankedList> ranks, Function<Long,List<RankedList>> responsesRanks){
        LabeledMeasurableGraph g = new DirectedWeightedLabeledGraph(true);

        // Nodes are composed by the results that occur in the ranks from the query q.
 		// The weight of node v(A) is given by the sum of similarities that the response item A has in the ranks of q.
        for(RankedList rank : ranks){
        	List<Pair<Long,Float>> rank_ = rank.getRank();
            for(int idx = 0; idx < rank_.size(); idx++){
            	Pair<Long,Float> a_simQA = rank_.get(idx);
				Long a = a_simQA.getA();
            	Float sim_qa = a_simQA.getB();
            	//int pos_a = idx+1;

                addVertex(g, a, sim_qa);
            }
        }

    	//for each result of each rank, obtain its rank and use it to create edges:
    	for(RankedList rank : ranks){
    		List<Pair<Long,Float>> rank_ = rank.getRank();
    		for(int idx = 0; idx < rank_.size(); idx++){
    			Pair<Long,Float> a_simQA = rank_.get(idx);
    			Long a = a_simQA.getA();
    			//Float simQA = a_simQA.getB();
    			int posA = idx+1;

    			String a_ = a.toString();
    			List<RankedList> ranks_a = responsesRanks.apply(a);
    			for(RankedList rank_a : ranks_a) {
    				for(Pair<Long,Float> b_simAB : rank_a){
    					String b_ = b_simAB.getA().toString();
    					Float simAB = b_simAB.getB();
    					addEdge(g, a_, posA, /*simQA,*/ b_, simAB);
    				}
    			}
    			//print(g);
    		}
        }

        return g;
    }

	private void addVertex(LabeledMeasurableGraph g, Long a, Float weight) {
        g.addVertex(a.toString(), weight);
    }

	private void addEdge(LabeledMeasurableGraph g, String a_, int posA, /*Float simQA,*/ String b_, Float simAB){
    	if(!g.containsVertex(a_) || !g.containsVertex(b_) || a_.equals(b_))
    		return;
        double weight = simAB / posA;
        g.addEdgeOtherwiseWeight(a_, b_, weight);
	}

	public String getSetupInfoSufix(){
        return "_edgeWeightSymABDividedByPosA";
	}
}
