package me.vaperion.inspector.transformer;

import lombok.AllArgsConstructor;
import me.vaperion.inspector.Inspector;
import org.objectweb.asm.tree.ClassNode;

import java.util.Random;

@AllArgsConstructor
public abstract class Transformer {

    protected final Inspector inspector;
    protected final Random random;

    public abstract void begin();
    public abstract void finish();
    public abstract void visit(ClassNode classNode);
}