package ru.kdev.extensions;

import ru.kdev.extensions.processor.ExtensionsClassFileTransformer;

import java.lang.instrument.Instrumentation;

public class ExtensionsBootstrap {

    public static void premain(String arg, Instrumentation instrumentation) {
        try {
            ExtensionList list = ExtensionList.parse(ExtensionsBootstrap.class.getResourceAsStream("/extensions"));

            instrumentation.addTransformer(new ExtensionsClassFileTransformer(list), true);
            instrumentation.retransformClasses(list.getAllTargets());
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Main class not found or main method not found.");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
