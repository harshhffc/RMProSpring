package com.homefirstindia.rmproserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class RmProServerSpringApplication

fun main(args: Array<String>) {
	runApplication<RmProServerSpringApplication>(*args)
}
