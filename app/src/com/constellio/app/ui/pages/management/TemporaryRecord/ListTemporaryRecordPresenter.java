package com.constellio.app.ui.pages.management.TemporaryRecord;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.returnAll;
import static java.util.Arrays.asList;

public class ListTemporaryRecordPresenter extends BasePresenter<ListTemporaryRecordView> {

	private Map<String, RecordVODataProvider> provider;
	private User user;

	public ListTemporaryRecordPresenter(ListTemporaryRecordView view) {
		super(view);
		provider = new HashMap<>();
		user = modelLayerFactory.newUserServices()
				.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), view.getCollection());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.hasAny(CorePermissions.ACCESS_TEMPORARY_RECORD, CorePermissions.SEE_ALL_TEMPORARY_RECORD).globally();
	}

	public void deleteButtonClick(String index, String schema) {
		RecordVO currentTemporaryRecord = getDataProviderFromType(schema).getRecordVO(Integer.parseInt(index));
		RecordServices recordServices = recordServices();
		if (canDeleteArchive(currentTemporaryRecord, user)) {
			recordServices.logicallyDelete(currentTemporaryRecord.getRecord(), user,
					new RecordLogicalDeleteOptions().setSkipValidations(true));
			recordServices.physicallyDelete(currentTemporaryRecord.getRecord(), user);
			view.navigate().to().listTemporaryRecords();
		}
	}

	private boolean canDeleteArchive(RecordVO recordVO, User user) {
		if (recordVO == null) {
			return false;
		}
		Record record = recordVO.getRecord();
		MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypeOf(record);
		boolean hasPermission = !schemaType.hasSecurity() || modelLayerFactory.newAuthorizationsServices()
				.hasRestaurationPermissionOnHierarchy(user, record, asList(recordVO.getRecord()));
		return hasPermission && (user.has(CorePermissions.ACCESS_DELETE_ALL_TEMPORARY_RECORD).globally() ||
								 getCurrentUser().getId().equals(recordVO.get(Schemas.CREATED_BY.getCode())));
	}

	public boolean isVisible(String index, String schema) {
		RecordVO currentTemporaryRecord = getDataProviderFromType(schema).getRecordVO(Integer.parseInt(index));
		return canDeleteArchive(currentTemporaryRecord, user);
	}

	public RecordVODataProvider getDataProviderFromType(final String schema) {
		if (!provider.containsKey(schema)) {
			final MetadataSchemaType temporaryRecordSchemaType = types().getSchemaType(TemporaryRecord.SCHEMA_TYPE);
			MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(temporaryRecordSchemaType.getSchema(schema),
					RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
			provider.put(schema, new RecordVODataProvider(
					schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
				@Override
				public LogicalSearchQuery getQuery() {
					//TODO Gabriel move condition to an extension
					if (schema.equals(BatchProcessReport.FULL_SCHEMA)) {
						return LogicalSearchQuery.returningNoResults();
					} else {
						User user = view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices()
								.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(),
										view.getCollection());
						OngoingLogicalSearchCondition FromCondition = from(temporaryRecordSchemaType.getSchema(schema));
						LogicalSearchCondition condition = user.has(CorePermissions.SEE_ALL_TEMPORARY_RECORD).globally() ?
														   FromCondition.where(returnAll()) :
														   FromCondition.where(Schemas.CREATED_BY).isEqualTo(user);
						return new LogicalSearchQuery().setCondition(condition).sortDesc(Schemas.CREATED_ON);
					}
				}
			});
		}
		return provider.get(schema);
	}
}
