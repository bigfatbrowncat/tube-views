package tubeviews;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import firststep.Image;
import firststep.Image.Flag;
import firststep.Image.Flags;

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
	
	public void delete() {
		if (previewImage != null) previewImage.delete();
	}
}
