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
	
	private SoundThread soundThread = new SoundThread();
	private TubeThread tubeThread = new TubeThread();
	
	private Font lightFont;
	
	private TubeData data;
	//private HashMap<String, Float> lastVideoChangeTimes = new HashMap<>();

	private HashMap<String, ItemPane> panes = new HashMap<>();
	
	private static long startupMoment;
	private float getTimeSinceStartup() {
		return (float)((double)System.currentTimeMillis() - startupMoment) / 1000;
	}
		
	@Override
	protected synchronized void onFrame() {
		if (data != null) {
			int j = 0;
			
			for (String id : data.videos.keySet()) {
				float h = getHeight() / data.videos.size();
				
				float yt = h * j;
				
				fontFace(lightFont);
				
				if (panes.containsKey(id)) {
					panes.get(id).draw(this, 0, yt, getWidth(), h);
				}
				
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
				
				if (data.videos.containsKey(id)) {
					if (!panes.containsKey(id)) {
						panes.put(id, new ItemPane(id));
					}

					panes.get(id).setVideo(data.videos.get(id));
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
		startupMoment = System.currentTimeMillis();
		
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