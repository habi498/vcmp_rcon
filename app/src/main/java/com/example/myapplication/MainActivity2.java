package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class MainActivity2 extends AppCompatActivity {
int passlen;byte[] data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        EditText t=(EditText)findViewById(R.id.cmdEdittext);
        t.requestFocus();
        Intent intent = getIntent();
        data = intent.getByteArrayExtra("key");
        passlen=intent.getIntExtra("passlen",0);
        String ip=intent.getStringExtra("ip");
        String port=intent.getStringExtra("port");
        Button button = (Button) findViewById(R.id.sendbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String command = t.getText().toString();
               int cmdlen=command.length();
               data[13+passlen]=Integer.valueOf((cmdlen>>8)&0xFF).byteValue();
               data[14+passlen]=Integer.valueOf((cmdlen)&0xFF).byteValue();
               for(int i=0;i<cmdlen;i++)
                   data[15+passlen+i]=command.getBytes()[i];
                DatagramSocket client_socket = null;
                try {
                     client_socket= new DatagramSocket(Integer.valueOf(port));
                    InetAddress IPAddress =  InetAddress.getByName(ip);
                    DatagramPacket send_packet = new DatagramPacket(data,15+passlen+cmdlen, IPAddress, Integer.valueOf(port));
                    client_socket.send(send_packet);
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    client_socket.receive(receivePacket);
                    if(receiveData.length<13) {
                        Toast.makeText(getApplicationContext(), "Invalid Reply", Toast.LENGTH_LONG).show();
                        client_socket.close();
                        return;
                    }
                    if(!(receiveData[0]=='V'&&receiveData[1]=='C'&&
                            receiveData[2]=='M'&&receiveData[3]=='P'&&receiveData[10]=='r'))
                    {
                        Toast.makeText(getApplicationContext(), "Invalid Reply", Toast.LENGTH_LONG).show();
                        client_socket.close();
                        return;
                    }
                    int msglen=receiveData[11]*256+receiveData[12];
                    if(receiveData.length<(13+msglen))
                    {
                        Toast.makeText(getApplicationContext(), "Invalid Reply", Toast.LENGTH_LONG).show();
                        client_socket.close();
                        return;
                    }
                    EditText t2 = (EditText) findViewById(R.id.consoleEditText);
                    t2.append("\n>" + command);
                    String reply=new String(receiveData,13,msglen);
                    if(reply.length()>14&&reply.substring(0,14).equals("rcon_password "))
                    {
                        if(reply.length()>14)
                        {
                            String password=reply.substring(14);
                            passlen=password.length();
                            data[11]=Integer.valueOf((passlen>>8)&0xFF).byteValue();
                            data[12]=Integer.valueOf((passlen)&0xFF).byteValue();
                            for(int k=0;k<password.length();k++)
                                data[13+k]=password.getBytes()[k];
                            t2.append("\n");
                        }
                    }else {
                        t2.append("\n" + reply);
                    }
                    client_socket.close();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error occured: "+e, Toast.LENGTH_LONG).show();
                    if(!client_socket.isClosed())
                        client_socket.close();
                }

            }
        });
        final EditText edittext = (EditText) findViewById(R.id.cmdEdittext);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Button send=(Button)findViewById(R.id.sendbutton);
                    send.performClick();
                     return true;
                }
                return false;
            }
        });
    }
}