package app.beautyminder.controller;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({"awsBasic", "test"})
class TodoApiControllerTest {

    private static final String TEST_USER_EMAIL = "todotest@gmail.com";
    private static final String TEST_USER_PASSWORD = "test";
    private static final String CREATE_TEMPLATE = """
            {
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
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofMinutes(3);
    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(1);
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    private String userId;
    private String todoId;
    private List<String> taskIds;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_USER_EMAIL);
        addUserRequest.setPassword(TEST_USER_PASSWORD);

        User user = userService.saveUser(addUserRequest);
        userId = user.getId();
        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
    }

    @Test
    @Order(1)
    @DisplayName("Test Todo Create")
    public void testCreateTodo() throws Exception {
        // given
        String url = "/todo/create";
        String todoJson = String.format(CREATE_TEMPLATE, "2023-11-11");

        // when
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + accessToken)
//                        .cookie(new Cookie("XRT", refreshToken))
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
    @Order(1)
    @DisplayName("Test Todo Create Fail")
    public void testCreateTodo_Fail() throws Exception {
        // given
        String url = "/todo/create";
        String todoJson = String.format(CREATE_TEMPLATE, "2023-11-11");

        // when
        mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + accessToken)
//                        .cookie(new Cookie("XRT", refreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(todoJson.getBytes(StandardCharsets.UTF_8)))
                // then
                .andExpect(status().isBadRequest());

    }


    @Test
    @Order(2)
    @DisplayName("Test Todo Get")
    public void testGetTodo() throws Exception {
        // given
        String url = "/todo/all";
        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").value("Here are the todos"))
                .andExpect(jsonPath("$.todos[0].date").value("2023-11-11"))
                .andExpect(jsonPath("$.todos[*].user.id").exists());
    }

    @Test
    @Order(2)
    @DisplayName("Test Today Todo Get")
    public void testGetTodayTodo() throws Exception {
        // given
        String url = "/todo/2023-11-11";
        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").value("Here are the todos of the day"))
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
        MvcResult result = mockMvc.perform(put(url)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.getBytes(StandardCharsets.UTF_8)))
                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.message").value("Updated Todo"))
                .andExpect(jsonPath("$.todo.tasks[0].description").value("세수해3"))
                .andExpect(jsonPath("$.todo.tasks[1].description").value("세수해222"))
                .andExpect(jsonPath("$.todo.tasks[2].description").value("세수해4"))
                .andExpect(jsonPath("$.todo.user.id").exists())
                .andReturn();

        // Extract the response body
        String responseContent = result.getResponse().getContentAsString();

        // Parse the response content to a JSON object
        // Assuming you are using Jackson's ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseContent);

        // Extract taskIds
        JsonNode tasksNode = rootNode.path("todo").path("tasks");
        for (JsonNode taskNode : tasksNode) {
            String taskId = taskNode.path("taskId").asText();
            taskIds.add(taskId);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test Todo Task Delete Success")
    public void testDeleteTodoTask() throws Exception {
        // given
        String url = "/todo/delete/" + todoId + "/task/" + taskIds.get(0);
        // when
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + accessToken))
                // then
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Order(5)
    @DisplayName("Test Todo Task Delete Fail")
    public void testDeleteTodoTask_Fail() throws Exception {
        // given
        String url = "/todo/delete/" + todoId + "/task/" + taskIds.get(0) + "1";
        // when
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + accessToken))
                // then
                .andExpect(status().isNotFound());
    }


    @Test
    @Order(6)
    @DisplayName("Test Todo Delete Success")
    public void testDeleteTodo() throws Exception {
        // given
        String url = "/todo/delete/" + todoId;
        // when
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + accessToken))
                // then
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Order(7)
    @DisplayName("Test Todo Delete Fail")
    public void testDeleteTodo_Fail() throws Exception {
        // given
        String url = "/todo/delete/" + todoId + "1";

        // when
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + accessToken))
                // then
                .andExpect(status().isNotFound());
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
