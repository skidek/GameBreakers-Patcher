package me.vaperion.inspector;

import me.vaperion.inspector.transformer.Transformer;
import me.vaperion.inspector.transformer.impl.AuthFinderTransformer;
import me.vaperion.inspector.transformer.impl.FieldFinderTransformer;
import me.vaperion.inspector.transformer.impl.PatchTransformer;
import me.vaperion.inspector.utils.StreamUtils;
import me.vaperion.inspector.utils.WebUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Inspector {

    private final List<ClassNode> classes = new ArrayList<>();
    private final List<Transformer> transformers = new ArrayList<>();
    public static final List<ClassNode> newClasses = new ArrayList<>();

    public String authUrl = "", usernameFieldName = "", passwordFieldName = "", twofactFieldName = "";

    public Inspector(File inputFile, File outputFile, boolean silent) throws IOException {
        transformers.add(new FieldFinderTransformer(this, ThreadLocalRandom.current()));
        transformers.add(new AuthFinderTransformer(this, ThreadLocalRandom.current()));
        transformers.add(new PatchTransformer(this, ThreadLocalRandom.current()));

        JarFile inputJar = new JarFile(inputFile);

        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(outputFile))) {
            if (!silent)System.out.println("[%] Reading jar...");

            out.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            StreamUtils.copy(getClass().getResourceAsStream("/manifest"), out);

            for (Enumeration<JarEntry> iter = inputJar.entries(); iter.hasMoreElements(); ) {
                JarEntry entry = iter.nextElement();
                try (InputStream in = inputJar.getInputStream(entry)) {
                    if (entry.getName().endsWith(".class")) {
                        ClassReader reader = new ClassReader(in);
                        ClassNode classNode = new ClassNode();
                        reader.accept(classNode, 0);
                        classes.add(classNode);
                    } else {
                        if (entry.getName().contains("MANIFEST")) continue;
                        out.putNextEntry(new JarEntry(entry.getName()));
                        StreamUtils.copy(in, out);
                    }
                }
            }

            if (!silent)System.out.println("[%] Running transformers...");
            for (Transformer transformer : transformers) {
                transformer.begin();
                if (!silent)System.out.println(" - Running " + transformer.getClass().getSimpleName() + "...");
                classes.forEach(transformer::visit);
                transformer.finish();
            }

            if (!silent)System.out.println("[%] Writing...");
            if (!silent)System.out.println(" - Writing classes...");
            for (ClassNode classNode : classes) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                out.putNextEntry(new JarEntry(classNode.name + ".class"));
                out.write(writer.toByteArray());
            }

            if (!silent)System.out.println(" - Writing generated classes...");
            for (ClassNode classNode : newClasses) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classNode.accept(writer);
                out.putNextEntry(new JarEntry(classNode.name + ".class"));
                out.write(writer.toByteArray());
            }

            if (!silent)System.out.println("[%] Done.");
        }
    }

}