package com.constellio.app.ui.handlers;

import java.io.Serializable;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurNotifier;
import com.vaadin.event.FieldEvents.FocusNotifier;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;


/**
 * Source : http://ramontalaverasuarez.blogspot.ca/2014/06/vaadin-7-detect-enter-key-in-textfield.html
 * Use: https://vaadin.com/forum/#!/thread/77601/1857835
 * 
 * @author Vincent
 */
@SuppressWarnings("serial")
public abstract class OnEnterKeyHandler implements Serializable {

     final ShortcutListener enterShortCut = new ShortcutListener(
        "EnterOnTextAreaShorcut", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                onEnterKeyPressed();
            }
        };

     public void installOn(TextField component) {
    	 doInstallOn(component);
     }

     public void installOn(PasswordField component) {
    	 doInstallOn(component);
     }

     public void installOn(DateField component) {
    	 doInstallOn(component);
     }

     public void installOn(ComboBox component) {
    	 doInstallOn(component);
     }

     private void doInstallOn(final AbstractComponent component) {
         ((FocusNotifier) component).addFocusListener(new FieldEvents.FocusListener() {
             @Override
             public void focus(FieldEvents.FocusEvent event) {
                 component.addShortcutListener(enterShortCut);
             }
	     });

         ((BlurNotifier) component).addBlurListener(new FieldEvents.BlurListener() {
             @Override
             public void blur(FieldEvents.BlurEvent event) {
                 component.removeShortcutListener(enterShortCut);
             }
         });
     }

     public abstract void onEnterKeyPressed();

}

