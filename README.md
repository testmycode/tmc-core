# tmc-core

[![Build Status](https://travis-ci.org/testmycode/tmc-core.svg?branch=master)](https://travis-ci.org/testmycode/tmc-core)
[![Build status](https://ci.appveyor.com/api/projects/status/o3l71ihukd7dwgks/branch/master?svg=true)](https://ci.appveyor.com/project/testmycode/tmc-core/branch/master)

[![Coverage Status](https://coveralls.io/repos/testmycode/tmc-core/badge.svg?branch=master&service=github)](https://coveralls.io/github/testmycode/tmc-core?branch=master)

This is a library for the core functionalities of a [TestMyCode](https://testmycode.github.io) 
client. The tmc-core uses [tmc-langs](https://github.com/testmycode/tmc-langs) for 
running tests locally, and supports fully the same languages as tmc-langs. Other features
may be used with even languages other than those supported by tmc-langs, provided the
[tmc-server](https://github.com/testmycode/tmc-server) has such courses.

# Features

 - verify credentials
 - list courses available on the specified server
 - get course
 - download all/some exercises of a given course
 - submit a project
 - run tests locally on a project
 - get unread code reviews
 - get new exercises and updated exercises
 - send feedback on an exercise
 - send snapshots to spyware server
 - paste code to the tmc pastebin

# Usage
 
 The client should use the tmc core through a TmcCore object only. The TmcCore
 class provides a method for each of the features. The client will also have to
 implement the TmcSettings interface which holds maintains important information
 such as the users credentials and the tmc server address. The update checking
 feature requires a file for permanent storage of exercise checksums. This file
 must be given in the constructor of TmcCore or set separately. In some courses
 some of the exercises will not be visible to the student at first, so it is to 
 be expected that new exercises appear. 

 All the methods of TmcCore will initiate a background task to and return a 
 ListenableFuture containing the tasks result. Many of the returned results use
 classes defined in this library, and it is recommended the creator of a client 
 familiarises themselves with these classes. The most important aspects of these
 classes is discussed below.

## Course

A course using TMC.
Some important fields:

 - id (unique within a tmc-server)
 - name
 - exercises (a list of exercises that can be seen by the student)

## Exercise

An exercise in a course. In current TMC implementations an exercise is a whole project,
not just a few lines of code.
Some important fields:

 - id (unique in one course)
 - name
 - deadline (currently a String in the format yyyy-MM-dd'T'hh:mm:ssX)

## Review

A code review written by an instructor, for code submitted by the student.
Some important fields:

 - exerciseName
 - reviewerName
 - reviewBody (the content of the review)

## SubmissionResult

The results of the serverside tests. Include points awarded to the student. If all 
serverside tests pass, the result will also have a list of feedbackquestions, if any
have been set. 
Some important fields:

 - allTestsPassed
 - userId
 - course (the course name)
 - exerciseName
 - points
 - feedbackQuestions

## RunResult

The results of the local tests of this exercise. This is the same as the RunResult
user by tmc-langs.
Some important fields:

 - status (passed, tests failed, compile failed or generic error)
 - testResults (list of TestResult objects. These will contain error messages)

# Examples

### Creating an using tmc-core

```java

TmcCore tmcCore = new TmcCore();
ListenableFuture<List<Course>> resultFuture = tmcCore.listCourses(mySettings);
resultFuture.addListener(courseListListener);

```

### Tmc-core with a cache file

```java

File cacheFile = getCacheFile();
// the file can be given in the constructor
TmcCore core = new TmcCore(cacheFile);

// or set later
core.setCacheFile(cacheFile);

```
When a TmcCore object has a cache file set it will automatically use this file with 
downloading and getting new updates. Download will write the checksums of the 
exercises, so that updates may be detected.

```java

ListenableFuture<List<Exercise>> downloadedStuffFuture;
downloadedStuffFuture = core.downloadExercises(projectRoot, courseId, settings);
downloadedStuffFuture.addListener(projectOpeningListener);

ListenableFuture<List<Exercise>> newStuffFuture;
newStuffFuture = core.getNewAndUpdatedExercises(currentCourse, settings);
newStuffFuture.addListener(updateListener);

```


# Credits

The tmc-core was initially created as part of the [TMC commandline Client](https://github.com/rage/tmc-cli) project, which was a Software Engineering lab project at the
[University Of Helsinki CS Dept.](https://cs.helsinki.fi). The authors of TMC commandline Client were

 - Jani Luukko
 - Chang Rajani
 - Ilari Sinkkonen
 - Samu Tamminen
 - Pihla Toivanen
 - Kristian Wahlroos
The course instructors were Jarmo Isotalo ([jamox](https://github.com/jamox)) and Leo Leppänen 

The tmc-core was later separated and is maintained by the University of Helsinki CS 
Dept. [Agile Education Research](https://github.com/rage) group.

