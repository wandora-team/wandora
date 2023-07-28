/*
 * JAVASCRIPT RESOURCE FOR WANDORA EMBEDDED HTTP SERVER TEMPLATE
 * 
 * Copyright 2012 Eero Lehtonen / Grip Studios Interactive Oy
 *
 */


var w = window.innerWidth - d3.select("#mainContent")[0][0].offsetLeft,
h = window.innerHeight,
colors = new Array();

function init(words,font,spiral,scale,rotOr,rotFrom,rotTo){
    console.log(words);
    var scl;
    if(scale == "sqrt"){
        scl = d3.scale.sqrt().range([5,100]);
    } else if (scale == "linear") {
        scl = d3.scale.linear().range([5,100]);
    } else { //log
        scl = d3.scale.log().range([5,100]);
    }

    var sclMin,sclMax;

    for (i = 0;i<words.length;i++){
        if(sclMin == null || words[i].size < sclMin) sclMin = words[i].size;
        if(sclMax == null || words[i].size > sclMax) sclMax = words[i].size;
    }

    scl.domain([sclMin,sclMax]);

    d3.layout.cloud()
        .size([w,h])
        .font(font)
        .padding(0)
        .spiral(spiral)
        .words(words.map(function(d){
            return {
                text: decodeURIComponent(d.name),
                size: d.size,
                SI: d.si
            };
        }))
        .rotate(function(){
            var rotation;
            if(rotOr == 0) rotation = 0;
            else {
                var fr = parseFloat(rotFrom),
                to = parseFloat(rotTo),
                n = parseInt(rotOr);
                rotation = fr + ~~( Math.random() * n ) * ( to - fr ) / (n-1) ;
            }
            return rotation;
        })
        .fontSize(function(d) {
          return Math.max(12,scl(d.size));
        })
        .on("end", draw)
        .start();



    function draw(words) {
        console.log(words);
        d3.select("#svg")
            .attr("width", w)
            .attr("height", h)
            .append("g")
            .attr("transform", "translate(" + [w>> 1, h >> 1] + ")")
            .selectAll("text")
            .data(words)
            .enter().append("text")
            .text(function(d) {return decodeURIComponent(d.text.replace(/\+/g," "));})
            .attr("class","cloudText")
            .attr("text-anchor", "middle")
            .attr("fill", function(){return "hsl(" + Math.random() * 360 + ","+Math.random()*100+"%,50%)";})
            .attr("cursor","pointer")
            .attr("transform", function(d) {return "translate(" + [d.x, d.y] + ")rotate("+d.rotate+")";})
            .style("font-size", function(d) {return d.size + "px";})
            .style("font-family",font)
            .style("opacity","1.0")
            .on("click", refreshURL)
            .on("mouseover",mouseover)
            .on("mouseout",mouseout);

        siPattern = new RegExp(/&si=([^&#]*)/);

        var lastClicked = (siPattern.test(window.location.href)) ? 
            d3.select("#svg"). selectAll("text").filter(function(d){
                return d.si == window.location.href.match(/&si=([^&#]*)/)[1]
            }) : d3.select("text")

        lastClicked.attr("id","lastClicked");

        function refreshURL(d) {
            var oldHref = document.location.href,
            queryString,
            newHref,
            queryStringPattern = new RegExp(/\?.*/),
            siPattern = new RegExp(/si=([^&#]*)/);

            if(queryStringPattern.test(oldHref)){ // has a query string
                queryString = queryStringPattern.exec(oldHref);
                if(siPattern.test(queryString)){ // has si argument
                    var oldSI = siPattern.exec(queryString)[0];
                    newHref = oldHref.replace(oldSI,"si="+escape(d.SI));
                } else {
                    newHref = oldHref + "&si="+escape(d.SI);
                }
            } else {
                newHref = oldHref + "?si="+escape(d.SI);
            }
            window.location.assign(newHref);
        }

        function mouseover(){
            d3.select(this)
            .style("font-weight","bold");
        }

        function mouseout(){
            d3.select(this)
            .style("font-weight","normal");
        }  
    }
  
    var desc = d3.select('.description');
    d3.select('.info-toggle').on('click',function(){
        var isVisible = desc.classed('visible');
        desc.classed('visible',!isVisible);
        d3.select(this).classed('on',!isVisible);
    });
}