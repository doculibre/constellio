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
package com.constellio.app.ui.framework.data;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.LookupTreeDataProvider;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;

public class RecordLookupTreeDataProvider implements LookupTreeDataProvider<String> {
	private String taxonomyCode;
	private String schemaTypeCode;
	private int rootObjectsCount = -1;
	private Map<String, Integer> childrenCounts = new HashMap<>();
	private Map<String, String> parentCache = new HashMap<>();
	private Map<String, Boolean> selectableCache = new HashMap<>();
	private boolean ignoreLinkability;

	public RecordLookupTreeDataProvider(String schemaTypeCode, String taxonomyCode) {
		this.schemaTypeCode = schemaTypeCode;
		this.taxonomyCode = taxonomyCode;
		ignoreLinkability = false;
	}

	@Override
	public final String getTaxonomyCode() {
		return taxonomyCode;
	}

	@Override
	public int getRootObjectsCount() {
		if (rootObjectsCount == -1) {
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			String currentCollection = sessionContext.getCurrentCollection();
			UserVO currentUserVO = sessionContext.getCurrentUser();

			ConstellioFactories constellioFactories = getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			TaxonomiesSearchServices taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
			UserServices userServices = modelLayerFactory.newUserServices();

			User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);

			LinkableTaxonomySearchResponse response = taxonomiesSearchServices.getLinkableRootConceptResponse(currentUser,
					currentCollection, taxonomyCode, schemaTypeCode, new TaxonomiesSearchOptions());
			rootObjectsCount = new Long(response.getNumFound()).intValue();
		}
		return rootObjectsCount;
	}

	@Override
	public List<String> getRootObjects(int start, int maxSize) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		TaxonomiesSearchServices taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		UserServices userServices = modelLayerFactory.newUserServices();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);

		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions(maxSize, start, null);
		List<TaxonomySearchRecord> matches = taxonomiesSearchServices.getLinkableRootConcept(currentUser, currentCollection,
				taxonomyCode, schemaTypeCode, taxonomiesSearchOptions);
		List<String> recordIds = new ArrayList<String>();
		for (TaxonomySearchRecord match : matches) {
			String recordId = match.getRecord().getId();
			boolean selectable = ignoreLinkability || match.isLinkable();
			recordIds.add(recordId);
			selectableCache.put(recordId, selectable);
		}
		return recordIds;
	}

	@Override
	public String getParent(String child) {
		return parentCache.get(child);
	}

	@Override
	public int getChildrenCount(String parent) {
		Integer childrenCount = childrenCounts.get(parent);
		if (childrenCounts.get(parent) == null) {
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			String currentCollection = sessionContext.getCurrentCollection();
			UserVO currentUserVO = sessionContext.getCurrentUser();

			ConstellioFactories constellioFactories = getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			TaxonomiesSearchServices taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
			UserServices userServices = modelLayerFactory.newUserServices();
			RecordServices recordServices = modelLayerFactory.newRecordServices();

			User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
			Record record = recordServices.getDocumentById(parent);

			LinkableTaxonomySearchResponse response = taxonomiesSearchServices.getLinkableChildConceptResponse(currentUser,
					record, taxonomyCode, schemaTypeCode, new TaxonomiesSearchOptions());
			childrenCount = new Long(response.getNumFound()).intValue();
			childrenCounts.put(parent, childrenCount);
		}
		return childrenCount;
	}

	@Override
	public List<String> getChildren(String parent, int start, int maxSize) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		TaxonomiesSearchServices taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		UserServices userServices = modelLayerFactory.newUserServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
		Record record = recordServices.getDocumentById(parent);

		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions(maxSize, start, null);
		List<TaxonomySearchRecord> matches = taxonomiesSearchServices
				.getLinkableChildConcept(currentUser, record, taxonomyCode, schemaTypeCode,
						taxonomiesSearchOptions);

		List<String> recordIds = new ArrayList<String>();
		for (TaxonomySearchRecord match : matches) {
			String recordId = match.getRecord().getId();
			boolean selectable = match.isLinkable();
			recordIds.add(recordId);
			parentCache.put(recordId, parent);
			selectableCache.put(recordId, selectable);
		}
		return recordIds;
	}

	@Override
	public boolean hasChildren(String parent) {
		return getChildrenCount(parent) > 0;
	}

	@Override
	public boolean isLeaf(String object) {
		return !hasChildren(object);
	}

	@Override
	public boolean isSelectable(String selection) {
		return selectableCache.get(selection);
	}

	@Override
	public TextInputDataProvider<String> search() {
		return new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode);
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
	}
}
