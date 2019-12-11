var btn = document.createElement('BUTTON');


var clipboard = new Clipboard(btn, {
    text: function() {
        return text;
    }
});
clipboard.on('success', function(e) {
    e.clearSelection();
});

btn.click();