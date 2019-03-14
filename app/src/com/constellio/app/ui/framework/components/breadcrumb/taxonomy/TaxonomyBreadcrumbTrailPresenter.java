package com.constellio.app.ui.framework.components.breadcrumb.taxonomy;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbTrail;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyPresentersService;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.records.RecordUtils.parentPaths;

public class TaxonomyBreadcrumbTrailPresenter implements Serializable {

	private String collection;

	private String taxonomyCode;

	private String conceptId;

	private BreadcrumbTrail breadcrumbTrail;

	private transient TaxonomiesManager taxonomiesManager;

	private transient SchemaPresenterUtils taxonomyPresenterUtils;


	public TaxonomyBreadcrumbTrailPresenter(String taxonomyCode, String conceptId, BreadcrumbTrail breadcrumbTrail) {
		this.taxonomyCode = taxonomyCode;
		this.conceptId = conceptId;
		this.breadcrumbTrail = breadcrumbTrail;

		collection = breadcrumbTrail.getSessionContext().getCurrentCollection();

		initTransientObjects();
		addBreadcrumbItems();
	}

	private void addBreadcrumbItems() {
		List<BreadcrumbItem> breadcrumbItems = new ArrayList<>();

		breadcrumbItems.add(new TaxonomyRootBreadcrumbItem());

		String[] pathParts = ((String) parentPaths(taxonomyPresenterUtils.getRecord(conceptId)).get(0)).split("/");
		for (int i = 0; i < pathParts.length; i++) {
			String pathPart = pathParts[i];
			if (!taxonomyCode.equals(pathPart) && StringUtils.isNotBlank(pathPart)) {
				breadcrumbItems.add(new TaxonomyBreadcrumbItem(pathPart));
			}
		}
		breadcrumbItems.add(new TaxonomyBreadcrumbItem(conceptId));

		for (BreadcrumbItem breadcrumbItem : breadcrumbItems) {
			breadcrumbTrail.addItem(breadcrumbItem);
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ConstellioFactories constellioFactories = breadcrumbTrail.getConstellioFactories();
		SessionContext sessionContext = breadcrumbTrail.getSessionContext();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		String schema = recordServices.getDocumentById(conceptId).getSchemaCode();
		taxonomyPresenterUtils = new SchemaPresenterUtils(schema, constellioFactories, sessionContext);
	}

	public boolean itemClicked(BreadcrumbItem item) {
		boolean handled;
		if (item instanceof TaxonomyBreadcrumbItem) {
			handled = true;
			String conceptId = ((TaxonomyBreadcrumbItem) item).getConceptId();
			breadcrumbTrail.navigate().to().taxonomyManagement(taxonomyCode, conceptId);
		} else if (item instanceof TaxonomyRootBreadcrumbItem) {
			handled = true;
			breadcrumbTrail.navigate().to().taxonomyManagement(taxonomyCode);
		} else {
			handled = false;
		}
		return handled;
	}

	class TaxonomyBreadcrumbItem implements BreadcrumbItem {

		private String conceptId;

		TaxonomyBreadcrumbItem(String conceptId) {
			this.conceptId = conceptId;
		}

		public final String getConceptId() {
			return conceptId;
		}

		@Override
		public String getLabel() {
			return SchemaCaptionUtils.getCaptionForRecordId(conceptId, breadcrumbTrail.getSessionContext().getCurrentLocale());
		}

		@Override
		public boolean isEnabled() {
			boolean enabled;
			TaxonomyPresentersService taxonomyPresentersService = new TaxonomyPresentersService(
					breadcrumbTrail.getConstellioFactories().getAppLayerFactory());
			if (conceptId.equals(TaxonomyBreadcrumbTrailPresenter.this.conceptId)) {
				enabled = false;
			} else {
				User user = taxonomyPresenterUtils.getCurrentUser();
				enabled = taxonomyPresentersService.canManage(taxonomyCode, user) ||
					taxonomyPresentersService.canConsult(taxonomyCode, user);
			}
			return enabled;
		}

	}

	class TaxonomyRootBreadcrumbItem implements BreadcrumbItem {

		@Override
		public String getLabel() {
			return taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode).getTitle(Language.withCode(breadcrumbTrail.getSessionContext().getCurrentLocale().getLanguage()));
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

	}

}
