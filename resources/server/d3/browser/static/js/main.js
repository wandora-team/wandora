(function() {

'use strict';

var tm, svg, zoomWrapper, force, linkAll, linkEnter, nodeAll, nodeEnter,
	pseudoAll, pseudoEnter;




var myAddress = window.location.protocol+'//'+window.location.hostname+(window.location.port ? ':'+window.location.port: '');




/**
 * Global config
 * @type {Object}
 */
var conf = {
	debug: false, //log stuff?
	serviceAddr: myAddress+'/jtm/', //wheres data?
	//#SVG size
	w: 900,
	h: 900,
	gw: 800,
	gh: 800,
	// Force layout conf
	linkDistance: 200,
	nodeSize: 20,
	strength: 1,
	charge: -200,
	// Label element conf
	rw: 200,
	rh: 20,
	labelLength: 23,
	// Special association and player types for type-instance associations
	typeInstance: "si:http://psi.topicmaps.org/iso13250/model/type-instance",
	type: "si:http://psi.topicmaps.org/iso13250/model/type",
	instance: "si:http://psi.topicmaps.org/iso13250/model/instance",
	// Colors for Nodes and links
	cols: d3.scale.category20(),
	assocCols: d3.scale.category20b()
};

/**
 * Basic console.log wrapper
 * @param  {Object} o Object to be logged. Note special handling for groups.
 */
var log = function(o) {
	if (!conf.debug || !console) return;

	if (o instanceof Group && console.group) {
		console.group(o.g);
		for (var i = 0; i < o.a.length; i++) {
			log(o.a[i]);
		}
		console.groupEnd();
	} else {
		console.log(o);
	}
};

/**
 * The Group class used for logging
 * @param  {String} g Group description
 * @param  {Array of objects} a An array of objects to log
 */
var Group = function(g, a) {
	this.g = g;
	this.a = a;
};

/**
 * The Node class used to represent topics in the graph
 * @param  {String} si   The SI of the topic
 * @param  {String} name The name to be displayed
 */
var Node = function(si, name) {
	this.si = si;
	this.name = name;
};

/**
 * The PseudoNode class used to glue associations with more than two players 
 * together.
 */
var PseudoNode = function() {};

/**
 * The TopicMap wrapper class used to hold the response data from the server.
 * @param  {Object} data The response from the server.
 */
var TopicMap = function(data) {
	this.as = data.associations;
	this.topics = data.topics;
	this.version = data.version;
	this.initNodes();
	this.initLinks();
};

/**
 * Basic getter
 * @return {Array of objects} All the topics in the TopicMap
 */
TopicMap.prototype.getTopics = function() {
	return this.topics;
};

/**
 * Basic getter
 * @return {Array of objects} All the associations in the TopicMap
 */
TopicMap.prototype.getAssociations = function() {
	return this.associations;
};

/**
 * Get the topic with the subject identifier si
 * @param  {String} si The subject identifier
 * @return {Object}    The topic with the identifier si
 */
TopicMap.prototype.getTopic = function(si) {
	var t, sis;
	for (var i = 0; i < this.topics.length; i++) {
		t = this.topics[i];
		sis = t.subject_identifiers;
		if (sis.indexOf(si) != -1) return t;
	}
};

/**
 * Get the Node with the subject identifier si
 * @param  {String} si The subject identifier
 * @return {Node}    The node with the identifier si
 */
TopicMap.prototype.getNode = function(si) {
	for (var i = this.nodes.length - 1; i >= 0; i--) {
		if (this.nodes[i].si == si) return this.nodes[i];
	}
};

/**
 * Create Nodes from the topics in the TopicMap
 */
TopicMap.prototype.initNodes = function() {
	this.nodes = [];
	for (var i = this.topics.length - 1; i >= 0; i--) {
		var si = this.topics[i].subject_identifiers[0],
			name = this.topics[i].names[0].value;
		this.nodes.push(new Node(si, name));
	}
};

/**
 * Create links from the associations in the TopicMap. Also create
 * PseudoNodes if we need them and creates indices for topic and association
 * types.
 */
TopicMap.prototype.initLinks = function() {

	/**
	 * Simple helper to see if an two player association (pair) is already 
	 * added.
	 * @param  {Array of pairs} arr The array against which to check
	 * @param  {String}			s1  The first element of the pair
	 * @param  {String}			s2  The second element of the pair
	 * @return {Boolean}		True if the array already has the pair
	 */
	function arrayHasPair(arr, s1, s2) {
		for (var i = arr.length - 1; i >= 0; i--) {
			var ap = arr[i].sort(),
				p = [s1, s2].sort();

			if (ap[0] == p[0] && ap[1] == p[1]) return true;
		}
		return false;
	}

	this.links = [];
	this.pseudoNodes = [];
	this.types = [];
	this.assocTypes = [];
	var pairs = [];
	var a, s1, s2, t1, t2;
	var i, j, k;

	/*
	Iterate over all the associations in the TopicMap
	 */
	for (i = this.as.length - 1; i >= 0; i--) {
		a = this.as[i];

		/*
		Update the association type array
		 */
		if (this.assocTypes.indexOf(a.type))
			this.assocTypes.push(a.type);

		/*
		Special case: type-instance associations are used to define types for
		topics so we use them to define topic types.
		 */
		if (a.type == conf.typeInstance) {
			var ins, type;
			for (j = 0; j < a.roles.length; j++) {
				if (a.roles[j].type == conf.type)
					type = a.roles[j].player;
				else if (a.roles[j].type == conf.instance)
					ins = a.roles[j].player;
			}
			if (ins.search("si:") !== 0) break;
			ins = ins.slice(3);
			if (type.search("si:") !== 0) break;
			type = type.slice(3);

			var t = this.getNode(ins);
			t.type = type;
			if (this.types.indexOf(type) == -1) this.types.push(type);

		/*
		The usual case: association has two players and is represented with a 
		single link.
		 */
		} else if (a.roles.length > 2) {

			var pn = new PseudoNode();
			pn.type = a.type;
			this.pseudoNodes.push(pn);
			for (k = a.roles.length - 1; k >= 0; k--) {
				s1 = a.roles[k].player;
				if (s1.search("si:") !== 0) break;
				s1 = s1.slice(3);
				var p = this.getNode(s1);
				this.links.push({
					source: pn,
					target: p,
					type: a.type
				});
			}

		/*
		Special case: more than two players. Here we need to add a PseudoNode
		to which all the players are connected to.
		 */
		} else if (a.roles.length == 2) {
			for (j = a.roles.length - 1; j >= 0; j--) {

				s1 = a.roles[0].player;
				if (s1.search("si:") !== 0) break;
				s1 = s1.slice(3);
				s2 = a.roles[1].player;
				if (s2.search("si:") !== 0) break;
				s2 = s2.slice(3);

				if (s1 != s2 && !arrayHasPair(pairs, s1, s2)) {
					pairs.push([s1, s2]);
					this.links.push({
						type: a.type,
						source: this.getNode(s1),
						target: this.getNode(s2)
					});
				}
			}
		}
	}
};

/**
 * Event listener for the 'tick' event from the force layout. Updates the node
 * and link coordinates according to the coordinates from the layout.
 */
function tick() {

	linkAll.attr({
		"x1": function(d) {
			return d.source.x;
		},
		"y1": function(d) {
			return d.source.y;
		},
		"x2": function(d) {
			return d.target.x;
		},
		"y2": function(d) {
			return d.target.y;
		}
	});

	nodeAll.attr("transform", function(d) {
		return "translate(" + d.x + ", " + d.y + ")";
	});

	pseudoAll.attr("transform", function(d) {
		return "translate(" + d.x + ", " + d.y + ")";
	});
}

/**
 * Initialize the graph element in the DOM. Also declare zoom behavior for the
 * zoomWrapper inside the SVG element and initialize the force layout.
 */
function initGraph() {
	svg = d3.select("#svg")
		.attr({
			width: conf.w,
			height: conf.h,
		}).call(d3.behavior.zoom().on("zoom", zoom));
	zoomWrapper = svg.append("g");

	force = d3.layout.force()
		.size([conf.gw, conf.gh])
		.linkDistance(conf.linkDistance)
		.charge(conf.charge)
		.nodes(tm.nodes.concat(tm.pseudoNodes))
		.links(tm.links);
}

/**
 * Event listener for clicking on Nodes. Update the location hash with the SI
 * from the clicked Node.
 * @param  {Node} d   The datum Node from the clicked element
 */
function onNodeClick(d) {
	//http://github.com/mbostock/d3/pull/1341
	if (!d3.event.defaultPrevented) {
		if (!d.si) return;

		var si = d.si;

		location.hash = si; // triggers hashchange

	}
}

/**
 * Event listener: add hover class to nodes on mouseover
 * @param  {Node} d The datum Node of the mouseovered element
 */
function onNodeMouseover(d) {
	/*jshint validthis:true */
	d3.select(this).classed("hover", true);

}

/**
 * Event listener: remove hover class from nodes on mouseout
 * @param  {Node} d The datum Node of the mouseouted (?) element
 */
function onNodeMouseout(d) {
	/*jshint validthis:true */
	d3.select(this).classed("hover", false);
}

/**
 * Event listener: update the zoom scale and translation of the .zoomWrapper on 
 * 'zoom' event on #SVG
 */
function zoom() {
	var t = d3.event.translate,
		s = d3.event.scale;

	zoomWrapper.attr("transform", "translate(" + t + ")scale(" + s + ")");
}

/**
 * Bind data to selections and add / remove nodes and links in the graph.
 */
function draw() {

	/*
	Bind tick and (re)start the force layout
	 */
	force.on("tick", tick).start();

	/*
	Bind data
	 */
	linkAll = zoomWrapper.selectAll(".link")
		.data(tm.links);

	nodeAll = zoomWrapper.selectAll(".node")
		.data(tm.nodes);

	pseudoAll = zoomWrapper.selectAll(".pseudo")
		.data(tm.pseudoNodes);

	log(new Group('enter node data', [nodeAll.enter()]));

	/*
	Add new Nodes
	 */
	nodeEnter = nodeAll.enter().append("g")
		.classed("node", true)
		.attr("data-id", function(d) {
			return d.si;
		})
		.on("mouseover", onNodeMouseover)
		.on("mouseout", onNodeMouseout)
		.on('touchstart', function(d) {
			d3.event.preventDefault(); // no scrolling
		})
		.on('click', onNodeClick)
		.call(force.drag);

	nodeEnter.append("rect")
		.attr({
			x: '-' + (conf.rw / 2) + 'px',
			y: '-' + (4 + conf.rh / 2) + 'px',
			width: conf.rw + 'px',
			height: conf.rh + 'px',
			fill: function(d) {
				var i = tm.types.indexOf(d.type);
				if (i > -1) return conf.cols(i);
				return "#999";
			}
		});

	nodeEnter.append("text")
		.text(function(d) {
			if (d.name)
				return (d.name.length > conf.labelLength) ?
					d.name.substring(0, conf.labelLength - 3) + "..." : d.name;
			return "";
		});


	log(new Group('exit node data', [nodeAll.exit()]));

	/*
	Remove old Nodes
	 */
	nodeAll.exit().remove();

	/*
	Add new PseudoNodes
	 */
	pseudoEnter = pseudoAll.enter().insert("g", ".node")
		.classed("pseudo", true);

	pseudoEnter.append("circle")
		.attr({
			r: 5,
			fill: function(d) {
				var i = tm.assocTypes.indexOf(d.type);
				return conf.assocCols(i);
			}
		});

	/*
	Remove old PseudoNodes
	 */
	pseudoAll.exit().remove();

	log(new Group('enter link data', [linkAll.enter()]));

	var enter = linkAll.enter();

	/*
	Add new links
	 */
	linkEnter = linkAll.enter().insert("line", ".node")
		.classed("link", true)
		.attr('stroke', function(d) {
			var i = tm.assocTypes.indexOf(d.type);
			return conf.assocCols(i);
		});

	log(new Group('exit link data', [linkAll.exit()]));

	/*
	Remove old links
	 */
	linkAll.exit().remove();

}

/**
 * Loads the JTM data from u. Usually called on hashchange
 * @param  {String} u The URL for the JTM resource provided by the JTM service module.
 */
function loadData(u) {
	d3.json(u, function(err, data) {

		if (typeof(data) != "undefined" && data.item_type == "topicmap") {
			tm = new TopicMap(data);
			force.nodes(tm.nodes.concat(tm.pseudoNodes));
			force.links(tm.links);
			draw();
		}

	});
}

/*
Construct the URL for the first fetch
 */
var u = conf.serviceAddr;
if (location.hash.length > 2) u += "?topic=" + location.hash.slice(1);

/*
On with the circus! Fetch data from u and start initializing stuff.
 */
d3.json(u, function(err, data) {

	var desc = d3.select('.description');
	d3.select('.info-toggle').on('click',function(){
		var isVisible = desc.classed('visible');
		desc.classed('visible',!isVisible);
		d3.select(this).classed('on',!isVisible);
	});

	/*
	All's fine? d3.json sets data to undefined on error.
	 */
	if (typeof(data) != "undefined" && data.item_type == "topicmap") {
		/**
		 * Yup!
		 */
		d3.select(".error").remove();
		tm = new TopicMap(data);
		initGraph();
		draw();

		window.onhashchange = function() {
			var si = location.hash.slice(1),
				u = conf.serviceAddr + "?topic=" + encodeURIComponent(si);
			loadData(u);
		};

	
	} else {
		/**
		* Nope! Display the error in the main content span.
		*/
		log(err);
		d3.select("#mainContent").insert("div", "#chart")
			.classed("error", true)
			.html(err.response);
	}

});

})();
