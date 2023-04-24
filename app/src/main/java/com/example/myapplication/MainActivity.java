package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) { StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); StrictMode.setThreadPolicy(policy); }
        Button button = (Button) findViewById(R.id.supabutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText t = (EditText) findViewById(R.id.edittext);
                String add = t.getText().toString();
                if (add.contains(":"))
                {
                    int i=add.indexOf(':');
                    String ip=add.substring(0,i);
                    String port=add.substring(i+1);
                    if(port.matches("-?\\d+"))
                    {
                        //Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_LONG).show();
                        try {
                            DatagramSocket client_socket = new DatagramSocket(Integer.valueOf(port));
                            InetAddress IPAddress =  InetAddress.getByName(ip);
                            byte[] send_data=new byte[512];
                            send_data[0]='V';send_data[1]='C';send_data[2]='M';send_data[3]='P';
                            String[] j=ip.split("\\.");
                            if(j.length<4)
                            {
                                Toast.makeText(getApplicationContext(), "Incorrect IP. "+j.length, Toast.LENGTH_LONG).show();
                                client_socket.close();
                                return;
                            }
                            send_data[4]= (Integer.valueOf(j[0])).byteValue();
                            send_data[5]= (Integer.valueOf(j[1])).byteValue();
                            send_data[6]= (Integer.valueOf(j[2])).byteValue();
                            send_data[7]= (Integer.valueOf(j[3])).byteValue();
                            int l=Integer.valueOf(port);
                            send_data[8]=Integer.valueOf(l&0xFF).byteValue();
                            send_data[9]=Integer.valueOf((l>>8)&0xFF).byteValue();
                            send_data[10]='r';
                            EditText t2 = (EditText) findViewById(R.id.edittext2);
                            String password=t2.getText().toString();
                            int len=password.length();
                            send_data[11]=Integer.valueOf((len>>8)&0xFF).byteValue();
                            send_data[12]=Integer.valueOf((len)&0xFF).byteValue();

                            for(int k=0;k<password.length();k++)
                                send_data[13+k]=password.getBytes()[k];
                            send_data[13+len]=0;
                            send_data[14+len]=1;
                            send_data[15+len]='L';

                            //Toast.makeText(getApplicationContext(), "Sending packet", Toast.LENGTH_LONG).show();
                            DatagramPacket send_packet = new DatagramPacket(send_data,16+len, IPAddress, Integer.valueOf(port));
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
                            String reply=new String(receiveData,13,msglen);
                            if(reply.equals("OK"))
                            {
                                //Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_LONG).show();
                                Intent myIntent = new Intent(MainActivity.this, MainActivity2.class);
                                myIntent.putExtra("key", send_data);
                                myIntent.putExtra("passlen", len);
                                myIntent.putExtra("ip", ip);
                                myIntent.putExtra("port", port);
                                client_socket.close();
                                MainActivity.this.startActivity(myIntent);
                            }else if(reply.equals("NO")){
                                Toast.makeText(getApplicationContext(), "Incorrect Password", Toast.LENGTH_LONG).show();
                                client_socket.close();
                                return;
                            }else {
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                                client_socket.close();
                                return;
                            }
                            client_socket.close();
                        }catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(), "Error occured: "+e.toString(), Toast.LENGTH_LONG).show();

                        }

                    }
                }else
                {
                    Toast.makeText(getApplicationContext(), "Give address in IP:Port format", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
