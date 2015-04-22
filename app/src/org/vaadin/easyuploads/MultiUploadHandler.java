/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.vaadin.easyuploads;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;

import org.vaadin.easyuploads.MultiUpload.FileDetail;

import com.vaadin.server.StreamVariable.StreamingEndEvent;
import com.vaadin.server.StreamVariable.StreamingErrorEvent;
import com.vaadin.server.StreamVariable.StreamingProgressEvent;
import com.vaadin.server.StreamVariable.StreamingStartEvent;

public interface MultiUploadHandler extends Serializable {

	void streamingStarted(StreamingStartEvent event);

	void streamingFinished(StreamingEndEvent event);

	OutputStream getOutputStream();

	void streamingFailed(StreamingErrorEvent event);

	void onProgress(StreamingProgressEvent event);

	void filesQueued(Collection<FileDetail> pendingFileNames);

	boolean isInterrupted();

}
