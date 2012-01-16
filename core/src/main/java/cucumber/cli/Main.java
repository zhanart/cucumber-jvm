package cucumber.cli;

import cucumber.formatter.FormatterFactory;
import cucumber.formatter.MultiFormatter;
import cucumber.io.FileResourceLoader;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.snippets.SummaryPrinter;
import gherkin.formatter.Formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;

public class Main {
    private static final String USAGE = "TODO - Write the help";
    static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");

    public static void main(String[] argv) throws Throwable {
        List<String> args = new ArrayList<String>(asList(argv));
        RuntimeOptions opts = new RuntimeOptions();

        String format = "progress";

        FormatterFactory formatterFactory = new FormatterFactory();
        MultiFormatter multiFormatter = new MultiFormatter();

        while (!args.isEmpty()) {
            String arg = args.remove(0);

            if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println(USAGE);
                System.exit(0);
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println(VERSION);
                System.exit(0);
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = args.remove(0);
                opts.addGluePath(gluePath);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                String tags = args.remove(0);
                opts.addTags(tags);
            } else if (arg.equals("--format") || arg.equals("-f")) {
                format = args.remove(0);
                opts.addFormat(format);
            } else if (arg.equals("--out") || arg.equals("-o")) {
                File out = new File(args.remove(0));
                opts.addOut(out);
                Formatter formatter = formatterFactory.createFormatter(format, out);
                multiFormatter.add(formatter);
            } else if (arg.equals("--dotcucumber")) {
                File dotCucumber = new File(args.remove(0));
                opts.setDotCucumber(dotCucumber);
            } else if (arg.equals("--dry-run") || arg.equals("-d")) {
                opts.setDryRun(true);
            } else {
                opts.addFeaturePath(arg);
            }
        }

        if (multiFormatter.isEmpty()) {
            multiFormatter.add(formatterFactory.createFormatter(format, System.out));
        }

        Runtime runtime = new Runtime(opts, new FileResourceLoader());

        runtime.run();
        printSummary(runtime);
        System.exit(runtime.exitStatus());
    }

    private static void printSummary(Runtime runtime) {
        new SummaryPrinter(System.out).print(runtime);
    }
}
