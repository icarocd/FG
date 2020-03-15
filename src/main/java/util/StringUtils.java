package util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static String asNotEmpty(String s, String defaultValue) {
        return isNotEmpty(s) ? s : defaultValue;
    }

    public static StringBuilder append(StringBuilder buffer, char[] ch, int start, int length) {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        buffer.append(ch, start, length);
        return buffer;
    }

    public static String trimToNull(CharSequence text) {
        if (text == null) {
            return null;
        }
        String text_ = text.toString().trim();
        return text_.isEmpty() ? null : text_;
    }

    public static int[] splitInts(String s, String separatorChars){
        String[] pieces = split(s, separatorChars);
        int[] ints = new int[pieces.length];
        for (int i = 0; i < pieces.length; i++)
            ints[i] = Integer.parseInt(pieces[i]);
        return ints;
    }

    public static double[] splitDoubles(String s, String separatorChars){
        String[] pieces = split(s, separatorChars);
        double[] ints = new double[pieces.length];
        for (int i = 0; i < pieces.length; i++)
            ints[i] = Double.parseDouble(pieces[i]);
        return ints;
    }

    public static String retainDigits(String str) {
        return str.replaceAll("\\D+", "");
    }

    public static BufferedReader toBufferedReader(String s){
		return new BufferedReader(new StringReader(s));
	}

    public static DecimalFormat getDecimalFormatter() {
		return getDecimalFormatter(null, null);
	}
	public static DecimalFormat getDecimalFormatter(Integer maxDecimalDigits) {
		return getDecimalFormatter(null, maxDecimalDigits);
	}
	public static DecimalFormat getDecimalFormatter(Integer minDecimalDigits, Integer maxDecimalDigits){
		DecimalFormat format = new DecimalFormat();
		format.setRoundingMode(RoundingMode.HALF_UP);
		format.setGroupingUsed(false);
		if(minDecimalDigits != null) //PS: in this case, decimals with always be showed
			format.setMinimumFractionDigits(minDecimalDigits);
		format.setMaximumFractionDigits(maxDecimalDigits != null ? maxDecimalDigits : 6);
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH)); //dot as decimal separator
		return format;
	}
}
