package com.app.chatserver;

import java.awt.Color;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.io.BufferedReader;

import java.io.IOException;

import java.io.InputStreamReader;

import java.io.OutputStream;

import java.net.Socket;

import java.net.UnknownHostException;

import javax.swing.JButton;

import javax.swing.JFrame;

import javax.swing.JList;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.border.TitledBorder;

public class MainChat extends JFrame implements ActionListener, Runnable {
	JList<String> roomInfo, memInfo, waitInfo;
	JScrollPane roomInfoList, roomMemList, waitMemList;
	JButton createBtn, enterBtn, exitBtn, inviteBtn;
	JPanel panel;
	ChatModel chatModel;
	// ���� ����°�ü
	BufferedReader in;
	OutputStream out;
	String selectRoom, selectNick;
	
	public MainChat() {
		setTitle("����");
		chatModel = new ChatModel();
		roomInfo = new JList<String>();
		roomInfo.setBorder(new TitledBorder("������"));
		roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String str = roomInfo.getSelectedValue();
				if (str == null)
					return;
				System.out.println("STR=" + str);
				selectRoom = str.substring(0, str.indexOf("-"));
				// "�ڹٹ�" <---- substring(0,3)
				// ��ȭ�� ���� �ο�����
				sendMsg("��ȭ���ο�����|" + selectRoom);
			}
		});
		memInfo = new JList<String>();
		memInfo.setBorder(new TitledBorder("�ο�����"));
		waitInfo = new JList<String>();
		waitInfo.setBorder(new TitledBorder("��������"));
		roomInfoList = new JScrollPane(roomInfo);
		roomMemList = new JScrollPane(memInfo);
		waitMemList = new JScrollPane(waitInfo);
		createBtn = new JButton("�� �����");
		enterBtn = new JButton("�� ����");
		exitBtn = new JButton("������"); 
		panel = new JPanel();
		roomInfoList.setBounds(10, 10, 300, 300);
		roomMemList.setBounds(320, 10, 150, 300);
		waitMemList.setBounds(10, 320, 300, 130);
		createBtn.setBounds(320, 330, 150, 30);
		enterBtn.setBounds(320, 370, 150, 30);
		exitBtn.setBounds(320, 410, 150, 30);
		panel.setLayout(null);
		//panel.setBackground(Color.orange);		
		panel.add(roomInfoList);
		panel.add(roomMemList);
		panel.add(waitMemList);
		panel.add(createBtn);
		panel.add(enterBtn);
		panel.add(exitBtn);
		add(panel);
		setBounds(300, 200, 500, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// socket ����
		connect();
		new Thread(this).start();
		
		sendMsg("Ŭ���̾�Ʈ����|");// (����)���� �˸�
		String nickName = JOptionPane.showInputDialog(this, "�г���:");
		sendMsg("�г���|" + nickName);
		eventUp();
	}// ������

	private void eventUp() {// �̺�Ʈ�ҽ�-�̺�Ʈó���� ����
		// ����(MainChat)
		createBtn.addActionListener(this);
		enterBtn.addActionListener(this);
		exitBtn.addActionListener(this);
		//inviteBtn.addActionListener(this);
		
		
		// ��ȭ��(ChatModel)
		chatModel.textField.addActionListener(this);
		chatModel.outBtn.addActionListener(this);
		chatModel.inviteBtn.addActionListener(this);
		chatModel.broadcastBtn.addActionListener(this);
		chatModel.unicastBtn.addActionListener(this);
		chatModel.exitBtn.addActionListener(this);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent e) {
		Object object = e.getSource();
		if (object == createBtn) {// �游��� ��û
			String title = JOptionPane.showInputDialog(this, "������:");
			// �������� �������� ����
			sendMsg("������|" + title);
			chatModel.setTitle("ä�ù�-[" + title + "]");
			sendMsg("��ȭ���ο�����|");// ��ȭ�泻 �ο����� ��û
			setVisible(false);
			chatModel.setVisible(true); // ��ȭ���̵�
		} else if (object == enterBtn) {// ����� ��û
			if (selectRoom == null) {
				JOptionPane.showMessageDialog(this, "���� ����!!");
				return;
			}
			
			sendMsg("��ȭ������|" + selectRoom);
			sendMsg("��ȭ���ο�����|");// ��ȭ�泻 �ο����� ��û
			setVisible(false);
			chatModel.outBtn.setVisible(false);
			chatModel.inviteBtn.setVisible(false);
			chatModel.setVisible(true);
		} 
		
		else if(object == inviteBtn) {
			if (selectNick == null) {
				JOptionPane.showMessageDialog(this, "�ʴ��� ����� ����!!");
				return;
			}
			sendMsg("�ʴ��ϱ�|" + selectNick);
			//sendMsg("��ȭ������|" + selectRoom);
			sendMsg("��ȭ���ο�����|");// ��ȭ�泻 �ο����� ��û
			setVisible(false);
			chatModel.outBtn.setVisible(false);
			chatModel.inviteBtn.setVisible(false);
			chatModel.setVisible(true);
		} 
		
		
		else if (object == chatModel.exitBtn) {// ��ȭ�� ������ ��û
			sendMsg("��ȭ������|");
			chatModel.setVisible(false);
			setVisible(true);
		} else if(object == chatModel.outBtn) { // �����ϱ�
			if (chatModel.selectNick == null) {
				JOptionPane.showMessageDialog(this, "�������� ��� ����!!");
				return;
			}
			sendMsg("����|" + chatModel.selectNick);
//			chatModel.setVisible(false);
//			setVisible(true);
		} 
		
		else if(object == chatModel.inviteBtn) { // �ʴ��ϱ�
			sendMsg("�ʴ�|");
			
			//setVisible(true);
		}
		
		
		else if (object == chatModel.textField) {// (TextField�Է�)�޽��� ������ ��û
			String msg = chatModel.textField.getText();
			if (msg.length() > 0) {
				sendMsg("�޽���������|" + msg);
				chatModel.textField.setText("");
			}
		} else if(object == chatModel.broadcastBtn) { // ��� ��ȭ�濡 ��ġ��
			String msg = chatModel.textField.getText();
			if(msg.length() > 0) {
				sendMsg("��ġ��|" + msg);
				chatModel.textField.setText("");
			}
		} else if(object == chatModel.unicastBtn) { // Ư�� ����ڿ��� �ӼӸ��ϱ�
			if (chatModel.selectNick == null) {
				JOptionPane.showMessageDialog(this, "�ӼӸ� ��� ����!!");
				return;
			}
			String msg = chatModel.textField.getText();
			if(msg.length() > 0) {
				sendMsg("�ӼӸ�|" + chatModel.selectNick+"|"+msg);
				chatModel.textField.setText("");
			}
		}
		else if (object == exitBtn) {// ������(���α׷�����) ��û
			System.exit(0);// ���� �������α׷� �����ϱ�
		}
	}

	// �������� ��û
	public void connect() {
		try {
			Socket s = new Socket("localhost", 5000);// ����õ�
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = s.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsg(String msg) {
		try {
			out.write((msg + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ������ ��ü ����
	public void run() {
		try {
			while (true) {
				String msg = in.readLine();
				String msgs[] = msg.split("\\|");
				String protocol = msgs[0];
				switch (protocol) {
				case "�޽���������":
					chatModel.textArea.append(msgs[1] + "\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "��ġ��" :
					chatModel.textArea.append(msgs[1] + "\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "�ӼӸ�" :
					chatModel.textArea.append(msgs[1] + "\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "������":// �游���
					// �������� List�� �Ѹ���
					if (msgs.length > 1) {
						// ������ ���� �Ѱ� �̻��̾����� ����
						String roomNames[] = msgs[1].split(",");
						roomInfo.setListData(roomNames);
					}
					break;
					
				case "��ȭ���ο�����":// (���ǿ���) ��ȭ�� �ο�����
					String roomInwons[] = msgs[1].split(",");
					memInfo.setListData(roomInwons);
					break;
					
				case "��ȭ���ο�����":// (��ȭ�濡��) ��ȭ�� �ο�����
					String myRoomInwons[] = msgs[1].split(",");
					chatModel.memList.setListData(myRoomInwons);
					break;

				case "�����ο�����":// ���� �ο�����
					String waitNames[] = msgs[1].split(",");
					waitInfo.setListData(waitNames);
					break;

				case "��ȭ������":// ��ȭ�� ����
					chatModel.textArea.append("=========[" + msgs[1] + "]�� �����Ͽ����ϴ�.=========\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;

				case "��ȭ������":// ��ȭ�� ����
					chatModel.textArea.append("=========[" + msgs[1] + "]�� �����Ͽ����ϴ�.=========\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "����" : // ����
					chatModel.textArea.append("=========[" + msgs[1] + "]���� ���� ���� ���߽��ϴ�.=========\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
				
				case "�����ϱ�" :
					System.out.println(msg.toString());
					//setDefaultCloseOperation(MainChat.EXIT_ON_CLOSE);
					//chatModel.setVisible(false);
					//MainChat mainChat = new MainChat();
					break;
					
				case "�ʴ�" : // �ʴ�
					JFrame f = new JFrame("�ʴ�"); 
					panel = new JPanel();
					waitInfo = new JList<String>();
					if (msgs.length > 1) {
						String inviteNames[] = msgs[1].split(",");
						waitInfo.setListData(inviteNames);
					}
					waitInfo.setBorder(new TitledBorder("��������"));
					waitInfo.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							String str = waitInfo.getSelectedValue();
							if (str == null)
								return;
							System.out.println("nickname=" + str);
							// "�ڹٹ�" <---- substring(0,3)
							// ��ȭ�� ���� �ο�����
							selectNick = str;
						}
					});
					waitMemList = new JScrollPane(waitInfo);
					inviteBtn = new JButton("�ʴ��ϱ�");
					waitMemList.setBounds(10, 10, 280, 200);
					inviteBtn.setBounds(120, 210, 100,50);
					panel.add(waitMemList);
					panel.add(inviteBtn);
					panel.setLayout(null);
					f.add(panel);
					f.setBounds(300, 200, 400, 300);
					f.setVisible(true);
					eventUp();
					break;
					
				case "�ʴ��ϱ�" :
					System.out.println(msg.toString());
					
					break;
					
				
				case "��Ÿ��Ʋ":// ������ ���� Ÿ��Ʋ ���� ���
					chatModel.setTitle("ä�ù�-[" + msgs[1] + "]");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// run

	public static void main(String[] args) {
		new MainChat();
	}
}

