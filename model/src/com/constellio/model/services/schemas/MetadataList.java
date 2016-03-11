package com.constellio.model.services.schemas;

import static com.constellio.data.utils.LangUtils.compareStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;

public class MetadataList implements List<Metadata> {

	boolean readOnly = false;
	List<Metadata> nestedList = new ArrayList<>();
	Map<String, Metadata> localCodeIndex = new HashMap<>();
	Map<String, Metadata> codeIndex = new HashMap<>();
	Map<String, Metadata> datastoreCodeIndex = new HashMap<>();

	public MetadataList() {
		super();
	}

	public MetadataList(Metadata... metadatas) {
		super();
		addAll(Arrays.asList(metadatas));
	}

	public MetadataList(Collection<? extends Metadata> collection) {
		super();
		addAll(collection);
	}

	@Override
	public int size() {
		return nestedList.size();
	}

	@Override
	public boolean isEmpty() {
		return nestedList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Metadata) {
			String code = codeOf((Metadata) o);
			return codeIndex.containsKey(code);
		} else {
			return false;
		}
	}

	@Override
	public Iterator<Metadata> iterator() {
		return nestedList.iterator();
	}

	@Override
	public Object[] toArray() {
		return nestedList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return nestedList.toArray(a);
	}

	@Override
	public boolean add(Metadata metadata) {
		ensureNotReadOnly();
		if (!contains(metadata)) {
			addToIndex(metadata);
			return nestedList.add(metadata);
		} else {
			return false;
		}
	}

	@Override
	public boolean remove(Object o) {
		ensureNotReadOnly();
		if (o instanceof Metadata) {
			removeFromIndex((Metadata) o);
		}
		return nestedList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		ensureNotReadOnly();
		return nestedList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Metadata> c) {
		ensureNotReadOnly();
		boolean added = false;
		for (Metadata metadata : c) {
			if (!localCodeIndex.containsKey(metadata.getLocalCode())) {
				addToIndex(metadata);
				added = nestedList.add(metadata);
			}
		}
		return added;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Metadata> c) {
		ensureNotReadOnly();
		boolean added = false;
		for (Metadata metadata : c) {
			if (!localCodeIndex.containsKey(metadata.getLocalCode())) {
				addToIndex(metadata);
				nestedList.add(index, metadata);
				added = true;
			}
			index++;
		}
		return added;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		ensureNotReadOnly();
		for (Object object : c) {
			removeFromIndex((Metadata) object);
		}
		return nestedList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		ensureNotReadOnly();
		retainAllInIndexes(c);
		return nestedList.retainAll(c);
	}

	@Override
	public void clear() {
		ensureNotReadOnly();
		clearIndexes();
		nestedList.clear();
	}

	@Override
	public boolean equals(Object o) {
		return nestedList.equals(o) || ((o instanceof MetadataList) && nestedList.equals(((MetadataList) o).nestedList));
	}

	@Override
	public int hashCode() {
		return nestedList.hashCode();
	}

	@Override
	public Metadata get(int index) {
		return nestedList.get(index);
	}

	@Override
	public Metadata set(int index, Metadata element) {
		ensureNotReadOnly();
		setIndexes(index, element);
		return nestedList.set(index, element);
	}

	@Override
	public void add(int index, Metadata element) {
		ensureNotReadOnly();
		if (!localCodeIndex.containsKey(element.getLocalCode())) {
			addToIndex(element);
			nestedList.add(index, element);
		}
	}

	@Override
	public Metadata remove(int index) {
		ensureNotReadOnly();
		removeFromIndex(nestedList.get(index));
		return nestedList.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		if (o instanceof Metadata) {
			String searchedLocalCode = ((Metadata) o).getLocalCode();
			List<Metadata> nestedList1 = this.nestedList;
			for (int i = 0; i < nestedList1.size(); i++) {
				Metadata metadata = nestedList1.get(i);
				if (metadata.getLocalCode().equals(searchedLocalCode)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		return nestedList.lastIndexOf(o);
	}

	@Override
	public ListIterator<Metadata> listIterator() {
		return nestedList.listIterator();
	}

	@Override
	public ListIterator<Metadata> listIterator(int index) {
		return nestedList.listIterator(index);
	}

	@Override
	public List<Metadata> subList(int fromIndex, int toIndex) {
		ensureNotReadOnly();
		return nestedList.subList(fromIndex, toIndex);
	}

	public List<String> toMetadatasCodesList() {
		return new SchemaUtils().toMetadataCodes(this);
	}

	public List<String> toLocalCodesList() {
		return new SchemaUtils().toMetadataLocalCodes(this);
	}

	public MetadataList onlyReferencesToType(String schemaTypeCode) {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getType() == MetadataValueType.REFERENCE && metadata.getAllowedReferences().getTypeWithAllowedSchemas()
					.equals(
							schemaTypeCode)) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyAlwaysRequired() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.isDefaultRequirement()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyPopulatedByStyles() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (!metadata.getPopulateConfigs().getStyles().isEmpty()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyPopulated() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {

			if (!metadata.getPopulateConfigs().getProperties().isEmpty() ||
					!metadata.getPopulateConfigs().getStyles().isEmpty() ||
					!metadata.getPopulateConfigs().getRegexes().isEmpty()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyPopulatedByProperties() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (!metadata.getPopulateConfigs().getProperties().isEmpty()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyNonParentReferences() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getType() == MetadataValueType.REFERENCE && !metadata.isChildOfRelationship()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyParentReferences() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isChildOfRelationship()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyParentReferenceToSchemaType(String typeCode) {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isChildOfRelationship()
					&& metadata.getAllowedReferences().getTypeWithAllowedSchemas().equals(typeCode)) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyTaxonomyReferences() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isTaxonomyRelationship()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList only(MetadataListFilter filter) {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (filter.isReturned(metadata)) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlySearchable() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.isSearchable()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyEnabled() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.isEnabled()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyNonSystemReserved() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (!metadata.isSystemReserved()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyWithType(MetadataValueType... types) {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			for (MetadataValueType type : types) {
				if (metadata.getType() == type) {
					filteredMetadatasList.add(metadata);
				}
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	private void addToIndex(Metadata metadata) {
		localCodeIndex.put(metadata.getLocalCode(), metadata);
		codeIndex.put(codeOf(metadata), metadata);
		datastoreCodeIndex.put(metadata.getDataStoreCode(), metadata);
	}

	private void removeFromIndex(Metadata metadata) {
		localCodeIndex.remove(metadata.getLocalCode());
		codeIndex.remove(codeOf(metadata));
		datastoreCodeIndex.remove(metadata.getDataStoreCode());
	}

	private void clearIndexes() {
		localCodeIndex.clear();
		codeIndex.clear();
		datastoreCodeIndex.clear();
	}

	private void setIndexes(int index, Metadata element) {
		removeFromIndex(nestedList.get(index));
		localCodeIndex.put(element.getLocalCode(), element);
		codeIndex.put(codeOf(element), element);
		datastoreCodeIndex.put(element.getDataStoreCode(), element);
	}

	private void retainAllInIndexes(Collection<?> c) {
		clearIndexes();
		for (Object object : c) {
			addToIndex((Metadata) object);
		}
	}

	private void ensureNotReadOnly() {
		if (readOnly) {
			throw new UnsupportedOperationException("Operation cannot be done on read-only list");
		}
	}

	public MetadataList unModifiable() {
		readOnly = true;
		return this;
	}

	public MetadataList onlyEssentialMetadatasAndCodeTitle() {

		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.isEssential()) {
				filteredMetadatasList.add(metadata);
			} else if (metadata.getLocalCode().equals("code") || metadata.getLocalCode().equals("title")) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyManuals() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyNotSystemReserved() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (!metadata.isSystemReserved()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlySchemaAutocomplete() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.isSchemaAutocomplete()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public Metadata getMetadataWithLocalCode(String localCode) {
		for (Metadata metadata : this) {
			if (metadata.getLocalCode().equals(localCode)) {
				return metadata;
			}
		}
		return null;
	}

	public MetadataList onlyUniques() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.isUniqueValue()) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList onlyWithDefaultValue() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getDefaultValue() != null) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public List<Metadata> onlyWithoutInheritance() {
		List<Metadata> filteredMetadatasList = new ArrayList<>();
		for (Metadata metadata : nestedList) {
			if (metadata.getInheritance() == null) {
				filteredMetadatasList.add(metadata);
			}
		}
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	public MetadataList sortAscTitle() {
		return sortedUsing(new Comparator<Metadata>() {
			@Override
			public int compare(Metadata o1, Metadata o2) {
				return compareStrings(o1.getLabel(), o2.getLabel());
			}
		});
	}

	public MetadataList sortedUsing(Comparator<Metadata> comparator) {
		List<Metadata> filteredMetadatasList = new ArrayList<>(nestedList);
		Collections.sort(filteredMetadatasList, comparator);
		return new MetadataList(filteredMetadatasList).unModifiable();
	}

	private String codeOf(Metadata metadata) {
		String code = metadata.getCode();
		if (metadata.getInheritance() == null) {
			return metadata.getCode();
		} else {
			String[] parts = SchemaUtils.underscoreSplitWithCache(code);
			return parts[0] + "_default_" + parts[2];
		}
	}

	public boolean containsMetadataWithLocalCode(String localCode) {
		return localCodeIndex.containsKey(localCode);
	}
}
