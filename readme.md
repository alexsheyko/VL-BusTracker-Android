VL Bus Tracker
===============
this fork for Vladivostok (Russia) Bus http://map.vl.ru

BUS Vl Автобусы владивостока
Клиента для сайта map.vl.ru


Install
-------
1. Install and open [Android Studio](https://developer.android.com/sdk/installing/studio.html).
2. Open the SDK Manager (Tools > Android > SDK Manager) from Android Studio.
3. Install API level 20, Android Support Repository, Android Support Library, Google Analytics App 
Tracking SDK, Google Play Services, and Google Repository, then close the SDK Manager.
4. Fork this repository on GitHub by clicking the Fork button at the top right.
5. Clone your fork inside Android Studio (VCS > Checkout from Version Control > Log in to GitHub > 
Select your fork > Click clone).
6. Select use default gradle wrapper and click OK.
7. If needed, open the project view by hovering over the icon at the bottom left of Android Studio.
8. Set your API keys in the 
[API keys](../master/NYUBusTracker/src/main/res/values/api-keys.xml) file. You will need:
  * A [Google Developer](https://console.developers.google.com) project consuming the Maps API,
  following the directions 
  [here](https://developers.google.com/maps/documentation/android/start#get_an_android_certificate_and_the_google_maps_api_key).
  * Optionally a [Google Analytics](http://www.google.com/analytics/) application with production
  and debug properties.
10. Run the app (green arrow at the top of Android Studio)!


Release
-------
Here is the release process, for when you're ready to push a new version to the Play Store.

1. Make sure `MainActivity.LOCAL_LOGV = false`. Run the app and make sure there is no logging.
3. Make sure your API keys are correct.
4. Bump the release version in build.gradle.
5. Tag a release on GitHub.
6. ./gradlew assembleRelease
7. Run the app as a last minute check, to make sure everything is in working order.
8. Upload.

Please see the LICENSE file for license information.