import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Server{
    private Socket socket;
    private ServerSocket serverSocket;

    Server(ServerSocket serverSocket){
        this.serverSocket=serverSocket;
    }

    public void startServer(){
        while(!serverSocket.isClosed()){
            try {
                socket=serverSocket.accept();
                System.out.println("A new Client has joined");
                ClientHandler clientHandler=new ClientHandler(socket);
                Thread thread=new Thread(clientHandler);
                thread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void closeServer(){
        if(serverSocket!=null){
            try {
                serverSocket.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket=new ServerSocket(9999);
        Server server=new Server(serverSocket);
        server.startServer();
    }
}