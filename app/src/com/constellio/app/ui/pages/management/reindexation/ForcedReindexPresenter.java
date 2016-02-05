package com.constellio.app.ui.pages.management.reindexation;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ForcedReindexPresenter extends BasePresenter<ForcedReindexView> {
	public ForcedReindexPresenter(ForcedReindexView view) {
		super(view);
	}

	public void reindex(String rawHashes) {
		List<String> hashes = Arrays.asList(rawHashes.split("\n"));
		ModelLayerFactory modelLayerFactory = view.getConstellioFactories().getModelLayerFactory();
		for (String hash : hashes) {
			modelLayerFactory.getContentManager().reparse(hash);
		}
		view.reindexFinished();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.isSystemAdmin();
	}
}
