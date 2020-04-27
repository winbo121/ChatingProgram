package chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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



public class TcpClient1 extends JFrame {

	JTextArea jta_display;
	JList<String> jlist_user_list;
	JTextField jtf_message;
	JButton jbt_connect;
	boolean beConnect=false;
	
	Font F=new Font("굴림체",Font.BOLD,20);
	
	Socket client;
	String nickname="김재훈";
	
	//Ű�̺�Ʈ
	
	
	
	public TcpClient1() throws Exception {
	
		super("타이틀");
		
		init_display(); //Center
	
		init_user_list(); //East
		
		init_input(); // South
		

		setLocation(400, 200);
	
		
		pack();


		setVisible(true);

	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	
		
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
	

	private void init_input() {

		JPanel p=new JPanel(new BorderLayout());
		jtf_message =new JTextField();
		jbt_connect =new JButton("연결");
		
		p.add(jtf_message,"Center");
		p.add(jbt_connect,"East");
		
		this.add(p,BorderLayout.SOUTH);
		
		
		
		
		jbt_connect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				beConnect =!beConnect;
				
				
				jbt_connect.setText(beConnect?"끈기":"연결");
				
				if(beConnect) {
					
					try {
						client =new Socket("localhost",8000);
						
						String send_data=String.format("IN#%s\n", nickname);
						client.getOutputStream().write(send_data.getBytes());
						my_read_message();
						
						
					} catch (Exception e1) {
						beConnect=false;
						
						my_display_message("연결실패");
						
					}
				}
				else {
					try {
						client.close();
					} catch (IOException e1) {
						
						e1.printStackTrace();
					}
					
				}
				
			}
			
			

			
		});
		
		jtf_message.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					try {
						my_send_message();
					} catch (Exception e1) {
						
						e1.printStackTrace();
					}
				}
				
			}
			
		});
		
		
	}
	
	protected void my_send_message() throws Exception {
		
		if(beConnect==false) {
			return;
			
		}
		
		String message=jtf_message.getText().trim();
		
	
		//���۵����� ����
		String send_data=String.format("MSG#%s#%s\n", nickname,message);
		
		client.getOutputStream().write(send_data.getBytes());
		jtf_message.setText("");
	}





	private void my_read_message() throws Exception {
		
		InputStreamReader isr=new InputStreamReader(client.getInputStream());
		BufferedReader br=new BufferedReader(isr);
		
		//���� ������
		new Thread() {
			@Override
			public void run() {
				
				while(true) {
					String readStr;
					try {
						readStr=br.readLine();
						if(readStr==null) {
							break;
						}
						//readStr="IN#ȫ�浿"
						//readStr="OUT#ȫ�浿"
						//readStr="MSG#ȫ�浿#������
						
						String []msg_array=readStr.split("#");
						
						if(msg_array[0].equals("IN")) {
							String msg=String.format("[%s]님 입장", msg_array[1]);
							my_display_message(msg);
						}
						else if(msg_array[0].equals("OUT")) {
							String msg=String.format("[%s]님퇴장", msg_array[1]);
							my_display_message(msg);
						}
						else if(msg_array[0].equals("MSG")) {
							String msg=String.format("[%s]님 메세지:%s", msg_array[1],msg_array[2]);	
							my_display_message(msg);
						}
						else if(msg_array[0].equals("LIST")) {
							my_display_user_list(readStr);
						}
						else if(msg_array[0].equals("DRAW")) {
							
						}
												
						
					} catch (IOException e) {
						
						break;
					
					}
					
				}
				String [] user_array=new String[0];
				jlist_user_list.setListData(user_array);
				
				
			}

			
		}.start();
		
	}

	
	
	protected void my_display_user_list(String readStr) {
		
		readStr=readStr.replaceAll("LIST#", "");
		//ȫ�浿#ȫ�ǵ�2#
		jlist_user_list.setListData(readStr.split("#"));
	}





	protected void my_display_message(String message) {
		jta_display.append(message+"\n");
		
		int position = jta_display.getDocument().getLength();
		jta_display.setCaretPosition(position);
		
	}





	public static void main(String[] args) throws Exception {
	
		new TcpClient1();
	}
}