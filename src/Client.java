import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
// A client has a socket to connect to the server and a reader and writer to receive and send messages respectively.
class Client extends JFrame {
    public JPanel jPanel;
    public JTextField jTextField;
    public JButton jButton;
    public JTextArea jTextArea;
    public JScrollPane jScrollPane;
    public Socket socket;
    public BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;
    public String username;
    static Scanner sc = new Scanner(System.in);
    public Client(Socket socket,String username){
        super(username);
        this.username=username;
        this.socket=socket;
        GUI();
        try{
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e){
            closeEveryThing(socket,bufferedReader,bufferedWriter);
        }
    }

    public void GUI(){
        setSize(500,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        jTextField=new JTextField();
        jPanel=new JPanel();
        jTextField.setBounds(10,300,400,25);
        jPanel.add(jTextField);
        jButton = new JButton("send");
        jButton.setBounds(415,300,70,25);
        jTextArea=new JTextArea();
        jTextArea.setFont(new Font("Tacoma",Font.PLAIN,19));
        jTextArea.setBounds(40,40,420,320);
        jTextArea.setEditable(false);
        jScrollPane = new JScrollPane(jTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setBounds(37,20,400,270);
        jPanel.add(jButton);
        jPanel.add(jScrollPane);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        jTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    sendMessage();
                }
            }
        });
        setContentPane(jPanel);
        jPanel.setLayout(null);
    }
    // Sending a message isn't blocking and can be done without spawning a thread, unlike waiting for a message.
    public void usernameSend(){
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (Exception e){
            closeEveryThing(socket,bufferedReader,bufferedWriter);
        }
    }
    // Listening for a message is blocking so need a separate thread for that.
    public void listenMesssage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(socket.isConnected()){
                    try {
                        String msg=bufferedReader.readLine();
                        jTextArea.append(msg);
                        jTextArea.append("\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sendMessage(){
        String msgtosend=jTextField.getText();
        if(msgtosend.equals("")){
            return;
        }
        try{
            bufferedWriter.write(username + ": " + msgtosend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        jTextField.setText("");
    }

    public void closeEveryThing(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter){
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

    public static void main(String[] args) {
        System.out.println("Enter the name for grp chat: ");
        String name=sc.nextLine();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket=new Socket("localhost",9999);
                    Client frame=new Client(socket,name);
                    frame.setVisible(true);
                    frame.listenMesssage();
                    frame.usernameSend();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}