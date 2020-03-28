package com.cmd;

import com.ServerLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command{

    protected ServerLogic serverLogic;
    protected BufferedReader reader;

    public Command(ServerLogic serverLogic) {
        this.serverLogic = serverLogic;
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void input() throws Exception {

        if (reader.ready()) {
            String line = reader.readLine();
            System.out.println(line);
            if (line.equals("stop")) {
                serverLogic.stop();
            }
        }

    }

    public void destroy() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
