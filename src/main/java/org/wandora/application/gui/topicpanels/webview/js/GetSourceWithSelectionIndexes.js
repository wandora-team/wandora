var WandoraWebview = {
    getPath : function(ancestor, node) {
        var n = node;
        var p = n.parentNode;
        if (n == ancestor || !p)
            return null;
        var path = [];
        if (!path)
            return null;
        do {
            for (var i = 0; i < p.childNodes.length; i++) {
                if (p.childNodes.item(i) == n) {
                    path.push(i);
                    break;
                }
            }
            n = p;
            p = n.parentNode;
        } while (n != ancestor && p);
        return path;
    },

    MARK_SELECTION_START : '\u200B\u200B\u200B\u200B\u200B',
    MARK_SELECTION_END : '\u200B\u200B\u200B\u200B\u200B',

    getSourceWithSelectionIndexes : function(selection, doc) {
        var docElem = doc.documentElement;
        if (selection.rangeCount <= 0)
            return {
                content: docElem.innerHTML,
                selectionStart: -1,
                selectionEnd: -1
            };
        var range = selection.getRangeAt(0);
        var startContainer = range.startContainer;
        var endContainer = range.endContainer;
        var startOffset = range.startOffset;
        var endOffset = range.endOffset;
        var startPath = this.getPath(docElem, startContainer);
        var endPath = this.getPath(docElem, endContainer);

        if (!range.collapsed && startContainer !== null && endContainer !== null) {
            docElem = docElem.cloneNode(true);
            startContainer = docElem;
            endContainer = docElem;
            var i;
            var tmpNode;
            for (i = startPath.length - 1; i >= 0; i--) {
                startContainer = startContainer.childNodes.item(startPath[i]);
            }
            for (i = endPath.length - 1; i >= 0; i--) {
                endContainer = endContainer.childNodes.item(endPath[i]);
            }
            if (endContainer.nodeType == Node.TEXT_NODE || endContainer.nodeType == Node.CDATA_SECTION_NODE) {
                if ((endOffset > 0 && endOffset < endContainer.data.length) || !endContainer.parentNode || !endContainer.parentNode.parentNode)
                    endContainer.insertData(endOffset, this.MARK_SELECTION_END);
                else {
                    tmpNode = doc.createTextNode(this.MARK_SELECTION_END);
                    endContainer = endContainer.parentNode;
                    if (endOffset === 0) endContainer.parentNode.insertBefore(tmpNode, endContainer);
                    else endContainer.parentNode.insertBefore(tmpNode, endContainer.nextSibling);
                }
            } 
            else {
                tmpNode = doc.createTextNode(this.MARK_SELECTION_END);
                endContainer.insertBefore(tmpNode, endContainer.childNodes.item(endOffset));
            }
            if (startContainer.nodeType == Node.TEXT_NODE || startContainer.nodeType == Node.CDATA_SECTION_NODE) {
                if ((startOffset > 0 && startOffset < startContainer.data.length) || !startContainer.parentNode || !startContainer.parentNode.parentNode ||
                    startContainer != startContainer.parentNode.lastChild)
                    startContainer.insertData(startOffset, this.MARK_SELECTION_START);
                else {
                    tmpNode = doc.createTextNode(this.MARK_SELECTION_START);
                    startContainer = startContainer.parentNode;
                    if (startOffset === 0) startContainer.parentNode.insertBefore(tmpNode, startContainer);
                    else startContainer.parentNode.insertBefore(tmpNode, startContainer.nextSibling);
                }
            } 
            else {
                tmpNode = doc.createTextNode(this.MARK_SELECTION_START);
                startContainer.insertBefore(tmpNode, startContainer.childNodes.item(startOffset));
            }

            var html = docElem.innerHTML;
            startOffset = html.indexOf(this.MARK_SELECTION_START);
            html = html.substring(0, startOffset) + html.substring(startOffset + this.MARK_SELECTION_START.length);
            endOffset = html.indexOf(this.MARK_SELECTION_END);
            html = html.substring(0, endOffset) + html.substring(endOffset + this.MARK_SELECTION_END.length);

            return {
                content: html,
                selectionStart: startOffset,
                selectionEnd: endOffset
            };
        } 
        else {
            return {
                content: docElem.innerHTML,
                selectionStart: -1,
                selectionEnd: -1
            };
        }
    }
}
WandoraWebview.getSourceWithSelectionIndexes(window.getSelection(), window.document);