package com.constellio.app.ui.framework.data;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.LookupTreeDataProvider;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

public class RecordLookupTreeDataProvider extends BaseRecordTreeDataProvider implements LookupTreeDataProvider<String> {
	
	private String schemaTypeCode;
	private Map<String, Boolean> selectableCache = new HashMap<>();
	private boolean ignoreLinkability;
	private boolean writeAccess;

	public RecordLookupTreeDataProvider(String schemaTypeCode, String taxonomyCode, boolean writeAccess) {
		super(new LinkableRecordTreeNodesDataProvider(taxonomyCode, schemaTypeCode, writeAccess));
		this.writeAccess = writeAccess;
		this.schemaTypeCode = schemaTypeCode;
		ignoreLinkability = false;

	}

	public RecordLookupTreeDataProvider(String schemaTypeCode, boolean writeAccess,
			RecordTreeNodesDataProvider recordTreeNodesDataProvider) {
		super(recordTreeNodesDataProvider);
		this.writeAccess = writeAccess;
		this.schemaTypeCode = schemaTypeCode;
		ignoreLinkability = false;
	}

	protected void saveResultInCache(TaxonomySearchRecord searchRecord) {
		super.saveResultInCache(searchRecord);
		boolean selectable = ignoreLinkability || searchRecord.isLinkable();
		selectableCache.put(searchRecord.getId(), selectable);
	}

	@Override
	public boolean isSelectable(String selection) {
		return selectableCache.get(selection);
	}

	@Override
	public TextInputDataProvider<String> search() {
		return new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, writeAccess, true);
	}

	@Override
	public TextInputDataProvider<String> searchWithoutDisabled() {
		return new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, writeAccess, false);
	}

	public void setIgnoreLinkability(boolean ignoreLinkability) {
		this.ignoreLinkability = ignoreLinkability;
		if (nodesDataProvider instanceof LinkableRecordTreeNodesDataProvider) {
			((LinkableRecordTreeNodesDataProvider) nodesDataProvider).setIgnoreLinkability(ignoreLinkability);
		}
	}

}
