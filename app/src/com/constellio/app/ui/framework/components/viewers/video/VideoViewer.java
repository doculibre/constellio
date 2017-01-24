package com.constellio.app.ui.framework.components.viewers.video;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Video;

//import com.kbdunn.vaadin.addons.mediaelement.MediaElementPlayer;
//import com.kbdunn.vaadin.addons.mediaelement.MediaElementPlayerOptions;
//import com.kbdunn.vaadin.addons.mediaelement.PlayedListener;

public class VideoViewer extends CustomComponent {

	private static Map<String, File> cache = new HashMap<>();

	public static final String[] SUPPORTED_EXTENSIONS = { /*"mp4"*/ };

	public VideoViewer(ContentVersionVO contentVersionVO) {
		try {
			final String fileName = contentVersionVO.getFileName();
			final String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));

			final int width = 720;
			final int height = 480;
			//			setWidth(width + "px");
			//			setHeight(height + "px");

			boolean useMediaElementPlayer = false;
			if (useMediaElementPlayer) {
				//				String hash = contentVersionVO.getHash();
				//				File mediaViewerFile = cache.get(hash);
				//				if (mediaViewerFile == null) {
				//					IOServices ioServices = ConstellioFactories.getInstance().getIoServicesFactory().newIOServices();
				//					ContentManager contentManager = ConstellioFactories.getInstance().getModelLayerFactory().getContentManager();
				//					File tempDir = ioServices.newTemporaryFolder(filename);
				//					mediaViewerFile = new File(tempDir, filename);
				//					mediaViewerFile.deleteOnExit();
				//
				//					InputStream in = null;
				//					OutputStream out = null;
				//					try {
				//						in = contentManager.getContentInputStream(hash, getClass() + ".mediaViewerFile");
				//						out = new FileOutputStream(mediaViewerFile);
				//						IOUtils.copy(in, out);
				//						cache.put(hash, mediaViewerFile);
				//					} catch (FileNotFoundException e) {
				//						mediaViewerFile = null;
				//					} catch (IOException e) {
				//						mediaViewerFile = null;
				//					} finally {
				//						IOUtils.closeQuietly(in);
				//						IOUtils.closeQuietly(out);
				//					}
				//				}
				//				final File mediaViewerFinalFile = mediaViewerFile;
				//				MediaElementPlayer mediaPlayer = new MediaElementPlayer() {
				//					@Override
				//					public void attach() {
				//						super.attach();
				//						setSource(new FileResource(mediaViewerFinalFile) {
				//							@Override
				//							public String getMIMEType() {
				//								return getExtraMimeType(extension, super.getMIMEType());
				//							}
				//						});
				//					}
				//				};
				//
				//				mediaPlayer.addPlayListener(new PlayedListener() {
				//					@Override
				//					public void played(MediaElementPlayer player) {
				//						System.err.println(player.getConnectorId());
				//						JavaScript javascript = new JavaScript();
				//						javascript.execute("setTimeout(function() { document.getElementsByTagName('video')[0].src='test'; document.getElementsByTagName('video')[0].style.width='" + width + "px'; document.getElementsByTagName('video')[0].style.height='" + height + "px';}, 1000);");
				//					}
				//				});
				//
				//				MediaElementPlayerOptions playerOptions = new MediaElementPlayerOptions();
				//				playerOptions.setEnableAutosize(true);
				//				playerOptions.setVideoWidth(width);
				//				playerOptions.setVideoHeight(height);
				//				mediaPlayer.setOptions(playerOptions);
				//				mediaPlayer.setWidth(width + "px");
				//				mediaPlayer.setHeight(height + "px");
				//
				//				setCompositionRoot(mediaPlayer);
			} else {
				Video video = new Video();
				video.setHtmlContentAllowed(true);
				video.setSource(new ContentVersionVOResource(contentVersionVO) {
					@Override
					public String getMIMEType() {
						return getExtraMimeType(extension, super.getMIMEType());
					}
				});
				video.setWidth(width + "px");
				video.setHeight(height + "px");

				setCompositionRoot(video);
			}
		} catch (Throwable t) {
			// FIXME
			t.printStackTrace();
			setVisible(false);
		}
	}

	private String getExtraMimeType(String extension, String mimeType) {
		if (mimeType == null || !(mimeType.startsWith("video") && !mimeType.equals("application/x-mpegURL"))) {
			if ("flv".equals(extension)) {
				mimeType = "video/x-flv";
			} else if ("ts".equals(extension)) {
				mimeType = "video/MP2T";
			} else if ("3gp".equals(extension)) {
				mimeType = "video/3gp";
			} else if ("mov".equals(extension)) {
				mimeType = "video/quicktime";
			} else if ("avi".equals(extension)) {
				mimeType = "video/x-msvideo";
			} else if ("wmv".equals(extension)) {
				//				mimeType = "video/x-ms-wmv";
				mimeType = "video/wmv";
			} else if ("mkv".equals(extension)) {
				mimeType = "video/x-matroska";
			}
		}
		return mimeType;
	}

}
