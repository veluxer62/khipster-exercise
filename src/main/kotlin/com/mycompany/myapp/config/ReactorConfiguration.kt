package com.mycompany.myapp.config

import tech.jhipster.config.JHipsterConstants
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import reactor.core.publisher.Hooks

@Configuration
@Profile("!" + JHipsterConstants.SPRING_PROFILE_PRODUCTION)
class ReactorConfiguration {
    fun ReactorConfiguration() = Hooks.onOperatorDebug()
}
