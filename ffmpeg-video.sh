#!/bin/bash
ffmpeg -f image2 -framerate 12 -i foo-%03d.jpeg -s WxH foo.avi


#For example, for creating a video from filenames matching the glob pattern "foo-*.jpeg":

ffmpeg -f image2 -pattern_type glob -framerate 12 -i 'foo-*.jpeg' -s WxH foo.avi

ffmpeg -f avfoundation -r 12  hhtp://localhost:8090/feed1.ffm < example.jpg

ffmpeg -f avfoundation -video_size 640x480 -pix_fmt uyvy422 -r 30 -i 0 -y -f lavfi -i anullsrc -map 0:a? -map 1:v?    http://127.0.0.1:8090/feed1.ffm

ffmpeg -f avfoundation -video_size 640x480 -pix_fmt uyvy422 -r 30 -i 0 -f lavfi -i anullsrc http://127.0.0.1:8090/feed1.ffm


ffmpeg -r 30 -f avfoundation -video_size 640x480 -pix_fmt yuyv422 -i 0 -f lavfi -i anullsrc -b:a 64k -b:v 64k -bufsize 64k http://127.0.0.1:8090/feed1.ffm

ffmpeg -r 30 -video_size 640x480 -pix_fmt yuyv422 -f avfoundation -i 0 -f lavfi -i anullsrc -r 15 http://127.0.0.1:8090/feed1.ffm