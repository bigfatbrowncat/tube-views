package tubeviews;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import firststep.Color;
import firststep.Font;
import firststep.Image;
import firststep.IntXY;
import firststep.Paint;
import firststep.Window;
import hacktube.HackTube;
import hacktube.HackTubeException;

public class TubeViewsWindow extends Window implements TubeThread.UpdateHandler {
	
	private static final String APPNAME = "Tube Views";
	private static float fps = 25.0f;
	private static long startupMoment;
	
	private SoundThread soundThread = new SoundThread();
	private TubeThread tubeThread = new TubeThread();
	
	private Font lightFont;
	
	private TubeData data;
	private HashMap<String, Float> lastVideoChangeTimes = new HashMap<>();

	private HashMap<String, ItemPane> panes = new HashMap<>();
	
	private float getTimeSinceStartup() {
		return (float)((double)System.currentTimeMillis() - startupMoment) / 1000;
	}
	
	
	private float textBlinkEnlarging(float startTime) {
		float raisingTime = 0.03f;
		float loweringTime = 0.1f;
		float curTime = getTimeSinceStartup();
		
		if (curTime < startTime) return 0.0f; 
		else if (curTime - startTime < raisingTime) {
			float x = curTime - startTime;
			return x / raisingTime;
		} else if (curTime - startTime < raisingTime + loweringTime) {
			float x = curTime - startTime - raisingTime;
			return 1.0f - x / loweringTime;
		} else {
			return 0.0f;
		}
	}
		
	@Override
	protected synchronized void onFrame() {
		if (data != null) {
			int j = 0;
			
			for (String id : data.videos.keySet()) {
				float h = getHeight() / data.videos.size();
				
				float x = getWidth() / 2, yt = h * j, y = h * (0.5f + j);
				
				if (panes.containsKey(id)) {
					panes.get(id).draw(this, 0, yt, getWidth(), h);

				} else {
					panes.put(id, new ItemPane(id));
				}
								
				// Drawing counter
	
				beginPath();
				textAlign(HAlign.CENTER, VAlign.MIDDLE);
				fontFace(lightFont);
				float textSize = h * (0.85f + 0.15f * textBlinkEnlarging(lastVideoChangeTimes.get(id) != null ? lastVideoChangeTimes.get(id) : 0));
				fontSize(textSize);
				
				text(x, y, String.valueOf(data.videos.get(id).viewsCount));
				fill();
				j++;
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
		float curTime = getTimeSinceStartup();

		boolean kick = false;
		if (oldData != null && data != null) {
			for (String id : data.videos.keySet()) {
				if (data.videos.containsKey(id) && oldData.videos.containsKey(id)) {
					if (data.videos.get(id).viewsCount != oldData.videos.get(id).viewsCount) {
						lastVideoChangeTimes.put(id, curTime);
						kick = true;
					}
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
	public void onClose() {
		soundThread.stopSound();
		tubeThread.interrupt();
		super.onClose();
	}
	
	private static float backRed = 0.2f, backGreen = 0.21f, backBlue = 0.22f;
	public TubeViewsWindow() {
		super (APPNAME, 480, 360, Color.fromRGBA(backRed, backGreen, backBlue, 1.0f));
		
		try {
			InputStream is = TubeViewsWindow.class.getResourceAsStream("/tubeviews/ClearSans-Light.ttf");
			lightFont = Font.createOrFindFont("bold", is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		startupMoment = System.currentTimeMillis();
		
		tubeThread.setUpdater(this);
		
		soundThread.start();
		tubeThread.start();
	}
	
	public static void main(String... args) {
        
        try {
			HackTube.login();
		} catch (HackTubeException e) {
			e.printStackTrace();
		}
	        
        /*try {
			JSONObject resp = HackTubeSearchQuery.requestJSON();

			HackTubeData.decodeSearchTitleData(resp);
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        
        new TubeViewsWindow();
		Window.loop(fps);
	}
	
}