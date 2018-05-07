package com.constellio.app.ui.pages.elevations;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.model.services.search.Elevations;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class EditElevationViewImpl extends BaseViewImpl implements EditElevationView {
    EditElevationPresenter presenter;

    BaseTable baseTable;
    ButtonsContainer<?> buttonsContainer;
    IndexedContainer indexedContainer;

    public static final String INFORMATION = "information";
    public static final String EXCLUDED = "-Excluded";
    public static final String RAISED = "-Raised";
    public static final String SPACE_4 = "&nbsp;&nbsp;&nbsp;&nbsp;";
    public static final String SPACES_8 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    Map<Integer, Object> containerMapperWithElevationObject = new HashMap<>();

    public EditElevationViewImpl() {
        presenter = new EditElevationPresenter(this);
    }

    @Override
    protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
        return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
    }

    @Override
    protected String getTitle() {
        return $("EditElevationView.title");
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout verticalLayout = new VerticalLayout();

        indexedContainer = new IndexedContainer();
        buttonsContainer = new ButtonsContainer<>(indexedContainer);

        indexedContainer.addContainerProperty(INFORMATION, Label.class, null);

        baseTable = new BaseTable(EditElevationViewImpl.class.getName());
        baseTable.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
        baseTable.setColumnHeader(INFORMATION, $("EditElevationView.query"));
        baseTable.setContainerDataSource(buttonsContainer);
        baseTable.setSizeFull();
        baseTable.setSortEnabled(false);
        buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
                final Integer index = (Integer) itemId;
                
                final boolean queryItem = isQuery(index);
                final boolean queryDocsElevation = isQueryDocsElevation(index);
                final boolean queryDocsExclusion = isQueryDocsExclusion(index);
                final boolean singleDocElevation = isSingleDocElevation(index);
                final String buttonLabel;
                final String confirmDialogMessage;
                if (queryItem) {
                	buttonLabel = $("EditElevationView.cancelQueryElevationAndExclusion");
                	confirmDialogMessage = $("EditElevationView.confirmCancel");
                } else if (queryDocsElevation) {
                	buttonLabel = $("EditElevationView.cancelQueryElevation");
                	confirmDialogMessage = $("EditElevationView.confirmCancelElevation");
                } else if (queryDocsExclusion) {
                	buttonLabel = $("EditElevationView.cancelQueryExclusion");
                	confirmDialogMessage = $("EditElevationView.confirmCancelExclusion");
                } else {
                	buttonLabel = $("EditElevationView.cancelDocElevation");
                	confirmDialogMessage = $("EditElevationView.confirmCancelElevation");
                }
            	DeleteButton cancelButton = new DeleteButton(buttonLabel, true) {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
                        Object containedObject = containerMapperWithElevationObject.get(index);
                        if (singleDocElevation) {
                            Elevations.QueryElevation.DocElevation docElevation = (Elevations.QueryElevation.DocElevation) containedObject;
                            presenter.cancelDocElevationButtonClicked(docElevation);
                        } else {
                            String query = (String) containedObject;
    		                if (queryItem) {
                                presenter.cancelQueryElevationAndExclusionButtonClicked(query);
    		                } else if (queryDocsElevation) {
                                presenter.cancelQueryElevationButtonClicked(query);
    		                } else if (queryDocsExclusion) {
                                presenter.cancelQueryExclusionButtonClicked(query);
    		                }
                        }
					}

					@Override
					protected String getConfirmDialogMessage() {
						return confirmDialogMessage;
					}
				};
                return cancelButton;
            }
        });

        for(String query : presenter.getAllQuery()) {
        	addQueryToTable(query);

            baseTable.setColumnExpandRatio(INFORMATION, 1);
            baseTable.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID,  60);

            List<Elevations.QueryElevation.DocElevation> docExclusions = presenter.getExclusions(query);
            addDocExclusionToTable(query, docExclusions);

            List<Elevations.QueryElevation.DocElevation> docElevations = presenter.getElevations(query);
            addDocElevationsToTable(query, docElevations);
        }

        verticalLayout.addComponent(baseTable);
        verticalLayout.setSizeFull();

        return verticalLayout;
    }
    
    private boolean isQuery(int index) {
    	boolean queryItem;
    	Object containedObject = containerMapperWithElevationObject.get(index);
        if (containedObject instanceof String) {
            String informationValue = ((Label) baseTable.getContainerProperty(index, INFORMATION).getValue()).getValue();
            if (informationValue.equals(SPACE_4 + $("EditElevationView.excluded"))) {
            	queryItem = false;
            } else if (informationValue.equals(SPACE_4 + $("EditElevationView.raised"))) {
            	queryItem = false;
            } else {
            	queryItem = true;
            }
        } else {
        	queryItem = false;
        }
        return queryItem;
    }
    
    private boolean isSingleDocElevation(int index) {
    	Object containedObject = containerMapperWithElevationObject.get(index);
        return containedObject instanceof Elevations.QueryElevation.DocElevation;
    }
    
    private boolean isQueryDocsExclusion(int index) {
    	boolean queryDocsExclusion;
    	Object containedObject = containerMapperWithElevationObject.get(index);
        if (containedObject instanceof String) {
            String informationValue = ((Label) baseTable.getContainerProperty(index, INFORMATION).getValue()).getValue();
            if (informationValue.equals(SPACE_4 + $("EditElevationView.excluded"))) {
                queryDocsExclusion = true;
            } else {
            	queryDocsExclusion = false;
            }
        } else {
        	queryDocsExclusion = false;
        }
        return queryDocsExclusion;
    }
    
    private boolean isQueryDocsElevation(int index) {
    	boolean queryDocsElevation;
    	Object containedObject = containerMapperWithElevationObject.get(index);
        if (containedObject instanceof String) {
            String informationValue = ((Label) baseTable.getContainerProperty(index, INFORMATION).getValue()).getValue();
            if (informationValue.equals(SPACE_4 + $("EditElevationView.raised"))) {
                queryDocsElevation = true;
            } else {
            	queryDocsElevation = false;
            }
        } else {
        	queryDocsElevation = false;
        }
        return queryDocsElevation;
    }
    
    private void addQueryToTable(String query) {
    	addOneItemToTableAndSetValue(query, query);
    }
    
    private void addDocExclusionToTable(String query, List<Elevations.QueryElevation.DocElevation> docExclusions) {
        if (docExclusions.size() > 0) {
	    	addOneItemToTableAndSetValue(query, SPACE_4 + $("EditElevationView.excluded"));
	        addItemsToTable(query, docExclusions);
        }    
    }
    
    private void addDocElevationsToTable(String query, List<Elevations.QueryElevation.DocElevation> docElevations) {
        if (docElevations.size() > 0) {
            addOneItemToTableAndSetValue(query, SPACE_4 + $("EditElevationView.raised"));
            addItemsToTable(query, docElevations);
        }
    }

    private void addOneItemToTableAndSetValue(Object valueToAdd, String value) {
        Object addedItemNumber = baseTable.addItem();

        containerMapperWithElevationObject.put((Integer)addedItemNumber, valueToAdd);
        Label label = new Label(value);
        label.setContentMode(ContentMode.HTML);
        indexedContainer.getContainerProperty(addedItemNumber, INFORMATION).setValue(label);
    }

    private void addItemsToTable(String query, List<Elevations.QueryElevation.DocElevation> docElevated) {
        for (Iterator<Elevations.QueryElevation.DocElevation> iterator = docElevated.iterator(); iterator.hasNext();) {
            Elevations.QueryElevation.DocElevation docElevation = iterator.next();
            docElevation.setQuery(query);
            addOneItemToTableAndSetValue(docElevation, SPACES_8 + presenter.getRecordTitle(docElevation.getId()));
        }
    }

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}
}
