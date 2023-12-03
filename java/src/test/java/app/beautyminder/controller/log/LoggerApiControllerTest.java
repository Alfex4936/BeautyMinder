package app.beautyminder.controller.log;

import app.beautyminder.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
public class LoggerApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private LogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;


    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
    }

    @Test
    public void testGetSpringLogJSON() throws Exception {
        List<String> logs = List.of("2023-10-13 hey", "2023-10-14 boo");
        when(logService.getTodaysLogs()).thenReturn(logs);

        mockMvc.perform(get("/log/spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(logs.size())));
    }

    @Test
    public void testGetSpringLogJSON_JsonProcessingExceptionFromMapper() throws Exception {
        // Include a string that is malformed JSON
        List<String> logs = List.of("{\"date\":\"2023-10-13\", \"message\":\"valid log\"}", "invalid log entry");
        when(logService.getTodaysLogs()).thenReturn(logs);

        mockMvc.perform(get("/log/spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(logs.size())))
                .andExpect(jsonPath("$[0]").isNotEmpty()) // Assuming first log is parsed successfully as JSON
                .andExpect(jsonPath("$[1]").value("Error parsing log entry")); // The "invalid log entry" causes JsonProcessingException
    }

    @Test
    public void testGetSpringLogJSON_IOExceptionFromOpensearchClient() throws Exception {
        // Simulate IOException when opensearchClient.search is called
        when(logService.getTodaysLogs()).thenThrow(IOException.class);

        // Perform the request and expect an internal server error due to IOException
        mockMvc.perform(get("/log/spring"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDropLogDocuments_ExceptionScenario() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Test Exception"))
                .when(logService).deleteAllDocuments(anyString());

        mockMvc.perform(delete("/log/spring/delete"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDropLogDocuments() throws Exception {
        doNothing().when(logService).deleteAllDocuments(anyString());

        mockMvc.perform(delete("/log/spring/delete"))
                .andExpect(status().isOk());
    }
    @AfterEach
    public void cleanUp() {
        // Clean up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {
    }
}
