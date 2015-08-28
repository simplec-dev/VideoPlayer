/*-
 * cordova VideoPlayer Plugin for Android
 *
 * Created by Simon MacDonald (2008) MIT Licensed
 * Revised for Cordova 3.3+ by Dawson Loudon (2013) MIT Licensed
 *
 * Usages:
 *
 * VideoPlayer.play("http://path.to.my/video.mp4");
 * VideoPlayer.play("file:///path/to/my/video.mp4");
 * VideoPlayer.play("file:///android_asset/www/path/to/my/video.mp4");
 * VideoPlayer.play("https://www.youtube.com/watch?v=en_sVVjWFKk");
 */

var exec = require("cordova/exec");

var VideoPlayer = {		
	    play: function(url, success, failure) {
	    	if (url) {
		        exec(success, failure, "VideoPlayer", "play", [url]);
	    	} else {
		        exec(success, failure, "VideoPlayer", "play", []);
	    	}
	    },
	    
	    resume: function(url, success, failure) {
		    exec(success, failure, "VideoPlayer", "play", []);
	    },

	    stop: function(success, failure) {
	        exec(success, failure, "VideoPlayer", "stop", []);
	    },
	    
	    pause: function(success, failure) {
	        exec(null, null, "VideoPlayer", "pause", []);
	    },
	    
	    getDuration: function(success, failure) {
	        exec(success, failure, "VideoPlayer", "duration", []);
	    },
	    
	    seek: function(seekTo, success, failure) {
	        exec(success, failure, "VideoPlayer", "seek", [seekTo]);
	    },
	    
	    isPlaying: function(success, failure) {
	        exec(success, failure, "VideoPlayer", "playing", []);
	    }
};

module.exports = VideoPlayer;

