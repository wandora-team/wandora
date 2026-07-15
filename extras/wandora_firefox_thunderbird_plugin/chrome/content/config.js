function onLoad(){
  document.getElementById("serverTextBox").value = window.arguments[0].inn.server;
  document.getElementById("autoExtractTextBox").value = window.arguments[0].inn.autoExtract;
}

function onOK(){
    window.arguments[0].out= {
        server:document.getElementById("serverTextBox").value,
        autoExtract:document.getElementById("autoExtractTextBox").value
    };
    return true;
}