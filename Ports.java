package src;

public class Ports {
    public static final String PORT_REGEX = "^(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3}|0)$";

    public static boolean isValid(String port) {
        return port.matches(PORT_REGEX);
    }
}