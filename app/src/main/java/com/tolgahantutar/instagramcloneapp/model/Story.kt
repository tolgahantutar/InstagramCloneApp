package com.tolgahantutar.instagramcloneapp.model

class Story(imageurl: String, timestart: Long, timeend: Long, storyid: String, userid: String) {
     var imageurl = imageurl
     var timestart = timestart
     var timeend = timeend
     var storyid = storyid
     var userid = userid

     constructor(): this("",0L,0L,"","")g
}