package com.constellio.app.ui.pages.management.extractors;

import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;

public class AddEditMetadataExtractorPresenter extends BasePresenter<AddEditMetadataExtractorView> {

	private MetadataExtractorVO metadataExtractorVO;

	private boolean addView;

	private MetadataToVOBuilder metadataToVOBuilder;

	private MetadataSchemaToVOBuilder metadataSchemaToVOBuilder;

	private MetadataSchemaTypeToVOBuilder metadataSchemaTypeToVOBuilder;

	private String schemaCode;

	private MetadataVO metadataVO;

	public AddEditMetadataExtractorPresenter(AddEditMetadataExtractorView view) {
		super(view);
		metadataToVOBuilder = new MetadataToVOBuilder();
		metadataSchemaToVOBuilder = new MetadataSchemaToVOBuilder();
		metadataSchemaTypeToVOBuilder = new MetadataSchemaTypeToVOBuilder();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATAEXTRACTOR).globally();
	}

	public void forParams(String parameters) {
		SessionContext sessionContext = view.getSessionContext();
		MetadataSchemaTypes types = types();

		addView = StringUtils.isBlank(parameters);
		if (addView) {
			metadataExtractorVO = new MetadataExtractorVO(null, new MetadataPopulateConfigs());
			view.setSchemaTypeFieldVisible(true);
			view.setSchemaFieldVisible(true);
			view.setMetadataFieldEnabled(true);

			List<MetadataSchemaTypeVO> metadataSchemaTypeVOs = new ArrayList<>();
			List<MetadataSchemaType> schemaTypes = types.getSchemaTypes();
			MetadataListFilter filterSearchable = new MetadataListFilter() {
				@Override
				public boolean isReturned(Metadata metadata) {
					return metadata.isSearchable();
				}
			};
			for (MetadataSchemaType metadataSchemaType : schemaTypes) {
				if (!metadataSchemaType.getAllMetadatas().only(filterSearchable).onlyEnabled().isEmpty()) {
					metadataSchemaTypeVOs.add(metadataSchemaTypeToVOBuilder.build(metadataSchemaType, sessionContext));
				}
			}
			view.setSchemaTypeOptions(metadataSchemaTypeVOs);
		} else {
			view.setSchemaTypeFieldVisible(false);
			view.setSchemaFieldVisible(false);
			view.setMetadataFieldEnabled(false);

			Metadata metadata = types.getMetadata(parameters);

			schemaCode = metadata.getSchemaCode();
			metadataVO = metadataToVOBuilder.build(metadata, sessionContext);

			metadataExtractorVO = new MetadataExtractorVO(metadataVO, metadata.getPopulateConfigs());
		}
		view.setMetadataExtractorVO(metadataExtractorVO);

	}

	public void saveButtonClicked() {
		MetadataVO metadataVO = metadataExtractorVO.getMetadataVO();

		final List<RegexConfig> regexConfigs = voToRegexConfigs();

		final String metadataCode = metadataVO.getCode();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataBuilder metadataBuilder = types.getMetadata(metadataCode);
				metadataBuilder.getPopulateConfigsBuilder().setStyles(metadataExtractorVO.getStyles());
				metadataBuilder.getPopulateConfigsBuilder().setProperties(metadataExtractorVO.getProperties());
				metadataBuilder.getPopulateConfigsBuilder().setRegexes(regexConfigs);
				metadataBuilder.getPopulateConfigsBuilder().setAddOnly(metadataExtractorVO.isAddOnly());
			}
		});
		view.navigate().to().listMetadataExtractors();
	}

	private List<RegexConfig> voToRegexConfigs() {
		final List<RegexConfig> regexConfigs = new ArrayList<>();
		for (RegexConfigVO regexConfigVO : metadataExtractorVO.getRegexes()) {
			RegexConfig regexConfig = new RegexConfig();
			regexConfig.setInputMetadata(regexConfigVO.getInputMetadata());
			regexConfig.setRegex(Pattern.compile(regexConfigVO.getRegex()));
			regexConfig.setValue(regexConfigVO.getValue());
			regexConfig.setRegexConfigType(regexConfigVO.getRegexConfigType());
			regexConfigs.add(regexConfig);
		}
		return regexConfigs;
	}

	public void cancelButtonClicked() {
		view.navigate().to().listMetadataExtractors();
	}

	public void backButtonClicked() {
		view.navigate().to().listMetadataExtractors();
	}

	public void schemaTypeSelected(MetadataSchemaTypeVO schemaTypeVO) {
		List<MetadataSchemaVO> schemaOptions = new ArrayList<>();
		if (schemaTypeVO != null) {
			SessionContext sessionContext = view.getSessionContext();
			String schemaTypeCode = schemaTypeVO.getCode();
			MetadataSchemaTypes types = types();
			MetadataSchemaType schemaType = types.getSchemaType(schemaTypeCode);
			for (MetadataSchema schema : schemaType.getAllSchemas()) {
				schemaOptions.add(metadataSchemaToVOBuilder.build(schema, VIEW_MODE.TABLE, sessionContext));
			}
		} else {
			view.setMetadataOptions(new ArrayList<MetadataVO>());
		}
		view.setSchemaOptions(schemaOptions);
	}

	public void schemaSelected(MetadataSchemaVO schemaVO) {
		if (schemaVO != null) {
			List<MetadataVO> metadataOptions = getMetadataVOs(schemaVO.getCode());
			schemaCode = schemaVO.getCode();
			view.setMetadataOptions(metadataOptions);
		}
	}

	private List<MetadataVO> getMetadataVOs(String schemaCode) {
		List<MetadataVO> metadataOptions = new ArrayList<>();
		if (schemaCode != null) {
			SessionContext sessionContext = view.getSessionContext();
			MetadataSchemaTypes types = types();
			MetadataSchema schema = types.getSchema(schemaCode);
			for (Metadata metadata : schema.getMetadatas()
					.onlyWithType(TEXT, STRING, REFERENCE)
					.onlyManuals()
					.excludingNonValueListReferences()
					.onlyNotSystemReserved()
					.onlyEnabled()) {
				metadataOptions.add(metadataToVOBuilder.build(metadata, sessionContext));
			}
		}
		return metadataOptions;
	}

	protected List<MetadataVO> getMetadataVOsForRegexes(String schemaCode, MetadataVO excludeMetadataVO) {
		List<MetadataVO> metadataOptionsForRegexes = new ArrayList<>();
		if (schemaCode != null) {
			SessionContext sessionContext = view.getSessionContext();
			MetadataSchemaTypes types = types();
			MetadataSchema schema = types.getSchema(schemaCode);
			for (Metadata metadata : schema.getMetadatas().onlyWithType(TEXT, STRING, CONTENT).onlyManuals()
					.onlyNotSystemReserved()
					.onlyEnabled()) {
				if (excludeMetadataVO.getCode() != metadata.getCode()) {
					metadataOptionsForRegexes.add(metadataToVOBuilder.build(metadata, sessionContext));
				}
			}
		}
		return metadataOptionsForRegexes;
	}

	public void afterViewAssembled() {
		setMetadataVOsForRegexes(schemaCode, metadataVO);
	}

	public void setMetadataVOsForRegexes(String schemaCode, MetadataVO metadataVO) {
		List<MetadataVO> metadataVOsForRegexes = getMetadataVOsForRegexes(schemaCode, metadataVO);
		view.setRegexMetadataOptions(metadataVOsForRegexes);

	}

	protected MetadataExtractorVO getMetadataExtractorVO() {
		return metadataExtractorVO;
	}

	protected MetadataVO getMetadataVO() {
		return metadataVO;
	}
}
