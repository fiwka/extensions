package ru.kdev.extensions.test;

public class TestClass {

    private final int test = 1;
    private static TestClass testClassImpl;

    public static void main(String[] args) {
        test();
        test2();

        testClassImpl = new TestClass();
        testClassImpl.aaa("aaaa");

        System.out.println(testClassImpl.hello());
    }

    public void aaa(String a) {
        System.out.println(test);
        System.out.println(a);
        newMethod();
    }

    public String hello() {
        return "hello";
    }

    public void newMethod() {
        System.out.println("invoked new method!");
    }

    public void testInterface() {
        System.out.println("hi");
    }

    public static void test() {
        System.out.println("test");
    }
    public static void test2() {
        System.out.println("test 2");
    }
}
