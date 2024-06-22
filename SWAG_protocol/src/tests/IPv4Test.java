package src;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IPv4Test {

    @Test
    public void testValidIPv4Addresses() {
        assertTrue(IPv4Address.isValid("192.168.1.1"));
        assertTrue(IPv4Address.isValid("127.0.0.1"));
        assertTrue(IPv4Address.isValid("255.255.255.255"));
    }

    @Test
    public void testInvalidIPv4Addresses() {
        assertFalse(IPv4Address.isValid("192.168.1.256"));
        assertFalse(IPv4Address.isValid("192.168.1"));
        assertFalse(IPv4Address.isValid("192.168.1.1.1"));
        assertFalse(IPv4Address.isValid("abc.def.ghi.jkl"));
    }
}