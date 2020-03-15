package dataMining.retrieval;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.MinMaxPriorityQueue.Builder;
import util.DataStructureUtils;
import util.FileUtils;
import util.Logs;
import util.MathUtils;
import util.Pair;
import util.StringUtils;
import util.TriConsumer;

public class RankedList implements Iterable<Pair<Long,Float>> {
	private MinMaxPriorityQueue<Pair<Long,Float>> priorityQueue; //queue of pairs of <id,weight> elements, sorted. The sorting order depends on the criteria used: similarity are decreasing, distance are increasing.
	private List<Pair<Long,Float>> finalRank;

	public RankedList(int size, boolean biggerValuesAsBetter) {
		Builder<Pair<Long,Float>> builder = MinMaxPriorityQueue.orderedBy(biggerValuesAsBetter ? Pair.<Long,Float>createComparatorByBReversed() : Pair.<Long,Float>createComparatorByB());
		if(size >= 0)
			builder = builder.expectedSize(size).maximumSize(size);
		priorityQueue = builder.create();
	}
	public RankedList(List<Pair<Long,Float>> finalRank) {
	    this.finalRank = finalRank;
	}

	public synchronized void add(long id, float weight) {
		priorityQueue.offer(new Pair<>(id, weight));
	}

	public synchronized List<Pair<Long, Float>> getRank() {
		if(finalRank == null){
		    finalRank = DataStructureUtils.consumeQueueToList(priorityQueue, null);
			priorityQueue = null; //as we changed the queue, here we guarantee the rankedList would not be wrongly used afterwards
		}
		return finalRank;
	}

	public int size(){
		return getRank().size();
	}

	public Long getID(int i){
		return getRank().get(i).getA();
	}

	public void forEach(BiConsumer<Long,Float> consumer){
		for(Pair<Long,Float> pair : getRank())
			consumer.accept(pair.getA(), pair.getB());
	}

	public void forEach(TriConsumer<Integer,Long,Float> consumer){
		List<Pair<Long,Float>> r = getRank();
		for(int i = 0; i < r.size(); i++){
			Pair<Long,Float> pair = r.get(i);
			consumer.accept(i, pair.getA(), pair.getB());
		}
	}

	public void forEachID(Consumer<Long> consumer){
		for(Pair<Long,Float> pair : getRank())
			consumer.accept(pair.getA());
	}

    public Iterator<Pair<Long, Float>> iterator() {
        return getRank().iterator();
    }

	public List<Long> getIDs() {
		return DataStructureUtils.collectAsList(getRank(), pair -> pair.getA());
	}

	public LinkedHashSet<Long> getIDsAsSet() {
		return DataStructureUtils.collectAsSet(getRank(), pair -> pair.getA());
	}

	public static Set<Long> getIDs(Collection<RankedList> ranks){
		HashSet<Long> ids = new HashSet<>();
		ranks.forEach(rank -> rank.forEachID(id -> ids.add(id)));
		return ids;
	}

	/** Returns <pos,weight> for the response item, if it belongs to the rank, otherwise returns null. 'Pos' starts by 0. */
	public Pair<Integer,Float> getPositionAndWeight(Long responseItem){
		List<Pair<Long,Float>> rank = getRank();
		for(int pos = 0; pos < rank.size(); pos++){
			Pair<Long,Float> rankElement = rank.get(pos);
			if(rankElement.getA().equals(responseItem))
				return Pair.get(pos, rankElement.getB());
		}
		return null;
	}

	/** Returns weight for the response item, if it belongs to the rank, otherwise returns defaultValue */
	public Float getWeight(Long responseItem, Float defaultValue){
		for(Pair<Long,Float> rankElement : getRank()){
			if(rankElement.getA().equals(responseItem))
				return rankElement.getB();
		}
		return defaultValue;
	}

	public void normalize() {
		normalize(0, 1);
	}
	public void normalize(float min_new, float max_new) {
		MathUtils.normalizeFloat(getRank(), Pair::getB, Pair::setB, min_new, max_new);
    }

	/**
	 * normalizes to interval [min, max], but first making sure the scores decreases from first to last element.
	 */
	public RankedList normalizeDecreasing(float min, float max) {
		boolean decreasing = isDecreasing();
	    if(!decreasing)
	    	invertValues();
    	normalize(min, max);
	    return this;
    }

	/**
	 * Changes the weights of the rank uniformifly starting by 'first' and ending by 'last', with uniform steps.
	 */
	public void changeWeightsToInterval(float first, float last) {
	    List<Pair<Long,Float>> elements = getRank();
	    int n = elements.size();
	    float step = (last - first)/(n-1);
	    for (int i = 0; i < n; i++) {
	        elements.get(i).setB(first + i*step);
        }
	}

