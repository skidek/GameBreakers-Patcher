package me.vaperion.inspector.transformer.impl;

import me.vaperion.inspector.Inspector;
import me.vaperion.inspector.transformer.Transformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Random;

public class AuthFinderTransformer extends Transformer {

    public AuthFinderTransformer(Inspector inspector, Random random) {
        super(inspector, random);
    }

    @Override
    public void begin() {

    }

    @Override
    public void finish() {

    }

    @Override
    public void visit(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (!method.name.equalsIgnoreCase("login")
                || !method.desc.startsWith("(Ljava/lang/String;)")) continue;
            InsnList insnList = method.instructions;

            insnList.iterator().forEachRemaining(inst -> {
                if (inst instanceof LdcInsnNode) {
                    LdcInsnNode ldc = (LdcInsnNode) inst;
                    if (!(ldc.cst instanceof String)) return;
                    String val = (String) ldc.cst;
                    if (val.startsWith("http")) {
                        System.out.println("Found auth url: " + val);
                        inspector.authUrl = val;
                    }
                }
            });
        }
    }
}