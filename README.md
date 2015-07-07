# tmc-core

This is a library for the core functionalities of a [TestMyCode](testmycode.github.io) 
client. The tmc-core uses [tmc-langs](https://github.com/testmycode/tmc-langs) for 
running tests locally, and supports fully the same languages as tmc-langs. Other features
may be used with even languages other than those supported by tmc-langs, provided the
[tmc-server](https://github.com/testmycode/tmc-server) has such courses.

The tmc-core is currently in use in the [TMC commandline client](https://github.com/rage/tmc-cli),
and will be integrated into [TMC-netbeans](https://github.com/testmycode/tmc-netbeans)

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

A course

## Exercise

One exercise. Also a compilable project

## Review

A code review

## SubmissionResult

The results of the serverside tests. Includes awarded points

## RunResult

The results of the local tests of this exercise. This is the same as the RunResult
user by tmc-langs

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
[University Of Helsinki CS Dept.](cs.helsinki.fi). The authors of TMC commandline Client were

 - Jani Luukko
 - Chang Rajani
 - Ilari Sinkkonen
 - Samu Tamminen
 - Pihla Toivanen
 - Kristian Wahlroos

The tmc-core was later separated and is maintained by the University of Helsinki CS Dept. [Agile Education Research](https://github/rage) group.


  [![Build Status](https://travis-ci.org/rage/tmc-core.svg?branch=master)](https://travis-ci.org/rage/tmc-core)
  
