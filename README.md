# Reddit - Profile Saver - Java App Edition
This is a JavaFx app that I made to help me keep a list of posts I upvote on Reddit.com and allow me to filter and view them.
The reason for this is because Reddit only allows you to view the most recent 1000 posts you have upvoted and doesn't allow for any searching system. This allows you to store as many posts as you want in a Json file that can be parsed and viewed by the app.

I ended up dropping this version of the project and switching over to a [C# Asp.Net Core version](https://github.com/kenny2892/RedditProfileSaver-C-Sharp). This was for various reasons, but the main 2 were mobility (I can view a webpage on my phone, but can only use this on the computer) and JavaFx being very slow whenever more then a couple dozen high-ish quality images were loaded in. This meant that,  viewing posts in a horizontal style using thumbnails was fine, but trying to properly display the posts wasn't feasible (at least not as far as I know).

There are 2 parts to the app.
1. The Python Retrieval Script
2. The Java App to view the results

To run the Python script that gets your upvotes, you must register a bot with the Reddit Api and get a refresh token for your account. Then you can enter the appropriate information into the Config.json within the Data folder. This will allow the Python script to run and store the 1000 most recent upvoted posts for your account.

To run the Java app, download the project and set up the class paths for JavaFx 11 along with the Run Configuration. Then you can view the posts that are saved by the Python script (there is also a test file within the Data folder).

<p align="center"><b>Standard View</b></p>
<p align="center">
  This is what the app looks like when it loads a file.
  <img src="https://i.gyazo.com/ce84c1539007b87f6a9a521e18cbe2ea.png" width="600">
</p>

<p align="center"><b>Webview for images</b></p>
<p align="center">
  This is the web browser that is loaded whenever you click on a link.
  <img src="https://i.gyazo.com/9160f0c21a13529b384d4db9efe4b29f.png" width="600">
</p>

<p align="center"><b>Prototype Vertical Mode</b></p>
<p align="center">This is the Vertical View that I was developing before dropping the project. It shined a light a lot of JavaFx' limitations such as slowing down dramaticaly when loading to many images and not handling gifs very well.</p>

<p align="center">
  <img src="https://i.gyazo.com/c3f484e89bb4caa843707d3271c9e92b.png" width="600">
</p>
