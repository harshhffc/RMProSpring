package com.homefirstindia.rmproserver.security

import com.homefirstindia.rmproserver.dto.v1.common.UserAuthRequest
import com.homefirstindia.rmproserver.dto.v1.externalPartner.EPAuthRequest
import com.homefirstindia.rmproserver.repository.v1.PartnerMasterRepository
import com.homefirstindia.rmproserver.repository.v1.UserRepositoryMaster
import com.homefirstindia.rmproserver.utils.*
import com.homefirstindia.rmproserver.utils.LoggerUtils.printLog
import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Order(1)
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
class ExternalPartnerSecurityConfig(
    @Autowired val partnerAuthentication: PartnerAuthentication
) {

    @Autowired
    @Qualifier("myAuthenticationEntryPoint")
    var authEntryPoint: AuthenticationEntryPoint? = null

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors().and().csrf().disable()
            .requestMatchers().antMatchers("/ep/v1/**")
            .and()
            .authorizeRequests()
            .antMatchers("/ep/v1/authenticate").permitAll()
            .antMatchers("/ep**").authenticated()
            .and()
            .addFilterBefore(partnerAuthentication, BasicAuthenticationFilter::class.java)
            .exceptionHandling()
            .authenticationEntryPoint(authEntryPoint)

        println("Order 1")

        return http.build()
    }

}

