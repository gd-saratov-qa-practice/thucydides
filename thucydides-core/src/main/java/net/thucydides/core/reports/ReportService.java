package net.thucydides.core.reports;

import net.thucydides.core.model.TestOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Generates different Thucydides reports in a given output directory.
 */
@SuppressWarnings("restriction")
public class ReportService {

    /**
     * Where will the reports go?
     */
    private File outputDirectory;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

    /**
     * Who needs to be notified when a test is done.
     */
    private List<AcceptanceTestReporter> subscribedReporters;

    public ReportService(final File outputDirectory, final Collection<AcceptanceTestReporter> subscribedReporters) {
        this.outputDirectory = outputDirectory;
        getSubscribedReporters().addAll(subscribedReporters);
    }

    public  List<AcceptanceTestReporter> getSubscribedReporters() {
        if (subscribedReporters == null) {
            subscribedReporters = new ArrayList<AcceptanceTestReporter>();
        }
        return subscribedReporters;
    }

    public void subscribe(final AcceptanceTestReporter reporter) {
        getSubscribedReporters().add(reporter);
    }

    public void useQualifier(final String qualifier) {
        for (AcceptanceTestReporter reporter : getSubscribedReporters()) {
            reporter.setQualifier(qualifier);
        }
    }

    /**
     * A test runner can generate reports via Reporter instances that subscribe
     * to the test runner. The test runner tells the reporter what directory to
     * place the reports in. Then, at the end of the test, the test runner
     * notifies these reporters of the test outcomes. The reporter's job is to
     * process each test run outcome and do whatever is appropriate.
     *
     */
    public void generateReportsFor(final List<TestOutcome> testOutcomeResults) {

        for (AcceptanceTestReporter reporter : getSubscribedReporters()) {
            for(TestOutcome testOutcomeResult : testOutcomeResults) {
                generateReportFor(testOutcomeResult, reporter);
            }
        }
    }

    /**
     * The default reporters applicable for standard test runs.
     */
    public static List<AcceptanceTestReporter> getDefaultReporters() {
        List<AcceptanceTestReporter> reporters = new ArrayList<AcceptanceTestReporter>();

        Iterator<?> reporterImplementations = Service.providers(AcceptanceTestReporter.class);

        while (reporterImplementations.hasNext()) {
            reporters.add((AcceptanceTestReporter)reporterImplementations.next());
        }
        return reporters;
    }

    private void generateReportFor(final TestOutcome testOutcome,
                                   final AcceptanceTestReporter reporter) {
        try {
            LOGGER.info("Generating reports for test results: {})", testOutcome.getTitle());
            reporter.setOutputDirectory(outputDirectory);
            reporter.generateReportFor(testOutcome);
        } catch (IOException e) {
            throw new ReportGenerationFailedError(
                    "Failed to generate reports using " + reporter, e);
        }
    }

}
