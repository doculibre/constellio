package com.constellio.app.ui.pages.management.schemas.display.display;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.constellio.data.utils.AccentApostropheCleaner.removeAccents;

public class DisplayConfigPresenter extends SingleSchemaBasePresenter<DisplayConfigView> {

	private Map<String, String> parameters;

	public DisplayConfigPresenter(DisplayConfigView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATASCHEMAS).globally();
	}

	public MetadataVODataProvider getDataProvider() {
		return new MetadataVODataProvider(new MetadataToVOBuilder(), modelLayerFactory, collection, getSchemaCode());
	}

	public void setParameters(Map<String, String> params) {
		this.parameters = params;
	}

	public List<FormMetadataVO> getMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataList list = schemasManager.getSchemaTypes(collection).getSchema(getSchemaCode()).getMetadatas();
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		MetadataSchemaType schemaType = schemasManager.getSchemaTypes(collection)
				.getSchemaType(SchemaUtils.getSchemaTypeCode(getSchemaCode()));

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder(view.getSessionContext());
		for (Metadata metadata : list) {
			if (this.isAllowedMetadata(metadata, schemaType, true)) {
				formMetadataVOs
						.add(builder.build(metadata, displayManager, parameters.get("schemaTypeCode"), view.getSessionContext()));
			}
		}

		final String language = view.getSessionContext().getCurrentLocale().getLanguage();
		Collections.sort(formMetadataVOs, new Comparator<FormMetadataVO>() {
			@Override
			public int compare(FormMetadataVO o1, FormMetadataVO o2) {
				String s1 = removeAccents(o1.getLabel(language).toLowerCase());
				String s2 = removeAccents(o2.getLabel(language).toLowerCase());
				return s1.compareTo(s2);
			}
		});

		return formMetadataVOs;
	}

	public List<FormMetadataVO> getValueMetadatas() {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		SchemasDisplayManager displayManager = schemasDisplayManager();
		MetadataSchemaType schemaType = schemasManager.getSchemaTypes(collection)
				.getSchemaType(SchemaUtils.getSchemaTypeCode(getSchemaCode()));
		List<String> codeList = displayManager.getSchema(collection, getSchemaCode()).getDisplayMetadataCodes();

		List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
		MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder(view.getSessionContext());
		for (String metadataCode : codeList) {
			Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
			if (this.isAllowedMetadata(metadata, schemaType, false)) {
				formMetadataVOs
						.add(builder.build(metadata, displayManager, parameters.get("schemaTypeCode"), view.getSessionContext()));
			}
		}

		return formMetadataVOs;
	}

	private boolean isAllowedMetadata(Metadata metadata, MetadataSchemaType schemaType, boolean mustBeEnabled) {
		List<Metadata> restrictedMetadata = Arrays.asList(Schemas.SCHEMA, Schemas.VERSION, Schemas.PATH, Schemas.PRINCIPAL_PATH,
				Schemas.REMOVED_AUTHORIZATIONS, Schemas.ALL_REMOVED_AUTHS, Schemas.ATTACHED_ANCESTORS,
				Schemas.IS_DETACHED_AUTHORIZATIONS, Schemas.TOKENS, Schemas.COLLECTION,
				Schemas.LOGICALLY_DELETED_STATUS, Schemas.SHARE_DENY_TOKENS, Schemas.SHARE_TOKENS,
				Schemas.DENY_TOKENS, Schemas.ALL_REFERENCES,
				Schemas.LOGICALLY_DELETED_ON, Schemas.MANUAL_TOKENS,
				Schemas.SCHEMA_AUTOCOMPLETE_FIELD, Schemas.VISIBLE_IN_TREES, Schemas.MIGRATION_DATA_VERSION);

		List<String> localCodes = new SchemaUtils().toMetadataLocalCodes(restrictedMetadata);
		boolean isEnabled = metadata.getSchemaCode().contains("_default") ?
				isEnabledInAtLeastOneSchema(metadata, schemaType) :
				metadata.isEnabled();

		return !localCodes.contains(metadata.getLocalCode()) && (!mustBeEnabled || isEnabled);
	}

	public void saveButtonClicked(List<FormMetadataVO> schemaVOs) {
		SchemasDisplayManager manager = schemasDisplayManager();
		SchemaDisplayConfig config = schemasDisplayManager().getSchema(collection, getSchemaCode());

		List<String> metadataCode = new ArrayList<>();
		for (FormMetadataVO formMetadataVO : schemaVOs) {
			metadataCode.add(formMetadataVO.getCode());
		}

		config = config.withDisplayMetadataCodes(metadataCode);
		manager.saveSchema(config);

		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigate().to().listSchema(params);
	}

	public void cancelButtonClicked() {
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA, parameters);
		view.navigate().to().listSchema(params);
	}
}
