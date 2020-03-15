package util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Ordering;

public class DataStructureUtils {

	public static <T> boolean isEmpty(T[] c) {
        return c == null || c.length == 0;
    }
	public static boolean isEmpty(Map<?,?> c) {
        return c == null || c.isEmpty();
    }
	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}
	public static <T> boolean isEmpty(Iterator<?> c) {
        return c == null || !c.hasNext();
    }

	public static <T> boolean isNotEmpty(T[] c) {
        return !isEmpty(c);
    }
	public static boolean isNotEmpty(Map<?,?> map) {
	    return !isEmpty(map);
	}
    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }
    public static boolean isNotEmpty(Iterator<?> c) {
        return !isEmpty(c);
    }

    /** utility for null-safe loop */
    public static <T> void forEach(Collection<T> collection, Consumer<T> consumer) {
        if(collection != null)
            for(T element : collection)
                consumer.accept(element);
    }
    public static <T> void forEach(T[] collection, Consumer<T> consumer) {
        if(collection != null)
            for(T element : collection)
                consumer.accept(element);
    }

    public static <A, B> ArrayList<B> collectAsList(A[] c, Function<A, B> t) {
        return collectAsList(c, t, true);
    }
    public static <A, B> ArrayList<B> collectAsList(Collection<A> c, Function<A, B> t) {
        return collectAsList(c, t, true);
    }
    public static <A, B> ArrayList<B> collectAsList(Collection<A> c, Function<A, B> t, boolean collectNull) {
        if(isEmpty(c))
            return new ArrayList<>(0);
        ArrayList<B> container = new ArrayList<>(c.size());
        collect(c.iterator(), container, t, collectNull);
        return container;
    }
    public static <A, B> ArrayList<B> collectAsList(Iterable<A> c, Function<A, B> t) {
        return collectAsList(c, t, true);
    }
    public static <A, B> ArrayList<B> collectAsList(Iterable<A> c, Function<A, B> t, boolean collectNull) {
    	if(c==null)
    		return new ArrayList<>(0);
    	ArrayList<B> container = new ArrayList<>();
        collect(c.iterator(), container, t, collectNull);
        return container;
    }
    public static <A, B> ArrayList<B> collectAsList(A[] c, Function<A, B> t, boolean collectNull) {
    	if(isEmpty(c))
            return new ArrayList<>(0);
    	return collectAsList(c, 0, c.length, t, collectNull);
    }
    public static <A, B> ArrayList<B> collectAsList(A[] c, int initInclusive, int endExclusive, Function<A, B> t, boolean collectNull) {
        Preconditions.checkArgument(initInclusive <= endExclusive);
    	if(isEmpty(c) || initInclusive==endExclusive)
            return new ArrayList<>(0);
        ArrayList<B> container = new ArrayList<>(endExclusive - initInclusive);
        collect(c, container, initInclusive, endExclusive, t, collectNull);
        return container;
    }
    public static <A, B> LinkedHashSet<B> collectAsSet(Collection<A> c, Function<A, B> t) {
        return collectAsSet(c, t, true);
    }
    public static <A, B> LinkedHashSet<B> collectAsSet(Collection<A> c, Function<A, B> t, boolean collectNull) {
        if(DataStructureUtils.isEmpty(c))
            return new LinkedHashSet<>(0);
        LinkedHashSet<B> container = new LinkedHashSet<>(c.size());
        collect(c.iterator(), container, t, collectNull);
        return container;
    }
    public static <A, B> LinkedHashSet<B> collectAsSet(Stream<A> c, Function<A,B> t){
    	return collectAsSet(c, t, true);
    }
    public static <A, B> LinkedHashSet<B> collectAsSet(Stream<A> c, Function<A,B> t, boolean collectNull){
    	LinkedHashSet<B> set = new LinkedHashSet<>();
    	collect(c, set, t, collectNull);
    	return set;
	}
	public static <A, B> SortedSet<B> collectAsSortedSet(Stream<A> c, Function<A, B> t, boolean collectNull) {
    	return collectAsSortedSet(c==null ? null : c.iterator(), t, collectNull);
    }
    public static <A, B> SortedSet<B> collectAsSortedSet(Collection<A> c, Function<A, B> t, boolean collectNull) {
    	return collectAsSortedSet(c==null ? null : c.iterator(), t, collectNull);
    }
    public static <A, B> SortedSet<B> collectAsSortedSet(Iterator<A> c, Function<A, B> t, boolean collectNull) {
        if(DataStructureUtils.isEmpty(c))
            return new TreeSet<>();
        TreeSet<B> container = new TreeSet<>();
        collect(c, container, t, collectNull);
        return container;
    }
    public static <A,B,C> Map<B,C> collectAsMap(A[] elements, Function<A,B> keyTransformer, Function<A,C> valueTransformer){
    	if(isEmpty(elements))
    		return new LinkedHashMap(0);
    	return collectToMap(elements, keyTransformer, valueTransformer, new LinkedHashMap(elements.length));
	}
    public static <A,B,C> Map<B,C> collectAsMap(Stream<A> elements, Function<A,B> keyTransformer, Function<A,C> valueTransformer){
    	return collectToMap(elements, keyTransformer, valueTransformer, new LinkedHashMap());
    }
    public static <A,B,X,Y> Map<X,Y> collectAsMap(Map<A,B> map, Function<A,X> c1, Function<B,Y> c2){
		return collectAsMap(map.entrySet().stream(), entry -> c1.apply(entry.getKey()), entry -> c2.apply(entry.getValue()));
	}
    public static <A,B,C> Map<B,C> collectToMap(A[] elements, Function<A,B> keyTransformer, Function<A,C> valueTransformer, Map<B,C> map){
    	for(A element : elements)
			map.put(keyTransformer.apply(element), valueTransformer.apply(element));
    	return map;
	}
    public static <A,B,C> Map<B,C> collectToMap(Stream<A> elements, Function<A,B> keyTransformer, Function<A,C> valueTransformer, Map<B,C> map){
    	elements.forEach(element ->
			map.put(keyTransformer.apply(element), valueTransformer.apply(element))
		);
    	return map;
	}
	public static <A, B> Collection<B> collect(Iterator<A> collection, Collection<B> container, Function<A, B> t) {
        return collect(collection, container, t, true);
    }
    public static <A, B> Collection<B> collect(Iterator<A> iterator, Collection<B> container, Function<A, B> t, boolean collectNull) {
        if (isNotEmpty(iterator) && container != null) {
            while(iterator.hasNext()) {
				A a = iterator.next();
				B b = t.apply(a);
				if(collectNull || b != null)
					container.add(b);
			}
        }
        return container;
    }
    public static <A, B> Collection<B> collect(Stream<A> elements, Collection<B> container, Function<A,B> t, boolean collectNull){
    	elements.forEach(element -> {
    		B obj = t.apply(element);
    		if(collectNull || obj != null)
    			container.add(obj);
    	});
		return container;
	}
    public static <A, B> void collect(A[] collection, Collection<B> container, int initInclusive, int endExclusive, Function<A, B> t, boolean collectNull) {
    	Preconditions.checkArgument(initInclusive <= endExclusive);
    	if (isNotEmpty(collection) && container != null) {
            for(int i = initInclusive; i < endExclusive; i++) {
            	B b = t.apply(collection[i]);
            	if(collectNull || b != null)
            		container.add(b);
			}
        }
    }

	@SafeVarargs
	public static <T> List<T> asList(T... elements) {
		List<T> list = new ArrayList<>(elements.length);
		for (int i = 0; i < elements.length; i++) {
			list.add(elements[i]);
		}
		return list;
	}

	@SafeVarargs
	public static <T> Set<T> asSet(T... elements) {
		int size = elements==null ? 0 : elements.length;
		LinkedHashSet<T> set = new LinkedHashSet<>(size);
		for(int i = 0; i < size; i++)
			set.add(elements[i]);
		return set;
	}

	public static <T extends Comparable<T>> SortedSet<T> asSortedSet(T[] elements) {
	    TreeSet<T> sortedSet = new TreeSet<>();
	    addAll(sortedSet, elements);
	    return sortedSet;
	}
	public static <T extends Comparable<T>> SortedSet<T> asSortedSet(Stream<T> elements) {
		return elements.collect(Collectors.toCollection(TreeSet::new));
	}

	public static <T> LinkedHashSet<T> asLinkedSet(Stream<T> elements) {
		return elements.collect(Collectors.toCollection(LinkedHashSet::new));
	}

    public static <C extends Collection<T>, T> C addAll(C container, T[] elements) {
        for (T element : elements) {
	        container.add(element);
	    }
        return container;
    }
    public static <C extends Collection<T>, T> C addAll(C container, Stream<T> elements) {
        for (Iterator<T> iterator = elements.iterator(); iterator.hasNext(); ) {
			container.add(iterator.next());
		}
        return container;
    }
    public static <C extends Collection<T>, T> C addAll(C container, Collection<T> elements) {
        container.addAll(elements);
        return container;
    }

    public static <T> List<T> asListUnit(T element) {
    	ArrayList<T> set = new ArrayList<>(1);
        set.add(element);
        return set;
	}

	public static <T> Set<T> asSetUnit(T element) {
		Set<T> set = new HashSet<>(1);
        set.add(element);
        return set;
	}

	public static <X, Y> LinkedHashMap<X, Y> asMap(Object... entries) {
		LinkedHashMap<X, Y> map = new LinkedHashMap<>(entries.length / 2);
	    addToMap(map, entries);
	    return map;
	}

	public static <K,V> Map<K,V> asMapUnit(K key, V value) {
        Map<K,V> map = new HashMap<>(1);
        map.put(key, value);
        return map;
    }

	public static Map<String,String> asMapString(Object... entries) {
        Map<String,String> map = new LinkedHashMap<>(entries.length / 2);
        addToMapString(map, entries);
        return map;
    }

    public static void addToMap(Map map, Object... entries) {
        for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i], entries[i + 1]);
        }
    }

    public static void addToMapString(Map<String,String> map, Object... entries) {
        for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i].toString(), entries[i + 1].toString());
        }
    }

    /** Indexa os elementos conforme chave definida pelo transformer especificado */
    public static <A, B> Map<B, A> index(Collection<A> collection, Function<A, B> transformer) {
        HashMap<B, A> map = new HashMap<>();
        for (A element : collection) {
            map.put(transformer.apply(element), element);
        }
        return map;
    }
    public static <A, B> Map<B, A> index(A[] collection, Function<A, B> transformer) {
        HashMap<B, A> map = new HashMap<>();
        for (A element : collection) {
            map.put(transformer.apply(element), element);
        }
        return map;
    }

	/** Agrupa os itens da coleção conforme uma chave que é definida pelo transformer indicado. */
	public static <A, B> LinkedHashMap<B, List<A>> group(Collection<A> collection, Function<A, B> transformer) {
		LinkedHashMap<B, List<A>> map = new LinkedHashMap<>();
		for (A element : collection) {
			putOnListValue(map, transformer.apply(element), element);
		}
		return map;
	}
	public static <A, B> LinkedHashMap<B, List<A>> group(A[] collection, Function<A, B> transformer) {
        LinkedHashMap<B, List<A>> map = new LinkedHashMap<>();
        for (A element : collection) {
            putOnListValue(map, transformer.apply(element), element);
        }
        return map;
    }

	public static <CHAVE, VALOR> void putOnListValue(Map<CHAVE, List<VALOR>> map, CHAVE key, VALOR value) {
		getOrCreateListValue(map, key).add(value);
	}

	/**
	 * Recupera a lista do mapa, cuja lista corresponde ao value no mapa do key especificado. Se a lista não existir
	 * para o key especificado, cria e a atrela ao key, e então a retorna (vazia).
	 */
	private static <CHAVE, VALOR> List<VALOR> getOrCreateListValue(Map<CHAVE, List<VALOR>> map, CHAVE key) {
		List<VALOR> list = map.get(key);
		if(list == null)
			map.put(key, list = new ArrayList<>());
		return list;
	}

	public static <K> Map<K,MutableInt> countDistinct(Collection<K> els) {
	    Map<K,MutableInt> countDistinct = new HashMap<>();
	    for(K el : els)
            DataStructureUtils.incrementMapValue(countDistinct, el);
	    return countDistinct;
	}
	public static <K> Map<K,MutableInt> countDistinct(Stream<K> els){
		Map<K,MutableInt> countDistinct = new HashMap<>();
	    els.forEach(el ->
	    	DataStructureUtils.incrementMapValue(countDistinct, el));
	    return countDistinct;
	}

	/**
	 * Incrementa o contador para a chave especificada, no mapa.
	 * Se a chave não for encontrada no mapa, criamos uma entrada para ela com contagem 1.
	 */
	public static <T> void incrementMapValue(Map<T, MutableInt> map, T key) {
		MutableInt value = map.get(key);
		if (value == null) {
			value = new MutableInt();
			map.put(key, value);
		}
		value.increment();
	}

	public static <T> void incrementMapValueConc(ConcurrentHashMap<T, AtomicInteger> map, T key) {
		AtomicInteger value = map.get(key);
	    if (value == null) {
	        value = map.putIfAbsent(key, new AtomicInteger(1));
	    }
	    if (value != null) {
	        value.incrementAndGet();
	    }
	}

	/**
	 * Incrementa o contador para a chave especificada, no mapa.
	 * Se a chave não for encontrada no mapa, criamos uma entrada para ela com contagem 1.
	 * @return o valor atual associado ao key especificado.
	 */
	public static <T> void incrementMapValueDouble(Map<T, MutableDouble> map, T key) {
		MutableDouble value = map.get(key);
		if (value == null) {
			value = new MutableDouble();
			map.put(key, value);
		}
		value.increment();
	}

	public static <X, Y extends Comparable<Y>> void putIfHigherValue(Map<X, Y> map, X key, Y value) {
		Y currentMax = map.get(key);
		if(currentMax == null || currentMax.compareTo(value) < 0){
			map.put(key, value);
		}
	}

	/**
	 * Decrementa o contador para a chave especificada, no mapa.
	 * Se a chave não for encontrada, ignora a operação. Se a chave for encontrada com valor 1,
	 * a ação será a de remover o key do mapa.
	 */
	public static <T> void decrementMapValueDouble(Map<T, MutableDouble> map, T key) {
		MutableDouble value = map.get(key);
		if(value != null){
			if(value.doubleValue() == 1.0){
				map.remove(key);
			}else{
				value.decrement();
			}
		}
	}

	public static <T extends Number> double getSumMapValueDouble(Map<?,T> map) {
		double sum = 0;
		for(T entryValue : map.values())
			sum += entryValue.doubleValue();
		return sum;
	}

	public static <T extends Number> double getSumMapValueFloat(Map<?,T> map) {
		float sum = 0;
		for(T entryValue : map.values())
			sum += entryValue.floatValue();
		return sum;
	}

	public static <T extends Number> int getSumMapValueInteger(Map<?,T> map) {
        int sum = 0;
        for(T entryValue : map.values())
            sum += entryValue.intValue();
        return sum;
    }

	public static <K, V extends Comparable<? super V>> Set<K> getMapKeysSortedByValue(Map<K, V> map, final boolean asc) {
	    return getMapKeysSortedByValue(map, asc, -1);
	}

	public static <K, V extends Comparable<? super V>> Set<K> getMapKeysSortedByValue(Map<K, V> map, final boolean asc, int limit) {
	    List<Entry<K, V>> entries = getMapEntriesSortedByValue(map, asc, limit);
        Set<K> keys = new LinkedHashSet<>(entries.size());
        for (Entry<K, V> entry : entries) {
            keys.add(entry.getKey());
        }
        return keys;
    }

	public static <K, V extends Comparable<? super V>> List<K> getMapKeysSortedByValueAsList(Map<K, V> map, final boolean asc, int limit) {
        List<Entry<K, V>> entries = getMapEntriesSortedByValue(map, asc, limit);
        List<K> keys = new ArrayList<>(entries.size());
        for (Entry<K, V> entry : entries) {
            keys.add(entry.getKey());
        }
        return keys;
    }

	public static <K, V extends Comparable<? super V>> Map<K, V> getMapSortedByValue(Map<K, V> map, final boolean asc) {
		List<Entry<K, V>> mapEntries = getMapEntriesSortedByValue(map, asc);

		Map<K, V> mapSortedByValue = new LinkedHashMap<>();
		for (Entry<K, V> entry : mapEntries) {
			mapSortedByValue.put(entry.getKey(), entry.getValue());
		}
		return mapSortedByValue;
	}

	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> getMapEntriesSortedByValue(Map<K, V> map, final boolean asc) {
        return getMapEntriesSortedByValue(map, asc, -1);
    }

	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> getMapEntriesSortedByValue(Map<K, V> map, boolean asc, int limit) {
        return getMapEntriesSortedByValue(new ArrayList<>(map.entrySet()), asc, limit);
    }
	private static <V extends Comparable<? super V>, K> List<Entry<K,V>> getMapEntriesSortedByValue(List<Entry<K,V>> mapEntries, boolean asc, int limit){
		Comparator<Entry<K, V>> valueMapComparator = getEntryMapComparatorByValue(asc);
        Collections.sort(mapEntries, valueMapComparator);
        if(limit > 0 && mapEntries.size() > limit){
            mapEntries = mapEntries.subList(0, limit);
        }
        return mapEntries;
	}

	public static <K, V extends Comparable<? super V>> Comparator<Entry<K, V>> getEntryMapComparatorByValue(boolean asc) {
		if(asc)
			return (o1,o2) -> o1.getValue().compareTo(o2.getValue());
		return (o1,o2) -> o2.getValue().compareTo(o1.getValue());
	}

	public static <K,V extends Comparable<? super V>> Entry<K,V> getEntryHighestValue(Map<K,V> map){
		if(isEmpty(map))
			return null;
		if(map.size() == 1)
			return map.entrySet().iterator().next();
		return MathUtils.max(map.entrySet(), getEntryMapComparatorByValue(true));
	}

	public static <K,V extends Comparable<? super V>> Entry<K,V> getEntryLowestValue(Map<K,V> map){
		if(isEmpty(map))
			return null;
		if(map.size() == 1)
			return map.entrySet().iterator().next();
		return MathUtils.min(map.entrySet(), getEntryMapComparatorByValue(true));
	}

	public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
		Set<T> cloneSet = new LinkedHashSet<>(set1);
		cloneSet.retainAll(set2);
		return cloneSet;
	}

	public static <T> Set<T> intersection(Set<T>... sets){
		Set<T> cloneSet = new LinkedHashSet<>(sets[0]);
		for(int i = 1; i < sets.length; i++){
			cloneSet.retainAll(sets[i]);
		}
		return cloneSet;
	}

	public static <T> int intersectionCount(Set<T> set1, Set<T> set2) {
		int count = 0;
		if (set1.size() > set2.size()) { //swap, so set1 will be the lesser set
        	Set<T> temp = set1;
        	set1 = set2;
        	set2 = temp;
        }
		for (T e : set1) {
			if (set2.contains(e)) {
				count++;
			}
		}
		return count;
	}

	public static <T> int unionCount(Set<T> set1, Set<T> set2) {
	    int count = set1.size() + set2.size();
	    if (set1.size() > set2.size()) { //swap, so set1 will be the lesser set
        	Set<T> temp = set1;
        	set1 = set2;
        	set2 = temp;
        }
	    for (T e : set1) {
	    	if (set2.contains(e)) {
	    		count--;
	    	}
	    }
	    return count;
    }

	public static <T> int disjointCount(Set<T> set1, Set<T> set2) {
	    return Math.abs(set1.size() + set2.size() - 2*unionCount(set1, set2));
	}

	/**
	 * Jaccard index.
	 * @return o quociente entre a intersecao pela uniao, dos dois conjuntos. Se ambos vazios, retorna 1.
	 */
	public static <T> float intersectionUnionRatio(Set<T> set1, Set<T> set2) {
		int union = set1.size() + set2.size();
		if(union == 0)
			return 1;
		int intersection = 0;
	    if (set1.size() > set2.size()) { //swap, so set1 will be the lesser set
        	Set<T> temp = set1;
        	set1 = set2;
        	set2 = temp;
        }
	    for (T e : set1) {
	    	if (set2.contains(e)) {
	    		intersection++;
	    		union--;
	    	}
	    }
        return (float)intersection / (float)union;
	}

	/**
	 * @return o quociente entre a intersecao pelo maior tamanho, dos dois conjuntos. Se ambos vazios, retorna 1.
	 */
	public static <T> float intersectionMaxRatio(Set<T> set1, Set<T> set2) {
		int max = Math.max(set1.size(), set2.size());
		if(max == 0)
			return 1;
		int intersection = 0;
	    if (set1.size() > set2.size()) { //swap, so set1 will be the lesser set
        	Set<T> temp = set1;
        	set1 = set2;
        	set2 = temp;
        }
	    for (T e : set1) {
	    	if (set2.contains(e)) {
	    		intersection++;
	    	}
	    }
        return (float)intersection / (float)max;
	}

	/** Returns all elements within first collection but not in the second collection */
	public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
		Set<T> cloneSet = new LinkedHashSet<>(set1);
		cloneSet.removeAll(set2);
		return cloneSet;
	}

	public static <T> int differenceCount(Set<T> set1, Set<T> set2) {
        return difference(set1, set2).size();
    }

	/**
	 * Remove todas as entradas do mapa cujo value seja diferente do parametro especificado.
	 */
	public static <X, Y> void retainEntryMapsByValue(Map<X, Y> map, Y valueToRetain) {
		Iterator<Entry<X, Y>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<X, Y> entry = it.next();
			if (!valueToRetain.equals(entry.getValue())) {
				it.remove();
			}
		}
	}

	public static void setValue(boolean[] array, List<Integer> idxs, boolean value) {
		for (int idx : idxs) {
			array[idx] = value;
		}
	}

	public static List<Integer> collect(List<Integer> array, boolean[] filter) {
		List<Integer> filtered = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			if (filter[i]) {
				filtered.add(array.get(i));
			}
		}
		return filtered;
	}

	public static boolean[] newArrayTrue(int length) {
		boolean[] array = new boolean[length];
		Arrays.fill(array, true);
		return array;
	}

	/** Returns the kth lowest value from collection, where k starts in 1 */
	public static <T extends Comparable<? super T>> T kthLowest(Iterable<T> collection, int k){
		return Ordering.natural().leastOf(collection, k).get(k - 1);
	}

	/**
	 * Removes elements from collection until size <= limit.
	 * The elements to be removed are randomly chosen from collection.
	 */
	public static <T> void reduceRandomly(List<T> collection, int limit, Random r) {
		if(limit < 0)
			throw new IllegalArgumentException();
		while(collection.size() > limit)
			collection.remove( r.nextInt(collection.size()) );
	}

	/**
	 * Transfers the element from 'list' that exceeds 'limit' size, to a container.
	 * The order from exceeding elements will be maintained in container.
	 */
    public static <T> void transferExceedingElements(List<T> list, List<T> container, int limit) {
        while (list.size() > limit) {
            T element = list.remove(limit);
            container.add(element);
        }
    }

    public static <T> List<T> subListStart(List<T> list, int numElements) {
    	if(list.size() > numElements) {
    		return list.subList(0, list.size() - numElements);
    	}
    	return list;
    }

    public static <T> List<T> subListEnd(List<T> list, int numElements) {
    	if(list.size() > numElements) {
    		return list.subList(list.size() - numElements, list.size());
    	}
    	return list;
    }

    public static <T> void removeAtEnd(List<T> list, int numElementsToRemove) {
    	int lastIdx = list.size() - 1;
		while (numElementsToRemove > 0) {
			list.remove(lastIdx--);
			numElementsToRemove--;
		}
    }

    public static <T> List<T> retainAtStart(List<T> list, int maxToRetain){
    	int numElementsToRemove = list.size() - maxToRetain;
    	int lastIdx = list.size() - 1;
    	while (numElementsToRemove > 0) {
    		list.remove(lastIdx--);
    		numElementsToRemove--;
    	}
    	return list;
	}

    public static String[] subArrayStartingAt(String[] array, int startIndex){
		Preconditions.checkArgument(startIndex > 0);
		return Arrays.copyOfRange(array, startIndex, array.length);
	}

	/**
	 * Returns a copy from the list, from init to end indices, both inclusive.
	 * This functions differs from {@link List}{@link #subListEnd(List, int)} because changes in this one
	 * reflects into the original structure.
	 */
    public static <T> List<T> copyRange(List<T> list, int init, int end) {
        Preconditions.checkState(end >= init, "init must be lesser or equal to the end index");

        ArrayList<T> copy = new ArrayList<>(end - init + 1);
        while(init <= end){
            copy.add(list.get(init));
            init++;
        }
        return copy;
    }

    public static <T> int indexOf(Iterable<T> set, T obj) {
        return indexOf(set, obj, -1);
    }
	public static <T> int indexOf(Iterable<T> set, T obj, int notFoundValue) {
		int idx = 0;
		for (T element : set) {
			if(Objects.equals(element, obj))
				return idx;
			idx++;
		}
		return notFoundValue;
	}
	public static <A,B> int indexOf(Iterable<A> collection, B obj, int notFoundValue, Function<A,B> transformer) {
		int idx = 0;
		for (A element : collection) {
			B b = transformer.apply(element);
			if(Objects.equals(b, obj))
				return idx;
			idx++;
		}
		return notFoundValue;
	}

    public static <A,B> List<B> consumeQueueToList(Queue<A> priorityQueue, Function<A,B> decorator) {
        ListCollector<B> finalRank = new ListCollector<>(priorityQueue.size());
        consumeQueue(priorityQueue, decorator, finalRank);
        return finalRank.getElements();
    }
    public static <A,B> void consumeQueue(Queue<A> priorityQueue, Function<A,B> decorator, Consumer<B> consumer) {
        int elementsLeft = priorityQueue.size();
        if(decorator != null){
            while(elementsLeft-- > 0)
                consumer.accept(decorator.apply(priorityQueue.poll()));
        }else {
            while(elementsLeft-- > 0)
                consumer.accept((B)priorityQueue.poll());
        }
    }

    public static int getLast(int[] els) {
        return els[els.length - 1];
    }
    public static <T> T getLast(List<T> els) {
        return els.get(els.size() - 1);
    }

    public static <T> void retainAll(Collection<T> els, Predicate<T> conditionToRetain) {
        for (Iterator<T> it = els.iterator(); it.hasNext();) {
            T t = it.next();
            if(!conditionToRetain.test(t))
                it.remove();
        }
    }

    public static <T> void removeAll(Collection<T> els, Predicate<T> conditionToRemove) {
    	removeAll(els, conditionToRemove, null);
    }
    public static <T> void removeAll(Collection<T> els, Predicate<T> conditionToRemove, Consumer<T> consumer) {
        for (Iterator<T> it = els.iterator(); it.hasNext();) {
            T t = it.next();
            if(conditionToRemove.test(t)){
                it.remove();
                if(consumer != null){
                	consumer.accept(t);
                }
            }
        }
    }
    public static <T> List<T> removeAllAndReturn(Collection<T> els, Predicate<T> conditionToRemove) {
    	ListCollector<T> removed = new ListCollector<>();
		removeAll(els, conditionToRemove, removed);
		return removed.getElements();
    }

    public static <A> A[] toArray(Collection<A> collection, Class<A> type){
    	A[] array = (A[])Array.newInstance(type, collection.size());
    	return collection.toArray(array);
	}
	public static <A,B> B[] toArray(Collection<A> collection, Class<B> type, Function<A,B> transformer){
		int len = collection.size();
    	B[] array = (B[])Array.newInstance(type, len);
    	int i = 0;
    	for(A a : collection){
			array[i++] = transformer.apply(a);
		}
    	return array;
	}

	public static <T> List<T> stableSort(List<T> list, Comparator<? super T> c) {
		Collections.sort(list, c);
		return list;
	}
	public static <T> List<T> sort(List<T> list, Comparator<? super T> c) {
		return stableSort(list, c);
	}

	public static <T> T[] sort(T[] collection){
		Arrays.sort(collection);
		return collection;
	}

	public static <T> List<T> sorted(Collection<T> collection, Comparator<? super T> comparator){
		List<T> list;
		if(collection instanceof List)
			list = (List<T>)collection;
		else
			list = new ArrayList<>(collection);
		Collections.sort(list, comparator);
		return list;
	}

	public static <X, Y> List<Pair<X,Y>> zip(X[] as, Y[] bs){
	    return IntStream.range(0, Math.min(as.length, bs.length)).mapToObj(i -> new Pair<>(as[i], bs[i])).collect(Collectors.toList());
	}
	public static <X, Y> List<Pair<X,Y>> zip(List<X> as, List<Y> bs){
	    return IntStream.range(0, Math.min(as.size(), bs.size())).mapToObj(i -> new Pair<>(as.get(i), bs.get(i))).collect(Collectors.toList());
	}

	public static <T> SortedMap<Integer,T> enumerate(Iterable<T> els){
		return enumerate(els, new TreeMap<>());
	}
	public static <T> SortedMap<Integer,T> enumerate(Stream<T> els){
		return enumerate(els, new TreeMap<>());
	}
	public static <C extends Map<Integer,T>, T> C enumerate(Iterable<T> els, C map){
		els.forEach(label -> map.put(Integer.valueOf(map.size()), label));
		return map;
	}
	public static <C extends Map<Integer,T>, T> C enumerate(Stream<T> els, C map){
		els.forEach(label -> map.put(Integer.valueOf(map.size()), label));
		return map;
	}
	public static <T> SortedMap<Long,T> enumerateLong(Stream<T> els){
		SortedMap<Long,T> map = new TreeMap();
		els.forEach(label -> map.put(Long.valueOf(map.size()), label));
		return map;
	}

	public static <T> BiMap<T,Integer> enumerateBiDirectional(Collection<T> els){
		HashBiMap<T,Integer> enumerated = HashBiMap.create(els.size());
		for(T el : els)
			enumerated.put(el, enumerated.size());
		return enumerated;
	}

	public static <T> Map<T,Integer> index(Set<T> els){
		HashMap<T,Integer> indexed = new HashMap<>(els.size());
		for(T el : els)
			indexed.put(el, indexed.size());
		return indexed;
	}

	public static <T> Stream<T> flattenCollectionOfLists(Collection<List<T>> list){
		 return list.stream().flatMap(List::stream);
	}

	public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate){
		if(collection != null) {
			for(T el : collection){
				if(predicate.test(el))
					return el;
			}
		}
		return null;
	}

	public static <T> List<T> findAll(T[] collection, Predicate<T> predicate){
		ArrayList<T> filtered = new ArrayList<>();
		if(collection != null){
			for(T el : collection){
				if(predicate.test(el))
					filtered.add(el);
			}
		}
		return filtered;
	}
	public static <T> List<T> findAll(Iterable<T> collection, Predicate<T> predicate){
		ArrayList<T> filtered = new ArrayList<>();
		if(collection != null){
			for(T el : collection){
				if(predicate.test(el))
					filtered.add(el);
			}
		}
		return filtered;
	}
	public static <A, B> Map<A,B> findAll(Map<A,B> collection, Predicate<Entry<A,B>> predicate){
		LinkedHashMap<A,B> filtered = new LinkedHashMap<>();
		if(collection != null){
			for(Entry<A,B> entry : collection.entrySet()) {
				if(predicate.test(entry)) {
					filtered.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return filtered;
	}

	public static boolean hasMatches(int[] els, IntPredicate predicate){
		if(els != null){
			for(int el : els){
				if(predicate.test(el))
					return true;
			}
		}
		return false;
	}

	public static <T> T getFirst(Collection<T> c){
		if(isEmpty(c))
			return null;
		if(c instanceof List)
			return ((List<T>)c).get(0);
		return c.iterator().next();
	}

	public static <A,B,V> void putOnMultiMap(Map<A,Map<B,V>> map, A a, B b, V v){
		Map<B,V> subMap = map.get(a);
		if(subMap == null)
			map.put(a, subMap = new LinkedHashMap<>());
		subMap.put(b, v);
	}
	public static <A,B,C,V> void putOnMultiMap(Map<A,Map<B,Map<C,V>>> map, A a, B b, C c, V v){
		Map<B,Map<C,V>> subMap = map.get(a);
		if(subMap == null)
			map.put(a, subMap = new LinkedHashMap<>());
		Map<C,V> subSubMap = subMap.get(b);
		if(subSubMap == null)
			subMap.put(b, subSubMap = new LinkedHashMap<>());
		subSubMap.put(c, v);
	}

	public static <A,B,V> V getOnMultiMap(Map<A,Map<B,V>> map, A a, B b){
		Map<B,V> subMap = map.get(a);
		if(subMap == null)
			return null;
		return subMap.get(b);
	}
	public static <A,B,C,V> V getOnMultiMap(Map<A,Map<B,Map<C,V>>> map, A a, B b, C c){
		Map<B,Map<C,V>> subMap = map.get(a);
		if(subMap == null)
			return null;
		Map<C,V> subSubMap = subMap.get(b);
		if(subSubMap == null)
			return null;
		return subSubMap.get(c);
	}

	public static <A> A[] replaceAll(A[] c, Function<A,A> f){
		for(int i = 0; i < c.length; i++){
			c[i] = f.apply(c[i]);
		}
		return c;
	}
}
