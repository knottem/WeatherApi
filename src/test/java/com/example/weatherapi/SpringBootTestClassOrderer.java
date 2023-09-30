package com.example.weatherapi;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;

/**
 * This class is used to order the tests in the order we want them to run.
 * We use this to make sure that we run unit tests before integration tests since if unit tests fail,
 * we don't want to wait for the integration tests to run.
 * <p>
 * From: <a href="https://howtodoinjava.com/junit5/test-execution-order/">https://howtodoinjava.com/junit5/test-execution-order/</a>
 */
public class SpringBootTestClassOrderer implements ClassOrderer {

    /**
     * Order the supplied {@code ClassDescriptor ClassDescriptors} and return a new list
     * @param classOrdererContext the {@code ClassOrdererContext} containing the
     * {@linkplain ClassDescriptor class descriptors} to order; never {@code null}
     */
    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(SpringBootTestClassOrderer::getOrder));
    }

    /**
     * Get the order of the test class, with the highest order being run first
     * @param classDescriptor the {@code ClassDescriptor} to get the order for
     * @return the order of the test class
     */
    private static int getOrder(ClassDescriptor classDescriptor) {
        if (classDescriptor.findAnnotation(SpringBootTest.class).isPresent()) {
            return 4;
        } else if (classDescriptor.findAnnotation(WebMvcTest.class).isPresent()) {
            return 3;
        } else if (classDescriptor.findAnnotation(DataJpaTest.class).isPresent()) {
            return 2;
        } else {
            return 1;
        }
    }
}
