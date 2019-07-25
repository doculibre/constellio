package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.Choice;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.FieldOverridePresenter;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.OverrideMode;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class AddEditSchemaRecordPresenter extends SingleSchemaBasePresenter<AddEditSchemaRecordView>
		implements FieldOverridePresenter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditSchemaRecordPresenter.class);

	private static final String CANNOT_CHANGE_LINKED_SCHEMA_WHEN_REFERENCED = "cannotChangeLinkedSchemaWhenReferenced";

	private String schemaCode;

	private RecordVO recordVO;

	public AddEditSchemaRecordPresenter(AddEditSchemaRecordView view, RecordVO recordVO) {
		super(view);
		this.recordVO = recordVO;
	}

	public void forParams(String params) {
		if (params != null) {
			computeParams(params);
		}
		view.setRecordVO(recordVO);
	}

	private void computeParams(String params) {
		if (schemaCode == null) {
			Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
			schemaCode = paramsMap.get("schema");
			String id = paramsMap.get("id");
			String parentRecordId = paramsMap.get("parentRecordId");

			RecordServices recordServices = recordServices();
			Record record;
			if (StringUtils.isNotBlank(id)) {
				record = recordServices.getDocumentById(id);
				schemaCode = record.getSchemaCode();
			} else if (StringUtils.isNotBlank(parentRecordId)) {
				Record parentRecord = recordServices.getDocumentById(parentRecordId);
				schemaCode = parentRecord.getSchemaCode();
				MetadataSchema schema = schema(schemaCode);
				Metadata parentMetadata = schema.get(HierarchicalValueListItem.PARENT);
				record = recordServices.newRecordWithSchema(schema);
				record.set(parentMetadata, parentRecordId);
			} else {
				MetadataSchema schema = schema(schemaCode);
				record = recordServices.newRecordWithSchema(schema);
			}

			setSchemaCode(schemaCode);
			recordVO = new RecordToVOBuilder().build(record, VIEW_MODE.FORM, view.getSessionContext());
		}
	}

	public void saveButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		try {
			Record record = toRecord(recordVO);
			if (record.isModified(Schemas.LINKED_SCHEMA)) {
				LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(view.getCollection())
						.where(Schemas.ALL_REFERENCES).isEqualTo(record.getId());
				long resultsCount = searchServices().getResultsCount(condition);
				if (resultsCount != 0) {
					ValidationErrors validationErrors = new ValidationErrors();
					validationErrors.add(getClass(), CANNOT_CHANGE_LINKED_SCHEMA_WHEN_REFERENCED);
					throw new RecordServicesException.ValidationException(record, validationErrors);
				}
			}

			addOrUpdate(record);
			view.navigate().to().displaySchemaRecord(record.getId());
		} catch (Exception e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigate().to().listSchemaRecords(schemaCode);
	}

	@Override
	public OverrideMode getOverride(String metadataCode) {
		if (metadataCode.endsWith("linkedSchema")) {
			return OverrideMode.DROPDOWN;
		} else {
			return OverrideMode.NONE;
		}
	}

	@Override
	public List<Choice> getChoices(String metadataCode) {
		return getSchemaChoices(getLinkedSchemaType(metadataCode));
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		computeParams(params);
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
		return new SchemaRecordsPresentersServices(appLayerFactory).canManageSchemaType(schemaTypeCode, user);
	}

	private List<Choice> getSchemaChoices(String schemaTypeCode) {
		MetadataSchemaType type = types().getSchemaType(schemaTypeCode);
		List<Choice> result = new ArrayList<>();
		for (MetadataSchema schema : type.getCustomSchemas()) {
			if (schema.isActive()) {
				Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
				result.add(new Choice(schema.getCode(), schema.getLabel(language)));
			}
		}
		return result;
	}

	private String getLinkedSchemaType(String metadataCode) {

		String ddvTypeCode = SchemaUtils.getSchemaTypeCode(metadataCode);

		for (MetadataSchemaType type : types().getSchemaTypes()) {
			MetadataSchema defaultSchema = type.getDefaultSchema();
			if (defaultSchema.hasMetadataWithCode("type")) {
				Metadata metadata = defaultSchema.getMetadata("type");
				if (metadata.getType() == MetadataValueType.REFERENCE
					&& ddvTypeCode.equals(metadata.getAllowedReferences().getTypeWithAllowedSchemas())) {
					return type.getCode();
				}
			}
		}

		throw new ImpossibleRuntimeException("Schema '" + ddvTypeCode + "' is not a type of any schema type");
	}
}
