package com.constellio.app.ui.pages.spellchecker;

import com.constellio.app.services.corrector.CorrectorExcluderManager;
import com.constellio.app.services.corrector.CorrectorExclusion;
import com.constellio.app.ui.entities.ExclusionCollectionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.List;

public class DeleteSpellCheckerExclusionsPresenter extends BasePresenter<DeleteSpellCheckerExclusionsView> {
	private CorrectorExcluderManager correctorExcluderManager;


	public DeleteSpellCheckerExclusionsPresenter(DeleteSpellCheckerExclusionsView view) {
		super(view);
		correctorExcluderManager = appLayerFactory.getCorrectorExcluderManager();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return getCurrentUser().has(CorePermissions.DELETE_CORRECTION_SUGGESTION).globally();
	}

	public List<ExclusionCollectionVO> getExcluded() {
		List<CorrectorExclusion> exclusionList = correctorExcluderManager.getAllExclusion(collection);
		List<ExclusionCollectionVO> exclusionCollectionVOList = new ArrayList<>();

		for (CorrectorExclusion exclusion : exclusionList) {
			ExclusionCollectionVO exclusionCollectionVO = new ExclusionCollectionVO();

			exclusionCollectionVO.setCollection(exclusion.getCollection());
			exclusionCollectionVO.setExclusion(exclusion.getExclusion());

			exclusionCollectionVOList.add(exclusionCollectionVO);
		}

		return exclusionCollectionVOList;
	}

	public void deleteExclusionButtonClicked(ExclusionCollectionVO exclusionCollectionVO) {
		CorrectorExclusion correctorExclusion = new CorrectorExclusion();
		correctorExclusion.setExclusion(exclusionCollectionVO.getExclusion())
						  .setCollection(exclusionCollectionVO.getCollection());
		correctorExcluderManager.deleteException(correctorExclusion);
		view.navigate().to().deleteExclusions();
	}

	public void backButtonClicked() {
		view.navigate().to().searchConfiguration();
	}

}
