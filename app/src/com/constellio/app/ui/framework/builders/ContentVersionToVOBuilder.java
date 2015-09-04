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
package com.constellio.app.ui.framework.builders;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.joda.time.LocalDateTime;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ContentVersionToVOBuilder implements Serializable {

	transient ModelLayerFactory modelLayerFactory;

	public ContentVersionToVOBuilder(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
	}

	@Deprecated
	public ContentVersionVO build(Content content) {
		// TODO Separate layers
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		return build(content, sessionContext);
	}

	public ContentVersionVO build(Content content, SessionContext sessionContext) {
		// TODO Separate layers
		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String username = currentUserVO.getUsername();
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(username, collection);
		ContentVersion contentVersion = content.getCurrentVersionSeenBy(currentUser);
		return build(content, contentVersion);
	}

	public ContentVersionVO build(Content content, ContentVersion contentVersion) {
		String contentId = content.getId();
		String hash = contentVersion.getHash();
		String fileName = contentVersion.getFilename();
		String mimeType = contentVersion.getMimetype();
		long length = contentVersion.getLength();
		String version = contentVersion.getVersion();
		LocalDateTime jodaLastModificationDateTime = contentVersion.getLastModificationDateTime();
		Date lastModificationDateTime = jodaLastModificationDateTime != null ? jodaLastModificationDateTime.toDate() : null;
		String lastModifiedBy = contentVersion.getModifiedBy();
		InputStreamProvider inputStreamProvider = new ContentInputStreamProvider(hash, modelLayerFactory);
		String checkouUserId = content.getCheckoutUserId();
		LocalDateTime checkoutDateTime = content.getCheckoutDateTime();
		return new ContentVersionVO(contentId, hash, fileName, mimeType, length, version, lastModificationDateTime,
				lastModifiedBy, checkouUserId, checkoutDateTime, inputStreamProvider);
	}

	private static class ContentInputStreamProvider implements InputStreamProvider {

		private String id;
		private transient ModelLayerFactory modelLayerFactory;

		public ContentInputStreamProvider(String id, ModelLayerFactory modelLayerFactory) {
			this.id = id;
			this.modelLayerFactory = modelLayerFactory;
		}

		private void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			stream.defaultReadObject();
			init();
		}

		private void init() {
			modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		}

		@Override
		public InputStream getInputStream(String streamName) {
			//			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			//			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			ContentManager contentManager = modelLayerFactory.getContentManager();
			return contentManager.getContentInputStream(id, streamName);
		}

		@Override
		public void deleteTemp() {
			// Nothing to deleteLogically
		}
	}
}
