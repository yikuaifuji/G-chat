package com.example.g_chat;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity
{
	ConnectThread connectThread;
	IOThread ioThread;
	EditText sentMessage, receivedMessage;
	Button send, connect;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sentMessage = (EditText)findViewById(R.id.sentMessage);
		receivedMessage = (EditText)findViewById(R.id.receivedMessage);
		send = (Button)findViewById(R.id.send);
		connect = (Button)findViewById(R.id.connect);
		Handler handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if ( msg.what == 0x2 )
				{
					receivedMessage.setText(msg.obj.toString());
				}
			}
		};
		connectThread = new ConnectThread(handler);
		connect.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				new Thread(connectThread).start();		
			}
		});
		
		
		send.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub	
				Message msg = new Message();
				msg.what = 0x1;
				msg.obj = sentMessage.getText();
				connectThread.ioThread.recvHandler.sendMessage(msg);
			}
		});
		
	}
}


class ConnectThread implements Runnable
{
	Socket sock;
	String str;
	InputStream in;
	byte bt[];
	IOThread ioThread;
	Handler handler;
	public ConnectThread(Handler handler)
	{
		this.handler = handler;
	}
	public void run()
	{
		try
		{
			sock = new Socket("192.168.0.100", 30000);
			in = sock.getInputStream();
			bt = new byte[256];
			ioThread = new IOThread(handler, sock);
			new Thread(ioThread).start();
		} 
		catch (Exception e)
		{
		}
	}
}

class IOThread implements Runnable
{
	Handler handler, recvHandler;
	InputStream in;
	OutputStream out;
	String str;
	byte rd[], wt[];
	Socket sock;
	public IOThread(Handler handler, Socket sock)
	{
		this.handler = handler;
		this.sock = sock;
	}
	public void run()
	{
		new Thread()
		{
			public void run()
			{
				Looper.prepare();
				recvHandler = new Handler()
				{
					public void handleMessage(Message msg)
					{
						if ( msg.what == 0x1 )
						{
							try
							{
								wt = new byte[256];
								wt = msg.obj.toString().getBytes();
								out = sock.getOutputStream();
								out.write(wt);
							}
							catch (Exception e)
							{
							}
						}
					}
				};
				Looper.loop();
			}
		}.start();
		
		try
		{
			in = sock.getInputStream();
			rd = new byte[256];
			while ( in.read(rd) != 0 )
			{
				str = new String(rd);
				Message msg = new Message();
				msg.what = 0x2;
				msg.obj = str;
				handler.sendMessage(msg);
			}
		}
		catch (Exception e)
		{
		}
	}
}



