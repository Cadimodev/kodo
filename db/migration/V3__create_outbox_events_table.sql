CREATE TABLE outbox_events (
id UUID PRIMARY KEY,
event_id UUID NOT NULL,
game_id VARCHAR(100) NOT NULL,
event_type VARCHAR(100) NOT NULL,
created_at TIMESTAMPTZ NOT NULL,
published_at TIMESTAMPTZ,

CONSTRAINT uk_outbox_game_event
UNIQUE (game_id, event_id)
);

CREATE INDEX idx_outbox_events_pending
    ON outbox_events (created_at, id)
    WHERE published_at IS NULL;