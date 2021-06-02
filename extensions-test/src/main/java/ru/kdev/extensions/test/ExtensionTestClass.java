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

    @Inject(method = { "toRewrite" }, at = Inject.At.REWRITE)
    public void testRewrite() {
        System.out.println("Hello!");
    }

    @Inject(method = { "hello" }, at = Inject.At.BOTTOM, changeReturn = true)
    public String helloInject() {
        return "injected hello";
    }

    @TargetMethod
    public void newMethod() {}
}
