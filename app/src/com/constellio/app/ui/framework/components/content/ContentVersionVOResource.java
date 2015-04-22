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
package com.constellio.app.ui.framework.components.content;

import java.io.InputStream;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.vaadin.server.StreamResource;

public class ContentVersionVOResource extends StreamResource {

	private static final String STREAM_NAME = "ContentVersionVOResource-InputStream";

	public ContentVersionVOResource(final ContentVersionVO contentVersionVO) {
		super(new StreamSource() {
			@Override
			public InputStream getStream() {
				return contentVersionVO.getInputStreamProvider().getInputStream(STREAM_NAME);
			}
		}, contentVersionVO.getFileName());
	}

}
