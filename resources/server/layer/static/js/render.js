/*
 * JAVASCRIPT RESOURCE FOR WANDORA EMBEDDED HTTP SERVER TEMPLATE
 * 
 * Copyright 2012 Eero Lehtonen / Grip Studios Interactive Oy
 *
 */

var render = {
    color : d3.scale.category20c(),
    selectionColor : d3.scale.category20c(),

    browserCheck: function() {
        var uagent = navigator.userAgent.toLowerCase(),
            is_chrome = uagent.indexOf('chrome') > -1,
            is_safari = uagent.indexOf('safari') > -1,
            is_opera = uagent.indexOf('opera') > -1

        if(!is_chrome && !is_safari && !is_opera){
            alert("This visualization has only been verified to work on recent versions of Chrome, Safari and Opera");
        }
    },

    draw : function (json, nTopics, nLayers, nMergedTopics) {

		var width = window.innerWidth-250,
			height = window.innerHeight,
			r = nTopics * 10,
			w = r,
			h = w,
			x = d3.scale.linear().range([0, 2 * Math.PI]),
			y = d3.scale.linear().domain([0, 1]).range([-r, -r / 1.5]),
			p = 5,

			vis = d3.select("#chart").append("svg")
				.attr("width", width)
				.attr("height", height)
				.append("g")
				.attr("transform", "translate(" + width / 2 + "," + height / 2 + ") scale(" + Math.min(width, height) / (r * 2) + ")"),

			partition = d3.layout.partition()
				.sort(null)
				.value(function (d) {return 0.5; }),

			arc = d3.svg.arc()
				.startAngle(function (d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x))); })
				.endAngle(function (d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx))); })
			    .innerRadius(function (d) { return y(d.y); })
				.outerRadius(function (d) {
					return y(d.y + d.dy);
				}),

			group = vis.data([json.nodes]).selectAll("path")
				.data(partition.nodes)
				.enter().append("g")
				.attr("class", function (d, i) { return d.type; })
				.attr("id", function (d, i) { return d.id; })
				.on("mouseover", render.mouseover)
				.on("mouseout", render.mouseout)
				.on("click", render.click),

			groupPath = group.append("path")
				.attr("display", function (d) { return d.depth ? null : "none"; }) // hide inner ring
				.attr("d", arc)
				.attr("fill-rule", "evenodd")
				.attr("id", function (d, i) {return "path-" + d.id; })
				.attr("class", "path")
				.style("fill", function (d) { return render.color((d.children ? d : d.parent).name); }),

			layerText = d3.selectAll(".layer")
				.style("font-size",(y(0.5) - y(0.25)) * 0.9)
				.append("text")
				.attr("id", function (d, i) {return "text" + i; })
				.attr("index", function (d, i) {return i; })
				.attr("class", "text")
				.attr("x", (y(0.5) - y(0.25)) * 0.1)
				.attr("dy", -(y(0.5) - y(0.25)) * 0.2)
				.append("svg:textPath")
					.attr("xlink:href", function (d) {return "#path-" + d.id; })
					.text(function (d) {return d.depth !== 0 ? decodeURIComponent(d.name.replace(/\+/g, " ")) : null; }),

			links = json.links,
			link = vis.selectAll("line.link").data(links, function (d) {
				return d.target;
			});

		link.enter().append("svg:path")
			.attr("class", function (d) { return "link source-" + d.source + " target-" + d.target; })
			.attr("d", linkPath);

		Pan.setup();

		d3.select("#loading").remove();

		var statHTML = "<div class='stat'>" + nTopics + " topics in " + nLayers + " layers</div>";
		statHTML += "<div class='stat'>" + nMergedTopics + " merged topics</div>";
		statHTML += "<div class='stat'>" + Math.round(nMergedTopics / nTopics * 100) / 100 + " ratio of merged topics / total topics</div>";

		d3.select("#statistics").append("p").html(statHTML);

		function linkPath(d) {
			var srcd = d3.select("#" + d.source + " path").attr("d"),
				tard = d3.select("#" + d.target + " path").attr("d"),
				dd = "M" + dToXY(srcd)[0] + "," + dToXY(srcd)[1] + "Q 0 0 " + dToXY(tard)[0] + " " + dToXY(tard)[1];
			return dd;
		}

		function dToXY(d) {
			var m = d.indexOf("M"),
				a = d.indexOf("A"),
				l = d.indexOf("L"),
				xy1 = d.substr(m + 1, a - 1).split(","),
				xy2 = d.substr(a + 1, l - a - 1).split(" ")[3].split(","),
				xavr = (xy1[0] * 1 + xy2[0] * 1) / 2,
				yavr = (xy1[1] * 1 + xy2[1] * 1) / 2;
			return [xavr, yavr];
		}
                
                var desc = d3.select('.description');
                d3.select('.info-toggle').on('click',function(){
                    var isVisible = desc.classed('visible');
                    desc.classed('visible',!isVisible);
                    d3.select(this).classed('on',!isVisible);
                });
	},

	mouseover : function (d) {
		if (d.type !== "topic") {
			return;
		}
		var selectedLinks = d3.selectAll(".source-" + d.id + ",.target-" + d.id);
		if (selectedLinks[0].length > 0) {
			// Add mouseover class to links
			selectedLinks.classed("mouseover", true);
			// Find merged topics
			var selectedTopics = [];
			selectedLinks.each(function (d) {
				if (selectedTopics.indexOf(d.target) === -1) {
					selectedTopics.push(d.target);
				}
				if (selectedTopics.indexOf(d.source) === -1) {
					selectedTopics.push(d.source);
				}
			});
		} else {
			selectedTopics = [d.id];
		}
		// Add mouseover class to topics and create table row for "Now selected" table
		selectedTopics.forEach(function (element) {

			var topic = d3.select(".topic#" + element),
				datum = topic.datum();
			topic.classed("mouseover", true);
			d3.select("#" + element + " path").classed("mouseover", true);

			// If there is already a row for the topic bold it instead of adding a new one
			if (d3.select("tr#" + datum.id)[0][0]) {
				d3.select("tr#" + datum.id).classed("bold", true);
			} else {
				var name = decodeURIComponent(datum.name.replace(/\+/g, " ")),
					html = "<td>" + name + "</td><td>" + datum.inLayer + "</td>";
				d3.select("#selected tbody")
					.append("tr")
					.classed("mouseover", true)
					.attr("id", datum.id)
					.html(html)
					.on("mouseover", function () {render.mouseover(datum); })
					.on("mouseout", function () {render.mouseout(datum); });
			}
		});
	},

	mouseout: function (d) {
		if (d.type === "topic") {
			d3.selectAll("tr.mouseover:not(.clicked)").remove();
			d3.selectAll(".mouseover").classed("mouseover", false);
			d3.selectAll("tr.bold").classed("bold", false);
		}
	},

	click: function (d) {
		var c = render.selectionColor(d.name);
		d3.selectAll(".topic.mouseover:not(.clicked)").
			classed("clicked", true);
		d3.selectAll(".topic path.mouseover:not(.clicked)")
			.classed("clicked", true)
			.style("fill", c);
		d3.selectAll("path.link.mouseover:not(.clicked)")
			.classed("clicked", true)
			.style("stroke", c);
		d3.selectAll("tr.mouseover:not(.clicked)")
			.classed("clicked", true)
			.style("color", c);
		d3.selectAll(".mouseover").classed("mouseover", false);
	}
};

