package dataMining;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import util.Filter;

public abstract class SampleFilter<T extends Sample> implements Filter<T> {

    private boolean skipUnlabeledSamples;
    private boolean skipMultiLabeledSamples;

    public SampleFilter(boolean skipUnlabeledSamples, boolean skipMultiLabeledSamples) {
        this.skipUnlabeledSamples = skipUnlabeledSamples;
        this.skipMultiLabeledSamples = skipMultiLabeledSamples;
    }

    @Override
    public boolean isAccepted(T sample) {
        if (skipUnlabeledSamples && sample.isUnlabeled()) {
            return false;
        }
        if(skipMultiLabeledSamples && sample.getNumberLabels() != 1){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
