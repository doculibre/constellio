package com.constellio.app.modules.restapi.certification;

import com.constellio.app.modules.restapi.certification.adaptor.CertificationAdaptor;
import com.constellio.app.modules.restapi.certification.dao.CertificationDao;
import com.constellio.app.modules.restapi.certification.dto.CertificationDto;
import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.resource.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.resource.service.ResourceService;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.pdf.PdfAnnotation;

import javax.inject.Inject;

public class CertificationService extends ResourceService {

	@Inject
	private CertificationDao certificationDao;

	@Inject
	private ValidationService validationService;

	@Override
	protected BaseDao getDao() {
		return certificationDao;
	}

	public CertificationDto create(String host, String documentId, String serviceKey, String method, String date,
								   int expiration, String signature, CertificationDto certification,
								   String flushMode, boolean urlValidated) throws Exception {
		validateParameters(host, documentId, serviceKey, method, date, expiration, null, null, null, signature, urlValidated);

		Record document = getRecord(documentId, true);
		String collection = document.getCollection();
		User user = getUserByServiceKey(serviceKey, collection);

		validateUserAccess(user, document, method);

		return createCertification(user, certification, flushMode, document);
	}

	private CertificationDto createCertification(User user, CertificationDto certification, String flushMode,
												 Record document) throws Exception {
		try {

			PdfAnnotation pdfSignatureAnnotation = certificationDao.createCertification(user, certification, flushMode, document);

			return new CertificationAdaptor().adapt(pdfSignatureAnnotation, document.getId());

		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	protected SchemaTypes getSchemaType() {
		return null;
	}

	@Override
	protected ResourceAdaptor<CertificationDto> getAdaptor() {
		return null;
	}

}