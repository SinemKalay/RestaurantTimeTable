package com.wolt.restaurant.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class TestUtil {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    fun loadJson(fileName: String): String {
        resourceLoader= DefaultResourceLoader()
        return resourceLoader.getResource(TestConstants.JSON_REQUESTS_PATH+fileName).file
            .readText(charset = Charsets.UTF_8)
    }

    fun loadText(fileName: String): String {
        resourceLoader= DefaultResourceLoader()
        return resourceLoader.getResource(TestConstants.JSON_RESPONSES_PATH+fileName).file
            .readText(charset = Charsets.UTF_8)
    }
}