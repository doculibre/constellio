package com.constellio.model.services.search;

import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueryElevation implements Serializable {
	private String query;
	private List<DocElevation> docElevations;

	public QueryElevation(String query) {
		this.query = query;
		this.docElevations = new ArrayList<>();
	}

	@XmlElement(name = "doc")
	public List<DocElevation> getDocElevations() {
		return docElevations;
	}

	@XmlAttribute(name = "text")
	public String getQuery() {
		return query;
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public void addUpdate(List<DocElevation> docElevations) {
		for (DocElevation newDocElevation : docElevations) {
			for (Iterator<DocElevation> iterator = this.docElevations.iterator(); iterator.hasNext(); ) {
				DocElevation oldDocElevation = iterator.next();
				if (oldDocElevation.getId().equals(newDocElevation.getId())) {
					iterator.remove();
				}
			}
		}

		this.docElevations.addAll(docElevations);
	}

	public QueryElevation addDocElevation(DocElevation docElevation) {
		this.docElevations.add(docElevation);
		return this;
	}

	public static class DocElevation implements Serializable {
		private String id;
		private String query;

		public DocElevation(String id, String query) {
			this.id = id;
			this.query = query;
		}

		public String getQuery() {
			return query;
		}

		@XmlAttribute(name = "id")
		public String getId() {
			return id;
		}

		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
	}
}
