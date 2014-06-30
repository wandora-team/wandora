/*
 * JAVASCRIPT RESOURCE FOR WANDORA EMBEDDED HTTP SERVER TEMPLATE
 * 
 * Copyright 2013 Niko Laitinen / Grip Studios Interactive Oy
 *
 */

function draw(json) {

    var diameter = window.innerHeight,
        format = d3.format(",d"),
        color = d3.scale.category20c();

    var bubble = d3.layout.pack()
        .sort(null)
        .size([diameter, diameter])
        .padding(1.5);

    var svg = d3.select("#chart")
        .append("svg")
        .attr("width", diameter)
        .attr("height", diameter)
        .attr("class", "bubble");
    
    var nodes = bubble.nodes(json);
	
    var node = svg.selectAll(".node")
        .data(nodes.filter(function(d) {
            return !d.children;
        }))
        .enter().append("g")
        .attr("class", "node")
        .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

    //Add elements
    node.append("title").text(function(d) {
        return d.className + ": " + format(d.value);
    });

    node.append("circle").attr("r", function(d) {
        return d.r;
    }).style("fill", function(d) {
        return color(d.value);
    });

    node.append("text").attr("dy", ".3em").attr("text-anchor", "middle").text(function(d) {
        return d.className.substring(0, d.r / 3);
    });

    d3.select("#mainContent").style("height", window.innerHeight + "px");

    //Resize event
    window.addEventListener( 'resize', onWindowResize(), false );

    function onWindowResize() {
        svg.attr("height", window.innerHeight);
        svg.attr("width", window.innerWidth);
        d3.select("#mainContent").style("height", window.innerHeight + "px");
    }

}


window.onload = function() {
    var desc = d3.select('.description');
    d3.select('.info-toggle').on('click',function(){
        var isVisible = desc.classed('visible');
        desc.classed('visible',!isVisible);
        d3.select(this).classed('on',!isVisible);
    });
}