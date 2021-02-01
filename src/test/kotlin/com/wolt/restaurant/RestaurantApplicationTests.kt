package com.wolt.restaurant

import com.wolt.restaurant.exception.UnmatchedOpenCloseTimeException
import com.wolt.restaurant.util.ErrorResponse
import com.wolt.restaurant.util.TestConstants
import com.wolt.restaurant.util.TestUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime

@AutoConfigureMockMvc
@SpringBootTest
class RestaurantApplicationTests {

	@Autowired
	lateinit var webApplicationContext: WebApplicationContext
	lateinit var mockMvc: MockMvc

	@BeforeEach
	fun beforeEach() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
	}

	@Test
	@DisplayName("Should return readable data for days in request")
	fun `successful when partial part of week received` () {
		sendPostRequest(TestConstants.JSON_FILE_PATH_PARTLY_WEEK,
			TestConstants.RESP_FILE_PATH_PARTLY_WEEK)
	}

	@Test
	@DisplayName("Should return readable data for whole week")
	fun `successful when whole week info received`() {
		sendPostRequest(TestConstants.JSON_FILE_PATH_WHOLE_WEEK,
			TestConstants.RESP_FILE_PATH_WHOLE_WEEK)
	}

	@Test
	@DisplayName("Should return readable data for week with sunday overtime")
	fun `successful when week with Sunday overtime info`() {
		sendPostRequest(TestConstants.JSON_FILE_PATH_SUNDAY_OVERTIME,
			TestConstants.RESP_FILE_PATH_SUNDAY_OVERTIME)
	}

	@Test
	@DisplayName("Should return error about relational days not on sequential order")
	fun  `fail when non-sequential day info with overnight time` () {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_NON_SEQUENTIAL,
			"Opening-Closing times must be on same or sequential day")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NON_SEQUENTIAL, errorResponse)
	}

	@Test
	@DisplayName("Should return error about value")
	fun `fail when negative value for time field received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_INACCURATE_TIMING,
			"Value field should be exist and in between 0 and 86400.")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NEGATIVE_VALUE, errorResponse)
	}

	@Test
	@DisplayName("Should return error about non-existing field")
	fun `fail when non-exist value field received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_VALUE_NOT_FOUND,
			"Value field is required")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NON_EXIST_VALUE, errorResponse)
	}

	@Test
	@DisplayName("Should return error about non-existing type field")
	fun `fail when non-exist type field received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_TYPE_NOT_FOUND,
			"Type field is required")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NON_EXIST_TYPE, errorResponse)
	}

	@Test
	@DisplayName("Should return error about unexpected opening time")
	fun  `fail when unexpected opening time received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_OPENING,
			"Unexpected opening time on saturday")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_UNEXP_OPENING, errorResponse)
	}

	@Test
	@DisplayName("Should return error about unexpected closing time")
	fun `fail when unexpected closing time received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_CLOSING,
			"Unexpected closing time on saturday")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_UNEXP_CLOSING, errorResponse)
	}

	@Test
	@DisplayName("Should return error about time value limitation")
	fun `fail when time value exceeds max limit`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_INACCURATE_TIMING,
			"Value field should be exist and in between 0 and 86400.")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_MAX_VALUE, errorResponse)
	}

	@Test
	@DisplayName("Should return error about no such day")
	fun `fail when wrong day value received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_NO_SUCH_DAY,
			"Day names must be one of the followings: [monday, tuesday, wednesday, thursday, friday, saturday, sunday]")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NO_SUCH_DAY, errorResponse)
	}

	@Test
	@DisplayName("Should return error about no such type")
	fun `fail when wrong type value received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_NO_SUCH_TYPE,
			"Type names must be one of the followings: [open, close]")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NO_SUCH_TYPE, errorResponse)
	}

	private fun sendPostRequest(jsonReqFileName: String, jsonResFileName: String): MvcResult? {
		return mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.POST_URI).
		accept(MediaType.parseMediaType(TestConstants.MEDIA_TYPE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(TestUtil().loadJson(jsonReqFileName)))
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andExpect(content().string(TestUtil().loadText(jsonResFileName)))
			.andReturn()
	}

	private fun sendPostRequest(jsonFileName: String, errorResponse: ErrorResponse): MvcResult? {
		return mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.POST_URI).
		accept(MediaType.parseMediaType(TestConstants.MEDIA_TYPE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(TestUtil().loadJson(jsonFileName)))
			.andExpect(jsonPath(TestConstants.ERROR_STATUS).value(errorResponse.status.name))
			.andExpect(jsonPath(TestConstants.ERROR_NAME).value(errorResponse.error))
			.andExpect(jsonPath(TestConstants.ERROR_MSG).value(errorResponse.message))
			.andReturn()
	}
}