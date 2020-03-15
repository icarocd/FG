package util;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import com.google.common.base.Preconditions;

public class Params {
    private final Map<String,String> params;

    public Params(Object... keysAndValues) {
    	params = DataStructureUtils.asMapString(keysAndValues);
    }
    public Params(Map<String, String> params) {
        this.params = params;
    }

    public Map<String,String> getParams(){
		return params;
	}

    @Override
    protected Params clone(){
    	return new Params(new LinkedHashMap<>(params));
    }

	public void addAll(Params params2){
		params.putAll(params2.params);
	}

	public void addSome(Params params2, String... keys){
		for(String key : keys)
			set(key, params2.get(key));
	}

    public void set(String key, Object value) {
        if(value == null)
            params.remove(key);
        else
            params.put(key, value.toString());
    }

    public void setIfAbsent(String key, Object value) {
    	if(value != null && params.get(key) == null)
			params.put(key, value.toString());
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    public boolean contains(String key) {
    	return get(key) != null;
    }

    public String get(String key) {
        return params.get(key);
    }

    public String get(String key, String defaultValue) {
        String v = get(key);
		return v != null ? v : defaultValue;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if(StringUtils.isBlank(value))
            return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    public int getInt(String key) {
    	String value = get(key);
    	Preconditions.checkArgument(value != null, "Parameter not found:" + key);
        return Integer.parseInt(value);
    }
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        return StringUtils.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }
    public Integer getInteger(String key, Integer defaultValue) {
        String value = get(key);
        if(StringUtils.isBlank(value))
            return defaultValue;
        return Integer.parseInt(value);
    }

    public Pair<Integer,Integer> getIntRange(String key, Pair<Integer,Integer> defaultValue) {
    	String value = get(key);
        if(StringUtils.isBlank(value))
            return defaultValue;
        String[] pieces = StringUtils.split(value, "|/");
        return new Pair<>(Integer.valueOf(pieces[0]), Integer.valueOf(pieces[1]));
	}

    public Long getLong(String key, Long defaultValue) {
        String value = get(key);
        if(StringUtils.isBlank(value))
            return defaultValue;
        return Long.parseLong(value);
    }
    public AtomicLong getLongAtomic(String key, AtomicLong defaultValue) {
    	String value = get(key);
        if(StringUtils.isBlank(value))
            return defaultValue;
        return new AtomicLong(Long.parseLong(value));
	}

    public float getFloat(String key, float defaultValue) {
    	String value = get(key);
        return StringUtils.isBlank(value) ? defaultValue : Float.parseFloat(value);
	}

    public double getDouble(String key) {
    	String value = get(key);
        return Double.parseDouble(value);
	}
    public double getDouble(String key, double defaultValue) {
    	String value = get(key);
        return StringUtils.isBlank(value) ? defaultValue : Double.parseDouble(value);
	}

    public <T extends Enum> T getEnum(String key, Class<T> type, T defaultValue){
		String name = get(key);
		if(StringUtils.isEmpty(name))
			return defaultValue;
		return ObjectUtils.getEnumValue(type, name, true);
	}

    public File getFile(File folder, String key) {
    	return getFile(folder, key, null);
    }
    public File getFile(File folder, String key, String defaultName) {
        String filepath = get(key);
        if(filepath == null){
        	if(defaultName == null)
        		return null;
        	filepath = defaultName;
        }
        return new File(folder, filepath);
    }

    public String assertParam(String key, String... acceptedValues) {
        String value = get(key);
        Preconditions.checkArgument(StringUtils.isNotBlank(value), "Parameter required: " + key);

        if (acceptedValues.length > 0) {
            boolean ok = false;
            for (String acceptedValue : acceptedValues) {
                if(value.equals(acceptedValue)){
                    ok = true;
                    break;
                }
            }
            if (!ok)
                throw new IllegalArgumentException("Parameter " + key + " must be one of these values: " + StringUtils.join(acceptedValues, ','));
        }

        return value;
    }

    public String[] assertArray(String key) {
        return StringUtils.split(assertParam(key), "|/");
    }

    public String[] getArray(String key) {
    	return getArray(key, "");
    }
    public String[] getArray(String key, String defaultValue) {
        return StringUtils.split(get(key, defaultValue), "|/");
    }
    public int[] getArrayInt(String key) {
    	return StringUtils.splitInts(get(key), "|/");
    }
    public int[] getArrayInt(String key, String defaultValue) {
        return StringUtils.splitInts(get(key, defaultValue), "|/");
    }
    public double[] getArrayDouble(String key) {
    	return StringUtils.splitDoubles(get(key), "|/");
    }
    public double[] getArrayDouble(String key, String defaultValue) {
        return StringUtils.splitDoubles(get(key, defaultValue), "|/");
    }
    public <T> T[] getArrayObj(String key, String defaultValue, Class<T> type, Function<String,?> transformer) {
        String[] array = getArray(key, defaultValue);
        T[] objs = (T[]) Array.newInstance(type, array.length);
        for (int i = 0; i < array.length; i++) {
            objs[i] = (T)transformer.apply(array[i]);
        }
        return objs;
    }
    public <T extends Enum> T[] getArrayEnum(String key, String defaultValue, Class<T> type){
    	String[] array = getArray(key, defaultValue);
        T[] objs = (T[]) Array.newInstance(type, array.length);
        for (int i = 0; i < array.length; i++) {
            objs[i] = ObjectUtils.getEnumValue(type, array[i], true);
        }
        return objs;
	}
    public <T> List<T> getList(String key, String defaultValue, Function<String,?> transformer) {
        String[] array = getArray(key, defaultValue);
        List<T> objs = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            objs.add((T)transformer.apply(array[i]));
        }
        return objs;
    }
    public Set<String> getSet(String key) {
    	return getSet(key, null);
    }
    public Set<String> getSet(String key, Set<String> defaultValue) {
    	String[] array = getArray(key, "");
    	if(array.length == 0 && defaultValue != null)
    		return defaultValue;
		return DataStructureUtils.asSet(array);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + params.hashCode();
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Params other = (Params) obj;
        if (!params.equals(other.params))
            return false;
        return true;
    }

    public String toString() {
        return params.toString();
    }

    public static Params parse(String... args) {
        Map<String,String> paramEntries = parseParams(args);
        return new Params(paramEntries);
    }
	protected static Map<String,String> parseParams(String... args){
		Map<String,String> paramEntries;
        if (args == null || args.length == 0) {
            paramEntries = new LinkedHashMap<>(0);
        } else {
            paramEntries = new LinkedHashMap<>();
            String params = args[0].trim();
            String[] paramOptions = StringUtils.split(params, ',');
            for (String paramOption : paramOptions) {
            	int idx = paramOption.indexOf("=");
            	if(idx < 1) throw new IllegalArgumentException("Invalid parameter format");
                String param = paramOption.substring(0, idx);
                String value = paramOption.substring(idx+1);
                paramEntries.put(param, value);
            }
        }
		return paramEntries;
	}
}