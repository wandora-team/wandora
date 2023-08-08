/*
 * JAVASCRIPT RESOURCE FOR WANDORA EMBEDDED HTTP SERVER TEMPLATE
 *
 * Copyright 2012 Eero Lehtonen / Grip Studios Interactive Oy
 *
 */


function setupChart(json) {

  var desc = d3.select('.description');
  d3.select('.info-toggle').on('click',function(){
    var isVisible = desc.classed('visible');
    desc.classed('visible',!isVisible);
    d3.select(this).classed('on',!isVisible);
  });
  
  var margin = {
    top : 160,
    right : 0,
    bottom : 10,
    left : 160
  };

  window.matrix = [];
  window.nodes = json.nodes;
  window.n = nodes.length;
  
  window.x = d3.scale.ordinal().rangeBands([0, 30*n]);
  window.z = d3.scale.linear().domain([0, 4]).clamp(true);
  window.c = d3.scale.category10().domain(d3.range(10));
  
  var width = 30*n, height = 30*n;
  
  d3.select("#mainContent").style("height",window.innerHeight+"px");

  var svg = d3.select("#chart").append("svg")
    .attr("class","svg")
    .attr("height", window.innerHeight)
    .attr("width", window.innerWidth)
    .style("overflow","hidden")
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    
  window.addEventListener('resize', onWindowResize, false);

  function onWindowResize() {
    d3.select("svg").attr("height", window.innerHeight);
    d3.select("svg").attr("width", window.innerWidth);
    d3.select("#mainContent").style("height",window.innerHeight+"px");
  }
  
  //Pan.setup();
  
  d3.select("#chart svg").call(d3.behavior.zoom().on("zoom", function(){
    var t = d3.event.translate,
        s = d3.event.scale;

    svg.attr("transform", "translate(" + t + ")scale(" + s + ")");
  }));
      
  // Compute index per node.
  nodes.forEach(function(node, i) {
    node.index = i;
    node.count = 0;
    matrix[i] = d3.range(n).map(function(j) {
      return {
        x : j,
        y : i,
        z : 0
      };
    });
  });

  // Convert links to matrix; count character occurrences.
  json.links.forEach(function(link) {
    matrix[link.source][link.target].z = 4;
    matrix[link.target][link.source].z = 4;
    matrix[link.source][link.source].z = 4;
    matrix[link.target][link.target].z = 4;

    if(link.class == "assoc") {
      matrix[link.target][link.source].color = link.color;
      matrix[link.source][link.target].color = link.color;
      matrix[link.target][link.source].type = link.type;
      matrix[link.source][link.target].type = link.type;
      matrix[link.target][link.source].player1 = link.player1;
      matrix[link.source][link.target].player1 = link.player1;
      matrix[link.target][link.source].player2 = link.player2;
      matrix[link.source][link.target].player2 = link.player2;
      matrix[link.target][link.source].player1role = link.player1role;
      matrix[link.source][link.target].player1role = link.player1role;
      matrix[link.target][link.source].player2role = link.player2role;
      matrix[link.source][link.target].player2role = link.player2role;


    } else if(link.class == "type") {
      matrix[link.target][link.source].color = "red";
      matrix[link.source][link.target].color = "red";
      matrix[link.target][link.source].type = link.type;
      matrix[link.source][link.target].type = link.type;
      matrix[link.target][link.source].player1 = link.player1;
      matrix[link.source][link.target].player1 = link.player1;
      matrix[link.target][link.source].player2 = link.player2;
      matrix[link.source][link.target].player2 = link.player2;
      matrix[link.target][link.source].player1role = link.player1role;
      matrix[link.source][link.target].player1role = link.player1role;
      matrix[link.target][link.source].player2role = link.player2role;
      matrix[link.source][link.target].player2role = link.player2role;
    }

    if(link.source.x == link.source.y && link.target.x == link.target.y) {
      matrix[link.target][link.target].color = "grey";
    }

    nodes[link.source].count += link.value;
    nodes[link.target].count += link.value;
  });

  // Precompute the orders.
  var orders = {
    name : d3.range(n).sort(function(a, b) {
      return d3.ascending(nodes[a].name, nodes[b].name);
    }),
    count : d3.range(n).sort(function(a, b) {
      return nodes[b].count - nodes[a].count;
    }),
    group : d3.range(n).sort(function(a, b) {
      return nodes[b].group - nodes[a].group;
    })
  };

  // The default sort order.
  x.domain(orders.name);

  svg.append("rect")
    .attr("class", "matrixBackground")
    .attr("width", width)
    .attr("height", height);

  var row = svg.selectAll(".row").data(matrix).enter()
    .append("g")
      .attr("class", "row")
      .attr("transform", function(d, i) {return "translate(0," + x(i) + ")";})
      .each(row);

  row.append("line")
    .attr("x2", width);



  row.append("text")
    .attr("x", -6)
    .attr("y", x.rangeBand() / 2)
    .attr("dy", ".32em").attr("text-anchor", "end")
    .attr("class","matrixText")
    .text(function(d, i) {
      return decodeURIComponent(nodes[i].name.replace(/\+/g, " "));
    });

  var column = svg.selectAll(".column").data(matrix).enter().append("g")
    .attr("class", "column")
    .attr("transform", function(d, i) {
    return "translate(" + x(i) + ")rotate(-90)";
    });

  column.append("line")
    .attr("x1", -width);

  column.append("text")
    .attr("x", 6)
    .attr("y", x.rangeBand() / 2)
    .attr("dy", ".32em")
    .attr("text-anchor", "start")
    .attr("class","matrixText")
    .text(function(d, i) {
      return decodeURIComponent(nodes[i].name.replace(/\+/g, " "));
    });

  function row(row){
    var cell = d3.select(this).selectAll(".cell").data(row.filter(function(d) {return d.z;})).enter().append("rect")
     .attr("class", "cell")
     .attr("x", function(d) {return x(d.x);})
     .attr("width", x.rangeBand())
     .attr("height", x.rangeBand())
     .style("fill-opacity", function(d) {return z(d.z);})
     .style("fill", function(d) {return d.color;})
     .on("mouseover", mouseover)
     .on("mouseout", mouseout);
  }

  function mouseover(p){
    
    var verticalPlayer,horizontalPlayer,verticalRole,horizontalRole;

    d3.selectAll(".row text").classed("active", function(d, i) {return i == p.y;});
    d3.selectAll(".column text").classed("active", function(d, i) {return i == p.x;});
    if(p.x == p.y){
      svg.append("text")
        .attr("class","mouseOverText")
        .text("self")
        .attr("x",x(p.x)+35)
        .attr("y",x(p.y)+18)
        .style("font-size",10);
    } else {
      if(d3.selectAll(".row text").filter(function(d,i){return i==p.x;}).text() == decodeURIComponent(p.player1).replace(/\+/g, " ")){
        verticalPlayer = p.player1;
        verticalRole = p.player1role;
        horizontalPlayer = p.player2;
        horizontalRole = p.player2role;
      } else {
        verticalPlayer = p.player2;
        verticalRole = p.player2role;
        horizontalPlayer = p.player1;
        horizontalRole = p.player1role;
      }
      
      if(x(p.x) < 120){
        svg.append("text")
          .attr("class","mouseOverText")
          .text(decodeURIComponent(verticalRole + ": " + verticalPlayer).replace(/\+/g, " "))
          .attr("transform", "rotate(-90)")
          .attr("x",-x(p.y)+2)
          .attr("y",x(p.x)+x.rangeBand() / 2)
          .style("font-size",10);
        
        svg.append("text")
          .attr("class","mouseOverText")
          .text(decodeURIComponent(horizontalRole + ": " + horizontalPlayer).replace(/\+/g, " "))
          .attr("x",x(p.x)+32)
          .attr("y",x(p.y)+x.rangeBand() / 2)
          .style("font-size",10);
      } else if(x(p.y) < 120){
        svg.append("text")
          .attr("class","mouseOverText")
          .text(decodeURIComponent(verticalRole + ": " + verticalPlayer).replace(/\+/g, " "))
          .attr("transform", "rotate(-90)")
          .attr("x",-x(p.y)-32)
          .attr("y",x(p.x)+x.rangeBand() / 2)
          .attr("text-anchor","end")
          .style("font-size",10);
        
        svg.append("text")
          .attr("class","mouseOverText")
          .text(decodeURIComponent(horizontalRole + ": " + horizontalPlayer).replace(/\+/g, " "))
          .attr("x",x(p.x)-2)
          .attr("y",x(p.y)+x.rangeBand() / 2)
          .attr("text-anchor","end")
          .style("font-size",10);
      } else{
        svg.append("text")
          .attr("class","mouseOverText")
          .text(decodeURIComponent(verticalRole + ": " + verticalPlayer).replace(/\+/g, " "))
          .attr("transform", "rotate(-90)")
          .attr("x",-x(p.y)+2)
          .attr("y",x(p.x)+x.rangeBand() / 2)
          .style("font-size",10);
        
        svg.append("text")
          .attr("class","mouseOverText")
          .text(decodeURIComponent(horizontalRole + ": " + horizontalPlayer).replace(/\+/g, " "))
          .attr("x",x(p.x)-2)
          .attr("y",x(p.y)+x.rangeBand() / 2)
          .attr("text-anchor","end")
          .style("font-size",10);
      }
    }
  }

  function mouseout() {
    d3.selectAll("text").classed("active", false);
    d3.selectAll(".mouseOverText").remove();
  }

}