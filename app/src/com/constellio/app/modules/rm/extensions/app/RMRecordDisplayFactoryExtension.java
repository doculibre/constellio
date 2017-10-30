package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.RecordDisplayFactoryExtension;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.retentionRule.AdministrativeUnitReferenceDisplay;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.rm.model.enums.CompleteDatesWhenAddingFolderWithManualStatusChoice.ENABLED;

public class RMRecordDisplayFactoryExtension extends RecordDisplayFactoryExtension {

	private AppLayerFactory appLayerFactory;
	private String collection;

	public RMRecordDisplayFactoryExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
	}

	@Override
	public ReferenceDisplay getDisplayForReference(AllowedReferences allowedReferences, String id) {
		if(allowedReferences != null && allowedReferences.getAllowedSchemaType() != null &&
				allowedReferences.getAllowedSchemaType().contains(AdministrativeUnit.SCHEMA_TYPE)) {
			return getReferenceDisplayForAdministrativeUnit(id);
		}
		return null;
	}
	public AdministrativeUnitReferenceDisplay getReferenceDisplayForAdministrativeUnit(String id) {
		return new AdministrativeUnitReferenceDisplay(id);
	}
}
