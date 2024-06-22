package src.tests;

import org.junit.jupiter.api.Test;
import src.Ports;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PortTest {

    @Test
    public void testValidPorts() {
        assertTrue(Ports.isValid("0"));
        assertTrue(Ports.isValid("80"));
        assertTrue(Ports.isValid("443"));
        assertTrue(Ports.isValid("65535"));
    }

    @Test
    public void testInvalidPorts() {
        assertFalse(Ports.isValid("65536"));
        assertFalse(Ports.isValid("-1"));
        assertFalse(Ports.isValid("abc"));
    }
}

