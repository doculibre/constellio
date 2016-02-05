package org.vaadin.easyuploads;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.StreamVariable;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;

/**
 * Server side component for the VMultiUpload widget. Pretty much hacked up
 * together to test new Receiver support in the GWT terminal.
 */
@SuppressWarnings("serial")
public class MultiUpload extends AbstractComponent implements LegacyComponent {

	List<FileDetail> pendingFiles = new ArrayList<FileDetail>();

	private MultiUploadHandler receiver;

	StreamVariable streamVariable = new StreamVariable() {

		public void streamingStarted(StreamingStartEvent event) {
			final FileDetail next = getPendingFileNames().iterator().next();
			receiver.streamingStarted(new StreamingStartEvent() {

				public String getMimeType() {
					return next.getMimeType();
				}

				public String getFileName() {
					return next.getFileName();
				}

				public long getContentLength() {
					return next.getContentLength();
				}

				public long getBytesReceived() {
					return 0;
				}

				public void disposeStreamVariable() {

				}
			});
		}

		public void streamingFinished(final StreamingEndEvent event) {

			final FileDetail next = getPendingFileNames().iterator().next();

			receiver.streamingFinished(new StreamingEndEvent() {

				public String getMimeType() {
					return next.getMimeType();
				}

				public String getFileName() {
					return next.getFileName();
				}

				public long getContentLength() {
					return next.getContentLength();
				}

				public long getBytesReceived() {
					return event.getBytesReceived();
				}

			});
			pendingFiles.remove(0);
		}

		public void streamingFailed(StreamingErrorEvent event) {
			receiver.streamingFailed(event);
		}

		public void onProgress(StreamingProgressEvent event) {
			receiver.onProgress(event);
		}

		public boolean listenProgress() {
			return true;
		}

		public boolean isInterrupted() {
			return receiver.isInterrupted();
		}

		public OutputStream getOutputStream() {
			return receiver.getOutputStream();
		}
	};

	private boolean ready;

	public void setHandler(MultiUploadHandler receiver) {
		this.receiver = receiver;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		// super.paintContent(target);
		target.addVariable(this, "target", streamVariable);
		if (ready) {
			target.addAttribute("ready", true);
			ready = false;
		}
		target.addAttribute("buttoncaption", getButtonCaption());
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		// super.changeVariables(source, variables);
		if (variables.containsKey("filequeue")) {
			String[] filequeue = (String[]) variables.get("filequeue");
			List<FileDetail> newFiles = new ArrayList<FileDetail>(
					filequeue.length);
			for (String string : filequeue) {
				newFiles.add(new FileDetail(string));
			}
			receiver.filesQueued(newFiles);
			pendingFiles.addAll(newFiles);
			requestRepaint();
			ready = true;
		}
	}

	public Collection<FileDetail> getPendingFileNames() {
		return Collections.unmodifiableCollection(pendingFiles);
	}

	public void setButtonCaption(String buttonCaption) {
		this.buttonCaption = buttonCaption;
	}

	public String getButtonCaption() {
		return buttonCaption;
	}

	public static final class FileDetail implements Serializable {
		private String caption;
		private String mimeType = "application/octet-stream";
		private long contentLength;

		public FileDetail(String data) {
			String[] split = data.split("---xXx---");
			caption = split[1];
			contentLength = Long.parseLong(split[0]);
      if (split.length >= 3) {
				mimeType = split[2];
			}
		}

		public long getContentLength() {
			return contentLength;
		}

		public String getFileName() {
			return caption;
		}

		public String getMimeType() {
			return mimeType;
		}
	}

	private String buttonCaption = "...";

}
