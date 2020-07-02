package com.wolt.restaurant

import com.wolt.restaurant.exception.InaccurateTimingException
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
	fun successfulWithWholeWeekInfo() {
		sendPostRequest(TestConstants.JSON_FILE_PATH_WHOLE_WEEK,
			TestConstants.RESP_FILE_PATH_WHOLE_WEEK)
	}

	@Test
	@DisplayName("Should return readable data for whole week")
	fun `successful With Whole Week Info Received`() {
		sendPostRequest(TestConstants.JSON_FILE_PATH_PARTLY_WEEK,
			TestConstants.RESP_FILE_PATH_PARTLY_WEEK)
	}

	@Test
	@DisplayName("Should return error about relational days not sequential")
	fun  `fail With Non-Sequential Day Info When Overnight Working Time` () {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_NON_SEQUENTIAL,
			"Opening-Closing times must be on same or sequential day")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NON_SEQUENTIAL, errorResponse)
	}

	@Test
	@DisplayName("Should return error about opening - closing times are not compatible")
	fun  `fail With InaccurateTiming`() {
		val exception= InaccurateTimingException(TestConstants.EXP_MSG_INACCURATE_TIMING,
			"Opening time can not be later than closing time")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_INACCURATE_TIMING, errorResponse)
	}

	@Test
	@DisplayName("Should return error about unexpected opening time")
	fun  `fail With Unexpected Opening Time Received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_OPENING,
			"Opening time information was not expected")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_UNEXP_OPENING, errorResponse)
	}

	@Test
	@DisplayName("Should return error about unexpected closing time")
	fun `fail With Unexpected Closing Time Received`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_CLOSING,
			"Closing time information was not expected")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_UNEXP_CLOSING, errorResponse)
	}

	@Test
	@DisplayName("Should return error about waiting for closing time")
	fun `fail With Waiting For Closing`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_WAIT_CLOSING,
			"Closing time was waited for opening time at the end")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_WAIT_CLOSING, errorResponse)
	}

	@Test
	@DisplayName("Should return error about value")
	fun `fail With Negative Value Field`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_INACCURATE_TIMING,
			"Value field should be exist and in between 0 and 86400.")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NEGATIVE_VALUE, errorResponse)
	}

	@Test
	@DisplayName("Should return error about non-existing  field")
	fun `fail With Non-Exist Value Field`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_VALUE_NOT_FOUND,
			"Value field is required")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NON_EXIST_VALUE, errorResponse)
	}

	@Test
	@DisplayName("Should return error about non-existing type field")
	fun `fail With Non-Exist Type Field`() {
		val exception= UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_TYPE_NOT_FOUND,
			"Type field is required")
		val errorResponse= ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(TestConstants.JSON_FILE_PATH_NON_EXIST_TYPE, errorResponse)
	}

	internal fun sendPostRequest(jsonReqFileName: String, jsonResFileName: String): MvcResult? {
		return mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.POST_URI).
		accept(MediaType.parseMediaType(TestConstants.MEDIA_TYPE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(TestUtil().loadJson(jsonReqFileName)))
			.andExpect(MockMvcResultMatchers.status().isOk)
			.andExpect(content().string(TestUtil().loadText(jsonResFileName)))
			.andReturn()
	}

	internal fun sendPostRequest(jsonFileName: String, errorResponse: ErrorResponse): MvcResult? {
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
