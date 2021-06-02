package ru.kdev.extensions.test;

import ru.kdev.extensions.annotation.*;

@Extension(classes = {TestClass.class})
public class ExtensionTestClass {

    @TargetField
    private int test;

    @TargetField
    private static TestClass testClassImpl;

    @Inject(method = { "newMethod" }, at = Inject.At.TOP)
    public void testInject() {
        System.out.println("hello from inject!");
        System.out.println(test);
    }

    @Inject(method = { "hello" }, at = Inject.At.BOTTOM, changeReturn = true)
    public String helloInject() {
        return "injected hello";
    }

    @Inject(method = { "main" }, at = Inject.At.BOTTOM)
    public static void mainInject(String[] args) {
        ((TestInterface) testClassImpl).testInterface();
    }

    @TargetMethod
    public void newMethod() {}
}
