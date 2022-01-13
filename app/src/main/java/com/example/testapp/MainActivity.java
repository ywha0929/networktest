package com.example.testapp;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {
    EditText editText0;
    EditText editText1;
    EditText editText2;
    TextView textView;
    background_thread thread;
    Socket sock;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText0 = findViewById(R.id.editText0);
        editText1 = findViewById(R.id.editText1);
        textView = findViewById(R.id.textView5);
        Button button = findViewById(R.id.button0);
        editText0.setText(NetUtils.getIPAddress(true));
        editText1.setText("5001");
        editText2 = findViewById(R.id.editText2);
        Button button2 = findViewById(R.id.button2);

        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new sendmsg_thread().start();
            }
        });

        textView.setMovementMethod(new ScrollingMovementMethod());
        button.setOnClickListener(new View.OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               thread = new background_thread();
               thread.start();
           }
        });
    }

    public void startServer()
    {
        try
        {
            int portnumber = parseInt(editText1.getText().toString());
            ServerSocket server = new ServerSocket(portnumber);
            printServerLog("Server has Started at port " + portnumber);

            InputStream instream;
            while(true)
            {
                sock = server.accept();
                InetAddress clientHost = sock.getInetAddress();
                int clientPort = sock.getPort();
                printServerLog("Connected with client : "+clientHost + "/"+clientPort);
                instream = sock.getInputStream();
                if(sock.isConnected() == true)
                    break;
            }
            String input = "";
            while(true)
            {

                byte[] buffer = new byte[100];
                instream.read(buffer);


                input = "";
                for(int i = 0; buffer[i]!=0; i++)
                {
                    input = input.concat(Character.toString((char)buffer[i]));

                }

                printServerLog("Received data from client : " +input.toString());
                if(input.toString().equals("EnD")==true)
                {
                    sock.close();
                    printServerLog("connection closed from client");
                    break;
                }

            }


        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public void sendmsg() throws IOException
    {
        String text = editText2.getText().toString();
        byte[] buffer = new byte[100];
        buffer = text.getBytes(StandardCharsets.US_ASCII);
        OutputStream outputstream;
        outputstream = sock.getOutputStream();
        outputstream.write(buffer);
        printServerLog("Sent data from server : "+text);
        handler.post(new Runnable() {
            @Override
            public void run() {
                editText2.setText("");
            }
        });
    }
    public void printServerLog(final String data)
    {
        Log.d("MainActivity",data);
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                textView.append(data+"\n");
            }
        });
    }
    class background_thread extends Thread
    {
        public void run()
        {
            startServer();
        }

    }
    class sendmsg_thread extends Thread
    {
        public void run()
        {
            try
            {
                sendmsg();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
