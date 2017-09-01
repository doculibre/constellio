package com.constellio.app.ui.framework.buttons.SIPButton;

public class SIPGenerationValidationException extends RuntimeException {
    public SIPGenerationValidationException(){
        super();
    }

    public SIPGenerationValidationException(String error) {
        super(error);
    }
}
