package noninoni.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import noninoni.entity.OrderInfo;
import noninoni.entity.OrderStatus;
import noninoni.repository.OrderInfoRepository;

@Slf4j
@Service
public class TrackingService {
	

	@Autowired
	private OrderInfoRepository orderInfoRepository;

	public void checkAndUpdateDeliveryStatus(List<OrderInfo> orderInfos, String AccessToken) {
		for (OrderInfo orderInfo : orderInfos) {
			if (orderInfo.getOrderStatus().getDescription().equals("배송중")||orderInfo.getOrderStatus().getDescription().equals("교환")) {
				String trackNumber = orderInfo.getTrackNumber();
				// 외부 API를 호출하여 실제 배송 상태를 확인하는 로직 구현
				if (trackNumber != null && !trackNumber.trim().isEmpty()) {
					String apiStatus = fetchStatusFromExternalAPI(trackNumber, AccessToken);
					// API로부터 받은 상태를 OrderStatus enum으로 변환
					if (!apiStatus.equals("ERROR") && apiStatus.equals("배송완료")) {
						if(orderInfo.getOrderStatus().getDescription().equals("배송중")) {
						OrderStatus newStatus = OrderStatus.valueOf("DELIVERED");
						orderInfo.setOrderStatus(newStatus);
						}
						else {
							OrderStatus newStatus = OrderStatus.valueOf("EXCHANGED_DELIVERED");
							orderInfo.setOrderStatus(newStatus);
						}
						
						orderInfoRepository.save(orderInfo);
					}
				}
			}

		}
	}

	@Autowired
	private RestTemplate restTemplate;


	private String fetchStatusFromExternalAPI(String trackNumber, String AccessToken) {
		try {
			if (AccessToken == null) {
				throw new IllegalStateException("Failed to fetch access token");
			}

			String url = "https://apis.tracker.delivery/graphql";

			// GraphQL 쿼리와 변수를 준비
			String query = "query Track($carrierId: ID!, $trackingNumber: String!) {"
					+ "track(carrierId: $carrierId, trackingNumber: $trackingNumber) {" + "lastEvent {"
					+ "status { name }" + "}" + "}" + "}";
			Map<String, Object> variables = Map.of("carrierId", "kr.cjlogistics", "trackingNumber", trackNumber);
			Map<String, Object> requestMap = Map.of("query", query, "variables", variables);

			// 헤더에 액세스 토큰을 추가
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(AccessToken);

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);

			// API 요청 및 응답 처리
			ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

			// JSON 응답 파싱
			JSONObject jsonObject = new JSONObject(response.getBody());
			String status = jsonObject.getJSONObject("data").getJSONObject("track").getJSONObject("lastEvent")
					.getJSONObject("status").getString("name");

			return status;
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR"; // 오류 발생 시
		}
	}

}