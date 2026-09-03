CREATE INDEX idx_events_game_occurred_at
    ON events (game_id, occurred_at DESC, id DESC);