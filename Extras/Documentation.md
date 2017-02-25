# Fussroll - Documentation

This document contains explanation of some of the unusual coding patterns used in Java classes of Android application.

### List of files documented

- MainActivity.java
- RegisterActivity.java
- VerifyActivity.java
- HomeActivity.java

##### MainActivity.java
1. **Note :** It is recommened that you should go through **RegisterActivity.java** and **VerifyActivity.java** first and then read this one.
2. We are checking what is done yet, so if we are completed with registration step then we are skipping the registration step and move towards verification step. If we are completed with both registration and verification then we skip them and start the HomeActivity which is the actual user's home page. Intent contains the UID and mobile number for dealing with REST APIs.

##### RegisterActivity.java
1. After successfully registering, we save UID in encrypted format and mobile number in SharedPreferences and a checkpoint that registration is done by adding registered = true so when user visits the app for the next time we skip this step by checking if registered = true in MainActivity.java. **Note :** We are using REST APIs for like every thing, so for each request we need two important parameters for authentication (and authorization) and those are mobile and UID (in decrypted format).

##### VerifyActivity.java
1. We are taking 6-digits verification code in two EditText, each one contains 3-digits and to make UX a bit better we added listener here, in which when user is done entering 3-digits in first box, we automatically focus on the other box.
2. This is to count 60 seconds after user enters a verification code for the first time, using which we disable the **Resend** button. 
3. We are using HandlerThread here to keep the up the count down - it ticks every 1 second. The condition for 9,8,7,6,5,4,3,2,1 is because while decrementing after 10, it will result in 00:9,00:8... which looks unpleasant and so the conditions to make it look better. **ISSUE 1 :** The thread keeps running after we move on to the next activity (HomeActivity.class), and it only terminates when user removes the application from the Task Manager. It does not start when the user visit the app for the next time because after clearing the verification step VerificationActivity is skipped everytime, but still, we need to work on it. **ISSUE 2 :** When user waits for 60 seconds count down in this step, one thing is possible here to do by the user, to go back to the registration process and dive back in here, which results in sending another verification code without waiting for 60 seconds, which will result in 2 threads running... count will increase if done for more number of times.
4. We get new UID here and just like in Registration process, we save UID in encrypted format in SharedPreferences. 
5. Here, we are done with verification and so we have added checkpoint of confirmed = true in SharedPreferences to skip this step on next visits and a default setting of Notification preferences (Notification setting from SettingsActivity.java) is set here, in fact, all default things should be set here.

##### HomeActivity.java
1. Checking if Firebase token for that particular user is refreshed. If it is, then we are sending to our server for updating the user's information
2. This is the setup code required for setting up [Material Sheet FAB](https://github.com/gowong/material-sheet-fab) which pops up when fab (FloatingActionButton) is pressed.
3. It's a list of updates we are getting from other users and it is maintained in CloudMessages.java for sending out notifications, so since the user is active on the app (or just logged in) we are clearing that list as user will check the updates in People section and after that, if there are some notifications in notification bar regarding the updates, we are clearing out all of them. 
4. We have cleared all CloudMessages list before leaving the activity, since after that every updates from other users will be added in the list and if the user is out of the app then we will be notifying it about new updates (about notifying logic, it is in CloudMessages.java).

##### ContactsFragment.java
1. onCreatePerm will be true when the permission is granted when asked for the first time from onCreate() Therefore, checking here (onResume()) will not result in pop up for asking permissions.Why to check here then? : It might happen that from onCreate we got the permission and after sometime user revokes it, then onResume() check comes to the rescue.
2. Removing all characters which are added to a phone number while saving like (,),-,[SPACE]
