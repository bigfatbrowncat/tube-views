package tubeviews;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import firststep.Color;
import firststep.Font;
import firststep.Window;
import tubeviews.TubeData.Video;
import tubeviews.TubeThread.Request;
import tubeviews.TubeThread.Status;

public class TubeViewsWindow extends Window implements AutoCloseable, TubeThread.UpdateHandler {
	
	private static final String APPNAME = "Tube Views";
	private static float fps = 25.0f;
	
	private SoundThread soundThread = new SoundThread();
	private TubeThread tubeThread = new TubeThread();
	
	private Font thinFont, regularFont;
	
	private TubeData data;
	//private HashMap<String, Float> lastVideoChangeTimes = new HashMap<>();

	private HashMap<String, VideoItemView> panes = new HashMap<>();
			
	@Override
	protected synchronized void onFrame() {
		if (data != null) {

			// Sorting videos
			List<String> videoKeys = new ArrayList<String>(data.videos.keySet());
			Collections.sort(videoKeys, new Comparator<String>() {
				@Override
				public int compare(String id1, String id2) {
					// The topmost video is the one that is 
					// viewed most times during the last 48 hours
					return (int)(data.videos.get(id2).viewsLast48Hours - data.videos.get(id1).viewsLast48Hours); 
				}
			});
			
			// Calculating the measures
			float minAspect = 1.f / 6;		// minimal height/width value
			float maxAspect = 2.f / 5;		// maximal height/width value

			int shownCount = data.videos.size();
			float h = getHeight() / shownCount;
			while ((shownCount > 1) && (h / getWidth() < minAspect)) {
				shownCount --;
				h = getHeight() / shownCount;
			}
			
			if (h / getWidth() > maxAspect) {
				h = maxAspect * getWidth();
			}
			
			// Measuring the panes on the screen
			VideoItemView.MeasuresCapacitor measuresCapacitor = null;
			for (int j = 0; j < shownCount; j++) {
				String id = videoKeys.get(j);
				if (panes.containsKey(id)) {
					measuresCapacitor = panes.get(id).measure(this, measuresCapacitor, getWidth(), h);
				}
			}
			
			// Drawing the panes on the screen
			for (int j = 0; j < shownCount; j++) {
				String id = videoKeys.get(j);
				float yt = h * j;
				if (panes.containsKey(id)) {
					panes.get(id).draw(this, measuresCapacitor, 0, yt);
				}
			}	
		}
	}
	
	/**
	 * @param viewsCounter
	 * @return how much we should wait before the next request
	 */
	@Override
	public synchronized float update(TubeData data) {
		TubeData oldData = this.data;
		this.data = data;

		boolean kick = false;
		if (oldData != null && data != null) {
			for (String id : data.videos.keySet()) {
				
				if (data.videos.containsKey(id)) {
					if (!panes.containsKey(id)) {
						panes.put(id, new VideoItemView(thinFont, regularFont, id));
					}

					// Due to a rare YouTube bug (709<->707), 
					// we hack viewsOnPage not to decrease
					Video oldVideo = panes.get(id).getVideo();
					Video newVideo = data.videos.get(id);
					if (newVideo != null && oldVideo != null && newVideo.viewsOnPage < oldVideo.viewsOnPage) {
						data.videos.put(id, new Video(
								newVideo.name, 
								newVideo.preview, 
								oldVideo.viewsOnPage,
								newVideo.viewsLast48Hours,
								newVideo.viewsLastHour,
								newVideo.viewsLastMinute
								));
					}
					
					if (panes.get(id).setVideo(data.videos.get(id))) kick = true;
				}
			}
		}
		if (kick) {
			soundThread.randomKick();
		}
			
		return 1.0f;
	}
	
	@Override
	protected void onSizeChange(final int width, final int height) {
		
	}

	@Override
	public void close() {
		if (soundThread != null) {
			soundThread.stopSound();
			soundThread = null;
		}
		if (tubeThread != null) {
			tubeThread.cancel();
			tubeThread = null;
		}
		System.out.println("Threads stopped");
	}
	
	@Override
	public boolean onCloseAsked() {
		close();
		return super.onCloseAsked();
	}
	
	private static float backRed = 0.2f, backGreen = 0.21f, backBlue = 0.22f;
	public TubeViewsWindow() {
		super (APPNAME, 480, 360, Color.fromRGBA(backRed, backGreen, backBlue, 1.0f));
		
		try {
			InputStream is = TubeViewsWindow.class.getResourceAsStream("/tubeviews/ClearSans-Thin.ttf");
			thinFont = Font.createOrFindFont("thin", is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			InputStream is = TubeViewsWindow.class.getResourceAsStream("/tubeviews/ClearSans-Regular.ttf");
			regularFont = Font.createOrFindFont("regular", is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		tubeThread.setUpdater(this);
		
		soundThread.start();
		tubeThread.start();
	}

	@Override
	public void reportState(Request request, Status status) {
		System.out.println("State change: " + request + "; " + status);
	}

	@Override
	public void onKeyStateChange(Key key, int scancode, KeyState state, Modifiers modifiers) {
		if (state == KeyState.PRESS) {
			if (key == Key.SPACE && modifiers.isEmpty()) {
				soundThread.randomKick();
				for (VideoItemView ip : panes.values()) {
					ip.testFullViewsUpdate();
					ip.testLast48ViewsUpdate();
				}
			} else if (key == Key.F && modifiers.isEmpty()) {
				soundThread.randomKick();
				for (VideoItemView ip : panes.values()) {
					ip.testFullViewsUpdate();
				}
			} else if (key == Key.TOP_4 && modifiers.isEmpty()) {
				soundThread.randomKick();
				for (VideoItemView ip : panes.values()) {
					ip.testLast48ViewsUpdate();
				}
			}
		}
	}
	
	public static void main(String... args) {
		try (TubeViewsWindow mainWindow = new TubeViewsWindow()){
			Window.loop(fps);
		}
	}
	
}