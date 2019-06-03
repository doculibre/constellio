package com.constellio.app.ui.framework.containers;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.SearchEventVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.collections4.ListUtils.union;

public class SearchEventVOLazyContainer extends LazyQueryContainer implements RefreshableContainer {

	private static Logger LOGGER = LoggerFactory.getLogger(SearchEventVOLazyContainer.class);

	public static final String ID = "searchEvent_default_id";
	public static final String CLICK_COUNT = "searchEvent_default_clickCount";
	public static final String ORIGINAL_QUERY = "searchEvent_default_originalQuery";
	public static final String LAST_PAGE_NAVIGATION = "searchEvent_default_lastPageNavigation";
	public static final String PARAMS = "searchEvent_default_params";
	public static final String QUERY = "searchEvent_default_query";
	public static final String USER = "searchEvent_default_username";
	public static final String CREATION_DATE = "searchEvent_default_createdOn";
	public static final String Q_TIME = "searchEvent_default_qTime";
	public static final String NUM_FOUND = "searchEvent_default_numFound";
	public static final String NUM_PAGE = "searchEvent_default_numPage";
	public static final String SOUS_COLLECTION = "searchEvent_default_sousCollection";
	public static final String LANGUE = "searchEvent_default_langue";
	public static final String TYPE_RECHERCHE = "searchEvent_default_type_recherche";
	public static final String CAPSULE = "searchEvent_default_capsule";
	public static final String CLICKS = "searchEvent_default_clicks";

	private static final List<String> PROPERTIES = unmodifiableList(
			asList(ID, CREATION_DATE, ORIGINAL_QUERY, SOUS_COLLECTION, LANGUE, NUM_FOUND, CLICK_COUNT, Q_TIME, NUM_PAGE,
					TYPE_RECHERCHE, CAPSULE, CLICKS));
	private static final List<String> PROPERTIES_WITH_PARAMS = unmodifiableList(union(PROPERTIES, asList(PARAMS)));

	public SearchEventVOLazyContainer(QueryDefinition queryDefinition, QueryFactory queryFactory) {
		super(queryDefinition, queryFactory);
	}

	@Override
	public void forceRefresh() {
		refresh();
	}

	public static List<String> getProperties(MetadataSchemaVO schema) {
		return getDeclaredProperties(schema, PROPERTIES);
	}

	public static List<String> getPropertiesWithParams(MetadataSchemaVO schema) {
		return getDeclaredProperties(schema, PROPERTIES_WITH_PARAMS);
	}

	@NotNull
	private static List<String> getDeclaredProperties(MetadataSchemaVO schema, List<String> properties) {
		List<String> props = new ArrayList<>();
		for (String p : properties) {
			switch (p) {
				case NUM_PAGE:
				case SOUS_COLLECTION:
				case LANGUE:
				case TYPE_RECHERCHE:
					props.add(p);
					break;

				default:
					for (MetadataVO metadataVO : schema.getDisplayMetadatas()) {
						if (p.equals(metadataVO.getCode())) {
							props.add(p);
							break;
						}
					}
			}
		}
		return props;
	}

	public static class SearchEventVOLazyQueryDefinition extends LazyQueryDefinition {
		private Map<String, MetadataVO> definedMetadatas = new HashMap<>();

		public SearchEventVOLazyQueryDefinition(SearchEventVODataProvider dataProvider, List<String> properties) {
			super(true, 100, null);

			if (properties == null || properties.isEmpty()) {
				properties = getPropertiesWithParams(dataProvider.getSchema());
			}

			MetadataSchemaVO schema = dataProvider.getSchema();
			List<MetadataVO> dataProviderDisplayMetadataVOs = schema.getDisplayMetadatas();
			for (MetadataVO metadataVO : dataProviderDisplayMetadataVOs) {
				if (properties.contains(metadataVO.getCode())) {
					definedMetadatas.put(metadataVO.getCode(), metadataVO);
				}
			}

			for (String code : properties) {
				MetadataVO metadataVO = definedMetadatas.get(code);
				Class<?> javaType;
				if (metadataVO != null) {
					javaType = metadataVO.getJavaType();
				} else {
					javaType = String.class;
				}

				super.addProperty(StringUtils.trimToEmpty(code), javaType, getDefaultValue(javaType), true, true);
			}
		}

		private Object getDefaultValue(Class type) {
			if (Number.class.isAssignableFrom(type)) {
				return new BigDecimal(0);
			}

			return null;
		}

		public Map<String, MetadataVO> getDefinedMetadatas() {
			return (Map<String, MetadataVO>) Collections.unmodifiableMap(definedMetadatas);
		}
	}

	public static class SearchEventVOLazyQueryFactory implements QueryFactory, Serializable {
		private final Map<String, MetadataVO> definedMetadatas;
		private final SearchEventVODataProvider dataProvider;

