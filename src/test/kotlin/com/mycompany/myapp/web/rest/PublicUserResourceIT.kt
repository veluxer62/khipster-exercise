package com.mycompany.myapp.web.rest

import com.mycompany.myapp.IntegrationTest
import com.mycompany.myapp.security.ADMIN
import com.mycompany.myapp.security.USER
import com.mycompany.myapp.domain.User
import com.mycompany.myapp.repository.UserRepository
import com.mycompany.myapp.service.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import com.mycompany.myapp.service.dto.UserDTO
import org.springframework.test.web.reactive.server.WebTestClient


import org.assertj.core.api.Assertions.assertThat

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@AutoConfigureWebTestClient
@WithMockUser(authorities = [ADMIN])
@IntegrationTest
class PublicUserResourceIT {

    private val DEFAULT_LOGIN = "johndoe"


    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private lateinit var user: User

    @BeforeEach
    fun initTest() {
        user = UserResourceIT.initTestUser(userRepository, em)
    }

    @Test
    fun getAllPublicUsers() {
        // Initialize the database
        userRepository.save(user).block()

        // Get all the users
        val foundUser = webTestClient.get().uri("/api/users?sort=id,DESC")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .returnResult(UserDTO::class.java).responseBody.blockFirst()

        assertThat(foundUser.login).isEqualTo(DEFAULT_LOGIN)
            }

    @Test
    fun getAllAuthorities() {
        webTestClient.get().uri("/api/authorities")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[?(@=='$ADMIN')]").hasJsonPath()
            .jsonPath("$[?(@=='$USER')]").hasJsonPath()
    }

    @Test
    @Throws(Exception::class)
    fun getAllUsersSortedByParameters() {
        // Initialize the database
        userRepository.save(user).block()

            webTestClient.get().uri("/api/users?sort=resetKey,DESC").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isBadRequest
            webTestClient.get().uri("/api/users?sort=password,DESC").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isBadRequest
            webTestClient.get().uri("/api/users?sort=resetKey,id,DESC").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isBadRequest
            webTestClient.get().uri("/api/users?sort=id,DESC").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk
    }
}
