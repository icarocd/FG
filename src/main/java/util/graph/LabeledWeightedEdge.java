package util.graph;

import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jgrapht.graph.DefaultWeightedEdge;
import util.MathUtils;
import util.ObjectUtils;
import util.Pair;

public class LabeledWeightedEdge extends DefaultWeightedEdge {

    private static final long serialVersionUID = -4046507748499162165L;

    private String label = "";

    public LabeledWeightedEdge() {
    }

    public LabeledWeightedEdge(Object source, Object target, String label, double weight) {
        ObjectUtils.writeField(this, "source", source);
        ObjectUtils.writeField(this, "target", target);
        setWeight(weight);
        this.label = label;
    }

    public Object getSource() {
        return super.getSource();
    }

    public Object getTarget() {
        return super.getTarget();
    }

    public double getWeight() {
        return super.getWeight();
    }
    public void setWeight(double weight) {
        ObjectUtils.writeField(this, "weight", weight);
    }
    public void addWeight(double weight) {
        setWeight(getWeight() + weight);
    }

    protected double getWeightRounded() {
        return MathUtils.round(getWeight(), 3, RoundingMode.HALF_UP);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static Set<LabeledWeightedEdge> cloneEdgesWithoutWeights(Set<LabeledWeightedEdge> set) {
        Set<LabeledWeightedEdge> copySet = new LinkedHashSet<>(set.size());
        for (LabeledWeightedEdge edge : set) {
            LabeledWeightedEdge cloneEdge = new LabeledWeightedEdge(edge.getSource(), edge.getTarget(), edge.getLabel(), 1);
            copySet.add(cloneEdge);
        }
        return copySet;
    }

	public Pair<String, String> getSourceTarget() {
		return new Pair<>((String)getSource(),(String)getTarget());
	}

	public String toString() {
        double w = getWeight();
        if(w != 1.0){
            return label + " [#" + w + "]";
        }
        return label;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
        result = prime * result + ((getTarget() == null) ? 0 : getTarget().hashCode());
//        long temp;
//        temp = Double.doubleToLongBits(getWeightRounded());
//        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LabeledWeightedEdge)) {
            return false;
        }
        LabeledWeightedEdge other = (LabeledWeightedEdge) obj;
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (getSource() == null) {
            if (other.getSource() != null) {
                return false;
            }
        } else if (!getSource().equals(other.getSource())) {
            return false;
        }
        if (getTarget() == null) {
            if (other.getTarget() != null) {
                return false;
            }
        } else if (!getTarget().equals(other.getTarget())) {
            return false;
        }
//        if (Double.doubleToLongBits(getWeightRounded()) != Double.doubleToLongBits(other.getWeightRounded())) {
//            return false;
//        }
        return true;
    }
}
