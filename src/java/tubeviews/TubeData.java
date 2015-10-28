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
		public final long viewsCount;

		public Video(String name, URL preview, long viewsCount) {
			this.name = name;
			this.preview = preview;
			this.viewsCount = viewsCount;
		}
	}
	
	public final Map<String, Video> videos;

	public TubeData(Map<String, Video> videos) {
		super();
		this.videos = Collections.unmodifiableMap(new HashMap<>(videos));
	}
}
