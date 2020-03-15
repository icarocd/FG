package util;

public class ExceptionUtils {

	public static RuntimeException asRuntimeException(Exception e){
		if(e instanceof RuntimeException)
			return (RuntimeException)e;
		throw new RuntimeException(e);
	}
}
