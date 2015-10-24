package hacktube;

import java.util.Collection;

public class Tools {
	public static String join(Collection<?> list, String delim) {

	    StringBuilder sb = new StringBuilder();

	    String loopDelim = "";

	    for (Object s : list) {

	        sb.append(loopDelim);
	        sb.append(s);            

	        loopDelim = delim;
	    }

	    return sb.toString();
	}
}
