#!/bin/bash
mvn install:install-file \
   -Dfile=/usr/local/Cellar/opencv3/HEAD-7dd3723_4/share/OpenCV/java/opencv-320.jar \
   -DgroupId=org.opencv \
   -DartifactId=opencv \
   -Dversion=3.20 \
   -Dpackaging=jar \
   -DgeneratePom=true

