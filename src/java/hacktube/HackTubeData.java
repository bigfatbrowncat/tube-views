package hacktube;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HackTubeData {
	
	private static Map<Date, Long> decodeTable(JSONArray data) {
		JSONArray respResult237 = data.getJSONArray(0);
		JSONArray respResult307 = data.getJSONArray(1);
		
		JSONArray respResult237_data = respResult237.getJSONArray(4);
		JSONArray respResult307_data = respResult307.getJSONArray(4);
		
		assert (respResult237_data.length() == respResult307_data.length());
		
		HashMap<Date, Long> res = new HashMap<>();
		for (int i = 0; i < respResult237_data.length(); i++) {
			JSONArray item = respResult237_data.getJSONArray(i);		// [null,1,null,"<timeMillis>"]
			JSONArray item2 = respResult307_data.getJSONArray(i);		// [null,1,null,"<views>"]
			long timeMillis = item.getLong(3);
			long views = item2.getLong(3);
			res.put(new Date(timeMillis), views);
		}
		
		return res;
	}

	
	
	private static JSONArray extractLevel3(JSONArray respResult) {
		
		JSONArray level1 = (JSONArray) respResult.get(1);
		JSONArray level2 = (JSONArray) level1.get(0);
		JSONArray level3 = (JSONArray) level2.get(2);
		return level3;
	}
	
	public static List<TitleData> decodeSearchTitleData(JSONArray respResult) {
		
		JSONArray level3 = extractLevel3(respResult);
		List<TitleData> res = new ArrayList<TitleData>();
		
		for (int i = 0; i < level3.length(); i++) {
			try {
				JSONArray titlej = level3.getJSONArray(i);
				String id = titlej.getJSONArray(1).getJSONArray(2).getString(2);
				String title = titlej.getJSONArray(3).getString(1);
				Date dateCreated = new Date(titlej.getJSONArray(3).getLong(2));
				long countedViews = titlej.getJSONArray(3).getLong(5);
				URL videoURL = new URL("http://" + titlej.getJSONArray(3).getString(11));
				URL previewURL = new URL(titlej.getJSONArray(3).getString(12));
				Date datePublished = new Date(titlej.getJSONArray(3).getLong(19));
				//Date dateOther = new Date(titlej.getJSONArray(3).getLong(38));	// Equals to dateCreated
				TitleData toAdd = new TitleData(id, title, previewURL, videoURL, dateCreated, datePublished, countedViews);
				System.out.println(toAdd);
				res.add(toAdd);
			} catch (MalformedURLException e) {
				throw new RuntimeException("YouTube returned an incorrect url :)", e);
			}
		}
		
		return res;

	}
	
	private static List<TitleData> decodeQueryTitleData(JSONArray level3) {
		List<TitleData> res = new ArrayList<TitleData>();
		
		for (int i = 0; i < level3.length(); i++) {
			try {
				JSONArray titlej = level3.getJSONArray(i);
				String id = titlej.getJSONArray(1).getString(2);
				String title = titlej.getJSONArray(3).getString(1);
				Date dateCreated = new Date(titlej.getJSONArray(3).getLong(2));
				long countedViews = titlej.getJSONArray(3).getLong(5);
				URL videoURL = new URL("http://" + titlej.getJSONArray(3).getString(11));
				URL previewURL = new URL(titlej.getJSONArray(3).getString(12));
				Date datePublished = new Date(titlej.getJSONArray(3).getLong(19));
				//Date dateOther = new Date(titlej.getJSONArray(3).getLong(38));	// Equals to dateCreated
				TitleData toAdd = new TitleData(id, title, previewURL, videoURL, dateCreated, datePublished, countedViews);
				System.out.println(toAdd);
				res.add(toAdd);
			} catch (MalformedURLException e) {
				throw new RuntimeException("YouTube returned an incorrect url :)", e);
			}
		}
		
		return res;
	}
	
	public static LinkedHashSet<VideoData> decodeVideosData(List<String> videoIds, JSONArray respResult) throws DataDecodeException {
		try {
			LinkedHashSet<VideoData> res = new LinkedHashSet<>();
			
			JSONArray titles = respResult.getJSONArray(2);
			List<TitleData> titlesData = decodeQueryTitleData(extractLevel3(titles));
			TitleData[] tds = titlesData.toArray(new TitleData[] {});
			
			JSONArray tables = respResult.getJSONArray(1);
			for (int i = 0; i < tds.length; i++) {
				JSONArray respResult_48Hours1 = tables.getJSONArray(2 * i + 0);
				JSONArray respResult_48Hours2 = respResult_48Hours1.getJSONArray(1);
				
				JSONArray respResult_60Minutes1 = tables.getJSONArray(2 * i + 1);
				JSONArray respResult_60Minutes2 = respResult_60Minutes1.getJSONArray(1);
		
				Map<Date, Long> views48Hours = decodeTable(respResult_48Hours2);
				Map<Date, Long> views60Minutes = decodeTable(respResult_60Minutes2);
				
				String id = videoIds.get(i);
				TitleData td = null;
				for (int p = 0; p < tds.length; p++) {
					if (tds[p].id.equals(id)) {
						td = tds[p];
						break;
					}
				}
				if (td == null) throw new DataDecodeException("Can't find the id " + id + " among the returned title list");
				VideoData vd = new VideoData(td, views48Hours, views60Minutes);
				System.out.println(vd);
				res.add(vd);
			}
			return res;
		} catch (JSONException e) {
			throw new DataDecodeException(e);
		}
	}
}
