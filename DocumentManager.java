package usa.bogdan.web;

import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.springframework.web.bind.annotation.RequestBody;

import javax.print.Doc;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DocumentManager {
    private final Map<String, Document> storage = new HashMap<>();
    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(document -> matchesSearchRequest(document, request))
                .collect(Collectors.toList());
    }
    public Document save(Document document) {
        if (storage.containsKey(document.getId())) {
            Document existingDocument = storage.get(document.getId());
            document.setCreated(existingDocument.getCreated());
        } else {
            document.setId(String.valueOf(atomicInteger.incrementAndGet()));
        }
        storage.put(document.getId(), document);
        return document;
    }
    private boolean matchesSearchRequest(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            boolean matchesTitlePrefix = request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> document.getTitle().startsWith(prefix));
            if (!matchesTitlePrefix) {
                return false;
            }
        }
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            boolean matchesContent = request.getContainsContents().stream()
                    .anyMatch(content -> document.getContent().contains(content));
            if (!matchesContent) {
                return false;
            }
        }
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            boolean matchesAuthor = request.getAuthorIds().contains(document.getAuthor().getId());
            if (!matchesAuthor) {
                return false;
            }
        }
        if (request.getCreatedFrom() != null && document.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }
        if (request.getCreatedTo() != null && document.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }
        return true;
    }
    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }
    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }
    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
