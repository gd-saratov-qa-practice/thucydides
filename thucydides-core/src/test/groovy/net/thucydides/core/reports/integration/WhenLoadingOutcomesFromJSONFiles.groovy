package net.thucydides.core.reports.integration

import com.github.goldin.spock.extensions.tempdir.TempDir
import com.google.common.base.Optional
import net.thucydides.core.annotations.Issue
import net.thucydides.core.annotations.Story
import net.thucydides.core.model.DataTable
import net.thucydides.core.model.TestOutcome
import net.thucydides.core.model.TestResult
import net.thucydides.core.model.TestStep
import net.thucydides.core.reports.json.JSONTestOutcomeReporter
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import spock.lang.Specification

class WhenLoadingOutcomesFromJSONFiles extends Specification {

	def JSONTestOutcomeReporter outcomeReporter

    private static final DateTime FIRST_OF_JANUARY = new LocalDateTime(2013, 1, 1, 0, 0, 0, 0).toDateTime()

	@TempDir File outputDirectory

    class AUserStory {
    }

    @Story(AUserStory.class)
    class SomeTestScenario {
        public void a_simple_test_case() {
        }

        public void should_do_this() {
        }

        public void should_do_that() {
        }
    }

	public void setup() throws IOException {
		outcomeReporter = new JSONTestOutcomeReporter();
		outcomeReporter.setOutputDirectory(outputDirectory);
	}

    def "should load acceptance test report from json file"(){

        given:
            def report = new File(outputDirectory,"saved-report.json");
            report << """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "$FIRST_OF_JANUARY",
              "issues": [],
              "tags": [],
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenStoringTestOutcomesAsJSON"
			  },
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1374810594394,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
    		"""
        when:
            TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
        then:
            testOutcome.result == TestResult.SUCCESS
		and:
            testOutcome.title == "Should do this"
            testOutcome.methodName == "should_do_this"
            testOutcome.startTime == FIRST_OF_JANUARY
    }

    def "should load acceptance tests without steps"(){

        given:
        def report = new File(outputDirectory,"saved-report.json");
        report << """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "PENDING",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "$FIRST_OF_JANUARY",
              "issues": [],
              "tags": [],
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenStoringTestOutcomesAsJSON"
			  },
			  "test-steps": []
			}
    		"""
        when:
            TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
        then:
            testOutcome.result == TestResult.PENDING
    }



    def "should load manual acceptance test report from json file"() {
        given:
            def report = new File(outputDirectory,"saved-report.json")
            report << """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "manual": "true",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1374811596702,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
			"""		                                     		
        when:
		    TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
		then:
			testOutcome.isManual()
        and:
            testOutcome.duration == 0
    }

    def "should load a qualified acceptance test report from json file"() throws Exception {

        given:
            File report = new File(outputDirectory,"saved-report.json")
            report << """
			{
			  "title": "Should do this [a qualifier]",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			  },
			  "result": "SUCCESS",
			  "qualifier": "a qualifier",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373543479702,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
			"""        		        
        when:
            TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
		then:
        	testOutcome.qualifier.get() == "a qualifier"
        and:
			testOutcome.title.contains "[a qualifier]"
    } 

    // TODO - handle new lines in titles properly
    def "should unescape newline in the title and qualifier of a qualified acceptance test report from json file"() throws Exception {
        given:
            File report = new File(outputDirectory,"saved-report.json")
            report << """
			{
			  "title": "Should do this [a qualifier with \u0026#10; a new line]",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "qualifier": "a qualifier with \u0026#10; a new line",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373544008557,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
			"""        		
        when:
            Optional<TestOutcome> testOutcome = outcomeReporter.loadReportFrom(report);
		then:
        	testOutcome.get().qualifier.get().equals("a qualifier with \n a new line");
			testOutcome.get().title.indexOf("[a qualifier with \n a new line]") > -1;
    }
    
    def "should load tags from json file"() {
        File report = new File(outputDirectory,"saved-report.json")
        report << """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.SomeTestScenario",
			    "storyName": "Some test scenario with tags",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "Some test scenario with tags",
			      "type": "story"
			    },
			    {
			      "name": "simple story",
			      "type": "story"
			    },
			    {
			      "name": "important feature",
			      "type": "feature"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373544217353,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
			"""        		
        when:
            TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
		then :
        	testOutcome.tags.collect { it.name } == ["simple story", "Some test scenario with tags", "important feature"]
    }
    
