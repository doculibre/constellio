package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.converters.TaxonomyCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.TaxonomyRecordIdToContextCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.LinkableConceptFilter;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchFilter;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class TaskTypeFieldLookupImpl extends LookupRecordField implements TaskTypeField {

	public TaskTypeFieldLookupImpl(List<String> unavailablesTaskTypes) {
		this(TaskType.SCHEMA_TYPE, unavailablesTaskTypes);
	}

	private TaxonomyCodeToCaptionConverter captionConverter = new TaxonomyCodeToCaptionConverter();

	public TaskTypeFieldLookupImpl(String schemaTypeCode, List<String> unavailablesTaskTypes) {
		this(schemaTypeCode, null, unavailablesTaskTypes);
	}

	public TaskTypeFieldLookupImpl(String schemaTypeCode, String schemaCode, List<String> unavailablesTaskTypes) {
		this(schemaTypeCode, schemaCode, false, unavailablesTaskTypes);
	}

	public TaskTypeFieldLookupImpl(String schemaTypeCode, String schemaCode, boolean writeAccess,
								   List<String> unavailablesTaskTypes) {
		this(schemaTypeCode, schemaCode, writeAccess, true, true, unavailablesTaskTypes);
	}

	public TaskTypeFieldLookupImpl(String schemaTypeCode, String schemaCode, boolean writeAccess,
								   boolean showDeactivated, boolean isShowAllIfHasAccessToManageSecurity,
								   final List<String> unavailablesTaskTypes) {
		super(new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, schemaCode, writeAccess, showDeactivated) {
				  @Override
				  public LogicalSearchQuery getQuery(User user, String text, int startIndex, int count) {
					  LogicalSearchQuery query = super.getQuery(user, text, startIndex, count);
					  if (unavailablesTaskTypes != null) {
						  query.setCondition(query.getCondition().andWhere(Schemas.IDENTIFIER).isNotIn(unavailablesTaskTypes));
					  }
					  return query;
				  }
			  },
				getTreeDataProvider(schemaTypeCode, schemaCode, writeAccess, isShowAllIfHasAccessToManageSecurity, unavailablesTaskTypes));
		this.isShowDeactivated = showDeactivated;
		setItemConverter(new TaxonomyRecordIdToContextCaptionConverter());
	}

	private static LookupTreeDataProvider<String>[] getTreeDataProvider(String schemaTypeCode, String schemaCode,
																		boolean writeAccess,
																		boolean isShowAllIfHasAccessToManageSecurity,
																		List<String> unavailablesTaskTypes) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
		List<Taxonomy> taxonomies = new ArrayList<>();
		if (schemaTypeCode != null) {
			taxonomies = taxonomiesManager
					.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, currentUser, metadataSchemasManager);
		} else if (schemaCode != null) {
			taxonomies = taxonomiesManager.getAvailableTaxonomiesForSchema(schemaCode, currentUser, metadataSchemasManager);
		}
		List<RecordLookupTreeDataProvider> dataProviders = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomies) {
			String taxonomyCode = taxonomy.getCode();
			if (StringUtils.isNotBlank(taxonomyCode)) {
				dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, writeAccess, getDataProvider(taxonomyCode, unavailablesTaskTypes)));
			}
		}

		return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
	}

	public static RecordTreeNodesDataProvider getDataProvider(String taxonomyCode,
															  final List<String> unavailablesTaskTypes) {
		final TaxonomiesSearchFilter filter = new TaxonomiesSearchFilter();
		filter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				return unavailablesTaskTypes == null || !unavailablesTaskTypes.contains(params.getRecord().getId());
			}
		});

		return new LinkableRecordTreeNodesDataProvider(taxonomyCode, TaskType.SCHEMA_TYPE, false, filter);
	}

	@Override
	public String getFieldValue() {
		return (String) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue((String) value);
	}
}
