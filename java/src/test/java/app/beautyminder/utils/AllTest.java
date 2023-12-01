package app.beautyminder.utils;

import app.beautyminder.dto.Event;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.util.CookieUtil;
import app.beautyminder.util.EventQueue;
import app.beautyminder.util.UserIdValidationResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class AllTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    public void addCookie_ShouldAddCookie() {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        CookieUtil.addCookie(mockResponse, "testCookie", "testValue", 3600);

        // Verify that addCookie is called with the correct arguments
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(mockResponse).addCookie(cookieCaptor.capture());

        Cookie addedCookie = cookieCaptor.getValue();
        assertEquals("testCookie", addedCookie.getName());
        assertEquals("testValue", addedCookie.getValue());
        assertEquals(3600, addedCookie.getMaxAge());
    }

    @Test
    public void deleteCookie_ShouldDeleteCookie() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        Cookie[] cookies = {new Cookie("testCookie", "testValue")};
        when(mockRequest.getCookies()).thenReturn(cookies);

        CookieUtil.deleteCookie(mockRequest, mockResponse, "testCookie");

        // Verify that addCookie is called with a cookie that has 0 MaxAge
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(mockResponse).addCookie(cookieCaptor.capture());

        Cookie deletedCookie = cookieCaptor.getValue();
        assertEquals(0, deletedCookie.getMaxAge());
    }

    @Test
    public void enqueueAndDequeueAll_ShouldWorkCorrectly() {
        EventQueue eventQueue = new EventQueue();
        Event event = new Event("652cdc2d2bf53d0109d1e210", CosmeticRankService.ActionType.CLICK);

        eventQueue.enqueue(event);
        List<Event> dequeuedEvents = eventQueue.dequeueAll();

        assertEquals(1, dequeuedEvents.size());
        assertSame(event, dequeuedEvents.get(0));
    }

    @Test
    public void resolveArgument_ValidUserId_ShouldReturnUserId() {
        UserService mockUserService = mock(UserService.class);
        UserIdValidationResolver resolver = new UserIdValidationResolver(mockUserService);

        NativeWebRequest mockWebRequest = mock(NativeWebRequest.class);
        when(mockWebRequest.getParameter("userId")).thenReturn("123");

        String userId = (String) resolver.resolveArgument(null, null, mockWebRequest, null);

        assertEquals("123", userId);
        verify(mockUserService).findById("123");
    }

//    @Test()
//    public void resolveArgument_InvalidUserId_ShouldThrowException() {
//        UserService mockUserService = mock(UserService.class);
//        UserIdValidationResolver resolver = new UserIdValidationResolver(mockUserService);
//
//        NativeWebRequest mockWebRequest = mock(NativeWebRequest.class);
//        when(mockWebRequest.getParameter("userId")).thenReturn("-1");
//
//        resolver.resolveArgument(null, null, mockWebRequest, null);
//    }

    @AfterEach
    public void cleanUp() {
        // Clea  up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {

    }
}
