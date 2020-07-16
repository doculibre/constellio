"use strict";

function SignatureAnnotationPicker(signatureDataStore, dropZoneManager, signatureOptionsElement) {
	this.signatureDataStore = signatureDataStore;
	this.dropZoneManager = dropZoneManager;
	this.signatureOptionsElement = signatureOptionsElement;
};

SignatureAnnotationPicker.prototype.openPicker = function(signHereAnnotation) {
	this.removeChildren(this.signatureOptionsElement);
    
    var signatureOptionsContainerElement = document.createElement("div");
    signatureOptionsContainerElement.id = "signatureOptionsContainer";
    signatureOptionsContainerElement.classList.add("signatureOptionsContainer");

    var signatureContainer = this.buildSignatureContainer(signHereAnnotation, true);
    var initialsContainer = this.buildSignatureContainer(signHereAnnotation, false);

    this.signatureOptionsElement.appendChild(signatureOptionsContainerElement);
    signatureOptionsContainerElement.appendChild(signatureContainer);
    signatureOptionsContainerElement.appendChild(initialsContainer);

	this.setSignatureOptionsElementVisible(true);
	$(this.signatureOptionsElement).find(".tooltip").tooltipster();
};	

SignatureAnnotationPicker.prototype.closePicker = function() {
	this.setSignatureOptionsElementVisible(false);
};

SignatureAnnotationPicker.prototype.setSignatureOptionsElementVisible = function(visible) {
    if (visible) {
        this.signatureOptionsElement.classList.remove("hidden");
    } else {
        this.signatureOptionsElement.classList.add("hidden");
    }
};

SignatureAnnotationPicker.prototype.buildSignatureContainer = function(signHereAnnotation, signature) {
	var self = this;

    var prefix;
    var thumbnailUrl;
	if (signature) {
		prefix = "signature";
		thumbnailUrl = this.signatureDataStore.getSignatureImageUrl();
	} else {
		prefix = "initials";
		thumbnailUrl = this.signatureDataStore.getInitialsImageUrl();
	}
	var signatureExists = thumbnailUrl;

	var adjustVisibility = function() {
        if (signatureExists) {
            signatureContainer.classList.add("existing");
        } else {
            signatureContainer.classList.remove("existing");
        }
		self.setVisible(thumbnailElement, signatureExists);
		self.setVisible(deleteButton, signatureExists);
		self.setVisible(addButton, !signatureExists);
	};

	var useSignature = function() {
		var imageUrl = thumbnailElement.src;			
		var signatureImageAnnotation = new SignatureImageAnnotation(imageUrl);
		signatureImageAnnotation.setSignature(signature);
		if (signHereAnnotation) {
			signatureImageAnnotation.setX(signHereAnnotation.getX());
			signatureImageAnnotation.setY(signHereAnnotation.getY());
			signatureImageAnnotation.setWidth(signHereAnnotation.getWidth());
			signatureImageAnnotation.setHeight(signHereAnnotation.getHeight());
		}

		if (signHereAnnotation) {
			self.replaceSignHereAnnotation(signHereAnnotation, signatureImageAnnotation);
		} else {
			self.dropZoneManager.defineAnnotation(signatureImageAnnotation);
        } 
        
        self.closePicker();
	};

	var deleteSignature = function() {
		if (signature) {
			self.signatureDataStore.removeSignatureImageUrl();
		} else {
			self.signatureDataStore.removeInitialsImageUrl();
		}
		signatureExists = false;
		adjustVisibility();
	};

	var addSignature = function() {
        self.closePicker();
		if (signHereAnnotation) {
			self.buildAddSignatureWindow(signHereAnnotation, signHereAnnotation, signature);
		} else {
			// Use to collect x, y, width and height
			var placeHolderAnnotation = new Annotation();
			placeHolderAnnotation.annotationDefined = function() {
				self.buildAddSignatureWindow(placeHolderAnnotation, null, signature);
			};
			self.dropZoneManager.defineAnnotation(placeHolderAnnotation);
		}
	};

	var thumbnailElement = document.createElement("img");
	thumbnailElement.title = this.i10n("signaturePicker." + prefix + ".use.tooltip", "Use");
	thumbnailElement.classList.add("tooltip");
	thumbnailElement.classList.add(prefix + "Thumbnail");
	thumbnailElement.src = thumbnailUrl;
	thumbnailElement.style.maxWidth = "200px";
	thumbnailElement.style.height = "auto";	
	thumbnailElement.style.maxHeight = "60px";	
	thumbnailElement.addEventListener("click", function(e) {
		useSignature();
	}, false);

	var deleteButton = document.createElement("button");
	deleteButton.classList.add(prefix + "DeleteButton");
	deleteButton.setAttribute("data-l10n-id", "buttons.delete");
	deleteButton.classList.add("tooltip");
	deleteButton.title=this.i10n("signaturePicker." + prefix + ".delete.tooltip", "Delete");
	deleteButton.onclick = function(e) {
		deleteSignature();
	};

	var addButton = document.createElement("button");
	addButton.classList.add(prefix + "AddButton");
	addButton.setAttribute("data-l10n-id", prefix + ".add");
	addButton.classList.add("tooltip");
	addButton.title=this.i10n("signaturePicker." + prefix + ".add", "Add");
	addButton.innerHTML=this.i10n("signaturePicker." + prefix + ".add.tooltip", "Add");
	addButton.onclick = function(e) {
		addSignature();
    };
    
    var signatureContainer = document.createElement("div");
    signatureContainer.classList.add("signature-picker-signature-container");
    signatureContainer.classList.add(prefix + "OptionContainer");
    signatureContainer.appendChild(thumbnailElement);
    signatureContainer.appendChild(deleteButton);
    signatureContainer.appendChild(addButton);

    adjustVisibility();

    return signatureContainer;
};	

