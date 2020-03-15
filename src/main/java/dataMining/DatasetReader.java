package dataMining;

import java.util.function.Consumer;
import util.Filter;

public interface DatasetReader<T extends Sample> {

    void readSamples(Filter<T> sampleFilter, Consumer<T> collector);
}
