# Static Analysis Gradle Plugin

1. [Overview](#overview)
1. [Usage](#usage)
1. [Plugin Details](#plugin-details)
    1. [PMD](#pmd)
    1. [FindBugs](#findbugs)
    1. [Android Lint](#android-lint)
1. [Configuration](#configuration)
1. [License](#license)

### Overview
The Static Analysis Gradle Plugin adds gradle tasks for running PMD and FindBugs, and includes a default configuration for these along with Android Lint that is common to most Intrepid Android projects.

### Usage
Add the plugin to your project:
```
buildscript {
    repositories {
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "gradle.plugin.io.intrepid:static-analysis:1.0.1"
    }
}

apply plugin: "io.intrepid.static-analysis"
```

Or alternatively, if you are using Gradle 2.1+, add the following to the top of your app `build.gradle`:
```
plugins {
    id "io.intrepid.static-analysis" version "1.0.1"
}
```

To run pmd, use the command `./gradlew pmd`.

To run FindBugs, use the command `./gradlew findBugs${buildVariant}`. By default, this produces a html result. You can change it to produce a xml report instead by adding `findBugsXml` flag (FindBugs can only produce one type of report at a time).
ex: `./gradlew findBugsDebug -PfindBugsXml`

Android Lint can be run using the standard `./gradlew lint${buildVariant}`

To run all three tasks in a single command, use `./gradlew analyze${buildVariant}`

Note for multi-module projects: You must run lint only on the TOP-MOST module in order to avoid duplicate/false lint warnings. Running lint on the top-most module will run lint on all of the lower modules as well. You must then run pmd and findBugs separately on each lower module in order to get complete coverage. So for example, if you have an app that depends on libraryOne and libraryTwo, to get complete coverage you must run something similar to `./gradlew app:analyzeDebug libraryOne:pmd libraryOne:findBugsDebug libraryTwo:pmd libraryTwo:findBugsDebug`

### Plugin Details
Here's a detailed list of changes/additions that Static Analysis Gradle Plugin made to the associated plugins:

#### PMD
* Sets the default source files to those that are typical in Android projects
* Changes the default `ignoreFailures` to true
* Sets a default `ruleSetFile`
* Enables xml and html reporting

#### FindBugs
* Creates Gradle task for each of build variants and ensures that these tasks are run after the assembleVariant tasks
* Sets the default source files and classes to those that are typical in Android projects
* Changes the default `effort` to max
* Changes the default `ignoreFailures` to true
* Sets a default `excludeFilter`
* Enables xml and html reporting

#### Android Lint
* Changes the default `abortOnError` flag to false
* Sets a default `lintConfig` file

### Configuration
The following configurations can be set in the app `build.gradle` to override the default behaviors:

```
staticAnalysis {
    pmdVersion              // default:  "5.5.1"
    findbugsVersion         // default:  "3.0.1"

    source                  // default:  'src'
    include                 // default:  '**/*.java'
    exclude                 // default:  '**/gen/**'

    pmdRuleSetFile

    findBugsEffort          // default:  "max"
    findBugsReportLevel     // default:  "medium"
    findBugsClasses         // default:  files("${project.buildDir}/intermediates/classes")
    findBugsExcludeFilterFile

    lintAbortOnError        // default:  false
    lintConfigFile
}
```
Refer to the gradle doc for [PmdExtension](https://docs.gradle.org/current/dsl/org.gradle.api.plugins.quality.PmdExtension.html) and [FindBugsExtension](https://docs.gradle.org/current/dsl/org.gradle.api.plugins.quality.FindBugsExtension.html) for an explaination of these fields.

The default `pmdRuleSetFile`, `findBugsExcludeFilterFile`, and `lintConfig` files can be found [here](src/main/resources).

### License
```
Copyright 2017 Intrepid Pursuits LLC.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
