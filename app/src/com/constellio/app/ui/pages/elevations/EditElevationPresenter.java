package com.constellio.app.ui.pages.elevations;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.Elevations;
import com.constellio.model.services.search.SearchConfigurationsManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditElevationPresenter extends BasePresenter<EditElevationView> {
    SearchConfigurationsManager searchConfigurationsManager;

    public EditElevationPresenter(EditElevationView view) {
        super(view);
        searchConfigurationsManager = modelLayerFactory.getSearchConfigurationsManager();
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public List<String> getAllQuery() {
        return searchConfigurationsManager.getAllQuery();
    }

    public List<Elevations.QueryElevation.DocElevation> getElevations(String query) {
        List<Elevations.QueryElevation.DocElevation> docElevations = searchConfigurationsManager.getDocElevation(query);
        List<Elevations.QueryElevation.DocElevation> docElevationExcluded = new ArrayList<>();

        for(Iterator<Elevations.QueryElevation.DocElevation> iterator = docElevations.iterator(); iterator.hasNext();) {
            Elevations.QueryElevation.DocElevation docElevation = iterator.next();
            if(!docElevation.isExclude()) {
                docElevationExcluded.add(docElevation);
            }
        }

        return docElevationExcluded;
    }


    public List<Elevations.QueryElevation.DocElevation> getExclusions(String query) {
        List<Elevations.QueryElevation.DocElevation> docElevations = searchConfigurationsManager.getDocElevation(query);
        List<Elevations.QueryElevation.DocElevation> docElevationExcluded = new ArrayList<>();

        for(Iterator<Elevations.QueryElevation.DocElevation> iterator = docElevations.iterator(); iterator.hasNext();) {
            Elevations.QueryElevation.DocElevation docElevation = iterator.next();
            if(docElevation.isExclude()) {
                docElevationExcluded.add(docElevation);
            }
        }

        return docElevationExcluded;
    }
}
