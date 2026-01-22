package com.moustass.transactions_service;

import com.moustass.transactions_service.client.minio.MinIOService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TransactionsServiceApplicationTests {

	@MockitoBean
	private MinIOService minIOService;

	@Test
	void contextLoads() {
	}

}
