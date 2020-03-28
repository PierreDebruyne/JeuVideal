package com.cmd;

import com.ClientLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command{

    protected ClientLogic clientLogic;
    protected BufferedReader reader;

    public Command(ClientLogic clientLogic) {
        this.clientLogic = clientLogic;
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void input() throws Exception {


        if (reader.ready()) {
            String line = reader.readLine();
            System.out.println(line);
            if (line.equals("stop")) {
                clientLogic.stop();
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
