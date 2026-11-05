package co.com.crediya.jpa.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AdapterOperationsTest {

    interface TestRepository extends CrudRepository<String, String>, QueryByExampleExecutor<String> {
    }

    static class TestAdapter extends AdapterOperations<String, String, String, TestRepository> {
        TestAdapter(TestRepository repository, ObjectMapper mapper) {
            super(repository, mapper, d -> mapper.map(d, String.class));
        }
    }

    @Mock
    private TestRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    private TestAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(objectMapper.map("value", String.class)).thenReturn("value");

        adapter = new TestAdapter(repository, objectMapper);
    }

    @Test
    void testSave() {
        when(repository.save("value")).thenReturn("value");

        assertEquals("value", adapter.save("value"));
    }

    @Test
    void testSaveAllEntities() {
        List<String> values = List.of("value", "value");
        when(repository.saveAll(values)).thenReturn(values);

        assertEquals(values, adapter.saveAllEntities(values));
    }

    @Test
    void testFindById() {
        when(repository.findById("id")).thenReturn(Optional.of("value"));

        assertEquals("value", adapter.findById("id"));
    }

    @Test
    void testFindByIdNotFound() {
        when(repository.findById("id")).thenReturn(Optional.empty());

        assertNull(adapter.findById("id"));
    }

    @Test
    void testFindAll() {
        List<String> values = List.of("value", "value");
        when(repository.findAll()).thenReturn(values);

        assertEquals(values, adapter.findAll());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFindByExample() {
        List<String> values = List.of("value", "value");
        when(repository.findAll(any(Example.class))).thenReturn(values);

        assertEquals(values, adapter.findByExample("value"));
    }
}
