package com.app.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class Service extends Thread {
	// Service == 접속 클라이언트 한명!!
	RoomVO RoomVO;// 클라이언트가 입장한 대화방
	// 소켓관련 입출력서비스
	BufferedReader in;
	OutputStream out;
	List<Service> allVO;// 모든 사용자(대기실사용자 + 대화방사용자)
	List<Service> waitVO;// 대기실 사용자
	List<RoomVO> roomVO;// 개설된 대화방 Room-vs(Vector) : 대화방사용자
	Socket socket;
	String nickName;

	public Service(Socket socket, Server server) {
		allVO = server.allVO;
		waitVO = server.waitVO;
		roomVO = server.roomVO;
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// 생성자

	@Override
	public void run() {
		try {
			while (true) {
				String msg = in.readLine();// 클라이언트의 모든 메시지를 받기
				if (msg == null)
					return; // 비정상적인 종료
				if (msg.trim().length() > 0) {
					System.out.println("from Client: " + msg + ":" +
							socket.getInetAddress().getHostAddress());
					// 서버에서 상황을 모니터!!
					String msgs[] = msg.split("\\|");
					String protocol = msgs[0];
					switch (protocol) {
					
					case "클라이언트접속": // 대기실 접속
						allVO.add(this);// 전체사용자에 등록
						waitVO.add(this);// 대기실사용자에 등록
						break;
						
					case "닉네임": // 닉네임 입력
						nickName = msgs[1];
						// 최초 대화명 입력했을때 대기실의 정보를 출력
						messageWait("방제목|" + getRoomInfo());
						messageWait("대기실인원정보|" + getWaitMem());
						break;
						
					case "방제목": // 방만들기 (대화방 입장)
						RoomVO = new RoomVO();
						RoomVO.title = msgs[1];// 방제목
						RoomVO.count = 1;
						RoomVO.boss = nickName;
						roomVO.add(RoomVO);
						// 대기실----> 대화방 이동!!
						waitVO.remove(this);
						RoomVO.userVO.add(this);
						messageRoom("대화방입장|" + nickName);// 방인원에게 입장 알림
						// 대기실 사용자들에게 방정보를 출력
						// 예) 대화방명:JavaLove
						// -----> roomInfo(JList) : JavaLove--1
						messageWait("방제목|" + getRoomInfo());
						messageWait("대기실인원정보|" + getWaitMem());
						break;
						
					case "대화방인원보기": // (대기실에서) 대화방 인원정보
						sendMsg("대화방인원보기|" + getRoomMem(msgs[1]));
						break;
						
					case "대화방인원정보": // (대화방에서) 대화방 인원정보
						messageRoom("대화방인원정보|" + getRoomMem());
						break;

					case "대화방입장": // 방들어가기 (대화방 입장) ----> msgs[] = {"200","자바방"}
						for (int i = 0; i < roomVO.size(); i++) {// 방이름 찾기!!
							RoomVO r = roomVO.get(i);
							if (r.title.equals(msgs[1])) {// 일치하는 방 찾음!!
								RoomVO = r;
								RoomVO.count++;// 인원수 1증가
								break;
							}
						}
						// 대기실----> 대화방 이동!!
						waitVO.remove(this);
						RoomVO.userVO.add(this);
						messageRoom("대화방입장|" + nickName);// 방인원에게 입장 알림
						// 들어갈 방의 title전달
						sendMsg("방타이틀|" + RoomVO.title);
						messageWait("방제목|" + getRoomInfo());
						messageWait("대기실인원정보|" + getWaitMem());
						break;
					
					case "강퇴" :
						for(int i = 0; i < RoomVO.userVO.size(); i++) {
							Service user = RoomVO.userVO.get(i);
							if(user.nickName.equals(msgs[1])) { // 일치하는 닉네임 찾음
								RoomVO.count--; // 인원수 1 감소
								waitVO.add(user);
								RoomVO.userVO.remove(user);								
								messageRoom("강퇴|" + user.nickName);
								messageWait("방제목|" + getRoomInfo());
								messageWait("대기실인원정보|" + getWaitMem());
								messageRoom("대화방인원정보|" + getRoomMem());
								sendMsg("강퇴하기|" + user.socket);
								break;
							}
						}
						break;
					
					case "초대" :
						sendMsg("초대|" + getWaitMem());
						break;
					
					case "초대하기" :
						for(int i = 0; i < RoomVO.userVO.size(); i++) {
							Service user = RoomVO.userVO.get(i);
							if(user.nickName.equals(msgs[1])) {
								RoomVO.count++;
								waitVO.remove(user);
								RoomVO.userVO.add(user);
								messageRoom("초대하기|" + user.nickName);
								messageWait("방제목|" + getRoomInfo());
								messageWait("대기실인원정보|" + getWaitMem());
								messageRoom("대화방인원정보|" + getRoomMem());
								break;
							}
						}
						break;
						
						
					case "메시지보내기": // 메시지
						messageRoom("메시지보내기|[" + nickName + "] : " + msgs[1]);
						// 클라이언트에게 메시지 보내기
						break;
						
					case "외치기" : // 외기치
						messageAll("외치기|[" + nickName + "] 님의 외치기 : " + msgs[1]);
						break;
						
					case "귓속말": // 귓속말
						//messageAlone("귓속말|" + getNickname(msgs[1]));
						getNickname("귓속말|["+ nickName+"] 님의 귓속말 : " + msgs[2], msgs[1]);
						break;

					case "대화방퇴장": // 대화방 퇴장
						RoomVO.count--;// 인원수 감소
						messageRoom("대화방퇴장" + nickName);// 방인원들에게 퇴장 알림!!
						// 대화방----> 대기실 이동!!
						RoomVO.userVO.remove(this);
						waitVO.add(this);
						// 대화방 퇴장후 방인원 다시출력
						messageRoom("대화방인원정보|" + getRoomMem());
						// 대기실에 방정보 다시출력
						messageWait("방제목|" + getRoomInfo());
						break;
					}
				}
			}
		} catch (IOException e) {
			System.out.println("★");
			e.printStackTrace();
		}
	}

	public String getRoomInfo() { // 방 인원정보
		String str = "";
		for (int i = 0; i < roomVO.size(); i++) {
			RoomVO r = roomVO.get(i);
			str += r.title + "-" + r.count;
			if (i < roomVO.size() - 1)
				str += ",";
		}
		return str;
	}

	public String getRoomMem() {// 같은방의 인원정보
		String str = "";
		for (int i = 0; i < RoomVO.userVO.size(); i++) {
			Service ser = RoomVO.userVO.get(i);
			str += ser.nickName;
			if (i < RoomVO.userVO.size() - 1)
				str += ",";
		}
		return str;
	}

	public String getRoomMem(String title) {// 방제목 클릭시 방의 인원정보
		String str = "";
		for (int i = 0; i < roomVO.size(); i++) {
			// "길동,라임,주원"
			RoomVO room = roomVO.get(i);
			if (room.title.equals(title)) {
				for (int j = 0; j < room.userVO.size(); j++) {
					Service ser = room.userVO.get(j);
					str += ser.nickName;
					if (j < room.userVO.size() - 1)
						str += ",";
				}
				break;
			}
		}
		return str;
	}

	public String getWaitMem() {
		String str = "";
		for (int i = 0; i < waitVO.size(); i++) {
			Service ser = waitVO.get(i);
			str += ser.nickName;
			if (i < waitVO.size() - 1)
				str += ",";
		}
		return str;
	}

	public void messageAll(String msg) {// 전체사용자
		// 접속된 모든 클라이언트(대기실+대화방)에게 메시지 전달
		for (int i = 0; i < allVO.size(); i++) {
			Service service = allVO.get(i); 
			try {
				service.sendMsg(msg);
			} catch (IOException e) {
				allVO.remove(i--); 
				System.out.println("클라이언트 접속 끊음!!");
			}
		}
	}
	
	public void getNickname(String msgs, String nickname) { // 귓속말
		for(int i = 0; i < RoomVO.userVO.size(); i++) {
			Service user = RoomVO.userVO.get(i);
			if(user.nickName.equals(nickname)) {
				try {
					user.sendMsg(msgs);
					this.sendMsg(msgs);
				} catch (IOException e) {
					System.out.println("클라이언트 접속 끊음!!");
				}
			}
		}
	}

	public void messageWait(String msg) {// 대기실 사용자
		for (int i = 0; i < waitVO.size(); i++) {
			Service service = waitVO.get(i); 
			try {
				service.sendMsg(msg);
			} catch (IOException e) {
				waitVO.remove(i--);
				System.out.println("클라이언트 접속 끊음!!");
			}
		}
	}

	public void messageRoom(String msg) {// 대화방사용자
		for (int i = 0; i < RoomVO.userVO.size(); i++) {
			Service service = RoomVO.userVO.get(i);
			try {
				service.sendMsg(msg);
			} catch (IOException e) {
				RoomVO.userVO.remove(i--);
				System.out.println("클라이언트 접속 끊음!!");
			}
		}
	}
	

	
 
	// 클라이언트에게 메시지 전달
	public void sendMsg(String msg) throws IOException {
		out.write((msg + "\n").getBytes());
	}
}
