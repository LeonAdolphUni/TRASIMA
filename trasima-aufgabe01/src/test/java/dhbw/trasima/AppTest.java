package dhbw.trasima;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void greetingMentionsTrasima() {
        assertTrue(App.greeting().contains("TRASIMA"));
    }
}