SignatureAnnotationPicker.prototype.buildAddSignatureWindow = function(placeHolderAnnotation, signHereAnnotation, signature) {
	var self = this;
	
	var tooltipPrefix;
	if (signature) {
		tooltipPrefix = "signature";
	} else {
		tooltipPrefix = "initials";
	}

    var saveSignature = true;

    var adjustedFormHtmlElementSize = this.computeSizeBasedOnPlaceHolderAnnotation(placeHolderAnnotation);
    
	var closeWindow = function() {
        windowElement.parentNode.removeChild(windowElement);
        placeHolderAnnotation.remove();
	};

	var windowElement = document.createElement("div");
	windowElement.classList.add("signature-picker-window");
	
	var windowContentElement = document.createElement("div");
	windowContentElement.classList.add("signature-picker-window-content");
	
	var closeButton = document.createElement("span");
	closeButton.innerHTML = "&#10006;";
	closeButton.classList.add("signature-picker-window-close-button");
	closeButton.onclick = function(e) {
		closeWindow();
    };
    
    var uploadedImageUrl;
	var handleFiles = function(files) {
		for (var i = 0; i < files.length; i++) {
			var file = files.item(i);
			if (file.type.indexOf("image/") == 0) {
				var reader = new FileReader();
				reader.readAsDataURL(file);
				reader.onloadend = function() {
                    var imageUrl = reader.result;
                    uploadedImageUrl = imageUrl;
					dropContainerElement.innerHTML = "";
					dropContainerElement.style.backgroundImage = "url(" + uploadedImageUrl + ")";
				};
				break;
			}
		};
	};
	var setFormContainerContent = function(formHtmlElement) {
		self.removeChildren(formContainerElement);
		formHtmlElement.classList.add("signature-picker-field");
		formContainerElement.appendChild(formHtmlElement);
	};

	var formContainerElement = document.createElement("div");
	formContainerElement.classList.add("signature-picker-form-container");

	var saveCheckBox = document.createElement("input");
	saveCheckBox.setAttribute("type", "checkbox");
	saveCheckBox.checked = saveSignature;
	saveCheckBox.addEventListener("change", function() {
		saveSignature = this.checked;
	});

    var actionButtonsElement = document.createElement("div");
	actionButtonsElement.classList.add("signature-picker-action-buttons");
    
	var applyButton = document.createElement("button");
	applyButton.classList.add("annotation-editor-window-apply-button");
	applyButton.classList.add("primary");
	applyButton.innerHTML = this.i10n("buttons.apply", "Apply");
	
	var cancelButton = document.createElement("button");
	cancelButton.classList.add("annotation-editor-window-cancel-button");
	cancelButton.innerHTML = this.i10n("buttons.cancel", "Cancel");
    cancelButton.onclick = closeWindow;
    
    var resetActionButtons = function(addButtonsAfterReset) {
        self.removeChildren(actionButtonsElement);
        if (addButtonsAfterReset) {
            actionButtonsElement.appendChild(applyButton);
            actionButtonsElement.appendChild(cancelButton);
        }
	};
	
	var selectedTypeButton;
	var setSelectedTypeButton = function(button) {
		if (selectedTypeButton) {
			selectedTypeButton.classList.remove("selected");
		}
		selectedTypeButton = button;
		selectedTypeButton.classList.add("selected");
	};

	var drawSignature = function() {
		setSelectedTypeButton(drawButton);

		var signaturePadAnnotation = new SignaturePadAnnotation();
		signaturePadAnnotation.setSignature(signature);
		signaturePadAnnotation.setX(placeHolderAnnotation.getX());
		signaturePadAnnotation.setY(placeHolderAnnotation.getY());
		signaturePadAnnotation.setWidth(placeHolderAnnotation.getWidth());
		signaturePadAnnotation.setHeight(placeHolderAnnotation.getHeight());
		
		applyButton.onclick = function() {
			var imageUrl = signaturePadAnnotation.editor.getImageUrl();
			if (saveSignature) {
				if (signature) {
					self.signatureDataStore.setSignatureImageUrl(imageUrl);
				} else {
					self.signatureDataStore.setInitialsImageUrl(imageUrl);
				}
			}
			closeWindow();

			if (signHereAnnotation) {
				self.replaceSignHereAnnotation(signHereAnnotation, signaturePadAnnotation);
			} else {
				self.replacePlaceHolderAnnotation(placeHolderAnnotation, signaturePadAnnotation);
            }
            signaturePadAnnotation.getSaveCallback();
		};

        resetActionButtons(false);
		var formHtmlElement = signaturePadAnnotation.editor.getFormHtmlElement(signaturePadAnnotation, actionButtonsElement, applyButton, cancelButton);
        actionButtonsElement.appendChild(applyButton);
        actionButtonsElement.appendChild(cancelButton);
        setFormContainerContent(formHtmlElement);	
	};

	var typeSignature = function() {
		setSelectedTypeButton(typeButton);

		var signatureTextAnnotation = new SignatureTextAnnotation();
		signatureTextAnnotation.setSignature(signature);
		signatureTextAnnotation.setX(placeHolderAnnotation.getX());
		signatureTextAnnotation.setY(placeHolderAnnotation.getY());
		signatureTextAnnotation.setWidth(placeHolderAnnotation.getWidth());
		signatureTextAnnotation.setHeight(placeHolderAnnotation.getHeight());
	
		applyButton.onclick = function() {
			closeWindow();

			if (signHereAnnotation) {
				self.replaceSignHereAnnotation(signHereAnnotation, signatureTextAnnotation);
			} else {
				self.replacePlaceHolderAnnotation(placeHolderAnnotation, signatureTextAnnotation);
            }

            signatureTextAnnotation.onTextConvertedToImage = function (imageUrl) {
                if (saveSignature) {
                    if (signature) {
                        self.signatureDataStore.setSignatureImageUrl(imageUrl);
                    } else {
                        self.signatureDataStore.setInitialsImageUrl(imageUrl);
                    }
                }
            };
            signatureTextAnnotation.getSaveCallback();
		};

        resetActionButtons(true);
		var formHtmlElement = signatureTextAnnotation.editor.getFormHtmlElement(signatureTextAnnotation, actionButtonsElement, applyButton, cancelButton);
        formHtmlElement.style.width = adjustedFormHtmlElementSize.width + "px";
        formHtmlElement.style.height = adjustedFormHtmlElementSize.height + "px";
        formHtmlElement.style.lineHeight = adjustedFormHtmlElementSize.height + "px";
        formHtmlElement.style.fontSize = (adjustedFormHtmlElementSize.height / 2.5) + "px";
		setFormContainerContent(formHtmlElement);	
	};

	var uploadSignature = function() {
		setSelectedTypeButton(imageButton);

		applyButton.onclick = function() {
            var imageUrl = uploadedImageUrl;
            if (saveSignature) {
                if (signature) {
                    self.signatureDataStore.setSignatureImageUrl(imageUrl);
                } else {
                    self.signatureDataStore.setInitialsImageUrl(imageUrl);
                }
            }
            closeWindow();
            
            var signatureImageAnnotation = new SignatureImageAnnotation(imageUrl);
            signatureImageAnnotation.setSignature(signature);
            signatureImageAnnotation.setX(placeHolderAnnotation.getX());
            signatureImageAnnotation.setY(placeHolderAnnotation.getY());
            signatureImageAnnotation.setWidth(placeHolderAnnotation.getWidth());
            signatureImageAnnotation.setHeight(placeHolderAnnotation.getHeight());
    
            if (signHereAnnotation) {
                self.replaceSignHereAnnotation(signHereAnnotation, signatureImageAnnotation);
            } else {
				self.replacePlaceHolderAnnotation(placeHolderAnnotation, signatureImageAnnotation);
            }
            signatureImageAnnotation.getSaveCallback();
		};

        resetActionButtons(true);
		var formHtmlElement = dropContainerElement;
        formHtmlElement.style.width = adjustedFormHtmlElementSize.width + "px";
        formHtmlElement.style.height = adjustedFormHtmlElementSize.height + "px";
        formHtmlElement.style.lineHeight = adjustedFormHtmlElementSize.height + "px";
		setFormContainerContent(formHtmlElement);	
	};

	var signatureTypeButtonsElement = document.createElement("div");
	signatureTypeButtonsElement.classList.add("signature-picker-signature-type-buttons");
	
	var inputField = document.createElement("input");
	inputField.classList.add("signature-picker-input");
	inputField.type = "file";
	inputField.accept = "image/*";	
	inputField.onchange = function(e) {
		handleFiles(inputField.files);
		e.preventDefault();
		e.stopPropagation();
	};
	this.setVisible(inputField, false);
	
    var dropContainerElement = document.createElement("div");
	dropContainerElement.classList.add("signature-picker-drop-container");
	dropContainerElement.innerHTML=this.i10n("signaturePicker." + tooltipPrefix + ".drop.tooltip", "Click here to upload an image");
	
	dropContainerElement.addEventListener("click", function(e) {
		inputField.click();
	}, false);
	dropContainerElement.addEventListener("drop", function(e) {
		var dt = e.dataTransfer;
		handleFiles(dt.files);
	}, false);
	;["dragenter", "dragover", "dragleave", "drop"].forEach(function(eventName) {
		dropContainerElement.addEventListener(eventName, function(e) {
			e.preventDefault();
			e.stopPropagation();
		}, false);
	});
	;["dragenter", "dragover"].forEach(function(eventName) {
		dropContainerElement.addEventListener(eventName, function(e) {
			dropContainerElement.classList.add("highlight");
		}, false);
	});

	;["dragleave", "drop"].forEach(function(eventName) {
		dropContainerElement.addEventListener(eventName, function(e) {
			dropContainerElement.classList.remove("highlight");
		}, false);
	});
	
	var drawButton = document.createElement("button");
	drawButton.innerHTML=this.i10n("buttons.draw", "Draw");
	drawButton.classList.add("signature-picker-draw-button");
	drawButton.setAttribute("data-l10n-id", "buttons.draw");
	drawButton.classList.add("tooltip");
	drawButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".draw.tooltip", "Draw");
	drawButton.onclick = function(e) {
		drawSignature();
	};
	
	var typeButton = document.createElement("button");
	typeButton.innerHTML=this.i10n("buttons.type", "Type");
	typeButton.classList.add("signature-picker-type-button");
	typeButton.setAttribute("data-l10n-id", "buttons.type");
	typeButton.classList.add("tooltip");
	typeButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".type.tooltip", "Type");
	typeButton.onclick = function(e) {
		typeSignature();
	};
	
	var imageButton = document.createElement("button");
	imageButton.innerHTML=this.i10n("buttons.image", "Image");
	imageButton.setAttribute("data-l10n-id", "buttons.image");
	imageButton.classList.add("signature-picker-image-button");
	imageButton.setAttribute("data-l10n-id", "buttons.image");
	imageButton.classList.add("tooltip");
	imageButton.title=this.i10n("signaturePicker." + tooltipPrefix + ".image.tooltip", "Image");
	imageButton.onclick = function(e) {
		uploadSignature();
	};

	var titleElement = document.createElement("div");
	titleElement.classList.add("signature-picker-window-title");
    titleElement.innerHTML = this.i10n("signaturePicker.title", "To save signatures in the PDF document, click on the Certify button.");
	
	windowElement.appendChild(windowContentElement);
	windowContentElement.appendChild(closeButton);
	//windowContentElement.appendChild(titleElement);
    windowContentElement.appendChild(signatureTypeButtonsElement);
    windowContentElement.appendChild(formContainerElement);
    //windowContentElement.appendChild(saveCheckBox);
    windowContentElement.appendChild(actionButtonsElement);
    
    signatureTypeButtonsElement.appendChild(typeButton);
    signatureTypeButtonsElement.appendChild(drawButton);
    signatureTypeButtonsElement.appendChild(imageButton);

    // Default selection
    typeButton.click();
	
	document.body.appendChild(windowElement);
	$(".signature-picker-window .tooltip").tooltipster();
};	

