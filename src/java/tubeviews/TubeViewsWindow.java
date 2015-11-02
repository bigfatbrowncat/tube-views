package tubeviews;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import firststep.Color;
import firststep.Font;
import firststep.Window;
import tubeviews.TubeThread.Request;
import tubeviews.TubeThread.Status;
import tubeviews.VideoItemView.MeasuresCapacitor;

public class TubeViewsWindow extends Window implements TubeThread.UpdateHandler {
	
	private static final String APPNAME = "Tube Views";
	private static float fps = 25.0f;
	
	private SoundThread soundThread = new SoundThread();
	private TubeThread tubeThread = new TubeThread();
	
	private Font lightFont, regularFont;
	
	private TubeData data;
	//private HashMap<String, Float> lastVideoChangeTimes = new HashMap<>();

	private HashMap<String, VideoItemView> panes = new HashMap<>();
			
	@Override
	protected synchronized void onFrame() {
		if (data != null) {
			int j = 0;
			float h = getHeight() / data.videos.size();
			
			VideoItemView.MeasuresCapacitor measuresCapacitor = null;
			for (String id : data.videos.keySet()) {
				if (panes.containsKey(id)) {
					measuresCapacitor = panes.get(id).measure(this, measuresCapacitor, getWidth(), h);
				}
				
				j++;
			}
			
			j = 0;
			for (String id : data.videos.keySet()) {
				float yt = h * j;
				if (panes.containsKey(id)) {
					panes.get(id).draw(this, measuresCapacitor, 0, yt);
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

		boolean kick = false;
		if (oldData != null && data != null) {
			for (String id : data.videos.keySet()) {
				
				if (data.videos.containsKey(id)) {
					if (!panes.containsKey(id)) {
						panes.put(id, new VideoItemView(lightFont, regularFont, id));
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
			lightFont = Font.createOrFindFont("light", is);
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
        new TubeViewsWindow();
		Window.loop(fps);
	}
	
}