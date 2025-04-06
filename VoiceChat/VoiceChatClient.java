package VoiceChat;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class VoiceChatClient {
    public static JButton connectBtn;
    public static JButton disconnectBtn;
    public static JLabel statusLbl;
    public static JSpinner portSpinner;
    public static JLabel portLbl;
    public static JTextField hostField;
    public static JLabel hostLbl;
    
    public VoiceChatClient()
    {
        JFrame frame = new JFrame("VoiceChat");
        connectBtn = new JButton ("Connect");
        disconnectBtn = new JButton ("Hang up/quit");
        statusLbl = new JLabel("");
        portSpinner = new JSpinner();
        portLbl = new JLabel("Port No.");
        hostField = new JTextField();
        hostLbl = new JLabel("Host name");

        //adjust size and set layout
        frame.pack();
        frame.setSize(400, 300);
        frame.setLayout(null);
        
        //action for connect button
        connectBtn.addActionListener(new ActionListener(){  
    public void actionPerformed(ActionEvent e){           
        try {
            connect();
        } catch (IOException|LineUnavailableException ex) {
            
        } 
            
    }  
    }); 
        //action for disconnect button
        disconnectBtn.addActionListener(new ActionListener(){  
    public void actionPerformed(ActionEvent e){  
            disconnect();
    }  
    }); 
       
        //add components
        frame.add(connectBtn);
        frame.add(disconnectBtn);
        frame.add(statusLbl);
        frame.add(portSpinner);
        frame.add(portLbl);
        frame.add(hostField);
        frame.add(hostLbl);
        //set component bounds (Absolute Positioning)
        connectBtn.setBounds (45, 85, 120, 30);
        disconnectBtn.setBounds (200, 85, 115, 30);
        statusLbl.setBounds(130 , 175, 200, 30);
        portSpinner.setBounds(250 , 40, 60, 30);
        portLbl.setBounds(200, 40, 60, 30);
        hostField.setBounds(90,40,100,30);
        hostLbl.setBounds(20, 40, 80, 30);
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible (true);
        //set defaults
        hostField.setText("127.0.0.1");
        portSpinner.setValue(5000);
    }
    public static void connect() throws IOException, LineUnavailableException
    {   
        
        Thread thread = new Receive();
        thread.start();
        
	
    }
    public static void disconnect()
    {
        
            //frame.dispose();
            System.exit(0);
    }
    public static void main (String[] args) {
        
        new VoiceChatClient();
    }
}
class Receive extends Thread 
{
    public void run() 
    {   
        int port=(Integer)VoiceChatClient.portSpinner.getValue();
        String host=(String)VoiceChatClient.hostField.getText();       //"127.0.0.1";
        Socket socket;
	SourceDataLine speakers;
        //for sending
        TargetDataLine microphone = null;
        
        try{
            //socket stuff starts here
        VoiceChatClient.statusLbl.setText("Connecting...");    
	socket = new Socket(host, port);
	VoiceChatClient.statusLbl.setText("Connected!");
        //socket stuff ends here
        //input stream
	InputStream in = socket.getInputStream();
        //audioformat
	AudioFormat format = new AudioFormat(16000, 8, 2, true, true);
        //audioformat
        //selecting and strating speakers
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        speakers = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
        speakers.open(format);
        speakers.start();
        
        //for sending
        OutputStream out = null;
        out = socket.getOutputStream();
        
        //selecting and starting microphone
        microphone = AudioSystem.getTargetDataLine(format);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        
        
        byte[] bufferForOutput = new byte[1024];
        int bufferVariableForOutput = 0;
            
        byte[] bufferForInput = new byte[1024];
        int bufferVariableForInput;

        while((bufferVariableForInput = in.read(bufferForInput)) > 0  || (bufferVariableForOutput=microphone.read(bufferForOutput, 0, 1024)) > 0) {
            out.write(bufferForOutput, 0, bufferVariableForOutput);
            speakers.write(bufferForInput, 0, bufferVariableForInput);
                
        }
        }
        catch(IOException | LineUnavailableException e)
        {
            e.printStackTrace();
            //System.out.println("some error occured");
        }
    }
}