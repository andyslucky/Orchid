package com.eden.orchid.testhelpers

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

interface OrchidUnitTest : BaseOrchidTest {

    @BeforeEach
    fun unitTestSetUp() {
    }

    @AfterEach
    fun unitTestTearDown() {
    }
}
