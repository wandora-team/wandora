/*
 * JAVASCRIPT RESOURCE FOR WANDORA EMBEDDED HTTP SERVER TEMPLATE
 * 
 * Copyright 2012 Eero Lehtonen / Grip Studios Interactive Oy
 * 
 */

/*
 * Global variable for storing datum for the last clicked topic
 * 
 * The refresh function uses this to determine the base topic for the current zoom
 */
var lastClicked = null;

//"Main" function called from the Velocity template
function draw(json,colorScheme) {
  
  var desc = d3.select('.description');
  d3.select('.info-toggle').on('click',function(){
    var isVisible = desc.classed('visible');
    desc.classed('visible',!isVisible);
    d3.select(this).classed('on',!isVisible);
  });
  
	var  node;
	
	var w = window.innerWidth;
	var h = window.innerHeight;
	
	var x = d3.scale.linear().range([0, w]);
  var y = d3.scale.linear().range([0, h]);

  d3.select("#mainContent").style("height",window.innerHeight + "px");
	
	//Create the base SVG elements.
  var cont = d3.select("#chart")
    .append("svg:svg")
      .attr("viewbox","0 0 600 600")
      .attr("width","100%")
      .attr("height","100%")
  
  var vis = cont.append("g")
    .attr("transform","translate(0,0)")
  
  var partition = d3.layout.partition()
    .value(function(d) { return d.size; });
  
  //Use the supplied JSON to create SVG group objects.
  var g = vis.selectAll("g")
      .data(partition.nodes(json))
    .enter().append("svg:g")
      .attr("class","topic")
      .on("click", click)
      .attr("transform", function(d) { return "translate(" + x(d.y) + "," + y(d.x) + ")"; })
  
  kx = w / json.dx
  ky = h;
  
  var colors = new Array();
  
  //Append a rectangle and text to each group element created above.
  g.append("svg:rect")
      .attr("width", function(d){return d.dy * kx;})
      .attr("height", function(d) { return d.dx * ky; })
      .attr("fill", function(d){ return get_color(d,colors)})
      .on("click", click);
  
  g.append("svg:text")
    .attr("transform", function(d) { return "translate(8,"+y(d.dx)*0.5+")"})
    .style("opacity", function(d) { return d.dx * h > 15 ? 1 : 0; })
    .text(function(d) { return decodeURIComponent(d.name.replace(/\+/g," ")); })
    
  window.lastClicked = d3.select(".topic").property("__data__");
  
  //Function used to return a specific color for a topic based on the selected color scheme
  function get_color(d,c){
    switch(colorScheme){
      case 0: //has children? (default)
        return d.children != null ? "lightblue" : "grey";
      case 1: //color indicates topic type (class)
        if(!c.hasOwnProperty(d.type)) c[d.type] = get_random_color();
        return c[d.type];
      case 2: //color indicates topic's depth in the tree
        if(!c.hasOwnProperty(d.depth)) c[d.depth] = get_random_color();
        return c[d.depth];
      case 3: //color indicates the amount of children the topic has
        if(d.hasOwnProperty('children')) {
          if(!c.hasOwnProperty(d.children.length)) c[d.children.length] = get_random_color();
          return c[d.children.length];
        } else {
          if(!c.hasOwnProperty(0)) c[0] = get_random_color();
          return c[0];
        }
      case 4: //color indicates whether topic has a subject locator
        if(!c.hasOwnProperty(d.hasSL)) c[d.hasSL] = get_random_color();
        return c[d.hasSL];
      case 5: //color indicates whether topic has any occurrences
        if(!c.hasOwnProperty(d.hasOcc)) c[d.hasOcc] = get_random_color();
        return c[d.hasOcc];
      case 6: //color indicates whether topic has any variant names
        if(!c.hasOwnProperty(d.hasVS)) c[d.hasVS] = get_random_color();
        return c[d.hasVS];
      default:
        return d.children != null ? "lightblue" : "grey";
    }
  }
  
  //Simple function that return a random color
  function get_random_color() {
    return "hsl(" + Math.random() * 360 + ","+Math.random()*100+"%,50%)";
  }
  
  //Fired on click  
  function click(d) {
    
    w = window.innerWidth;
    h = window.innerHeight;
    
    if (!d.children) {
      kx = w / json.dx
      ky = h;
      
      x.domain([0, 1]).range([0, w]);
      y.domain([0, 1]);
      
      window.lastClicked = d3.select(".topic").property("__data__");
      
    } else {
      kx = (d.y ? w - 40 : w) / (1 - d.y);
      ky = h / d.dx;
      
      x.domain([d.y, 1]).range([d.y ? 40 : 0, w]);
      y.domain([d.x, d.x + d.dx]);
      
      window.lastClicked = d;
    }
    
    var t = g.transition()
        .duration(d3.event.altKey ? 7500 : 750)
        .attr("transform", function(d) { return "translate(" + x(d.y)+ "," + y(d.x) + ")"; });

    t.select("rect")
        .attr("width", d.dy * kx)
        .attr("height", function(d) { return d.dx * ky; });

    t.select("text")
        .attr("transform", function(d) { return "translate(8,"+d.dx*ky*0.5+")"})
        .style("opacity", function(d) { return d.dx * ky > 15 ? 1 : 0; });
       
   function transform(d) {
    return "translate(8," + d.dx * ky *window.innerHeight*0.5 + ")";
   }  
   d3.event.stopPropagation();
  }
  
}

//Fired on window resize
function refresh(){
  
  var w = window.innerWidth;
  var h = window.innerHeight;
  var d = window.lastClicked;
  
  kx = (d.y ? w - 40 : w) / (1 - d.y);
  ky = h / d.dx;
  
  x = d3.scale.linear().domain([d.y, 1]).range([d.y ? 40 : 0, w]);
  y = d3.scale.linear().range([0, window.innerHeight]).domain([d.x, d.x + d.dx]);
  
  var g = d3.selectAll(".topic");
  
  g.attr("transform", function(d) { return "translate(" + x(d.y)+ "," + y(d.x) + ")"; });
  
  g.selectAll("rect")
    .attr("width", d.dy * kx)
    .attr("height", function(d) { return d.dx * ky; });
    
  g.selectAll("text")
    .attr("transform", function(d) { return "translate(8,"+d.dx*ky*0.5+")"})
    .style("opacity", function(d) { return d.dx * ky > 12 ? 1 : 0; });

    
  d3.select("#mainContent").style("height",window.innerHeight + "px");
}
