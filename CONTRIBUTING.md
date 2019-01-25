# How to contribute

Awesome that you would like to contribute! This is a small document to help you getting
started with your contribution. 

SBT Native Packager provides packaging support for a variety of operating systems,
packaging types, systemloader and service types. Sometimes this means a pull request
may take a bit longer, because testing is sometimes hard and tedious. 

Most of the changes are of a very small nature, but can have a huge impact, so try
to cover the three topis _Code_, _Documentation_ and _Test_ :)

## Code

We have a lot of different languages and DSLs:

### Scala

Make sure to run `sbt scalafmtAll` before opening a pull request. This will format
the sourcecode according to our standards

### Bash

Provide the bash version you tested your code with. 

### Bat (Windows)

Provide the environment you tested your bat file; windows version, cmd, powershell.

## Documentation

If you add or change the behaviour of a task or setting provide a small documentation
for it.  The documentation can be found in `src/sphinx`.

Build the documentation with

```
~sphinx:generateHtml
```

For more details see the [Developer Guide](https://github.com/sbt/sbt-native-packager/wiki/Developer-Guide#documentation)

## Test

If you fix a bug or introduce a new feature, provide a test for it. You can run the
tests for

### Linux

```
$ sbt
>  scripted universal/* debian/* rpm/*
```

### Windows

```
$ sbt
>  scripted windows/*
```

### Docker

```
$ sbt
>  scripted docker/*
```

