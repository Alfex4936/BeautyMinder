package app.beautyminder.controller;

import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoApiControllerTest {

    private static final String TEST_USER_EMAIL = "usertest@gmail.com";
    private static final String TEST_USER_PASSWORD = "test";
    private static final String CREATE_TEMPLATE = """
            {
              "userId": "%s",
              "date": "%s",
              "tasks": [
                {
                  "description": "세수해",
                  "category": "morning"
                },
            		{
                  "description": "세수해222",
                  "category": "morning"
                },
                {
                  "description": "밥해",
                  "category": "dinner"
                }
              ]
            }
            """;

    private static final String UPDATE_TEMPLATE = """
            {
            	"tasksToUpdate": [
            		{
            			"taskId": "%s",
            			"description": "세수해3",
            			"isDone": true
            		}
            	],
            	"tasksToAdd": [
            		{
            			"description": "세수해4",
            			"category": "dinner"
            		}
            	],
            	"taskIdsToDelete": ["%s"]
            }
            """;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserService userService;
    private String userId;
    private String todoId;
    private List<String> taskIds;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_USER_EMAIL);
        addUserRequest.setPassword(TEST_USER_PASSWORD);

        userId = userService.saveUser(addUserRequest);
    }

    @Test
    @Order(1)
    @DisplayName("Test Todo Create")
    public void testCreateTodo() throws Exception {
        // given
        String url = "/todo/create";
        String todoJson = String.format(CREATE_TEMPLATE, userId, "2023-11-11");

        // when
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(todoJson.getBytes(StandardCharsets.UTF_8)))
                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").value("Todo added successfully"))
                .andExpect(jsonPath("$.todo.id").exists())
                .andExpect(jsonPath("$.todo.date").value("2023-11-11"))
                .andExpect(jsonPath("$.todo.tasks[0].description").value("세수해"))
                .andExpect(jsonPath("$.todo.tasks[0].category").value("morning"))
                .andExpect(jsonPath("$.todo.tasks[0].done").value(false))
                .andExpect(jsonPath("$.todo.user.id").exists())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        todoId = (String) ((Map<String, Object>) responseMap.get("todo")).get("id");
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) ((Map<String, Object>) responseMap.get("todo")).get("tasks");
        taskIds = tasks.stream()
                .map(task -> (String) task.get("taskId"))
                .collect(Collectors.toList());

    }

    @Test
    @Order(2)
    @DisplayName("Test Todo Get")
    public void testGetTodo() throws Exception {
        // given
        String url = "/todo/all?userId=" + userId;
        // when
        mockMvc.perform(get(url))
                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").value("Here are the todos"))
                .andExpect(jsonPath("$.todos[0].date").value("2023-11-11"))
                .andExpect(jsonPath("$.todos[*].user.id").exists());
    }

    @Test
    @Order(3)
    @DisplayName("Test Todo Update")
    public void testUpdateTodo() throws Exception {
        // given
        String url = "/todo/update/" + todoId;
        String requestBody = String.format(UPDATE_TEMPLATE, taskIds.get(0), taskIds.get(2));

        // when
        mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.getBytes(StandardCharsets.UTF_8)))
                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").value("Updated Todo"))
                .andExpect(jsonPath("$.todo.tasks[0].description").value("세수해3"))
                .andExpect(jsonPath("$.todo.tasks[1].description").value("세수해222"))
                .andExpect(jsonPath("$.todo.tasks[2].description").value("세수해4"))
                .andExpect(jsonPath("$.todo.user.id").exists());
    }

    @AfterEach
    public void tearDown() {
        // Clean up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {
        try {
            // Final cleanup logic to run after all tests
            userService.deleteUserAndRelatedData(userId);
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }
}
