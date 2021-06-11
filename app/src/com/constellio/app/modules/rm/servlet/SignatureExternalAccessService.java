package com.constellio.app.modules.rm.servlet;

import com.constellio.app.api.pdf.signature.config.ESignatureConfigs;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.servlet.SignatureExternalAccessServiceException.SignatureExternalAccessServiceException_EmailServerNotConfigured;
import com.constellio.app.modules.rm.servlet.SignatureExternalAccessServiceException.SignatureExternalAccessServiceException_NoSignCertificate;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.servlet.BaseServletDao;
import com.constellio.app.servlet.BaseServletService;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.commons.lang.StringUtils;

public class SignatureExternalAccessService extends BaseServletService {

	private SignatureExternalAccessDao dao;

	@Override
	protected BaseServletDao getDao() {
		return dao;
	}

	public SignatureExternalAccessService() {
		dao = new SignatureExternalAccessDao();
	}

	public String accessExternalSignature(String token, String accessId, String language, String ipAddress) {
		Record accessRecord = getRecord(accessId, false);
		return dao.accessExternalSignature(accessRecord, token, language, ipAddress);
	}

	public void sendSignatureRequest(String token, String serviceKey, String documentId, String internalUserId,
									 String externalUserFullname, String externalUserEmail, String expirationDate,
									 String language) {
		validateToken(token, serviceKey);

		Record record = getRecord(documentId, true);

		User user;
		try {
			user = getUserByServiceKey(serviceKey, record.getCollection());
		} catch (Exception e) {
			throw new UnauthenticatedUserException();
		}

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		EmailConfigurationsManager emailConfigurationsManager = appLayerFactory.getModelLayerFactory().getEmailConfigurationsManager();
		EmailServerConfiguration emailConfiguration = emailConfigurationsManager.getEmailConfiguration(record.getCollection(), false);

		if (emailConfiguration == null || !emailConfiguration.isEnabled()) {
			throw new SignatureExternalAccessServiceException_EmailServerNotConfigured();
		}

		if (appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(ESignatureConfigs.SIGNING_KEYSTORE) == null) {
			throw new SignatureExternalAccessServiceException_NoSignCertificate();
		}

		if (!user.hasWriteAccess().on(record) ||
			!user.has(RMPermissionsTo.SEND_SIGNATURE_REQUEST).globally()) {
			throw new UnauthorizedAccessException();
		}

		Record internalUserRecord = null;
		if (StringUtils.isNotBlank(internalUserId)) {
			internalUserRecord = getRecord(internalUserId, true);
		}

		dao.sendSignatureRequest(user, record, internalUserRecord, externalUserFullname, externalUserEmail,
				expirationDate, language);
	}
}
