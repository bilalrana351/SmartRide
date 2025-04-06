package VoiceChat;

//GUI 

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 * @author mana
 */
public class VoiceChatServer {
    
    public static JButton connectBtn;
    public static JButton disconnectBtn;
    public static JLabel statusLbl;
    public static JSpinner portSpinner;
    public static JLabel portLbl;
    public VoiceChatServer()
    {
        JFrame frame = new JFrame("VoiceChat");
        connectBtn = new JButton ("Connect");
        disconnectBtn = new JButton ("Hang up/quit");
        statusLbl = new JLabel("");
        portSpinner = new JSpinner();
        portLbl = new JLabel("Port No.");
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
        //set component bounds ( Absolute Positioning)
        connectBtn.setBounds (45, 85, 120, 30);
        disconnectBtn.setBounds (200, 85, 115, 30);
        statusLbl.setBounds(130 , 175, 200, 30);
        portSpinner.setBounds(160 , 40, 60, 30);
        portLbl.setBounds(100, 40, 60, 30);
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - frame.getWidth();
        int y = (screenSize.height - frame.getHeight()) / 2; // Center vertically
        frame.setLocation(x, y);

        frame.setVisible (true);
        //set defualts
        portSpinner.setValue(5000);
    }
    public static void connect() throws IOException, LineUnavailableException
    {   
        
        Thread thread = new Send();
        thread.start();
	
    }
    public static void disconnect()
    {
        
            //frame.dispose();
            System.exit(0);
    }
    public static void main (String[] args) {
        
        new VoiceChatServer();
    }


}
class Send extends Thread 
{
    public void run() 
    {   
        int port= (Integer)VoiceChatServer.portSpinner.getValue();
        ServerSocket socket = null;
        //for sending 
	TargetDataLine microphone = null;
        //for recieveing
        SourceDataLine speakers;
        
        Socket client = null;
        try {
            //socket stuff starts here
            socket = new ServerSocket(port);
            VoiceChatServer.statusLbl.setText("Waiting for Connection...");
            client = socket.accept();
            VoiceChatServer.statusLbl.setText("Connected!");
            //socket stuff ends here
            //outputstream
            OutputStream out = null;
            out = client.getOutputStream();
            //outputstream
            //audioformat
            AudioFormat format = new AudioFormat(16000, 8, 2, true, true);
            //audioformat
            //selecting and starting microphone
            microphone = AudioSystem.getTargetDataLine(format);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            
            //for recieveing
            InputStream in = client.getInputStream();
            //selecting and starting speakers
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
            speakers.open(format);
            speakers.start();
            
            
            byte[] bufferForInput = new byte[1024];
            int bufferVariableForInput = 0;
	
        
            byte[] bufferForOutput = new byte[1024];
            int bufferVariableForOutput = 0;
            
            //send/recieve
            while(((bufferVariableForOutput = microphone.read(bufferForOutput, 0, 1024)) > 0) || (bufferVariableForInput = in.read(bufferForInput)) > 0) {
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