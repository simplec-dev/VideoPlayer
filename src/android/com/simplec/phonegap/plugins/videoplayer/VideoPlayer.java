/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 */

package com.simplec.phonegap.plugins.videoplayer;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.VideoView;

public class VideoPlayer extends CordovaPlugin implements OnCompletionListener, OnPreparedListener, OnErrorListener {

    protected static final String LOG_TAG = "VideoPlayer";

    protected static final String ASSETS = "/android_asset/";
    protected static final String FILE = "file://";

    private Dialog dialog;
    private VideoView videoView;
    private MediaPlayer player;
    private CallbackContext callbackContext;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArray of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        Log.v(LOG_TAG, "got command: "+action);
        if (action.equals("play")) {
            Log.v(LOG_TAG, "playing");
            CordovaResourceApi resourceApi = webView.getResourceApi();
            String target = args.getString(0);
            final JSONObject options = new JSONObject();
            
            try {
            	args.getJSONObject(1);
            } catch (Exception e) {
            	// gobble.  no options sent
            }

            String fileUriStr;
            try {
                Uri targetUri = resourceApi.remapUri(Uri.parse(target));
                fileUriStr = targetUri.toString();
            } catch (IllegalArgumentException e) {
                fileUriStr = target;
            }

            Log.v(LOG_TAG, fileUriStr);
            
            File f = new File(fileUriStr);
            if (!f.exists()) {
            	callbackContext.error("video does not exist");
            	return true;
            }

            final String path = stripFileProtocol(fileUriStr);

            Log.v(LOG_TAG, "playing path: "+path);
            // Create dialog in new thread
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    openVideoDialog(path, options, callbackContext);
                }
            });

            return true;
        }
        if (action.equals("stop")) {
            Log.v(LOG_TAG, "stopping");
        	stop(callbackContext);
        	return true;
        }
        return false;
    }

    /**
     * Removes the "file://" prefix from the given URI string, if applicable.
     * If the given URI string doesn't have a "file://" prefix, it is returned unchanged.
     *
     * @param uriString the URI string to operate on
     * @return a path without the "file://" prefix
     */
    public static String stripFileProtocol(String uriString) {
        if (uriString.startsWith("file://")) {
            return Uri.parse(uriString).getPath();
        }
        return uriString;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void openVideoDialog(String path, JSONObject options, final CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
		
        // Let's create the main dialog
        dialog = new Dialog(cordova.getActivity(), android.R.style.Theme_NoTitleBar);
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        // Main container layout
        LinearLayout main = new LinearLayout(cordova.getActivity());
        main.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        main.setOrientation(LinearLayout.VERTICAL);
        main.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        main.setVerticalGravity(Gravity.CENTER_VERTICAL);

        videoView = new VideoView(cordova.getActivity());
        videoView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // videoView.setVideoURI(uri);
        // videoView.setVideoPath(path);
        main.addView(videoView);

        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

        if (path.startsWith(ASSETS)) {
            String f = path.substring(ASSETS.length());
            AssetFileDescriptor fd = null;
            try {
                fd = cordova.getActivity().getAssets().openFd(f);
                player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            } catch (Exception e) {
                callbackContext.error(e.getLocalizedMessage());
                Log.v(LOG_TAG, "error: "+e.getLocalizedMessage());
            }
        } else {
            try {
                player.setDataSource(path);
            } catch (Exception e) {
                callbackContext.error(e.getLocalizedMessage());
                Log.v(LOG_TAG, "error: "+e.getLocalizedMessage());
            }
        }

        try {
            float volume = Float.valueOf(options.getString("volume"));
            player.setVolume(volume, volume);
        } catch (Exception e) {
            callbackContext.error(e.getLocalizedMessage());
            Log.v(LOG_TAG, "error: "+e.getLocalizedMessage());
        }

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            try {
                int scalingMode = options.getInt("scalingMode");
                switch (scalingMode) {
                    case MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:
                        player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        break;
                    default:
                        player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                }
            } catch (Exception e) {
                callbackContext.error(e.getLocalizedMessage());
            }
        }

        final SurfaceHolder mHolder = videoView.getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            	if (player!=null) {
	                player.setDisplay(holder);
	                try {
	                    player.prepare();
	                } catch (Exception e) {
	                    callbackContext.error(e.getLocalizedMessage());
	                }
            	}
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            	if (player!=null) {
                    player.release();
                    player = null;
            	}
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.setContentView(main);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

	    @Override
	    public boolean onError(MediaPlayer mp, int what, int extra) {
	        Log.e(LOG_TAG, "AudioPlayer.onError(" + what + ", " + extra + ")");

	        JSONObject event = new JSONObject();
	        try {
				event.put("type", "error");
		        event.put("what", what);
		        event.put("extra", extra);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
            PluginResult errorResult = new PluginResult(PluginResult.Status.OK, event.toString());
            errorResult.setKeepCallback(false);
            callbackContext.sendPluginResult(errorResult);

	        mp.stop();
	        mp.release();
	        dialog.dismiss();
	        
	        dialog = null;
	        player = null;
	        videoView = null;
	        callbackContext = null;
            
	        return false;
	    }
	
	    @Override
	    public void onPrepared(MediaPlayer mp) {
	        JSONObject event = new JSONObject();
	        try {
				event.put("type", "prepared");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
            PluginResult errorResult = new PluginResult(PluginResult.Status.OK, event.toString());
            errorResult.setKeepCallback(true);
            callbackContext.sendPluginResult(errorResult);
            
	        mp.start();
	    }
	
	    @Override
	    public void onCompletion(MediaPlayer mp) {
	        JSONObject event = new JSONObject();
	        try {
				event.put("type", "completed");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
            PluginResult errorResult = new PluginResult(PluginResult.Status.OK, event.toString());
            errorResult.setKeepCallback(false);
            callbackContext.sendPluginResult(errorResult);

	        mp.stop();
	        mp.release();
	        dialog.dismiss();
	        
	        dialog = null;
	        player = null;
	        videoView = null;
	        callbackContext = null;
	    }
	
	    public void stop(CallbackContext stopContext) {
	    	if (player!=null) {
		        JSONObject event = new JSONObject();
		        try {
					event.put("type", "completed");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
	            PluginResult errorResult = new PluginResult(PluginResult.Status.OK, event.toString());
	            errorResult.setKeepCallback(false);
	            callbackContext.sendPluginResult(errorResult);

            	if (player!=null) {
    		        player.stop();
    		        player.release();
                    player = null;
            	}
            	if (dialog!=null) {
			        dialog.dismiss();
			        dialog=null;
            	}
            	
		        videoView = null;
		        callbackContext = null;

		        stopContext.success();
	    	} else {
	    		stopContext.error("player does not exist");
	    	}
	        
	    }
}
