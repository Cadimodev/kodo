package com.kodo.api.infrastructure.web.controllers;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.application.dto.EventResponse;
import com.kodo.api.application.dto.PagedResult;
import com.kodo.api.application.exceptions.RateLimitExceededException;
import com.kodo.api.application.ports.in.EventQueryService;
import com.kodo.api.application.ports.in.EventService;
import com.kodo.api.infrastructure.web.handlers.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(ApiExceptionHandler.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventQueryService eventQueryService;

    @Test
    void shouldReturnAcceptedWhenEventIsAccepted() throws Exception {
        when(eventService.createEvent(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        MvcResult result = mockMvc.perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson())
                )
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturnTooManyRequestsWhenRateLimitIsExceeded() throws Exception {
        when(eventService.createEvent(any()))
                .thenThrow(new RateLimitExceededException(2));

        mockMvc.perform(
                        post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson())
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "2"));
    }

    @Test
    void shouldReturnEventsWithDefaultPagination() throws Exception {

        EventQuery expectedQuery = new EventQuery(
                null,
                null,
                null,
                0,
                20
        );

        PagedResult<EventResponse> result = new PagedResult<>(
                List.of(),
                0,
                20,
                0,
                0
        );

        when(eventQueryService.findEvents(expectedQuery))
                .thenReturn(result);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(eventQueryService).findEvents(expectedQuery);
    }

    @Test
    void shouldPassFiltersAndPaginationToQueryService() throws Exception {

        EventQuery expectedQuery = new EventQuery(
                "game-1",
                "player-7",
                "PLAYER_DIED",
                2,
                50
        );

        PagedResult<EventResponse> result = new PagedResult<>(
                List.of(),
                2,
                50,
                0,
                0
        );

        when(eventQueryService.findEvents(expectedQuery))
                .thenReturn(result);

        mockMvc.perform(
                        get("/events")
                                .param("gameId", "game-1")
                                .param("playerId", "player-7")
                                .param("type", "PLAYER_DIED")
                                .param("page", "2")
                                .param("size", "50")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(50));

        verify(eventQueryService).findEvents(expectedQuery);
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeExceedsMaximum() throws Exception {

        mockMvc.perform(
                        get("/events")
                                .param("size", "101")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventQueryService);
    }

    private String validRequestJson() {
        return """
                {
                  "eventId": "d2eaf250-86c8-4ac7-bbb0-6acdfde46fe7",
                  "gameId": "test-game",
                  "playerId": "player-123",
                  "type": "PLAYER_DIED",
                  "occurredAt": "2026-09-02T10:00:00Z",
                  "payload": {
                    "weapon": "test-weapon"
                  }
                }
                """;
    }
}