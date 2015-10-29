package tubeviews;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import firststep.Canvas;
import firststep.Canvas.HAlign;
import firststep.Canvas.VAlign;
import firststep.Image;
import firststep.Image.Flags;
import firststep.IntXY;
import firststep.Paint;
import tubeviews.TubeData.Video;

public class ItemPane {
	private String videoId;
	
	/**
	 * Image data being downloaded by the {@link previewImageDownloader} thread
	 */
	private volatile byte[] previewImageData;
	
	/**
	 * Downloader of the preview image
	 */
	private Thread previewImageDownloader;
	
	/**
	 * Preview image constructed from {@link previewImageData} when the downloading is finished
	 */
	private Image previewImage;
	
	private float lastVideoChangeTime;
	
	private Video video;

	private static long startupMoment;
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
	
	private void loadPreview(URL target) {
		try {
			System.out.println("Downloading image from " + target.toString());
			
			InputStream is = target.openStream();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = is.read(buffer);
			while (len != -1) {
			    os.write(buffer, 0, len);
			    len = is.read(buffer);
			}
			
			synchronized (previewImageDownloader) {
				previewImageData = os.toByteArray();
			}
		} catch (IOException e) {
			throw new RuntimeException("Can't load image for video " + videoId, e);
		}
	}
	
	public ItemPane(String videoId) {
		this.videoId = videoId;
		startupMoment = System.currentTimeMillis();
	}

	public Image getPreviewImage() {
		if (previewImage != null) {
			return previewImage;
		} else {
			// We should create the preview image from the downloaded data
			if (previewImageDownloader == null) {
				previewImageDownloader = new Thread("Preview loading for " + videoId) {
					@Override
					public void run() {
						try {
							URL hqpreview = new URL("https://i.ytimg.com/vi/" + videoId + "/maxresdefault.jpg");
							loadPreview(hqpreview);
						} catch (Exception e) {
							System.err.println("Error occured while downloading of the preview image for video " + videoId);
							e.printStackTrace();
							
							// Resetting the previewImageDownloader value to download again
							previewImageDownloader = null; 
						}
					}
				};
				previewImageDownloader.start();
				// Downloading has just started
				return null;
			} else {
			
				synchronized (previewImageDownloader) {
					if (previewImageData != null) {
						try {
							previewImage = new Image(new ByteArrayInputStream(previewImageData), Flags.NONE);
						} catch (IOException e) {
							System.err.println("Error occured while creating of the preview image for video " + videoId + " from the downloaded data");
							e.printStackTrace();
							
							// Resetting the previewImageDownloader value to download the image again
							previewImageDownloader = null;
						}						
						return previewImage;
					} else {
						// Still downloading
						return null;
					}
				}
				
			}
	
		}
	}
	
	public void draw(Canvas c, float left, float top, float width, float height) {
		if (video != null) {
			if (getPreviewImage() != null) {
			
				IntXY imgSize = previewImage.getSize();
				float imgH = ((float)imgSize.getY() / imgSize.getX() * width); 
		
				c.beginPath();
				
				float delta = imgH / 2 - height / 2 - top;
				Paint p = c.imagePattern(0, -delta, width, imgH, 0, previewImage, 1.f);
				c.fillPaint(p);
				c.rect(0, top, width, height);
				c.fill();
			}
		
			c.beginPath();
			c.textAlign(HAlign.CENTER, VAlign.MIDDLE);
			float textSize = height * (0.85f + 0.15f * textBlinkEnlarging(lastVideoChangeTime));
			c.fontSize(textSize);
			
			c.text(left + width / 2, top + height / 2, String.valueOf(video.viewsCount));
			c.fill();
		}
	}
	
	public void delete() {
		if (previewImage != null) previewImage.delete();
	}
	
	/**
	 * 
	 * @param video
	 * @return <code>true</code> if the video actually changed 
	 */
	public boolean setVideo(Video video) {
		boolean res = false;
		if (this.video == null || video.viewsCount != this.video.viewsCount) {
			this.lastVideoChangeTime = (float)System.currentTimeMillis() / 1000;
			res = true;
		}
		this.video = video;
		return res;
	}
}
