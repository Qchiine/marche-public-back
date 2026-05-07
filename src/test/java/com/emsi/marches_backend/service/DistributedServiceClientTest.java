package com.emsi.marches_backend.service;

import com.emsi.marches_backend.exception.ServiceCommunicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributedServiceClientTest {

    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private DistributedServiceClient distributedServiceClient;

    @Test
    void callRemoteService_shouldReturnResponse() {
        String serviceUrl = "http://localhost:8080/api/test";
        String requestBody = "{\"data\":\"test\"}";
        Map<String, String> expectedResponse = Map.of("status", "ok");

        when(restTemplate.postForEntity(eq(serviceUrl), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        Map<String, String> response = distributedServiceClient.callRemoteService(serviceUrl, requestBody, Map.class);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void callRemoteService_shouldThrowExceptionOnError() {
        String serviceUrl = "http://localhost:8080/api/test";

        when(restTemplate.postForEntity(eq(serviceUrl), any(), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> distributedServiceClient.callRemoteService(serviceUrl, "{}", Map.class))
                .isInstanceOf(ServiceCommunicationException.class);
    }

    @Test
    void getFromRemoteService_shouldReturnData() {
        String serviceUrl = "http://localhost:8080/api/data";
        Map<String, String> expectedResponse = Map.of("data", "value");

        when(restTemplate.getForEntity(serviceUrl, Map.class))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        Map<String, String> response = distributedServiceClient.getFromRemoteService(serviceUrl, Map.class);

        assertThat(response).isEqualTo(expectedResponse);
    }
}