function onBodyResize() {
	d3.select("#chart svg")
		.attr("width", window.innerWidth)
		.attr("height", window.innerHeight);
}

function selectTopics() {
	var q = d3.select("#appendedInputButton").property("value");
	q = encodeURIComponent(q);
	var res = d3.selectAll(".topic").filter(function (d, i) { return (d.bn.indexOf(q) !== -1 || q.indexOf(d.bn) !== -1); });
	res.each(function (d) {
		var c = render.selectionColor(d.name),
			selectedLinks = d3.selectAll(".source-" + d.id + ",.target-" + d.id);
		if (selectedLinks[0].length > 0) {
			// Add clicked class to links
			selectedLinks
				.classed("clicked", true)
				.style("stroke", c);
			// Find merged topics
			var selectedTopics = [];
			selectedLinks.each(function (d) {
				if (selectedTopics.indexOf(d.target) === -1) {
					selectedTopics.push(d.target);
				}
				if (selectedTopics.indexOf(d.source) === -1) {
					selectedTopics.push(d.source);
				}
			});
		} else {
			selectedTopics = [d.id];
		}
		// Add mouseover class to topics and create table row for "Now selected" table
		selectedTopics.forEach(function (element) {

			var topic = d3.select(".topic#" + element),
				datum = topic.datum();
			topic.classed("clicked", true);
			d3.select("#" + element + " path")
				.classed("clicked", true)
				.style("fill", c);

			// Don't add a row for the topic if it already exists.
			if (!d3.select("tr#" + datum.id)[0][0]) {
				var name = decodeURIComponent(datum.name.replace(/\+/g, " "));
				html = "<td>" + name + "</td><td>" + datum.inLayer + "</td>";
				d3.select("#selected tbody")
					.append("tr")
					.classed("clicked", true)
					.attr("id", datum.id)
					.style("color", c)
					.html(html)
					.on("mouseover", function() {render.mouseover(datum); })
					.on("mouseout", function() {render.mouseout(datum); });
			}
		});
	})
	res.select("path").classed("clicked",true);
}

function clearTopics () {
    d3.selectAll("tr").remove();
    d3.selectAll(".topic .clicked").style("fill", function (d) {return render.color(d.parent.name); });
    d3.selectAll("path.link.clicked").style("stroke", null);
    d3.selectAll(".clicked, .mouseover")
        .classed("clicked", false)
        .classed("mouseover", false);
}


    