package fusionGraph;

import java.util.logging.Level;
import util.Logs;

public class Main {

    public static void main(String[] args) {
    	Configs params = Configs.parse(args);
    	Logs.init(Level.parse(params.get("logLevel", "FINEST")));
        String dataset = params.assertParam("dataset");

		if(params.getBoolean("genFusedGraphs")) //gera, por amostra, um grafo que agrega seus rankings por descritor
        	new RankAggregation(dataset, params).aggregateRanks(dataset, params.incremental());
        if(params.getBoolean("rankFromFusedGraphsByQuerying")) //a partir dos fusion graphs, gera rankings usando-os como consulta e resposta, e entao os avalia
            new RankAggregation(dataset, params).rankFromFusedGraphsByQuerying(dataset, params);
    }
}
