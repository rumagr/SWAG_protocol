package src;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class main2 {
    public final static String s = "test.json";

    public static final Logger logger = LogManager.getLogger(main2.class);

    public static void main(String[] args) {
        logger.info("Hello, World!" + s);
    }
}