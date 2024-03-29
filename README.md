# Fussroll
Fussroll lets you quickly share your daily activities with your friends and family.

## Introduction
Project is mainly an integration of Android and Node.js (with MongoDB) both acting as front-end and back-end respectively. Fussroll contains list of activities, feelings and many more, all associated with cool emojis, from which user selects one according to the situation or context and shares the selected "status snippet" with his (or her) friends.

## Features
- 6 categories of activities to select from
    - Feeling
    - Activity
    - Place
    - Food
    - Drink
    - Game
- Timeline - Check all latest updates of anyone from your contact list
- Get notified about new updates from your friends
- Block suspicious contacts
- Maintain privacy
- Minimal registration - Requires only the phone number of user
- Supports 4 countries in terms of verification and handling of phone numbers : India, United States, United Kingdom and Canada

## Screenshots
#### Home
![HomeActivity of Fussroll](https://res.cloudinary.com/dmvkaukjs/image/upload/v1488012079/1_wapbge.png)
#### Categories
![Categories in Fussroll](https://res.cloudinary.com/dmvkaukjs/image/upload/v1488012079/2_dbybtf.png)
#### Timeline
![Timeline in Fussroll](https://res.cloudinary.com/dmvkaukjs/image/upload/v1488012079/3_he5ase.png)

## Setup
- Connect the Android application with Firebase from Android Studio using Firebase Assistant or manually
- Get yourself registered with [MSG91](https://msg91.com/) and get your Auth key. Also, get your Server key from Firebase console present under Cloud Messaging tab
- Add your Server key in **serverKey** variable present in REST_APIs/library/sendCloudMessages.js and Auth key in **authKey** variable present in REST_APIs/library/sendOTP.js
- Create database named **fussroll** using MongoDB and create following blank collections: **users**, **contacts**, **logs**, **cloudMessages**, **blockedBy**
- Host the back-end on VPS or any managed hosting and finalize your end-point (for e.g, api.example.com)
- Edit the API end-points section present in strings.xml file of Android application and change it to yours

## Current Development
This project is no longer maintained.

## Bugs
- Lags too much while swiping ViewPager, which is present in HomeActivity, mostly due to the processing done by each Fragment associated with the ViewPager.
- Samsung S5 incompatibility - ANR after verification step.

## Possible solution to resolve lag bug
- In Yours and People Fragments, get only the cursor from DatabaseHandler (instead of all rows in List\<Logs> object) and send it to respective Adapters or try to implement a SimpleCursorAdapter mechanism for RecyclerView.
- Also, implement Loader to get all contacts from the device in Contacts Fragment. 
