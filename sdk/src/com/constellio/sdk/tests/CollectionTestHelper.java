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
package com.constellio.sdk.tests;

import java.util.List;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;

public class CollectionTestHelper {

	protected List<String> collections;

	protected AppLayerFactory appLayerFactory;
	protected ModelLayerFactory modelLayerFactory;
	protected DataLayerFactory dataLayerFactory;
	protected IOServicesFactory ioLayerFactory;
	protected FileSystemTestFeatures fileSystemTestFeatures;

	public CollectionTestHelper(List<String> collections, AppLayerFactory appLayerFactory,
			FileSystemTestFeatures fileSystemTestFeatures) {
		this.collections = collections;
		this.fileSystemTestFeatures = fileSystemTestFeatures;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		this.ioLayerFactory = dataLayerFactory.getIOServicesFactory();
	}

	public CollectionTestHelper giveWriteAndDeleteAccessTo(String... users) {
		for (String collection : collections) {
			UserServices userServices = modelLayerFactory.newUserServices();
			RecordServices recordServices = modelLayerFactory.newRecordServices();

			Transaction transaction = new Transaction();
			for (String userWithReadAccess : users) {
				User user = userServices.getUserInCollection(userWithReadAccess, collection);
				user.setCollectionWriteAccess(true);
				user.setCollectionDeleteAccess(true);
				transaction.add(user.getWrappedRecord());
			}
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
		return this;

	}

	public CollectionTestHelper giveReadAccessTo(String... users) {
		for (String collection : collections) {
			UserServices userServices = modelLayerFactory.newUserServices();
			RecordServices recordServices = modelLayerFactory.newRecordServices();

			Transaction transaction = new Transaction();
			for (String userWithReadAccess : users) {
				User user = userServices.getUserInCollection(userWithReadAccess, collection);
				user.setCollectionReadAccess(true);
				transaction.add(user.getWrappedRecord());
			}
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
		return this;

	}

	public void setCollectionTitleTo(String title) {
		for (String collection : collections) {
			RecordWrapper collectionWrapper = appLayerFactory.getCollectionsManager().getCollection(collection).setTitle(title);
			try {
				modelLayerFactory.newRecordServices().update(collectionWrapper.getWrappedRecord());
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
