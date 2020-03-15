package util;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ListCollector<T> implements Consumer<T> {

	private ArrayList<T> elements;

	public ListCollector() {
	    elements = new ArrayList<>();
    }
	public ListCollector(int initialCapacity) {
	    elements = new ArrayList<>(initialCapacity);
    }

	@Override
	public void accept(T element) {
		elements.add(element);
	}

	public ArrayList<T> getElements() {
		return elements;
	}

	public T getElement(int index) {
        return elements.get(index);
    }
}
