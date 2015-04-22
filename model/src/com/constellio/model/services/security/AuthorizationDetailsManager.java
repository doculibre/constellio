/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.joda.time.LocalDate;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.security.AuthorizationDetailsManagerRuntimeException.AuthorizationDetailsAlreadyExists;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;

public class AuthorizationDetailsManager
		implements StatefulService, OneXMLConfigPerCollectionManagerListener<Map<String, AuthorizationDetails>> {

	static String AUTHORIZATIONS_CONFIG = "/authorizations.xml";
	private OneXMLConfigPerCollectionManager<Map<String, AuthorizationDetails>> oneXMLConfigPerCollectionManager;
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

	protected OneXMLConfigPerCollectionManager<Map<String, AuthorizationDetails>> newOneXMLConfigPerCollectionManager() {
		return new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager, AUTHORIZATIONS_CONFIG,
				xmlConfigReader(), this);
	}

	@Override
	public void close() {

	}

	Map<String, AuthorizationDetails> getAuthorizationsDetails(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection);
	}

	public void createCollectionAuthorizationDetail(String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				AuthorizationDetailsWriter writer = newAuthorizationWriter(document);
				writer.createEmptyAuthorizations();
			}
		};
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	public void add(AuthorizationDetails authorizationDetails) {
		if (getAuthorizationsDetails(authorizationDetails.getCollection()).containsKey(authorizationDetails.getId())) {
			throw new AuthorizationDetailsAlreadyExists(authorizationDetails.getId());
		}
		validateDates(authorizationDetails.getStartDate(), authorizationDetails.getEndDate());
		String collection = authorizationDetails.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newAddAuthorizationDocumentAlteration(authorizationDetails));
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

	public void remove(AuthorizationDetails authorizationDetails) {
		String collection = authorizationDetails.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newRemoveAuthorizationDocumentAlteration(authorizationDetails));
	}

	public void modifyEndDate(AuthorizationDetails authorizationDetails, LocalDate endate) {
		String collection = authorizationDetails.getCollection();
		authorizationDetails = authorizationDetails.withNewEndDate(endate);
		oneXMLConfigPerCollectionManager
				.updateXML(collection, newModifyEndDateAuthorizationDocumentAlteration(authorizationDetails));
	}

	public AuthorizationDetails get(String collection, String id) {
		return getAuthorizationsDetails(collection).get(id);
	}

	public boolean isEnabled(AuthorizationDetails authorizationDetails) {
		LocalDate currentDate = TimeProvider.getLocalDate();
		LocalDate startDate = authorizationDetails.getStartDate();
		LocalDate endDate = authorizationDetails.getEndDate();
		if (authorizationDetails.getStartDate() == null || authorizationDetails.getEndDate() == null) {
			return true;
		} else {
			return !startDate.isAfter(currentDate) && !endDate.isBefore(currentDate);
		}
	}

	public boolean isDisabled(AuthorizationDetails authorizationDetails) {
		return !isEnabled(authorizationDetails);
	}

	public List<String> getListOfFinishedAuthorizationsIds(String collection) {
		List<String> finishedAuthorizations = new ArrayList<>();
		for (AuthorizationDetails authorizationDetails : getAuthorizationsDetails(collection).values()) {
			if (authorizationDetails.getEndDate() != null && authorizationDetails.getEndDate()
					.isBefore(TimeProvider.getLocalDate())) {
				finishedAuthorizations.add(authorizationDetails.getId());
			}
		}
		return finishedAuthorizations;
	}

	AuthorizationDetailsWriter newAuthorizationsWriter(Document document) {
		return new AuthorizationDetailsWriter(document);
	}

	DocumentAlteration newAddAuthorizationDocumentAlteration(final AuthorizationDetails authorizationDetails) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newAuthorizationsWriter(document).add(authorizationDetails);
			}
		};
	}

	DocumentAlteration newRemoveAuthorizationDocumentAlteration(final AuthorizationDetails authorizationDetails) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newAuthorizationsWriter(document).remove(authorizationDetails.getId());
			}
		};
	}

	DocumentAlteration newModifyEndDateAuthorizationDocumentAlteration(final AuthorizationDetails authorizationDetails) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newAuthorizationsWriter(document).modifyEndDate(authorizationDetails.getId(), authorizationDetails.getEndDate());
			}
		};
	}

	AuthorizationDetailsWriter newAuthorizationWriter(Document document) {
		return new AuthorizationDetailsWriter(document);
	}

	AuthorizationDetailsReader newAuthorizationReader(Document document) {
		return new AuthorizationDetailsReader(document);
	}

	private XMLConfigReader<Map<String, AuthorizationDetails>> xmlConfigReader() {
		return new XMLConfigReader<Map<String, AuthorizationDetails>>() {
			@Override
			public Map<String, AuthorizationDetails> read(String collection, Document document) {
				return newAuthorizationReader(document).readAll();
			}
		};
	}

	@Override
	public void onValueModified(String collection, Map<String, AuthorizationDetails> newValue) {

	}
}
