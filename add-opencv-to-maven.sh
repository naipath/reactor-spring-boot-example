#!/bin/bash
mvn install:install-file \
   -Dfile=/usr/local/Cellar/opencv3/3.2.0/share/OpenCV/java/opencv-320.jar \
   -DgroupId=org.opencv \
   -DartifactId=opencv \
   -Dversion=3.2.0 \
   -Dpackaging=jar \
   -DgeneratePom=true

