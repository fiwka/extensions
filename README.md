# Extensions

## How to use

Create your extension and add it to ```extensions``` file in resources.

```extensions``` example:

```
ru.kdev.extensions.example.ExtenstionMyClass
ru.kdev.extensions.example.ExtenstionMyAnotherClass
```

Run your jar file with javaagent ```extensions-core-XXX-SNAPSHOT.jar```

Run command example:

```
java -javaagent:extensions-core-1.0-SNAPSHOT.jar -jar myjar.jar
```

## Example

Your extension class:

```java
@Extension(classes = { MyClass.class })
public class ExtensionMyClass {

    @TargetField
    private int a;

    @Inject(method = { "printA" }, at = Inject.At.TOP)
    public void printAInject() {
        System.out.println("Hello from inject!");
    }
    
    @Inject(method = { "getA" }, at = Inject.At.BOTTOM, changeReturn = true)
    public int getAInject() {
        return a * 2;
    }
    
    @Inject(method = { "otherMethod" }, at = Inject.At.REWRITE)
    public void otherMethodInject() {
        System.out.println("rewrited");
    }
}
```

Your class:

```java
public class MyClass {

    private int a = 120;
    
    public void printA() {
        System.out.println(a);
    }
    
    public void otherMethod() {
        System.out.println("not rewrited");
    }
    
    public int getA() {
        return a;
    }
}
```

Output:

```java
public class MyClass {

    private int a = 120;
    
    public void printA() {
        System.out.println("Hello from inject!");
        System.out.println(a);
    }
    
    public void otherMethod() {
        System.out.println("rewrited");
    }
    
    public int getA() {
        return a * 2;
    }
}
```