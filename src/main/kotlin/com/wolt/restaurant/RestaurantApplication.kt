package com.wolt.restaurant

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class RestaurantApplication

fun main(args: Array<String>)
{
    SpringApplication.run(RestaurantApplication::class.java, *args)
}
