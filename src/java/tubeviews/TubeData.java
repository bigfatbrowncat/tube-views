package tubeviews;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * YouTube statistics data item
 */
public class TubeData {
	public static class Video {
		public final String name;
		public final URL preview;
		public final long viewsOnPage;
		public final long viewsLast48Hours;
		public final long viewsLastHour;
		public final long viewsLastMinute;

		public Video(String name, URL preview, long viewsOnPage, long viewsLast48Hours, long viewsLastHour, long viewsLastMinute) {
			this.name = name;
			this.preview = preview;
			this.viewsOnPage = viewsOnPage;
			this.viewsLast48Hours = viewsLast48Hours;
			this.viewsLastHour = viewsLastHour;
			this.viewsLastMinute = viewsLastMinute;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Video) {
				Video v = (Video)obj;
				return v.name.equals(name) &&
				       v.preview.equals(preview) &&
				       v.viewsLast48Hours == viewsLast48Hours &&
				       v.viewsLastHour == viewsLastHour &&
				       v.viewsLastMinute == viewsLastMinute;
			}
			return false;
		}
	}
	
	public final Map<String, Video> videos;

	public TubeData(Map<String, Video> videos) {
		super();
		this.videos = /*Collections.unmodifiableMap(*/new HashMap<>(videos)/*)*/;
	}
	
}
