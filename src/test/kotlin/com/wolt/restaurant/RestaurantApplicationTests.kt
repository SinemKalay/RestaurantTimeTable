package com.wolt.restaurant

import com.wolt.restaurant.exception.InaccurateTimingException
import com.wolt.restaurant.exception.UnmatchedOpenCloseTimeException
import com.wolt.restaurant.util.ErrorResponse
import com.wolt.restaurant.util.ResponseConstants
import com.wolt.restaurant.util.TestConstants
import com.wolt.restaurant.util.TestUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@AutoConfigureMockMvc
@SpringBootTest
class RestaurantApplicationTests {

	@Autowired
	lateinit var webApplicationContext: WebApplicationContext
	lateinit var mockMvc: MockMvc
	lateinit var testUtil: TestUtil

	@BeforeEach
	fun beforeEach() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
	}

	@Test
	@DisplayName("Should return readable data for days in request")
	fun successfulWithWholeWeekInfo() {
		sendPostRequest(TestConstants.JSON_FILE_PATH_WHOLE_WEEK,
			TestConstants.RESP_FILE_PATH_WHOLE_WEEK)
	}

	@Test
	@DisplayName("Should return readable data for whole week")
	fun successfulWithPartOfWeekInfo() {
		sendPostRequest(TestConstants.JSON_FILE_PATH_PARTLY_WEEK,
			TestConstants.RESP_FILE_PATH_PARTLY_WEEK)
	}

	@Test
	@DisplayName("Should return error about relational days not sequential")
	fun failWithNonSequentialDayInfoWhenOvernightWorkingTime() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_NON_SEQUENTIAL)
		val res= ErrorResponse(ResponseConstants.CORRUPTED_DATA.value, exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NON_SEQUENTIAL, res)
	}

	@Test
	@DisplayName("Should return error about opening - closing times are not compatible")
	fun failWithInaccurateTiming() {
		val exception= InaccurateTimingException(TestConstants.EXP_MSG_INACCURATE_TIMING)
		val res= ErrorResponse(ResponseConstants.INCOMP_DATA.value, exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_INACCURATE_TIMING, res)
	}

	@Test
	@DisplayName("Should return error about unexpected opening time")
	fun failWithUnexpectedOpeningTimeReceived() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_OPENING)
		val res= ErrorResponse(ResponseConstants.CORRUPTED_DATA.value, exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_UNEXP_OPENING, res)
	}

	@Test
	@DisplayName("Should return error about unexpected closing time")
	fun failWithUnexpectedClosingTimeReceived() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_CLOSING)
		val res= ErrorResponse(ResponseConstants.CORRUPTED_DATA.value, exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_UNEXP_CLOSING, res)
	}

	internal fun sendPostRequest(jsonReqFileName: String, jsonResFileName: String): MvcResult? {
		return mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.POST_URI).
		accept(MediaType.parseMediaType(TestConstants.MEDIA_TYPE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(testUtil.loadJson(jsonReqFileName)))
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andExpect(content().string(testUtil.loadText(jsonResFileName)))
			.andReturn()
	}

	internal fun sendPostRequest(jsonFileName: String, errorResponse: ErrorResponse): MvcResult? {
		return mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.POST_URI).
		accept(MediaType.parseMediaType(TestConstants.MEDIA_TYPE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(testUtil.loadJson(jsonFileName)))
			.andExpect(jsonPath(TestConstants.ERROR_CODE).value(errorResponse.errorCode))
			.andExpect(jsonPath(TestConstants.ERROR_MSG).value(errorResponse.errorMessage))
			.andReturn()
	}
}
