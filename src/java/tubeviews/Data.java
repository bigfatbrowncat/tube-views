package tubeviews;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Data {
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

	public Data(Map<String, Video> videos) {
		super();
		this.videos = Collections.unmodifiableMap(new HashMap<>(videos));
	}
}
