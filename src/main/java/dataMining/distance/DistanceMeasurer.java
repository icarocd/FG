package dataMining.distance;

public interface DistanceMeasurer<T> {

	public abstract float getDistance(T sampleA, T sampleB);

	public default float getMaxDistance(){
		return 1F;
	}
}
