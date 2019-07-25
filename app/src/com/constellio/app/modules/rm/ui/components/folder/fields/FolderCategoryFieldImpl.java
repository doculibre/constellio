package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.LinkableConceptFilter;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchFilter;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class FolderCategoryFieldImpl extends LookupRecordField implements FolderCategoryField {

	public FolderCategoryFieldImpl() {
		this(Category.SCHEMA_TYPE, null, false);
	}

	private FolderCategoryFieldImpl(String schemaTypeCode, String schemaCode, boolean writeAccess) {
		super(new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, schemaCode, writeAccess,
				false, true, false), getTreeDataProvider(schemaTypeCode, schemaCode, writeAccess));
		this.isShowDeactivated = false;
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}

	private static LookupTreeDataProvider<String>[] getTreeDataProvider(final String schemaTypeCode,
																		final String schemaCode,
																		boolean writeAccess) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		final String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = getInstance();
		final ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
		List<Taxonomy> taxonomies;
		if (schemaTypeCode != null) {
			taxonomies = taxonomiesManager
					.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, currentUser, metadataSchemasManager);
		} else {
			taxonomies = taxonomiesManager.getAvailableTaxonomiesForSchema(schemaCode, currentUser, metadataSchemasManager);
		}
		List<RecordLookupTreeDataProvider> dataProviders = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomies) {
			String taxonomyCode = taxonomy.getCode();
			if (StringUtils.isNotBlank(taxonomyCode)) {
				dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, writeAccess,
						getDataProvider(taxonomyCode, constellioFactories, sessionContext)));
			}
		}
		return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
	}

	static public LinkableRecordTreeNodesDataProvider getDataProvider(String taxonomyCode,
																	  ConstellioFactories constellioFactories,
																	  SessionContext sessionContext) {
		MetadataSchemaType categoryType = constellioFactories.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(sessionContext.getCurrentCollection()).getSchemaType(Category.SCHEMA_TYPE);
		LogicalSearchCondition searchCondition = from(categoryType)
				.where(categoryType.getDefaultSchema().get(Category.DEACTIVATE)).isNotEqual(true);
		TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();

		//taxonomiesSearchFilter.setLinkableConceptsCondition(searchCondition);

		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {

				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(params.getRecord().getCollection(),
						ConstellioFactories.getInstance().getAppLayerFactory());

				Category category = rm.wrapCategory(params.getRecord());
				return LangUtils.isFalseOrNull(category.<Boolean>get(Category.DEACTIVATE));
			}
		});

		return new LinkableRecordTreeNodesDataProvider(taxonomyCode, Category.SCHEMA_TYPE, false, taxonomiesSearchFilter);
	}
}
