package dataMining.distance;

import dataMining.distance.graphDistance.GraphDistanceType;
import dataMining.distance.graphDistance.GraphSampleDistanceMeasurer;

public abstract class SampleDistanceMeasurer<T> implements DistanceMeasurer<T> {

	public abstract float getSimilarity(T sampleA, T sampleB);

	public String toString() {
		return getName();
	}

	public String getName() {
        if(this instanceof GraphSampleDistanceMeasurer){
            GraphDistanceType type = ((GraphSampleDistanceMeasurer)this).getGraphDistanceType();
            if(GraphDistanceType.MCS == type)
                return "MCS";
            if(GraphDistanceType.WGU == type)
                return "WGU";
            throw new IllegalArgumentException("not implemented yet for: " + type);
        }
        throw new IllegalArgumentException("not implemented yet for: " + getClass().getName());
    }
	public static <T> SampleDistanceMeasurer<T> get(String name) {
		if ("MCS".equalsIgnoreCase(name))
		    return (SampleDistanceMeasurer<T>) new GraphSampleDistanceMeasurer(GraphDistanceType.MCS);
		if ("WGU".equalsIgnoreCase(name))
            return (SampleDistanceMeasurer<T>) new GraphSampleDistanceMeasurer(GraphDistanceType.WGU);
		throw new IllegalArgumentException("unsupported type: " + name);
	}
}
