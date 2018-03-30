package com.constellio.app.ui.pages.management.valueDomains;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ListValueDomainPresenter extends BasePresenter<ListValueDomainView> {

	private List<String> labels;

	public ListValueDomainPresenter(ListValueDomainView view) {
		super(view);
	}

	public void valueDomainCreationRequested(String valueDomain) {
		valueDomain = valueDomain.trim();
		boolean canCreate = canCreate(valueDomain);
		if (canCreate) {
			valueListServices().createValueDomain(valueDomain);
			view.refreshTable();
			labels.add(valueDomain);
		}
	}

	public void displayButtonClicked(MetadataSchemaTypeVO schemaType) {
		view.navigate().to().listSchemaRecords(schemaType.getCode() + "_default");
	}

	public void editButtonClicked(MetadataSchemaTypeVO schemaTypeVO, String newLabel) {
		if (!verifyIfExists(newLabel)) {
			MetadataSchemaTypesBuilder metadataSchemaTypesBuilder = modelLayerFactory.getMetadataSchemasManager()
					.modify(view.getCollection());
			Language language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
			metadataSchemaTypesBuilder.getSchemaType(schemaTypeVO.getCode()).addLabel(language, newLabel);
			metadataSchemaTypesBuilder.getSchemaType(schemaTypeVO.getCode()).getDefaultSchema().addLabel(language, newLabel);

			try {
				modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(metadataSchemaTypesBuilder);
			} catch (OptimisticLocking optimistickLocking) {
				throw new RuntimeException(optimistickLocking);
			}
			view.refreshTable();
		} else if (newLabel != null && !newLabel.equals(schemaTypeVO.getLabel(view.getSessionContext().getCurrentLocale()))) {
			view.showErrorMessage($("ListValueDomainView.existingValueDomain", newLabel));
		}
	}

	public List<MetadataSchemaTypeVO> getDomainValues() {
		labels = new ArrayList<>();
		MetadataSchemaTypeToVOBuilder builder = newMetadataSchemaTypeToVOBuilder();
		List<MetadataSchemaTypeVO> result = new ArrayList<>();
		for (MetadataSchemaType schemaType : valueListServices().getValueDomainTypes()) {
			result.add(builder.build(schemaType));
			labels.add(schemaType.getLabel(Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage())).trim());
		}
		return result;
	}

	public List<MetadataSchemaTypeVO> getDomainValues(boolean isUserCreated) {
		labels = new ArrayList<>();
		MetadataSchemaTypeToVOBuilder builder = newMetadataSchemaTypeToVOBuilder();
		List<MetadataSchemaTypeVO> result = new ArrayList<>();
		for (MetadataSchemaType schemaType : valueListServices().getValueDomainTypes()) {
			if((isUserCreated && schemaType.getCode().matches(".*\\d")) ||
					(!isUserCreated && !schemaType.getCode().matches(".*\\d"))) {
				result.add(builder.build(schemaType));
			}

			labels.add(schemaType.getLabel(Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage())).trim());
		}
		return result;
	}

	MetadataSchemaTypeToVOBuilder newMetadataSchemaTypeToVOBuilder() {
		return new MetadataSchemaTypeToVOBuilder();
	}

	ValueListServices valueListServices() {
		return new ValueListServices(appLayerFactory, view.getCollection());
	}

	boolean canCreate(String valueDomain) {
		valueDomain = valueDomain.trim();
		boolean canCreate = false;
		if (StringUtils.isNotBlank(valueDomain)) {
			boolean exist = verifyIfExists(valueDomain);
			canCreate = !exist;
		}
		return canCreate;
	}

	private boolean verifyIfExists(String valueDomain) {
		if (labels == null) {
			getDomainValues();
		}
		boolean exits = false;
		for (String label : labels) {
			if (label.equals(valueDomain)) {
				exits = true;
			}
		}
		return exits;
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
