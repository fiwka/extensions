package ru.kdev.extensions;

import ru.kdev.extensions.annotation.Extension;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ExtensionList {

    private final List<Class<?>> extensions = new ArrayList<>();

    public static ExtensionList parse(InputStream stream) throws ClassNotFoundException {
        Scanner scanner = new Scanner(stream);
        ExtensionList list = new ExtensionList();

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            list.extensions.add(Class.forName(line));
        }

        return list;
    }

    public Class<?> getExtensionByTarget(Class<?> target) {
        for(Class<?> extension : extensions) {
            if(Arrays.asList(extension.getAnnotation(Extension.class).classes()).contains(target)) {
                return extension;
            }
        }
        return null;
    }

    public Class<?>[] getAllTargets() {
        List<Class<?>> targets = new ArrayList<>();

        for(Class<?> ext : extensions) {
            targets.addAll(Arrays.asList(ext.getAnnotation(Extension.class).classes()));
        }

        return targets.toArray(new Class<?>[0]);
    }

    public List<Class<?>> getExtensions() {
        return extensions;
    }
}
