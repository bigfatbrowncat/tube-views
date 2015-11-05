package tubeviews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hacktube.HackTube;
import hacktube.HackTubeData;
import hacktube.HackTubeException;
import hacktube.HackTubeQuery;
import hacktube.HackTubeSearch;
import hacktube.HackTubeServiceLogin;
import hacktube.HackTubeServiceLoginAuth;
import hacktube.TitleData;
import hacktube.VideoData;
import tubeviews.TubeData.Video;

public class TubeThread extends Thread {
	private static final int LOGIN_ATTEMPTS = 3;
	
	public enum Request {
		LOGIN, SEARCH, QUERY;
	}
	
	public enum Status {
		STARTED, SUCCEEDED, FAILED;
	}
	
	public interface UpdateHandler {
		void reportState(Request request, Status status);
		
		float update(TubeData data);
	}
	
	private UpdateHandler updater;
	private volatile boolean gracefulCancel = true;
	
	public void setUpdater(UpdateHandler updater) {
		this.updater = updater;
	}
	
	//private long vv = 0;
	
	public void cancel() {
		gracefulCancel = true;
		HackTube.cancelAllConnections();
	}
	
	private boolean login() {
		int attempt = 0;
		boolean loginSucceeded = false;
		while (!loginSucceeded && attempt < LOGIN_ATTEMPTS) {
			try {
				updater.reportState(Request.LOGIN, Status.STARTED);
				if (!HackTube.login()) return false;
				loginSucceeded = true;
				updater.reportState(Request.LOGIN, Status.SUCCEEDED);
			} catch (HackTubeException e) {
				e.printStackTrace();
			}
			attempt ++;
		}
		return loginSucceeded;
	}
	
	@Override
	public void run() {

		// I. Logging in to the server
		if (!login()) {
			updater.reportState(Request.LOGIN, Status.FAILED);
			gracefulCancel = true;
		}
		
		// II. Starting main search-query loop
		int requestsAfterSearch = 0;
		List<String> foundVideoIDs = null;
		while (!gracefulCancel) {
			HashMap<String, Video> videosMap = new HashMap<>();
			float waitTime = 1.0f;
			
			try {
				if (requestsAfterSearch == 0) {

					updater.reportState(Request.SEARCH, Status.STARTED);
					List<TitleData> tds = HackTubeSearch.request();
					foundVideoIDs = new ArrayList<>();
					for (TitleData td : tds) {
						foundVideoIDs.add(td.id);
					}
					updater.reportState(Request.SEARCH, Status.SUCCEEDED);
				}

				updater.reportState(Request.QUERY, Status.STARTED);
				List<VideoData> vds = HackTubeQuery.request(foundVideoIDs);
				
				// Counting views per last 48 hours
				HashMap<String, Long> views48Hours = new HashMap<>();
				for (VideoData vd : vds) {
					String id = vd.titleData.id;
					long views = 0;

					for (Long viewsPerPoint : vd.views48Hours.values()) {
						views += viewsPerPoint;
					}

					views48Hours.put(id, views);
				}
				updater.reportState(Request.QUERY, Status.SUCCEEDED);
				
				HashMap<String, VideoData> vdss = new HashMap<>();
				for (VideoData vd : vds) {
					vdss.put(vd.titleData.id, vd);
				}
				
				for (String s : vdss.keySet()) {
					videosMap.put(s, 
							new Video(
									vdss.get(s).titleData.title, 
									vdss.get(s).titleData.previewImage,
									vdss.get(s).titleData.countedViews,
									views48Hours.get(s),
									0L,	// TODO Implement
									0L	// TODO Implement
							)
					);
					System.out.println(s + ": " + vdss.get(s).titleData.countedViews + " + " + views48Hours.get(s));
				}
				System.out.println();
				
				waitTime = updater.update(new TubeData(videosMap));
				
				requestsAfterSearch = (requestsAfterSearch + 1) % 10;
				
			} catch (HackTubeException e) {
				if (!gracefulCancel) {
					System.err.println("We have a problem here: ");
					e.printStackTrace();
				} else {
					System.out.println("Request interrupted gracefully");
					break;
				}
			}
			
			try {
				Thread.sleep((long) (waitTime * 1000));
			} catch (InterruptedException e) {
				System.out.println("Sleep interrupted");
				e.printStackTrace();
			}

		}
		System.out.println("Networking thread finished gracefully");

	}
}
