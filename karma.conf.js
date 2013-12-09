module.exports = function(config) {
  config.set({
    basePath:'./',
    frameworks: ['mocha','requirejs'],
    files: [
      {pattern: 'test/**/*-spec.js',   included: false, watched: true,  served: true},
      {pattern: 'src/**/*.js',         included: false, watched: true,  served: true},
      {pattern: 'test/test-main.js',   included: true,  watched: true,  served: true}
    ],
	exclude: [
      'src/require.js'
    ],
    reporters:['dots'],
    browsers:['PhantomJS'],
    autoWatch: false,
	singleRun: true,
    colors: true,
    logLevel: config.LOG_INFO
  });
};