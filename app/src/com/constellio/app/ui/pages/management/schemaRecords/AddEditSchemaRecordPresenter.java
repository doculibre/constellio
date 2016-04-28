package com.constellio.app.ui.pages.management.schemaRecords;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.Choice;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.FieldOverridePresenter;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.OverrideMode;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;

@SuppressWarnings("serial")
public class AddEditSchemaRecordPresenter extends SingleSchemaBasePresenter<AddEditSchemaRecordView>
		implements FieldOverridePresenter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditSchemaRecordPresenter.class);

	public AddEditSchemaRecordPresenter(AddEditSchemaRecordView view) {
		super(view);
	}

	public void forSchema(String schemaCode) {
		setSchemaCode(schemaCode);
	}

	public RecordVO getRecordVO(String id) {
		if (StringUtils.isNotBlank(id)) {
			return presenterService().getRecordVO(id, VIEW_MODE.FORM, view.getSessionContext());
		} else {
			return new RecordToVOBuilder().build(newRecord(), VIEW_MODE.FORM, view.getSessionContext());
		}
	}

	public void saveButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		try {
			Record record = toRecord(recordVO);
			addOrUpdate(record);
			view.navigate().to().listSchemaRecords(schemaCode);
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
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(params);
		return new SchemaRecordsPresentersServices(appLayerFactory).canManageSchemaType(schemaTypeCode, user);
	}

	private List<Choice> getSchemaChoices(String schemaTypeCode) {
		MetadataSchemaType type = types().getSchemaType(schemaTypeCode);
		List<Choice> result = new ArrayList<>();
		for (MetadataSchema schema : type.getCustomSchemas()) {
			result.add(new Choice(schema.getCode(), schema.getLabel()));
		}
		return result;
	}

	private String getLinkedSchemaType(String metadataCode) {

		String ddvTypeCode = new SchemaUtils().getSchemaTypeCode(metadataCode);

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
