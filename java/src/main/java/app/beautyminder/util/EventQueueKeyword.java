package app.beautyminder.util;

import app.beautyminder.dto.Event;
import app.beautyminder.dto.KeywordEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class EventQueueKeyword {

    private final ConcurrentLinkedQueue<KeywordEvent> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(KeywordEvent event) {
        queue.offer(event);
    }

    public void enqueueAll(Collection<KeywordEvent> events) {
        queue.addAll(events);
    }

    public List<KeywordEvent> dequeueAll() {
        List<KeywordEvent> events = new ArrayList<>();
        while (!queue.isEmpty()) {
            KeywordEvent event = queue.poll();
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }


}