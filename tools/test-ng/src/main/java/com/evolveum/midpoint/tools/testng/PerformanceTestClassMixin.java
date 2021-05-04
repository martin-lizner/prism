/*
 * Copyright (C) 2010-2021 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.tools.testng;

import org.testng.annotations.BeforeClass;

/**
 * Mixin supporting work with {@link TestMonitor} at the class-scope level
 * (one test report for all the methods in the test class).
 * Details of setting of {@link TestMonitor} is up to the class, methods from
 * {@link PerformanceTestCommonMixin} must be implemented.
 */
public interface PerformanceTestClassMixin extends PerformanceTestCommonMixin {

    @BeforeClass
    default void initTestMonitor() {
        createTestMonitor();
    }

    /*
     * @AfterClass dumpReport() implemented in AbstractUnitTest and AbstractSpringTest.
     * After-class method is implemented in both concrete classes with `instanceof` check,
     * because if implemented here the method is called too late.
     * This is not a problem for before-class method, so it stays here.
     */
}
