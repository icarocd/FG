package util;

public interface Filter<T> {

    boolean isAccepted(T element);

}
