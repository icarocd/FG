package fusionGraph.dataset;

import java.io.File;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableInt;
import fusionGraph.Configs;
import util.DataStructureUtils;
import util.FileUtils;
import util.Pair;
import util.StringUtils;

public class DistanceMatricesLabeledDataset extends DistanceMatricesDataset {

	public DistanceMatricesLabeledDataset(String name) {
		super(name);
	}

	public Pair<Map<String,MutableInt>,SortedMap<Long,String>> loadInfo(){
		SortedMap<Long,String> classById = loadIdsClasses();
		SortedMap<String, MutableInt> numSamplesByClass = new TreeMap();
		classById.forEach((id,label) -> DataStructureUtils.incrementMapValue(numSamplesByClass, label));
		return new Pair<>(numSamplesByClass, classById);
	}

	private SortedMap<Long,String> loadIdsClasses(){
		try( Stream<String> names = loadNames(null) ){
			return DataStructureUtils.enumerateLong(names.map(line -> StringUtils.substringBefore(line, "-")));
		}
	}

	public SortedMap<Long,String> loadIdsNames(Function<String,String> nameChanger){
		try( Stream<String> names = loadNames(nameChanger) ){
			return DataStructureUtils.enumerateLong(names);
		}
	}

	/** IMPORTANT: you need to close the returned Stream, either manually or using a try-with-resources block, otherwise the resource is left open! */
	private Stream<String> loadNames(Function<String,String> nameChanger){
		Stream<String> names = FileUtils.lines(new File(getDatasetFolder(), "ids"));
		if(nameChanger != null)
			names = names.map(nameChanger);
		return names;
	}

	public String getLabel(String sampleName){
		return sampleName.split("-")[0];
	}

	public File getSamplesFolder(){
		return FileUtils.get(getDatasetFolder(), "samples");
	}

	public Function<String,File> getterSampleFile(){
		File imagesFolder = getSamplesFolder();
		return sampleName -> {
			String[] label_and_subName = sampleName.split("-");
			return FileUtils.get(imagesFolder, label_and_subName[0], label_and_subName[1]);
		};
	}

	public Function<String,File> getterFeaturesFile(){
		return descriptor -> Configs.getFeaturesFile(name, descriptor);
	}
}
