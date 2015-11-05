package tubeviews;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.w3c.dom.css.ViewCSS;

import firststep.Canvas;
import firststep.Canvas.Bounds;
import firststep.Canvas.HAlign;
import firststep.Canvas.VAlign;
import firststep.FloatXY;
import firststep.Font;
import firststep.Image;
import firststep.Image.Flags;
import firststep.IntXY;
import firststep.Paint;
import tubeviews.TubeData.Video;

public class VideoItemView {
	public static class MeasuresCapacitor {
		float maxWidth;
		FloatXY viewsOnPagePos, plusPos, views48Pos;
		float viewsOnPageSize, plusSize, views48Size;
		FloatXY size;
	}
	
	private Font views48Font, baseFont;
	
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
	
	private float viewsOnPageChangeTime;
	private float last48ViewsChangeTime;
	
	private Video video;

	private static long startupMoment;
	private float getTimeSinceStartup() {
		return (float)((double)System.currentTimeMillis() - startupMoment) / 1000;
	}
	
	private float textBlinkEnlarging(float startTime) {
		float raisingTime = 0.03f;
		float loweringTime = 0.2f;
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
	
	public VideoItemView(Font views48Font, Font baseFont, String videoId) {
		this.videoId = videoId;
		this.views48Font = views48Font;
		this.baseFont = baseFont;
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
	
	private float getTextHeightBase(float width, float height) {
		float heightBase = height;
		if (width < heightBase * 3.f) {
			heightBase = width / 3.f;
		}
		return heightBase;
	}
	
	public MeasuresCapacitor measure(Canvas c, MeasuresCapacitor capacitor, float width, float height) {
		float heightBase = getTextHeightBase(width, height);
		float plusSize = 0.7f * heightBase;
		float vopSize = 0.5f * heightBase;

		// Views on page
		c.textAlign(HAlign.RIGHT, VAlign.MIDDLE);
		c.fontFace(baseFont);
		c.fontSize(vopSize);
		float vopX = - plusSize / 3;
		float vopY = + height / 2 - vopSize / 16;
		Bounds vopSourceBounds = c.textBounds(vopX, vopY, String.valueOf(video.viewsOnPage));	// 1.234K

		// +
		c.textAlign(HAlign.CENTER, VAlign.MIDDLE);
		c.fontFace(views48Font);
		c.fontSize(plusSize);
		float plusX = 0;
		float plusY = height / 2 - plusSize / 10;
		Bounds pBounds = c.textBounds(plusX, plusY, "+");

		// Views 48 hours
		c.textAlign(HAlign.LEFT, VAlign.MIDDLE);
		float v48Size = 0.9f * heightBase;
		c.fontFace(views48Font);
		c.fontSize(v48Size);
		float v48X = plusSize / 3;
		float v48Y = height / 2 - v48Size / 16;
		Bounds v48Bounds = c.textBounds(v48X, v48Y, String.valueOf(video.viewsLast48Hours));	// 99.9K
		
		if (capacitor == null) {
			capacitor = new MeasuresCapacitor();
		} 
		
		float minLeft = Math.min(Math.min(vopSourceBounds.xmin, pBounds.xmin), v48Bounds.xmin);
		float maxRight = Math.max(Math.max(vopSourceBounds.xmax, pBounds.xmax), v48Bounds.xmax);
		float maxWidth = maxRight - minLeft;
		if (capacitor.maxWidth <= maxWidth) {
			capacitor.maxWidth = maxWidth;
			
			float dx = 0;
			
			if (minLeft < 0 || maxRight > width) {
				dx = -(maxRight + minLeft) / 2 + width / 2;
			}
			
			capacitor.viewsOnPagePos = new FloatXY(vopX + dx, vopY);
			capacitor.plusPos = new FloatXY(plusX + dx, plusY);
			capacitor.views48Pos = new FloatXY(v48X + dx, v48Y);
			capacitor.viewsOnPageSize = vopSize;
			capacitor.plusSize = plusSize;
			capacitor.views48Size = v48Size;

			capacitor.size = new FloatXY(width, height);

		}
	
		return capacitor;
	}
	
	public void draw(Canvas c, MeasuresCapacitor measures, float left, float top) {
		if (video != null) {
			// Image
			
			if (getPreviewImage() != null) {
			
				IntXY imgSize = previewImage.getSize();
				float imgH = ((float)imgSize.y / imgSize.x * measures.size.x); 
		
				c.beginPath();
				
				float delta = imgH / 2 - measures.size.y / 2 - top;
				Paint p = c.imagePattern(0, -delta, measures.size.x, imgH, 0, previewImage, 1.f);
				c.fillPaint(p);
				c.rect(left, top, measures.size.x, measures.size.y);
				c.fill();
			}
		
			// Text
			
			// Views on page
			c.beginPath();
			c.textAlign(HAlign.RIGHT, VAlign.MIDDLE);
			c.fontFace(baseFont);
			c.fontSize(measures.viewsOnPageSize * (0.85f + 0.15f * textBlinkEnlarging(viewsOnPageChangeTime)));
			c.text(measures.viewsOnPagePos.x, measures.viewsOnPagePos.y + top, String.valueOf(video.viewsOnPage));	// 1.234K
			c.fill();

			// +
			c.beginPath();
			c.textAlign(HAlign.CENTER, VAlign.MIDDLE);
			c.fontFace(views48Font);
			c.fontSize(measures.plusSize);
			c.text(measures.plusPos.x, measures.plusPos.y + top, "+");
			c.fill();

			// Views 48 hours
			c.beginPath();
			c.textAlign(HAlign.LEFT, VAlign.MIDDLE);
			c.fontFace(views48Font);
			c.fontSize(measures.views48Size * (0.85f + 0.15f * textBlinkEnlarging(last48ViewsChangeTime)));
			c.text(measures.views48Pos.x, measures.views48Pos.y + top, String.valueOf(video.viewsLast48Hours));	// 99.9K
			c.fill();
		}
	}
	
	public void delete() {
		if (previewImage != null) previewImage.delete();
	}
	
	/**
	 * @return true if video has changed
	 */
	public boolean setVideo(Video video) {
		boolean res = false;
		if (video != null) {
			if (this.video == null || video.viewsOnPage != this.video.viewsOnPage) {
				this.viewsOnPageChangeTime = getTimeSinceStartup();
				res = true;
			}
			if (this.video == null || video.viewsLast48Hours != this.video.viewsLast48Hours) {
				this.last48ViewsChangeTime = getTimeSinceStartup();
				res = true;
			}
		}
		this.video = video;
		return res;
	}
	
	public Video getVideo() {
		return video;
	}
	
	void testFullViewsUpdate() {
		this.viewsOnPageChangeTime = getTimeSinceStartup();
	}
	void testLast48ViewsUpdate() {
		this.last48ViewsChangeTime = getTimeSinceStartup();
	}
}
