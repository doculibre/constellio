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
