var webViewerInstance = null;

const FitWidth = "FitWidth";

(window.setWebViewerReadOnly = function(isReadOnly) {
    if(webViewerInstance !== null) {
        webViewerInstance.setReadOnly(isReadOnly);
    }
});

(window.setEnableAnnotations = function (enableAnnotations) {
    if (webViewerInstance !== null) {
        if (enableAnnotations) {
            webViewerInstance.enableAnnotations();
        } else {
            webViewerInstance.disableAnnotations()
        }
    }
});

(window.refreshReference = function () {
    let foundWebViewerInstance = document.getElementById(canvasId).childNodes[0].contentWindow.readerControl;

    if (foundWebViewerInstance != webViewerInstance) {
        webViewerInstance = foundWebViewerInstance;

        webViewerInstance.setAnnotationUser(name);
        webViewerInstance.setAdminUser(admin);
        webViewerInstance.setReadOnly(isReadOnly);
        webViewerInstance.setLanguage(language);
        debugger
        registerAnnotationChange(webViewerInstance.getBBAnnotManager());
        rePullAnnotations();
    }
});

(window.rePullAnnotations = function () {
    if (webViewerInstance !== null) {
        $.get(documentAnnotationUrl, (data) => {
            ignoreAnnotationChange = true;
            if (data) {
                webViewerInstance.setReadOnly(true);
                webViewerInstance.annotManager.importAnnotations(data);
            }
            ignoreAnnotationChange = false;
        });
    }
});

(window.registerAnnotationChange = function (annotManager) {
    annotManager.on('annotationChanged', async (event, annotations, action) => {
        if (action === 'add' || action === 'modify' || action === 'delete') {
            if (ignoreAnnotationChange) {
                return;
            }

            const annotationsFile = await annotManager.exportAnnotations({links: false, widgets: false});

            $.post(documentAnnotationCallBack, {
                'resourceKey': documentAnnotationRK,
                'data': annotationsFile
            })
        }
    });
});

$(() => {
    let mapParams;

    if (license) {
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

        const {docViewer} = instance;
        const annotManager = instance.annotManager;
        registerAnnotationChange(annotManager);

        docViewer.on('documentLoaded', () => {
            console.log('documentloaded')
        });

        docViewer.on('annotationsLoaded', () => {
            instance.setFitMode(FitWidth);
            $.get(documentAnnotationUrl, (data) => {

                if (data) {
                    annotManager.importAnnotations(data);
                }
                ignoreAnnotationChange = false;

                    if(searchTerm) {
                        instance.searchTextFull(searchTerm);
                    }
                });
            });
        });
})
