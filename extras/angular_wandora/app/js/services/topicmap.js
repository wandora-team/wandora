(function() {
	'use strict';

	/**
	 * Add indexOf for <IE8
	 */

	if (!Array.prototype.indexOf) {
		Array.prototype.indexOf = function(elt /*, from*/ ) {
			var len = this.length;

			var from = Number(arguments[1]) || 0;
			from = (from < 0) ? Math.ceil(from) : Math.floor(from);
			if (from < 0)
				from += len;

			for (; from < len; from++) {
				if (from in this &&
					this[from] === elt)
					return from;
			}
			return -1;
		};
	}

	/**
	 * The factory responsible for providing the topicmapservice.
	 * @param  $http	The $http service from angular
	 */
	var topicmapFactory = function($http) {

		/**
		 * Helper stuff
		 */

		function unwrapPlayers(as, role) {
			var ret = [];
			for (var i in as) {
				var a = as[i];
				var p = a.getPlayer(role);
				if (p) {
					ret.push(p);
				}
			}
			return ret;
		}

		function topicSiComparator(t1, t2) {
			if (t1 == t2) return 0;
			if (!t1.sis || t1.sis.length) {
				if (!t2.sis || t2.sis.length) return 0;
				else return 1;
			} else if (!t2.sis || t2.sis.length) return -1;
			var si1 = t1.sis[0].toLowerCase();
			var si2 = t2.sis[0].toLowerCase();
			if (si1 < si2) return -1;
			else if (si1 == si2) return 0;
			else return 1;
		}

		function topicNameComparator(t1, t2) {
			if (t1 == t2) return 0;
			if (!t1.bns || t1.bns.length) {
				if (!t2.bns || t2.bns.length) return 0;
				else return 1;
			} else if (!t2.bns || t2.bns.length) return -1;
			var n1 = t1.bns[0].toLowerCase();
			var n2 = t2.bns[0].toLowerCase();
			if (n1 < n2) return -1;
			else if (n1 == n2) return topicSiComparator(t1, t2);
			else return 1;
		}

		function playerRoleComparator(p1, p2) {
			return topicNameComparator(p1.role, p2.role);
		}

		function associationComparator(a1, a2) {
			var c = topicNameComparator(a1.type, a2.type);
			if (c) return c;

			if (!a1.playersSorted) {
				a1.players.sort(playerRoleComparator);
				a1.playersSorted = true;
			}
			if (!a2.playersSorted) {
				a2.players.sort(playerRoleComparator);
				a2.playersSorted = true;
			}
			for (var i = 0;; i++) {
				if (i >= a1.players.length) {
					if (i >= a2.players.length) return 0;
					else return -1;
				} else if (i >= a2.players.length) return 1;
				c = topicNameComparator(
					a1.players[i].role,
					a2.players[i].role
				);
				if (c) return c;
				c = topicNameComparator(
					a1.players[i].member,
					a2.players[i].member
				);
				if (c) return c;
			}
		}



		/**
		 * The Topic Class
		 * @param {TopicMap} tm
		 */
		function Topic(tm) {
			this.tm = tm;
			this.sis = []; // of Strings
			this.sls = []; // of Strings
			this.bns = []; // of Strings
			this.os = []; // of Occurrences
			this.as = []; // of Associations (where this topic is a member)
			this.vns = []; // of Variants
		}

		Topic.prototype.addSi = function(si) {
			if (!(si in this.sis)) {
				if (si in this.tm.siIndex) {
					this.tm.mergeTopics(this, this.tm.siIndex[si]);
				} else {
					this.sis.push(si);
					this.tm.siIndex[si] = this;
				}
			}
		};

		Topic.prototype.isInstanceOf = function(type) {
			return this.getTypes().indexOf(type) >= 0;
		};

		Topic.prototype.getAssociations = function(type, role) {
			var ret = [];
			for (var i in this.as) {
				var a = this.as[i];
				if (type && a.type != type) {
					continue;
				}
				if (role && a.getPlayer(role) != this) {
					continue;
				}
				ret.push(a);
			}
			return ret;
		};

		Topic.prototype.getAllPlayers = function() {
			var ret = [];
			for (var i in this.as) {
				var a = this.as[i];
				for (var j in a.players) {
					var p = a.players[j];
					if (p.member != this) {
						ret.push(p.member);
					}
				}
			}
			return ret;
		};

		Topic.prototype.getSuperClasses = function() {
			if (this.superClasses) {
				return this.superClasses;
			}

			var supSub = this.tm.getTopic(SI_SUPER_SUB);
			var sup = this.tm.getTopic(SI_SUPER);
			var sub = this.tm.getTopic(SI_SUB);
			if (!supSub || !sup || !sub) {
				this.superClasses = [];
			} else {
				this.superClasses = this.getPlayers(supSub, sub, sup);
			}

			for (var i = 0; i < this.superClasses.length; i++) {
				var t = this.superClasses[i];
				var t2 = t.getTypes();
				for (var j = 0; j < t2.length; j++) {
					if (this.superClasses.indexOf(t2[j]) < 0) {
						this.superClasses.push(t2[j]);
					}
				}
			}
			return this.superClasses;
		};

		Topic.prototype.isOfType = function(type) {
			return this.getTypes().indexOf(type) >= 0;
		};

		Topic.prototype.isOfSuperType = function(type) {
			return this.getSuperTypes().indexOf(type) >= 0;
		};

		Topic.prototype.getPlayers = function(type, thisrole, otherrole) {
			var assocs = this.getAssociations(type, thisrole);
			return unwrapPlayers(assocs, otherrole);
		};

		Topic.prototype.getSuperTypes = function() {
			if (this.superTypes) {
				return this.superTypes;
			}
			// slice makes a copy of the array
			this.superTypes = this.getTypes().slice();
			var l = this.superTypes.length;
			for (var i = 0; i < l; i++) {
				var t2 = this.superTypes[i].getSuperClasses();
				for (var j = 0; j < t2.length; j++) {
					if (this.superTypes.indexOf(t2[j]) < 0) {
						this.superTypes.push(t2[j]);
					}
				}
			}
			return this.superTypes;
		};

		Topic.prototype.getTypes = function() {
			if (this.types) {
				return this.types;
			}
			var typeInstance = this.tm.getTopic(SI_TYPE_INSTANCE);
			var type = this.tm.getTopic(SI_TYPE);
			var instance = this.tm.getTopic(SI_INSTANCE);
			if (!typeInstance || !type || !instance) {
				this.types = [];
			} else {
				this.types = this.getPlayers(typeInstance, instance, type);
			}
			return this.types;
		};

		Topic.prototype.getInstances = function() {
			if (this.instances) {
				return this.instances;
			}
			var typeInstance = this.tm.getTopic(SI_TYPE_INSTANCE);
			var type = this.tm.getTopic(SI_TYPE);
			var instance = this.tm.getTopic(SI_INSTANCE);
			if (!typeInstance || !type || !instance) {
				this.instances = [];
			} else {
				this.instances = this.getPlayers(typeInstance, type, instance);
			}
			return this.instances;
		};

		Topic.prototype.getOccurrence = function(type, lang) {
			// currently ignores lang
			for (var i in this.os) {
				var o = this.os[i];
				if (o.type == type) {
					return o.value;
				}
			}
			return null;
		};

		Topic.prototype.getOccurrenceTypes = function() {
			var ret = [];
			for (var i in this.os) {
				var o = this.os[i];
				if (ret.indexOf(o.type) < 0) ret.push(o.type);
			}
			return ret;
		};

		function scopesEqual(s1, s2) {
			if (!(s1 && s2)) return false;
			if (s1.length != s2.length) return false;
			for (var t in s1) {
				if (s2.indexOf(t) < 0) return false;
			}
			return true;
		}

		// Array of Topics
		Topic.prototype.getVariant = function(scope) {
			for (var vn in this.vns) {
				if (scopesEqual(vn.scope, scope)) {
					return vn;
				}
			}
			return null;
		};

		Topic.prototype.getDisplayName = function(langSI) {
			var lang = this.tm.getOrCreateTopic(langSI);
			for (var i = 0; i < this.vns.length; i++) {
				var variant = this.vns[i];
				for (var j = 0; j < variant.scope.length; j++) {
					if (variant.scope[j] == lang) return variant.value;
				}
			}
			return this.bns[0];
		};

		/**
		 * The Occurrence class 
		 * @param Topic 			topic
		 * @param String 			type
		 * @param Array of Topics 	scope
		 * @param String 			value
		 */
		function Occurrence(topic, type, scope, value) {
			this.topic = topic;

			this.type = (type) ? type : null;
			this.scope = (scope) ? scope : [];
			this.value = (value) ? value : "";

		}

		/**
		 * The Association Clas
		 * @param {Topic} type
		 * @param {Array of Players} players
		 */
		function Association(type, players) {
			this.type = (type) ? type : null;
			this.players = (players) ? players : [];
			this.playersSorted = false;

		}

		Association.prototype.getPlayer = function(role) {
			for (var i in this.players) {
				var p = this.players[i];
				if (p.role == role) {
					return p.member;
				}
			}
			return null;
		};

		/**
		 * The Player class
		 * @param {Topic} role
		 * @param {Topic} member
		 */
		function Player(role, member) {
			this.role = role;
			this.member = member;
		}

		/**
		 * The Variant Class
		 * @param {Array of Topics} scope
		 * @param {String} value
		 */
		function Variant(scope, value) {
			this.scope = scope;
			this.value = value;
		}

		/**
		 * The TopicMap class
		 */
		function TopicMap() {
			this.siIndex = {};
			this.status = 'not loaded';
		}

		TopicMap.prototype.getTopic = function(si) {
			if (si in this.siIndex) {
				return this.siIndex[si];
			} else {
				return null;
			}
		};

		TopicMap.prototype.getOrCreateTopic = function(si) {
			if (si in this.siIndex) {
				return this.siIndex[si];
			} else {
				var t = new Topic(this);
				t.addSi(si); // adds the topic to siIndex
				return t;
			}
		};

		TopicMap.prototype.mergeTopics = function(t1, t2) {
			var i, j;
			for (i = 0; i < t2.sis.length; i++) {
				t1.sis.push(t2.sis[i]);
				this.siIndex[t2.sis[i]] = t1;
			}
			for (i = 0; i < t2.sls.length; i++) {
				if (t1.sls.indexOf(t2.sls[i]) < 0) {
					t1.sls.push(t2.sls[i]);
				}
			}
			for (i = 0; i < t2.bns.length; i++) {
				if (t1.bns.indexOf(t2.bns[i]) < 0) {
					t1.bns.push(t2.bns[i]);
				}
			}
			for (i = 0; i < t2.os.length; i++) {
				t1.os.push(t2.os[i]);
			}
			for (i = 0; i < t2.as.length; i++) {
				t1.as.push(t2.as[i]);
			}
			for (i = 0; i < t2.vns.length; i++) {
				t1.vns.push(t2.vns[i]);
			}

			for (var si in this.siIndex) {
				var t = this.siIndex[si];
				for (i = 0; i < t.as.length; i++) {
					a = t.as[i];
					if (a.type == t2) {
						a.type = t1;
					}
					for (j = 0; j < a.players.length; j++) {
						p = a.players[j];
						if (p.role == t2) {
							p.role = t1;
						}
						if (p.player == t2) {
							p.player = t1;
						}
					}
				}
				for (i = 0; i < t.os.length; i++) {
					o = t.os[i];
					if (o.type == t2) {
						o.type = t1;
					}
					var ind = o.scope.indexOf(t2);
					if (ind >= 0) {
						o.scope.splice(ind, 1);
						o.scope.push(t1);
					}
				}
			}
		};

		TopicMap.prototype.getReference = function(ref) {
			if (ref.substring(0, 3) == "si:") {
				return this.getOrCreateTopic(ref.substring(3));
			} else {
				alert("unknown topic reference type " + si);
			}
		};

		/**
		 * Loads the JTM from src and calls callback when done.
		 * @param  {String}   src
		 * @param  {Function} callback
		 */
		TopicMap.prototype.loadJTM = function(src, callback) {

			this.status = 'loading';
			var cb = function(data, callback) {

				var i, j, k;
				var type, scope, st, value;
				for (k = 0; k < data.topics.length; k++) {
					var tdata = data.topics[k];
					if (!tdata.subject_identifiers) {
						alert("topic doesn't have a subject identifier");
						continue;
					}

					var t = this.getOrCreateTopic(tdata.subject_identifiers[0]);
					for (i = 1; i < tdata.subject_identifiers.length; i++) {
						t.addSi(tdata.subject_identifiers[i]);
					}

					if (tdata.subject_locators) {
						for (i = 0; i < tdata.subject_locators.length; i++) {
							t.sls.push(tdata.subject_locators[i]);
						}
					}

					if (tdata.names) {
						for (i = 0; i < tdata.names.length; i++) {
							t.bns.push(tdata.names[i].value);
							if (!tdata.names[i].variants) continue;
							var variants = tdata.names[i].variants;
							for (j = 0; j < variants.length; j++) {
								value = variants[j].value;
								scope = [];
								if (variants[j].scope) {
									for (var l = 0; l < variants[j].scope.length; l++) {
										st = this.getReference(variants[j].scope[l]);
										if (st) scope.push(st);
									}
								}
								t.vns.push(new Variant(scope, value));
							}
						}
					}

					if (tdata.occurrences) {
						for (i = 0; i < tdata.occurrences.length; i++) {
							var odata = tdata.occurrences[i];
							scope = [];
							if (!odata.type) {
								alert("occurrence doesn't have a type");
								continue;
							}
							if (odata.scope) {
								for (j = 0; j < odata.scope.length; j++) {
									st = this.getReference(odata.scope[j]);
									if (st) {
										scope.push(st);
									}
								}
							}
							type = this.getReference(odata.type);
							value = "";
							if (odata.value) {
								value = odata.value;
							}

							t.os.push(new Occurrence(t, type, scope, value));
						}
					}
				}


				for (k = 0; k < data.associations.length; k++) {
					var adata = data.associations[k];

					if (!adata.type) {
						alert("association doesn't have a type");
						continue;
					}
					if (!adata.roles || !adata.roles.length) {
						alert("association doesn't have roles");
						continue;
					}

					type = this.getReference(adata.type);
					if (!type) {
						continue;
					}

					var member;
					var players = [];
					for (i = 0; i < adata.roles.length; i++) {
						var role = this.getReference(adata.roles[i].type);
						member = this.getReference(adata.roles[i].player);
						if (!role || !member) {
							continue;
						}
						players.push(new Player(role, member));
					}
					if (!players.length) continue;
					var a = new Association(type, players);

					for (i = 0; i < players.length; i++) {
						member = players[i].member;
						if (member.as.indexOf(a) < 0) {
							member.as.push(a);
						}
					}

				}

				if (callback) callback();

				for (i = 0; i < this.afterInit.length; i++) {
					(this.afterInit[i])();
				}
				this.status = 'loaded';

			};

			var that = this;
			$http.get(src).success(function(data) {
				cb.apply(that, [data, callback]);
			});

		};

		/**
		 * The RenderedTopic class used in controllers
		 */
		var RenderedTopic = function() {};

		/*
		Exposed stuff
		 */

		/**
		 * The service object we will return;
		 * @type {Object}
		 */
		
		var topicmapService = {};

		/**
		 * Attach the topicmap to the service. It's not loaded yet.
		 * @type {TopicMap}
		 */
		topicmapService.tm = new TopicMap();

		/**
		 * An array of functions we call after the topicmap is initialized.
		 * @type {Array}
		 */
		topicmapService.tm.afterInit = [];

		/**
		 * Loads the config, binds it to the service and calls callback
		 * @param  {Function} callback
		 */
		topicmapService.loadConf = function(callback) {
			$http.get("config.json").success(function(data) {
				topicmapService.config = data;
				callback.apply(topicmapService);
			});
		};

		/**
		 * Initializes the TopicMap, initializes the config and calls callback
		 * if the TopicMap isn't initialized already. Otherwise just initializes
		 * the config and calls callback;
		 * @param  {Function} callback
		 */
		topicmapService.init = function(callback) {

			var cb = function() {

				var sis = this.config.sis;
				this.config.activeLang = this.config.defaultLang;
				this.config.langCodes = {};

				for (var code in this.config.langs) {
					var si = this.config.langs[code];
					this.config.langCodes[si] = code;
				}

				var typeInstanceT = this.tm.getOrCreateTopic(sis.typeInstance),
					typeT = this.tm.getOrCreateTopic(sis.type),
					instanceT = this.tm.getOrCreateTopic(sis.instance);

				typeInstanceT.bns.push("Type-Instance");
				typeT.bns.push("Type");
				instanceT.bns.push("Instance");

				this.tm.loadJTM(this.config.dataUrl, callback);

			};
			if (!this.config) this.loadConf(cb);
			else cb();

		};

		/**
		 * Renders a topic accounting for localization
		 * @param  {Topic} topic
		 * @return {RenderedTopic}
		 */
		topicmapService.renderTopic = function(topic) {
			if (topic.bns.length) {

				var t = new RenderedTopic();

				t.disp = topic.getDisplayName(this.config.activeLang);
				t.url = 'index.html#/topic/?si=' + encodeURIComponent(topic.sis[0]);
				t.si = topic.sis[0];
				t.icon = (topic.icon) ? topic.icon : 'icon-arrow-right';

				return t;

			}
			if (topic.sis.length) return topic.sis[0];
			return '<nada>';
		};

		/**
		 * Renders the associations for the given topic.
		 * @param  {Topic} topic
		 * @return {Object} AssocJSON
		 */
		topicmapService.renderAssocs = function(topic) {

			var assoc, player, role, type, rend,
				types = {},
				instances = {},
				classes = {},
				supers = {},
				subs = {};


			var superClass = this.tm.getTopic(this.config.sis.superr),
				subClass = this.tm.getTopic(this.config.sis.sub),
				superSub = this.tm.getTopic(this.config.sis.superSub),

				instanceClass = this.tm.getTopic(this.config.sis.instance),
				typeClass = this.tm.getTopic(this.config.sis.type),
				typeInstance = this.tm.getTopic(this.config.sis.typeInstance);

			for (var i = 0; i < topic.as.length; i++) {
				assoc = topic.as[i];
				if (assoc.type == superSub || assoc.type == typeInstance) {
					for (var j = 0; j < assoc.players.length; j++) {
						player = assoc.players[j];
						if (player.role == subClass && player.member != topic) {
							subs[player.member.sis[0]] = this.renderTopic(player.member);
						} else if (player.role == superClass && player.member != topic) {
							supers[player.member.sis[0]] = this.renderTopic(player.member);
						} else if (player.role == instanceClass && player.member != topic) {
							instances[player.member.sis[0]] = this.renderTopic(player.member);
						} else if (player.role == typeClass && player.member != topic) {
							classes[player.member.sis[0]] = this.renderTopic(player.member);
						}
					}
					continue;
				}

				var typeSI = assoc.type.sis[0];
				if (!types.hasOwnProperty(typeSI)) {
					types[typeSI] = {
						type: this.renderTopic(assoc.type),
						roles: [],
						roleSIs: [], //helps us keep track of things
						assocs: [],
						hidden: false,
						sort: 0,
						descending: false,
						filter: ""
					};
				}

				type = types[typeSI];

				var numAssocs = type.assocs.push([]);

				for (var k = 0; k < assoc.players.length; k++) {
					player = assoc.players[k];
					var roleSI = player.role.sis[0];
					var roleIndex = type.roleSIs.indexOf(roleSI);
					if (roleIndex < 0) {
						type.roleSIs.push(roleSI);
						type.roles.push(this.renderTopic(player.role));
						roleIndex = type.roleSIs.indexOf(roleSI);
					}
					type.assocs[numAssocs - 1].type = type;
					type.assocs[numAssocs - 1][roleIndex] = this.renderTopic(player.member);
				}
			}

			var supersArr = [],
				subsArr = [],
				classesArr = [],
				instancesArr = [];

			for (var key in supers) {
				supersArr.push(supers[key]);
			}
			supersArr.ord = 0;
			supersArr.filter = "";

			for (key in subs) {
				subsArr.push(subs[key]);
			}
			subsArr.ord = 0;
			subsArr.filter = "";

			for (key in instances) {
				instancesArr.push(instances[key]);
			}
			instancesArr.ord = 0;
			instancesArr.filter = "";

			for (key in classes) {
				classesArr.push(classes[key]);
			}
			classesArr.ord = 0;
			classesArr.filter = "";

			return {
				types: types,
				specs: {
					"superclasses": supersArr,
					"subclasses": subsArr,
					"instances": instancesArr,
					"classes": classesArr
				}
			};
		};

		/**
		 * Renders the occurrences for the given topic.
		 * @param  {Topic} topic
		 * @return {Array of rendered Occurrences}
		 */
		topicmapService.renderOccurrences = function(topic) {
			var occurrences = [],
				occurrence, type, rend, lang, inScope;

			lang = this.tm.getOrCreateTopic(this.config.activeLang);

			for (var i = 0; i < topic.os.length; i++) {
				inScope = false;

				occurrence = topic.os[i];

				for (var j = 0; j < occurrence.scope.length; j++) {
					if (lang == occurrence.scope[j]) inScope = true;
				}
				if (!inScope) continue;
				rend = {
					type: this.renderTopic(occurrence.type),
					value: occurrence.value
				};
				if (occurrences.indexOf(rend) < 0)
					occurrences.push(rend);
			}

			return occurrences;
		};

		/**
		 * Renders the frontpage using the default topic.
		 * @param  {Function} callback
		 */
		topicmapService.renderFront = function(callback) {

			var rootSI = this.config.sis.root;
			this.renderTopicDetails(rootSI, callback);
		};

		/**
		 * Renders the details for the topic specified by si. calls callback
		 * with the given details
		 * @param  {String}   si
		 * @param  {Function} callback
		 */
		topicmapService.renderTopicDetails = function(si, callback) {
			var cb = function() {

				var topic = this.tm.getTopic(si);

				callback({
					topic: this.renderTopic(topic),
					assocs: this.renderAssocs(topic),
					occurrences: this.renderOccurrences(topic)
				});
			};

			var that = this;

			if (this.tm.status == 'loaded') cb.apply(that);
			else this.tm.afterInit.push(function() {
				cb.apply(that);
			});
		};

		/**	
		 * Gets the root topic (Usually Wandora Class), and calls callback
		 * with the rendered topic
		 * @param  {Function} callback
		 */
		topicmapService.getRootTopic = function(callback) {

			var cb = function() {

				var topic = this.tm.getTopic(this.config.sis.root);

				callback(this.renderTopic(topic));
			};

			var that = this;

			if (this.tm.status == 'loaded') cb.apply(that);
			else this.tm.afterInit.push(function() {
				cb.apply(that);
			});
		};

		/**
		 * Gets the children of the topic specified by parentSI. Calls callback
		 * with the children.
		 * @param  {String}   parentSI
		 * @param  {Function} callback
		 */
		topicmapService.getTopicChildren = function(parentSI, callback) {
			var cb = function() {
				var topic = this.tm.getTopic(parentSI),
					t;

				var superClass = this.tm.getTopic(this.config.sis.superr),
					subClass = this.tm.getTopic(this.config.sis.sub),
					superSub = this.tm.getTopic(this.config.sis.superSub),

					instanceClass = this.tm.getTopic(this.config.sis.instance),
					typeClass = this.tm.getTopic(this.config.sis.type),
					typeInstance = this.tm.getTopic(this.config.sis.typeInstance),

					children = [],
					subs = [],
					ins = [],

					addedSubs = [],
					addedIns = [];

				for (var si in this.tm.siIndex) {
					t = this.tm.siIndex[si];
					var validSuper = false,
						validSub = false,
						validClass = false,
						validIns = false,
						i, j, role, player;
					for (i = 0; i < t.as.length; i++) {
						var type = t.as[i].type;
						if (type == superSub) {
							for (j = 0; j < t.as[i].players.length; j++) {
								role = t.as[i].players[j].role,
								player = t.as[i].players[j].member;
								if (role == superClass && player == topic) validSuper = true;
								if (role == subClass && player == t) validSub = true;
							}
							if (validSub && validSuper && addedSubs.indexOf(t) < 0) {
								t.icon = 'icon-th-large';
								subs.push(this.renderTopic(t));
								addedSubs.push(t);
							}

						} else if (type == typeInstance) {
							for (j = 0; j < t.as[i].players.length; j++) {
								role = t.as[i].players[j].role,
								player = t.as[i].players[j].member;
								if (role == typeClass && player == topic) validClass = true;
								if (role == instanceClass && player == t && t != topic) validIns = true;
							}
							if (validClass && validIns && addedIns.indexOf(t) < 0) {
								t.icon = 'icon-arrow-right';
								ins.push(this.renderTopic(t));
								addedIns.push(t);
							}
						}
					}
				}

				//children = subs.concat(ins);

				callback(subs, ins);
			};

			var that = this;

			if (this.tm.status == 'loaded') cb.apply(that);
			else this.tm.afterInit.append(function() {
				cb.apply(that);
			});
		};

		/**
		 * Gets an array of rendered language topics using the config. Calls
		 * callback with the rendered language topics.
		 * @param  {Function} callback
		 */
		topicmapService.getLangs = function(callback) {

			var cb = function() {
				var langSIs = this.config.langs;
				var rend = {};
				for (var key in langSIs) {
					var langTopic = this.tm.getTopic(langSIs[key]);
					if (langTopic)
						rend[key] = this.renderTopic(langTopic);
					if (langSIs[key] == this.config.activeLang)
						rend[key].isActive = true;
				}
				callback(rend);
			};

			var that = this;

			if (this.tm.status == 'loaded') cb.apply(that);
			else this.tm.afterInit.push(function() {
				cb.apply(that);
			});
		};

		/**
		 * Simply set the current lang in config to the si of topic
		 * @param  {Topic} topic
		 */
		topicmapService.setLang = function(topic) {
			this.config.activeLang = topic.si;
		};

		/**
		 * Translates the object o. If o is a rendered topic it calls
		 * getDisplayName on it's respective Topic. Else it treats o as a string
		 * and tries to translate the string using the translation found in the
		 * conf. If both fail it simply returns the given object without 
		 * translation.
		 * 
		 * @param  {Object} o
		 * @return {Object}
		 */
		topicmapService.translate = function(o) {

			var c = this.config;

			if (o instanceof RenderedTopic) {
				var si = o.si,
					t = this.tm.getOrCreateTopic(si);
				return t.getDisplayName(c.activeLang);
			}

			if (c && c.hasOwnProperty('langCodes')) {
				var code = c.langCodes[c.activeLang];
				for (var i = 0; i < c.translation.length; i++) {
					for (var trans in c.translation[i]) {
						var item = c.translation[i];
						if (item[trans] == o && item.hasOwnProperty(code))
							return item[code];
					}
				}
			}
			return o;
		};

		return topicmapService;

	};

	/**
	 * Register the service in wandora.services.
	 */
	angular.module('Wandora.services', [])
		.factory('topicmap', ['$http', topicmapFactory]);

})();