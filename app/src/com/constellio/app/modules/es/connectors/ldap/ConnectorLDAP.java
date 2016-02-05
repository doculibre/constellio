package com.constellio.app.modules.es.connectors.ldap;

import static com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPDocumentType.USER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.app.modules.es.connectors.ConnectorDeleterJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.DefaultAbstractConnector;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ConnectorLDAP extends DefaultAbstractConnector {
	private static final Logger LOGGER = LogManager.getLogger(ConnectorLDAP.class);
	private List<String> allObjectsToFetch;
	private List<String> allObjectsToRemoveConstellioIds;
	final private ConnectorLDAPServices ldapServices;
	private int maxJobPerBatch;
	private ConnectorLDAPInstance ldapInstance;
	private String url;
	private int documentsPerJob;

	public ConnectorLDAP(ConnectorLDAPServices ldapServices) {
		this.ldapServices = ldapServices;
	}

	public ConnectorLDAP() {
		this.ldapServices = new ConnectorLDAPServicesImpl();
	}

	@Override
	public List<ConnectorJob> getJobs() {
		if (isFetchStarting()) {
			initFetch();
		} else if (isFetchEnded()) {
			setFetchAsStarting();
			return new ArrayList<>();
		}
		List<ConnectorJob> nextJobs = nextJobs();
		if (nextJobs.isEmpty()) {
			setFetchAsStarting();
		}
		return nextJobs;
	}

	private List<ConnectorJob> nextJobs() {
		List<ConnectorJob> returnJobs = new ArrayList<>();
		int i = 0;
		boolean moreJobs = true;
		while (i < maxJobPerBatch && moreJobs) {
			ConnectorJob addJob = createAddJob();
			if (addJob != null) {
				i++;
				returnJobs.add(addJob);
			} else {
				moreJobs = false;
			}
		}
		moreJobs = true;
		while (i < maxJobPerBatch && moreJobs) {
			ConnectorJob addJob = createRemoveJob();
			if (addJob != null) {
				i++;
				returnJobs.add(addJob);
			} else {
				moreJobs = false;
			}
		}
		return returnJobs;
	}

	private void setFetchAsStarting() {
		this.allObjectsToFetch = null;
	}

	private boolean isFetchEnded() {
		if (this.allObjectsToFetch.isEmpty()
				&& this.allObjectsToRemoveConstellioIds.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	void initFetch() {
		this.allObjectsToFetch = new ArrayList<>();
		maxJobPerBatch = ldapInstance.getNumberOfJobsInParallel();
		documentsPerJob = ldapInstance.getDocumentsPerJobs();
		if (documentsPerJob == 0) {
			throw new InvalidDocumentsBatchRuntimeException("At least one document per job");
		}
		if (maxJobPerBatch == 0) {
			throw new InvalidJobsBatchRuntimeException("At least one job per batch");
		}
		if (!isFetchOnlyForUsers(this.ldapInstance)) {
			throw new RuntimeException("The current version support fetch only for LDAP users");
		}
		ConnectorLDAPDocumentType documentType = USER;
		this.url = getUrl();
		LdapContext ctx = null;
		try {
			ctx = connectToLDAP(url, ldapInstance, ldapServices);
			Set<String> contextList = new HashSet<>();
			contextList.addAll(ldapInstance.getUsersBaseContextList());
			RegexFilter filter = new RegexFilter(ldapInstance.getIncludeRegex(), ldapInstance.getExcludeRegex());
			ConnectorLDAPSearchResult searchResult = this.ldapServices
					.getAllObjectsUsingFilter(ctx, documentType.getObjectClass(), documentType.getObjectCategory(), contextList,
							filter);
			allObjectsToFetch.addAll(searchResult.getDocumentIds());
			if (searchResult.isErrorDuringSearch()) {
				LOGGER.warn("No document fetched from ldap server, no document will be removed from constellio");
			} else {
				allObjectsToRemoveConstellioIds = getObjectsToRemoveConstellioIds(allObjectsToFetch, ldapInstance);
			}
			ctx.close();
		} catch (Exception e) {
			LOGGER.error(e);
			if (ctx != null) {
				try {
					ctx.close();
				} catch (Exception e1) {
					LOGGER.warn("Error when closing context ", e);
				}
			}
			this.allObjectsToFetch = new ArrayList<>();
			this.allObjectsToRemoveConstellioIds = new ArrayList<>();
		}
	}

	private boolean isFetchStarting() {
		return this.allObjectsToFetch == null
				|| this.allObjectsToRemoveConstellioIds == null;
	}

	private ConnectorJob createRemoveJob() {
		if (this.allObjectsToRemoveConstellioIds.isEmpty()) {
			return null;
		}
		List<String> documentToDeleteConstellioIds = this.allObjectsToRemoveConstellioIds
				.subList(0, Math.min(this.documentsPerJob, this.allObjectsToRemoveConstellioIds.size()));
		if (this.documentsPerJob < this.allObjectsToRemoveConstellioIds.size()) {
			this.allObjectsToRemoveConstellioIds = this.allObjectsToRemoveConstellioIds.subList(this.documentsPerJob,
					this.allObjectsToRemoveConstellioIds.size());
		} else {
			this.allObjectsToRemoveConstellioIds = new ArrayList<>();
		}
		return new ConnectorDeleterJob(this, documentToDeleteConstellioIds);
	}

	private ConnectorJob createAddJob() {
		if (this.allObjectsToFetch.isEmpty()) {
			return null;
		}
		List<String> documentsToCrawlLDAPIds = this.allObjectsToFetch
				.subList(0, Math.min(this.documentsPerJob, this.allObjectsToFetch.size()));
		if (this.documentsPerJob < this.allObjectsToFetch.size()) {
			this.allObjectsToFetch = this.allObjectsToFetch.subList(this.documentsPerJob, this.allObjectsToFetch.size());
		} else {
			this.allObjectsToFetch = new ArrayList<>();
		}
		return new ConnectorLDAPCrawlerJob(this, ldapInstance, USER, url, documentsToCrawlLDAPIds);
	}

	@Override
	protected void initialize(Record instance) {
		this.ldapInstance = getEs().wrapConnectorLDAPInstance(instance);
	}

	List<String> getObjectsToRemoveConstellioIds(List<String> allObjectsToFetch, ConnectorLDAPInstance connectorInstance) {
		List<String> returnSet = new ArrayList<>();
		List<ConnectorDocument<?>> allConnectorInstanceDocument = es
				.searchConnectorDocuments(
						new LogicalSearchQuery(
								where(es.connectorLdapUserDocument.connector()).isEqualTo(connectorInstance)));
		for (ConnectorDocument document : allConnectorInstanceDocument) {
			String dn = ((ConnectorLDAPUserDocument) document).getDistinguishedName();
			if (!allObjectsToFetch.contains(dn)) {
				returnSet.add(document.getId());
			}
		}
		return returnSet;
	}

	static LdapContext connectToLDAP(String url, ConnectorLDAPInstance connectorInstance, ConnectorLDAPServices ldapServices) {
		String user = connectorInstance.getConnectionUsername();
		String password = connectorInstance.getPassword();
		boolean followReferences = (connectorInstance.getFollowReferences() == null) ?
				false :
				connectorInstance.getFollowReferences();
		boolean activeDirectory = (connectorInstance.getDirectoryType() == null) ?
				true :
				connectorInstance.getDirectoryType().equals(
						LDAPDirectoryType.ACTIVE_DIRECTORY);
		return ldapServices.connectToLDAP(url, user, password, followReferences, activeDirectory);
	}

	boolean isFetchOnlyForUsers(ConnectorLDAPInstance ldapInstance) {
		if ((ldapInstance.getFetchGroups() != null && ldapInstance.getFetchGroups())
				||
				(ldapInstance.getFetchComputers() != null && ldapInstance.getFetchComputers())) {
			return false;
		}
		return ldapInstance.getFetchUsers();
	}

	private String getUrl() {
		List<String> urls = ldapInstance.getUrls();
		if (urls.size() != 1) {
			throw new RuntimeException(
					"Unsupported in the current version. Connector instance should have only one url, given urls are "
							+ StringUtils.join(urls, ","));
		}
		//should have at least one value since metadata is a default requirement
		return urls.get(0);
	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorLDAPUserDocument.SCHEMA_TYPE);
	}

	@Override
	public void start() {
		LOGGER.info("started");
	}

	@Override
	public void stop() {
		LOGGER.info("stopped");
	}

	@Override
	public void afterJobs(List<ConnectorJob> jobs) {

	}

	@Override
	public void resume() {
		LOGGER.info("resumed");
	}

	@Override
	public void onAllDocumentsDeleted() {

	}

	public ConnectorLDAPServices getLdapServices() {
		return ldapServices;
	}

	public static class InvalidDocumentsBatchRuntimeException extends RuntimeException {
		public InvalidDocumentsBatchRuntimeException(String s) {
			super(s);
		}
	}

	public static class InvalidJobsBatchRuntimeException extends RuntimeException {
		public InvalidJobsBatchRuntimeException(String s) {
			super(s);
		}
	}
}
