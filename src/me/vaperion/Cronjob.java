package me.vaperion;

import com.yamajun.cloudbypass.CHttpRequester;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import me.vaperion.inspector.Main;
import org.jsoup.Connection;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class Cronjob {

    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("url").withRequiredArg().required().ofType(String.class);

        OptionSet options;

        try {
            options = parser.parse(args);
        } catch (OptionException ex) {
            System.out.println("csicskakoponya --url <url>");
            System.out.println(ex.getMessage());
            System.exit(1);
            return;
        }

        try {
            System.out.println("Downloading new launcher...");
            File target = new File("/home/topatch.jar");

            CHttpRequester requester = new CHttpRequester();
            Connection.Response resp = requester.getFile((String) options.valueOf("url"), new HashMap<>());
            if (resp.statusCode() == 200) {
                try (FileOutputStream fos = new FileOutputStream(target)) {
                    fos.write(resp.bodyAsBytes());
                }
            } else {
                System.out.println("Failed to download file");
            }

            File dlPath = new File("/var/www/gb/latest.jar");
            //File dlPath = new File("/var/www/gb/patched.jar");
            if (dlPath.exists()) dlPath.delete();

            System.out.println("Patching jar...");
            Main.main(new String[]{
                    "--input",
                    target.getPath(),
                    "--output",
                    "/home/latest.jar",
                    //"/home/patched.jar",
                    "--silent",
                    "true"
            });

            System.out.println("Copying jar...");
            //Files.copy(new File("/home/patched.jar").toPath(), new File("/var/www/gb/patched.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new File("/home/latest.jar").toPath(), new File("/var/www/gb/latest.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}