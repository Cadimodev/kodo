CREATE TABLE events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    player_id VARCHAR(100),
    event_type VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    payload JSONB NOT NULL,

    CONSTRAINT uk_events_game_event UNIQUE (game_id, event_id)
);