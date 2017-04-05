package io.intrepid.analysis

class StaticAnalysisExtension {

    String pmdVersion = "5.5.1"
    String findbugsVersion = "3.0.1"

    String source = 'src'
    String include = '**/*.java'
    String exclude = '**/gen/**'

    String pmdRuleSetFile

    String findBugsEffort = "max"
    String findBugsReportLevel = "medium"
    String findBugsExcludeFilter
    String findBugsClasses
}
