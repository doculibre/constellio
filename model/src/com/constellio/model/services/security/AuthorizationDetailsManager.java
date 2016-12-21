package com.constellio.model.services.security;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.security.AuthorizationDetailsManagerRuntimeException.AuthorizationDetailsAlreadyExists;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthorizationDetailsManager
		implements StatefulService, OneXMLConfigPerCollectionManagerListener<Map<String, XMLAuthorizationDetails>> {

	static String AUTHORIZATIONS_CONFIG = "/authorizations.xml";
	private OneXMLConfigPerCollectionManager<Map<String, XMLAuthorizationDetails>> oneXMLConfigPerCollectionManager;
	private ConfigManager configManager;
	private CollectionsListManager collectionsListManager;

	public AuthorizationDetailsManager(ConfigManager configManager, CollectionsListManager collectionsListManager) {
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
	}

	@Override
	public void initialize() {
		this.oneXMLConfigPerCollectionManager = newOneXMLConfigPerCollectionManager();
	}

	protected OneXMLConfigPerCollectionManager<Map<String, XMLAuthorizationDetails>> newOneXMLConfigPerCollectionManager() {
		return new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager, AUTHORIZATIONS_CONFIG,
				xmlConfigReader(), this, newCreateEmptyDocumentDocumentAlteration());
	}

	@Override
	public void close() {

	}

	public Map<String, XMLAuthorizationDetails> getAuthorizationsDetails(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection);
	}

	public void createCollectionAuthorizationDetail(String collection) {

		oneXMLConfigPerCollectionManager.createCollectionFile(collection, newCreateEmptyDocumentDocumentAlteration());
	}

	public void add(XMLAuthorizationDetails xmlAuthorizationDetails) {
		if (getAuthorizationsDetails(xmlAuthorizationDetails.getCollection()).containsKey(xmlAuthorizationDetails.getId())) {
			throw new AuthorizationDetailsAlreadyExists(xmlAuthorizationDetails.getId());
		}
		validateDates(xmlAuthorizationDetails.getStartDate(), xmlAuthorizationDetails.getEndDate());
		String collection = xmlAuthorizationDetails.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newAddAuthorizationDocumentAlteration(xmlAuthorizationDetails));
	}

	void validateDates(LocalDate startDate, LocalDate endDate) {
		LocalDate now = TimeProvider.getLocalDate();
		if ((startDate != null && endDate != null) && startDate.isAfter(endDate)) {
			throw new AuthorizationDetailsManagerRuntimeException.StartDateGreaterThanEndDate(startDate, endDate);
		}
		if (endDate != null && !endDate.isAfter(now)) {
			throw new AuthorizationDetailsManagerRuntimeException.EndDateLessThanCurrentDate(endDate.toString());
		}
	}

	public void remove(XMLAuthorizationDetails xmlAuthorizationDetails) {
		String collection = xmlAuthorizationDetails.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newRemoveAuthorizationDocumentAlteration(xmlAuthorizationDetails));
	}

	public void modifyEndDate(XMLAuthorizationDetails xmlAuthorizationDetails, LocalDate endate) {
		String collection = xmlAuthorizationDetails.getCollection();
		xmlAuthorizationDetails = xmlAuthorizationDetails.withNewEndDate(endate);
		oneXMLConfigPerCollectionManager
				.updateXML(collection, newModifyEndDateAuthorizationDocumentAlteration(xmlAuthorizationDetails));
	}

	public XMLAuthorizationDetails get(String collection, String id) {
		return getAuthorizationsDetails(collection).get(id);
	}

	public boolean isEnabled(XMLAuthorizationDetails xmlAuthorizationDetails) {
		LocalDate currentDate = TimeProvider.getLocalDate();
		LocalDate startDate = xmlAuthorizationDetails.getStartDate();
		LocalDate endDate = xmlAuthorizationDetails.getEndDate();
		if (xmlAuthorizationDetails.getStartDate() == null || xmlAuthorizationDetails.getEndDate() == null) {
			return true;
		} else {
			return !startDate.isAfter(currentDate) && !endDate.isBefore(currentDate);
		}
	}

	public boolean isDisabled(XMLAuthorizationDetails xmlAuthorizationDetails) {
		return !isEnabled(xmlAuthorizationDetails);
	}

	public List<String> getListOfFinishedAuthorizationsIds(String collection) {
		List<String> finishedAuthorizations = new ArrayList<>();
		for (XMLAuthorizationDetails xmlAuthorizationDetails : getAuthorizationsDetails(collection).values()) {
			if (xmlAuthorizationDetails.getEndDate() != null && xmlAuthorizationDetails.getEndDate()
					.isBefore(TimeProvider.getLocalDate())) {
				finishedAuthorizations.add(xmlAuthorizationDetails.getId());
			}
		}
		return finishedAuthorizations;
	}

	AuthorizationDetailsWriter newAuthorizationsWriter(Document document) {
		return new AuthorizationDetailsWriter(document);
	}

	DocumentAlteration newAddAuthorizationDocumentAlteration(final XMLAuthorizationDetails xmlAuthorizationDetails) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newAuthorizationsWriter(document).add(xmlAuthorizationDetails);
			}
		};
	}

	DocumentAlteration newRemoveAuthorizationDocumentAlteration(final XMLAuthorizationDetails xmlAuthorizationDetails) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newAuthorizationsWriter(document).remove(xmlAuthorizationDetails.getId());
			}
		};
	}

	DocumentAlteration newModifyEndDateAuthorizationDocumentAlteration(final XMLAuthorizationDetails xmlAuthorizationDetails) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newAuthorizationsWriter(document).modifyEndDate(xmlAuthorizationDetails.getId(), xmlAuthorizationDetails.getEndDate());
			}
		};
	}

	AuthorizationDetailsWriter newAuthorizationWriter(Document document) {
		return new AuthorizationDetailsWriter(document);
	}

	AuthorizationDetailsReader newAuthorizationReader(Document document) {
		return new AuthorizationDetailsReader(document);
	}

	private DocumentAlteration newCreateEmptyDocumentDocumentAlteration() {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				AuthorizationDetailsWriter writer = newAuthorizationWriter(document);
				writer.createEmptyAuthorizations();
			}
		};
	}

	private XMLConfigReader<Map<String, XMLAuthorizationDetails>> xmlConfigReader() {
		return new XMLConfigReader<Map<String, XMLAuthorizationDetails>>() {
			@Override
			public Map<String, XMLAuthorizationDetails> read(String collection, Document document) {
				return newAuthorizationReader(document).readAll();
			}
		};
	}

	@Override
	public void onValueModified(String collection, Map<String, XMLAuthorizationDetails> newValue) {

	}

	public XMLAuthorizationDetails getByIdWithoutPrefix(String collection, String idWithoutPrefix) {
		Map<String, XMLAuthorizationDetails> collectionDetails = getAuthorizationsDetails(collection);
		for (String currentId : collectionDetails.keySet()) {
			if (currentId.endsWith("_" + idWithoutPrefix)) {
				return collectionDetails.get(currentId);
			}
		}
		return null;
	}

	public String fileFor(String collection) {
		return oneXMLConfigPerCollectionManager.getConfigPath(collection);
	}
}
