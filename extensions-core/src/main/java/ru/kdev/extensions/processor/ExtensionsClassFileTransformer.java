package ru.kdev.extensions.processor;

import ru.kdev.extensions.ExtensionList;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ExtensionsClassFileTransformer implements ClassFileTransformer {

    private final ExtensionList extensionList;

    public ExtensionsClassFileTransformer(ExtensionList list) {
        this.extensionList = list;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        Class<?> extension = extensionList.getExtensionByTarget(classBeingRedefined);

        if(extension != null) {
            try {
                return ExtensionsProcessor.process(extension, classBeingRedefined);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return classfileBuffer;
    }
}
