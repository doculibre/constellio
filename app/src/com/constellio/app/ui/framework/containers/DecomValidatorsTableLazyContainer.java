package com.constellio.app.ui.framework.containers;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

public abstract class DecomValidatorsTableLazyContainer extends LazyQueryContainer {
    public static final String LABEL = "label";
    public static final String VALUE = "value";

    public DecomValidatorsTableLazyContainer(QueryFactory queryFactory, Object idPropertyId, int batchSize, boolean compositeItems) {
        super(queryFactory, idPropertyId, batchSize, compositeItems);
    }

    /*public DecomValidatorsTableLazyContainer(DecomValidatorsDataProvider dataProvider) {
        super(new DecomValidatorVOLazyQueryDefinition(), new DecomValidatorVOLazyQueryFactory(dataProvider));
    }

    public class DecomValidatorVOLazyQueryDefinition extends LazyQueryDefinition {


        public DecomValidatorVOLazyQueryDefinition() {
            super(true, 100, null);

            super.addProperty(LABEL, String.class, null, true, true);
            super.addProperty(VALUE, Float.class, null, true, true);
        }

        class DecomValidatorVOLazyQueryFactory implements QueryFactory, Serializable {

            DecomValidatorsDataProvider dataProvider;

            public DecomValidatorVOLazyQueryFactory(DecomValidatorsDataProvider dataProvider) {
                this.dataProvider = dataProvider;
            }

            @Override
            public Query constructQuery(final QueryDefinition queryDefinition) {
                return new SerializableQuery() {
                    @Override
                    public int size() {
                        return dataProvider.size();
                    }

                    @Override
                    public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
                        throw new UnsupportedOperationException("Query is read-only");
                    }

                    @Override
                    public List<Item> loadItems(int startIndex, int count) {
                        List<Item> items = new ArrayList<>();
                        List<DecomValidatorVO> decomValidatorVOs = dataProvider.getDecomValidators(startIndex, count);
                        for (DecomValidatorVO decomValidatorVO : decomValidatorVOs) {
                            Item item = new BeanItem<>(decomValidatorVO);
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

            private static interface SerializableQuery extends Query, Serializable {

            }

        }

    }*/
}
