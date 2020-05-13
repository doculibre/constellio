package com.constellio.app.modules.rm.ui.components.breadcrumb;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.FavoritesBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.GroupFavoritesBreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.ListContentAccessAndRoleAuthorizationsBreadCrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.SearchResultsBreadcrumbItem;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class FolderDocumentBreadcrumbTrailPresenter implements Serializable {

	private final static Logger LOGGER = LoggerFactory.getLogger(FolderDocumentBreadcrumbTrailPresenter.class);

	private String recordId;

	private String taxonomyCode;

	private String collection;

	private String containerId;

	private String favoritesId;

	private FolderDocumentContainerBreadcrumbTrail breadcrumbTrail;

	private transient TaxonomiesManager taxonomiesManager;

	private transient SchemaPresenterUtils folderPresenterUtils;

	private transient RMSchemasRecordsServices rmSchemasRecordsServices;

	public FolderDocumentBreadcrumbTrailPresenter(String recordId, String taxonomyCode,
												  FolderDocumentContainerBreadcrumbTrail breadcrumbTrail,
												  String containerId) {
		this(recordId, taxonomyCode, breadcrumbTrail, containerId, null);
	}

	public FolderDocumentBreadcrumbTrailPresenter(String recordId, String taxonomyCode,
												  FolderDocumentContainerBreadcrumbTrail breadcrumbTrail,
												  String containerId, String favoritesId) {
		this.recordId = recordId;
		this.taxonomyCode = taxonomyCode;
		this.breadcrumbTrail = breadcrumbTrail;
		this.containerId = containerId;
		this.favoritesId = favoritesId;
		initTransientObjects();
		addBreadcrumbItems();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = breadcrumbTrail.getConstellioFactories();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		SessionContext sessionContext = breadcrumbTrail.getSessionContext();
		collection = sessionContext.getCurrentCollection();

		folderPresenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, breadcrumbTrail);
	}

	private void addBreadcrumbItems() {
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();
		int folderOffSet = 0;
		if (containerId != null) {
			breadcrumbItems.add(new ContainerBreadcrumbItem(containerId));
			folderOffSet = 1;
		}

		breadcrumbItems.addAll(getGetFolderDocumentBreadCrumbItems(recordId, folderPresenterUtils,
				rmSchemasRecordsServices));

		UIContext uiContext = breadcrumbTrail.getUIContext();
		String searchId = uiContext.getAttribute(BaseBreadcrumbTrail.SEARCH_ID);
		Boolean advancedSearch = uiContext.getAttribute(BaseBreadcrumbTrail.ADVANCED_SEARCH);
		String recordAuthorisationsSchemaType = uiContext.getAttribute(BaseBreadcrumbTrail.RECORD_AUTHORIZATIONS_TYPE);
		if (taxonomyCode == null) {
			taxonomyCode = uiContext.getAttribute(BaseBreadcrumbTrail.TAXONOMY_CODE);
		}

		if (taxonomyCode != null && recordId != null) {
			Record record = folderPresenterUtils.getRecord(recordId);
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);

			Taxonomy selectedTaxonomy;
			Taxonomy administrativeUnitTaxonomy = taxonomiesManager.getTaxonomyFor(collection, AdministrativeUnit.SCHEMA_TYPE);
			Taxonomy categoryTaxonomy = taxonomiesManager.getTaxonomyFor(collection, Category.SCHEMA_TYPE);

			Folder folder;
			if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				folder = rmSchemasRecordsServices.wrapFolder(record);
			} else {
				Document document = rmSchemasRecordsServices.wrapDocument(record);
				folder = rmSchemasRecordsServices.getFolder(document.getFolder());
			}
			if (administrativeUnitTaxonomy != null && administrativeUnitTaxonomy.getCode().equals(taxonomyCode)) {
				selectedTaxonomy = administrativeUnitTaxonomy;
				String currentAdministrativeUnitId = folder.getAdministrativeUnit();
				while (currentAdministrativeUnitId != null) {
					AdministrativeUnit currentAdministrativeUnit = rmSchemasRecordsServices.getAdministrativeUnit(currentAdministrativeUnitId);
					breadcrumbItems.add(0, new TaxonomyElementBreadcrumbItem(currentAdministrativeUnitId));
					currentAdministrativeUnitId = currentAdministrativeUnit.getParent();
				}
			} else if (categoryTaxonomy != null && categoryTaxonomy.getCode().equals(taxonomyCode)) {
				SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(categoryTaxonomy.getCode(), breadcrumbTrail.getView().getConstellioFactories(), breadcrumbTrail.getSessionContext());
				selectedTaxonomy = categoryTaxonomy;
				String currentCategoryId = folder.getCategory();
				while (currentCategoryId != null) {
					final String finalCurrentCategoryId = currentCategoryId;
					Category currentCategory = rmSchemasRecordsServices.getCategory(currentCategoryId);
					breadcrumbItems.add(0, new TaxonomyElementBreadcrumbItem(currentCategoryId) {
						//						@Override
						//						public boolean isEnabled() {
						//							Record record = schemaPresenterUtils.getRecord(finalCurrentCategoryId);
						//							User user = schemaPresenterUtils.getCurrentUser();
						//							return user.hasAny(RMPermissionsTo.DISPLAY_CLASSIFICATION_PLAN, RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).on(record);
						//						}
					});
					currentCategoryId = currentCategory.getParent();
				}
			} else {
				try {
					selectedTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
					MetadataSchemaTypes types = rmSchemasRecordsServices.getTypes();
					MetadataSchema schema = types.getSchemaOf(record);
					List<Metadata> taxonomyRelationshipReferences = schema.getTaxonomyRelationshipReferences(selectedTaxonomy);
					if (!taxonomyRelationshipReferences.isEmpty()) {
						Metadata firstTaxonomyRelationshipReference = taxonomyRelationshipReferences.get(0);
						List<String> taxonomyItemReferences = null;
						Object value = record.get(firstTaxonomyRelationshipReference);
						if (value != null && value instanceof String) {
							taxonomyItemReferences = asList((String) value);
						} else {
							taxonomyItemReferences = (List<String>) value;
						}
						if (taxonomyItemReferences != null && !taxonomyItemReferences.isEmpty()) {
							String taxonomyItemReference = taxonomyItemReferences.get(0);
							while (taxonomyItemReference != null) {
								Record taxonomyItem = rmSchemasRecordsServices.get(taxonomyItemReference);
								breadcrumbItems.add(0, new TaxonomyElementBreadcrumbItem(taxonomyItem.getId()));
								taxonomyItemReference = taxonomyItem.getParentId();
							}
						}
					}
				} catch (TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound e) {
					selectedTaxonomy = null;
				}
			}
			if (selectedTaxonomy != null) {
				breadcrumbItems.add(0, new TaxonomyBreadcrumbItem(taxonomyCode, selectedTaxonomy.getTitle(Language.withCode(breadcrumbTrail.getSessionContext().getCurrentLocale().getLanguage()))));
			}
		} else if (favoritesId != null) {
			breadcrumbItems.add(0, new FavoritesBreadcrumbItem());


			String title = favoritesId.equals(breadcrumbTrail.getView().getSessionContext().getCurrentUser().getId()) ? i18n.$("CartView.defaultFavorites") : rmSchemasRecordsServices.getCart(favoritesId).getTitle();


			breadcrumbItems.add(1, new GroupFavoritesBreadcrumbItem(favoritesId, title));
		} else if (searchId != null) {
			breadcrumbItems.add(0, new SearchResultsBreadcrumbItem(searchId, advancedSearch));
		}

		if (recordAuthorisationsSchemaType != null) {
			if (!breadcrumbItems.isEmpty()) {
				BreadcrumbItem breadcrumbItem = breadcrumbItems.get(breadcrumbItems.size() - 1);
				if (breadcrumbItem instanceof FolderBreadCrumbItem) {
					((FolderBreadCrumbItem) breadcrumbItem).setForcedEnabled(true);
				} else if (breadcrumbItem instanceof DocumentBreadCrumbItem) {
					((DocumentBreadCrumbItem) breadcrumbItem).setForcedEnabled(true);
				}
			}
			breadcrumbItems.add(new ListContentAccessAndRoleAuthorizationsBreadCrumbItem(recordId, recordAuthorisationsSchemaType, rmSchemasRecordsServices));
		}

		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	public static List<BreadcrumbItem> getGetFolderDocumentBreadCrumbItems(String currentRecordId,
																		   SchemaPresenterUtils schemaPresenterUtils,
																		   RMSchemasRecordsServices rmSchemasRecordsServices) {

		String baseRecordId = currentRecordId;

		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();
		while (currentRecordId != null) {
			Record currentRecord = schemaPresenterUtils.getRecord(currentRecordId);
			String currentSchemaCode = currentRecord.getSchemaCode();
			String currentSchemaTypeCode = SchemaUtils.getSchemaTypeCode(currentSchemaCode);
			if (Folder.SCHEMA_TYPE.equals(currentSchemaTypeCode)) {
				FolderBreadCrumbItem folderBreadCrumbItem = new FolderBreadCrumbItem(currentRecordId, schemaPresenterUtils,
						baseRecordId);
				breadcrumbItems.add(0, folderBreadCrumbItem);

				Folder folder = rmSchemasRecordsServices.wrapFolder(currentRecord);
				currentRecordId = folder.getParentFolder();
			} else if (Document.SCHEMA_TYPE.equals(currentSchemaTypeCode)) {
				breadcrumbItems.add(new DocumentBreadCrumbItem(currentRecordId, schemaPresenterUtils));

				Document document = rmSchemasRecordsServices.wrapDocument(currentRecord);
				currentRecordId = document.getFolder();
			} else {
				currentRecordId = null;
				LOGGER.error("Unrecognized schema type code : " + currentSchemaTypeCode);
			}
		}

		return breadcrumbItems;
	}

	public boolean itemClicked(BreadcrumbItem item) {
		boolean handled;
		if (item instanceof FolderBreadCrumbItem) {
			handled = true;
			String folderId = ((FolderBreadCrumbItem) item).getFolderId();
			if (favoritesId != null) {
				breadcrumbTrail.navigate().to(RMViews.class).displayFolderFromFavorites(folderId, favoritesId);
			} else {
				breadcrumbTrail.navigate().to(RMViews.class).displayFolder(folderId);
			}
		} else if (item instanceof DocumentBreadCrumbItem) {
			handled = true;
			String documentId = ((DocumentBreadCrumbItem) item).getDocumentId();
			if (favoritesId != null) {
				breadcrumbTrail.navigate().to(RMViews.class).displayDocumentFromFavorites(documentId, favoritesId);
			} else {
				breadcrumbTrail.navigate().to(RMViews.class).displayDocument(documentId);
			}
		} else if (item instanceof TaxonomyElementBreadcrumbItem) {
			handled = true;
			TaxonomyElementBreadcrumbItem taxonomyElementBreadcrumbItem = (TaxonomyElementBreadcrumbItem) item;
			String expandedRecordId = taxonomyElementBreadcrumbItem.getTaxonomyElementId();
			breadcrumbTrail.navigate().to().home(taxonomyCode, expandedRecordId, null);
		} else if (item instanceof TaxonomyBreadcrumbItem) {
			handled = true;
			breadcrumbTrail.navigate().to().home(taxonomyCode, null, null);
		} else if (item instanceof SearchResultsBreadcrumbItem) {
			handled = true;
			SearchResultsBreadcrumbItem searchResultsBreadcrumbItem = (SearchResultsBreadcrumbItem) item;
			String searchId = searchResultsBreadcrumbItem.getSearchId();
			boolean advancedSearch = searchResultsBreadcrumbItem.isAdvancedSearch();
			if (advancedSearch) {
				breadcrumbTrail.navigate().to().advancedSearchReplay(searchId);
			} else {
				breadcrumbTrail.navigate().to().simpleSearchReplay(searchId);
			}
		} else if (item instanceof ContainerBreadcrumbItem) {
			handled = true;
			ContainerBreadcrumbItem containerBreadcrumbItem = (ContainerBreadcrumbItem) item;
			if (favoritesId != null) {
				breadcrumbTrail.navigate().to(RMViews.class).displayDocumentFromFavorites(containerBreadcrumbItem.getContainerId(),
						favoritesId);
			} else {
				breadcrumbTrail.navigate().to(RMViews.class).displayContainer(containerBreadcrumbItem.getContainerId());
			}
		} else if (item instanceof GroupFavoritesBreadcrumbItem) {
			handled = true;
			GroupFavoritesBreadcrumbItem groupFavoritesBreadcrumbItem = (GroupFavoritesBreadcrumbItem) item;
			breadcrumbTrail.navigate().to(RMViews.class).cart(groupFavoritesBreadcrumbItem.getFavoriteGroupId());
		} else if (item instanceof FavoritesBreadcrumbItem) {
			handled = true;
			breadcrumbTrail.navigate().to(RMViews.class).listCarts();
		} else if (item instanceof ListContentAccessAndRoleAuthorizationsBreadCrumbItem) {
			handled = true;
			String recordAuthorisationsSchemaType =
					((ListContentAccessAndRoleAuthorizationsBreadCrumbItem) item).getRecordAuthorizationsSchemaType();
			String recordId = ((ListContentAccessAndRoleAuthorizationsBreadCrumbItem) item).getRecordId();
			if (Document.SCHEMA_TYPE == recordAuthorisationsSchemaType) {
				breadcrumbTrail.navigate().to(RMViews.class).displayDocument(recordId);
			} else if (Folder.SCHEMA_TYPE == recordAuthorisationsSchemaType) {
				breadcrumbTrail.navigate().to(RMViews.class).displayFolder(recordId);
			} else {
				handled = false;
			}
		} else {
			handled = false;
		}
		return handled;
	}

	class ContainerBreadcrumbItem implements BreadcrumbItem {
		private String folderId;

		public ContainerBreadcrumbItem(String containerId) {
			this.folderId = containerId;
		}

		public final String getContainerId() {
			return folderId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(folderId);
		}

		@Override
		public boolean isEnabled() {
			boolean enabled;
			if (folderId.equals(recordId)) {
				enabled = false;
			} else {
				Record record = folderPresenterUtils.getRecord(folderId);
				User user = folderPresenterUtils.getCurrentUser();
				enabled = user.hasReadAccess().on(record);
			}
			return enabled;
		}
	}

	class TaxonomyBreadcrumbItem implements BreadcrumbItem {

		private String taxonomyCode;

		private String taxonomyLabel;

		TaxonomyBreadcrumbItem(String taxonomyCode, String taxonomyLabel) {
			this.taxonomyCode = taxonomyCode;
			this.taxonomyLabel = taxonomyLabel;
		}

		public final String getTaxonomyCode() {
			return taxonomyCode;
		}

		@Override
		public String getLabel() {
			return taxonomyLabel;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

	}

	class TaxonomyElementBreadcrumbItem implements BreadcrumbItem {

		private String taxonomyElementId;

		TaxonomyElementBreadcrumbItem(String taxonomyElementId) {
			this.taxonomyElementId = taxonomyElementId;
		}

		public final String getTaxonomyElementId() {
			return taxonomyElementId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(taxonomyElementId);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

	}
}
