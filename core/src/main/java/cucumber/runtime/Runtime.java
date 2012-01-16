package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import cucumber.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static cucumber.runtime.model.CucumberFeature.loadFeatures;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {
    private static final byte ERRORS = 0x1;

    private final UndefinedStepsTracker tracker;
    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final Collection<? extends Backend> backends;
    private final RuntimeOptions runtimeOptions;
    private final ResourceLoader resourceLoader;

    public Runtime(RuntimeOptions runtimeOptions, ResourceLoader resourceLoader) {
        this(runtimeOptions, resourceLoader, loadBackends(resourceLoader));
    }

    public Runtime(RuntimeOptions runtimeOptions, ResourceLoader resourceLoader, Collection<? extends Backend> backends) {
        this.runtimeOptions = runtimeOptions;
        this.backends = backends;
        this.resourceLoader = resourceLoader;
        this.tracker = new UndefinedStepsTracker(backends);
    }

    private static Collection<? extends Backend> loadBackends(ResourceLoader resourceLoader) {
        return new ClasspathResourceLoader().instantiateSubclasses(Backend.class, "cucumber/runtime", new Class[]{ResourceLoader.class}, new Object[]{resourceLoader});
    }

    public void addError(Throwable error) {
        errors.add(error);
    }

    public void run() {
        runtimeOptions.writeDotCucumber(runtime);
        for (CucumberFeature cucumberFeature : loadFeatures(resourceLoader, runtimeOptions.getFeaturePaths(), runtimeOptions.getFilters())) {
            run(cucumberFeature);
        }
        runtimeOptions.getFormatter().done();
    }

    public void run(CucumberFeature cucumberFeature) {
        Formatter formatter = runtimeOptions.getFormatter();
        formatter.uri(cucumberFeature.getUri());
        formatter.feature(cucumberFeature.getFeature());
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            cucumberTagStatement.run(formatter, runtimeOptions.getReporter(), this);
        }
        formatter.eof();
    }

    public void buildBackendWorlds(World world) {
        for (Backend backend : backends) {
            backend.buildWorld(runtimeOptions.getGluePaths(), world);
        }
        tracker.reset();
    }

    public void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }

    public boolean isDryRun() {
        return runtimeOptions.isDryRun();
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public byte exitStatus() {
        byte result = 0x0;
        if (!errors.isEmpty()) {
            result |= ERRORS;
        }
        return result;
    }

    public void storeStepKeyword(Step step, Locale locale) {
        tracker.storeStepKeyword(step, locale);
    }

    public void addUndefinedStep(Step step, Locale locale) {
        tracker.addUndefinedStep(step, locale);
    }

    public List<String> getSnippets() {
        return tracker.getSnippets();
    }
}
