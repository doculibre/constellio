package com.constellio.app.ui.framework.data;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public abstract class FacetsDataProvider extends AbstractDataProvider {
    public long size() {
        SimpleOrderedMap queryS = getQuerySMap(0, 1);

        if(queryS == null || queryS.get("numBuckets") == null) {
            return 0;
        } else {
            return ((Number)queryS.get("numBuckets")).longValue();
        }
    }

    public List<Facets> facetsList(Integer offset, Integer limit) {
        List<Facets> facets = new ArrayList<>();

        List<SimpleOrderedMap> buckets = listBuckets(offset, limit);
        for (int i = 0; i < buckets.size(); i++) {
            SimpleOrderedMap bucket = buckets.get(i);

            String query = (String) bucket.get("val");
            SimpleOrderedMap originalQuery = (SimpleOrderedMap) bucket.get("originalQuery_s");
            if(originalQuery != null  && originalQuery.get("buckets") != null) {
                List<SimpleOrderedMap> originalQueryBuckets = (List<SimpleOrderedMap>) originalQuery.get("buckets");
                if (!originalQueryBuckets.isEmpty() &&
                        StringUtils.isNotBlank((String) originalQueryBuckets.get(0).get("val"))) {
                    query = (String) originalQueryBuckets.get(0).get("val");
                }
            }

            String clickCount = String.valueOf(((Number)bucket.get("clickCount_d")).intValue());
            String frequency = String.valueOf(((Number)bucket.get("count")).intValue());

            List<String> clicks = new ArrayList<>();

            SimpleOrderedMap clicksSs = (SimpleOrderedMap) bucket.get("clicks_ss");
            if(clicksSs != null && clicksSs.get("buckets") != null) {
                List<SimpleOrderedMap> clicksBuckets = (List<SimpleOrderedMap>) clicksSs.get("buckets");
                ListIterator<SimpleOrderedMap> iterator = clicksBuckets.listIterator();
                while(iterator.hasNext()) {
                    clicks.add((String) iterator.next().get("val"));
                }
            }

            facets.add(new Facets(query, clickCount, frequency, clicks));
        }

        return facets;
    }

    private List<SimpleOrderedMap> listBuckets(Integer offset, Integer limit) {
        List<SimpleOrderedMap> buckets = new ArrayList<>();

        SimpleOrderedMap queryS = getQuerySMap(offset, limit);
        if(queryS != null && queryS.get("buckets") != null) {
            buckets.addAll((ArrayList<SimpleOrderedMap>) queryS.get("buckets"));
        }

        return buckets;
    }

    private SimpleOrderedMap getQuerySMap(Integer offset, Integer limit) {
        QueryResponse queryResponse = getQueryResponse(offset, limit);
        NamedList<Object> namedList = queryResponse.getResponse();

        SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
        return (SimpleOrderedMap) facets.get("query_s");
    }

    public abstract QueryResponse getQueryResponse(Integer offset, Integer limit);

    public static class Facets {
        private final String query;
        private final String clickCount;
        private final String frequency;
        private final List<String> clicks;

        public Facets(String query, String clickCount, String frequency, List<String> clicks) {
            this.query = query;
            this.clickCount = clickCount;
            this.frequency = frequency;
            this.clicks = clicks;
        }

        public String getQuery() {
            return query;
        }

        public String getClickCount() {
            return clickCount;
        }

        public String getFrequency() {
            return frequency;
        }

        public List<String> getClicks() {
            return clicks;
        }
    }
}
