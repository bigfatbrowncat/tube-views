package tubeviews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import hacktube.HackTubeData;
import hacktube.HackTubeException;
import hacktube.HackTubeQuery;
import hacktube.HackTubeSearch;
import hacktube.TitleData;
import hacktube.VideoData;
import tubeviews.TubeData.Video;

public class TubeThread extends Thread {
	public interface UpdateHandler {
		float update(TubeData data);
	}
	
	private UpdateHandler updater;
	public void setUpdater(UpdateHandler updater) {
		this.updater = updater;
	}
	
	//private long vv = 0;
	
	public void interrupt() {
		HackTubeQuery.interruptConnection();
		super.interrupt();
	}
	
	@Override
	public void run() {
		int requestsAfterSearch = 0;
		List<String> videosList = null;
		while (true) {
			try {

				HashMap<String, Video> videosMap = new HashMap<>();
				float waitTime = 1.0f;
				
				try {
					if (requestsAfterSearch == 0) {
						JSONObject resp = HackTubeSearch.requestJSON();
						JSONArray respResult = (JSONArray) resp.get("result");
						List<TitleData> tds = HackTubeData.decodeSearchTitleData(respResult);
						videosList = new ArrayList<>();
						for (TitleData td : tds) {
							videosList.add(td.id);
						}
					}

					
					JSONObject resp = HackTubeQuery.requestJSON(videosList);
					JSONArray respResult = (JSONArray) resp.get("result");
					
					Set<VideoData> vds = HackTubeData.decodeVideosData(videosList, respResult);
					
					HashMap<String, Long> viewsRealtime = new HashMap<>();
					for (VideoData vd : vds) {
						String id = vd.titleData.id;
						long views = vd.titleData.countedViews;
	
						for (Long viewsPerPoint : vd.views48Hours.values()) {
							views += viewsPerPoint;
						}
	
						viewsRealtime.put(id, views);
					}
					
					HashMap<String, VideoData> vdss = new HashMap<>();
					for (VideoData vd : vds) {
						vdss.put(vd.titleData.id, vd);
					}
					
					//overallViews += vv+=2;
					for (String s : viewsRealtime.keySet()) {
						videosMap.put(s, new Video(vdss.get(s).titleData.title, vdss.get(s).titleData.previewImage, viewsRealtime.get(s)));
						System.out.println(s + ": " + viewsRealtime.get(s));
					}
					System.out.println();
					
					waitTime = updater.update(new TubeData(videosMap));
					
					requestsAfterSearch = (requestsAfterSearch + 1) % 10;
	
				} catch (HackTubeException e) {
					System.out.println("We have a problem here: ");
					e.printStackTrace();
				}
				
				Thread.sleep((long) (waitTime * 1000));
				
			} catch (InterruptedException e) {
				System.out.println("Interrupted by user!");
				break;
			}
		}
	}
}
