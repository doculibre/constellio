package com.constellio.app.modules.rm.ui.pages.legalrequirement;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class LegalRequirementListPresenter extends BasePresenter<LegalRequirementListView> {

	private RMSchemasRecordsServices rm;
	private RecordDeleteServices recordDeleteServices;
	private SearchServices searchServices;
	private MetadataSchemaTypes types;

	private String searchFilter;

	public LegalRequirementListPresenter(LegalRequirementListView view) {
		super(view);

		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordDeleteServices = new RecordDeleteServices(appLayerFactory.getModelLayerFactory());
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public RecordVODataProvider getDataProvider(String schemaType) {
		MetadataSchemaType type = types.getSchemaType(schemaType);
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(type.getDefaultSchema(), VIEW_MODE.TABLE,
				view.getSessionContext());

		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(),
				view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return LegalRequirementListPresenter.this.getQuery(type);
			}
		};
	}

	private LogicalSearchQuery getQuery(MetadataSchemaType schemaType) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType)
				.where(LOGICALLY_DELETED_STATUS).isFalseOrNull());

		if (StringUtils.isNotBlank(searchFilter)) {
			String groupSearchExpression = filterSolrOperators(searchFilter);
			if (groupSearchExpression.split(" ").length < 2) {
				groupSearchExpression = groupSearchExpression + " OR " + groupSearchExpression + "*";
			}

			query.setFreeTextQuery(groupSearchExpression).setPreferAnalyzedFields(true);
		}

		return query;
	}

	private String filterSolrOperators(String expression) {
		String groupSearchExpression = expression;

		if (StringUtils.isNotBlank(groupSearchExpression) && groupSearchExpression.startsWith("\"") && groupSearchExpression.endsWith("\"")) {
			groupSearchExpression = ClientUtils.escapeQueryChars(groupSearchExpression);
			groupSearchExpression = "\"" + groupSearchExpression + "\"";
		}

		return groupSearchExpression;
	}

	public void searchRequested(String value) {
		this.searchFilter = value;
		view.refreshTables();
	}

	public void clearSearchRequested() {
		this.searchFilter = null;
		view.refreshTables();
	}

	public void addButtonClicked(String schemaType) {
		if (!canEdit()) {
			return;
		}

		if (schemaType.equals(LegalRequirement.SCHEMA_TYPE)) {
			view.navigate().to(RMViews.class).addEditLegalRequirement(null);
		} else {
			view.navigate().to(RMViews.class).addEditLegalReference(null);
		}
	}

	public void displayButtonClicked(RecordVO recordVO) {
		if (recordVO.getSchemaCode().startsWith(LegalRequirement.SCHEMA_TYPE)) {
			view.navigate().to(RMViews.class).displayLegalRequirement(recordVO.getId());
		} else {
			view.navigate().to(RMViews.class).displayLegalReference(recordVO.getId());
		}
	}

	public void editButtonClicked(RecordVO recordVO) {
		if (!canEdit()) {
			return;
		}

		if (recordVO.getSchemaCode().startsWith(LegalRequirement.SCHEMA_TYPE)) {
			view.navigate().to(RMViews.class).addEditLegalRequirement(recordVO.getId());
		} else {
			view.navigate().to(RMViews.class).addEditLegalReference(recordVO.getId());
		}
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		if (!canEdit()) {
			return;
		}

		if (recordVO.getSchemaCode().startsWith(LegalRequirement.SCHEMA_TYPE)) {
			LogicalSearchCondition condition = from(rm.legalRequirementReference.schemaType())
					.where(rm.legalRequirementReference.ruleRequirement()).isEqualTo(recordVO.getId());
			List<Record> records = searchServices.search(new LogicalSearchQuery(condition));

			if (recordDeleteServices.isReferencedByOtherRecords(recordVO.getRecord(), records)) {
				view.showErrorMessage($("LegalRequirementManagement.recordHasReference"));
				return;
			}

			for (Record record : records) {
				recordDeleteServices.physicallyDeleteNoMatterTheStatus(record, getCurrentUser(),
						new RecordPhysicalDeleteOptions());
			}

			recordDeleteServices.physicallyDeleteNoMatterTheStatus(recordVO.getRecord(), getCurrentUser(),
					new RecordPhysicalDeleteOptions());
		} else {
			if (recordDeleteServices.isReferencedByOtherRecords(recordVO.getRecord(), new ArrayList<>())) {
				view.showErrorMessage($("LegalRequirementManagement.recordHasReference"));
				return;
			}

			recordDeleteServices.physicallyDeleteNoMatterTheStatus(recordVO.getRecord(), getCurrentUser(),
					new RecordPhysicalDeleteOptions());
		}

		view.refreshTables();
	}

	public boolean canEdit() {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_LEGAL_REQUIREMENTS).onSomething();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_LEGAL_REQUIREMENTS).onSomething()
			   || user.has(RMPermissionsTo.CONSULT_LEGAL_REQUIREMENTS).onSomething();
	}
}