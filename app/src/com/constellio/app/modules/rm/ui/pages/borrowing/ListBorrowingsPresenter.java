package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.contents.ContentFactory.checkedOut;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class ListBorrowingsPresenter extends BasePresenter<ListBorrowingsView> {
	private RMSchemasRecordsServices recordsServices;

	private String selectedAdministrativeUnit;
	private boolean showOverdueOnly;

	public ListBorrowingsPresenter(ListBorrowingsView view) {
		super(view);

		recordsServices = new RMSchemasRecordsServices(view.getSessionContext().getCurrentCollection(), appLayerFactory);
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	public void setAdministrativeUnit(String value) {
		selectedAdministrativeUnit = value;
	}

	public void setShowOverdueOnly(boolean value) {
		showOverdueOnly = value;
	}

	public RecordVODataProvider getDataProvider(String schemaTypeCode) {
		switch (schemaTypeCode) {
			case Document.SCHEMA_TYPE:
				return getDocumentDataProvider();
			case Folder.SCHEMA_TYPE:
				return getFolderDataProvider();
			case ContainerRecord.SCHEMA_TYPE:
				return getContainerDataProvider();

			default:
				return null;
		}
	}

	private RecordVODataProvider getFolderDataProvider() {
		MetadataSchemaType schemaType = recordsServices.folderSchemaType();
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				schemaType.getDefaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				List<LogicalSearchCondition> conditions = new ArrayList<>();
				conditions.add(getCheckedOutCondition(recordsServices.folder.borrowed()));
				if (StringUtils.isNotBlank(selectedAdministrativeUnit)) {
					conditions.add(getAdministrativeUnitCondition(recordsServices.folder.administrativeUnit()));
				}
				if (showOverdueOnly) {
					conditions.add(getOverdueCondition(recordsServices.folder.borrowPreviewReturnDate()));
				}
				return new LogicalSearchQuery(from(schemaType)
						.whereAllConditions(conditions))
						.sortDesc(Schemas.MODIFIED_ON);
			}
		};
	}

	private RecordVODataProvider getContainerDataProvider() {
		MetadataSchemaType schemaType = recordsServices.containerRecordSchemaType();
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				schemaType.getDefaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				List<LogicalSearchCondition> conditions = new ArrayList<>();
				conditions.add(getCheckedOutCondition(recordsServices.containerRecord.borrowed()));
				if (StringUtils.isNotBlank(selectedAdministrativeUnit)) {
					conditions.add(getContainerAdministrativeUnitCondition());
				}
				if (showOverdueOnly) {
					conditions.add(getOverdueCondition(recordsServices.containerRecord.planifiedReturnDate()));
				}
				return new LogicalSearchQuery(from(schemaType)
						.whereAllConditions(conditions))
						.sortDesc(Schemas.MODIFIED_ON);
			}
		};
	}

	private RecordVODataProvider getDocumentDataProvider() {
		MetadataSchemaType schemaType = recordsServices.documentSchemaType();
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				schemaType.getDefaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				List<LogicalSearchCondition> conditions = new ArrayList<>();
				conditions.add(getDocumentCheckedOutCondition());
				if (StringUtils.isNotBlank(selectedAdministrativeUnit)) {
					conditions.add(getAdministrativeUnitCondition(recordsServices.document.administrativeUnit()));
				}
				if (showOverdueOnly) {
					conditions.add(getDocumentOverdueCondition());
				}
				return new LogicalSearchQuery(from(schemaType)
						.whereAllConditions(conditions))
						.sortDesc(Schemas.MODIFIED_ON);
			}
		};
	}

	private LogicalSearchCondition getCheckedOutCondition(Metadata borrowedMetadata) {
		return where(borrowedMetadata).isEqualTo(true);
	}

	private LogicalSearchCondition getDocumentCheckedOutCondition() {
		return where(recordsServices.document.content()).is(checkedOut());
	}

	private LogicalSearchCondition getAdministrativeUnitCondition(Metadata administrativeUnitMetadata) {
		return where(administrativeUnitMetadata).isEqualTo(selectedAdministrativeUnit);
	}

	private LogicalSearchCondition getContainerAdministrativeUnitCondition() {
		return where(recordsServices.containerRecord.administrativeUnits()).isContaining(Collections.singletonList(selectedAdministrativeUnit));
	}

	private LogicalSearchCondition getOverdueCondition(Metadata expectedReturnDateMetadata) {
		return where(expectedReturnDateMetadata).isLessThan(LocalDateTime.now());
	}

	private LogicalSearchCondition getDocumentOverdueCondition() {
		// TODO::JOLA --> Replace config for getDocumentBorrowingDurationDays after the merge!
		int borrowingDuration = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).getBorrowingDurationDays();
		// TODO::JOLA --> Replace metadata for content_borrowing_date after the merge!
		return where(recordsServices.document.createdOn()).isLessThan(LocalDateTime.now().minus(Period.days(borrowingDuration)));
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_BORROWINGS).globally();
	}
}
