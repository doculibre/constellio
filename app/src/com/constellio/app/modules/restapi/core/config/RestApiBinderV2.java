package com.constellio.app.modules.restapi.core.config;

import com.constellio.app.modules.restapi.apis.v1.health.HealthService;
import com.constellio.app.modules.restapi.apis.v1.health.dao.HealthDao;
import com.constellio.app.modules.restapi.apis.v2.collection.CollectionDaoV2;
import com.constellio.app.modules.restapi.apis.v2.collection.CollectionServiceV2;
import com.constellio.app.modules.restapi.apis.v2.document.DocumentAdaptorV2;
import com.constellio.app.modules.restapi.apis.v2.document.DocumentDaoV2;
import com.constellio.app.modules.restapi.apis.v2.document.DocumentServiceV2;
import com.constellio.app.modules.restapi.apis.v2.folder.FolderAdaptorV2;
import com.constellio.app.modules.restapi.apis.v2.folder.FolderDaoV2;
import com.constellio.app.modules.restapi.apis.v2.folder.FolderServiceV2;
import com.constellio.app.modules.restapi.apis.v2.record.RecordAdaptorV2;
import com.constellio.app.modules.restapi.apis.v2.record.RecordDaoV2;
import com.constellio.app.modules.restapi.apis.v2.record.RecordServiceV2;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;

public class RestApiBinderV2 extends AbstractBinder {

	@Override
	protected void configure() {
		bind(CollectionServiceV2.class).to(CollectionServiceV2.class);
		bind(DocumentServiceV2.class).to(DocumentServiceV2.class);
		bind(FolderServiceV2.class).to(FolderServiceV2.class);
		bind(HealthService.class).to(HealthService.class).in(Singleton.class); // Health is the same for any tenant
		bind(RecordServiceV2.class).to(RecordServiceV2.class);

		bind(DocumentAdaptorV2.class).to(DocumentAdaptorV2.class);
		bind(FolderAdaptorV2.class).to(FolderAdaptorV2.class);
		bind(RecordAdaptorV2.class).to(RecordAdaptorV2.class);

		bind(CollectionDaoV2.class).to(CollectionDaoV2.class);
		bind(DocumentDaoV2.class).to(DocumentDaoV2.class);
		bind(FolderDaoV2.class).to(FolderDaoV2.class);
		bind(RecordDaoV2.class).to(RecordDaoV2.class);
		bind(HealthDao.class).to(HealthDao.class);
	}

}
