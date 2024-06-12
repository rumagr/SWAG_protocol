package SWAG_protocol.src;

import org.json.*;

import java.io.*;

public class main2 {
    public final static String s = "test.json";

        public static void main(String[] args) throws IOException {

           SimpleTCPServerNIO nio = new SimpleTCPServerNIO();

           nio.run();

        }
    }

