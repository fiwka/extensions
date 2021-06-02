package ru.kdev.extensions.processor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import ru.kdev.extensions.annotation.Extension;
import ru.kdev.extensions.annotation.Inject;
import ru.kdev.extensions.annotation.TargetField;
import ru.kdev.extensions.annotation.TargetMethod;
import ru.kdev.extensions.internal.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ExtensionsProcessor {

    private static final List<Integer> RETURN_OPCODES = Arrays.asList(
            Opcodes.RETURN,
            Opcodes.ARETURN,
            Opcodes.DRETURN,
            Opcodes.FRETURN,
            Opcodes.IRETURN,
            Opcodes.LRETURN
            );

    public static byte[] process(Class<?> extension, Class<?> target) throws IOException {
        if(extension.isAnnotationPresent(Extension.class)) {
            ClassNode extensionNode = new ClassNode();
            ClassReader extensionReader = new ClassReader(extension.getName());
            extensionReader.accept(extensionNode, 0);

            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(target.getName());
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            reader.accept(classNode, 0);

            for(Method method : extension.getDeclaredMethods()) {
                if(method.isAnnotationPresent(TargetMethod.class)) {
                    MethodNode node = extensionNode.methods.stream().filter(x -> x.name.equals(method.getName()) && x.desc.equals(Util.getMethodDescriptor(method))).findFirst().get();

                    for(MethodNode methodNode : extensionNode.methods) {
                        for(AbstractInsnNode insnNode : methodNode.instructions) {
                            if(insnNode instanceof MethodInsnNode) {
                                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;

                                if(methodInsnNode.name.equals(node.name) &&
                                    methodInsnNode.desc.equals(node.desc) &&
                                    methodInsnNode.owner.equals(extensionNode.name)) {
                                    if(Modifier.isStatic(method.getModifiers())) {
                                        methodNode.instructions.insertBefore(
                                                methodInsnNode,
                                                new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, node.name, node.desc)
                                        );
                                    } else {
                                        methodNode.instructions.insertBefore(
                                                methodInsnNode,
                                                new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classNode.name, node.name, node.desc)
                                        );
                                    }
                                    methodNode.instructions.remove(methodInsnNode);
                                }
                            }
                        }
                    }
                }
            }

            for(Field field : extension.getDeclaredFields()) {
                if(field.isAnnotationPresent(TargetField.class)) {
                    FieldNode node = extensionNode.fields.stream().filter(x -> x.name.equals(field.getName())).findFirst().get();

                    for(MethodNode methodNode : extensionNode.methods) {
                        for(AbstractInsnNode insnNode : methodNode.instructions) {
                            if(insnNode instanceof FieldInsnNode) {
                                FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;

                                if(fieldInsnNode.name.equals(field.getName()) &&
                                        fieldInsnNode.desc.equals(node.desc) &&
                                        fieldInsnNode.owner.equals(extensionNode.name)) {
                                    if(Modifier.isStatic(field.getModifiers())) {
                                        methodNode.instructions.insertBefore(
                                                fieldInsnNode,
                                                new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, fieldInsnNode.name, fieldInsnNode.desc)
                                        );
                                    } else {
                                        methodNode.instructions.insertBefore(
                                                fieldInsnNode,
                                                new FieldInsnNode(Opcodes.GETFIELD, classNode.name, fieldInsnNode.name, fieldInsnNode.desc)
                                        );
                                    }
                                    methodNode.instructions.remove(fieldInsnNode);
                                }
                            }
                        }
                    }
                }
            }

            for(Method method : extension.getDeclaredMethods()) {
                if(method.isAnnotationPresent(Inject.class)) {
                    Inject inject = method.getAnnotation(Inject.class);
                    MethodNode injectMethodNode = extensionNode.methods.stream().filter(x -> x.name.equals(method.getName()) && x.desc.equals(Util.getMethodDescriptor(method))).findFirst().get();

                    Method[] targetMethods = Arrays.stream(target.getDeclaredMethods())
                            .filter(x -> Arrays.stream(inject.method()).anyMatch(y -> x.getName().equals(y)))
                            .toArray(Method[]::new);

                    for(Method targetMethod : targetMethods) {
                        classNode.methods.stream().filter(x -> x.name.equals(targetMethod.getName()) && x.desc.equals(injectMethodNode.desc)).findFirst().ifPresent(targetMethodNode -> {
                            if(inject.at() == Inject.At.TOP) {
                                injectMethodNode.instructions.resetLabels();

                                Map<LabelNode, LabelNode> labels = new HashMap<>();

                                List<AbstractInsnNode> reversedList = validateInsns(injectMethodNode.instructions.toArray());
                                Collections.reverse(reversedList);

                                reversedList.forEach(x -> {
                                    if(x instanceof LabelNode) {
                                        labels.put((LabelNode) x, new LabelNode(new Label()));
                                    }
                                });

                                for(AbstractInsnNode insnNode : reversedList) {
                                    AbstractInsnNode clone = insnNode.clone(labels);
                                    targetMethodNode.instructions.insertBefore(targetMethodNode.instructions.getFirst(), clone);
                                }
                            } else if(inject.at() == Inject.At.BOTTOM) {
                                AbstractInsnNode[] returns = Arrays.stream(targetMethodNode.instructions.toArray()).filter(x -> RETURN_OPCODES.contains(x.getOpcode()))
                                        .toArray(AbstractInsnNode[]::new);
                                AbstractInsnNode targetReturnInsnNode = returns[returns.length - 1];

                                injectMethodNode.instructions.resetLabels();

                                Map<LabelNode, LabelNode> labels = new HashMap<>();

                                injectMethodNode.instructions.forEach(x -> {
                                    if(x instanceof LabelNode) {
                                        labels.put((LabelNode) x, new LabelNode(new Label()));
                                    }
                                });

                                List<AbstractInsnNode> validInsns = validateInsns(injectMethodNode.instructions.toArray());

                                for(AbstractInsnNode insnNode : validInsns) {
                                    targetMethodNode.instructions.insertBefore(targetReturnInsnNode, insnNode.clone(labels));
                                }

                                if(inject.changeReturn()) {
                                    AbstractInsnNode[] injectReturns = Arrays.stream(injectMethodNode.instructions.toArray()).filter(x -> RETURN_OPCODES.contains(x.getOpcode()))
                                            .toArray(AbstractInsnNode[]::new);
                                    AbstractInsnNode returnNode = injectReturns[injectReturns.length - 1];

                                    targetMethodNode.instructions.insertBefore(targetReturnInsnNode, returnNode);
                                    targetMethodNode.instructions.remove(targetReturnInsnNode);
                                }
                            }
                        });
                    }
                }
            }

            classNode.accept(writer);

            File dirs = new File(".out/extensions");
            File outFile = new File(".out/extensions/" + target.getSimpleName() + ".class");
            if(!outFile.exists()) {
                dirs.mkdirs();
                outFile.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(outFile);
            out.write(writer.toByteArray());
            out.close();
            return writer.toByteArray();
        }
        return new byte[0];
    }

    static List<AbstractInsnNode> validateInsns(AbstractInsnNode[] insns) {
        List<AbstractInsnNode> validInsns;

        if(RETURN_OPCODES.contains(insns[insns.length - 2].getOpcode())) {
            AbstractInsnNode[] buffer = Arrays.copyOf(insns, insns.length - 2);
            validInsns = Arrays.asList(buffer);
        } else {
            validInsns = Arrays.asList(insns);
        }

        return validInsns;
    }
}