SignatureAnnotationPicker.prototype.removeChildren = function(element) {
	var fc = element.firstChild;
	while (fc) {
		element.removeChild(fc);
		fc = element.firstChild;
	}
};	

SignatureAnnotationPicker.prototype.signHereAnnotationPicked = function() {
    var signHereAnnotation = new SignHereAnnotation();
    this.manageSignHereAnnotation(signHereAnnotation);
    this.dropZoneManager.defineAnnotation(signHereAnnotation);
};		

SignatureAnnotationPicker.prototype.manageSignHereAnnotation = function(signHereAnnotation) {
    var self = this;

    var signHereAnnotationEditor = new AnnotationEditor(); 
    signHereAnnotationEditor.open = function(annotation, callbackContext, saveCallback, cancelCallback) {
        self.openPicker(signHereAnnotation);
    };
    signHereAnnotation.setEditor(signHereAnnotationEditor);
};

SignatureAnnotationPicker.prototype.replacePlaceHolderAnnotation = function(placeHolderAnnotation, newAnnotation) {
	placeHolderAnnotation.remove();
	this.dropZoneManager.loadAnnotation(newAnnotation);
	this.dropZoneManager.onAnnotationSaved(newAnnotation);
};	

SignatureAnnotationPicker.prototype.replaceSignHereAnnotation = function(signHereAnnotation, newAnnotation) {
    var self = this;
    var defaultRemove = newAnnotation.remove;
    if (!defaultRemove) {
        defaultRemove = Object.getPrototypeOf(newAnnotation).remove;
    }    
    newAnnotation.remove = function(e) {
        if (defaultRemove.call(newAnnotation)) {
            self.dropZoneManager.loadAnnotation(signHereAnnotation);           
        }
    };    
    var defaultBind = newAnnotation.bind;
    if (!defaultBind) {
        defaultBind = Object.getPrototypeOf(newAnnotation).bind;
    }
    newAnnotation.bind = function(htmlElement) {
        defaultBind.call(newAnnotation, htmlElement);
        signHereAnnotation.remove();        
    };
    this.dropZoneManager.loadAnnotation(newAnnotation);
    this.dropZoneManager.onAnnotationSaved(newAnnotation);
};    