@Order(2)
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
class RMSecurityConfig(
    @Autowired val rmproAuthentication: RMProAuthentication
) {

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer? {
        return WebSecurityCustomizer {
                web: WebSecurity -> web.ignoring().antMatchers("/rms/public/**", "/rms/ep/**")
        }
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors().and().csrf().disable().authorizeRequests()
            .antMatchers("/public/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(rmproAuthentication, BasicAuthenticationFilter::class.java)

        return http.build()

    }

    private val domains = listOf(
        "http://localhost:4200",
        "http://localhost"
    )

    private val allowHeader = listOf(
        "Origin",
        "X-Requested-With",
        "Content-Type",
        "Accept",
        "Key",
        "Authorization",
        "userSourceId",
        "sessionPasscode",
        "sourcePasscode"
    )

    private val maxAge: Long = 3600

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource? {
        val source = UrlBasedCorsConfigurationSource()

        val configuration = CorsConfiguration()
        configuration.allowedOrigins = domains
        configuration.allowedMethods = listOf("GET", "POST")
        configuration.maxAge = maxAge
        configuration.allowedHeaders = allowHeader

        source.registerCorsConfiguration("/**", configuration)

        return source
    }

    @Bean
    fun roleHierarchy(): RoleHierarchy? {

        val hierarchy = StringBuilder()
        hierarchy.append("$ROLE_SUPER_ADMIN > $ROLE_ADMIN")
        hierarchy.append(" \n $ROLE_ADMIN > $ROLE_MANAGEMENT")
        hierarchy.append(" \n $ROLE_MANAGEMENT > $ROLE_HO_USER")
        hierarchy.append(" \n $ROLE_HO_USER > $ROLE_RGM")
        hierarchy.append(" \n $ROLE_RGM > $ROLE_CM")
        hierarchy.append(" \n $ROLE_CM > $ROLE_BM")
        hierarchy.append(" \n $ROLE_BM > $ROLE_RM")
        hierarchy.append(" \n $ROLE_RM > $ROLE_CSM")
        hierarchy.append(" \n $ROLE_CSM > $ROLE_CALL_CENTER_AGENT")

        val roleHierarchy = RoleHierarchyImpl()
        roleHierarchy.setHierarchy(hierarchy.toString())

        return roleHierarchy
    }
}

@Component
class PartnerAuthentication(
    @Autowired val partnerMasterRepository: PartnerMasterRepository,
    @Autowired val cryptoUtils: CryptoUtils
): OncePerRequestFilter() {

    private fun log(value : String) = LoggerUtils.log("PartnerAuthentication.$value")

    @Throws(Exception::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        if (!request.requestURI.contains("/rms/ep/")) {
            log("doFilterInternal - Path : ${request.requestURI}")
            filterChain.doFilter(request, response)
            return
        }

        val epAuthRequest = EPAuthRequest(request)
        log("doFilterInternal - OrgId : ${epAuthRequest.orgId}")
        log("doFilterInternal - IP Address : ${epAuthRequest.ipAddress}")
        log("doFilterInternal - Endpoint : ${epAuthRequest.requestUri}")

        if (!epAuthRequest.isRequestValid()) {
            log("doFilterInternal - Invalid request.")

            response.setFailureResponse(
                201,
                LocalResponse()
                    .setMessage("Invalid request.")
                    .setError(Errors.INVALID_REQUEST.value)
                    .setAction(Actions.FIX_RETRY.value)
            )

            return

        }

        val externalPartner = partnerMasterRepository.partnerRepository.findByOrgId(epAuthRequest.orgId)
        externalPartner ?: run {

            response.setFailureResponse(
                201,
                LocalResponse()
                    .setMessage("No partner found for orgId : ${epAuthRequest.orgId}")
                    .setError(Errors.RESOURCE_NOT_FOUND.value)
                    .setAction(Actions.CONTACT_ADMIN.value)
            )

            return
        }

        if (!externalPartner.isEnabled) {
            log("doFilterInternal - Partner is not enabled for orgId: ${epAuthRequest.orgId}")

            response.setFailureResponse(
                201,
                LocalResponse()
                    .setMessage("Your access is disabled. Please contact system admin.")
                    .setError(Errors.ACCESS_DENIED.value)
                    .setAction(Actions.CONTACT_ADMIN.value)
            )

            return

        }

        if (externalPartner.ipRestricted) {

            partnerMasterRepository.whitelistedIPRepository
                .findAllByOrgId(epAuthRequest.orgId)?.let { wi ->

                    if (wi.isNotEmpty()) {

                        val ipAddressed = ArrayList<String>()

                        for (ip in wi) {
                            ipAddressed.add(ip.ipAddress)
                        }

                        if (!ipAddressed.contains(epAuthRequest.ipAddress)) {
                            log("doFilterInternal - No ip whitelisted was found: ${epAuthRequest.orgId}")

                            response.setFailureResponse(
                                201,
                                LocalResponse()
                                    .setMessage("Your IP Address is blocked. Please contact system admin.")
                                    .setError(Errors.ACCESS_DENIED.value)
                                    .setAction(Actions.CONTACT_ADMIN.value)
                            )

                            return

                        }

                    } else {

                        log("doFilterInternal - No ip whitelisted was found: ${epAuthRequest.orgId}")

                        response.setFailureResponse(
                            201,
                            LocalResponse()
                                .setMessage("Your IP Address is blocked. Please contact system admin.")
                                .setError(Errors.ACCESS_DENIED.value)
                                .setAction(Actions.CONTACT_ADMIN.value)
                        )

                        return

                    }

                } ?: run {
                log("doFilterInternal - IP address is blocked.")

                response.setFailureResponse(
                    201,
                    LocalResponse()
                        .setMessage("Your IP Address is blocked. Please contact system admin.")
                        .setError(Errors.ACCESS_DENIED.value)
                        .setAction(Actions.CONTACT_ADMIN.value)
                )

                return
            }

        }

        val clientId = cryptoUtils.encryptAes(epAuthRequest.clientId)
        val clientSecret = cryptoUtils.encryptAes(epAuthRequest.clientSecret)

        when {

            clientId != externalPartner.clientId
                    || clientSecret != externalPartner.clientSecret -> {
                log("doFilterInternal - Incorrect client Id or Secret.")

                response.setFailureResponse(
                    201,
                    LocalResponse()
                        .setMessage("Incorrect client Id or Secret.")
                        .setError(Errors.INVALID_CREDENTIALS.value)
                        .setAction(Actions.FIX_RETRY.value)
                )

                return
            }

            epAuthRequest.requestUri.contains("/ep/v1/authenticate") -> {
                log("doFilterInternal - No session passcode required. Authenticated!")
                filterChain.doFilter(request, response)
//                request.getRequestDispatcher(request.servletPath).forward(request, response)
                return
            }

            externalPartner.sessionEnabled
                    && epAuthRequest.sessionPasscode.isInvalid() -> throw Exception("Invalid sessionPasscode.")

            else -> {

                if (epAuthRequest.sessionPasscode != externalPartner.sessionPasscode) {
                    log("doFilterInternal - Invalid sessionPasscode.")

                    response.setFailureResponse(
                        401,
                        LocalResponse()
                            .setMessage("Invalid sessionPasscode.")
                            .setError(Errors.UNAUTHORIZED_ACCESS.value)
                            .setAction(Actions.AUTHENTICATE_AGAIN.value)
                    )

                    return

                }

                if (!externalPartner.isSessionValid()) {
                    log("doFilterInternal - sessionPasscode expired.")

                    response.setFailureResponse(
                        401,
                        LocalResponse()
                            .setMessage("sessionPasscode expired. Please authenticate again.")
                            .setError(Errors.UNAUTHORIZED_ACCESS.value)
                            .setAction(Actions.AUTHENTICATE_AGAIN.value)
                    )

                    return

                }

                if (externalPartner.shouldIncreaseSessionValidity()) {

                    externalPartner.apply {
                        updateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                        sessionUpdateDatetime = DateTimeUtils.getCurrentDateTimeInIST()
                        sessionValidDatetime = DateTimeUtils.getDateTimeByAddingHours(1)
                    }

                    partnerMasterRepository.partnerRepository.save(externalPartner)

                }

                log("doFilterInternal - Partner and session authenticated.")
                //filterChain.doFilter(request, response)
                request.getRequestDispatcher(request.servletPath).forward(request, response)

            }
        }

    }

}

@Component
class RMProAuthentication(
    @Autowired val userRepositoryMaster: UserRepositoryMaster,
    @Autowired val appProperty: AppProperty
) : OncePerRequestFilter() {

    @Throws(Exception::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {


        val userAuthRequest = UserAuthRequest(request)

        if (request.requestURI.contains("/rms/ep/")) {
            LoggerUtils.log("RMProAuthentication.doFilterInternal - Path - /rms/ep/ - : ${request.requestURI}")
            request.getRequestDispatcher(request.servletPath).forward(request, response)
            return

        }

        if (request.requestURI.contains("/rms/cron/")) {

            LoggerUtils.log("RMProAuthentication.doFilterInternal - Path : ${request.requestURI}")

            if (userAuthRequest.crownPasscode != appProperty.crownPasscode) {

                LoggerUtils.log(
                    "RMProAuthentication.doFilterInternal - " +
                            "Crown passcode don't match for id : ${userAuthRequest.userId}"
                )

                response.setFailureResponse(
                    401,
                    LocalResponse()
                        .setMessage("Access authentication failed.")
                        .setError(Errors.UNAUTHORIZED_ACCESS.value)
                        .setAction(Actions.AUTHENTICATE_AGAIN.value)
                )
                return
            }

            printLog(
                "RMProAuthentication.doFilterInternal - Authenticated with crown passcode"
            )

            request.getRequestDispatcher(request.servletPath).forward(request, response)
            return
        }

        println("RMProAuthentication.doFilterInternal - UserId : ${userAuthRequest.userId}")
        println("RMProAuthentication.doFilterInternal - IP Address : ${userAuthRequest.ipAddress}")
        println("RMProAuthentication.doFilterInternal - Endpoint : ${userAuthRequest.requestUri}")

        val eUser = userRepositoryMaster.userRepository.findByUserId(userAuthRequest.userId)

        eUser ?: run {
            LoggerUtils.log("No user found for id : ${userAuthRequest.userId}")

            response.setFailureResponse(
                401,
                LocalResponse()
                    .setMessage("No user found for id : ${userAuthRequest.userId}")
                    .setError(Errors.UNAUTHORIZED_ACCESS.value)
                    .setAction(Actions.AUTHENTICATE_AGAIN.value)
            )
            return
        }

        if (!eUser.isActive) {

            LoggerUtils.log("RMProAuthentication.doFilterInternal - User is not active | id : ${eUser.id}")

            response.setFailureResponse(
                201,
                LocalResponse()
                    .setMessage("User is not active. Please contact system admin.")
                    .setError(Errors.ACCESS_DENIED.value)
                    .setAction(Actions.CONTACT_ADMIN.value)
            )
            return

        }

        if (userAuthRequest.sessionPasscode != eUser.sessionPasscode) {
            printLog(
                "RMProAuthentication.doFilterInternal - " +
                        "Session passcode don't match for id : ${userAuthRequest.userId}"
            )

            response.setFailureResponse(
                401,
                LocalResponse()
                    .setMessage("User authentication failed.")
                    .setError(Errors.UNAUTHORIZED_ACCESS.value)
                    .setAction(Actions.AUTHENTICATE_AGAIN.value)
            )
            return
        }

        printLog(
            "RMProAuthentication.doFilterInternal - " +
                    "user authenticated successfully | id : ${userAuthRequest.userId}"
        )

        printLog("RMProAuthentication.doFilterInternal - user authenticated successfully | id : ${userAuthRequest.userId}")
        request.getRequestDispatcher(request.servletPath).forward(request, response)

    }

}

private fun HttpServletResponse.setFailureResponse(
    statusCode: Int,
    localResponse: LocalResponse
) {
    this.apply {
        contentType = ContentType.APPLICATION_JSON.toString()
        status = statusCode
        outputStream.println(
            localResponse
                .toJson()
                .toString()
        )
    }

}



