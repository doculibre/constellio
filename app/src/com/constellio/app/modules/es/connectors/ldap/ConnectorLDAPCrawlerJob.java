package com.constellio.app.modules.es.connectors.ldap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPCrawlerJobRuntimeException.ConnectorLDAPCrawlerJobRuntimeException_LDAPCloseExceptionJob;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class ConnectorLDAPCrawlerJob extends ConnectorJob {
	private static final Logger LOGGER = Logger.getLogger(ConnectorLDAPCrawlerJob.class);
	private final Set<String> documentsToCrawlIds;
	private final ConnectorLDAPServices ldapServices;
	private final ConnectorLDAPInstance connectorInstance;
	private final ConnectorLDAPDocumentType documentType;
	private final ConnectorLDAPCrawlerHelper connectorLDAPCrawlerHelper;
	private final String url;

	public ConnectorLDAPCrawlerJob(Connector connector, ConnectorLDAPInstance connectorLDAPInstance,
			ConnectorLDAPDocumentType documentType, String url, List<String> documentsToCrawlLDAPIds) {
		super(connector, "ldapFetch");
		this.documentsToCrawlIds = new HashSet(documentsToCrawlLDAPIds);
		this.ldapServices = ((ConnectorLDAP) connector).getLdapServices();
		this.connectorInstance = connectorLDAPInstance;
		this.documentType = documentType;

		this.connectorLDAPCrawlerHelper = new ConnectorLDAPCrawlerHelper(connector.getEs());
		this.url = url;
	}

	@Override
	public void execute(Connector connector) {
		try {
			LdapContext ctx = ConnectorLDAP.connectToLDAP(url, connectorInstance, ldapServices);
			Map<String, LDAPObjectAttributes> ldapDocuments = this.ldapServices
					.getObjectsAttributes(ctx, this.documentsToCrawlIds);
			List<ConnectorDocument> constellioDocuments = this.connectorLDAPCrawlerHelper
					.wrapDocuments(this.connectorInstance, ldapDocuments, this.documentType, this.url);
			this.connector.getEventObserver().push(constellioDocuments);
			closeConnection(ctx);
		} catch (LDAPConnectionFailure e) {
			LOGGER.warn(e);
		}
	}

	private void closeConnection(LdapContext ctx) {
		try {
			ctx.close();
		} catch (NamingException e) {
			LOGGER.warn("Error when closing context " + url, e);
			throw new ConnectorLDAPCrawlerJobRuntimeException_LDAPCloseExceptionJob(e);
		}
	}
}