	private boolean isDecreasing(){
	    List<Pair<Long,Float>> elements = getRank();
	    for (int i = 1; i < elements.size(); i++)
			if( elements.get(i).getB() > elements.get(i-1).getB() )
				return false;
		return true;
	}

	private void invertValues() {
	    List<Pair<Long,Float>> elements = getRank();
	    float max = MathUtils.maxFloat(elements, e -> e.getB());
	    for (Pair<Long,Float> pair : elements) {
	        pair.setB(max - pair.getB());
	    }
    }

    public void saveToFolder(long id, File outputFolder) {
        save(getRankedListFile(id, outputFolder));
    }
	private static File getRankedListFile(long id, File outputFolder) {
        return new File(outputFolder, String.valueOf(id));
    }
    public void save(File destineFile) {
    	save(destineFile, getRank());
	}
	public void save(PrintStream out) {
		save(out, getRank());
	}
	public static <T> void save(File destineFile, List<Pair<T,Float>> rank) {
		try (PrintStream out = FileUtils.createPrintStreamToFile(destineFile, true)) {
			save(out, rank);
		}
	}
	public static <T> void save(PrintStream out, List<Pair<T,Float>> rank) {
		if(rank.get(0).getB() == null){ //unweighted rank
			for(Pair<?, Float> elementAndWeight : rank)
				out.append(elementAndWeight.getA().toString()).append('\n');
		}else{
			DecimalFormat decimalFormat = StringUtils.getDecimalFormatter();
			for(Pair<?, Float> elementAndWeight : rank)
				out.append(decimalFormat.format(elementAndWeight.getB())).append('\t').append(elementAndWeight.getA().toString()).append('\n');
		}
	}

	public static RankedList loadFromFolder(long sampleId, File folder, int limit) {
        return load(getRankedListFile(sampleId, folder), limit);
    }
	public static RankedList load(File file, int limit) {
	    try(Scanner reader = FileUtils.createScannerFromFile(file)){
	        return load(reader, limit);
        }catch (RuntimeException e) {
        	Logs.severe("error loading rank "+file);
        	throw e;
		}
    }
	static RankedList load(Scanner reader, int limit) {
		List<Pair<Long,Float>> list = limit > 0 ? new ArrayList<>(limit) : new ArrayList<>();
		load(reader, limit, (index,id,weight) -> list.add(new Pair<>(id, weight)));
		return new RankedList(list);
	}
	public static LinkedHashSet<Long> loadEntries(File file, int limit){
		return loadEntries(file, limit, new LinkedHashSet<>());
	}
	public static ArrayList<Long> loadEntriesAsList(File file, int limit){
		return loadEntries(file, limit, new ArrayList<>());
	}
	public static <T extends Collection<Long>> T loadEntries(File file, int limit, T container){
		load(file, limit, (index,id,weight) -> container.add(id));
		return container;
	}
	static void load(File file, int limit, TriConsumer<Integer,Long,Float> consumer) {
		try(Scanner reader = FileUtils.createScannerFromFile(file)){
	        load(reader, limit, consumer);
        }
	}
    static void load(Scanner reader, int limit, TriConsumer<Integer,Long,Float> consumer) {
    	if(!reader.hasNextLine() || limit == 0)
    		return;

    	String line = reader.nextLine();
		String[] pieces = line.split("\\s+");
		boolean pair = pieces.length > 1;
		int idxScore = 0, idxId = 1;
		if(pair && pieces[1].contains(".")){
			idxId = 0;
			idxScore = 1;
		}

		int consumed = 0;
		do{
			Long id;
			Float v = null;
			if(pair){
				v = Float.valueOf(pieces[idxScore]);
				id = Long.valueOf(pieces[idxId]);
			}else{
				id = Long.valueOf(line);
			}
			consumer.accept(consumed, id, v);
			consumed++;
			if(!reader.hasNextLine())
				break;
			if(limit > 0 && consumed >= limit)
				break;
			line = reader.nextLine();
			pieces = line.split("\\s+");
		}while(true);
    }

	public static void copyRankFileDecorated(File rankFile, File destineRankfile, int loadSizeLimit, Function<Long,String> idDecorator){
		List<Pair<String,Float>> elsNames = new ArrayList<>();
		load(rankFile, loadSizeLimit, (index, id, weight) -> elsNames.add(Pair.get(idDecorator.apply(id), weight)));
		save(destineRankfile, elsNames);
	}
}