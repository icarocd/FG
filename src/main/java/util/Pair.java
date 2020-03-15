package util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Pair<A, B> {

    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }
    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }
    public void setB(B b) {
        this.b = b;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair other = (Pair) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        if (b == null) {
            if (other.b != null) {
                return false;
            }
        } else if (!b.equals(other.b)) {
            return false;
        }
        return true;
    }

	public void addB(float v){
		Pair<?,Float> p = (Pair<?,Float>)this;
		p.b += v;
	}

	public void multiplyB(float v){
		Pair<?,Float> p = (Pair<?,Float>)this;
		p.b *= v;
	}

	public void divideB(int v){
		Pair<?,Float> p = (Pair<?,Float>)this;
		p.b /= v;
	}

	public void maxB(float v){
		Pair<?,Float> p = (Pair<?,Float>)this;
		p.b = Math.max(p.b, v);
	}

	public void minB(float v){
		Pair<?,Float> p = (Pair<?,Float>)this;
		p.b = Math.min(p.b, v);
	}

    public String toString() {
        return "[" + a + ", " + b + "]";
    }

    public static <X extends Comparable<X>, Y> Comparator<Pair<X, Y>> createComparatorByA() {
        return (p1, p2) -> p1.getA().compareTo(p2.getA());
    };

    public static <X extends Comparable<X>, Y> Comparator<Pair<X, Y>> createComparatorByAReversed() {
        return (p1, p2) -> p2.getA().compareTo(p1.getA());
    };

    public static <X, Y extends Comparable<Y>> Comparator<Pair<X, Y>> createComparatorByB() {
        return (p1, p2) -> p1.getB().compareTo(p2.getB());
    };

	public static <X, Y extends Comparable<Y>> Comparator<Pair<X, Y>> createComparatorByBReversed() {
		return (p1, p2) -> p2.getB().compareTo(p1.getB());
	}

    public static <X extends Comparable<X>, Y extends Comparable<Y>> Comparator<Pair<X, Y>> createComparatorByAB() {
        return (p1, p2) -> {
		    X a_p1 = p1.getA();
		    X a_p2 = p2.getA();
		    int r = a_p1.compareTo(a_p2);
		    if (r != 0) {
		        return r;
		    }
		    Y b_p1 = p1.getB();
		    Y b_p2 = p2.getB();
		    return b_p1.compareTo(b_p2);
		};
    }

    public static <X, Y> Pair<X,Y> get(X x, Y y) {
        return new Pair<>(x, y);
    }

    /** Creates a pair, whose order if <x,y> if x <= y, otherwise the pair <y,x> is created. */
    public static <X extends Comparable<X>> Pair<X,X> getSorted(X x, X y) {
    	if(x.compareTo(y) > 0)
    		return new Pair<>(y, x);
    	return new Pair<>(x, y);
    }

	public static <A,B> A[] getArrayA(Collection<Pair<A,B>> pairs, Class<A> type){
		int len = pairs.size();
		A[] array = (A[])Array.newInstance(type, len);
		int i = 0;
		for(Pair<A,B> pair : pairs){
			array[i++] = pair.getA();
		}
		return array;
	}

	public static <A,B> List<A> getListA(Collection<Pair<A,B>> pairs){
		return DataStructureUtils.collectAsList(pairs, p -> p.getA());
	}
	public static <A,B> List<B> getListB(Collection<Pair<A,B>> pairs){
		return DataStructureUtils.collectAsList(pairs, p -> p.getB());
	}
}
