package me.vaperion.inspector.transformer.impl;

import me.vaperion.inspector.Inspector;
import me.vaperion.inspector.transformer.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Random;

public class FieldFinderTransformer extends Transformer {

    public FieldFinderTransformer(Inspector inspector, Random random) {
        super(inspector, random);
    }

    @Override
    public void begin() {

    }

    @Override
    public void finish() {

    }

    private String ubType = "";

    @Override
    public void visit(ClassNode classNode) {
        if (classNode.fields.stream().anyMatch(f -> f.name.equalsIgnoreCase("error"))
            && classNode.fields.stream().anyMatch(f -> f.name.equalsIgnoreCase("errorMessage"))
            && classNode.fields.stream().anyMatch(f -> f.name.equalsIgnoreCase("username"))
            && classNode.fields.stream().anyMatch(f -> f.name.equalsIgnoreCase("sessionId"))) {
            ubType = "L" + classNode.name + ";";
            return;
        }
        if (ubType.isEmpty()) return;

        MethodNode method = null;

        for (MethodNode m : classNode.methods) {
            if (!m.name.equalsIgnoreCase("login")) continue;
            if (!m.desc.equalsIgnoreCase("(Ljava/lang/String;)" + ubType)) continue;
            method = m;
        }

        if (method != null) {
            InsnList list = method.instructions;
            int index = 0;
            int paramIndex = 0;

            for(AbstractInsnNode inst : list.toArray()) {
                if (inst instanceof MethodInsnNode) {
                    MethodInsnNode methodNode = (MethodInsnNode) inst;
                    if (methodNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        if (methodNode.name.equalsIgnoreCase("addProperty")) {
                            AbstractInsnNode[] instructions = list.toArray();
                            AbstractInsnNode[] history = Arrays.copyOfRange(instructions, index - 3, index);
                            for (AbstractInsnNode node : history) {
                                if (node.getOpcode() == Opcodes.LDC) {
                                    LdcInsnNode ldc = (LdcInsnNode) node;
                                    Object cst = ldc.cst;
                                    if (cst instanceof String) {
                                        String str = (String) cst;
                                        switch (paramIndex) {
                                            case 0: {
                                                inspector.usernameFieldName = str;
                                                System.out.println("Found username field: '" + str + "'");
                                                break;
                                            }
                                            case 1: {
                                                inspector.passwordFieldName = str;
                                                System.out.println("Found password field: '" + str + "'");
                                                break;
                                            }
                                            case 2: {
                                                inspector.twofactFieldName = str;
                                                System.out.println("Found 2fa field: '" + str + "'");
                                                break;
                                            }
                                        }
                                        paramIndex++;
                                    }
                                }
                            }
                        }
                    }
                }
                index++;
            }
        }
    }

    private boolean print(ParameterNode p) {
        System.out.println(p);
        return true;
    }
}