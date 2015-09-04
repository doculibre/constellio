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
package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordDepositReportViewImpl;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordTransferReportViewImpl;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class DisplayContainerPresenter extends BasePresenter<DisplayContainerView> implements ReportPresenter {

	private String containerId;

	public DisplayContainerPresenter(DisplayContainerView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public void backButtonClicked() {
		view.navigateTo().previousView();
	}

	public RecordVODataProvider getFoldersDataProvider(final String containerId) {
		final MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE);
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchCondition condition = LogicalSearchQueryOperators.from(schema(Folder.DEFAULT_SCHEMA))
						.where(schema(Folder.DEFAULT_SCHEMA).getMetadata(Folder.CONTAINER)).isEqualTo(containerId);
				return new LogicalSearchQuery(condition);
			}
		};
		return dataProvider;
	}

	public RecordVO getContainer(String containerId) {
		return new RecordToVOBuilder().build(recordServices().getDocumentById(containerId), VIEW_MODE.DISPLAY);
	}

	public void displayFolderButtonClicked(RecordVO folder) {
		view.navigateTo().displayFolder(folder.getId());
	}

	@Override
	public List<String> getSupportedReports() {
		return asList($("Reports.ContainerRecordReport"));
	}

	@Override
	public ReportBuilderFactory getReport(String report) {

		Record record = modelLayerFactory.newRecordServices().getDocumentById(containerId);
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(view.getCollection());
		ContainerRecord containerRecord = new ContainerRecord(record, types);

		if (containerRecord.getDecommissioningType() == DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE) {
			return new ContainerRecordTransferReportViewImpl(containerId);
		} else if (containerRecord.getDecommissioningType() == DecommissioningType.DEPOSIT) {
			return new ContainerRecordDepositReportViewImpl(containerId);
		}
		throw new RuntimeException("BUG: Unknown report: " + report);
	}

	public boolean isPrintReportEnable() {
		boolean enable1;
		enable1 = getFoldersDataProvider(containerId).size() > 0;
		boolean enable2;
		try {
			getReport("");
			enable2 = true;
		} catch (RuntimeException e) {
			enable2 = false;
		}
		return (enable1 && enable2);
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getContainerId() {
		return containerId;
	}

	public List<LabelTemplate> getTemplates() {
		return appLayerFactory.getLabelTemplateManager().listTemplates(ContainerRecord.SCHEMA_TYPE);

	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasReadAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return asList(params);
	}

	public Double getFillRatio(RecordVO container)
			throws ContainerWithoutCapacityException, RecordInContainerWithoutLinearMeasure {
		MetadataVO fillRatioMetadata = container.getMetadata(ContainerRecord.FILL_RATIO_ENTRED);
		Double fillRatioEntered = container.get(fillRatioMetadata);
		if (fillRatioEntered != null) {
			return fillRatioEntered;
		}
		MetadataVO capacityMetadata = container.getMetadata(ContainerRecord.CAPACITY);
		Double capacity = container.get(capacityMetadata);
		if (capacity == null || capacity == 0.0) {
			throw new ContainerWithoutCapacityException();
		}
		RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
		Metadata containerMetadata = schemas.folderSchemaType().getDefaultSchema().getMetadata(Folder.CONTAINER);
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).where(containerMetadata).isEqualTo(container.getId());
		DataStoreField linearSizeMetadata = schemas.folderSchemaType().getDefaultSchema().getMetadata(Folder.LINEAR_SIZE);
		LogicalSearchQuery query = new LogicalSearchQuery(condition).computeStatsOnField(linearSizeMetadata.getDataStoreCode());
		SPEQueryResponse result = modelLayerFactory.newSearchServices().query(query);
		Map<String, Object> linearSizeStats = result.getStatValues(linearSizeMetadata.getDataStoreCode());
		if (linearSizeStats == null) {
			if (result.getNumFound() > 0) {
				//no folder with linearSize
				throw new RecordInContainerWithoutLinearMeasure();
			} else {
				//No folder in container
				return 0d;
			}

		}
		if (includesMissing(linearSizeStats)) {
			throw new RecordInContainerWithoutLinearMeasure();
		}
		Double sum = getSum(linearSizeStats);
		return sum * 100 / capacity;
	}

	private Double getSum(Map<String, Object> result) {
		Object sum = result.get("sum");
		return Double.valueOf(sum.toString());
	}

	private boolean includesMissing(Map<String, Object> result) {
		Object missing = result.get("missing");
		if (missing != null) {
			return !missing.equals(0L);
		} else {
			return false;
		}
	}

	public void editContainer() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("containerId", containerId);
		String params = ParamUtils.addParams(NavigatorConfigurationService.EDIT_CONTAINER, paramsMap);
		view.navigateTo().editContainer(params);
	}
}
