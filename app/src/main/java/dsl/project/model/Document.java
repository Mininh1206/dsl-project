package dsl.project.model;

public class Document {
    private final String id;
    private final Object content;

    public Document(String id, Object content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Document{id='" + id + "', content=" + content + "}";
    }
}
