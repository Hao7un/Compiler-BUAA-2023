package utils;

import java.io.*;
import java.util.StringJoiner;

public class InOututils {
    static BufferedReader input;

    static {
        try {
            input = new BufferedReader(new FileReader("testfile.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static BufferedWriter output;

    public static String read() throws IOException {
        String line;
        StringJoiner stringJoiner = new StringJoiner("\n");
        while ((line = input.readLine()) != null) {
            stringJoiner.add(line);
        }
        return stringJoiner.toString();
    }

    public static void write(String s , String fileName) throws IOException {
        try {
            output = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        output.write(s);
        output.write("\n");
        output.close();
    }
}
