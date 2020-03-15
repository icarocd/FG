package util;

import java.util.function.Supplier;

public abstract class LazySupplier<T> implements Supplier<T> {

	private T el;

    public final synchronized T get(){
    	if(el == null)
    		el = create();
		return el;
    }

	protected abstract T create();

	public static <T> LazySupplier<T> create(Supplier<T> factory){
		return new LazySupplier<>() {
			@Override protected T create(){
				return factory.get();
			}
		};
	}
	public static <T> LazySupplier<T> createPreloaded(T el){
		return new LazySupplier<>() {
			@Override protected T create(){
				return el;
			}
		};
	}
}
