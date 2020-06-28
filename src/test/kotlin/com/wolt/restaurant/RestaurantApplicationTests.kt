package com.wolt.restaurant

import com.wolt.restaurant.exceptions.InaccurateTimingException
import com.wolt.restaurant.utils.ErrorResponse
import com.wolt.restaurant.utils.ResponseConstants
import com.wolt.restaurant.exceptions.UnmatchedOpenCloseTimeException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@AutoConfigureMockMvc
@SpringBootTest
class RestaurantApplicationTests {

	@Autowired
	lateinit var webApplicationContext: WebApplicationContext
	lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var resourceLoader: ResourceLoader

	@BeforeEach
	fun beforeEach() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
	}

	@Test
	@DisplayName("Should return readable data for days in request")
	fun successfulWithWholeWeekInfo() {
		sendPostRequest(TestsConstants.JSON_FILE_PATH_WHOLE_WEEK,
			TestsConstants.RESP_FILE_PATH_WHOLE_WEEK)
	}

	@Test
	@DisplayName("Should return readable data for whole week")
	fun successfulWithPartOfWeekInfo() {
		sendPostRequest(TestsConstants.JSON_FILE_PATH_PARTLY_WEEK,
			TestsConstants.RESP_FILE_PATH_PARTLY_WEEK)
	}

	@Test
	@DisplayName("Should return error about relational days not sequential")
	fun failWithNonSequentialDayInfoWhenOvernightWorkingTime() {
		val exception= UnmatchedOpenCloseTimeException(TestsConstants.EXP_MSG_NON_SEQUENTIAL)
		val res= ErrorResponse(ResponseConstants.CORRUPTED_DATA.value, exception.message.toString())
		sendPostRequest(TestsConstants.JSON_FILE_PATH_NON_SEQUENTIAL, res)
	}

	@Test
	@DisplayName("Should return error about opening - closing times are not compatible")
	fun failWithInaccurateTiming() {
		val exception= InaccurateTimingException(TestsConstants.EXP_MSG_INACCURATE_TIMING)
		val res= ErrorResponse(ResponseConstants.INCOMP_DATA.value, exception.message.toString())
		sendPostRequest(TestsConstants.JSON_FILE_PATH_INACCURATE_TIMING, res)
	}

	@Test
	@DisplayName("Should return error about unexpected opening time")
	fun failWithUnexpectedOpeningTimeReceived() {
		val exception= UnmatchedOpenCloseTimeException(TestsConstants.EXP_MSG_UNEXP_OPENING)
		val res= ErrorResponse(ResponseConstants.CORRUPTED_DATA.value, exception.message.toString())
		sendPostRequest(TestsConstants.JSON_FILE_PATH_UNEXP_OPENING, res)
	}

	@Test
	@DisplayName("Should return error about unexpected closing time")
	fun failWithUnexpectedClosingTimeReceived() {
		val exception= UnmatchedOpenCloseTimeException(TestsConstants.EXP_MSG_UNEXP_CLOSING)
		val res= ErrorResponse(ResponseConstants.CORRUPTED_DATA.value, exception.message.toString())
		sendPostRequest(TestsConstants.JSON_FILE_PATH_UNEXP_CLOSING, res)
	}

	private fun sendPostRequest(jsonReqFileName: String, jsonResFileName: String): MvcResult? {
		return mockMvc.perform(MockMvcRequestBuilders.post(TestsConstants.POST_URI).
				accept(MediaType.parseMediaType(TestsConstants.MEDIA_TYPE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(loadJson(jsonReqFileName)))
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andExpect(content().string(loadText(jsonResFileName)))
			.andReturn()
	}

	private fun sendPostRequest(jsonFileName: String, errorResponse: ErrorResponse): MvcResult? {
		return mockMvc.perform(MockMvcRequestBuilders.post(TestsConstants.POST_URI).
		accept(MediaType.parseMediaType(TestsConstants.MEDIA_TYPE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(loadJson(jsonFileName)))
			.andExpect(jsonPath(TestsConstants.ERROR_CODE).value(errorResponse.errorCode))
			.andExpect(jsonPath(TestsConstants.ERROR_MSG).value(errorResponse.errorMessage))
			.andReturn()
}

	fun loadJson(fileName: String): String {
		return resourceLoader.getResource(TestsConstants.JSON_REQUESTS_PATH+fileName).file
			.readText(charset = Charsets.UTF_8)
	}

	fun loadText(fileName: String): String {
		return resourceLoader.getResource(TestsConstants.JSON_RESPONSES_PATH+fileName).file
			.readText(charset = Charsets.UTF_8)
	}
}
