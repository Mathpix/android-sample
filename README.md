a) Install Android Studio: https://developer.android.com/studio/index.html

![Alt text](screenshots_for_readme/01.png?raw=true)

b) Download the “android-sample” SDK for Mathpix: https://github.com/Mathpix/android-sample

![Alt text](screenshots_for_readme/02.png?raw=true)

c) Open the “android-sample-master” folder in Android Studio.

![Alt text](screenshots_for_readme/03.png?raw=true)

*)To run the sample app, you must put the correct app_id and app_key.

In Constant.java, there are two variables for them.

   *******************************
   public static String app_id="mathpix";   
   public static String app_key="139ee4b61be2e4abcfb1238d9eb99902";
   *******************************
Please change the values to the correct values.

Notice: we had to disable the open API key due to API abuse, please request one from support@mathpix.com for now.

d) Run “build.gradle”

![Alt text](screenshots_for_readme/04.png?raw=true)

e) Make sure your: “Platform”, “Build Tools”, and Gradle Wrapper are all up-to-date. You can click on the blue link and Android Studios should initiate an install automatically. Click* “Install _____”

-Platform: 
 ![Alt text](screenshots_for_readme/051.png?raw=true)
 
-Build Tools: 
 ![Alt text](screenshots_for_readme/052.png?raw=true)
 
-Gradle Wrapper: 
 ![Alt text](screenshots_for_readme/053.png?raw=true)
 
f) Select a “Deployment Target” to run on. You should see the model name/number for your device. If you cannot find your device refer to Exhibit B below. 

![Alt text](screenshots_for_readme/06.png?raw=true)
  
Exhibit B. Make sure that the ADB option is turned on. If you cannot find your device this make sure that it is in “developer mode”. Some phone’s developer options can be activated on the phone itself. 
http://www.howtogeek.com/129728/how-to-access-the-developer-options-menu-and-enable-usb-debugging-on-android-4.2/ 

![Alt text](screenshots_for_readme/061.png?raw=true)

g) Enable “ADB Integration” on your phone. You can do this through Tools > Android > Enable ADB integration. In some cases you will get a message on your phone screen to confirm.

![Alt text](screenshots_for_readme/07.png?raw=true)
 
Exhibit A: Sample confirmation message on phone. (you may or may not see one depending on the device)

![Alt text](screenshots_for_readme/071.png?raw=true)

