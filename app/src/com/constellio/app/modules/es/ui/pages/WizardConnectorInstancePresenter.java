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
package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class WizardConnectorInstancePresenter extends AddEditConnectorInstancePresenter {
	public static final String SELECT_CONNECTOR_TYPE_TAB = "selectConnectorType";
	public static final String ADD_CONNECTOR_INSTANCE = "addConnectorInstance";

	public WizardConnectorInstancePresenter(WizardConnectorInstanceView view) {
		super(view);
	}

	@Override
	public void forParams(String params) {
		if (StringUtils.isNotBlank(params)) {
			if (SELECT_CONNECTOR_TYPE_TAB.equals(params)) {
				setAndSelectConnectorTypeTab();
			} else if (ADD_CONNECTOR_INSTANCE.equals(params)) {
				setAndSelectAddConnectorInstanceTab();
			} else {
				setAndSelectConnectorTypeTab();
			}
		} else {
			setAndSelectConnectorTypeTab();
		}
	}

	private void setAndSelectConnectorTypeTab() {
		view.setConnectorTypeListTable();
		view.selectConnectorTypeTab();
	}

	private void setAndSelectAddConnectorInstanceTab() {
		view.setAddConnectorInstanceForm();
		view.selectAddConnectorInstanceTab();
	}

	@Override
	public void cancelButtonClicked() {
		setAndSelectConnectorTypeTab();
	}

	@Override
	public void saveButtonClicked(RecordVO recordVO) {
		Record record = toRecord(recordVO);
		ConnectorInstance connectorInstance = esSchemasRecordsServices.wrapConnectorInstance(record);
		esSchemasRecordsServices.getConnectorManager().createConnector(connectorInstance);
		view.navigateTo().listConnectorInstances();
	}

	public void connectorTypeButtonClicked(String id) {
		setConnectorTypeId(id);
		Record record;
		ConnectorType connectorType = esSchemasRecordsServices.getConnectorType(connectorTypeId);
		ConnectorInstance connectorInstance;
		if (connectorType.getCode().equals(ConnectorType.CODE_HTTP)) {
			connectorInstance = esSchemasRecordsServices.newConnectorHttpInstance();
		} else if (connectorType.getCode().equals(ConnectorType.CODE_SMB)) {
			connectorInstance = esSchemasRecordsServices.newConnectorSmbInstance();
		} else {
			throw new ImpossibleRuntimeException("Invalid connector type");
		}

		record = connectorInstance.getWrappedRecord();
		setRecordVO(voBuilder.build(record, VIEW_MODE.FORM, view.getSessionContext()));
		setCurrentSchemaCode(recordVO.getSchema().getCode());
		setSchemaCode(currentSchemaCode);
		view.setRecordVO(recordVO);
		setAndSelectAddConnectorInstanceTab();

	}

	RecordVODataProvider getDataProviderSelectConnectorType() {

		List<String> metadataCodes = new ArrayList<>();
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		SessionContext sessionContext = view.getSessionContext();
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		MetadataSchemaVO schemaVO = schemaVOBuilder
				.build(schema(ConnectorType.DEFAULT_SCHEMA), VIEW_MODE.TABLE, metadataCodes, sessionContext);
		return new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory, sessionContext) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchCondition condition = from(schemaType(ConnectorType.SCHEMA_TYPE))
						.where(Schemas.CODE).isNot(containingText("http"));
				return new LogicalSearchQuery().setCondition(condition).sortAsc(Schemas.SCHEMA);
			}
		};
	}

	@Override
	public String getTitle() {
		return $("WizardConnectorInstanceView.viewTitle");
	}
}
