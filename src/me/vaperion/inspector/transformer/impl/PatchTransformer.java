package me.vaperion.inspector.transformer.impl;

import jdk.internal.org.objectweb.asm.Opcodes;
import me.vaperion.inspector.Inspector;
import me.vaperion.inspector.transformer.Transformer;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.Random;

public class PatchTransformer extends Transformer {

    public PatchTransformer(Inspector inspector, Random random) {
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
        afterStartCheck(classNode);
        modCheck(classNode);
    }

    private void modCheck(ClassNode classNode) {
        if (classNode.methods.stream().noneMatch(m -> m.name.equalsIgnoreCase("afterStartCheck")
            && m.desc.equalsIgnoreCase("()Z"))
            || classNode.methods.stream().noneMatch(m -> m.name.equalsIgnoreCase("saveUrl"))
            || classNode.methods.stream().noneMatch(m -> m.name.equalsIgnoreCase("clearNatives"))
            || classNode.methods.stream().noneMatch(m -> m.name.equalsIgnoreCase("run")
            && m.desc.equalsIgnoreCase("()V"))) return;

        for (MethodNode method : classNode.methods) {
            if (method.name.equalsIgnoreCase("run")
                    && method.desc.equalsIgnoreCase("()V")) {
                InsnList insnList = method.instructions;

                Iterator<AbstractInsnNode> iterator = insnList.iterator();

                AbstractInsnNode aloadFour = null;
                boolean methBefore = true;

                while (iterator.hasNext()) {
                    AbstractInsnNode inst = iterator.next();

                    if (inst instanceof VarInsnNode) {
                        VarInsnNode var = (VarInsnNode) inst;
                        if (var.getOpcode() == Opcodes.ALOAD && var.var == 4) aloadFour = var;
                    }
                    if (inst instanceof MethodInsnNode) {
                        MethodInsnNode meth = (MethodInsnNode) inst;
                        if (meth.name.equalsIgnoreCase("delete") && meth.desc.equalsIgnoreCase("()Z") && aloadFour != null) {
                            insnList.remove(aloadFour);
                            iterator.remove();
                            methBefore = true;
                            continue;
                        }
                    }
                    if (inst.getOpcode() == Opcodes.POP && methBefore) {
                        iterator.remove();
                        break;
                    }
                    methBefore = false;
                }
            }
        }
    }

    private void afterStartCheck(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equalsIgnoreCase("afterStartCheck")
                    && method.desc.equalsIgnoreCase("()Z")) {
                InsnList insnList = method.instructions;
                insnList.clear();

                InsnNode ireturn = new InsnNode(Opcodes.IRETURN);
                insnList.add(ireturn);

                insnList.insertBefore(ireturn, new LabelNode());
                insnList.insertBefore(ireturn, new InsnNode(Opcodes.ICONST_1));
            }
        }
    }
}