package kr.hhplus.be.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
	runApplication<ServerApplication>(*args)
}
