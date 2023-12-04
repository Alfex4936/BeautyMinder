package app.beautyminder.controller.admin;

import app.beautyminder.domain.Review;
import app.beautyminder.dto.ReviewStatusUpdateRequest;
import app.beautyminder.dto.chat.ChatKickDTO;
import app.beautyminder.dto.chat.ChatMessage;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.cosmetic.GPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final GPTService gptService;
    private final ReviewRepository reviewRepository;
    private final MongoService mongoService;
    private final SimpMessageSendingOperations messagingTemplate;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello admin";
    }

    @GetMapping("/reviews")
    public Page<Review> getAllReviews(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findAll(pageable);
    }

    @GetMapping("/reviews/denied")
    public List<Review> getFilteredReviews() {
        return reviewRepository.findAllFiltered();
    }

    @PatchMapping("/reviews/{reviewId}/status")
    public void updateReviewStatus(@PathVariable String reviewId, @RequestBody ReviewStatusUpdateRequest request) {
        boolean isFiltered = switch (request.status().toLowerCase()) {
            case "approved" -> false;
            case "denied" -> true;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + request.status());
        };

        mongoService.updateFields(reviewId, Map.of("isFiltered", isFiltered), Review.class);
    }

    @PostMapping("/chat/kick")
    public void kickUser(@RequestBody ChatKickDTO request) {
        var msg = ChatMessage.builder().message("You have been kicked out!").build();
        messagingTemplate.convertAndSendToUser(request.username(), "/queue/kick", msg);
    }
}