module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),


        targethtml: {
            dist: {
                files: {
                    'build/index.html': 'app/index.html'
                }
            }
        },
        copy: {
            main: {
                files: [{
                    expand: true,
                    cwd: 'app/',
                    src: [
                        'config.json',
                        '*.jtm',
                        'partials/*',
                        'img/*',
                        'font/*',
                        'css/styles.css',
                        'css/*.min.css',
                        'css/ajax_loader_gray_48.gif'
                    ],
                    dest: 'build/',
                    filter: 'isFile'
                }]
            }
        },
        concat: {
            build: {
                src: [
                    'app/lib/angular.js',
                    'app/lib/*',
                    'app/js/services/topicmap.js',
                    'app/js/controllers.js',
                    'app/js/directives.js',
                    'app/js/app.js'
                ],
                dest: 'build/js/angular-wandora.js'
            }
        },
        uglify: {
            options: {
                banner: '/*! <%= pkg.name %> <%= grunt.template.today("yyyy-mm-dd") %> */\n'
            },
            build: {
                src: 'build/js/angular-wandora.js',
                dest: 'build/js/angular-wandora.min.js'
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-targethtml');


    // Default task(s).

    grunt.registerTask('dist', ['targethtml', 'copy', 'concat', 'uglify']);

};