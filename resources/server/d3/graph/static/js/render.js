/*
 * JAVASCRIPT RESOURCE FOR WANDORA EMBEDDED HTTP SERVER TEMPLATE
 * 
 * Copyright 2012 Eero Lehtonen / Grip Studios Interactive Oy
 *
 */

function draw(json,amount) {
    var w = 900, h = 900, node, link, root;

    var force = d3.layout.force()
            .on("tick", tick)
            .charge(-2000)
            .linkDistance(40)
            .size([ w, h ])
            .gravity(0.4);

    d3.select("#mainContent").style("height",window.innerHeight+"px");

    var cont = d3.select("#chart")
            .append("svg:svg")
            .attr("viewbox","0 0 600 600")
            .attr("height",window.innerHeight)
            .attr("width",window.innerWidth)
            .style("overflow","hidden");

    cont.html(
    "<defs>"+
    "<marker id=\"TriangleEnd\" viewBox=\"0 0 10 10\" refX=\"19\" refY=\"5\" "+
    "markerUnits=\"strokeWidth\" markerWidth=\"10\" markerHeight=\"10\" orient=\"auto\">"+
    "<path class=\"marker\" d=\"M 0 0 L 10 5 L 0 10 z\" /></marker>"+
    "<marker id=\"TriangleStart\" viewBox=\"0 0 10 10\" refX=\"-9\" refY=\"5\" "+
    "markerUnits=\"strokeWidth\" markerWidth=\"10\" markerHeight=\"10\" orient=\"auto\">"+
    "<path class=\"marker\" d=\"M 10 0 L 0 5 L 10 10 z\" /></marker>"+
    "</defs>"
    );

    vis = cont.append("svg:g");
    vis.attr("width", "600");
    vis.attr("height", "600");

    root = json;
    update();


    cont.call(d3.behavior.zoom().on("zoom", function(){
        var t = d3.event.translate;
        var s = d3.event.scale;
        vis.attr("transform", "translate(" + t + ")scale(" + s + ")");
    }));


   

    var desc = d3.select('.description');
    d3.select('.info-toggle').on('click',function(){
        var isVisible = desc.classed('visible');
        desc.classed('visible',!isVisible);
        d3.select(this).classed('on',!isVisible);
    });
    
    window.addEventListener('resize', onWindowResize, false);

    function onWindowResize(){
        cont.attr("height",window.innerHeight);
        cont.attr("width",window.innerWidth);
        d3.select("#mainContent").style("height",window.innerHeight+"px");
    }
    
    function update() {
        var nodes = json.nodes, links = json.links;

        // Restart the force layout.
        force.nodes(nodes).links(links).start();

        // Update the links…
        link = vis.selectAll("line.link").data(links, function(d) {
            return d.id;
        });

        // Enter any new links.
        link.enter().insert("svg:line", ".node")
        .attr("class", function(d) {
                return d.class;
        }).attr("x1", function(d) {
                return d.source.x;
        }).attr("y1", function(d) {
                return d.source.y;
        }).attr("x2", function(d) {
                return d.target.x;
        }).attr("y2", function(d) {
                return d.target.y;
        }).attr("id", function(d) {
                return d.id;
        }).attr("source",function(d){
                return d.source.id;
        }).attr("target",function(d){
                return d.target.id;
        }).style("stroke",function(d){
                return  d.color;
        });

        // Exit any old links.
        link.exit().remove();

        // Update the nodes…
        node = vis.selectAll("g.node").data(nodes, function(d) {
            return d.id;
        });

        node.enter().append("svg:g")
            .attr("class", "node")
            .attr("id", function(d){return d.id;})
            .attr("cx",
                function(d) {
                    return d.x;
                }
            )
            .attr("cy", function(d) {
                return d.y;
                }
            )
            .call(force.drag)
            .on("mouseover", nodeMouseover)
            .on("mouseout", nodeMouseout);

        // Enter any new nodes.
        node.append("svg:circle")
            .attr("class", "circle")
            .attr("r", 15);

        node.append("svg:text")
            .attr("class", "nodetext")
            .text(function(d) {return decodeURIComponent(d.name.replace(/\+/g," "));});

        node.exit().remove();
    }


    function tick() {
        link.attr("x1", function(d) {
            return d.source.x;
        }).attr("y1", function(d) {
            return d.source.y;
        }).attr("x2", function(d) {
            return d.target.x;
        }).attr("y2", function(d) {
            return d.target.y;
        });

        node.attr("transform", function(d) {
            return "translate(" + d.x + "," + d.y + ")";
        });
    }
	
        
        
    function nodeMouseover(d,i) {
        //force.stop();
        d3.selectAll("g.node").style("opacity","0.3");
        d3.selectAll("line").style("opacity","0.3");
        d3.select(this).style("opacity","1.0");
        var nodeId = d.id;
        var nodeLines = d3.selectAll("line").filter(function(d){
                return d.target.id == nodeId || d.source.id == nodeId;
        });
        nodeLines.style("opacity","1.0");
        nodeLines.each(function(d) {
            var line = d;
            var nodes = d3.selectAll("g.node").filter(function(d){
                    return d.id == line.target.id || d.id == line.source.id;
            });
            nodes.style("opacity","1.0");
        });
    }
	
    function nodeMouseout(d,i) {
        //force.resume();
        d3.selectAll("line").style("opacity","1.0");
        d3.selectAll("g.node").style("opacity","1.0");
    }
}
