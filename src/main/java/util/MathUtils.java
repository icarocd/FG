package util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import org.apache.commons.lang3.mutable.MutableInt;
import com.google.common.base.Preconditions;

public class MathUtils
{
	private static final int ONE_MEGA_BYTE_IN_BYTES = 1024 * 1024;
    private static DecimalFormat percentFormatter;

	public static BigDecimal asBigDecimal(Object value) {
	    if (value instanceof BigDecimal) {
	        return (BigDecimal) value;
	    }
	    if (value instanceof Double) {
	    	return new BigDecimal(value.toString());
	    }
	    if (value instanceof Number) {
	        return new BigDecimal(((Number) value).doubleValue());
	    }
		if (value != null) {
			String s = value.toString();
			if (!s.isEmpty()) {
				return new BigDecimal(s);
			}
		}
	    return null;
	}

	public static BigInteger asBigInteger(Object value) {
	    if (value instanceof BigInteger) {
	        return (BigInteger) value;
	    }
	    if (value instanceof BigDecimal) {
	        return ((BigDecimal) value).toBigInteger();
	    }
	    if (value instanceof Double) {
	        return new BigDecimal(Double.toString((Double) value)).toBigInteger();
	    }
	    if (value instanceof Number) {
	        return BigInteger.valueOf(((Number) value).longValue());
	    }
	    if (value != null) {
			String s = value.toString();
			if (!s.isEmpty()) {
				return new BigInteger(s);
			}
		}
	    return null;
	}

	/** Extracts a integer value from the input, accepting Number, String etc */
    public static Integer asInteger(Object value) {
        return asInteger(value, null);
    }

	/** Extracts a integer value from the input, accepting Number, String etc */
	public static Integer asInteger(Object value, Integer defaultValue) {
	    if (value instanceof Number) {
	        return ((Number) value).intValue();
	    }
        if (value != null) {
            try {
                String s = value.toString();
                if(StringUtils.isNotBlank(s)){
                    return Integer.valueOf(s);
                }
            } catch (Exception e) {}
        }
        return defaultValue;
	}

	/** Extracts a long value from the input, accepting Number, String etc */
	public static Long asLong(Object value) {
	    if (value instanceof Number) {
	        return ((Number) value).longValue();
	    }
	    try {
	        return StringUtils.isBlank(value.toString()) ? null : Long.valueOf(value.toString());
	    } catch (Exception e) {
	        return null;
	    }
	}

	/** Extracts a float value from the input, accepting Number, String etc */
	public static Float asFloat(Object value) {
	    if (value instanceof Number) {
	        return ((Number) value).floatValue();
	    }
	    try {
	        return StringUtils.isBlank(value.toString()) ? null : Float.valueOf(value.toString());
	    } catch (Exception e) {
	        return null;
	    }
	}

	public static float[] asFloatArrayPrimitive(String[] values) {
        return asFloatArrayPrimitive(values, 0, values.length - 1);
    }
	public static float[] asFloatArrayPrimitive(String[] values, int init, int end) {
        float[] floats = new float[end - init + 1];
        int idx = 0;
        while(init <= end) {
            floats[idx++] = Float.parseFloat(values[init++]);
        }
        return floats;
    }

	public static <T extends Number> double[] asDoubleArray(Collection<T> collection){
		return collection.stream().mapToDouble(Number::doubleValue).toArray();
	}
	public static double[] asDoubleArray(float[] collection){
		int n;
		double[] output = new double[n = collection.length];
	    for (int i = 0; i < n; i++)
	        output[i] = collection[i];
	    return output;
	}

	public static Double round(Double v, int scale, RoundingMode roundingMode) {
        return v == null ? null : round(asBigDecimal(v), scale, roundingMode).doubleValue();
    }
	public static Float round(Float v, int scale, RoundingMode roundingMode) {
        return v == null ? null : round(asBigDecimal(v), scale, roundingMode).floatValue();
    }
    public static BigDecimal round(BigDecimal v, int scale, RoundingMode roundingMode) {
        return v == null ? null : v.setScale(scale, roundingMode);
    }

