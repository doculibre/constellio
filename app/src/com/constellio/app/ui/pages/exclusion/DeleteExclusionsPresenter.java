package com.constellio.app.ui.pages.exclusion;

import com.constellio.app.services.corrector.CorrectorExcluderManager;
import com.constellio.app.services.corrector.CorrectorExclusion;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.collection.CollectionGroupView;
import com.constellio.app.ui.pages.elevations.EditElevationView;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import java.util.ArrayList;
import java.util.HashMap;
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

    public HashMap<String, List<ExclusionCollectionVO>> getExcluded() {
        List<String> collectionList = correctorExcluderManager.getCollectionListExceptSystem();

        HashMap<String, List<ExclusionCollectionVO>> collectionCorrectorExclusion = new HashMap<>();


        for(String collection : collectionList) {
            List<CorrectorExclusion> exclusionList = correctorExcluderManager.getAllExclusion(collection);
            List<ExclusionCollectionVO> correctorExcluderManagerList = new ArrayList<>();

            for(CorrectorExclusion exclusion : exclusionList) {
                ExclusionCollectionVO exclusionCollectionVO = new ExclusionCollectionVO();

                exclusionCollectionVO.setCollection(exclusion.getCollection());
                exclusionCollectionVO.setExclusion(exclusion.getExclusion());

                correctorExcluderManagerList.add(exclusionCollectionVO);
            }

            collectionCorrectorExclusion.put(collection, correctorExcluderManagerList);

            ExclusionCollectionVO exclusionCollectionVO = new ExclusionCollectionVO();
            exclusionCollectionVO.setCollection(collection);
        }

        return collectionCorrectorExclusion;
    }
}
