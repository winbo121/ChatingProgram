package chat;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class TcpServer extends JFrame {

	JTextArea jta_display;
	JList<String> jlist_user_list;
	JTextField jtf_user_count;
	
	Font F=new Font("굴림체",Font.BOLD,20);

	ServerSocket server;
	
	List<ReadThread> socketList=new ArrayList<ReadThread>();
	//socket
	
	List<String> userList =new ArrayList<String>();
	//human
	
	
	
	Object sysnObj=new Object();
	
	public TcpServer() throws Exception {
	
		super("타이틀");
		
		
		
		init_display(); //Center
		
		init_user_list(); //East
		
		init_user_count(); //South
		
		init_server();

		setLocation(400, 200);
	
		
		pack();


		setVisible(true);

	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		
		
		
	}

	
	private void init_server() throws Exception {
		server =new ServerSocket(8000);
		
		
		new Thread() {
			
			
			public void run() {
				
				try {
					while(true){
					
						Socket clild =server.accept();
					
						ReadThread rt=new ReadThread(clild);
						socketList.add(rt);
						rt.start();
						
					
					}
					
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
				
			};
			
		}.start();
		jta_display.append("server wait");
		
	}
	
	
	
	
	
	


	private void init_display() {
		
		jta_display =new JTextArea();
		
		jta_display.setEditable(false);
		
		jta_display.setFont(F);
		
		JScrollPane jsp=new JScrollPane(jta_display);
		
		jsp.setPreferredSize(new Dimension(400,400));
		
		this.add(jsp,BorderLayout.CENTER);
		
		
		
		
		
	}


	private void init_user_list() {
		jlist_user_list =new JList<String>();
		
		JScrollPane jsp=new JScrollPane(jlist_user_list );
		
		jsp.setPreferredSize(new Dimension(120,400));
		
		this.add(jsp,BorderLayout.EAST);
		
	}

	
	private void init_user_count() {
		
		JPanel p=new JPanel(new GridLayout(1,3));
		
		JLabel j1=new JLabel("접속자수:",JLabel.RIGHT);
		jtf_user_count =new JTextField("0",JLabel.LEFT);
		JLabel j2=new JLabel("명",JLabel.LEFT);
		
		p.add(j1);
		
		p.add(jtf_user_count);
		
		p.add(j2);	
		
		
		
		this.add(p,BorderLayout.SOUTH);
		
	}

class ReadThread extends Thread{
		
		
		Socket child;
		
		BufferedReader br=null;
		
		public ReadThread(Socket child) throws Exception {
			super();
			this.child = child;
			
			
			InputStreamReader isr=new InputStreamReader(child.getInputStream());
			br=new BufferedReader(isr);
		}

		@Override
		public void run() {
			
			while(true) {
				String data;
				try {
				data=br.readLine();
				
				
				if(data==null) {
					System.out.println("종료");
					break;
				}
				}catch(Exception e) {
					
					break;
				}
				
				int position = jta_display.getDocument().getLength();
				
				jta_display.setCaretPosition(position); //scroll fix
				
				
				String [] messageArr = data.split("#");
				//messageArr={"IN","김재훈"}
				
				if(messageArr[0].equals("IN")) { //COMING
					
					synchronized(sysnObj) {
					
						userList.add(messageArr[1]);
						
						
						jta_display.append(data+"\n");
						
						my_display_user_count(); //humancount reset
						
						my_user_List();//human list reset
						
						try {
							my_send_user_list();
						} catch (Exception e) {
						
							e.printStackTrace();
						}
						
						//������鿡�� ������ �޼��� ����
						try {
							my_send_message_all(data+"\n");
						} catch (Exception e) {
							
							e.printStackTrace();
						}
						
					}
				}
				else if(messageArr[0].equals("MSG")) {
					
					synchronized(sysnObj) {
					try {
						my_send_message_all(data+"\n");
						jta_display.append(data+"\n");
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				}
					}
				
				
				
			}
			String del_nickname;
			synchronized(sysnObj) { //out
			
			int del_index=socketList.indexOf(this);
			
			 del_nickname=userList.get(del_index);
			
			socketList.remove(this);
			
			userList.remove(del_index);
			
			my_display_user_count(); //humancount reset
			my_user_List(); //human list reset
			try {
				my_send_user_list();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			}
			String send_data=String.format("OUT#%s\n", del_nickname);
			try {
				my_send_message_all(send_data);
				jta_display.append(send_data+"\n");
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
		}
		

	}
		private void my_display_user_count() {
			jtf_user_count.setText(socketList.size()+"");	
		}


		private void my_user_List() {
			String [] user_array=new String[userList.size()];
			userList.toArray(user_array); //LIST -> ARRAY
			jlist_user_list.setListData(user_array);
		}
		
		private void my_send_user_list() throws Exception {
			
			StringBuffer sb=new StringBuffer("LIST#");
			for(String nickname:userList) {
				sb.append(nickname);
				sb.append("#");
			}
			sb.append("\n");
			String user_list=sb.toString();
			
			
			//���� ������ ��� Ŭ���̾�Ʈ���� ������
			my_send_message_all(user_list);
			
			
		}

		private void my_send_message_all(String message) throws Exception {
			//for(ReadThread rt: socketList) { //���ϸ���Ʈ�ȿ� �ִ� ���  ����� ���� ������ ������
				
				for(int i=0; i<socketList.size(); i++) {
					
				ReadThread rt=socketList.get(i);
				rt.child.getOutputStream().write(message.getBytes());
				
				
				
				
				
			}
			
		}


		public static void main(String[] args) throws Exception {
	
		new TcpServer();
		}




	
}
