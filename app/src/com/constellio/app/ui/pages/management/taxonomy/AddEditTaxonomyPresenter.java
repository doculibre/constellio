package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.MetadataSchemasManagerException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;

public class AddEditTaxonomyPresenter extends BasePresenter<AddEditTaxonomyView> {

	transient UserServices userServices;
	private boolean actionEdit = false;
	private List<String> titles;
	private transient TaxonomiesManager taxonomiesManager;
	private transient MetadataSchemasManager schemasManager;
	private transient SchemasDisplayManager schemasDisplayManager;
	private Language language;

	public AddEditTaxonomyPresenter(AddEditTaxonomyView view) {
		super(view);
		language = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
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
		schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
	}

	public void saveButtonClicked(TaxonomyVO taxonomyVO) {
		final Language language1 = Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage());
		if (isActionEdit()) {
			Taxonomy taxonomy = fetchTaxonomy(taxonomyVO.getCode());
			boolean wasTitleChanged = !taxonomy.getTitle(language1).equals(taxonomyVO.getTitle());
			Map<Language, String> languageStringMap = new HashMap<>();
			languageStringMap.put(language1, taxonomyVO.getTitle());
			taxonomy = taxonomy.withTitle(languageStringMap)
					.withUserIds(taxonomyVO.getUserIds())
					.withGroupIds(taxonomyVO.getGroupIds())
					.withVisibleInHomeFlag(taxonomyVO.isVisibleInHomePage());

			renameTaxonomyType(taxonomy, wasTitleChanged);

			createMetadatasInClassifiedObjects(taxonomy, taxonomyVO.getClassifiedObjects(), wasTitleChanged);
			taxonomiesManager.editTaxonomy(taxonomy);
			view.navigate().to().listTaxonomies();
		} else {
			boolean canCreate = canCreate(taxonomyVO.getTitle());
			if (canCreate) {
				List<String> language = appLayerFactory.getCollectionsManager().getCollectionLanguages(collection);
				boolean isMultiLingual = language.size() > 1;

				Map<Language, String> titleMultiLagMap = new HashMap<>();
				titleMultiLagMap.put(language1, taxonomyVO.getTitle());

				Taxonomy taxonomy = valueListServices()
						.createTaxonomy(titleMultiLagMap, taxonomyVO.getUserIds(), taxonomyVO.getGroupIds(),
								taxonomyVO.isVisibleInHomePage(), isMultiLingual);
				createMetadatasInClassifiedObjects(taxonomy, taxonomyVO.getClassifiedObjects(), true);
				view.navigate().to().listTaxonomies();
				titles.add(taxonomyVO.getTitle());
			} else {
				view.showErrorMessage("Taxonomny already exists!");
				return;
			}
		}
	}

	void renameTaxonomyType(Taxonomy taxonomy, boolean wasTitleChanged) {
		if(wasTitleChanged) {
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

	void createMetadatasInClassifiedObjects(Taxonomy taxonomy, List<String> classifiedObjects, boolean wasTitleChanged) {

		if (classifiedObjects != null) {
			if (classifiedObjects.contains("folderObject")) {
				createOrRenameMetadatasInDefaultSchemaIfInexistent(taxonomy, Folder.SCHEMA_TYPE, wasTitleChanged);
			}

			if (classifiedObjects.contains("documentObject")) {
				createOrRenameMetadatasInDefaultSchemaIfInexistent(taxonomy, Document.SCHEMA_TYPE, wasTitleChanged);
			}
		}
	}

	void createOrRenameMetadatasInDefaultSchemaIfInexistent(Taxonomy taxonomy, String schemaType, boolean wasTitleChanged) {

		if (!getClassifiedSchemaTypes(taxonomy).contains(schemaType)) {
			//TODO Patrick - code instead label
			String groupLabel = $("classifiedInGroupLabel");
			valueListServices()
					.createAMultivalueClassificationMetadataInGroup(taxonomy, schemaType, "classifiedInGroupLabel", groupLabel);
		} else if(wasTitleChanged) {
			MetadataSchemaTypesBuilder types = schemasManager.modify(taxonomy.getCollection());
			String localCode = taxonomy.getCode() + "Ref";
			MetadataBuilder metadataBuilder = types.getSchemaType(schemaType).getDefaultSchema().get(localCode);

			for (Language language : schemasManager.getSchemaTypes(collection).getLanguages()) {
				metadataBuilder.addLabel(language, taxonomy.getTitle(language));
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
		TaxonomyVO taxonomyVO = voBuilder.build(taxonomy, Language.withCode(view.getSessionContext().getCurrentLocale().getLanguage()));
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

	boolean canCreate(String taxonomy) {
		taxonomy = taxonomy.trim();
		boolean canCreate = false;
		if (StringUtils.isNotBlank(taxonomy)) {
			boolean exist = verifyIfExists(taxonomy);
			canCreate = !exist;
		}
		return canCreate;
	}

	private boolean verifyIfExists(String taxonomy) {
		if (titles == null) {
			getTaxonomies();
		}
		boolean exits = false;
		for (String title : titles) {
			if (title.equals(taxonomy)) {
				exits = true;
			}
		}
		return exits;
	}

	private List<TaxonomyVO> getTaxonomies() {
		titles = new ArrayList<>();
		TaxonomyToVOBuilder builder = new TaxonomyToVOBuilder();
		List<TaxonomyVO> result = new ArrayList<>();
		for (Taxonomy taxonomy : valueListServices().getTaxonomies()) {
			result.add(builder.build(taxonomy, language));
			titles.add(taxonomy.getTitle(language));
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
