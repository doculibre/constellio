package com.constellio.app.ui.pages.management.valueDomains;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.utils.comparators.AbstractTextComparator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.jgoodies.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListValueDomainPresenter extends BasePresenter<ListValueDomainView> {

	private List<Map<Language, String>> labels;
	private List<String> collectionLanguage;

	public ListValueDomainPresenter(ListValueDomainView view) {
		super(view);
		collectionLanguage = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);
	}

	public List<Language> valueDomainCreationRequested(Map<Language, String> mapLanguage, boolean isMultiLingual) {
		List<Language> valueLanguageError = canAlterOrCreateLabels(mapLanguage, null);
		if (valueLanguageError != null && valueLanguageError.size() == 0) {
			valueListServices().createValueDomain(mapLanguage, isMultiLingual);
			view.refreshTable();
		} else {
			showExistingError(valueLanguageError);
		}

		return valueLanguageError;
	}

	private void showExistingError(List<Language> valueLanguageError) {
		StringBuilder errorMessage = new StringBuilder();
		int i = 0;
		for (Language language : valueLanguageError) {
			if (i != 0) {
				errorMessage.append("<br/>");
			}
			errorMessage.append($("ListValueDomainView.existingValueDomain", " (" + language.getCode().toUpperCase() + ")"));
			i++;
		}

		view.showErrorMessage(errorMessage.toString());
	}

	public List<String> getCollectionLanguage() {
		return collectionLanguage;
	}

	public void displayButtonClicked(MetadataSchemaTypeVO schemaType) {
		view.navigate().to().listSchemaRecords(schemaType.getCode() + "_default");
	}

	public List<Language> editButtonClicked(MetadataSchemaTypeVO schemaTypeVO, Map<Language, String> newLabel,
											Map<Language, String> oldValue) {
		List<Language> valueLanguageError = canAlterOrCreateLabels(newLabel, oldValue);
		if (valueLanguageError != null && valueLanguageError.size() == 0) {
			MetadataSchemaTypesBuilder metadataSchemaTypesBuilder = modelLayerFactory.getMetadataSchemasManager()
					.modify(view.getCollection());

			metadataSchemaTypesBuilder.getSchemaType(schemaTypeVO.getCode()).setLabels(newLabel);
			metadataSchemaTypesBuilder.getSchemaType(schemaTypeVO.getCode()).getDefaultSchema().setLabels(newLabel);

			try {
				modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(metadataSchemaTypesBuilder);
			} catch (OptimisticLocking optimistickLocking) {
				throw new RuntimeException(optimistickLocking);
			}
			view.refreshTable();
		} else {
			showExistingError(valueLanguageError);
		}

		return valueLanguageError;
	}

	public List<MetadataSchemaTypeVO> getDomainValues() {
		labels = new ArrayList<>();
		MetadataSchemaTypeToVOBuilder builder = newMetadataSchemaTypeToVOBuilder();
		List<MetadataSchemaTypeVO> result = new ArrayList<>();
		for (MetadataSchemaType schemaType : valueListServices().getValueDomainTypes()) {
			result.add(builder.build(schemaType));
			labels.add(schemaType.getLabel());
		}
		Collections.sort(result, new AbstractTextComparator<MetadataSchemaTypeVO>() {
			@Override
			protected String getText(MetadataSchemaTypeVO schemaTypeVO) {
				return schemaTypeVO.getLabel();
			}
		});
		return result;
	}

	public List<MetadataSchemaTypeVO> getDomainValues(boolean isUserCreated) {
		labels = new ArrayList<>();
		MetadataSchemaTypeToVOBuilder builder = newMetadataSchemaTypeToVOBuilder();
		List<MetadataSchemaTypeVO> result = new ArrayList<>();
		for (MetadataSchemaType schemaType : valueListServices().getValueDomainTypes()) {
			if ((isUserCreated && schemaType.getCode().matches(".*\\d")) ||
				(!isUserCreated && !schemaType.getCode().matches(".*\\d"))) {
				result.add(builder.build(schemaType));
			}

			labels.add(schemaType.getLabel());
		}
		Collections.sort(result, new AbstractTextComparator<MetadataSchemaTypeVO>() {
			@Override
			protected String getText(MetadataSchemaTypeVO schemaTypeVO) {
				return schemaTypeVO.getLabel();
			}
		});
		return result;
	}


	MetadataSchemaTypeToVOBuilder newMetadataSchemaTypeToVOBuilder() {
		return new MetadataSchemaTypeToVOBuilder();
	}

	ValueListServices valueListServices() {
		return new ValueListServices(appLayerFactory, view.getCollection());
	}


	public List<Language> canAlterOrCreateLabels(Map<Language, String> newMapTitle, Map<Language, String> oldValues) {
		List<Language> listUnvalidValueForLanguage = new ArrayList<>();

		if (labels == null || labels.size() == 0) {
			getDomainValues();
		}

		for (Language language : newMapTitle.keySet()) {
			String newCurrentTitle = newMapTitle.get(language);
			if (Strings.isBlank(newCurrentTitle)) {
				listUnvalidValueForLanguage.add(language);
			}
			for (Map<Language, String> existingTitleMap : labels) {
				String existingTitleInCurrentLanguage = existingTitleMap.get(language);
				if (Strings.isNotBlank(existingTitleInCurrentLanguage) && existingTitleInCurrentLanguage.equals(newCurrentTitle)
					&& (oldValues == null || oldValues != null && !existingTitleInCurrentLanguage.equals(oldValues.get(language)))) {
					listUnvalidValueForLanguage.add(language);
				}
			}
		}
		return listUnvalidValueForLanguage;
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_VALUELIST).globally();
	}

	public void deleteButtonClicked(String schemaTypeCode)
			throws ValidationException {

		valueListServices().deleteValueListOrTaxonomy(schemaTypeCode);
		view.refreshTable();
	}

	public boolean isValueListPossiblyDeletable(String schemaTypeCode) {
		String lcSchemaTypeCode = schemaTypeCode.toLowerCase();
		Pattern pattern = Pattern.compile("ddv[0-9]+[0-9a-z]*");
		return lcSchemaTypeCode.startsWith("ddvusr")
			   || lcSchemaTypeCode.startsWith("usrddv")
			   || pattern.matcher(lcSchemaTypeCode).matches();

	}
}
