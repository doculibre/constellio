package com.constellio.app.modules.es.connectors.ldap;

import static com.constellio.app.modules.es.connectors.ldap.ConnectorLDAPDocumentType.GROUP;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.structures.MapStringListStringStructure;

public class ConnectorLDAPCrawlerHelper {
	private static final Logger LOGGER = LogManager.getLogger(ConnectorLDAPCrawlerHelper.class);
	final ESSchemasRecordsServices esSchemas;

	public ConnectorLDAPCrawlerHelper(ESSchemasRecordsServices esSchemas) {
		this.esSchemas = esSchemas;
	}

	List<ConnectorDocument> wrapDocuments(ConnectorLDAPInstance connectorInstance, Map<String, LDAPObjectAttributes> ldapObjects,
			ConnectorLDAPDocumentType documentType, String url) {
		List<ConnectorDocument> returnList = new ArrayList<>();
		for (Entry<String, LDAPObjectAttributes> ldapObject : ldapObjects.entrySet()) {
			try {
				returnList.add(wrapDocument(connectorInstance, ldapObject, documentType, url));
			} catch (Exception e) {
				LOGGER.error("document escaped " + ldapObject.getKey(), e);
			}
		}
		return returnList;
	}

	ConnectorDocument wrapDocument(ConnectorLDAPInstance connectorInstance,
			Entry<String, LDAPObjectAttributes> ldapObject,
			ConnectorLDAPDocumentType documentType, String url) {
		ConnectorDocument document = getOrCreateDocumentByDNAndUrl(ldapObject.getKey(), url, connectorInstance).setURL(url);
		//Done by ConnectorEventObserver :
		// document = populateMappedMetadas(document, connectorInstance, ldapObject.getValue());
		switch (documentType) {
		case USER:
			ConnectorLDAPUserDocument returnDocument = (ConnectorLDAPUserDocument) ConnectorLDAPUserDocumentFactory
					.populateUser((ConnectorLDAPUserDocument) document, ldapObject.getValue(), connectorInstance)
					.setSearchable(true).setFetched(true).setManualTokens(Record.PUBLIC_TOKEN);
			if (returnDocument.getDistinguishedName().equals(ldapObject.getKey())) {
				return returnDocument.setTitle(returnDocument.getUsername());
			} else {
				throw new ConnectorLDAPCrawlerHelper_InvalidLDAPUserDN(ldapObject.getKey(),
						returnDocument.getDistinguishedName());
			}

		default:
			throw new RuntimeException("Unsupported type " + GROUP);
		}
	}

	ConnectorDocument getOrCreateDocumentByDNAndUrl(String dn, String url, ConnectorLDAPInstance connectorInstance) {
		List<ConnectorLDAPUserDocument> singleResult = esSchemas
				.searchConnectorLDAPUserDocuments(where(esSchemas.connectorLdapUserDocument.distinguishedName()).is(dn)
						.andWhere(esSchemas.connectorDocument.url()).is(url)
						.andWhere(esSchemas.connectorDocument.connectorType()).is(connectorInstance.getConnectorType())
						.andWhere(esSchemas.connectorDocument.connector()).is(connectorInstance.getId()));
		if (singleResult.size() > 1) {
			throw new RuntimeException(
					"Distinguished name " + dn + " is not unique in connector instance " + connectorInstance.getId());
		} else {
			if (singleResult.isEmpty()) {
				ConnectorLDAPUserDocument document = esSchemas.newConnectorLDAPUserDocument(connectorInstance);
				return document.setDistinguishedName(dn);
			} else {
				return singleResult.get(0);
			}
		}
	}

	private ConnectorDocument populateMappedMetadas(ConnectorDocument document, ConnectorLDAPInstance connectorInstance,
			LDAPObjectAttributes ldapObject) {
		MapStringListStringStructure propertiesMapping = connectorInstance
				.getPropertiesMapping();
		for (Entry<String, List<String>> entry : propertiesMapping.entrySet()) {
			String metadataName = entry.getKey();
			Metadata metadata = esSchemas.getTypes().getMetadata(metadataName);
			List<String> attributesNames = entry.getValue();
			if (metadata.isMultivalue()) {
				document.set(metadata.getLocalCode(), getMultiValues(attributesNames, ldapObject, metadata));
			} else {
				document.set(metadata.getLocalCode(), getSingleValue(attributesNames, ldapObject, metadata));
			}

		}
		return document;
	}

	private Object getSingleValue(List<String> attributesNames, LDAPObjectAttributes ldapObject, Metadata metadata) {
		if (attributesNames.size() > 1) {
			throw new RuntimeException(
					"Several values to populate a single value metadata " + StringUtils.join(attributesNames, "; ") + " :"
							+ metadata.getCode());
		}
		MetadataValueType metadataType = metadata.getType();
		switch (metadataType) {
		case TEXT:
		case STRING:
			return ldapObject.get(attributesNames.get(0)).getStringValue();
		default:
			throw new RuntimeException("Unsupported " + metadataType);
		}
	}

	private List<Object> getMultiValues(List<String> attributesNames, LDAPObjectAttributes ldapObject, Metadata metadata) {
		List<Object> returnList = new ArrayList<>();
		for (String attributeName : attributesNames) {
			List<Object> value = ldapObject.get(attributeName).getValue();
			if (value != null) {
				returnList.addAll(value);
			}
		}
		return returnList;
	}

	private class ConnectorLDAPCrawlerHelper_InvalidLDAPUserDN extends RuntimeException {
		public ConnectorLDAPCrawlerHelper_InvalidLDAPUserDN(
				String expectedValue, String actualValue) {
			super("Expected value is " + expectedValue + " actual value " + actualValue);
		}
	}
}
