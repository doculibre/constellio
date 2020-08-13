package com.constellio.app.modules.restapi.core.config;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.ace.dao.AceDao;
import com.constellio.app.modules.restapi.cart.CartService;
import com.constellio.app.modules.restapi.cart.dao.CartDao;
import com.constellio.app.modules.restapi.collection.CollectionService;
import com.constellio.app.modules.restapi.collection.dao.CollectionDao;
import com.constellio.app.modules.restapi.document.DocumentService;
import com.constellio.app.modules.restapi.document.adaptor.DocumentAdaptor;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.folder.FolderService;
import com.constellio.app.modules.restapi.folder.adaptor.FolderAdaptor;
import com.constellio.app.modules.restapi.folder.dao.FolderDao;
import com.constellio.app.modules.restapi.health.HealthService;
import com.constellio.app.modules.restapi.health.dao.HealthDao;
import com.constellio.app.modules.restapi.record.RecordService;
import com.constellio.app.modules.restapi.record.dao.RecordDao;à
import com.constellio.app.modules.restapi.taxonomy.TaxonomyService;
import com.constellio.app.modules.restapi.taxonomy.dao.TaxonomyDao;
import com.constellio.app.modules.restapi.url.UrlService;
import com.constellio.app.modules.restapi.url.dao.UrlDao;
import com.constellio.app.modules.restapi.user.UserService;
import com.constellio.app.modules.restapi.user.dao.UserDao;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.app.modules.restapi.validation.dao.ValidationDao;
import org.glassfish.jersey.internal.inject.AbstractBinder;

public class RestApiBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(ValidationService.class).to(ValidationService.class);
		bind(UrlService.class).to(UrlService.class);
		bind(DocumentService.class).to(DocumentService.class);
		bind(AceService.class).to(AceService.class);
		bind(FolderService.class).to(FolderService.class);
		bind(CollectionService.class).to(CollectionService.class);
		bind(TaxonomyService.class).to(TaxonomyService.class);
		bind(HealthService.class).to(HealthService.class);
		bind(UserService.class).to(UserService.class);
		bind(CartService.class).to(CartService.class);
		bind(RecordService.class).to(RecordService.class);

		bind(DocumentAdaptor.class).to(DocumentAdaptor.class);
		bind(FolderAdaptor.class).to(FolderAdaptor.class);

		bind(DocumentDao.class).to(DocumentDao.class);
		bind(FolderDao.class).to(FolderDao.class);
		bind(ValidationDao.class).to(ValidationDao.class);
		bind(AceDao.class).to(AceDao.class);
		bind(UrlDao.class).to(UrlDao.class);
		bind(CollectionDao.class).to(CollectionDao.class);
		bind(TaxonomyDao.class).to(TaxonomyDao.class);
		bind(HealthDao.class).to(HealthDao.class);
		bind(UserDao.class).to(UserDao.class);
		bind(CartDao.class).to(CartDao.class);
		bind(RecordDao.class).to(RecordDao.class);
	}

}
