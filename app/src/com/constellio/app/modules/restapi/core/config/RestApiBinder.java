package com.constellio.app.modules.restapi.core.config;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.ace.dao.AceDao;
import com.constellio.app.modules.restapi.document.DocumentService;
import com.constellio.app.modules.restapi.document.adaptor.DocumentAdaptor;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.folder.FolderService;
import com.constellio.app.modules.restapi.folder.adaptor.FolderAdaptor;
import com.constellio.app.modules.restapi.folder.dao.FolderDao;
import com.constellio.app.modules.restapi.health.HealthService;
import com.constellio.app.modules.restapi.health.dao.HealthDao;
import com.constellio.app.modules.restapi.signature.SignatureService;
import com.constellio.app.modules.restapi.url.UrlService;
import com.constellio.app.modules.restapi.url.dao.UrlDao;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.app.modules.restapi.validation.dao.ValidationDao;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;

public class RestApiBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(SignatureService.class).to(SignatureService.class).in(Singleton.class);
		bind(ValidationService.class).to(ValidationService.class).in(Singleton.class);
		bind(UrlService.class).to(UrlService.class).in(Singleton.class);
		bind(DocumentService.class).to(DocumentService.class).in(Singleton.class);
		bind(AceService.class).to(AceService.class).in(Singleton.class);
		bind(FolderService.class).to(FolderService.class).in(Singleton.class);
		bind(HealthService.class).to(HealthService.class).in(Singleton.class);

		bind(DocumentAdaptor.class).to(DocumentAdaptor.class).in(Singleton.class);
		bind(FolderAdaptor.class).to(FolderAdaptor.class).in(Singleton.class);

		bind(DocumentDao.class).to(DocumentDao.class).in(Singleton.class);
		bind(FolderDao.class).to(FolderDao.class).in(Singleton.class);
		bind(ValidationDao.class).to(ValidationDao.class).in(Singleton.class);
		bind(AceDao.class).to(AceDao.class).in(Singleton.class);
		bind(UrlDao.class).to(UrlDao.class).in(Singleton.class);
		bind(HealthDao.class).to(HealthDao.class).in(Singleton.class);
	}

}
