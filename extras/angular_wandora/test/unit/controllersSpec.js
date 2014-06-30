"use strict";

var scope, langCtrl, $httpBackend;

beforeEach(module('Wandora.controllers'));
beforeEach(inject(function(_$httpBackend_, $rootScope, $controller) {

	$httpBackend = _$httpBackend_;
	$httpBackend.expectGET('config.json').
	respond(mockConfig);

	$httpBackend.expectGET('ArtOfNoise.jtm').
	respond(mockJTM);

	scope = $rootScope.$new();
	mainCtrl = $controller('mainControl',{
		$scope: scope
	});
}));

describe('Wandora controllers', function() {

	describe('mainCtrl', function() {

		it('should get config', function() {

			expect(scope.message).toBe("Loading...");

			$httpBackend.flush();
			expect(scope.message).toBe("");
		});

	});

	describe('langCtrl', function() {

		beforeEach(inject(function(_$httpBackend_, $rootScope, $controller) {
			langCtrl = $controller('langControl', {
				$scope: scope
			});
		}));

		it('should get langs', function() {

			expect(scope.langs).toBeUndefined();

			$httpBackend.flush();
			expect(scope.langs).toBeDefined();
		});

	});

	describe('treeCtrl', function(){

		beforeEach(inject(function(_$httpBackend_, $rootScope, $controller) {
			treeCtrl = $controller('treeControl', {
				$scope: scope
			});
		}));

		it('should get a root topic', function(){

			expect(scope.tre).toBeUndefined();

			$httpBackend.flush();
			expect(scope.tree).toBeDefined();

		});

	});

	describe('topicDetailCtrl', function(){

		beforeEach(inject(function(_$httpBackend_, $rootScope, $controller) {
			$httpBackend.flush();
			topicDetailCtrl = $controller('topicDetail', {
				$scope: scope
			});
		}));

		it('should get a topic and assorted data', function(){

			expect(scope.topic).toBeDefined();
			expect(scope.assocs).toBeDefined();
			expect(scope.occurrences).toBeDefined();

		});

	});

});