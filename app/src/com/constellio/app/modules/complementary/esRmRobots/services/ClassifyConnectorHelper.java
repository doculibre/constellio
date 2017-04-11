package com.constellio.app.modules.complementary.esRmRobots.services;

import static java.util.Arrays.asList;

import java.util.Iterator;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;

public class ClassifyConnectorHelper {

	RecordServices recordServices;

	public ClassifyConnectorHelper(RecordServices recordServices) {
		this.recordServices = recordServices;
	}

	public static class ClassifiedRecordPathInfo {

		Record conceptWhereRecordIsCreated;

		String lastPathSegment;

		public ClassifiedRecordPathInfo(Record conceptWhereRecordIsCreated, String lastPathSegment) {
			this.conceptWhereRecordIsCreated = conceptWhereRecordIsCreated;
			this.lastPathSegment = lastPathSegment;
		}

		public Record getConceptWhereRecordIsCreated() {
			return conceptWhereRecordIsCreated;
		}

		public String getLastPathSegment() {
			return lastPathSegment;
		}

		@Override
		public String toString() {
			return "ClassifiedRecordPathInfo{" +
					"conceptWhereRecordIsCreated=" + (conceptWhereRecordIsCreated == null ?
					null :
					conceptWhereRecordIsCreated.getId()) +
					", lastPathSegment='" + lastPathSegment + '\'' +
					'}';
		}
	}

	/**
	 * Return the last segment of the path, and the concept where the record should be created
	 *
	 * @param path
	 * @param pathPrefix
	 * @param delimiter
	 * @param codeMetadata
	 * @return
	 */
	public ClassifiedRecordPathInfo extractInfoFromPath(String path, String pathPrefix, String delimiter,
			Metadata codeMetadata) {

		String[] rawPathParts = path.replace(pathPrefix, "").split("/");

		boolean taxonomyStarted = false;
		boolean stillInTaxonomy = true;
		Record conceptOfPreviousSegment = null;

		for (Iterator<String> iterator = asList(rawPathParts).iterator(); iterator.hasNext(); ) {
			String rawPathPart = iterator.next();
			String pathPart = rawPathPart;

			Record conceptOfCurrentSegment = null;
			if (stillInTaxonomy && delimiter != null && rawPathPart.contains(delimiter)) {
				String firstPart = rawPathPart.split(delimiter)[0];
				conceptOfCurrentSegment = recordServices.getRecordByMetadata(codeMetadata, firstPart);

				if (conceptOfCurrentSegment != null) {
					pathPart = firstPart;
				} else {
					stillInTaxonomy = false;
				}
			} else if (stillInTaxonomy) {
				conceptOfCurrentSegment = recordServices.getRecordByMetadata(codeMetadata, pathPart);
				if (taxonomyStarted) {
					stillInTaxonomy = conceptOfCurrentSegment != null;
				}
			}

			if (conceptOfCurrentSegment != null) {
				taxonomyStarted = true;
				if (!isDetectedConceptInHisParent(conceptOfPreviousSegment, conceptOfCurrentSegment)) {
					//The current segment is the code of concept, but this concept is not a child of the last segment's concept
					//This segment is considered as a regular "folder" outside of the taxonomy
					conceptOfCurrentSegment = null;
					pathPart = rawPathPart;
					stillInTaxonomy = false;
				}
			}

			if (!iterator.hasNext()) {
				if (conceptOfCurrentSegment == null) {
					return new ClassifiedRecordPathInfo(conceptOfPreviousSegment, pathPart);
				} else {
					return null;
				}
			}

			conceptOfPreviousSegment = stillInTaxonomy ? conceptOfCurrentSegment : null;
		}

		return null;
	}

	private boolean isDetectedConceptInHisParent(Record parentConcept, Record concept) {
		if (parentConcept == null) {
			if (concept.getParentId() != null) {
				return false;
			}
		} else if (!parentConcept.getId().equals(concept.getParentId())) {
			return false;
		}
		return true;
	}

}
