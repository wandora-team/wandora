basePath = '../';

files = [
  JASMINE,
  JASMINE_ADAPTER,

  'app/lib/angular.js',
  'app/lib/angular-*.js',
  'test/lib/angular/angular-mocks.js',
  'app/js/*.js',
  'app/js/**/*.js',
  'test/unit/*.js'
];

autoWatch = true;

browsers = ['Chrome'];

junitReporter = {
  outputFile: 'test_out/unit.xml',
  suite: 'unit'
};
