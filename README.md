# CS65 CAT CATCHER

Team project for Dartmouth CS65 17F <br>
Built using Android Studio

## Authors
Naman Goyal 20'<br>
Sia Peng 20'

## Submission

for lab1, please see the final code on branch lab1Submit

for lab2, please see the final code on branch lab2Submit

for lab3, please see the final code on branch lab3Submit

for lab4, please see the final code on branch Lab4Submit or master

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
 - We issue proximity alerts for all cats

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

**Serializing our cat class**:
I made our cat class serializable by implementing the Externalizable interface,
so that we could easily and efficiently save our list of cats to internal
storage, such that it only needs to be gotten by the server once. The game can
then at the least display cats and  whatnot without an internet connection.

**Workaround for BackStack**:
For some reason, backstacks simply weren't working for us. We asked the
professor for help but he too did not understand why it was not working. So,
we instead overrode the back button in the map activity to always send back
to our main activity instead of to whatever is in the backStack.

**Autoselection of tracked cat**:
If the user enters the mapActivity from the notification, whether or not
mapActivity has been closed or not, the cat that was being tracked is
automatically selected

## Directory Structure

**Lab1**: Sign up page

**Lab2**: Server authentication, tab, and preference fragment

**Lab3**: Google Map and cat interactions with real-time location. Adding more
preferences into preference fragment.

**Lab3**: Google Map and cat interaction on real-time location

**Lab4**: Notifications, Camera overlay, Tracking

...

## Assumptions
Lab Requirement:

**Lab1**: http://www.cs.dartmouth.edu/~sergey/cs65/lab1/

**Lab2**: http://www.cs.dartmouth.edu/~sergey/cs65/lab2/

**Lab3**: http://www.cs.dartmouth.edu/~sergey/cs65/lab3/

**Lab4**: http://www.cs.dartmouth.edu/~sergey/cs65/lab4/

## Contact us
If you have any questions about any of our code or how we implemented stuff
or the "cool things" we did, please feel free to slack us in the CS channel at
any point!
