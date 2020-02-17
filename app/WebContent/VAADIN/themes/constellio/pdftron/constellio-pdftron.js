var webViewerInstance = null;
var annotationEnabled = null;
var isViewerReadOnly = null;

const FitWidth = "FitWidth";

(window.setWebViewerReadOnly = function(isReadOnly) {
    if(webViewerInstance !== null) {
        webViewerInstance.setReadOnly(isReadOnly);
        isViewerReadOnly = isReadOnly;
    }
});

(window.setEnableAnnotations = function(enableAnnotations) {
    if(webViewerInstance !== null) {
        if(enableAnnotations) {
               webViewerInstance.enableAnnotations();
               annotationEnabled=true;
        } else {
            webViewerInstance.disableAnnotations();
            annotationEnabled=false;
        }
    }
});

(window.rePullAnnotations = function() {
    if(webViewerInstance !== null) {
            $.get(documentAnnotationUrl, (data) => {

                ignoreAnnotationChange = true;
                if(data) {
                    webViewerInstance.docViewer.getAnnotationManager();
                }
                ignoreAnnotationChange = false;
            });
    }
});

(window.resetToCurrentValues = function() {
    let viewerControl = document.getElementById(canvasId).childNodes[0].contentWindow.readerControl;

    if(webViewerInstance != null && webViewerInstance != viewerControl) {
        webViewerInstance = viewerControl;
        webViewerInstance.setAnnotationUser(name);
        webViewerInstance.setAdminUser(admin);

        webViewerInstance.setLanguage(language);

        registerEvents(webViewerInstance);
        registerAnnotationLoaded(webViewerInstance);

        webViewerInstance.setFitMode(FitWidth);
        webViewerInstance.setReadOnly(isViewerReadOnly);
    }
});

(window.registerEvents = function(instance) {

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
                console.log(data);
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
            webViewerInstance = instance;
            instance.setAnnotationUser(name);
            instance.setAdminUser(admin);
            instance.setReadOnly(isReadOnly);
            instance.setLanguage(language);
            annotationEnabled=true;
            isViewerReadOnly = isReadOnly;

            registerEvents(instance);
            registerAnnotationLoaded(instance);
        });
})
