package me.vaperion.inspector;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("input").withRequiredArg().required().ofType(File.class);
        parser.accepts("output").withRequiredArg().required().ofType(File.class);
        parser.accepts("silent").withOptionalArg().defaultsTo("false").ofType(Boolean.class);

        OptionSet options;

        try {
            options = parser.parse(args);
        } catch (OptionException ex) {
            System.out.println("program --input <path> --output <path> --silent <true/false>");
            System.out.println(ex.getMessage());
            System.exit(1);
            return;
        }

        File inputFile = (File) options.valueOf("input");
        File outputFile = (File) options.valueOf("output");

        try {
            new Inspector(inputFile, outputFile, (boolean) options.valueOf("silent"));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}