package com.constellio.app.modules.restapi.core.config;

import com.constellio.app.modules.restapi.apis.v1.ace.AceService;
import com.constellio.app.modules.restapi.apis.v1.ace.dao.AceDao;
import com.constellio.app.modules.restapi.apis.v1.cart.CartService;
import com.constellio.app.modules.restapi.apis.v1.cart.dao.CartDao;
import com.constellio.app.modules.restapi.apis.v1.category.CategoryService;
import com.constellio.app.modules.restapi.apis.v1.category.dao.CategoryDao;
import com.constellio.app.modules.restapi.apis.v1.collection.CollectionService;
import com.constellio.app.modules.restapi.apis.v1.collection.dao.CollectionDao;
import com.constellio.app.modules.restapi.apis.v1.document.DocumentService;
import com.constellio.app.modules.restapi.apis.v1.document.adaptor.DocumentAdaptor;
import com.constellio.app.modules.restapi.apis.v1.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.apis.v1.folder.FolderService;
import com.constellio.app.modules.restapi.apis.v1.folder.adaptor.FolderAdaptor;
import com.constellio.app.modules.restapi.apis.v1.folder.dao.FolderDao;
import com.constellio.app.modules.restapi.apis.v1.health.HealthService;
import com.constellio.app.modules.restapi.apis.v1.health.dao.HealthDao;
import com.constellio.app.modules.restapi.apis.v1.record.RecordService;
import com.constellio.app.modules.restapi.apis.v1.record.dao.RecordDao;
import com.constellio.app.modules.restapi.apis.v1.taxonomy.TaxonomyService;
import com.constellio.app.modules.restapi.apis.v1.taxonomy.dao.TaxonomyDao;
import com.constellio.app.modules.restapi.apis.v1.url.UrlService;
import com.constellio.app.modules.restapi.apis.v1.url.dao.UrlDao;
import com.constellio.app.modules.restapi.apis.v1.user.UserService;
import com.constellio.app.modules.restapi.apis.v1.user.dao.UserDao;
import com.constellio.app.modules.restapi.apis.v1.validation.ValidationService;
import com.constellio.app.modules.restapi.apis.v1.validation.dao.ValidationDao;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;

public class RestApiBinderV1 extends AbstractBinder {

	@Override
	protected void configure() {
		bind(ValidationService.class).to(ValidationService.class);
		bind(UrlService.class).to(UrlService.class);
		bind(DocumentService.class).to(DocumentService.class);
		bind(AceService.class).to(AceService.class);
		bind(FolderService.class).to(FolderService.class);
		bind(CollectionService.class).to(CollectionService.class);
		bind(TaxonomyService.class).to(TaxonomyService.class);
		bind(HealthService.class).to(HealthService.class).in(Singleton.class); // Health is the same for any tenant
		bind(UserService.class).to(UserService.class);
		bind(CartService.class).to(CartService.class);
		bind(RecordService.class).to(RecordService.class);
		bind(CategoryService.class).to(CategoryService.class);

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
		bind(CategoryDao.class).to(CategoryDao.class);
	}

}
