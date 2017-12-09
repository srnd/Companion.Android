# CodeDay Companion for Android

This repo contains the source for the official [CodeDay Companion app](https://play.google.com/store/apps/details?id=org.srnd.companion), licensed under GPLv3.

It is written in Kotlin.

<img src="http://bangla.babyonline.pl/foto/prod//0008/d/596_01_2098.jpg" width="200"/>

## Building

1. In `app/src/main/res/values`, copy `api_keys.xml.example` to `api_keys.xml` in the same directory.
2. Replace the keys there with the appropriate values. They are all required as of right now; we will make them optional later.
3. Gradle sync.
4. You should be good to go!