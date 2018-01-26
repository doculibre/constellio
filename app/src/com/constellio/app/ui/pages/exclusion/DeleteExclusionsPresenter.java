package com.constellio.app.ui.pages.exclusion;

import com.constellio.app.services.corrector.CorrectorExcluderManager;
import com.constellio.app.services.corrector.CorrectorExclusion;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.List;

public class DeleteExclusionsPresenter extends BasePresenter<DeleteExclusionsView>{
    private CorrectorExcluderManager correctorExcluderManager;


    public DeleteExclusionsPresenter(DeleteExclusionsView view) {
        super(view);
        correctorExcluderManager = appLayerFactory.getCorrectorExcluderManager();
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public List<ExclusionCollectionVO> getExcluded() {
            List<CorrectorExclusion> exclusionList = correctorExcluderManager.getAllExclusion(collection);
            List<ExclusionCollectionVO> exclusionCollectionVOList = new ArrayList();

            for(CorrectorExclusion exclusion : exclusionList) {
                ExclusionCollectionVO exclusionCollectionVO = new ExclusionCollectionVO();

                exclusionCollectionVO.setCollection(exclusion.getCollection());
                exclusionCollectionVO.setExclusion(exclusion.getExclusion());

                exclusionCollectionVOList.add(exclusionCollectionVO);
            }

        return exclusionCollectionVOList;
    }

    public void deleteExclusion(ExclusionCollectionVO exclusionCollectionVO) {
        CorrectorExclusion correctorExclusion = new CorrectorExclusion();
        correctorExclusion.setExclusion(exclusionCollectionVO.getExclusion())
                .setCollection(exclusionCollectionVO.getCollection());
        correctorExcluderManager.deleteException(correctorExclusion);
    }
}
