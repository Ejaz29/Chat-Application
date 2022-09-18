import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
/*
  When a client connects the server spawns a thread to handle the client.
  This way the server can handle multiple clients at the same time.
 */
class ClientHandler implements Runnable{
    private Socket socket;
    public static ArrayList<ClientHandler> clientHandlers=new ArrayList<>();
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // When a client connects their username is sent.
            this.username=bufferedReader.readLine();
            // Add the new client handler to the array,so they can receive messages from others.
            clientHandlers.add(this);
            broadCast("Server: "+ username+" has joined the chat");
        }
        catch (Exception e){
            closeEveryThing(socket,bufferedReader,bufferedWriter);
        }
    }
    // Everything in this method is run on a separate thread. We want to listen for messages
    // on a separate thread because listening (bufferedReader.readLine()) is a blocking operation.
    // A blocking operation means the caller waits for the callee to finish its operation.
    @Override
    public void run() {
        String messageFromClient;
        try{
            // Continue to listen for messages while a connection with the client is still established.
            while (socket.isConnected()){
                messageFromClient=bufferedReader.readLine();
                broadCast(messageFromClient);
            }
        }
        catch (Exception e){
            closeEveryThing(socket,bufferedReader,bufferedWriter);
        }
    }

    // Send a message through each client handler thread so that everyone gets the message.
    // Basically each client handler is a connection to a client. So for any message that
    // is received, loop through each connection and send it down it.

    private void broadCast(String messageToSend) {
        try {
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }
        }
        catch (Exception e){
            closeEveryThing(socket,bufferedReader,bufferedWriter);
        }
    }
    private void closeEveryThing(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void removeClientHandler(){
        clientHandlers.remove(this);
        broadCast("Server: "+ username+" has left the chat");
        System.out.println("A user has left the chat");
    }
}