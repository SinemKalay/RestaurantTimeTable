package com.wolt.restaurant

import com.wolt.restaurant.exception.InaccurateTimingException
import com.wolt.restaurant.exception.NoSuchDayException
import com.wolt.restaurant.exception.NoSuchTypeException
import com.wolt.restaurant.exception.TimeValueNotFoundException
import com.wolt.restaurant.exception.TypeNotFoundException
import com.wolt.restaurant.exception.UnmatchedOpenCloseTimeException
import com.wolt.restaurant.util.ErrorResponse
import com.wolt.restaurant.util.TestConstants
import com.wolt.restaurant.util.TestUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
    @DisplayName("Should return timetable for whole week")
    fun `successful when whole week info received`() {
        sendPostRequest("wholeWeekInfoRequest.json", "successResponseWholeWeekInfo")
    }

    @Test
    @DisplayName("Should return timetable for days in request")
    fun `successful when partial part of week received`() {
        sendPostRequest("partialWeekRequest.json", "successResponsePartialWeek")
    }

	@Test
	@DisplayName("Should return timetable with sorted intervals")
	fun `successful when intervals are out of order`() {
		sendPostRequest("intervalsOutOfOrderRequest.json", "successResponseIntervalsOutOfOrder")
	}

	@Test
	@DisplayName("Should return timetable with sorted days")
	fun `successful when days are out of order`() {
		sendPostRequest("dayNotInOrderRequest.json", "successResponseDayNotInOrder")
	}

	@Test
	@DisplayName("Should return timetable with sorted intervals")
	fun `successful when close comes before opening time`() {
		sendPostRequest("closeComesBeforeOpenRequest.json", "successResponseCloseComesFirst")
	}

    @Test
    @DisplayName("Should return timetable for week with sunday overtime")
    fun `successful when week with Sunday overtime info`() {
        sendPostRequest("weekInfoWithSundayOvertime.json", "successResponseSundayOvertime")
    }

	@Test
	@DisplayName("Should return timetable for closed week")
	fun `successful when whole week closed`() {
		sendPostRequest("closeAllWeekRequest.json", "successResponseCloseAllWeek")
	}

	@Test
	@DisplayName("Should return timetable for open for a second")
	fun `successful when open for a second`() {
		sendPostRequest("openJustForASecond.json", "successResponseOpenForASecond")
	}

	@Test
	@DisplayName("Should return timetable for open for a minute")
	fun `successful when open for a minute`() {
		sendPostRequest("openJustForAMinute.json", "successResponseOpenForAMinute")
	}

	@Test
	@DisplayName("Should return timetable for open for 59 minutes")
	fun `successful when open for 59 minutes`() {
		sendPostRequest("openFor59Minutes.json", "successResponseOpenFor59Minutes")
	}

	@ParameterizedTest
	@ValueSource(strings = ["overflowTimeValueWith1SecondRequest.json",
		"negativeTimeValueRequest.json", "exceedMaxValueFieldRequest.json" ])
	@DisplayName("Should return error about time value constraints")
	fun `fail when value out of time constraints`(jsonFileName: String) {
		val exception = InaccurateTimingException(TestConstants.EXP_MSG_INACCURATE_TIMING,
			TestConstants.EXP_MSG_TIMING_CONSTRAINTS)
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest(jsonFileName, errorResponse)
	}

	@Test
	@DisplayName("Should return error about non-existing type field")
	fun `fail when non-exist type field received`() {
		val exception = TypeNotFoundException(TestConstants.EXP_MSG_TYPE_NOT_FOUND,
			"Type field is required")
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest("nonExistTypeFieldRequest.json", errorResponse)
	}

	@Test
	@DisplayName("Should return error about non-existing value field")
	fun `fail when non-exist value field received`() {
		val exception = TimeValueNotFoundException(TestConstants.EXP_MSG_VALUE_NOT_FOUND,
			"Value field is required")
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest("nonExistValueFieldRequest.json", errorResponse)
	}

	@Test
	@DisplayName("Should return error about no such type")
	fun `fail when wrong type value received`() {
		val exception = NoSuchTypeException(TestConstants.EXP_MSG_NO_SUCH_TYPE,
			"Type names must be one of the followings: [open, close]")
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest("noSuchTypeRequest.json", errorResponse)
	}

	@Test
	@DisplayName("Should return error about no such day")
	fun `fail when wrong day value received`() {
		val exception = NoSuchDayException(TestConstants.EXP_MSG_NO_SUCH_DAY,
			"Day names must be one of the followings: [monday, tuesday, wednesday, thursday," +
				" friday, saturday, sunday]")
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest("noSuchDayRequest.json", errorResponse)
	}

	@Test
	@DisplayName("Should return error about unexpected closing time")
	fun `fail when unexpected closing time received`() {
		val exception = UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_CLOSING,
			"Unexpected closing time on saturday")
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest("unexpectedClosingTimeRequest.json", errorResponse)
	}

    @Test
    @DisplayName("Should return error about unexpected opening time")
    fun `fail when unexpected opening time received`() {
        val exception = UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_OPENING,
            "Unexpected opening time on saturday")
        val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
            "Malformed JSON request", exception.message.toString())
        sendPostRequest("unexpectedOpeningTimeRequest.json", errorResponse)
    }

	@Test
	@DisplayName("Should return error about days must be sequential when overnight time exists")
	fun `fail when non-sequential day info with overnight time`() {
		val exception = UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_NON_SEQUENTIAL,
			"Opening-Closing times must be on same or sequential day")
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest("daysNotSequentialRequest.json", errorResponse)
	}

	@Test
	@DisplayName("Should return error about open time haven't been closed exists")
	fun `fail when unclosed day exists`() {
		val exception = UnmatchedOpenCloseTimeException(TestConstants.EXP_MSG_UNEXP_OPENING,
			"Unclosed day exists saturday")
		val errorResponse = ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST,
			"Malformed JSON request", exception.message.toString())
		sendPostRequest("unclosedDayRequest.json", errorResponse)
	}

	private fun sendPostRequest(jsonReqFileName: String, jsonResFileName: String): MvcResult? {
        return mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.POST_URI).accept(MediaType.parseMediaType(TestConstants.MEDIA_TYPE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil().loadJson(jsonReqFileName)))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(content().string(TestUtil().loadText(jsonResFileName)))
            .andReturn()
    }

    private fun sendPostRequest(jsonFileName: String, errorResponse: ErrorResponse): MvcResult? {
        return mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.POST_URI).accept(MediaType.parseMediaType(TestConstants.MEDIA_TYPE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil().loadJson(jsonFileName)))
            .andExpect(jsonPath(TestConstants.ERROR_STATUS).value(errorResponse.status.name))
            .andExpect(jsonPath(TestConstants.ERROR_NAME).value(errorResponse.error))
            .andExpect(jsonPath(TestConstants.ERROR_MSG).value(errorResponse.message))
            .andReturn()
    }
}