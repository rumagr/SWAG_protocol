package src.tests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class RegExTest {
        private static final String isMessage = "((smto@|cto@|gcu).*)";
        private static final String regex_cto ="cto@((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3})";

        private static final String regex_smto = "smto@((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}):.*";

        @Test
        public void testValidCto() {
            assertTrue("cto@192.168.1.1:8080".matches(regex_cto));
        }

        @Test
        public void testInvalidCto() {
            assertFalse("cto@192.168.1.256:8080".matches(regex_cto));
            assertFalse("cto@192.168.1.1:70000".matches(regex_cto));
            assertFalse("cto@192.168.1".matches(regex_cto));
            assertFalse("cto@192.168.1.1".matches(regex_cto));
            assertFalse("cto@:8080".matches(regex_cto));
        }

        @Test
        public void testValidSmto() {
            assertTrue("smto@192.168.1.1:8080:Hello World".matches(regex_smto));
        }

        @Test
        public void testInvalidSmto() {
            assertFalse("smto@192.168.1.256:8080:Hello World".matches(regex_smto));
            assertFalse("smto@192.168.1.1:70000:Hello World".matches(regex_smto));
            assertFalse("smto@192.168.1:Hello World".matches(regex_smto));
            assertFalse("smto@192.168.1.1:Hello World".matches(regex_smto));
            assertFalse("smto@:8080:Hello World".matches(regex_smto));
            assertFalse("smto@192.168.1.1:8080".matches(regex_smto));
        }

        @Test
        public void testValidIsMessage() {
            assertTrue("smto@192.168.1.1:8080:Hello World".matches(isMessage));
            assertTrue("cto@192.168.1.1:8080".matches(isMessage));
            assertTrue("gcu".matches(isMessage));
        }


    }