    def "should load example data from json file"() {
        File report = new File(outputDirectory,"saved-report.json")
        report << """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373543300323,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ],
			  "examples": {
			    "headers": [
			      "firstName",
			      "lastName",
			      "age"
			    ],
			    "rows": [
			      {
			        "cellValues": [
			          "Joe",
			          "Smith",
			          "20"
			        ],
			        "result": "FAILURE"
			      },
			      {
			        "cellValues": [
			          "Jack",
			          "Jones",
			          "21"
			        ],
			        "result": "SUCCESS"
			      }
			    ],
			    "predefinedRows": true,
			    "currentRow": {
			      "value": 0
			    }
			  }
			}
			"""
        when:
            Optional<TestOutcome> testOutcome = outcomeReporter.loadReportFrom(report);
            DataTable table = testOutcome.get().dataTable;
		
		then:
        	table.headers == ["firstName","lastName","age"]
			table.rows.size() == 2
			table.rows.get(0).stringValues == ["Joe","Smith","20"]
			table.rows.get(0).result == TestResult.FAILURE
			table.rows.get(1).stringValues == ["Jack","Jones","21"]
			table.rows.get(1).result == TestResult.SUCCESS
    }
    
    def "should load acceptance test report including issues"()  {
        File report = new File(outputDirectory,"saved-report.json");
        report << """
		{
		  "title": "Should do this",
		  "name": "should_do_this",
		  "test-case": {
		    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario",
		    "issues": [
		      "#123",
		      "#456",
		      "#789"
		    ]
		  },
		  "result": "SUCCESS",
		  "steps": "1",
		  "successful": "1",
		  "failures": "0",
		  "skipped": "0",
		  "ignored": "0",
		  "pending": "0",
		  "duration": "0",
		  "timestamp": "2013-01-01T00:00:00.000+01:00",
		  "user-story": {
		    "userStoryClass": {
		      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
		    },
		    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory",
		    "storyName": "A user story",
		    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
		  },
		  "issues": [
		    "#456",
		    "#789",
		    "#123"
		  ],
		  "tags": [
		    {
		      "name": "A user story",
		      "type": "story"
		    }
		  ],
		  "test-steps": [
		    {
		      "description": "step 1",
		      "duration": 0,
		      "startTime": 1373542631993,
		      "screenshots": [],
		      "result": "SUCCESS",
		      "children": []
		    }
		  ]
		}
		"""
        when:
            TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
		then:
        	testOutcome.title == "Should do this"
        and:
            testOutcome.issues == ["#456","#789","#123"] as Set
    }
    
    def "should load feature details from json file"() {
        given:
            File report = new File(outputDirectory,"saved-report.json");
            report << """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory",
			    "storyName": "A user story in a feature",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AFeature",
			    "qualifiedFeatureClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AFeature",
			    "featureName": "A feature"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A feature",
			      "type": "feature"
			    },
			    {
			      "name": "A user story in a feature",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373572931641,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
			"""
        when:
            TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get()
        then:
        	testOutcome.feature.id == "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AFeature"
			testOutcome.feature.name == "A feature"
    }
    
    def "should load the session id from xml file"() {
        given:
            File report = new File(outputDirectory,"saved-report.json");
            report << """
			{
			  "title": "Should do this",
			  "name": "should_do_this",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "session-id": "1234",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373571216867,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
			"""
        when:
            TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
        then:
            testOutcome.sessionId == "1234"
    }
    
    def "should return null feature if no feature is present"() {
        expect:
            new TestOutcome("aTestMethod").feature == null
    }

    def "should load acceptance tests with nested groups"() {
        given:
            File report = new File(outputDirectory,"saved-report.json");
            report << """
			{
			  "title": "A nested test case",
			  "name": "a_nested_test_case",
			  "test-case": {
			    "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$SomeTestScenario"
			  },
			  "result": "SUCCESS",
			  "steps": "5",
			  "successful": "5",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {
			    "userStoryClass": {
			      "classname": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles\$AUserStory"
			    },
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "Group 1",
			      "duration": 0,
			      "startTime": 1373625031524,
			      "screenshots": [],
			      "children": [
			        {
			          "description": "step 1",
			          "duration": 0,
			          "startTime": 1373625031524,
			          "screenshots": [],
			          "result": "SUCCESS",
			          "children": []
			        },
			        {
			          "description": "step 2",
			          "duration": 0,
			          "startTime": 1373625031524,
			          "screenshots": [],
			          "result": "SUCCESS",
			          "children": []
			        },
			        {
			          "description": "step 3",
			          "duration": 0,
			          "startTime": 1373625031524,
			          "screenshots": [],
			          "result": "SUCCESS",
			          "children": []
			        },
			        {
			          "description": "Group 1.1",
			          "duration": 0,
			          "startTime": 1373625031524,
			          "screenshots": [],
			          "children": [
			            {
			              "description": "step 4",
			              "duration": 0,
			              "startTime": 1373625031530,
			              "screenshots": [],
			              "result": "SUCCESS",
			              "children": []
			            },
			            {
			              "description": "step 5",
			              "duration": 0,
			              "startTime": 1373625031530,
			              "screenshots": [],
			              "result": "SUCCESS",
			              "children": []
			            }
			          ]
			        }
			      ]
			    }
			  ]
			}
			"""
        when:
            def testOutcome = outcomeReporter.loadReportFrom(report).get();

		then :
        	testOutcome.title == "A nested test case"
			testOutcome.testSteps.size() == 1
        and:
            TestStep group1 = testOutcome.testSteps[0];
            group1.children.size() == 4
			group1.children[3].children().size() == 2
        
    }