	/**
	 * Generates an array from 0 to length-1, inclusive. Exemple for length 4: [0 1 2 3].
	 */
	public static List<Integer> range(int length) {
		List<Integer> range = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			range.add(i);
		}
		return range;
	}

	/** An Iterable for interval [0, to[ */
	public static Iterable<Integer> rangeIterable(int to){
		return rangeIterable(0, to);
	}

	/** An Iterable for interval [from, to[ */
	public static Iterable<Integer> rangeIterable(int from, int to){
		return () -> new Iterator<>() {
		    private int current = from;
		    public boolean hasNext() {
		        return current < to;
		    }
		    public Integer next() {
		        return current++;
		    }
		    public void remove() {
		        throw new UnsupportedOperationException();
		    }
		};
	}

	/** Returns the logarithm of a for base 2 */
	public static double log2(double a) {
		return Math.log(a) / Math.log(2);
	}

	/** use lucene's version */
	@Deprecated
	public static double logANaBaseB(double a, double b)
	{
		if (b == Math.E) { //desnecessario fazer conversao de base neste caso
			return Math.log(a);
		}

		//log a na base b = log a na base W dividido por log b na base W (pra qualquer W)
		return Math.log(a) / Math.log(b);
	}

	/** Returns the minimum value in the array a[], +infinity if no such value */
	public static double min(double[] elements) {
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < elements.length; i++) {
			if(elements[i] < min)
                min = elements[i];
		}
		return min;
	}
	public static double min(Collection<Double> elements) {
		double min = Double.POSITIVE_INFINITY;
		for (Double a : elements) {
			if(a < min)
                min = a;
		}
		return min;
	}
	public static int minInt(Collection<Integer> elements){
		int min = Integer.MAX_VALUE;
		for (Integer a : elements) {
			if(a < min)
                min = a;
		}
		return min;
	}
	public static float minFloat(Collection<Float> elements){
		float min = Float.POSITIVE_INFINITY;
		for (Float a : elements) {
			if(a < min)
                min = a;
		}
		return min;
	}

	public static <T> T min(Collection<T> elements, Comparator<T> comparator) {
		T min = null;
		for (T el : elements) {
			if (min==null || comparator.compare(el, min) < 0){
                min = el;
            }
		}
		return min;
	}

	/** Returns the maximum value in the array a[], -infinity if no such value */
	public static double max(double[] elements) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < elements.length; i++) {
			if(elements[i] > max)
                max = elements[i];
		}
		return max;
	}
	public static float max(float[] elements) {
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < elements.length; i++) {
			if(elements[i] > max)
                max = elements[i];
		}
		return max;
	}
	public static <T> double max(Iterable<T> elements, Function<T,Double> transformer) {
        double max = Double.NEGATIVE_INFINITY;
        for(T el : elements){
            double v = transformer.apply(el);
            if(v > max)
                max = v;
        }
        return max;
    }
	public static <T> float maxFloat(Iterable<T> elements, Function<T,Float> transformer) {
        float max = Float.NEGATIVE_INFINITY;
        for(T el : elements){
            float v = transformer.apply(el);
            if(v > max)
                max = v;
        }
        return max;
    }
	public static int maxInt(Iterable<Integer> elements) {
        int max = Integer.MIN_VALUE;
        for(Integer v : elements){
            if(v > max)
                max = v;
        }
        return max;
    }
	public static <T> int maxInt(Iterable<T> elements, Function<T,Integer> transformer) {
        int max = Integer.MIN_VALUE;
        for(T el : elements){
            int v = transformer.apply(el);
            if(v > max)
                max = v;
        }
        return max;
    }

	public static Pair<Double,Double> minMax(Iterable<Double> elements) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (double v : elements) {
			if(v < min)
                min = v;
            if(v > max)
				max = v;
		}
		return new Pair(min,max);
	}
	public static Pair<Double,Double> minMax(double[] elements) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double v : elements) {
            if(v < min)
                min = v;
            if(v > max)
                max = v;
        }
        return new Pair(min,max);
    }
	public static <T> Pair<Double,Double> minMax(Iterable<T> elements, Function<T,Double> transformer) {
	    double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (T el : elements) {
            double v = transformer.apply(el);
            if(v < min)
                min = v;
            if(v > max)
                max = v;
        }
        return new Pair(min,max);
	}
	public static <T> Pair<Float,Float> minMaxFloat(Iterable<T> elements, Function<T,Float> transformer) {
	    float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (T el : elements) {
            float v = transformer.apply(el);
            if(v < min)
                min = v;
            if(v > max)
                max = v;
        }
        return new Pair(min,max);
	}

	/** Returns the maximum value in the collection, or null if no such value. */
	public static <T extends Comparable<? super T>> T max(T[] elements) {
		T max = null;
		for (T el : elements) {
			if (max==null || el.compareTo(max) > 0) {
                max = el;
            }
		}
		return max;
	}

	/** Returns the maximum value in the collection, or null if no such value. */
	public static <T extends Comparable<? super T>> T max(Collection<T> elements) {
		T max = null;
		for (T el : elements) {
			if (max==null || el.compareTo(max) > 0) {
                max = el;
            }
		}
		return max;
	}

	public static <T> T max(Collection<T> elements, Comparator<T> comparator) {
		T max = null;
		for (T el : elements) {
			if (max==null || comparator.compare(el, max) > 0){
                max = el;
            }
		}
		return max;
	}

	/* value' = (max'-min')/(max-min)*(value-max)+max'
	 * optimizing:
	 * a = (max'-min')/(max-min)
	 * b = max' - a*max
	 * value' = a*value - a*max + max' = a*value + (max' - a*max) = a*value + b
	 */
	public static <T> void normalizeFloat(Iterable<T> elements, Function<T,Float> getter, BiConsumer<T,Float> setter) {
		normalizeFloat(elements, getter, setter, 0, 1);
	}
	public static <T> void normalizeFloat(Iterable<T> elements, Function<T,Float> getter, BiConsumer<T,Float> setter, float min_new, float max_new) {
		Pair<Float,Float> minMax = minMaxFloat(elements, getter);
		float min = minMax.getA(), max = minMax.getB(), range = max - min;
		if(range == 0){
            for(T element : elements)
            	setter.accept(element, max);
        }else{
        	float a = (max_new - min_new) / range;
        	if(a == 1) return; //already in desired interval
        	float b = max_new - a*max;
			for(T element : elements)
                setter.accept(element, a*getter.apply(element) + b);
        }
	}
	public static <T> void normalize(Iterable<T> elements, Function<T,Double> getter, BiConsumer<T,Double> setter, double min_new, double max_new) {
		Pair<Double,Double> minMax = minMax(elements, getter);
		double min = minMax.getA(), max = minMax.getB(), range = max - min;
		if(range == 0){
            for(T element : elements)
            	setter.accept(element, max);
        }else{
        	double a = (max_new - min_new) / range;
        	if(a == 1) return; //already in desired interval
        	double b = max_new - a*max;
			for(T element : elements)
                setter.accept(element, a*getter.apply(element) + b);
        }
	}
	public static void normalize(double[] elements, double min_new, double max_new) {
        Pair<Double,Double> minMax = minMax(elements);
        double min = minMax.getA(), max = minMax.getB(), range = max - min;
        if(range == 0){
            for (int i = 0; i < elements.length; i++)
                elements[i] = max;
        }else{
            double a = (max_new - min_new) / range;
            if(a == 1) return; //already in desired interval
            double b = max_new - a*max;
            for (int i = 0; i < elements.length; i++)
                elements[i] = a*elements[i] + b;
        }
    }

	/** 2ab/(a+b). We return 0 when 2ab = 0 */
	public static float harmonicMean(float a, float b) {
	    float temp = 2F*a*b;
	    if(temp == 0F) //treatment to avoid division by 0
	        return 0F;
        return temp / (a+b);
    }

	public static float[] harmonicMeans(float[] a, float[] v) {
        Preconditions.checkArgument(a.length == v.length);
        float[] harmonicMeans = new float[a.length];
        for (int i = 0; i < harmonicMeans.length; i++)
            harmonicMeans[i] = harmonicMean(a[i], v[i]);
        return harmonicMeans;
    }

	public static long mean(long[] values) {
	    return sum(values) / values.length;
	}
	public static float mean(float[] values) {
		return sum(values) / values.length;
	}
	public static float mean(Float[] values) {
		return sum(values) / values.length;
	}
	public static double mean(double[] values) {
        return sum(values) / values.length;
    }
	public static <T> float mean(Collection<T> values, Function<T, Float> transformer) {
	    return sumFloat(values, transformer) / values.size();
	}
    public static double mean(Collection<Double> values) {
        return sum(values) / values.size();
    }
    public static <T extends Number> float meanFloat(Collection<T> values) {
        return sumFloat(values) / values.size();
    }
    public static int meanInt(Collection<Integer> values) {
        return sumInt(values) / values.size();
    }

    public static long sum(long[] values) {
	    long total = 0;
        for (long element : values) {
            total += element;
        }
        return total;
    }
    public static int sum(Collection<MutableInt> values) {
        int total = 0;
        for (MutableInt element : values) {
            total += element.intValue();
        }
        return total;
    }
	public static float sum(float[] values) {
		float total = 0;
		for (float element : values) {
			total += element;
		}
		return total;
	}
	public static float sum(Float[] values) {
		float total = 0;
		for (float element : values) {
			total += element;
		}
		return total;
	}
	public static double sum(double[] values) {
        double total = 0;
        for(double element : values)
            total += element;
        return total;
    }
	public static <T> double sum(Collection<T> values, Function<T, Double> transformer) {
        double total = 0;
        for(T element : values)
            total += transformer.apply(element);
        return total;
    }
	public static <T> float sumFloat(Collection<T> values, Function<T, Float> transformer) {
	    float total = 0;
        for(T element : values)
            total += transformer.apply(element);
        return total;
	}
	public static <T extends Number> int sumInt(Collection<T> values) {
	    int total = 0;
        for(T element : values)
            total += element.intValue();
        return total;
	}
	public static double sum(Iterable<Double> values) {
        double total = 0;
        for(Double element : values)
            total += element;
        return total;
    }
	public static <T extends Number> float sumFloat(Iterable<T> values) {
        float total = 0;
        for(Number element : values)
            total += element.floatValue();
        return total;
    }

	public static double variance(Collection<Double> values) {
		return variance(values, mean(values));
	}
	public static double variance(float[] values) {
	    return variance(values, mean(values));
	}
	public static double variance(Float[] values) {
        return variance(values, mean(values));
    }
	public static double variance(double[] values) {
        return variance(values, mean(values));
    }

	public static double variance(float[] values, float mean) {
	    float temp = 0;
        for (float a : values) {
            float diff = a - mean;
            temp += diff * diff;
        }
        return temp / values.length;
    }
	public static double variance(Float[] values, float mean) {
        float temp = 0;
        for (float a : values) {
            float diff = a - mean;
            temp += diff * diff;
        }
        return temp / values.length;
    }
	public static double variance(double[] values, double mean) {
        double temp = 0;
        for (double a : values) {
            double diff = a - mean;
            temp += diff * diff;
        }
        return temp / values.length;
    }
	public static double varianceFloat(Collection<Float> values, float mean) {
		float temp = 0;
		for (Float a : values) {
			float diff = a - mean;
			temp += diff * diff;
		}
		return temp / values.size();
	}
	public static <T> double variance(Collection<T> values, Function<T, Float> transformer, float mean) {
	    float temp = 0;
        for (T a : values) {
            float diff = transformer.apply(a) - mean;
            temp += diff * diff;
        }
        return temp / values.size();
	}
	private static double variance(Collection<Double> values, double mean) {
		double temp = 0;
		for (Double a : values) {
			double diff = a - mean;
			temp += diff * diff;
		}
		return temp / values.size();
	}


	public static double standardDeviation(float[] values) {
	    return standardDeviation(variance(values));
	}
	public static double standardDeviation(Float[] values) {
        return standardDeviation(variance(values));
    }
	public static double standardDeviation(double[] values) {
        return standardDeviation(variance(values));
    }
	public static double standardDeviation(Collection<Double> values) {
        return standardDeviation(variance(values));
    }

	public static double standardDeviation(double variance) {
		return Math.sqrt(variance);
	}

    public static Pair<Float,Double> meanAndStandardDeviation(Collection<Float> values) {
    	float mean = meanFloat(values);
        double variance = varianceFloat(values, mean);
        double stdDev = standardDeviation(variance);
        return new Pair<>(mean, stdDev);
	}

    public static String printMeanAndStandardDeviation(float[] values, boolean asPercent) {
        float mean = mean(values);
        double variance = variance(values, mean);
        double stdDev = standardDeviation(variance);
        return printMeanAndStandardDeviation(mean, stdDev, asPercent);
    }
    public static String printMeanAndStandardDeviation(double[] values, boolean asPercent) {
        double mean = mean(values);
        double variance = variance(values, mean);
        double stdDev = standardDeviation(variance);
        return printMeanAndStandardDeviation(mean, stdDev, asPercent);
    }
    public static String printMeanAndStandardDeviationFloat(Collection<Float> values, boolean asPercent) {
    	float mean = meanFloat(values);
        double variance = varianceFloat(values, mean);
        double stdDev = standardDeviation(variance);
        return printMeanAndStandardDeviation(mean, stdDev, asPercent);
    }
    public static String printMeanAndStandardDeviation(double mean, double stdDev, boolean asPercent) {
        if(asPercent)
            return printPercent(mean) + " +- " + printPercent(stdDev);
        else
            return print(mean) + " +- " + print(stdDev);
    }

    public static String printPercent(double percent) {
        return getDecimalFormatter().format(percent * 100D);
    }
    public static String print(double v) {
        return getDecimalFormatter().format(v);
    }

    private static synchronized NumberFormat getDecimalFormatter() {
        if (percentFormatter == null) {
            percentFormatter = new DecimalFormat();
            percentFormatter.setMinimumFractionDigits(2);
            percentFormatter.setMaximumFractionDigits(2);
            percentFormatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
            percentFormatter.setRoundingMode(RoundingMode.UP);
        }
        return percentFormatter;
    }

    public static long bytesToMB(long bytes) {
		return bytes / ONE_MEGA_BYTE_IN_BYTES;
	}

    /**
     * @return true if minMax null or min <= value <= max
     */
	public static boolean isInRange(int value, Pair<Integer, Integer> minMax) {
		return minMax == null || (value >= minMax.getA().intValue() && value <= minMax.getB().intValue());
	}

	public static float ndcg(int numSamplesFromClass, int rankSize, IntPredicate relevanceByRankIndex) {
		float idcg = idcg(numSamplesFromClass, rankSize);
        if(idcg == 0F)
        	return 0;
        float dcg = dcg(rankSize, relevanceByRankIndex);
        assert dcg <= idcg;
        return dcg / idcg;
	}

	private static float idcg(int maxRelevants, int rankSize) {
		IntPredicate idealRelevanceByRankIndex;
		if (maxRelevants >= rankSize) { //treat the ranked list as if all were relevant
			idealRelevanceByRankIndex = asIntPredicateTrue();
		} else { //treat the ranked list as if all were relevant up to maxRelevants
			idealRelevanceByRankIndex = asIntPredicateTrueLessThan(maxRelevants);
		}
		return dcg(rankSize, idealRelevanceByRankIndex);
	}

	private static float dcg(int rankSize, IntPredicate relevanceByRankIndex) {
		final double logOf2 = Math.log(2);
        double dcg = 0;
        for (int k = 0; k < rankSize; k++) {
            if(relevanceByRankIndex.test(k)){
            	//summation of: (2^relevance - 1) / (log2(i+1)) -> [1/(log(i+1)/log(2))] for relevance 1 otherwise 0 -> log(2)/log(i+1) -> log(2)/log(k+2)
                dcg += (logOf2 / Math.log(k + 2));
            }
        }
        return (float)dcg;
	}

	public static IntPredicate asIntPredicateTrue() {
		return k -> true;
	}
	public static IntPredicate asIntPredicateTrueLessThan(int limit) {
		return k -> k < limit;
	}
	public static IntPredicate asIntPredicate(boolean... array) {
		return k -> array[k];
	}

	public static boolean isEven(int x){
		return (x & 1) == 0;
	}
	public static boolean isOdd(int x){
		return (x & 1) != 0;
	}
	public static boolean isPositiveEven(int x){
		return x > 0 && isEven(x);
	}
	public static boolean isPositiveOdd(int x){
		return x > 0 && isOdd(x);
	}

	public static float[] concatMatrixLines(float[][] matrix){
		int nLines = matrix.length, nColumns = matrix[0].length;
		float[] array = new float[nLines * nColumns]; //lines are concatenated one after another
        for (int i = 0; i < nLines; i++)
        	System.arraycopy(matrix[i], 0, array, i * nColumns, nColumns);
        return array;
	}

	public static void forRange(int startInclusive, int endExclusive, IntConsumer consumer){
		forRange(startInclusive, endExclusive, false, consumer);
	}
	public static void forRange(int startInclusive, int endExclusive, boolean parallel, IntConsumer consumer){
		IntStream range = IntStream.range(startInclusive, endExclusive);
		if(parallel)
			range = range.parallel();
		range.forEach(consumer);
	}
}
