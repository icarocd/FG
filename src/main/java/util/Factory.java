package util;

public interface Factory<T> {

    T create(Object... params);

}
