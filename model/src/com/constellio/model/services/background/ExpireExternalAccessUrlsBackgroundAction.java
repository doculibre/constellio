package com.constellio.model.services.background;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import lombok.AllArgsConstructor;
import org.joda.time.LocalDateTime;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

@AllArgsConstructor
public class ExpireExternalAccessUrlsBackgroundAction implements Runnable {

	ModelLayerFactory modelLayerFactory;

	@Override
	public void run() {

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem()) {

			SchemasRecordsServices records = new SchemasRecordsServices(collection, modelLayerFactory);

			LocalDateTime now = TimeProvider.getLocalDateTime();
			for (ExternalAccessUrl url : records.searchExternalAccessUrls(
					where(records.externalAccessUrl.status()).isEqualTo(ExternalAccessUrlStatus.OPEN))) {

				//TODO replace with andWhere once supported in cache
				//.andWhere(records.externalAccessUrl.expirationDate()).isLessOrEqualThan(now))) {

				if (url.getExpirationDate() != null && !url.getExpirationDate().isAfter(now)) {
					try {
						modelLayerFactory.newRecordServices().update(url.setStatus(ExternalAccessUrlStatus.EXPIRED));
					} catch (RecordServicesException e) {
						throw new RuntimeException(e);
					}
				}
			}

		}

	}
}
