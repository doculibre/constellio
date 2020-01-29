var webViewerInstance = null;

const FitWidth = "FitWidth";

(window.setWebViewerReadOnly = function(isReadOnly) {
    if(webViewerInstance !== null) {
        webViewerInstance.setReadOnly(isReadOnly);
    }
});

(window.setEnableAnnotations = function(enableAnnotations) {
    if(webViewerInstance !== null) {
        if(enableAnnotations) {
               webViewerInstance.enableAnnotations();
        } else {
            webViewerInstance.disableAnnotations()
        }
    }
});

(window.rePullAnnotations = function() {
    if(webViewerInstance !== null) {
            $.get(documentAnnotationUrl, (data) => {

                ignoreAnnotationChange = true;
                if(data) {
                    webViewerInstance.annotManager.importAnnotations(data);
                }
                ignoreAnnotationChange = false;
            });
    }
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

            const {docViewer} = instance;
            const annotManager = instance.annotManager;

            annotManager.on('annotationChanged', async (event, annotations, action) => {
                if (action === 'add' || action === 'modify' || action === 'delete') {
                     if(ignoreAnnotationChange) {
                        return;
                     }

                    const annotationsFile = await annotManager.exportAnnotations({links: false, widgets: false});

                    $.post(documentAnnotationCallBack, {
                        'resourceKey': documentAnnotationRK,
                        'data': annotationsFile
                    })
                }
            });

            docViewer.on('annotationsLoaded', () => {
                instance.setFitMode(FitWidth);
                $.get(documentAnnotationUrl, (data) => {

                    if(data) {
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