		public SearchEventVOLazyQueryFactory(SearchEventVODataProvider dataProvider,
											 Map<String, MetadataVO> definedMetadatas) {
			this.dataProvider = dataProvider;
			this.definedMetadatas = definedMetadatas;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			return new Query() {

				@Override
				public int size() {
					return (int) dataProvider.size();
				}

				@Override
				public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public List<Item> loadItems(int startIndex, int count) {
					List<Item> items = new ArrayList<>();
					List<RecordVO> recordVOsFromFirstDataProvider = dataProvider.listRecordVOs(startIndex, count);
					for (RecordVO recordVO : recordVOsFromFirstDataProvider) {
						Item item = new SearchEventVOItem(recordVO, definedMetadatas);
						items.add(item);
					}
					return items;
				}

				@Override
				public boolean deleteAllItems() {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public Item constructItem() {
					throw new UnsupportedOperationException("Query is read-only");
				}
			};
		}

		public static class SearchEventVOItem extends RecordVOItem {
			private final Map<String, MetadataVO> definedMetadatas;

			public SearchEventVOItem(RecordVO recordVO, Map<String, MetadataVO> definedMetadatas) {
				super(recordVO);
				this.definedMetadatas = definedMetadatas;
			}

			@Override
			public Property<?> getItemProperty(Object id) {
				if (NUM_PAGE.equals(id)) {
					return new ObjectValueProperty() {
						@Override
						public Object getValue() {
							//                            String params = getRecord().get(PARAMS).toString();
							//                            double rows = getDouble("rows", params);
							//                            double start = getDouble("start", params);
							//
							//                            if(start < 0) {
							//                                return 1;
							//                            } else if(rows == 0) {
							//                                return (int) start;
							//                            } else {
							//                                return (int) ((start / rows) + 1);
							//                            }
							Number value = getRecord().get(SearchEvent.LAST_PAGE_NAVIGATION);

							if (value != null) {
								return value.intValue();
							} else {
								return 1;
							}
						}
					};
				} else if (SOUS_COLLECTION.equals(id)) {
					return new ObjectValueProperty() {
						@Override
						public Object getValue() {
							String params = getRecord().get(PARAMS).toString();
							return getSousCollection(params);
						}
					};
				} else if (LANGUE.equals(id)) {
					return new ObjectValueProperty() {
						@Override
						public Object getValue() {
							String params = getRecord().get(PARAMS).toString();
							return StringUtils.removeAll(getLangue(params), "\"");
						}
					};
				} else if (TYPE_RECHERCHE.equals(id)) {
					return new ObjectValueProperty() {
						@Override
						public Object getValue() {
							String params = getRecord().get(PARAMS).toString();
							return getTypeRecherche(params);
						}
					};
				} else if (CAPSULE.equals(id)) {
					List<String> idCapsule = getRecord().get(CAPSULE);
					if (idCapsule != null) {
						AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
						String collection = getRecord().getSchema().getCollection();

						SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection,
								appLayerFactory.getModelLayerFactory());

						try {
							final List<Capsule> capsules = schemasRecordsServices.getCapsules(idCapsule);
							if (capsules != null) {
								return new ObjectValueProperty() {
									@Override
									public Object getValue() {
										List<String> codes = new ArrayList<>(capsules.size());
										for (Capsule capsule : capsules) {
											codes.add(capsule != null ? capsule.getCode() : null);
										}
										return StringUtils.join(codes, ", ");
									}
								};
							}
						} catch (NoSuchRecordWithId e) {
							// Capsule no longer exists
						}
					}
				} else if (CLICKS.equals(id)) {
					return new ObjectValueProperty() {
						@Override
						public Object getValue() {
							List<String> urls = getRecord().get(CLICKS);
							return StringUtils.join(urls, ", ");
						}
					};
				} else if (ORIGINAL_QUERY.equals(id)) {
					return new ObjectValueProperty() {
						@Override
						public Object getValue() {
							String q = getRecord().get(ORIGINAL_QUERY);
							if (q == null) {
								q = getRecord().get(QUERY);
							}
							return q;
						}
					};
				}

				return super.getItemProperty(this.definedMetadatas.get(id));
			}

			private String getLangue(String params) {
				return getRegexpValue("language_s:\\(\\\"(.*)\\\"\\)", params);
			}

			private String getSousCollection(String params) {
				return getRegexpValue("USRsousCollection_s:\\(\\\"(.*)\\\"\\)", params);
			}

			private String getTypeRecherche(String params) {
				Object o = getRecord().get(ORIGINAL_QUERY);
				if (o == null) {
					o = getRecord().get(QUERY);
				}

				if (o != null && StringUtils.contains(o.toString(), "\"")) {
					return $("StatisticsView.expressionExacte");
				}

				String mm = getTokenString("mm", params);
				if ("1".equals(mm)) {
					return $("StatisticsView.auMoinsUnDesMots");
				} else if ("100%".equals(mm)) {
					return $("StatisticsView.tousLesMots");
				}

				return mm;
			}

			private double getDouble(String token, String params) {
				try {
					return Double.parseDouble(getTokenString(token, params));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return 0;
				}
			}

			private String getTokenString(String token, String params) {
				return getRegexpValue(token + "=(\\S+),", params);
			}

			private String getRegexpValue(String regExpGroup, String from) {
				Pattern p = Pattern.compile(regExpGroup);   // the pattern to search for
				Matcher m = p.matcher(from);

				if (m.find()) {
					return m.group(1);
				} else {
					return "";
				}
			}

			@Override
			public Collection<?> getItemPropertyIds() {
				return definedMetadatas.keySet();
			}
		}
	}

	private abstract static class ObjectValueProperty extends AbstractProperty<Object> {
		@Override
		public void setValue(Object newValue)
				throws ReadOnlyException {
			throw new ReadOnlyException("This column is read-only");
		}

		@Override
		public Class<? extends Object> getType() {
			return String.class;
		}
	}

	public static SearchEventVOLazyContainer defaultInstance(SearchEventVODataProvider dataProvider,
															 List<String> properties) {
		SearchEventVOLazyQueryDefinition qDef = new SearchEventVOLazyQueryDefinition(dataProvider, properties);
		SearchEventVOLazyQueryFactory qFact = new SearchEventVOLazyQueryFactory(dataProvider, qDef.getDefinedMetadatas());

		final SearchEventVOLazyContainer searchEventVOLazyContainer = new SearchEventVOLazyContainer(qDef, qFact);

		dataProvider.addDataRefreshListener(new DataProvider.DataRefreshListener() {
			@Override
			public void dataRefresh() {
				searchEventVOLazyContainer.refresh();
			}
		});

		return searchEventVOLazyContainer;
	}
}
