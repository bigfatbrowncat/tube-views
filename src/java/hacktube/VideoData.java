package hacktube;

import java.util.Date;
import java.util.Map;

public class VideoData {
		public final TitleData titleData;
		public final Map<Date, Long> views48Hours;
		public final Map<Date, Long> views60Minutes;

		VideoData(TitleData title, Map<Date, Long> views48Hours, Map<Date, Long> views60Minutes) {
			this.titleData = title;
			this.views48Hours = views48Hours;
			this.views60Minutes = views60Minutes;
		}
		
		@Override
		public String toString() {
			long views48 = 0;
			for (Long l : views48Hours.values()) {
				views48 += l;
			}
			long views60 = 0;
			for (Long l : views60Minutes.values()) {
				views60 += l;
			}
			return "{ titleData: " + titleData + ", views48 (count): " + views48 + ", views60 (count): " + views60 + " }";
		}
	}
	