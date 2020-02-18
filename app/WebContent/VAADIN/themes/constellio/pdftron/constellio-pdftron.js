var webViewerInstance = false;
var annotationEnabled = true;
var isViewerReadOnly = true;

const FitWidth = "FitWidth";

(window.isWebViewerInstanceSet = function() {
    return window.webViewerInstance && true;
})

(window.setWebViewerReadOnly = function(isReadOnly) {
    if(isWebViewerInstanceSet()) {
        window.webViewerInstance.setReadOnly(isReadOnly);
        isViewerReadOnly = isReadOnly;
    }
});

(window.setEnableAnnotations = function(enableAnnotations) {
    if(isWebViewerInstanceSet()) {
        if(enableAnnotations) {
               window.webViewerInstance.enableAnnotations();
               annotationEnabled=true;
        } else {
            window.webViewerInstance.disableAnnotations();
            annotationEnabled=false;
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

    WebViewer(mapParams,
        document.getElementById(canvasId)).then(instance => {

            window.webViewerInstance = instance;

            instance.setAnnotationUser(name);
            instance.setAdminUser(admin);
            instance.setReadOnly(isReadOnly);
            instance.setLanguage(language);
            annotationEnabled=true;
            isViewerReadOnly = isReadOnly;

            registerAnnotationChanged(instance);
            registerAnnotationLoaded(instance);
        });
})
