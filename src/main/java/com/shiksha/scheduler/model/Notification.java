package com.shiksha.scheduler.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    private String message;

    @Column(name = "link")
    private String link;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User recipient;
        private String message;
        private String link;

        public Builder recipient(User v) { this.recipient = v; return this; }
        public Builder message(String v) { this.message = v; return this; }
        public Builder link(String v)    { this.link = v; return this; }

        public Notification build() {
            Notification n = new Notification();
            n.recipient = this.recipient;
            n.message   = this.message;
            n.link      = this.link;
            return n;
        }
    }

    public Long          getId()         { return id; }
    public User          getRecipient()  { return recipient; }
    public String        getMessage()    { return message; }
    public String        getLink()       { return link; }
    public boolean       isRead()        { return read; }
    public void          setRead(boolean v){ this.read = v; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
}
