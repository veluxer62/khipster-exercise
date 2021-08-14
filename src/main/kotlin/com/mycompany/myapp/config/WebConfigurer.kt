package com.mycompany.myapp.config

import com.fasterxml.jackson.databind.ObjectMapper
import tech.jhipster.config.JHipsterConstants
import tech.jhipster.config.JHipsterProperties
import tech.jhipster.config.h2.H2ConfigurationHelper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.util.CollectionUtils
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.WebExceptionHandler
import org.zalando.problem.spring.webflux.advice.ProblemExceptionHandler
import org.zalando.problem.spring.webflux.advice.ProblemHandling

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
class WebConfigurer(
    
    private val env: Environment,
    private val jHipsterProperties: JHipsterProperties
) : WebFluxConfigurer {

    init {
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))) {
            H2ConfigurationHelper.initH2Console()
        }
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun corsFilter(): CorsWebFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = jHipsterProperties.cors
        if (!CollectionUtils.isEmpty(config.allowedOrigins)) {
            log.debug("Registering CORS filter")
            source.apply {
                registerCorsConfiguration("/api/**", config)
                registerCorsConfiguration("/management/**", config)
                registerCorsConfiguration("/v2/api-docs", config)
                registerCorsConfiguration("/v3/api-docs", config)
                registerCorsConfiguration("/swagger-resources", config)
                registerCorsConfiguration("/swagger-ui/**", config)
            }
        }
        return CorsWebFilter(source)
    }

    // TODO: remove when this is supported in spring-data / spring-boot
    @Bean
    fun reactivePageableHandlerMethodArgumentResolver() = ReactivePageableHandlerMethodArgumentResolver()

    // TODO: remove when this is supported in spring-boot
    @Bean
    fun reactiveSortHandlerMethodArgumentResolver() = ReactiveSortHandlerMethodArgumentResolver()

    @Bean
    @Order(-2) // The handler must have precedence over WebFluxResponseStatusExceptionHandler and Spring Boot's ErrorWebExceptionHandler
    fun problemExceptionHandler(mapper: ObjectMapper, problemHandling: ProblemHandling): WebExceptionHandler {
        return ProblemExceptionHandler(mapper, problemHandling)
    }
}
