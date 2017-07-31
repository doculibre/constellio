package com.constellio.app.ui.framework.data.trees;

import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

import java.util.ArrayList;

public class SmbRecordTreeNodesDataProvider implements RecordTreeNodesDataProvider {
    String taxonomyCode;

    public SmbRecordTreeNodesDataProvider(String taxonomieCode) {
        taxonomyCode = taxonomieCode;
    }

    @Override
    public LinkableTaxonomySearchResponse getChildrenNodes(String recordId, int start, int maxSize, FastContinueInfos infos) {

        return null;
    }

    @Override
    public LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize, FastContinueInfos infos) {

        // Get the root that does'nt have a parent.


        return new LinkableTaxonomySearchResponse(0, infos, new ArrayList<TaxonomySearchRecord>());
    }

    @Override
    public String getTaxonomyCode() {
        return taxonomyCode;
    }
}
