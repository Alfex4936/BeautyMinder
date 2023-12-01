package app.beautyminder.utils;

import app.beautyminder.dto.Event;
import app.beautyminder.dto.KeywordEvent;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    public void serialize_ShouldSerializeObject() {
        String serializableObject = "Test String";
        String serialized = CookieUtil.serialize(serializableObject);

        assertNotNull(serialized);
        // Further checks can be made if you know the expected format of the serialized string
    }


    @Test
    public void deserialize_ShouldDeserializeToCorrectObjectType() {
        String testString = "Test String";
        String serialized = CookieUtil.serialize(testString);

        Cookie cookie = new Cookie("test", serialized);
        String deserialized = CookieUtil.deserialize(cookie, String.class);

        assertEquals(testString, deserialized);
    }


    @Test
    public void addSecureCookie_ShouldAddSecureCookieCorrectly() {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        CookieUtil.addSecureCookie(mockResponse, "secureCookie", "secureValue", 3600, true, "Strict");

        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockResponse).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        String expectedHeaderValue = "secureCookie=secureValue; Max-Age=3600; Path=/; HttpOnly; Secure; SameSite=Strict";
        assertEquals("Set-Cookie", headerNameCaptor.getValue());
        assertEquals(expectedHeaderValue, headerValueCaptor.getValue());
    }


    @Test
    public void enqueueKeywordAndDequeueAllKeywords_ShouldWorkCorrectly() {
        EventQueue eventQueue = new EventQueue();
        KeywordEvent keywordEvent = new KeywordEvent("keyword");

        eventQueue.enqueueKeyword(keywordEvent);
        List<KeywordEvent> dequeuedKeywordEvents = eventQueue.dequeueAllKeywords();

        assertEquals(1, dequeuedKeywordEvents.size());
        assertSame(keywordEvent, dequeuedKeywordEvents.get(0));
    }

    @Test
    public void enqueueAllAndDequeueAll_ShouldWorkCorrectly() {
        EventQueue eventQueue = new EventQueue();
        Event event1 = new Event("id1", CosmeticRankService.ActionType.CLICK);
        Event event2 = new Event("id2", CosmeticRankService.ActionType.CLICK);
        List<Event> events = Arrays.asList(event1, event2);

        eventQueue.enqueueAll(events);
        List<Event> dequeuedEvents = eventQueue.dequeueAll();

        assertEquals(2, dequeuedEvents.size());
        assertTrue(dequeuedEvents.containsAll(events));
    }

    @Test
    public void enqueueAllKeywordsAndDequeueAllKeywords_ShouldWorkCorrectly() {
        EventQueue eventQueue = new EventQueue();
        KeywordEvent keywordEvent1 = new KeywordEvent("keyword1");
        KeywordEvent keywordEvent2 = new KeywordEvent("keyword2");
        List<KeywordEvent> keywordEvents = Arrays.asList(keywordEvent1, keywordEvent2);

        eventQueue.enqueueAllKeywords(keywordEvents);
        List<KeywordEvent> dequeuedKeywordEvents = eventQueue.dequeueAllKeywords();

        assertEquals(2, dequeuedKeywordEvents.size());
        assertTrue(dequeuedKeywordEvents.containsAll(keywordEvents));
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

    @Test
    public void resolveArgument_InvalidUserId_ShouldThrowException() {
        UserService mockUserService = mock(UserService.class);
        UserIdValidationResolver resolver = new UserIdValidationResolver(mockUserService);

        NativeWebRequest mockWebRequest = mock(NativeWebRequest.class);
        when(mockWebRequest.getParameter("userId")).thenReturn("-1");

        Assertions.assertThrows(ResponseStatusException.class, () -> resolver.resolveArgument(null, null, mockWebRequest, null));
    }

    @AfterEach
    public void cleanUp() {
        // Clea  up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {

    }
}
