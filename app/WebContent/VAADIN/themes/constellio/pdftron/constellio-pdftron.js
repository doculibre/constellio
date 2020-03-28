var webViewerInstance = false;
var annotationEnabled = true;
var isViewerReadOnly = true;

const FitWidth = "FitWidth";

(window.isWebViewerInstanceSet = function() {
    return window.webViewerInstance && true;
})

(window.isSignatureAnnotation = function(subject) {
    return subject.localeCompare("ConstellioSignature") == 0;
});

(window.isSignatureOrInitialsAnnotation = function(subject) {
    return isSignatureAnnotation(subject) || subject.localeCompare("ConstellioInitials") == 0;
});

(window.isEmpty = function(data) {
    return data.localeCompare("") == 0;
});

(window.setWebViewerReadOnly = function(isReadOnly) {
    if(isWebViewerInstanceSet()) {
        window.webViewerInstance.setReadOnly(isReadOnly);
        isViewerReadOnly = isReadOnly;
    }
});

(window.disableAnnotationSignatureTool = function() {
    if(isWebViewerInstanceSet()) {
        window.webViewerInstance.disableTools(['AnnotationCreateSignature']);
    }
});

(window.setEnableAnnotations = function(enableAnnotations) {
    if(isWebViewerInstanceSet()) {
        const {docViewer} = window.webViewerInstance;
        const annotManager = docViewer.getAnnotationManager();
        var annotations = annotManager.getAnnotationsList();

        for (var i = 0; i < annotations.length; i++) {
            if (enableAnnotations || isSignatureOrInitialsAnnotation(annotations[i].Subject)) {
                annotManager.showAnnotation(annotations[i]);
            } else {
                annotManager.hideAnnotation(annotations[i]);
            }
        }

        annotationEnabled = enableAnnotations;
    }
});

(window.setEnableSignatureAnnotations = function(enableAnnotations) {
    if(isWebViewerInstanceSet()) {
        const {docViewer} = window.webViewerInstance;
        const annotManager = docViewer.getAnnotationManager();
        var annotations = annotManager.getAnnotationsList();

        for (var i = 0; i < annotations.length; i++) {
            if (isSignatureAnnotation(annotations[i].Subject)) {
                if (enableAnnotations) {
                    annotManager.showAnnotation(annotations[i]);
                } else {
                    annotManager.hideAnnotation(annotations[i]);
                }
            }
        }
    }
});

(window.rePullAnnotations = function() {
    if(isWebViewerInstanceSet()) {
        $.get(documentAnnotationUrl, (data) => {
            ignoreAnnotationChange = true;
            if(data) {
                window.webViewerInstance.docViewer.getAnnotationManager().importAnnotations(data);
            }
            ignoreAnnotationChange = false;
        });
    }
});

(window.finalizeDocument = async function() {
    if(isWebViewerInstanceSet()) {
        const { docViewer, annotManager } = window.webViewerInstance;

        setEnableSignatureAnnotations(false);
        const xfdfString = await annotManager.exportAnnotations({links: false, widgets: false});
        setEnableSignatureAnnotations(true);

        const doc = docViewer.getDocument();
        const data = await doc.getFileData({
            // saves the document with annotations in it
            xfdfString,
            downloadType: 'pdf'
        });
        const arr = new Uint8Array(data);
        const blob = new Blob([arr], { type: 'application/pdf' });

        var reader = new FileReader();
        var base64data;
        reader.onloadend = function() {
            base64data = reader.result;

            $.post(documentAnnotationCallBack, {
                'resourceKey': documentAnnotationRK,
                'blob': base64data
            });
        }
        reader.readAsDataURL(blob);
    }
});

(window.resetToCurrentValues = function() {
    let viewerControl = document.getElementById(canvasId).childNodes[0].contentWindow.readerControl;

    if(isWebViewerInstanceSet() && window.webViewerInstance != viewerControl) {
        window.webViewerInstance = viewerControl;
        window.webViewerInstance.setAnnotationUser(name);
        window.webViewerInstance.setAdminUser(admin);

        window.webViewerInstance.setLanguage(language);

        registerAnnotationChanged(window.webViewerInstance);
        registerAnnotationLoaded(window.webViewerInstance);

        window.webViewerInstance.setFitMode(FitWidth);
        window.webViewerInstance.setReadOnly(isViewerReadOnly);
    }
});

(window.registerAnnotationChanged = function(instance) {

    const {docViewer} = instance;
    const annotManager = docViewer.getAnnotationManager();

     annotManager.on('annotationChanged', async (event, annotations, action) => {
        if (action === 'add' || action === 'modify' || action === 'delete') {
             if(ignoreAnnotationChange) {
                return;
             }

            const annotationsFile = await annotManager.exportAnnotations({links: false, widgets: false});

            $.post(documentAnnotationCallBack, {
                'resourceKey': documentAnnotationRK,
                'data': annotationsFile
            });
        }
    });
});

(window.registerAnnotationLoaded = function(instance) {

    const {docViewer} = instance;

    docViewer.on('annotationsLoaded', () => {
        instance.setFitMode(FitWidth);
        $.get(documentAnnotationUrl, (data) => {
            const annotManager = docViewer.getAnnotationManager();

            ignoreAnnotationChange = true;
            if(data) {
                annotManager.importAnnotations(data);
            }
            ignoreAnnotationChange = false;

            setEnableAnnotations(annotationEnabled)

            if(searchTerm) {
                instance.searchTextFull(searchTerm);
            }

            instance.setReadOnly(isViewerReadOnly);
        });
    });
});

