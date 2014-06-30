
/**
 * The bootstrap process for the wandora app. details in 
 * https://github.com/angular/angular-seed
 */
(function(){
	'use strict';

	angular.module('Wandora', [
		'Wandora.services',
		'Wandora.controllers']
	).config([
		'$routeProvider',
		'$locationProvider',
		function($routeProvider,$locationProvider) {
			$routeProvider.
				when('/', {
					templateUrl: 'partials/topicDetail.html',
					controller: 'topicDetail'
				})
				.when('/topic/',{
					templateUrl: 'partials/topicDetail.html',
					controller: 'topicDetail'
				})
				.otherwise({redirectTo: '/'});
			}
		]
	);
})();
