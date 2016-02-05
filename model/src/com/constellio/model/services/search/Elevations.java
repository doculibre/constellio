package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;

@XmlRootElement
public class Elevations {
	public void addOrUpdate(QueryElevation queryElevation) {
		boolean updated = false;
		for (QueryElevation currentQueryElevation : this.queryElevations) {
			if (currentQueryElevation.getQuery().equals(queryElevation.getQuery())) {
				currentQueryElevation.addUpdate(queryElevation.getDocElevations());
				updated = true;
				break;
			}
		}
		if (!updated) {
			this.queryElevations.add(queryElevation);
		}
	}

	public void removeCollectionElevations(SchemasRecordsServices schemas) {
		for (Iterator<QueryElevation> iterator = this.queryElevations.iterator(); iterator.hasNext(); ) {
			QueryElevation queryElevation = iterator.next();
			queryElevation.removeCollectionElevations(schemas);

			if (queryElevation.getDocElevations().isEmpty()) {
				iterator.remove();
			}
		}
	}

	public void removeCollectionElevation(SchemasRecordsServices schemas, String query) {
		for (Iterator<QueryElevation> iterator = this.queryElevations.iterator(); iterator.hasNext(); ) {
			QueryElevation queryElevation = iterator.next();
			if (queryElevation.getQuery().equals(query)) {
				queryElevation.removeCollectionElevations(schemas);
				if (queryElevation.getDocElevations().isEmpty()) {
					iterator.remove();
				}
			}
		}
	}

	public void removeElevation(String query) {
		for (Iterator<QueryElevation> iterator = this.queryElevations.iterator(); iterator.hasNext(); ) {
			QueryElevation queryElevation = iterator.next();
			if (queryElevation.getQuery().equals(query)) {
				iterator.remove();
			}
		}
	}

	public Elevations getCollectionElevations(SchemasRecordsServices schemas) {
		Elevations returnElevation = new Elevations();
		for (QueryElevation queryElevation : this.queryElevations) {
			List<DocElevation> currentDocElevation = getCollectionElevation(schemas, queryElevation.getQuery());
			returnElevation.addQueryElevation(queryElevation.getQuery(), currentDocElevation);
		}
		return returnElevation;
	}

	private void addQueryElevation(String query, List<DocElevation> docElevations) {
		this.queryElevations.add(new QueryElevation().setQuery(query).setDocElevations(docElevations));
	}

	public List<DocElevation> getCollectionElevation(SchemasRecordsServices schemas, String query) {
		List<DocElevation> returnList = new ArrayList<>();
		QueryElevation queryElevation = getQueryElevation(query);
		if (queryElevation != null) {
			for (DocElevation docElevation : queryElevation.getDocElevations()) {
				if (docElevation.isInCollection(schemas)) {
					returnList.add(docElevation);
				}
			}
		}
		return returnList;
	}

	public static class QueryElevation {
		private String query;

		public void addUpdate(List<DocElevation> newDocElevations) {
			for (DocElevation newDocElevation : newDocElevations) {
				for (Iterator<DocElevation> iterator = this.docElevations.iterator(); iterator.hasNext(); ) {
					DocElevation oldDocElevation = iterator.next();
					if (oldDocElevation.getId().equals(newDocElevation.getId())) {
						iterator.remove();
					}
				}
			}
			this.docElevations.addAll(newDocElevations);
		}

		public QueryElevation addDocElevation(DocElevation docElevation) {
			this.docElevations.add(docElevation);
			return this;
		}

		public void removeCollectionElevations(SchemasRecordsServices schemas) {
			for (Iterator<DocElevation> iterator = this.docElevations.iterator(); iterator.hasNext(); ) {
				DocElevation docElevation = iterator.next();
				if (docElevation.isInCollection(schemas)) {
					iterator.remove();
				}
			}
		}

		public QueryElevation setDocElevations(List<DocElevation> docElevations) {
			this.docElevations = docElevations;
			return this;
		}

		public static class DocElevation {
			private String id;
			private boolean exclude;

			public DocElevation() {
			}

			public DocElevation(String id, boolean exclude) {
				this.id = id;
				this.exclude = exclude;
			}

			@XmlAttribute(name = "id")
			public String getId() {
				return id;
			}

			@XmlAttribute(name = "exclude")
			public boolean isExclude() {
				return exclude;
			}

			public void setExclude(boolean exclude) {
				this.exclude = exclude;
			}

			public void setId(String id) {
				this.id = id;
			}

			public boolean equals(Object obj) {
				return EqualsBuilder.reflectionEquals(this, obj);
			}

			public boolean isInCollection(SchemasRecordsServices schemas) {
				try {
					//FIXME Francis (1) on ne devrait pas retouner un record d une autre collection vu que l on passe par schema et (2)ca devrait etre le cas aussi avec la cache
					Record record = schemas.get(this.id);
					if (record.getCollection().equals(schemas.getCollection())) {
						return true;
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					//record not in collection
				}
				return false;
			}

		}

		private List<DocElevation> docElevations = new ArrayList<>();

		public QueryElevation() {
		}

		public QueryElevation(String query) {
			this.query = query;
		}

		@XmlElement(name = "doc")
		public List<DocElevation> getDocElevations() {
			return docElevations;
		}

		@XmlAttribute(name = "text")
		public String getQuery() {
			return query;
		}

		public QueryElevation setQuery(String query) {
			this.query = query;
			return this;
		}

		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
	}

	private List<QueryElevation> queryElevations = new ArrayList<>();

	@XmlElement(name = "query")
	public List<QueryElevation> getQueryElevations() {
		return queryElevations;
	}

	public QueryElevation getQueryElevation(String query) {
		for (QueryElevation queryElevation : queryElevations) {
			if (queryElevation.getQuery().equals(query)) {
				return queryElevation;
			}
		}
		return null;
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