(window.createConstellioSignatureTool = function(instance, signatureCaption, signatureImage) {
    const { Annotations, Tools, annotManager, docViewer } = instance;

    // Create custom annotation
    const ConstellioSignatureAnnotation = function() {
        Annotations.StampAnnotation.call(this);
        this.Subject = 'ConstellioSignature';
        this.ImageData  = signatureImage;
    };

    ConstellioSignatureAnnotation.prototype = new Annotations.StampAnnotation();
    ConstellioSignatureAnnotation.prototype.elementName = 'stamp';


    // Create custom tool
    const ConstellioSignatureCreateTool = function(docViewer) {
        Tools.GenericAnnotationCreateTool.call(this, docViewer, ConstellioSignatureAnnotation);
    };

    ConstellioSignatureCreateTool.prototype = new Tools.GenericAnnotationCreateTool();


    // Register custom tool
    const constellioSignatureToolName = 'AnnotationCreateConstellioSignature';

    //annotManager.registerAnnotationType(ConstellioSignatureAnnotation.prototype.elementName, ConstellioSignatureAnnotation);

    const constellioSignatureTool = new ConstellioSignatureCreateTool(docViewer);
    instance.registerTool({
        toolName: constellioSignatureToolName,
        toolObject: constellioSignatureTool,
        buttonImage: '/constellio/VAADIN/themes/constellio/pdftron/lib/ui/assets/hand-outline-gesture.png',
        buttonName: 'constellioSignatureButton',
        tooltip: signatureCaption
    }, ConstellioSignatureAnnotation);

    instance.setHeaderItems(header => {
        const constellioSignatureButton = {
            type: 'toolButton',
            toolName: constellioSignatureToolName
        };
        if (!isEmpty(signatureImage)) {
            header.get('freeHandToolGroupButton').insertBefore(constellioSignatureButton);
        }
    });

    docViewer.on('documentLoaded', () => {
        // set the tool mode to our tool so that we can start using it right away
        instance.setToolMode(constellioSignatureToolName);
    });
});

(window.createConstellioInitialsTool = function(instance, initialsCaption, initialsImage) {
    const { Annotations, Tools, annotManager, docViewer } = instance;

    // Create custom annotation
    const ConstellioInitialsAnnotation = function() {
        Annotations.StampAnnotation.call(this);
        this.Subject = 'ConstellioInitials';
        this.ImageData  = initialsImage;
    };

    ConstellioInitialsAnnotation.prototype = new Annotations.StampAnnotation();
    ConstellioInitialsAnnotation.prototype.elementName = 'stamp';


    // Create custom tool
    const ConstellioInitialsCreateTool = function(docViewer) {
        Tools.GenericAnnotationCreateTool.call(this, docViewer, ConstellioInitialsAnnotation);
    };

    ConstellioInitialsCreateTool.prototype = new Tools.GenericAnnotationCreateTool();


    // Register custom tool
    const constellioInitialsToolName = 'AnnotationCreateConstellioInitials';

    //annotManager.registerAnnotationType(ConstellioInitialsAnnotation.prototype.elementName, ConstellioInitialsAnnotation);

    const constellioInitialsTool = new ConstellioInitialsCreateTool(docViewer);
    instance.registerTool({
        toolName: constellioInitialsToolName,
        toolObject: constellioInitialsTool,
        buttonImage: '/constellio/VAADIN/themes/constellio/pdftron/lib/ui/assets/ic_annotation_signature_black_24px.svg',
        buttonName: 'constellioInitialsButton',
        tooltip: initialsCaption
    }, ConstellioInitialsAnnotation);

    instance.setHeaderItems(header => {
        const constellioInitialsButton = {
            type: 'toolButton',
            toolName: constellioInitialsToolName
        };
        if (!isEmpty(initialsImage)) {
            header.get('freeHandToolGroupButton').insertBefore(constellioInitialsButton);
        }
    });

    docViewer.on('documentLoaded', () => {
        // set the tool mode to our tool so that we can start using it right away
        instance.setToolMode(constellioInitialsToolName);
    });
});

$(() => {
    let mapParams;

     if(license) {
         mapParams = {
                licenseKey: license,
                path: '/constellio/VAADIN/themes/constellio/pdftron/lib',
                initialDoc: documentContent,
        }
    } else {
         mapParams = {
                path: '/constellio/VAADIN/themes/constellio/pdftron/lib',
                initialDoc: documentContent,
        }
    }

    WebViewer(mapParams, document.getElementById(canvasId)).then(instance => {
        window.webViewerInstance = instance;

        instance.setAnnotationUser(name);
        instance.setAdminUser(admin);
        instance.setReadOnly(isReadOnly);
        instance.setLanguage(language);
        annotationEnabled=true;
        isViewerReadOnly = isReadOnly;

        registerAnnotationChanged(instance);
        registerAnnotationLoaded(instance);

        createConstellioSignatureTool(instance, signatureCaption, signatureImage);
        createConstellioInitialsTool(instance, initialsCaption, initialsImage);
    });
})
