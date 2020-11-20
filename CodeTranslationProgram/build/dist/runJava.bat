@echo off
set CLASSPATH=\%JAVA_HOME%\bin\;./bin;

cd %~dp0/resources/runs

javac Main.java

java Main

pause

exit