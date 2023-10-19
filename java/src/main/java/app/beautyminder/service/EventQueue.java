package app.beautyminder.service;

import app.beautyminder.dto.Event;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class EventQueue {

    private final ConcurrentLinkedQueue<Event> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(Event event) {
        queue.offer(event);
    }

    public void enqueueAll(Collection<Event> events) {
        queue.addAll(events);
    }

    public List<Event> dequeueAll() {
        List<Event> events = new ArrayList<>();
        while (!queue.isEmpty()) {
            Event event = queue.poll();
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }


}