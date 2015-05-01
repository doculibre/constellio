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
package com.constellio.app.ui.framework.components.fields.taxonomy;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;

public class TaxonomyFieldPresenter implements Serializable {

	private TaxonomyField taxonomyField;

	public TaxonomyFieldPresenter(TaxonomyField taxonomyField) {
		this.taxonomyField = taxonomyField;
	}

	public void forTaxonomyAndSchemaTypeCodes(String taxonomyCode, String schemaTypeCode) {
		SessionContext sessionContext = taxonomyField.getSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		TaxonomiesSearchServices taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		UserServices userServices = modelLayerFactory.newUserServices();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);

		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions();
		List<TaxonomySearchRecord> matches = taxonomiesSearchServices.getLinkableRootConcept(currentUser, currentCollection,
				taxonomyCode, schemaTypeCode, taxonomiesSearchOptions);
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		List<RecordVO> recordVOs = new ArrayList<RecordVO>();
		for (TaxonomySearchRecord match : matches) {
			RecordVO recordVO = voBuilder.build(match.getRecord(), VIEW_MODE.TABLE);
			recordVOs.add(recordVO);
		}

		taxonomyField.setOptions(recordVOs);
	}

}
