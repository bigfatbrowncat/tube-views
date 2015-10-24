package tubeviews;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import firststep.Color;
import firststep.Font;
import firststep.Image;
import firststep.Image.Flag;
import firststep.Image.Flags;
import firststep.IntXY;
import firststep.Paint;
import firststep.Window;
import hacktube.Configuration;
import hacktube.HackTubeData;
import hacktube.HackTubeException;
import hacktube.HackTube;
import hacktube.HackTubeSearchQuery;
import hacktube.RequestException;
import tubeviews.TubeThread.Updater;

public class TubeViewsWindow extends Window implements Updater {
	
	private static final String APPNAME = "Tube Views";
	private static float fps = 25.0f;
	private static long startupMoment;
	
	private SoundThread soundThread = new SoundThread();
	private TubeThread tubeThread = new TubeThread();
	
	private Font lightFont;
	
	private Data data;
	private HashMap<String, Float> lastVideoChangeTimes = new HashMap<>();

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
	
	HashMap<String, byte[]> previewData = new HashMap<>();
	private HashMap<String, Thread> downloaders = new HashMap<>();
	void loadPreview(String key, URL target) {
		try {
			System.out.println("Downloading image from " + target.toString());
			
			//InputStream is = this.getClass().getResourceAsStream("/tubeviews/default.jpg");
			InputStream is = target.openStream();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = is.read(buffer);
			while (len != -1) {
			    os.write(buffer, 0, len);
			    len = is.read(buffer);
			}
			
			synchronized (previewData) {
				previewData.put(key, os.toByteArray());
			}
		} catch (IOException e) {
			throw new RuntimeException("Can't load image for video " + key, e);
		}
	}
	private HashMap<String, Image> previewImages = new  HashMap<>();
	
	@Override
	protected synchronized void onFrame() {
		if (data != null) {
			int j = 0;
			
			for (String s : data.videos.keySet()) {
				float h = getHeight() / data.videos.size();
				
				float x = getWidth() / 2, yt = h * j, y = h * (0.5f + j);
				

				// Drawing background
				synchronized (previewData) {
					if (!previewData.containsKey(s)) {
						final String ss = s;
						if (downloaders.get(ss) == null) {
							downloaders.put(ss, new Thread("Preview loading for " + s) {
								@Override
								public void run() {
									try {
										URL hqpreview = new URL("https://i.ytimg.com/vi/" + ss + "/maxresdefault.jpg");
										loadPreview(ss, hqpreview);
									} catch (MalformedURLException e) {
										e.printStackTrace();
									}
								}
							});
							downloaders.get(ss).start();
						}

					} else {
						
						Image img = null;
						if (!previewImages.containsKey(s)) {
							try {
								img = new Image(new ByteArrayInputStream(previewData.get(s)), Flags.of(Flag.REPEATX, Flag.REPEATY));
								previewImages.put(s, img);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}						
						} else {
							img = previewImages.get(s);
						}
						
						
							
							IntXY imgSize = img.getSize();
							float imgH = ((float)imgSize.getY() / imgSize.getX() * getWidth()); 
							
							beginPath();
							
							float delta = imgH / 2 - h / 2 - yt;
							Paint p = imagePattern(0, -delta, getWidth(), imgH, 0, img, 0.8f);
							fillPaint(p);
							//fillColor(Color.fromRGBA(j * 10.0f / 100f, j * 10.0f / 100, 0, 1.0f));
							rect(0, yt, getWidth(), h);
							fill();
							
							img.delete();
						
					}
				}
				
				// Drawing counter
	
				beginPath();
				textAlign(HAlign.CENTER, VAlign.MIDDLE);
				fontFace(lightFont);
				float textSize = h * (0.85f + 0.15f * textBlinkEnlarging(lastVideoChangeTimes.get(s) != null ? lastVideoChangeTimes.get(s) : 0));
				fontSize(textSize);
				
				text(x, y, String.valueOf(data.videos.get(s).viewsCount));
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
	public synchronized float update(Data data) {
		Data oldData = this.data;
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