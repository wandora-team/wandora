(function() {
	'use strict';

	/**
	 * The main controller bound to the body element. Starts loading the 
	 * configuration and the topicmap file specified in the config.
	 * @param   $scope			the scope
	 * @param   $routeParams	from angular
	 * @param   tmService 		from angular
	 */
	var mainCtrl = function($scope, $routeParams, tmService){
		$scope.message = "Loading...";
		tmService.init(function(){
			$scope.message = "";
		});
	};

	/**
	 * Controller for the language bar. Responsible for getting the render
	 * for the langs and updating the scope once done.
	 * @param   $scope
	 * @param   tmService
	 */
	var langCtrl = function($scope, tmService){

		tmService.getLangs(function(langs){
			$scope.langs = langs;
		});

		$scope.setLang = function(topic){
			tmService.setLang(topic);
				tmService.getLangs(function(langs){
				$scope.langs = langs;
			});
		};

	};

	/**
	 * The controller responsible for the topic details rendered in the main view.
	 * It puts the required data and a few simple helper functions to the scope.
	 * @param   $scope			The scope
	 * @param   $filter			from angular
	 * @param   $routeParams	from angular
	 * @param   tmService		the topicmapservice
	 */
	var  topicDetailCtrl = function($scope, $filter, $routeParams, tmService){
		var si = $routeParams.si,
			loaded = false;
		var cb = function(data){

			loaded = true;

			$scope.topic = data.topic;
			$scope.assocs = data.assocs;
			$scope.occurrences = data.occurrences;

		};

		if(si) tmService.renderTopicDetails(si,cb);
		else tmService.renderFront(cb);

		$scope.isLoaded = function(){
			return loaded;
		};

		$scope.isEmpty = function(obj){

			var discard = ['ord','$$hashKey'];
			for(var prop in obj) {
				if( obj.hasOwnProperty(prop) && discard.indexOf(prop) < 0 )
					return false;
			}
			return true;
		};

		$scope.specEmpty = function(spec){
			return (spec.hidden || !$filter("filter")(spec, spec.filter).length);
		};

		$scope.hide = function(type){
			type.hidden = true;
		};

		$scope.show = function(type){
			type.hidden = false;
		};

		$scope.toggle = function(obj){
			obj.hidden = !obj.hidden;
		};

		$scope.setSort = function(type,role,order){
			type.sort = type.roles.indexOf(role);
			type.descending = order;
		};

		$scope.sorter = function(assoc){
			var i = assoc.type.sort;
			return assoc[i].disp;

		};

		$scope.setOrder = function(obj,ord){
			obj.ord = ord;
		};

		$scope.t = function(str){
			return tmService.translate(str);
		};

	};

	/**
	 * The controller responsible for the topic map tree sidebar. First binds the
	 * root topic (specced in config) to scope and handles adding subsequent
	 * topics as children via $scope.add(data).
	 * @param   $scope		the scope
	 * @param   tmService 	the topicmapservice
	 */
	var treeCtrl = function($scope,tmService){

		$scope.add = function(data) {
			data.subs = [];
			data.ins = [];
			tmService.getTopicChildren(data.t.si,function(subs,ins){
				for (var i = 0; i < subs.length; i++) {
					console.log(subs[i].disp);
					data.subs.push({
						t: subs[i],
						icon: subs[i].icon,
						subs:[],
						ins:[]
					});
				}
				for (i = 0; i < ins.length; i++) {
					data.ins.push({
						t: ins[i],
						icon: ins[i].icon,
						subs:[],
						ins:[]
					});
				}
			});
		};

		$scope.t = function(str){
			return tmService.translate(str);
		};

		$scope.message = "Loading...";

		tmService.getRootTopic(function(topic){
			$scope.message = "";
			$scope.tree = [{
				t: topic,
				icon: "icon-th-large",
				subs: [],
				ins: []
			}];
		});

	};

	/**
	 * Bind the controllers declared above to the submodule wandora.controllers.
	 */

	angular.module('Wandora.controllers',['Wandora.services'])
		.controller('mainControl', ['$scope','$routeParams','topicmap',mainCtrl])
		.controller('langControl', ['$scope','topicmap',langCtrl])
		.controller('topicDetail',['$scope','$filter','$routeParams','topicmap',topicDetailCtrl])
		.controller('treeControl',['$scope','topicmap',treeCtrl]);
})();