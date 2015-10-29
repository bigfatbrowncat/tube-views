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
	private volatile boolean gracefulInterrupt = true;
	
	public void setUpdater(UpdateHandler updater) {
		this.updater = updater;
	}
	
	//private long vv = 0;
	
	public void interrupt() {
		gracefulInterrupt = true;
		HackTubeQuery.interruptConnection();
		super.interrupt();
	}
	
	private void login() {
		int attempt = 0;
		boolean loginSucceeded = false;
		while (!loginSucceeded && attempt < LOGIN_ATTEMPTS) {
			try {
				updater.reportState(Request.LOGIN, Status.STARTED);
				HackTube.login();
				loginSucceeded = true;
				updater.reportState(Request.LOGIN, Status.SUCCEEDED);
			} catch (HackTubeException e) {
				updater.reportState(Request.LOGIN, Status.FAILED);
				e.printStackTrace();
			}
			attempt ++;
		}
		
	}
	
	@Override
	public void run() {
		
		// I. Logging in to the server
		login();
		
		// II. Starting main search-query loop
		int requestsAfterSearch = 0;
		List<String> foundVideoIDs = null;
		while (true) {
			try {

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
					
					HashMap<String, Long> viewsRealtime = new HashMap<>();
					for (VideoData vd : vds) {
						String id = vd.titleData.id;
						long views = vd.titleData.countedViews;
	
						for (Long viewsPerPoint : vd.views48Hours.values()) {
							views += viewsPerPoint;
						}
	
						viewsRealtime.put(id, views);
					}
					updater.reportState(Request.QUERY, Status.SUCCEEDED);
					
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
					if (!gracefulInterrupt) {
						System.err.println("We have a problem here: ");
						e.printStackTrace();
					} else {
						System.out.println("Request interrupted gracefully");
					}
				}
				
				Thread.sleep((long) (waitTime * 1000));
				
			} catch (InterruptedException e) {
				System.out.println("Networking thread interrupted gracefully");
				break;
			}
		}
	}
}
