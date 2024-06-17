package com.homefirstindia.rmproserver.utils

import java.util.logging.Logger

object LoggerUtils {

    private val logger: Logger = Logger.getLogger(LoggerUtils::class.java.simpleName)

    fun log(value: String) {
        logger.info("\n\nRMProS - Value --> $value\n\n")
    }

    fun logBody(body: String) {
        logger.info("\n\nRMProS - Received body --> $body\n\n")
    }

    fun logMethodCall(value: String) {
        logger.info("\nRMProS -\n----------------------\n  Method --> $value  \n----------------------\n\n")
    }

    fun printLog(value: String) {
        println("\n\nRMProS - Value --> $value\n\n")
    }

}