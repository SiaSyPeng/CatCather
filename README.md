# CS65 CAT CATCHER

Team project for Dartmouth CS65 17F <br>
Built using Android Studio

## Authors
Naman Goyal 20'<br>
Sia Peng 20'

***Please read Lab4 section below for our extracredit and notes***

## Submission

for lab1, please see the final code on branch lab1Submit

for lab2, please see the final code on branch lab2Submit

for lab3, please see the final code on branch lab3Submit 

for lab4, please see the final code on branch lab4Submit  [***Please read Lab4 section below for our extracredit and notes***]


## Late passes

We are using one of our 24-hour late passes for Lab 3

## Extra Credit

### Lab2

- Login functioning
- Update Profile functioning: User preference will be saved to server

### Lab3
- have "hard" mode working
- pre-selecting the cat closest to your location, and display information
	      	about that cat when the user opens the Map activity. This is also
					triggered when the selected cat goes off screen— we auto-select the
					new closest cat to the user. If there is no closest cat, then the
					panel will be set back to default.
- "Creating and interacting with GoogleMaps objects"
    - design our own markers
    - have a configurable setting in the
        Settings fragment that controls how frequently (the minimum
        time interval) you receive location updates. i.e. When you request
        updates from locationManager, use the min time from
        the settings fragment.

### Lab4
- We submitted a few minutes after Monday midnight, which is the extension we get from Sergey. We are also using the two extensions that we have not used before.
- Some minor design changes we made. For notification, we have two modes, either vibrate or none. As we are using notification channel, Android 26 Oreo uses channels to not be intrusive to users. Thus, to change some of the channels configs, you have to go to settings in your phone. i.e. setting importance
- For back stack, it is not fully functioniong at first for us. We talked to Sergey about it, he says that it is an AOSP source bug, so we will not get points off that. Upon his advice, we had a walkthrough by overriding the onBackClick, which also worked to preserve our back navigation.
- 

## Usage

Run this totally not pokemon go game on your android phone!

## Cool things we did / explanations

**ListenableCatID**:
I wanted a way to listen to changes in an integer variable, so I created a class
called ListenableCatID that has a single function interface, a listener. This
allows changes to the cat ID to call a function every time, which, in our case,
we use to update the panel and redraw markers.

**Kotlin Data Classes**:
Kotlin has this structure called data classes which are like classes but that
auto-generate toString, getters and setters. This makes them pretty much perfect
for, say, changing JSON objects into programmable objects

## Directory Structure

**Lab1**: Sign up page

**Lab2**: Server authentication, tab, and preference fragment

**Lab3**: Google Map and cat interactions with real-time location. Adding more
preferences into preference fragment.

**Lab3**: Google Map and cat interaction on real-time location

...

## Assumptions
Lab Requirement:

**Lab1**: http://www.cs.dartmouth.edu/~sergey/cs65/lab1/

**Lab2**: http://www.cs.dartmouth.edu/~sergey/cs65/lab2/

**Lab2**: http://www.cs.dartmouth.edu/~sergey/cs65/lab3/
