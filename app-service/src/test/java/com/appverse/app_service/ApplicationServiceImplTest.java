package com.appverse.app_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.springframework.security.access.AccessDeniedException;

import com.appverse.app_service.client.SubscriptionServiceClient;
import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.MessageResponse;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.enums.MonetizationType;
import com.appverse.app_service.exception.BadRequestException;
import com.appverse.app_service.exception.ResourceNotFoundException;
import com.appverse.app_service.kafkaEvents.ApplicationEventPublisher;
import com.appverse.app_service.mapper.ApplicationMapper;
import com.appverse.app_service.model.Application;
import com.appverse.app_service.repository.ApplicationRepository;
import com.appverse.app_service.services.ApplicationMediaService;
import com.appverse.app_service.services.createService.ApplicationCreateService;
import com.appverse.app_service.services.serviceImpl.ApplicationServiceImpl;
import com.appverse.app_service.validator.ApplicationValidator;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplTest {

	@Mock
	private ApplicationMediaService mediaService;
	@Mock
	private ApplicationRepository repository;
	@Mock
	private ApplicationCreateService createService;
	@Mock
	private ApplicationMapper mapper;
	@Mock
	private SubscriptionServiceClient subscriptionServiceClient;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private ApplicationValidator validator;

	private Executor syncExecutor = Runnable::run;

	@InjectMocks
	private ApplicationServiceImpl service;

	@BeforeEach
	void setup() {
		service = new ApplicationServiceImpl(mediaService, repository, createService, mapper, subscriptionServiceClient,
				eventPublisher, syncExecutor, validator);
	}

	@Test
	void createApplication_success() {
		ApplicationRequest request = new ApplicationRequest(
				null,
				"Test",
				null,
				"desc",
				"1.0",
				"cat",
				new BigDecimal("-10"),
				"USD",
				false,
				MonetizationType.ONE_TIME_PURCHASE,
				null,
				null,
				null,
				null,
				null,
				null);

		Application app = new Application();
		app.setId("app123");
		app.setName("Test App");
		app.setMonetizationType(MonetizationType.FREE);
		app.setScreenshots(new java.util.ArrayList<>());

		when(createService.toEntity(request, "dev1")).thenReturn(app);
		when(mediaService.uploadThumbnail(any(), any())).thenReturn("thumb.jpg");
		when(mediaService.uploadScreenshots(any(), any(), any())).thenReturn(List.of());
		when(repository.save(any())).thenReturn(app);

		MessageResponse response = service.createApplication(request, null, null, List.of(), "dev1");

		assertEquals("app123", response.id());

		verify(repository).save(any());
		verify(eventPublisher).publishCreated(any());
	}

	@Test
	void createApplication_negativePrice_shouldThrow() {
		ApplicationRequest request = new ApplicationRequest(null, "Test", null, "desc", "1.0", "cat",
				new BigDecimal("-10"), "USD", false, MonetizationType.ONE_TIME_PURCHASE, null, null, null, null, null,
				null);

		Application app = new Application();
		app.setMonetizationType(MonetizationType.ONE_TIME_PURCHASE);
		app.setScreenshots(new ArrayList<>());

		when(createService.toEntity(request, "dev1")).thenReturn(app);

		assertThrows(BadRequestException.class,
				() -> service.createApplication(request, null, null, List.of(), "dev1"));
	}

	@Test
	void updateApplication_success() {
		Application existing = new Application();
		existing.setId("app1");
		existing.setDeveloperId("dev1");
		existing.setScreenshots(new java.util.ArrayList<>());

		when(repository.findById("app1")).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenReturn(existing);

		UpdateApplicationRequest updateRequest = mock(UpdateApplicationRequest.class);

		MessageResponse response = service.updateApplication("app1", updateRequest, null, null, List.of(), "dev1");

		assertEquals("app1", response.id());
		verify(repository).save(existing);
	}

	@Test
	void updateApplication_wrongDeveloper_shouldThrow() {
		Application existing = new Application();
		existing.setId("app1");
		existing.setDeveloperId("dev1");
		existing.setScreenshots(new java.util.ArrayList<>());

		when(repository.findById("app1")).thenReturn(Optional.of(existing));

		assertThrows(AccessDeniedException.class,
				() -> service.updateApplication("app1",
						mock(UpdateApplicationRequest.class), null, null, List.of(), "anotherDev"));
	}

	@Test
	void deleteApplication_success() {
		Application existing = new Application();
		existing.setId("app1");
		existing.setDeveloperId("dev1");

		when(repository.findById("app1")).thenReturn(Optional.of(existing));

		service.deleteApplication("app1", "dev1");

		verify(repository).deleteById("app1");
	}

	@Test
	void deleteApplication_notFound() {

		when(repository.findById("app1")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> service.deleteApplication("app1", "dev1"));
	}

	@Test
	void getApplicationById_success() {

		Application app = new Application();
		app.setId("app1");

		when(repository.findById("app1")).thenReturn(Optional.of(app));
		when(mapper.toResponse(app)).thenReturn(mock(ApplicationResponse.class));

		ApplicationResponse response = service.getApplicationById("app1");

		assertNotNull(response);
	}

	@Test
	void getApplicationById_notFound() {

		when(repository.findById("app1")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> service.getApplicationById("app1"));
	}

}
