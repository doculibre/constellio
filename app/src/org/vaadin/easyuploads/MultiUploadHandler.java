package org.vaadin.easyuploads;

import com.vaadin.server.StreamVariable.StreamingEndEvent;
import com.vaadin.server.StreamVariable.StreamingErrorEvent;
import com.vaadin.server.StreamVariable.StreamingProgressEvent;
import com.vaadin.server.StreamVariable.StreamingStartEvent;
import org.vaadin.easyuploads.MultiUpload.FileDetail;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;

public interface MultiUploadHandler extends Serializable {

	void streamingStarted(StreamingStartEvent event);

	void streamingFinished(StreamingEndEvent event);

	OutputStream getOutputStream();

	void streamingFailed(StreamingErrorEvent event);

	void onProgress(StreamingProgressEvent event);

	void filesQueued(Collection<FileDetail> pendingFileNames);

	boolean isInterrupted();

}
