package cucumber.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.formatter.MultiFormatter;
import cucumber.runtime.autocomplete.MetaStepdef;
import cucumber.runtime.autocomplete.StepdefGenerator;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cucumber.runtime.model.CucumberFeature.loadFeatures;
import static java.util.Collections.emptyList;

public class RuntimeOptions {
    private static final List<Object> NO_FILTERS = emptyList();
    private static final Collection<String> NO_TAGS = emptyList();

    private final MultiFormatter multiFormatter = new MultiFormatter();
    private final List<Object> filters = new ArrayList<Object>();
    private final List<String> featurePaths = new ArrayList<String>();
    private final List<String> gluePaths = new ArrayList<String>();
    private boolean dryRun;
    private File dotCucumber;

    public void addGluePath(String gluePath) {
        gluePaths.add(gluePath);
    }

    public void addTags(String tags) {
        filters.add(tags);
    }

    public void addFormat(String format) {
    }

    public void addOut(File out) {
    }

    public void setDotCucumber(File dotCucumber) {
        this.dotCucumber = dotCucumber;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    // TODO: Use PathWithLines and add line filter if any
    public void addFeaturePath(String path) {
        featurePaths.add(path);
    }

    public void validate() {
        if (gluePaths.isEmpty()) {
            throw new CucumberException("Missing option: --glue");
        }
    }

    public List<String> getGluePaths() {
        return gluePaths;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public List<Object> getFilters() {
        return filters;
    }

    public List<String> getFeaturePaths() {
        return featurePaths;
    }

    public Formatter getFormatter() {
        return multiFormatter.formatterProxy();
    }

    public Reporter getReporter() {
        return multiFormatter.reporterProxy();
    }

    public void writeDotCucumber(Runtime runtime) throws IOException {
        dotCucumber.mkdirs();
        List<CucumberFeature> features = loadFeatures(resourceLoader, featurePaths, NO_FILTERS);
        World world = new RuntimeWorld(runtime, NO_TAGS);
        runtime.buildBackendWorlds(world);
        List<StepDefinition> stepDefs = world.getStepDefinitions();
        List<MetaStepdef> metaStepdefs = new StepdefGenerator().generate(stepDefs, features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(metaStepdefs);

        FileWriter metaJson = new FileWriter(new File(dotCucumber, "stepdefs.json"));
        metaJson.append(json);
        metaJson.close();
    }
}
