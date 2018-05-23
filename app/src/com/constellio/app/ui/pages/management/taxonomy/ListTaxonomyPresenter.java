package com.constellio.app.ui.pages.management.taxonomy;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.metadata.MetadataDeletionException;
import com.constellio.app.services.metadata.MetadataDeletionService;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.ConceptNodesTaxonomySearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListTaxonomyPresenter extends BasePresenter<ListTaxonomyView> {

	private List<String> titles;
	private transient TaxonomiesManager taxonomiesManager;
	private transient MetadataDeletionService metadataDeletionService;

	public ListTaxonomyPresenter(ListTaxonomyView view) {
		super(view);
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
	}

	public List<TaxonomyVO> getTaxonomies() {
		titles = new ArrayList<>();
		TaxonomyToVOBuilder builder = new TaxonomyToVOBuilder();
		User user = getCurrentUser();
		TaxonomyPresentersService presentersService = new TaxonomyPresentersService(appLayerFactory);
		List<TaxonomyVO> result = new ArrayList<>();
		for (Taxonomy taxonomy : valueListServices().getTaxonomies()) {
			if (presentersService.canManage(taxonomy.getCode(), user) && presentersService.displayTaxonomy(taxonomy.getCode(),
					user)) {
				result.add(builder.build(taxonomy));
				titles.add(taxonomy.getTitle());
			}
		}
		return result;
	}

	ValueListServices valueListServices() {
		return new ValueListServices(appLayerFactory, view.getCollection());
	}

	public void addButtonClicked() {
		view.navigate().to().addTaxonomy();
	}

	public void editButtonClicked(String taxonomyCode) {
		view.navigate().to().editTaxonomy(taxonomyCode);
	}

	public void displayButtonClicked(TaxonomyVO taxonomy) {
		view.navigate().to().taxonomyManagement(taxonomy.getCode());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_TAXONOMIES).globally();
	}

	public void deleteButtonClicked(String taxonomyCode) throws MetadataDeletionException {
		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
		if (hasConcepts(taxonomy)) {
			view.showMessage($("ListTaxonomyView.cannotDeleteTaxonomy"));
		}else{
			deleteMetadatasInClassifiedObjects(taxonomy);
			taxonomiesManager.deleteWithoutValidations(taxonomy);
			view.navigate().to().listTaxonomies();
		}
	}

	private void deleteMetadatasInClassifiedObjects(Taxonomy taxonomy) throws MetadataDeletionException {
		String localFolderCode =  Folder.DEFAULT_SCHEMA +"_"+ taxonomy.getCode() + "Ref";
		String localDocumentCode =  Document.DEFAULT_SCHEMA +"_"+ taxonomy.getCode() + "Ref";
		MetadataSchema defaultFolderSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getDefaultSchema(Folder.SCHEMA_TYPE);
		MetadataSchema defaultDocumentSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getDefaultSchema(Document.SCHEMA_TYPE);

		if(defaultFolderSchema.metadataExists(localFolderCode)){
			metadataDeletionService().deleteMetadata(localFolderCode);
		}
		if(defaultDocumentSchema.metadataExists(localDocumentCode)){
			metadataDeletionService().deleteMetadata(localDocumentCode);
		}
	}

	private boolean hasConcepts(Taxonomy taxonomy){
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchQuery query = new ConceptNodesTaxonomySearchServices(modelLayerFactory)
				.getRootConceptsQuery(view.getSessionContext().getCurrentCollection(), taxonomy.getCode(),
						new TaxonomiesSearchOptions());
		Long numberOfConcepts = searchServices.getResultsCount(query);
		return !numberOfConcepts.equals(0L);
	}

	private MetadataDeletionService metadataDeletionService() {
		if (metadataDeletionService == null) {
			this.metadataDeletionService = new MetadataDeletionService(appLayerFactory, collection);
		}
		return metadataDeletionService;
	}
}
