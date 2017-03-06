package com.constellio.app.ui.framework.components.breadcrumb.taxonomy;

import static com.constellio.model.services.records.RecordUtils.parentPaths;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbItem;
import com.constellio.app.ui.framework.components.breadcrumb.BreadcrumbTrail;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyPresentersService;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

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

	public void itemClicked(BreadcrumbItem item) {
		if (item instanceof TaxonomyBreadcrumbItem) {
			String conceptId = ((TaxonomyBreadcrumbItem) item).getConceptId();
			breadcrumbTrail.navigateTo().taxonomyManagement(taxonomyCode, conceptId);
		} else if (item instanceof TaxonomyRootBreadcrumbItem) {
			breadcrumbTrail.navigateTo().taxonomyManagement(taxonomyCode);
		} else {
			throw new IllegalArgumentException("Unrecognized item type : " + item.getClass());
		}
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
			return SchemaCaptionUtils.getCaptionForRecordId(conceptId);
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
				enabled = taxonomyPresentersService.canManage(taxonomyCode, user);
			}
			return enabled;
		}

	}

	class TaxonomyRootBreadcrumbItem implements BreadcrumbItem {

		@Override
		public String getLabel() {
			return taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode).getTitle();
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

	}

}
