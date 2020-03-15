package util;

public class Triple<A,B,C> {

    private A a;
    private B b;
    private C c;

    public Triple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
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

    public C getC(){
		return c;
	}
    public void setC(C c){
		this.c = c;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        result = prime * result + ((c == null) ? 0 : c.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Triple)) {
            return false;
        }
        Triple other = (Triple) obj;
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
        if (c == null) {
            if (other.c != null) {
                return false;
            }
        } else if (!c.equals(other.c)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "[" + a + ", " + b + ", " + c + "]";
    }

    public static <X, Y, Z> Triple<X,Y,Z> get(X x, Y y, Z z) {
        return new Triple<>(x, y, z);
    }
}
