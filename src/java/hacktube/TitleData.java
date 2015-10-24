package hacktube;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Date;

public class TitleData {
		public final String id;
		public final String title;
		public final URL previewImage;
		public final URL videoPage;
		public final Date dateCreated;
		public final Date datePublished;
		public final long countedViews;
		
		TitleData(String id, String title, URL previewImage, URL videoPage, Date dateCreated, Date datePublished, long countedViews) {
			this.id = id;
			this.title = title;
			this.previewImage = previewImage;
			this.videoPage = videoPage;
			this.dateCreated = dateCreated;
			this.countedViews = countedViews;
			this.datePublished = datePublished;
		}
	
		@Override
		public String toString() {
			try {
				Field[] fields = getClass().getFields();
				StringBuilder sb = new StringBuilder("{ ");
				int c = 0;
				for (; c < fields.length - 1; c++) {
					sb.append(fields[c].getName());
					sb.append(": ");
					sb.append(fields[c].get(this));
					sb.append(", ");
				}
				sb.append(fields[c].getName());
				sb.append(": ");
				sb.append(fields[c].get(this));
				sb.append(" }");
				return sb.toString();
			} catch (IllegalAccessException e) {
				return e.getMessage();
			}
			
		}
	}
	