package searcher;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Validated
@RestController
@RequestMapping(value = "/search")
public class SearchController {

	private static final String API_URL = "http://api.stackexchange.com/2.2/search?" +
			"order=%s&sort=%s&intitle=%s&filter=%s&site=%s&page=%d&pagesize=%d";
	private static final String TOO_MANY_REQUESTS = "Too many requests, more requests will be available at ";

	/**
	* Filter to select only the following items fields: owner.display_name, is_answered, creation_date, link, title.
	 * To obtain all default fields use "default" value.
	* */
	private static final String FILTER = "!*0Opwc*9gmyki6sWc3.n6XWcS3jc8uKHk4xVEM3yr";
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final CloseableHttpClient client = HttpClientBuilder.create().build();
	private LocalDateTime throttlingExpired;
	private volatile boolean throttled;

	/**
	 * @param order The one of the following options: desc or asc (descending and ascending correspondingly).
	 * @param sort Response will be sorted by one of the following cafeterias:  activity, votes, creation, relevance
	 * @param title Title to search a question. Must be provided to search.
	 * @param filter By default the filter is set to select only the following items fields: owner.display_name,
	 *                  is_answered, creation_date, link, title. To obtain all default fields use "default" value.
	 * @param site Site where selected title will be searched. Must be specified one of stackexchange sites; by
	 *                default it searches in stackoverflow.com.
	 * @param page Select page; default value is 1.
	 * @param pageSize Select page size, must be in range from 1 to 100. Default value is 30.
	 * @return ResponseEntity
	 */
	@GetMapping
	public ResponseEntity search(@RequestParam(value = "order", defaultValue = "desc") String order,
	                             @RequestParam(value = "sort", defaultValue = "activity") String sort,
	                             @RequestParam(value = "title") String title,
	                             @RequestParam(value = "filter", defaultValue = FILTER) String filter,
	                             @RequestParam(value = "site", defaultValue = "stackoverflow") String site,
	                             @RequestParam(value = "page", defaultValue = "1") Integer page,
	                             @RequestParam(value = "pagesize", defaultValue = "30") @Min(1) @Max(100) Integer pageSize) {

		if (throttled) {
			if (!LocalDateTime.now().isAfter(throttlingExpired)) {
				return getThrottlingResponse();
			} else {
				throttled = false;
			}
		}

		String reqURL;
		try {
			reqURL = String.format(API_URL, order, sort, URLEncoder.encode(title, "UTF-8"), filter, site, page,
					pageSize);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding", e);
			return ResponseEntity.badRequest().build();
		}

		return executeRequest(reqURL);
	}

	private ResponseEntity executeRequest(String reqURL) {
		try (CloseableHttpResponse response = client.execute(new HttpGet(reqURL))) {
			int statusCode = response.getStatusLine().getStatusCode();
			LOGGER.debug("Response code: " + statusCode);

			String result = EntityUtils.toString(response.getEntity(), "UTF-8");

			if (statusCode != 200 && result.contains("throttle_violation")) {
				return handleThrottling(result);
			}
			return ResponseEntity.ok().body(result);
		} catch (IOException e) {
			LOGGER.error("Failed to execute request", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private ResponseEntity handleThrottling(String result) {
		throttled = true;
		int secondsIndex = result.indexOf("seconds");
		String secondsVal = result.subSequence(92, secondsIndex - 1).toString();
		int seconds = Integer.valueOf(secondsVal);
		throttlingExpired = LocalDateTime.now().plusSeconds(seconds);
		return getThrottlingResponse();
	}

	private ResponseEntity getThrottlingResponse() {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.contentType(MediaType.TEXT_PLAIN)
				.body(TOO_MANY_REQUESTS + throttlingExpired.format(formatter));
	}
}
