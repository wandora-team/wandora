var dumpKeys = function(obj){

    keys = obj.keys();
    Application.console.log("keys of obj:");

    for (var i = keys.length - 1; i >= 0; i--) {
        Application.console.log(keys[i]);
    }
};

var WandoraPlugin = {

    serverAddress: "",
    autoExtract: "",
    appInfo: null,
    parser: new DOMParser(),

    init: function() {
        var prefService = Components.classes["@mozilla.org/preferences-service;1"]
            .getService(Components.interfaces.nsIPrefService);
        var branch = prefService.getBranch("extensions.wandora.");
        branch.QueryInterface(Components.interfaces.nsIPrefBranch2);
        var plugin = this;
        var observer = {
            observe: function(aSubject, aTopic, aData) {
                plugin.preferencesChanged(aSubject, aTopic, aData);
            }
        };
        branch.addObserver("", observer, false);
        this.readPreferences();

        this.appInfo = Components.classes["@mozilla.org/xre/app-info;1"].getService(Components.interfaces.nsIXULAppInfo);
        if (this.appInfo.name == "Firefox") {
            var appcontent = document.getElementById("appcontent");
            appcontent.addEventListener("DOMContentLoaded", function(aEvent) {
                WandoraPlugin.onPageLoad(aEvent);
            }, true);
        } 
        else if (this.appInfo.name == "Thunderbird") {

        }
    },

    openConfig: function() {
        var params = {
            inn: {
                server: this.serverAddress,
                autoExtract: this.autoExtract
            },
            out: null
        };
        window.openDialog("chrome://wandora/content/config.xul", "", "chrome, dialog, modal, resizable=yes", params).focus();
        if (params.out) {
            var prefs = Components.classes["@mozilla.org/preferences-service;1"].
            getService(Components.interfaces.nsIPrefService);
            var branch = prefs.getBranch("extensions.wandora.");
            branch.setCharPref("serverAddress", params.out.server);
            branch.setCharPref("autoExtract", params.out.autoExtract);
        }
    },


    onPageLoad: function(aEvent) {
        var doc = aEvent.originalTarget;
        if (doc.nodeName == "#document") {
            var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].
            getService(Components.interfaces.nsIWindowMediator);
            var recentWindow = wm.getMostRecentWindow("navigator:browser");
            if (recentWindow) {
                if (recentWindow.content.document == doc) {
                    if (this.autoExtract !== null && this.autoExtract.length > 0) {
                        this.doExtract(this.autoExtract, true);
                    }
                }
            }
        }
    },

    preferencesChanged: function(aSubject, aTopic, aData) {
        this.readPreferences();
    },

    readPreferences: function() {
        var prefs = Components.classes["@mozilla.org/preferences-service;1"].
        getService(Components.interfaces.nsIPrefService);
        var branch = prefs.getBranch("extensions.wandora.");
        this.serverAddress = branch.getCharPref("serverAddress");
        if (this.serverAddress === null || this.serverAddress.length === 0) {
            this.serverAddress = "http://localhost:8898";
            branch.setCharPref("serverAddress", this.serverAddress);
        }
        this.autoExtract = branch.getCharPref("autoExtract");
        if (this.autoExtract === null) {
            this.autoExtract = "";
            branch.setCharPref("autoExtract", this.autoExtract);
        }
    },

    errorlog: function(message) {
        var consoleService = Components.classes["@mozilla.org/consoleservice;1"]
            .getService(Components.interfaces.nsIConsoleService);
        consoleService.logStringMessage("Wandora message: " + message);
    },

    getPath: function(ancestor, node) {
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
    MARK_SELECTION_START: '\u200B\u200B\u200B\u200B\u200B',
    MARK_SELECTION_END: '\u200B\u200B\u200B\u200B\u200B',

    getContent: function(selection, doc) {
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
            for (i=startPath.length-1; i >= 0; i--) {
                startContainer = startContainer.childNodes.item(startPath[i]);
            }
            for (i=endPath.length-1; i >= 0; i--) {
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
                if ((startOffset > 0 && startOffset < startContainer.data.length) || !startContainer.parentNode || !startContainer.parentNode.parentNode || startContainer != startContainer.parentNode.lastChild)
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
        } else {
            return {
                content: docElem.innerHTML,
                selectionStart: -1,
                selectionEnd: -1
            };
        }
    },

    getMessageSource: function(uri) {
        var messageService = messenger.messageServiceFromURI(uri);
        var messageStream = Components.classes["@mozilla.org/network/sync-stream-listener;1"].createInstance().QueryInterface(Components.interfaces.nsIInputStream);
        var inputStream = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance().QueryInterface(Components.interfaces.nsIScriptableInputStream);
        inputStream.init(messageStream);
        messageService.streamMessage(uri, messageStream, {}, null, false, null);

        var body = "";
        inputStream.available();
        while (inputStream.available()) {
            body = body + inputStream.read(512);
        }

        messageStream.close();
        inputStream.close();
        return body;
    },

    makePostData: function() {
        var data, wm;
        if (this.appInfo.name == "Firefox") {
            try {
                wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].
                getService(Components.interfaces.nsIWindowMediator);
                var recentWindow = wm.getMostRecentWindow("navigator:browser");
                if (recentWindow) {
                    var w = recentWindow.document.commandDispatcher.focusedWindow;
                    var contentStruct = this.getContent(w.getSelection(), w.document);
                    var url = w.document.location;
                    var content = contentStruct.content;
                    var selectionStart = contentStruct.selectionStart;
                    var selectionEnd = contentStruct.selectionEnd;
                    //            if(selectionStart==-1) alert(url+" no selection");
                    //            else alert(url+" "+content.substring(selectionStart,selectionEnd));
                    data = "page=" + encodeURIComponent(url) + "&content=" + encodeURIComponent(content) + "&application=" + encodeURIComponent(this.appInfo.name);
                    if (selectionStart != -1) {
                        data += "&selectionStart=" + encodeURIComponent(selectionStart);
                        data += "&selectionEnd=" + encodeURIComponent(selectionEnd);
                    }
                    return data;
                } else return null;
            } catch (err) {
                alert("Wandora message: " + err);
            }
        } 
        else if (this.appInfo.name == "Thunderbird") {
            try {
                wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].
                getService(Components.interfaces.nsIWindowMediator);
                var win = wm.getMostRecentWindow("mail:3pane");
                //            var messengerWindowList=wm.getEnumerator("mail:3pane");
                //            var messageWindowList=wm.getEnumerator("mail:messageWindow");
                var messageURI = null;
                try {
                    messageURI = GetFirstSelectedMessage();
                } catch (err) {
                    messageURI = gFolderDisplay.selectedMessageUris[0];
                }
                if (messageURI !== null && messageURI !== "") {
                    //              var win;
                    //              while (true) {
                    //                if (messengerWindowList.hasMoreElements()) {
                    //                  win = messengerWindowList.getNext();
                    //                } else if (messageWindowList.hasMoreElements()) {
                    //                  win = messageWindowList.getNext();
                    //                } else {
                    //                  break;
                    //                }
                    //                var loadedMessageURI = win.GetLoadedMessage();
                    //                if (loadedMessageURI != messageURI) continue;

                    var doc = null;
                    try {
                        doc = win.getMessageBrowser().docShell.contentViewer.DOMDocument;
                    } catch (err) {
                        doc = win.document.commandDispatcher.focusedWindow.document;
                    }
                    var msgSource = this.getMessageSource(messageURI);
                    //                alert(msgSource);

                    //                var messageBody = doc.body.textContent;
                    //                var selstart=-1;
                    //                var selend=-1;

                    data = "page=" + encodeURIComponent(messageURI) + "&content=" + encodeURIComponent(msgSource) + "&application=" + encodeURIComponent(this.appInfo.name);
                    var selection = null;
                    if (doc !== null && getSelection in doc) {
                        selection = doc.getSelection();
                    }
                    if (selection !== null && selection !== "") {
                        data += "&selectionText=" + encodeURIComponent(selection);
                    }
                    return data;
                    //                alert(url);
                    //                alert(content);
                    //                alert(selectionStart+","+selectionEnd);

                    /*                var selection=doc.getSelection();
                if(selection!=null && selection!="") {
                  // dirty and unreliable way of doing this, can we get selection start and end directly somehow?
                  selstart=messageBody.indexOf(selection);
                  if(selstart!=-1) selend=selstart+selection.length;
                }
                var data="page="+encodeURIComponent(messageURI)+"&content="+encodeURIComponent(messageBody)+"&application="+encodeURIComponent(this.appInfo.name);
                if(selstart!=-1){
                  data+="&selectionStart="+encodeURIComponent(selstart);
                  data+="&selectionEnd="+encodeURIComponent(selend);                  
                }
                return data;*/
                    //              }
                }
                return null;
            } catch (err) {
                alert("Wandora message: " + err);
            }
        }
    },

    loadList: function() {
        var data = this.makePostData();
        if (data !== null) {
            data += "&action=getextractors";
            var req = new XMLHttpRequest();
            req.open('POST', this.serverAddress + "/plugin", true);
            req.onreadystatechange = function(evt) {
                WandoraPlugin.onListLoaded(req, evt);
            };
            req.setRequestHeader("Content-type", "application/x-www-form-urlencoded;");
            req.setRequestHeader("Content-length", data.length);
            req.send(data);
            var button = document.getElementById("wandora-button");
            button.setAttribute("status", "loading");
        }
    },

    clearMenu: function() {
        var menu = document.getElementById("wandora-menupopup");
        while (menu.lastChild) {
            menu.removeChild(menu.lastChild);
        }
    },

    onWandoraButtonCommand: function() {
        this.loadList();
    },

    doExtract: function(method, silent) {
        var data = this.makePostData();
        Application.console.log(data);
        if (data !== null) {
            data += "&action=doextract&method=" + method;
            var req = new XMLHttpRequest();
            req.open('POST', this.serverAddress + "/plugin", true);
            req.onreadystatechange = function(evt) {
                WandoraPlugin.onExtractDone(req, evt, silent);
            };
            req.setRequestHeader("Content-type", "application/x-www-form-urlencoded;");
            req.setRequestHeader("Content-length", data.length);
            req.send(data);
            var button = document.getElementById("wandora-button");
            button.setAttribute("status", "loading");
        }
    },

    onExtractButton: function(event) {
        var method = event.currentTarget.getAttribute("label");
        this.doExtract(method, false);
    },

    onExtractDone: function(req, evt, silent) {
        if (req.readyState == 4) {
            var button = document.getElementById("wandora-button");
            button.setAttribute("status", "idle");
            if (req.status == 200) {
                var xmltext = req.responseText;
                var dom = WandoraPlugin.parser.parseFromString(xmltext, "application/xml");
                var resultcode = dom.evaluate('//wandoraplugin/resultcode', dom, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null );
                var resultcodeStr = resultcode.singleNodeValue.textContent;
                if (resultcodeStr == "0") {
                    if (!silent) this.showInfoPopup("Extraction OK");
                } 
                else {
                    var resultText = dom.evaluate('//wandoraplugin/resulttext', dom, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null );
                    var resultTextStr = resultText.singleNodeValue.textContent;
                    alert("Wandora message: Could not perform data extraction.\nServer returned " + resultcodeStr + ": " + resultTextStr);
                }
            }
            else if (req.status === 0)
                alert("Wandora message: Could not perform data extraction.\nUnable to connect to server.");
            else
                alert("Wandora message: Could not perform data extraction.\nServer returned http response " + req.status);
        }
    },

    showInfoPopup: function(text) {
        var desc = document.getElementById("wandora-info-text");
        var tip = document.getElementById("wandora-info");
        desc.value = text;
        var button = document.getElementById("wandora-button");
        if (this.appInfo.name == "Firefox")
            tip.openPopup(button, "after_start", 0, 0, false, false);
        else
            tip.showPopup(button, -1, -1, "popup", "bottomleft", "topleft");
        this.infoPopupCounter++;
        setTimeout("WandoraPlugin.hideInfoPopup(" + this.infoPopupCounter + ");", 5000);
    },

    infoPopupCounter: 0,
    hideInfoPopup: function(counter) {
        if (counter === null || counter == this.infoPopupCounter) {
            // counter makes sure that old timers don't close later popups
            var tip = document.getElementById("wandora-info");
            tip.hidePopup();
        }
    },


    onListLoaded: function(req, evt) {
        if (req.readyState == 4) {
            var button = document.getElementById("wandora-button");
            button.setAttribute("status", "idle");
            if (req.status == 200) {
                var xmltext = req.responseText;
                var dom = WandoraPlugin.parser.parseFromString(xmltext, "application/xml");
                var resultcode = dom.evaluate('//wandoraplugin/resultcode', dom, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null );
                var resultcodeStr = resultcode.singleNodeValue.textContent;
                if (resultcodeStr == "0") {
                    this.clearMenu();
                    var menu = document.getElementById("wandora-menupopup");
                    var item;

                    var methods = dom.evaluate('//wandoraplugin/method', dom, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null );
                    var listener = function(evt){ WandoraPlugin.onExtractButton(evt); };
                    for ( var i=0 ; i<methods.snapshotLength; i++ ) {
                        item = document.createElement("menuitem");
                        item.setAttribute("label", methods.snapshotItem(i).textContent);
                        item.addEventListener("command", listener, true);
                        menu.appendChild(item);
                    }

                    if (this.appInfo.name == "Firefox") {
                        item = document.createElement("menuseparator");
                        menu.appendChild(item);
                        item = document.createElement("menuitem");
                        item.setAttribute("label", "View Wandora's topic");
                        item.addEventListener("command", function(evt) {
                                WandoraPlugin.viewTopicMap();
                            }, true);
                        menu.appendChild(item);
                    }

                    if (this.appInfo.name == "Firefox") {
                        menu.openPopup(button, "after_start", 0, 0, false, false);
                    }
                    else { 
                        menu.showPopup(button, -1, -1, "popup", "bottomleft", "topleft"); 
                    }
                } 
                else {
                    var resultText = dom.evaluate('//wandoraplugin/resulttext', dom, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null );
                    var resultTextStr = resultText.singleNodeValue.textContent;
                    alert("Wandora message: Could not get extractor list.\nServer returned " + resultcodeStr + ": " + resultTextStr);
                }
            }
            else if (req.status === 0)
                alert("Wandora message: Could not get extractor list.\nUnable to connect to server.");
            else
                alert("Wandora message: Could not get extractor list.\nServer returned http response " + req.status);
        }
    },

    viewTopicMap: function() {
        var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].
        getService(Components.interfaces.nsIWindowMediator);
        var recentWindow = wm.getMostRecentWindow("navigator:browser");
        if (recentWindow) {
            var document = recentWindow.content.document;
            document.location = this.serverAddress + "/topic?topic=" + encodeURIComponent(document.location);
        }
    },

    /*    
    getSupportedFlavours : function(){
      var flavours = new FlavourSet();
//      flavours.appendFlavour("application/x-moz-url","nsIURL");
      flavours.appendFlavour("text/html");
//      flavours.appendFlavour("text/unicode");
      return flavours;
    },
  
    onDragOver: function(evt,flavour,session){},
    
    onDrop: function(evt,dropdata,session){
        if(dropdata.data!=""){
          alert(dropdata.data);
        }
    },
    */
    doReloadAllChrome: function() {
        try {
            Components.classes["@mozilla.org/chrome/chrome-registry;1"].getService(Components.interfaces.nsIXULChromeRegistry).reloadChrome();
        } catch (e) {
            alert("Wandora message: " + e);
        }
    }


};
window.addEventListener("load", function(e) {
    WandoraPlugin.init(e);
}, false);