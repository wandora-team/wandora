/*
 * JAVASCRIPT RESOURCE FOR WANDORA EMBEDDED HTTP SERVER TEMPLATE
 * 
 * Copyright 2012 Eero Lehtonen / Grip Studios Interactive Oy
 *
 */

function draw(json,finalChildCount,finalDepth,topicCount, singleChildCount) {

  var desc = d3.select('.description');
  d3.select('.info-toggle').on('click',function(){
    var isVisible = desc.classed('visible');
    desc.classed('visible',!isVisible);
    d3.select(this).classed('on',!isVisible);
  });
  
	var node, link, root, layout, diagonal, cont, vis;
	
  var isCluster = singleChildCount / topicCount > 0.5 || finalChildCount < finalDepth ?  true : false;


  if(isCluster) {
    
    layout = d3.layout.cluster()
      .size([window.innerHeight,window.innerWidth]);
    
    diagonal = d3.svg.diagonal()
      .projection(function(d) {return [d.y, d.x];});

    cont = d3.select("#chart")
      .append("svg:svg")
      .attr("viewbox","0 0 600 600")
      .style("overflow","hidden")
      .attr("height",window.innerHeight)
      .attr("width",window.innerWidth);


    vis = cont.append("svg:g");
      
    alert("Too few braches - changing layout to cluster...");
    
  } else {
    
    var radius = singleChildCount*90 + finalChildCount * 10 / Math.PI;
  
    layout = d3.layout.tree()
      .size([360, radius+130])
      .separation(function(a, b) { return (a.parent == b.parent ? 1 : 2) / a.depth; });
      
    diagonal = d3.svg.diagonal.radial()
      .projection(function(d) { return [d.y, d.x / 180 * Math.PI]; });
  
    cont = d3.select("#chart")
      .append("svg:svg")
      .attr("viewbox","0 0 600 600")
      .style("overflow","hidden")
      .attr("height",window.innerHeight)
      .attr("width",window.innerWidth);
  
    vis = cont.append("g")
      .attr("transform", "translate(" + window.innerHeight/2 + "," + window.innerWidth/2 + ")");
  
  }
  
  var nodes = layout.nodes(json);
  

  d3.select("#mainContent").style("height",window.innerHeight + "px");
  
  
  link = vis.selectAll("path.link")
      .data(layout.links(nodes))
    .enter().append("path")
      .attr("class", "link")
      .attr("d", diagonal);
  
  node = vis.selectAll("g.node")
      .data(nodes)
    .enter().append("g")
      .attr("class", "node")
      .attr("transform", function(d) {
        return isCluster ? "translate(" + d.y + "," + d.x + ")" : "rotate(" + (d.x - 90) + ")translate(" + d.y + ")";
      });
  
  node.append("circle")
      .attr("r", 4.5);
  
  node.append("text")
      .attr("dx", function(d) { return d.x < 180 || isCluster ? 8 : -8; })
      .attr("dy", ".31em")
      .attr("text-anchor", function(d) { return d.x < 180 || isCluster? "start" : "end"; })
      .attr("transform", function(d) {
        if (d.x < 180 || isCluster ) {
          return null;
        } else {
          return "rotate(" + (180 - d.singleChild*90)+")";
        }
      })
      .text(function(d) { return d.name ; });
	
  cont.call(d3.behavior.zoom().on("zoom", function(){
    var t = d3.event.translate,
        s = d3.event.scale;

    vis.attr("transform", "translate(" + t + ")scale(" + s + ")");
  }));
    
	window.addEventListener( 'resize', onWindowResize, false );
	
	function onWindowResize(){
		
		cont.attr("height",window.innerHeight);
		cont.attr("width",window.innerWidth);
    d3.select("#mainContent").style("height",window.innerHeight + "px");
	}

}