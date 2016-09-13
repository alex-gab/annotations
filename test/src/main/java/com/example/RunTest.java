package com.example;

import com.example.annotations.Test;
import com.example.annotations.TesterInfo;

import java.lang.reflect.Method;

public final class RunTest {
    public static void main(String[] args) {
        System.out.println("Testing...");
        int passed = 0, failed = 0, count = 0, ignore = 0;

        final Class<TestExample> obj = TestExample.class;

        if (obj.isAnnotationPresent(TesterInfo.class)) {
            final TesterInfo testerInfo = obj.getAnnotation(TesterInfo.class);
            System.out.printf("%nPriority :%s", testerInfo.priority());
            System.out.printf("%nCreatedBy :%s", testerInfo.createdBy());
            System.out.printf("%nTags :");

            int tagLength = testerInfo.tags().length;
            for (String tag : testerInfo.tags()) {
                if (tagLength > 1) {
                    System.out.print(tag + ", ");
                } else {
                    System.out.print(tag);
                }
                tagLength--;
            }

            System.out.printf("%nLastModified :%s%n%n", testerInfo.lastModified());
        }

        for (Method method : obj.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                final Test test = method.getAnnotation(Test.class);
                if (test.enabled()) {
                    try {
                        method.invoke(obj.newInstance());
                        System.out.printf("%s - Test '%s' - passed %n", ++count, method.getName());
                        passed++;
                    } catch (Throwable ex) {
                        System.out.printf("%s - Test '%s' - failed: %s %n", ++count, method.getName(), ex.getCause());
                        failed++;
                    }
                }
            }
            System.out.printf("%nResult : Total : %d, Passed: %d, Failed %d, Ignore %d%n", count, passed, failed, ignore);
        }
    }
}
