package com;

import com.cmd.Command;
import com.game.MoveGame;
import com.network.Client;
import com.network.Network;
import com.network.Packet;
import com.network.ReceivedPacket;


public class MyServer extends ServerLogic {
    protected Network network;
    protected CountUpdate countUpdate;
    protected Command command;
    protected MoveGame game;
    @Override
    protected void init() throws Exception {
        network = new Network();
        countUpdate = new CountUpdate(this);
        command = new Command(this);
        game = new MoveGame();

        network.init(25565);
        network.addListener(game);
    }

    @Override
    protected void update(double timeElapsed) throws Exception {
        command.input();
        network.read();
        countUpdate.update(timeElapsed);
        for (Client client : network.getClients()) {
            while (client.hasInput()) {
                ReceivedPacket packet = client.readInput();
                if (packet.getType().equals("ping")) {
                    client.send(new Packet("pong", "Bonjour client!"));
                } else if (packet.getType().equals("move")) {
                    game.onMove(client, packet);
                } else if (packet.getType().equals("rotate")) {
                    game.onRotate(client, packet);
                }
            }
        }
        network.write();
    }

    @Override
    protected void destroy() {
        countUpdate.displayUpdateCount();
        command.destroy();
        network.destroy();
    }
}
