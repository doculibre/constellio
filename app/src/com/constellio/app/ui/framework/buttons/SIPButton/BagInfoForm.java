package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;


import static com.constellio.app.ui.i18n.i18n.$;

public class BagInfoForm extends CustomComponent {
    private BeanFieldGroup<BagInfoVO> binder;

    private BagInfoVO bagInfoVO;

    private FormLayout formLayout;

    private TextArea noteTextArea, descriptionSommaire;

    private TextField
            identificationOrganismeTextField,
            IDOrganismeTextField,
            adresseTextField,
            regionAdministrativeTextField,
            entiteResponsableTextField,
            identificationEntiteResponsableTextField,
            courrielResponsableTextField,
            telephoneResponsableTextField,
            categoryDocumentTextField,
            methodeTransfereTextField,
            restrictionAccessibiliteTextField,
            encodingTextField;

    private BaseView view;

    public BagInfoForm(BaseView view, BagInfoVO bagInfoVO){
        this.view = view;
        this.binder = new BeanFieldGroup<>(BagInfoVO.class);
        this.bagInfoVO = bagInfoVO;
        this.formLayout = new FormLayout();
        binder.setItemDataSource(bagInfoVO);
    }

   


}
