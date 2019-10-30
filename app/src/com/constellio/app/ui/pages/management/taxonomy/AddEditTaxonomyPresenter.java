package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.jgoodies.common.base.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditTaxonomyPresenter extends BasePresenter<AddEditTaxonomyView> {

	transient UserServices userServices;
	private boolean actionEdit = false;
	private List<Map<Language, String>> titles;
	private transient TaxonomiesManager taxonomiesManager;
	private transient MetadataSchemasManager schemasManager;

	private List<String> collectionLanguage;

	public AddEditTaxonomyPresenter(AddEditTaxonomyView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		collectionLanguage = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);
	}

	private boolean isTitleChanged(Map<Language, String> mapLang, Map<Language, String> mapLang2) {
		for (Language language : mapLang.keySet()) {
			if (!mapLang.get(language).equals(mapLang2.get(language))) {
				return true;
			}
		}
		return false;
	}

	public List<String> getCollectionLanguage() {
		return collectionLanguage;
	}

	public List<Language> saveButtonClicked(TaxonomyVO taxonomyVO, boolean isMultiLingual) {
		List<Language> languageInError = new ArrayList<>();

		if (isActionEdit()) {
			Taxonomy taxonomy = fetchTaxonomy(taxonomyVO.getCode());
			boolean wasTitleChanged = isTitleChanged(taxonomy.getTitle(), taxonomyVO.getTitleMap());
			if (wasTitleChanged) {
				languageInError = canAlterOrCreateLabels(taxonomyVO.getTitleMap(), taxonomy.getTitle());

				if (languageInError.size() > 0) {
					return languageInError;
				}
			}

			taxonomy = taxonomy.withTitle(taxonomyVO.getTitleMap())
					.withAbbreviation(taxonomyVO.getAbbreviationMap())
					.withUserIds(taxonomyVO.getUserIds())
					.withGroupIds(taxonomyVO.getGroupIds())
					.withVisibleInHomeFlag(taxonomyVO.isVisibleInHomePage());

			renameTaxonomyType(taxonomy, wasTitleChanged);

			createMetadatasInClassifiedObjects(taxonomy, taxonomyVO.getClassifiedObjects(), wasTitleChanged);
			taxonomiesManager.editTaxonomy(taxonomy);
			view.navigate().to().listTaxonomies();
		} else {
			languageInError = canAlterOrCreateLabels(taxonomyVO.getTitleMap(), null);

			if (languageInError.size() > 0) {
				return languageInError;
			}

			Taxonomy taxonomy = valueListServices()
					.createTaxonomy(taxonomyVO.getTitleMap(), taxonomyVO.getAbbreviationMap(), taxonomyVO.getUserIds(),
							taxonomyVO.getGroupIds(), taxonomyVO.isVisibleInHomePage(), isMultiLingual);
			createMetadatasInClassifiedObjects(taxonomy, taxonomyVO.getClassifiedObjects(), true);
			view.navigate().to().listTaxonomies();
			titles.add(taxonomyVO.getTitleMap());
		}

		return languageInError;
	}


	public List<Language> canAlterOrCreateLabels(Map<Language, String> newMapTitle, Map<Language, String> oldValues) {
		List<Language> listUnvalidValueForLanguage = new ArrayList<>();

		if (titles == null || titles.size() == 0) {
			getTaxonomies();
		}

		for (Language language : newMapTitle.keySet()) {
			String newCurrentTitle = newMapTitle.get(language);
			if (Strings.isBlank(newCurrentTitle)) {
				listUnvalidValueForLanguage.add(language);
			}
			for (Map<Language, String> existingTitleMap : titles) {
				String existingTitleInCurrentLanguage = existingTitleMap.get(language);
				if (Strings.isNotBlank(existingTitleInCurrentLanguage) && existingTitleInCurrentLanguage.equals(newCurrentTitle)
					&& (oldValues == null || oldValues != null && !existingTitleInCurrentLanguage.equals(oldValues.get(language)))) {
					listUnvalidValueForLanguage.add(language);
				}
			}
		}
		return listUnvalidValueForLanguage;
	}

	void renameTaxonomyType(Taxonomy taxonomy, boolean wasTitleChanged) {
		if (wasTitleChanged) {
			MetadataSchemaTypesBuilder types = schemasManager.modify(taxonomy.getCollection());
			MetadataSchemaTypeBuilder taxonomyType = types.getSchemaType(taxonomy.getSchemaTypes().get(0));
			MetadataSchemaBuilder defaultSchema = taxonomyType.getDefaultSchema();

			for (Language language : schemasManager.getSchemaTypes(collection).getLanguages()) {
				taxonomyType.addLabel(language, taxonomy.getTitle(language));
				defaultSchema.addLabel(language, taxonomy.getTitle(language));
			}

			try {
				schemasManager.saveUpdateSchemaTypes(types);
			} catch (MetadataSchemasManagerException.OptimisticLocking optimistickLocking) {
				throw new RuntimeException(optimistickLocking);
			}
		}
	}

	void createMetadatasInClassifiedObjects(Taxonomy taxonomy, List<String> classifiedObjects,
											boolean wasTitleChanged) {

		if (classifiedObjects != null) {
			if (classifiedObjects.contains("folderObject")) {
				createOrRenameMetadatasInDefaultSchemaIfInexistent(taxonomy, Folder.SCHEMA_TYPE, wasTitleChanged);
			}

			if (classifiedObjects.contains("documentObject")) {
				createOrRenameMetadatasInDefaultSchemaIfInexistent(taxonomy, Document.SCHEMA_TYPE, wasTitleChanged);
			}
		}
	}

	void createOrRenameMetadatasInDefaultSchemaIfInexistent(Taxonomy taxonomy, String schemaType,
															boolean wasTitleChanged) {

		if (!getClassifiedSchemaTypes(taxonomy).contains(schemaType)) {
			//TODO Patrick - code instead label
			String groupLabel = $("classifiedInGroupLabel");
			valueListServices()
					.createAMultivalueClassificationMetadataInGroup(taxonomy, schemaType, "classifiedInGroupLabel", groupLabel);
		} else if (wasTitleChanged) {
			MetadataSchemaTypesBuilder types = schemasManager.modify(taxonomy.getCollection());
			String localCode = taxonomy.getCode() + "Ref";
			MetadataBuilder metadataBuilder = types.getSchemaType(schemaType).getDefaultSchema().get(localCode);

			for (Language language : schemasManager.getSchemaTypes(collection).getLanguages()) {
				String title = taxonomy.getTitle(language);
				if (title != null) {
					metadataBuilder.addLabel(language, taxonomy.getTitle(language));
				}
			}

			try {
				schemasManager.saveUpdateSchemaTypes(types);
			} catch (MetadataSchemasManagerException.OptimisticLocking optimistickLocking) {
				throw new RuntimeException(optimistickLocking);
			}
		}
	}

	Taxonomy fetchTaxonomy(String taxonomyCode) {
		TaxonomiesManager taxonomiesManager = view.getConstellioFactories().getModelLayerFactory().getTaxonomiesManager();
		return taxonomiesManager.getEnabledTaxonomyWithCode(view.getSessionContext().getCurrentCollection(), taxonomyCode);
	}

	public TaxonomyVO newTaxonomyVO(Taxonomy taxonomy) {
		TaxonomyToVOBuilder voBuilder = new TaxonomyToVOBuilder();
		TaxonomyVO taxonomyVO = voBuilder.build(taxonomy);
		taxonomyVO.setClassifiedObjects(getClassifiedObjects(taxonomy));
		return taxonomyVO;

	}

	List<String> getClassifiedObjects(Taxonomy taxonomy) {
		List<String> classifiedObjects = new ArrayList<>();
		List<String> classifiedTypes = getClassifiedSchemaTypes(taxonomy);
		if (classifiedTypes.contains(Folder.SCHEMA_TYPE)) {
			classifiedObjects.add("folderObject");
		}
		if (classifiedTypes.contains(Document.SCHEMA_TYPE)) {
			classifiedObjects.add("documentObject");
		}
		return classifiedObjects;
	}

	List<String> getClassifiedSchemaTypes(Taxonomy taxonomy) {
		List<MetadataSchemaType> classifiedTypes = valueListServices().getClassifiedSchemaTypes(taxonomy);
		return new SchemaUtils().toSchemaTypeCodes(classifiedTypes);
	}

	ValueListServices valueListServices() {
		return new ValueListServices(appLayerFactory, view.getCollection());
	}

	public void cancelButtonClicked() {
		view.navigate().to().listTaxonomies();
	}

	boolean canCreate(Map<Language, String> taxonomyMapTitle) {
		boolean canCreate = false;
		if (taxonomyMapTitle != null && taxonomyMapTitle.size() > 0) {
			boolean exist = verifyIfExists(taxonomyMapTitle);
			canCreate = !exist;
		}
		return canCreate;
	}

	private boolean verifyIfExists(Map<Language, String> taxonomy) {
		if (titles == null || titles.size() == 0) {
			getTaxonomies();
		}
		boolean exits = false;
		for (Language lang : taxonomy.keySet()) {
			if (Strings.isBlank(taxonomy.get(lang))) {
				return true;
			}
			for (Map<Language, String> existingTitleMap : titles) {
				if (existingTitleMap.get(lang).equals(taxonomy.get(lang))) {
					return true;
				}
			}
		}
		return exits;
	}

	private List<TaxonomyVO> getTaxonomies() {
		titles = new ArrayList<>();
		TaxonomyToVOBuilder builder = new TaxonomyToVOBuilder();
		List<TaxonomyVO> result = new ArrayList<>();
		for (Taxonomy taxonomy : valueListServices().getTaxonomies()) {
			result.add(builder.build(taxonomy));
			titles.add(taxonomy.getTitle());
		}

		return result;
	}

	public void setEditAction(boolean actionEdit) {
		this.actionEdit = actionEdit;
	}

	public boolean isActionEdit() {
		return actionEdit;
	}

	public boolean canEditClassifiedObjects(TaxonomyVO taxonomyVO) {
		return !actionEdit || taxonomyVO.getClassifiedObjects().isEmpty();
	}

	@Override
	protected boolean hasPageAccess(String parameters, User user) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);
		String taxonomyCode = params.get("taxonomyCode");
		if (taxonomyCode == null) {
			return user.has(CorePermissions.MANAGE_TAXONOMIES).globally();
		} else {
			return new TaxonomyPresentersService(appLayerFactory).canManage(taxonomyCode, user);
		}
	}

}
