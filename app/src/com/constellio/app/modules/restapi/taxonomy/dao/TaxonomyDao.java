package com.constellio.app.modules.restapi.taxonomy.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.taxonomies.queryHandlers.GetChildrenContext;
import com.constellio.model.services.taxonomies.queryHandlers.TaxonomiesSearchServicesSummaryCacheQueryHandler;

import javax.annotation.PostConstruct;
import java.util.List;

public class TaxonomyDao extends BaseDao {

	private TaxonomiesSearchServicesSummaryCacheQueryHandler summaryCacheQueryHandler;
	private TaxonomiesManager taxonomiesManager;

	@PostConstruct
	protected void init() {
		super.init();

		summaryCacheQueryHandler = new TaxonomiesSearchServicesSummaryCacheQueryHandler(modelLayerFactory);
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
	}

	public List<Taxonomy> getTaxonomies(User user, String schemaType) {
		return schemaType != null ?
			   taxonomiesManager.getAvailableTaxonomiesForSelectionOfType(schemaType, user, metadataSchemasManager) :
			   taxonomiesManager.getAvailableTaxonomiesInHomePage(user);
	}

	public List<TaxonomySearchRecord> getTaxonomyNodes(Taxonomy taxonomy, Record parent, User user,
													   MetadataSchemaType schemaType, Integer rowsStart,
													   Integer rowsLimit, Boolean requireWriteAccess) {
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions();
		if (rowsStart != null && rowsLimit != null) {
			options.setStartRow(rowsStart);
			options.setRows(rowsLimit);
		}
		if (Boolean.TRUE.equals(requireWriteAccess)) {
			options.setRequiredAccess(Role.WRITE);
		}

		GetChildrenContext ctx = new GetChildrenContext(user, parent, options, schemaType, taxonomy, modelLayerFactory);
		return summaryCacheQueryHandler.getNodes(ctx).getRecords();
	}

	public Taxonomy getTaxonomyByCode(String collection, String taxonomyCode) {
		try {
			return taxonomiesManager.getEnabledTaxonomyWithCode(collection, taxonomyCode);
		} catch (TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound e) {
			return null;
		}
	}

	public Taxonomy getTaxonomyBySchemaTypeCode(String collection, String schemaTypeCode) {
		try {
			return taxonomiesManager.getTaxonomyFor(collection, schemaTypeCode);
		} catch (TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound e) {
			return null;
		}
	}

	public boolean areSummaryCacheLoaded() {
		return recordServices.getRecordsCaches().areSummaryCachesInitialized();
	}
}
