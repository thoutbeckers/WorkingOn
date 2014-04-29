[![Build Status](https://travis-ci.org/thoutbeckers/WorkingOn.svg?branch=master)](https://travis-ci.org/thoutbeckers/WorkingOn)

WorkingOn
=========

WorkingOn allows you to easily override certain parts of your Guice models, based on setting a Task.

When you provide Guice modules they will be automatically overridden or replaced with modules associated with that task.

It also hold some static field values that by convention could be used to make your app behave
differently, such as the Activity or Fragment that you want to develop on.

It provides a utility method for initializing a class (and silently failing if it doesn't exist).
This can be used to initialize the static fields from a class that can be outside of SCM (for example by putting it in .gitignore). 
This way different people on the project can be working with different configurations without getting in each others way.
