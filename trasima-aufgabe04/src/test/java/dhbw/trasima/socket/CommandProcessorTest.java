package dhbw.trasima.socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dhbw.trasima.v2.VehicleStore;

class CommandProcessorTest {

    private final CommandProcessor processor = new CommandProcessor(new VehicleStore());

    @Test
    void putThenGetReturnsTheStoredVehicle() {
        assertEquals("OK", processor.process("PUT V2-1 49.4879 8.4662 50 90 1000"));
        assertEquals("VALUE V2-1 49.487900 8.466200 50.00 90.00 1000", processor.process("GET V2-1"));
    }

    @Test
    void unknownVehicleIsNotFound() {
        assertEquals("NOT_FOUND", processor.process("GET nobody"));
        assertEquals("NOT_FOUND", processor.process("DEL nobody"));
    }

    @Test
    void listAndNearReturnOneLinePerVehicle() {
        processor.process("PUT near 49.4880 8.4662 50 0 1");
        processor.process("PUT far 49.5500 8.5400 50 0 1");

        assertEquals("COUNT 2", processor.process("COUNT"));
        assertEquals(3, processor.process("LIST").split("\n").length);
        assertEquals("VALUES 1\nVALUE near 49.488000 8.466200 50.00 0.00 1",
                processor.process("NEAR 49.4879 8.4662 0.5"));
    }

    @Test
    void deleteRemovesTheVehicle() {
        processor.process("PUT V2-1 49.4879 8.4662 50 90");
        assertEquals("OK", processor.process("DEL V2-1"));
        assertEquals("COUNT 0", processor.process("COUNT"));
    }

    @Test
    void malformedRequestsAreAnsweredWithError() {
        assertTrue(processor.process("").startsWith("ERROR"));
        assertTrue(processor.process("FLY away").startsWith("ERROR"));
        assertTrue(processor.process("PUT V2-1 49.4").startsWith("ERROR"));
        assertTrue(processor.process("PUT V2-1 north 8.4 50 90").startsWith("ERROR"));
        assertTrue(processor.process("GET").startsWith("ERROR"));
    }

    @Test
    void quitIsAnsweredWithBye() {
        assertEquals("BYE", processor.process("quit"));
    }
}
