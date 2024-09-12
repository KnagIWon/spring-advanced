package org.example.expert.domain.todo;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Test
    public void todo_저장을_성공한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");
        User user = new User(authUser.getEmail(), "password", UserRole.USER);
        String weather = "Sunny"; // 가짜 날씨 정보
        Todo newTodo = new Todo(todoSaveRequest.getTitle(), todoSaveRequest.getContents(), weather, user);
        given(todoRepository.save(any())).willReturn(newTodo);
        Todo savedTodo = todoRepository.save(newTodo);
        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

        // when
        TodoSaveResponse result = todoService.saveTodo(authUser, todoSaveRequest);

        // then
        assertNotNull(result);
        assertEquals(savedTodo.getId(), result.getId());
        assertEquals(savedTodo.getTitle(), result.getTitle());
        assertEquals(savedTodo.getContents(), result.getContents());
        assertEquals(weather, result.getWeather());
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getUser().getEmail());
    }

    @Test
    public void 할일목록을_페이지네이션으로_조회한다() {
        // given
        int page = 1;
        int size = 5;

        User user = new User("email", "password", UserRole.USER);

        List<Todo> todoList = Arrays.asList(
                new Todo("Title1", "Content1", "Sunny", user),
                new Todo("Title2", "Content2", "Rainy", user)
        );

        Page<Todo> todos = new PageImpl<>(todoList);
        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todos);

        // when
        Page<TodoResponse> result = todoService.getTodos(page, size);

        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Title1", result.getContent().get(0).getTitle());
        assertEquals("Title2", result.getContent().get(1).getTitle());
        assertEquals("Sunny", result.getContent().get(0).getWeather());
        assertEquals("email", result.getContent().get(0).getUser().getEmail());
    }

    @Test
    public void todo_단건_조회를_실패한다() {
        // given
        long todoId = 1L;
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            todoService.getTodo(todoId);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void todo_단건_조회를_성공한다() {
        // given
        long todoId = 1L;

        User user = new User("email", "password", UserRole.USER);
        Todo todo = new Todo("title", "contents","Sunny", user);
        User todoUser = todo.getUser();

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse result = todoService.getTodo(todoId);

        // then
        Assertions.assertNotNull(result);
        assertEquals(todo.getId(), result.getId());
        assertEquals(todo.getTitle(), result.getTitle());
        assertEquals(todo.getContents(), result.getContents());
        assertEquals(todo.getWeather(), result.getWeather());
        assertEquals(todoUser.getId(), result.getUser().getId());
        assertEquals(todoUser.getEmail(), result.getUser().getEmail());
    }

}
