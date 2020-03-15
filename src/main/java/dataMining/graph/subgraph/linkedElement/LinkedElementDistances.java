package dataMining.graph.subgraph.linkedElement;

import dataMining.distance.DistanceMeasurer;
import dataMining.distance.graphDistance.GraphDistanceType;
import util.StringUtils;

public enum LinkedElementDistances implements DistanceMeasurer<LinkedElement> {

	MCS {
		public float getDistance(LinkedElement a, LinkedElement b) {
			return GraphDistanceType.MCS.calculateDistance(a, b);
		}
	},
	WGU {
		public float getDistance(LinkedElement a, LinkedElement b) {
			return GraphDistanceType.WGU.calculateDistance(a, b);
		}
	};

	public static LinkedElementDistances get(String functionName, LinkedElementDistances defaultValue) {
		if(StringUtils.isNotBlank(functionName))
			return LinkedElementDistances.valueOf(functionName.toUpperCase());
		return defaultValue;
	}

	public boolean isNeighborNodeWeightsRequired() {
        return this == MCS || this == WGU;
    }
}
