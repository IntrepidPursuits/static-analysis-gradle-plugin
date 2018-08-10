Change Log
==========
Version 1.2.0 *(2018-8-10)*
----------------------------
*  Added: `updateLintFile` gradle task to update the project's `lint.xml` to match the library's version. Projects should now specify custom lint rules via `lintOptions` gradle block instead of modifying `lint.xml` so that they don't get overrriden.

Version 1.1.1 *(2018-6-15)*
----------------------------
* Changed: Set `GradleDependency` lint rule severity to informational

Version 1.1.0 *(2018-3-23)*
----------------------------
* Changed: Gradle Build Tools has been updated to 3.0.1 and Gradle has been updated to 4.3
* Changed: `abortOnError` lint option is now defaulted to true
* Added: `lint-baseline.xml` is now generated if none exists
* Added: `warningsAsErrors` and `checkDependencies` lint options are set to true as default

Version 1.0.3 *(2017-10-20)*
----------------------------
* Fixed: Corrected the `lint.xml` path being generated

Version 1.0.2 *(2017-10-20)*
----------------------------
* Changed: Updated `lint.xml` for current Intrepid lint checks.
* Added: Auto-creation of `lint.xml` if file does not exist in project.

Version 1.0.1 *(2017-5-25)*
----------------------------
* Added: Lint config to suppress illegal package errors from okio/retrofit.

Version 1.0.0 *(2017-04-19)*
----------------------------
* Initial release.
