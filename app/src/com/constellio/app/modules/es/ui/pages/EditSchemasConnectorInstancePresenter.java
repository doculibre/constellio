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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCode;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.users.UserServices;

public class EditSchemasConnectorInstancePresenter extends SingleSchemaBasePresenter<EditSchemasConnectorInstanceView> {

	private String connectorTypeId;
	private String connectorInstanceId;
	private ConnectorInstance connectorInstance;
	private ConnectorType connectorType;
	private MetadataSchemaVO schemaVO;
	private Map<String, List<MetadataVO>> metadataVOs;

	private transient ESSchemasRecordsServices esSchemasRecordsServices;
	private transient UserServices userServices;
	private transient RecordServices recordServices;

	public EditSchemasConnectorInstancePresenter(EditSchemasConnectorInstanceView view) {
		super(view);
		init();
	}

	public void init() {
		esSchemasRecordsServices = new ESSchemasRecordsServices(collection, appLayerFactory);
		userServices = modelLayerFactory.newUserServices();
		recordServices = modelLayerFactory.newRecordServices();
	}

	public void forParams(String params) {
		connectorInstanceId = params;
		connectorInstance = esSchemasRecordsServices.getConnectorInstance(connectorInstanceId);
		connectorTypeId = connectorInstance.getConnectorType();
		connectorType = esSchemasRecordsServices.getConnectorType(connectorTypeId);
		String codeSchema = ConnectorHttpDocument.SCHEMA_TYPE + "_" + connectorInstanceId;
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(codeSchema);
		List<String> metadatacodes = new ArrayList<>();
		schemaVO = new MetadataSchemaToVOBuilder().build(schema, VIEW_MODE.TABLE, metadatacodes, view.getSessionContext());

		metadataVOs = generateMetadataVO();
		System.out.println();
	}

	public Map<Integer, MetadataVO> getSettedField(Map<Integer, String> fields) {
		Map<Integer, MetadataVO> settedField = new HashMap<>();
		if(connectorInstance.getPropertiesMapping() != null) {
			MapStringListStringStructure propertiesMapping = connectorInstance.getPropertiesMapping();
			for(String key: propertiesMapping.keySet()) {
				List<String> value = propertiesMapping.get(key);
				for(String v: value) {
					Integer position = getPositionFor(fields, v);
					if(position != null) {
						settedField.put(position, schemaVO.getMetadata(key));
					}
				}
			}
		}

		return settedField;
	}

	private Integer getPositionFor(Map<Integer, String> fields, String value) {
		for(Integer position: fields.keySet()) {
			if(fields.get(position).equals(value)) {
				return position;
			}
		}

		return null;
	}

    private Map<String, List<MetadataVO>> generateMetadataVO() {
        metadataVOs = new HashMap<>();
		Map<String, String> defaultFields = getDefaultAvailableProperties();

		if(connectorInstance.getPropertiesMapping() != null) {
			MapStringListStringStructure propertiesMapping = connectorInstance.getPropertiesMapping();
			for (String code : propertiesMapping.keySet()) {
				MetadataVO metadataVO = schemaVO.getMetadata(code);
				List<String> properties = propertiesMapping.get(code);

				for (String prop : properties) {
					List<MetadataVO> vos;
					if (!metadataVOs.containsKey(defaultFields.get(prop))) {
						vos = new ArrayList<>();
					} else {
						vos = metadataVOs.get(defaultFields.get(prop));
					}

					vos.add(metadataVO);
					metadataVOs.put(defaultFields.get(prop), vos);
				}
			}
		}

        return metadataVOs;
    }

	public Map<String, String> getDefaultAvailableProperties() {
		return esSchemasRecordsServices.getHttpConnectorType().getDefaultAvailableProperties();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}

	public void backButtonClicked() {
		view.navigateTo().listConnectorInstances();
	}

	public void cancelButtonClicked() {
		view.navigateTo().listConnectorInstances();
	}

	public String getLocalCode(String metadataCode) {
		return metadataCode.split("_")[2];
	}

	public String getTitle() {
		return $("EditSchemasConnectorInstanceView.viewTitle") + " " + connectorInstance.getTitle();
	}

	public UserServices getUserServices() {
		return userServices;
	}