SignatureAnnotationPicker.prototype.i10n = function(key, defaultValue) {
	var value;
	var mozL10n = document.mozL10n || document.webL10n;
	if (mozL10n) {
        value = mozL10n.get(key, null, null);
        if (!value || value.indexOf("{{") == 0) {
            value = defaultValue;
        }
	} else {
		value = defaultValue;
	}
	return value;
};

SignatureAnnotationPicker.prototype.signHereAnnotationPicked = function() {
    var signHereAnnotation = new SignHereAnnotation();
    this.manageSignHereAnnotation(signHereAnnotation);
    this.dropZoneManager.defineAnnotation(signHereAnnotation);
};	

SignatureAnnotationPicker.prototype.drawAnnotationPicked = function(signHereAnnotation, signature, saveCallback) {
	console.debug("Draw " + (signature ? "signature" : "initials"));
};	

SignatureAnnotationPicker.prototype.imageAnnotationPicked = function(signHereAnnotation, signature, imageUrl) {
	console.debug("Use " + (signature ? "signature" : "initials"));
};	

SignatureAnnotationPicker.prototype.textAnnotationPicked = function(signHereAnnotation, signature, saveCallback) {
	console.debug("Type " + (signature ? "signature" : "initials"));
};	

SignatureAnnotationPicker.prototype.setVisible = function(htmlElement, visible) {
	if (visible) {
		htmlElement.style.display = "";
	} else {
		htmlElement.style.display = "none";
	}
};

 /**
  * Conserve aspect ratio of the original region. Useful when shrinking/enlarging
  * images to fit into a certain area.
  *
  * @param {Number} srcWidth width of source image
  * @param {Number} srcHeight height of source image
  * @param {Number} maxWidth maximum available width
  * @param {Number} maxHeight maximum available height
  * @return {Object} { width, height }
  */
SignatureAnnotationPicker.prototype.calculateAspectRatioFit = function(srcWidth, srcHeight, maxWidth, maxHeight) {
    var ratio = Math.min(maxWidth / srcWidth, maxHeight / srcHeight);
    return { width: srcWidth*ratio, height: srcHeight*ratio };
};

SignatureAnnotationPicker.prototype.closeWindow = function() {
	this.windowElement.parentNode.removeChild(this.windowElement);
};

SignatureAnnotationPicker.prototype.computeSizeBasedOnPlaceHolderAnnotation = function(placeHolderAnnotation) {
    var annotationWidth = placeHolderAnnotation.getWidth();
    var annotationHeight = placeHolderAnnotation.getHeight();
    var maxWidth = document.documentElement.clientWidth * 0.7;
    var maxHeight = 400;
    return this.calculateAspectRatioFit(annotationWidth, annotationHeight, maxWidth, maxHeight);
};

