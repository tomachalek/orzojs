(function (module) {
    'use strict';

    module.exports = function (grunt) {

        grunt.loadNpmTasks('assemble-less');
        grunt.loadNpmTasks('grunt-contrib-uglify');
        grunt.loadNpmTasks('grunt-contrib-copy');
        grunt.loadNpmTasks('grunt-contrib-clean');
        grunt.loadNpmTasks('grunt-babel');
        grunt.loadNpmTasks('grunt-browserify');

        grunt.initConfig({
            clean: {
                all: {
                    src: ["dist/**/*", "compiled/**/*"]
                }
            },
            copy: {
                images: {
                    files : [{
                        expand: true,
                        cwd: 'dev/img',
                        src: ['**/*.*'],
                        dest: 'dist/img'
                    }]
                }
            },
            less : {
                all: {
                    files: {
                        "dist/css/style.css": [
                            "dev/css/style.less"
                        ]
                    },
                    options: {
                        compress: true
                    }
                }
            },
            browserify: {
                vendor: {
                    dest: "dist/js/vendor.js",
                    src: ["dev/js/vendor/**/*.js"],
                    options: {
                        "require": ['react', 'jquery'],
                        "alias": {
                            "./vendor/Dispatcher": "./dev/js/vendor/Dispatcher.js"
                        }
                    }
                },
                orzo: {
                    dest: "dist/js/orzo-view.js",
                    src: ["compiled/js/**/*.js"],
                    options: {
                        external: ['jquery', 'react', './vendor/Dispatcher'],
                   }
                }
            },
            babel: {
                all: {
                    files: [
                        {
                            expand: true,
                            cwd: 'dev/js',
                            src: [
                                "**/*.js",
                                "**/*.jsx",
                                "!vendor/**/*.js"
                                ],
                            dest: "compiled/js",
                            ext: ".js"
                        }
                    ]
                }
            },
        });

        grunt.registerTask('production', ['clean:all', 'less:all',
                'browserify:vendor', 'babel:all', 'browserify:orzo', 'copy:images']);
        grunt.registerTask('devel', ['clean:all', 'less:all', 'browserify:vendor',
                'babel:all', 'browserify:orzo', 'copy:images']);
    };

}(module));