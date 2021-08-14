package com.mycompany.myapp.config

import com.mycompany.myapp.security.ADMIN
import com.mycompany.myapp.security.jwt.JWTFilter
import com.mycompany.myapp.security.jwt.TokenProvider
import tech.jhipster.config.JHipsterProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import(SecurityProblemSupport::class)
class SecurityConfiguration(
    private val userDetailsService: ReactiveUserDetailsService,
    private val tokenProvider: TokenProvider,
    private val jHipsterProperties: JHipsterProperties,
    private val problemSupport: SecurityProblemSupport
) {


    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun reactiveAuthenticationManager() =
        UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService).apply {
            setPasswordEncoder(passwordEncoder())
        }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // @formatter:off
        http
            .securityMatcher(
                NegatedServerWebExchangeMatcher(
                    OrServerWebExchangeMatcher(
                        pathMatchers("/app/**", "/i18n/**", "/content/**", "/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/test/**"),
                        pathMatchers(HttpMethod.OPTIONS, "/**")
                    )
                )
            )
            .csrf()
            .disable()
            .addFilterAt(JWTFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
            .authenticationManager(reactiveAuthenticationManager())
            .exceptionHandling()
            .accessDeniedHandler(problemSupport)
            .authenticationEntryPoint(problemSupport)
        .and()
            .headers()
            .contentSecurityPolicy(jHipsterProperties.security.contentSecurityPolicy)
        .and()
            .referrerPolicy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
        .and()
            .frameOptions().disable()
        .and()
            .authorizeExchange()
            .pathMatchers("/api/register").permitAll()
            .pathMatchers("/api/activate").permitAll()
            .pathMatchers("/api/authenticate").permitAll()
            .pathMatchers("/api/account/reset-password/init").permitAll()
            .pathMatchers("/api/account/reset-password/finish").permitAll()
            .pathMatchers("/api/auth-info").permitAll()
            .pathMatchers("/api/**").authenticated()
            .pathMatchers("/services/**").authenticated()
            .pathMatchers("/management/health").permitAll()
            .pathMatchers("/management/info").permitAll()
            .pathMatchers("/management/prometheus").permitAll()
            .pathMatchers("/management/**").hasAuthority(ADMIN)
        // @formatter:on
        return http.build()
    }
}