	public void createMetadata(String className, String localCodeMetadata, String label, int index) {
		String code = schemaVO.getCode() + "_" + localCodeMetadata;
		try {
			for (Entry<String, List<MetadataVO>> metadataVOMap : metadataVOs.entrySet()) {
				verifyIfExists(code, metadataVOMap);
			}
		} catch (InvalidCode e) {
			view.showErrorMessage($("EditSchemasConnectorInstanceView.error.codeExists"));
			return;
		}

		MetadataValueType type = getMetadataValueType(className);

		String collection = view.getCollection();

		String schemaTypeCode;
		if (connectorType.getCode().equals(ConnectorType.CODE_HTTP)) {
			schemaTypeCode = ConnectorHttpDocument.SCHEMA_TYPE;
		} else {
			throw new ImpossibleRuntimeException("Invalid connector type");
		}

		Map<Locale, String> labels = new HashMap<>();
		labels.put(view.getSessionContext().getCurrentLocale(), label);
		boolean readOnly = false;
		boolean required = false;
		boolean multivalue = true;
		boolean enabled = true;
		Class<? extends Enum<?>> enumClass = null;
		String[] taxonomyCodes = null;
		MetadataInputType metadataInputType = null;
		AllowedReferences allowedReferences = null;
		StructureFactory structureFactory = null;
		String metadataGroup = null;
		Object defaultValue = null;

		MetadataVO metadataVO = new MetadataVO(code, type, collection, schemaVO, required, multivalue, readOnly, labels,
				enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
				allowedReferences, enabled, structureFactory, metadataGroup, defaultValue);

		List<MetadataVO> newMetadataVOs = new ArrayList<>();
		newMetadataVOs.add(metadataVO);
		if (metadataVOs.containsKey(className)) {
			newMetadataVOs.addAll(metadataVOs.get(className));
		}
		metadataVOs.put(className, newMetadataVOs);
		view.setMetadataVOs(metadataVOs);
		view.setComboBoxValue(index, metadataVO);

	}

	private void verifyIfExists(String code, Entry<String, List<MetadataVO>> metadataVOMap) {
		for (MetadataVO metadataVO : metadataVOMap.getValue()) {
			if (metadataVO.getCode().equals(code)) {
				throw new InvalidCode("Code already created");
			}
		}
	}

	private MetadataValueType getMetadataValueType(String className) {
		MetadataValueType type;
		switch (className) {
		case "java.lang.String":
			type = MetadataValueType.STRING;
			break;
		case "org.joda.time.LocalDateTime":
			type = MetadataValueType.DATE_TIME;
			break;
		case "org.joda.time.LocalDate":
			type = MetadataValueType.DATE;
			break;
		case "java.lang.Integer":
			type = MetadataValueType.INTEGER;
			break;
		case "java.lang.Number":
		case "java.lang.Double":
			type = MetadataValueType.NUMBER;
			break;
		case "java.lang.Boolean":
			type = MetadataValueType.BOOLEAN;
			break;
		default:
			throw new ImpossibleRuntimeException("Invalid class name");
		}
		return type;
	}

	public Map<String, List<MetadataVO>> getMetadataVOs() {
		return metadataVOs;
	}

	public void saveButtonClicked(Map<Integer, String> properties, Map<Integer, MetadataVO> settedFields) {
		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();
		if (connectorType.getCode().equals(ConnectorType.CODE_HTTP)) {
			mapStringListStringStructure.putAll(getHTTPConnectorSelectedProperties(properties, settedFields));
		} else {
			throw new ImpossibleRuntimeException("Unsupported connector type");
		}

		try {
			connectorInstance.setPropertiesMapping(mapStringListStringStructure);
			recordServices.update(connectorInstance.getWrappedRecord());
		} catch (RecordServicesException e) {
			throw new ImpossibleRuntimeException(e);
		}

		view.navigateTo().listConnectorInstances();
	}

	private MapStringListStringStructure getHTTPConnectorSelectedProperties(Map<Integer, String> properties, Map<Integer, MetadataVO> settedFields) {
		List<String> createdMetadatas = new ArrayList<>();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		boolean created = false;
		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();

		for (Entry<Integer, MetadataVO> integerMetadataVOEntry : settedFields.entrySet()) {
			if (integerMetadataVOEntry.getValue() != null) {
				MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
				MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
				MetadataVO metadataVO = integerMetadataVOEntry.getValue();

				if (!createdMetadatas.contains(metadataVO.getCode())) {
					MetadataSchemaBuilder schemaBuilder = typesBuilder.getSchema(schemaVO.getCode());

					try {
						schemaBuilder.getMetadata(getLocalCode(metadataVO.getCode()));
					} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata nsm) {
						schemaBuilder.createUndeletable(getLocalCode(metadataVO.getCode()))
								.setType(metadataVO.getType())
								.setMultivalue(metadataVO.isMultivalue())
								.setLabel(metadataVO.getLabel(view.getSessionContext()));
						created = true;
					}

					createdMetadatas.add(metadataVO.getCode());
				}

				try {
					if (created) {
						metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
					}
					mapping(properties, mapStringListStringStructure, integerMetadataVOEntry, metadataVO);
				} catch (OptimistickLocking optimistickLocking) {
					throw new ImpossibleRuntimeException(optimistickLocking);
				}
			}

		}

		return mapStringListStringStructure;
	}

	private void mapping(Map<Integer, String> properties, MapStringListStringStructure mapStringListStringStructure, Entry<Integer, MetadataVO> integerMetadataVOEntry, MetadataVO metadataVO) {
		String property = properties.get(integerMetadataVOEntry.getKey());
		List<String> newProperties = new ArrayList<>();
		if (mapStringListStringStructure.containsKey(metadataVO.getCode())) {
			newProperties = mapStringListStringStructure.get(metadataVO.getCode());
		}
		newProperties.add(property);
		mapStringListStringStructure.put(metadataVO.getCode(), newProperties);
	}

	//TODO
	private String getSchemaLocalCode(String schemaCode) {
		return schemaCode.split("_")[1];
	}
}
