# Restaurant Time Table API
This is a simple API which has one endpoint. It takes JSON-formatted opening hours of a restaurant as an input
and outputs hours in more human readable format.

## Requirements
In order to run this application on your environment, following technologies are required. 

Spring Boot 2.3.1 + JDK 1.8 + Gradle + Kotlin

Preferred IDE to run application: Intellij Idea

### How to run the application

Restaurant application can be run via terminal or IDE:

* To run the the application from terminal, execute below command in project root.

> ./gradlew bootRun

* Or, the application can be imported to the IDE to run (prefereably Intellij Idea).After importing, first build and then run the application via the button on top right corner.

### How to call the API

Postman can be used to call rest api after run the application.
1. Open Postman application
2. Select "POST" as  request method
3. Enter URL as "http://localhost:8080/convertTimesToReadableFormat"
4. Choose one of request body sample from resources/static/requestJsonFile

NOTE: Wolt.postman_collection.json in the project root can be also imported
easily.

### How to run the API's unit tests
---TODO---


## Explore Rest API
As it has been mentioned above, our Rest API has only one endpoint.

### HTTP Request
The endpoint is accessible via the following url:

> POST http://localhost:8080/convertTimesToReadableFormat

### Request body

The body of request should incluse JSON payload.
Example json request body samples can be found under resources/static/requestJsonFile/
You can test them using postman manually or you can check out unit tests which uses
those request body files. 

### HTTP Response
In successful scenario API will return easily readable restaurant time
table as in requested format with success status code

In case an exception occured, regarding error response will be redirected.
Each error response has fields shown below:

* timestamp: when the exception occured
* status: HTTP status of the exception
* error: main exception description
* message: detailed message regarding to the exception

### HTTP Response Codes and Exceptions
All returned http responses have been handled via exception handler
mechanism. To response back with user friendly way, many custom
exceptions&handlers have been created and some of existed well-known
exceptions' handlers have been overridden as well.

You can find some of custom exceptions below;
* NoSuchDayException: 
* NoSuchTypeException
* InaccurateTimingException
* UnmatchedOpenCloseTimeException
* TypeNotFoundException
* TimeValueNotFoundException
* RespBodyNotFoundException

As well as the custome exceptions above has been handled, some of
well-known exceptions' handlers have been overridden to give more
precise messages;
* NoHandlerFoundException
* HttpMessageNotReadableException
* HttpMediaTypeNotSupportedException
* HttpRequestMethodNotSupportedException

Any other exceptions are being handled by default handler.

## Repository Information

Repository can be found in the following repository link:
> https://github.com/SinemKalay/RestaurantTimeTable

## Thoughts about data format
The current data format makes a bit harder to analyze the data. The main point that turns into bottleneck;
* Opening-closing time information is splitted into 2 different json object. Which lead to put more checks to cover several corner cases.
* Json object namings could be clearer. In my opinion, "type" and "value" is not that much understandable.
* Main structure of Json response body could be more organized to make parsing it to an object easier.
* Since day is using as json object name, I was mapping the incoming json to HashMap<WeekdayEnum, List<TypeValueDTO>> at the first place.
But that caused, overlapping, when there is same day name since HashMap doesn't allow same key. Afterwards I converted the controller to
a format which get the json as string and then parsing to the object via Gson. So that way, overlapping day issue has been solved.

I would create the data format something like below:

{
  [
	{
		"day": "Monday",
		"period_list": [
			{
				"opening_time": 1000,
				"closing_time": 2000
			},
			{
				"opening_time": 1000,
				"closing_time": 2000
			}
		]
	},
	{
		"day": "Tuesday",
		"period_list": [
			{
				"opening_time": 1000,
				"closing_time": 2000
			},
			{
				"opening_time": 1000,
				"closing_time": 2000
			}
		]
	}
  ]
}

That kind of structure would have below advantages:
* It makes easier to understand data
* Mapping json to obj would be possible at controller phase
* It eliminates the possiblity of unmatched open-close exceptions:
  	no need to searching for open and closing time in different object, 
	no need to checking whether open-close times is in correct sequence

NOTE: Constraint of that structure in case of opening time - closing time
are not on the same day, time value of closing time would be limited by max
value is 86399(a day)+ 43200(half day) 


## References
* https://kotlinlang.org/
* Kotlin Programming By Example: Build real-world Android and web applications the Kotlin way
  Iyanu Adelekan
* https://www.scalyr.com/blog/getting-started-quickly-kotlin-logging/
* Data validation issue trackers :https://youtrack.jetbrains.com/issue/KT-26605
* https://phauer.com/2018/best-practices-unit-testing-kotlin/