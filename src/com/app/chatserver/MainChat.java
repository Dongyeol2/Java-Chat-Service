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
	// 소켓 입출력객체
	BufferedReader in;
	OutputStream out;
	String selectRoom, selectNick;
	
	public MainChat() {
		setTitle("대기실");
		chatModel = new ChatModel();
		roomInfo = new JList<String>();
		roomInfo.setBorder(new TitledBorder("방정보"));
		roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String str = roomInfo.getSelectedValue();
				if (str == null)
					return;
				System.out.println("STR=" + str);
				selectRoom = str.substring(0, str.indexOf("-"));
				// "자바방" <---- substring(0,3)
				// 대화방 내의 인원정보
				sendMsg("대화방인원보기|" + selectRoom);
			}
		});
		memInfo = new JList<String>();
		memInfo.setBorder(new TitledBorder("인원정보"));
		waitInfo = new JList<String>();
		waitInfo.setBorder(new TitledBorder("대기실정보"));
		roomInfoList = new JScrollPane(roomInfo);
		roomMemList = new JScrollPane(memInfo);
		waitMemList = new JScrollPane(waitInfo);
		createBtn = new JButton("방 만들기");
		enterBtn = new JButton("방 참가");
		exitBtn = new JButton("나가기"); 
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
		
		// socket 생성
		connect();
		new Thread(this).start();
		
		sendMsg("클라이언트접속|");// (대기실)접속 알림
		String nickName = JOptionPane.showInputDialog(this, "닉네임:");
		sendMsg("닉네임|" + nickName);
		eventUp();
	}// 생성자

	private void eventUp() {// 이벤트소스-이벤트처리부 연결
		// 대기실(MainChat)
		createBtn.addActionListener(this);
		enterBtn.addActionListener(this);
		exitBtn.addActionListener(this);
		//inviteBtn.addActionListener(this);
		
		
		// 대화방(ChatModel)
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
		if (object == createBtn) {// 방만들기 요청
			String title = JOptionPane.showInputDialog(this, "방제목:");
			// 방제목을 서버에게 전달
			sendMsg("방제목|" + title);
			chatModel.setTitle("채팅방-[" + title + "]");
			sendMsg("대화방인원정보|");// 대화방내 인원정보 요청
			setVisible(false);
			chatModel.setVisible(true); // 대화방이동
		} else if (object == enterBtn) {// 방들어가기 요청
			if (selectRoom == null) {
				JOptionPane.showMessageDialog(this, "방을 선택!!");
				return;
			}
			
			sendMsg("대화방입장|" + selectRoom);
			sendMsg("대화방인원정보|");// 대화방내 인원정보 요청
			setVisible(false);
			chatModel.outBtn.setVisible(false);
			chatModel.inviteBtn.setVisible(false);
			chatModel.setVisible(true);
		} 
		
		else if(object == inviteBtn) {
			if (selectNick == null) {
				JOptionPane.showMessageDialog(this, "초대할 사용자 선택!!");
				return;
			}
			sendMsg("초대하기|" + selectNick);
			//sendMsg("대화방입장|" + selectRoom);
			sendMsg("대화방인원정보|");// 대화방내 인원정보 요청
			setVisible(false);
			chatModel.outBtn.setVisible(false);
			chatModel.inviteBtn.setVisible(false);
			chatModel.setVisible(true);
		} 
		
		
		else if (object == chatModel.exitBtn) {// 대화방 나가기 요청
			sendMsg("대화방퇴장|");
			chatModel.setVisible(false);
			setVisible(true);
		} else if(object == chatModel.outBtn) { // 강퇴하기
			if (chatModel.selectNick == null) {
				JOptionPane.showMessageDialog(this, "강제퇴장 대상 선택!!");
				return;
			}
			sendMsg("강퇴|" + chatModel.selectNick);
//			chatModel.setVisible(false);
//			setVisible(true);
		} 
		
		else if(object == chatModel.inviteBtn) { // 초대하기
			sendMsg("초대|");
			
			//setVisible(true);
		}
		
		
		else if (object == chatModel.textField) {// (TextField입력)메시지 보내기 요청
			String msg = chatModel.textField.getText();
			if (msg.length() > 0) {
				sendMsg("메시지보내기|" + msg);
				chatModel.textField.setText("");
			}
		} else if(object == chatModel.broadcastBtn) { // 모든 대화방에 외치기
			String msg = chatModel.textField.getText();
			if(msg.length() > 0) {
				sendMsg("외치기|" + msg);
				chatModel.textField.setText("");
			}
		} else if(object == chatModel.unicastBtn) { // 특정 사용자에게 귓속말하기
			if (chatModel.selectNick == null) {
				JOptionPane.showMessageDialog(this, "귓속말 대상 선택!!");
				return;
			}
			String msg = chatModel.textField.getText();
			if(msg.length() > 0) {
				sendMsg("귓속말|" + chatModel.selectNick+"|"+msg);
				chatModel.textField.setText("");
			}
		}
		else if (object == exitBtn) {// 나가기(프로그램종료) 요청
			System.exit(0);// 현재 응용프로그램 종료하기
		}
	}

	// 서버연결 요청
	public void connect() {
		try {
			Socket s = new Socket("localhost", 5000);// 연결시도
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

	// 스레드 객체 실행
	public void run() {
		try {
			while (true) {
				String msg = in.readLine();
				String msgs[] = msg.split("\\|");
				String protocol = msgs[0];
				switch (protocol) {
				case "메시지보내기":
					chatModel.textArea.append(msgs[1] + "\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "외치기" :
					chatModel.textArea.append(msgs[1] + "\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "귓속말" :
					chatModel.textArea.append(msgs[1] + "\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "방제목":// 방만들기
					// 방정보를 List에 뿌리기
					if (msgs.length > 1) {
						// 개설된 방이 한개 이상이었을때 실행
						String roomNames[] = msgs[1].split(",");
						roomInfo.setListData(roomNames);
					}
					break;
					
				case "대화방인원보기":// (대기실에서) 대화방 인원정보
					String roomInwons[] = msgs[1].split(",");
					memInfo.setListData(roomInwons);
					break;
					
				case "대화방인원정보":// (대화방에서) 대화방 인원정보
					String myRoomInwons[] = msgs[1].split(",");
					chatModel.memList.setListData(myRoomInwons);
					break;

				case "대기실인원정보":// 대기실 인원정보
					String waitNames[] = msgs[1].split(",");
					waitInfo.setListData(waitNames);
					break;

				case "대화방입장":// 대화방 입장
					chatModel.textArea.append("=========[" + msgs[1] + "]님 입장하였습니다.=========\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;

				case "대화방퇴장":// 대화방 퇴장
					chatModel.textArea.append("=========[" + msgs[1] + "]님 퇴장하였습니다.=========\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
					
				case "강퇴" : // 강퇴
					chatModel.textArea.append("=========[" + msgs[1] + "]님이 강제 퇴장 당했습니다.=========\n");
					chatModel.textArea.setCaretPosition(chatModel.textArea.getText().length());
					break;
				
				case "강퇴하기" :
					System.out.println(msg.toString());
					//setDefaultCloseOperation(MainChat.EXIT_ON_CLOSE);
					//chatModel.setVisible(false);
					//MainChat mainChat = new MainChat();
					break;
					
				case "초대" : // 초대
					JFrame f = new JFrame("초대"); 
					panel = new JPanel();
					waitInfo = new JList<String>();
					if (msgs.length > 1) {
						String inviteNames[] = msgs[1].split(",");
						waitInfo.setListData(inviteNames);
					}
					waitInfo.setBorder(new TitledBorder("대기실정보"));
					waitInfo.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							String str = waitInfo.getSelectedValue();
							if (str == null)
								return;
							System.out.println("nickname=" + str);
							// "자바방" <---- substring(0,3)
							// 대화방 내의 인원정보
							selectNick = str;
						}
					});
					waitMemList = new JScrollPane(waitInfo);
					inviteBtn = new JButton("초대하기");
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
					
				case "초대하기" :
					System.out.println(msg.toString());
					
					break;
					
				
				case "방타이틀":// 개설된 방의 타이틀 제목 얻기
					chatModel.setTitle("채팅방-[" + msgs[1] + "]");
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

