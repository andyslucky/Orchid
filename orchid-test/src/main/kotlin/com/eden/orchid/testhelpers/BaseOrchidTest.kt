package com.eden.orchid.testhelpers

import clog.Clog
import clog.dsl.setMinPriority
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

interface BaseOrchidTest {

    @BeforeEach
    fun baseSetUp() {
        disableLogging()
    }

    @AfterEach
    fun baseTearDown() {
        enableLogging()
    }

    fun disableLogging() {
        Clog.setMinPriority(Clog.Priority.FATAL)
    }

    fun enableLogging() {
        Clog.setMinPriority(Clog.Priority.VERBOSE)
    }
}
