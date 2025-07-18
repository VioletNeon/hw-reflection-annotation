package hw1;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.lang.reflect.Parameter;

import java.util.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface BeforeSuite {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface AfterSuite {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface BeforeTest {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface AfterTest  {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface Test  { int priority() default 5; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface CsvSource { String value(); }

class Suite {
    @BeforeSuite static void testStart1() { System.out.println("test before suite - 1"); }
    @AfterSuite static void testEnd1() { System.out.println("test after suite - 1"); }

    @BeforeTest void setup1() { System.out.println("test setup - 1"); }
    @AfterTest  void teardown1() { System.out.println("test teardown - 1"); }

    @Test(priority = -1) void test1() { System.out.println("test method - 1: priority = -1"); }
    @Test(priority = 7) void test2() { System.out.println("test method - 2: priority = 7"); }
    @Test(priority = 3) void test3() { System.out.println("test method - 3: priority = 3"); }
    @Test(priority = 11) void test4() { System.out.println("test method - 4: priority = 11"); }
    @Test void test5() { System.out.println("test method - 5: priority = 5"); }

    @CsvSource("10, Java, 20, true") void testCsvSourceMethod(int a, String b, int c, boolean d) {
        System.out.println("test csv source method: " + "a = " + a + ", b = " + b + ", c = " + c + ", d = " + d);
    }

    @BeforeTest void setup2() { System.out.println("test setup - 2"); }
    @AfterTest  void teardown2() { System.out.println("test teardown - 2"); }

    @AfterSuite static void testEnd2() { System.out.println("test after suite - 2"); }
    @BeforeSuite static void testStart2() { System.out.println("test before suite - 2"); }
}

public class TestRunner {
    public static void runTests(Class<?> c) throws Exception {
        List<Method> beforeSuites = new ArrayList<>();
        List<Method> afterSuites  = new ArrayList<>();
        List<Method> setups  = new ArrayList<>();
        List<Method> teardowns  = new ArrayList<>();
        List<Method> testMethods = new ArrayList<>();
        List<Method> testCsvSourceMethods = new ArrayList<>();

        Object inst = c.getDeclaredConstructor().newInstance();

        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(BeforeSuite.class)) beforeSuites.add(m);
            else if (m.isAnnotationPresent(BeforeTest.class)) setups.add(m);
            else if (m.isAnnotationPresent(AfterTest.class)) teardowns.add(m);
            else if (m.isAnnotationPresent(AfterSuite.class)) afterSuites.add(m);
            else if (m.isAnnotationPresent(CsvSource.class)) testCsvSourceMethods.add(m);
            else if (m.isAnnotationPresent(Test.class)) testMethods.add(m);
        }

        testMethods.sort(Comparator.comparingInt(
                (Method m) -> m.getAnnotation(Test.class).priority()).reversed());

        if (beforeSuites.size() > 1) {
            System.out.println("Multiple BeforeSuite annotations are present. Only the first one defined will be executed.");
        }

        if (afterSuites.size() > 1) {
            System.out.println("Multiple AfterSuite annotations are present. Only the first one defined will be executed");
        }

        if (setups.size() > 1) {
            System.out.println("Multiple BeforeTest annotations are present. Only the first one defined will be executed.");
        }

        if (teardowns.size() > 1) {
            System.out.println("Multiple AfterTest annotations are present. Only the first one defined will be executed." + "\n");
        }

        if (!beforeSuites.isEmpty()) {
            beforeSuites.get(0).invoke(null);
        }

        for (Method m : testMethods) {
            var priority = m.getAnnotation(Test.class).priority();

            if (priority < 0 || priority > 10) {
                System.out.println("The priority parameter (" + priority + ") must be within the range of 0 to 10, inclusive. Method invocation will be excluded.");
                continue;
            }

            if (!setups.isEmpty()) setups.get(0).invoke(inst);

            m.invoke(inst);

            if (!teardowns.isEmpty()) teardowns.get(0).invoke(inst);
        }

        for (Method m : testCsvSourceMethods) {
            runTestCsvSourceTest(m, inst);
        }

        if (!afterSuites.isEmpty()) {
            afterSuites.get(0).invoke(null);
        }
    }

    private static void runTestCsvSourceTest(Method method, Object inst) throws Exception {
        String csvString = method.getAnnotation(CsvSource.class).value();
        String[] csvRows = csvString.split(", ");
        Parameter[] parameters = method.getParameters();

        if (csvRows.length != parameters.length) {
            System.err.println("Error: The number of parameters does not match the number of values in the CSV for the method " + method.getName());
            return;
        }

        Object[] parsedValues = parseCsvValues(csvRows, parameters);

        method.invoke(inst, parsedValues);
    }


    private static Object[] parseCsvValues(String[] csvRows, Parameter[] parameters) {
        Object[] parsedValues = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            String value = csvRows[i];

            if (type == int.class) {
                parsedValues[i] = Integer.parseInt(value);
            } else if (type == String.class) {
                parsedValues[i] = value;
            } else if (type == boolean.class) {
                parsedValues[i] = Boolean.parseBoolean(value);
            } else if (type == double.class) {
                parsedValues[i] = Double.parseDouble(value);
            }
            else {
                throw new IllegalArgumentException("Unsupported parameter type: " + type.getName());
            }
        }

        return parsedValues;
    }

    public static void main(String[] a) throws Exception { runTests(Suite.class); }
}
