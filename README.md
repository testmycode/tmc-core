# tmc-core

This is a library for the core functionalities of a [TestMyCode](testmycode.md) client.

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

  First create  a TmcCore object. Calling a method of a TmcCore object will
  start a background task and return a ListenableFuture of the result.

# Examples


  [![Build Status](https://travis-ci.org/rage/tmc-core.svg?branch=master)](https://travis-ci.org/rage/tmc-core)
  