    def "should load acceptance test report with simple nested groups from xml file"() {
        given:
            File report = new File(outputDirectory,"saved-report.json");
            report << """
			{
			  "title": "A nested test case",
			  "name": "a_nested_test_case",			  
			  "result": "SUCCESS",
			  "steps": "3",
			  "successful": "3",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {			    
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "Group 1",
			      "duration": 0,
			      "startTime": 1373624139680,
			      "screenshots": [],
			      "children": [
			        {
			          "description": "step 1",
			          "duration": 0,
			          "startTime": 1373624139681,
			          "screenshots": [],
			          "result": "SUCCESS",
			          "children": []
			        },
			        {
			          "description": "step 2",
			          "duration": 0,
			          "startTime": 1373624139681,
			          "screenshots": [],
			          "result": "SUCCESS",
			          "children": []
			        },
			        {
			          "description": "step 3",
			          "duration": 0,
			          "startTime": 1373624139681,
			          "screenshots": [],
			          "result": "SUCCESS",
			          "children": []
			        }
			      ]
			    }
			  ]
			}
		""" 
        when:
            def TestOutcome testOutcome = outcomeReporter.loadReportFrom(report).get();
            TestStep group1 = testOutcome.testSteps[0];
		then:
        	testOutcome.title.equals("A nested test case")
			testOutcome.testSteps.size() == 1
		    group1.getChildren().size() == 3
    }

   def "should load acceptance test report with multiple test steps from xml file"() {

       given:
           File report = new File(outputDirectory,"saved-report.json");
           report << """
            {
              "title": "A simple test case",
              "name": "a_simple_test_case",
              "result": "FAILURE",
              "steps": "9",
              "successful": "2",
              "failures": "2",
              "errors": "1",
              "skipped": "1",
              "ignored": "2",
              "pending": "1",
              "duration": "0",
              "timestamp": "2013-01-01T00:00:00.000+01:00",
              "user-story": {
                "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
                "storyName": "A user story",
                "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
              },
              "issues": [],
              "tags": [
                {
                  "name": "A user story",
                  "type": "story"
                }
              ],
              "test-steps": [
                {
                  "description": "step 1",
                  "duration": 0,
                  "startTime": 1373601456591,
                  "screenshots": [],
                  "result": "SUCCESS",
                  "children": []
                },
                {
                  "description": "step 2",
                  "duration": 0,
                  "startTime": 1373601456592,
                  "screenshots": [],
                  "result": "IGNORED",
                  "children": []
                }
              ]
            }
            """
        when:
            def testOutcome = outcomeReporter.loadReportFrom(report).get();
		then :
        	testOutcome.title == "A simple test case"
			testOutcome.testSteps.size() == 2
			testOutcome.testSteps[0].result == TestResult.SUCCESS
			testOutcome.testSteps[0].description == "step 1"
			testOutcome.testSteps[1].result == TestResult.IGNORED
			testOutcome.testSteps[1].description == "step 2"
    }

    def "should load qualified acceptance test report with a qualified name"()  {
        given:
            File report = new File(outputDirectory,"saved-report.json");
            report << """
			{
			  "title": "A simple test case [a_b]",
			  "name": "a_simple_test_case",
			  "result": "SUCCESS",
			  "qualifier": "a_b",
			  "steps": "1",
			  "successful": "1",
			  "failures": "0",
			  "skipped": "0",
			  "ignored": "0",
			  "pending": "0",
			  "duration": "0",
			  "timestamp": "2013-01-01T00:00:00.000+01:00",
			  "user-story": {			    
			    "qualifiedStoryClassName": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles.AUserStory",
			    "storyName": "A user story",
			    "path": "net.thucydides.core.reports.integration.WhenLoadingOutcomesFromJSONFiles"
			  },
			  "issues": [],
			  "tags": [
			    {
			      "name": "A user story",
			      "type": "story"
			    }
			  ],
			  "test-steps": [
			    {
			      "description": "step 1",
			      "duration": 0,
			      "startTime": 1373601008887,
			      "screenshots": [],
			      "result": "SUCCESS",
			      "children": []
			    }
			  ]
			}
			"""
        when:
            def testOutcome = outcomeReporter.loadReportFrom(report).get();
		then :
        	testOutcome.title == "A simple test case [a_b]"
        	testOutcome.titleWithLinks == "A simple test case [a_b]"
    }

}
