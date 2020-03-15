package dataMining;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import util.DataStructureUtils;
import util.StringUtils;

public class Sample implements Serializable {

    private static final long serialVersionUID = 1;

    public static final Function<Sample, String> TRANSFORMER_FIRST_LABEL = sample -> sample.getFirstLabel();

    public static final Comparator<Sample> COMPARATOR_BY_ID = (o1, o2) -> Long.compare(o1.getId(), o2.getId());

    private long id;
    private Set<String> labels;

    public Sample(long id, Set<String> labels) {
        this.id = id;
        this.labels = labels;
    }

    public Sample(long id, String label) {
        this(id, DataStructureUtils.asSetUnit(label));
    }

    public long getId() {
        return id;
    }

    public Set<String> getLabels() {
        return labels;
    }
    public void setLabels(Set<String> labels){
		this.labels = labels;
	}

    public boolean isUnlabeled() {
        return getNumberLabels() == 0;
    }

    public int getNumberLabels() {
        return labels != null ? labels.size() : 0;
    }

    public String getLabel() {
        return getFirstLabel();
    }

    public String getFirstLabel() {
        if (getNumberLabels() == 0) {
            return null;
        }
        return labels.iterator().next();
    }

    public void addLabel(String label) {
        if (labels == null) {
            labels = new TreeSet<>();
        }
        labels.add(label);
    }

	public int hashCode() {
		return Long.hashCode(id);
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Sample)) {
			return false;
		}
		Sample other = (Sample) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

    public String toString() {
        return "Sample [id=" + id + ", labels=" + labels + "]";
    }

    public static String getIdFromFile(File sampleFile) {
        return StringUtils.retainDigits(sampleFile.getName());
    }
    public static long getIdFromFile_(File sampleFile) {
        return Long.parseLong(getIdFromFile(sampleFile));
    }
}
