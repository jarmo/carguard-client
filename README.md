Car Guard Client
===============

CarGuard is an Android app for guarding your car by using GPS positioning.

Check out server at https://github.com/jarmo/carguard-server


Features
---------------

* Transparent: client and server code is open-source - there's nothing secret going on
* Private: only encrypted location data is sent to the server - just you can decrypt it
* Smart: only needed location data is sent to the server - e.g. no need to send when you are driving
* Persistent: app runs even when it is exited or when phone is restarted


Requirements
---------------

* A car;
* An Android phone with Android version >= 4.0.3 (SDK API level 15)
* A SIM card for the phone with a cellular data access
* Car charger for the phone


Installation
---------------

* Install [Apache ANT](http://ant.apache.org/)
* Install [Android SDK](http://developer.android.com/sdk)
* Clone this project `git clone https://github.com/jarmo/carguard-client.git`
* Build apk `ant debug`
* Plug an Android phone into your PC
* Install apk on the phone `adb -d install bin\car_guard-debug.apk`
* Start application in the phone
* Store `API key` and `Secret` for later use - without them you cannot view your car location!
* Pair that Android phone over Bluetooth with your main phone - this is needed for detecting if you are driving
* Install phone into your car by providing continuous power
* Head to http://carguard.me to see your car location


Tips & Tricks
---------------

* Set your phone as silent
* Turn of WiFi
* Remove all unneeded software to reduce network traffic - maybe even install modified Android like CyanogenMod.
* Make sure that network data is allowed when roaming - you wan't to track your car if it's taken abroad as well ;)
