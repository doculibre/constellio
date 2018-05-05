package com.constellio.app.ui.pages.elevations;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.Elevations;
import com.constellio.model.services.search.SearchConfigurationsManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditElevationPresenter extends BasePresenter<EditElevationView> {
    SearchConfigurationsManager searchConfigurationsManager;
    SchemasRecordsServices schemasRecordsServices;

    public EditElevationPresenter(EditElevationView view) {
        super(view);
        searchConfigurationsManager = modelLayerFactory.getSearchConfigurationsManager();
        schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public List<String> getAllQuery() {
        return searchConfigurationsManager.getAllQuery(collection);
    }

    public List<Elevations.QueryElevation.DocElevation> getElevations(String query) {
        List<Elevations.QueryElevation.DocElevation> docElevations = searchConfigurationsManager.getDocElevation(collection, query);
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
        List<Elevations.QueryElevation.DocElevation> docElevations = searchConfigurationsManager.getDocElevation(collection, query);
        List<Elevations.QueryElevation.DocElevation> docElevationExcluded = new ArrayList<>();

        for(Iterator<Elevations.QueryElevation.DocElevation> iterator = docElevations.iterator(); iterator.hasNext();) {
            Elevations.QueryElevation.DocElevation docElevation = iterator.next();
            if(docElevation.isExclude()) {
                docElevationExcluded.add(docElevation);
            }
        }

        return docElevationExcluded;
    }

    public void cancelQueryElevationAndExclusionButtonClicked(String query) {
        searchConfigurationsManager.removeQuery(collection, query);
        view.navigate().to().editElevation();
    }

    public void cancelQueryExclusionButtonClicked(String query) {
        searchConfigurationsManager.removeAllExclusion(collection, query);
        view.navigate().to().editElevation();
    }

    public void cancelQueryElevationButtonClicked(String query) {
        searchConfigurationsManager.removeAllElevation(collection, query);
        view.navigate().to().editElevation();
    }

    public void cancelDocElevationButtonClicked(Elevations.QueryElevation.DocElevation docElevation) {
        searchConfigurationsManager.removeElevated(collection, docElevation.getQuery(), docElevation.getId());
        view.navigate().to().editElevation();
    }

    public String getRecordTitle(String id) {
        Record record;
        String title;
        try {
            record = schemasRecordsServices.get(id);
            title = record.getTitle();
        } catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
            title = id + " " + $("EditElevationView.notARecord");
        }
        return title;
    }

	public void backButtonClicked() {
		view.navigate().to().searchConfiguration();
	}
	
}
