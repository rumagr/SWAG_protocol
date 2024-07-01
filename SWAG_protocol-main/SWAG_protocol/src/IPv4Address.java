package src;

import java.util.regex.Pattern;

public class IPv4Address {
    private static final String IPV4_REGEX =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValid(String address) {
        return IPV4_PATTERN.matcher(address).matches();
    }
}