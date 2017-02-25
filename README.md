# Fussroll
Fussroll lets you quickly share your daily activities with your friends and family.

## Introduction
Project is mainly the integration of Android and Node.js + MongoDB both acting as front-end and back-end respectively. Fussroll contains list of activities, feelings and many more, from which user has to select anyone which is relevant to the context, for e.g, let's say user is feeling angry now, then he or she scrolls the list of feelings (or search) and tap on the list item which says "angry" with a cool emoji icon associated with it and then a pop-up shows the meta information to it that is, Feeling and Felt, since user is feeling angry right now, he or she will choose Feeling and will update the status. Which then will be shared with his or her friends. 

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

## Screenshots
#### Home
![HomeActivity of Fussroll](https://res.cloudinary.com/dmvkaukjs/image/upload/v1488012079/1_wapbge.png)
#### Categories
![Categories in Fussroll](https://res.cloudinary.com/dmvkaukjs/image/upload/v1488012079/2_dbybtf.png)
#### Timeline
![Timeline in Fussroll](https://res.cloudinary.com/dmvkaukjs/image/upload/v1488012079/3_he5ase.png)

### Setup on your own
- Get yourself registered with [Firebase](https://firebase.google.com/) and [MSG91](https://msg91.com/) and get the ServerKey from Firebase and AuthKey from MSG91
- Add your ServerKey in **serverKey** variable present in REST_APIs/library/sendCloudMessages.js and AuthKey in **authKey** variable present in REST_APIs/library/sendOTP.js
- Create database named **fussroll** using MongoDB and create following blank collections: **users**, **contacts**, **logs**, **cloudMessages**, **blockedBy**
- Host the back-end on VPS or any managed hosting and finalize your end-point (for e.g, api.example.com)
- Edit the API end-points section present in strings.xml file of Android application and change it to yours

### Current Development
This project is no longer maintained.

### Bugs to patch
- Lags too much while swiping ViewPager, which is present in HomeActivity, mostly due to the processing done by each Fragment associated with the ViewPager.
- Samsung S5 incompatibility - ANR after verification step.
