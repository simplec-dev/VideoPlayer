VideoPlayer
===========

Video Player Plugin for Cordova 3.3+ Android

Updated version of http://github.com/macdonst/VideoPlayer by Simon MacDonald

Installation
===========

For Cordova CLI -
`cordova plugin add https://github.com/dawsonloudon/VideoPlayer.git`

For PhoneGap Build -
Add `<gap:plugin name="com.dawsonloudon.videoplayer" version="1.0.0" />` to config.xml

Usages
===========

- VideoPlayer.play("file:///mnt/internal_sd/Android/data/com.simplec.therapyplayer.prd/files/offline-content/content/personal_media/internal_simplec_springs_manor/douglas_nelson/images/the_beach!/a1061e2a-ed34-4e17-87e9-5668f5220074.mp4");
- VideoPlayer.play("file:///path/to/my/video.mp4");
- VideoPlayer.play("file:///android_asset/www/path/to/my/video.mp4");
- VideoPlayer.play("https://www.youtube.com/watch?v=en_sVVjWFKk");
