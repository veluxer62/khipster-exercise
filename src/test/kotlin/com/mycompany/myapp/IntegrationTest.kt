package com.mycompany.myapp

import com.mycompany.myapp.KhipsterApp
import com.mycompany.myapp.ReactiveSqlTestContainerExtension
import org.junit.jupiter.api.extension.ExtendWith

import org.springframework.boot.test.context.SpringBootTest

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [KhipsterApp::class])
@ExtendWith(ReactiveSqlTestContainerExtension::class)
annotation class IntegrationTest {}
