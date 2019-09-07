package com.app.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class Service extends Thread {
	// Service == ���� Ŭ���̾�Ʈ �Ѹ�!!
	RoomVO RoomVO;// Ŭ���̾�Ʈ�� ������ ��ȭ��
	// ���ϰ��� ����¼���
	BufferedReader in;
	OutputStream out;
	List<Service> allVO;// ��� �����(���ǻ���� + ��ȭ������)
	List<Service> waitVO;// ���� �����
	List<RoomVO> roomVO;// ������ ��ȭ�� Room-vs(Vector) : ��ȭ������
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
	}// ������

	@Override
	public void run() {
		try {
			while (true) {
				String msg = in.readLine();// Ŭ���̾�Ʈ�� ��� �޽����� �ޱ�
				if (msg == null)
					return; // ���������� ����
				if (msg.trim().length() > 0) {
					System.out.println("from Client: " + msg + ":" +
							socket.getInetAddress().getHostAddress());
					// �������� ��Ȳ�� �����!!
					String msgs[] = msg.split("\\|");
					String protocol = msgs[0];
					switch (protocol) {
					
					case "Ŭ���̾�Ʈ����": // ���� ����
						allVO.add(this);// ��ü����ڿ� ���
						waitVO.add(this);// ���ǻ���ڿ� ���
						break;
						
					case "�г���": // �г��� �Է�
						nickName = msgs[1];
						// ���� ��ȭ�� �Է������� ������ ������ ���
						messageWait("������|" + getRoomInfo());
						messageWait("�����ο�����|" + getWaitMem());
						break;
						
					case "������": // �游��� (��ȭ�� ����)
						RoomVO = new RoomVO();
						RoomVO.title = msgs[1];// ������
						RoomVO.count = 1;
						RoomVO.boss = nickName;
						roomVO.add(RoomVO);
						// ����----> ��ȭ�� �̵�!!
						waitVO.remove(this);
						RoomVO.userVO.add(this);
						messageRoom("��ȭ������|" + nickName);// ���ο����� ���� �˸�
						// ���� ����ڵ鿡�� �������� ���
						// ��) ��ȭ���:JavaLove
						// -----> roomInfo(JList) : JavaLove--1
						messageWait("������|" + getRoomInfo());
						messageWait("�����ο�����|" + getWaitMem());
						break;
						
					case "��ȭ���ο�����": // (���ǿ���) ��ȭ�� �ο�����
						sendMsg("��ȭ���ο�����|" + getRoomMem(msgs[1]));
						break;
						
					case "��ȭ���ο�����": // (��ȭ�濡��) ��ȭ�� �ο�����
						messageRoom("��ȭ���ο�����|" + getRoomMem());
						break;

					case "��ȭ������": // ����� (��ȭ�� ����) ----> msgs[] = {"200","�ڹٹ�"}
						for (int i = 0; i < roomVO.size(); i++) {// ���̸� ã��!!
							RoomVO r = roomVO.get(i);
							if (r.title.equals(msgs[1])) {// ��ġ�ϴ� �� ã��!!
								RoomVO = r;
								RoomVO.count++;// �ο��� 1����
								break;
							}
						}
						// ����----> ��ȭ�� �̵�!!
						waitVO.remove(this);
						RoomVO.userVO.add(this);
						messageRoom("��ȭ������|" + nickName);// ���ο����� ���� �˸�
						// �� ���� title����
						sendMsg("��Ÿ��Ʋ|" + RoomVO.title);
						messageWait("������|" + getRoomInfo());
						messageWait("�����ο�����|" + getWaitMem());
						break;
					
					case "����" :
						for(int i = 0; i < RoomVO.userVO.size(); i++) {
							Service user = RoomVO.userVO.get(i);
							if(user.nickName.equals(msgs[1])) { // ��ġ�ϴ� �г��� ã��
								RoomVO.count--; // �ο��� 1 ����
								waitVO.add(user);
								RoomVO.userVO.remove(user);								
								messageRoom("����|" + user.nickName);
								messageWait("������|" + getRoomInfo());
								messageWait("�����ο�����|" + getWaitMem());
								messageRoom("��ȭ���ο�����|" + getRoomMem());
								sendMsg("�����ϱ�|" + user.socket);
								break;
							}
						}
						break;
					
					case "�ʴ�" :
						sendMsg("�ʴ�|" + getWaitMem());
						break;
					
					case "�ʴ��ϱ�" :
						for(int i = 0; i < RoomVO.userVO.size(); i++) {
							Service user = RoomVO.userVO.get(i);
							if(user.nickName.equals(msgs[1])) {
								RoomVO.count++;
								waitVO.remove(user);
								RoomVO.userVO.add(user);
								messageRoom("�ʴ��ϱ�|" + user.nickName);
								messageWait("������|" + getRoomInfo());
								messageWait("�����ο�����|" + getWaitMem());
								messageRoom("��ȭ���ο�����|" + getRoomMem());
								break;
							}
						}
						break;
						
						
					case "�޽���������": // �޽���
						messageRoom("�޽���������|[" + nickName + "] : " + msgs[1]);
						// Ŭ���̾�Ʈ���� �޽��� ������
						break;
						
					case "��ġ��" : // �ܱ�ġ
						messageAll("��ġ��|[" + nickName + "] ���� ��ġ�� : " + msgs[1]);
						break;
						
					case "�ӼӸ�": // �ӼӸ�
						//messageAlone("�ӼӸ�|" + getNickname(msgs[1]));
						getNickname("�ӼӸ�|["+ nickName+"] ���� �ӼӸ� : " + msgs[2], msgs[1]);
						break;

					case "��ȭ������": // ��ȭ�� ����
						RoomVO.count--;// �ο��� ����
						messageRoom("��ȭ������" + nickName);// ���ο��鿡�� ���� �˸�!!
						// ��ȭ��----> ���� �̵�!!
						RoomVO.userVO.remove(this);
						waitVO.add(this);
						// ��ȭ�� ������ ���ο� �ٽ����
						messageRoom("��ȭ���ο�����|" + getRoomMem());
						// ���ǿ� ������ �ٽ����
						messageWait("������|" + getRoomInfo());
						break;
					}
				}
			}
		} catch (IOException e) {
			System.out.println("��");
			e.printStackTrace();
		}
	}

	public String getRoomInfo() { // �� �ο�����
		String str = "";
		for (int i = 0; i < roomVO.size(); i++) {
			RoomVO r = roomVO.get(i);
			str += r.title + "-" + r.count;
			if (i < roomVO.size() - 1)
				str += ",";
		}
		return str;
	}

	public String getRoomMem() {// �������� �ο�����
		String str = "";
		for (int i = 0; i < RoomVO.userVO.size(); i++) {
			Service ser = RoomVO.userVO.get(i);
			str += ser.nickName;
			if (i < RoomVO.userVO.size() - 1)
				str += ",";
		}
		return str;
	}

	public String getRoomMem(String title) {// ������ Ŭ���� ���� �ο�����
		String str = "";
		for (int i = 0; i < roomVO.size(); i++) {
			// "�浿,����,�ֿ�"
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

	public void messageAll(String msg) {// ��ü�����
		// ���ӵ� ��� Ŭ���̾�Ʈ(����+��ȭ��)���� �޽��� ����
		for (int i = 0; i < allVO.size(); i++) {
			Service service = allVO.get(i); 
			try {
				service.sendMsg(msg);
			} catch (IOException e) {
				allVO.remove(i--); 
				System.out.println("Ŭ���̾�Ʈ ���� ����!!");
			}
		}
	}
	
	public void getNickname(String msgs, String nickname) { // �ӼӸ�
		for(int i = 0; i < RoomVO.userVO.size(); i++) {
			Service user = RoomVO.userVO.get(i);
			if(user.nickName.equals(nickname)) {
				try {
					user.sendMsg(msgs);
					this.sendMsg(msgs);
				} catch (IOException e) {
					System.out.println("Ŭ���̾�Ʈ ���� ����!!");
				}
			}
		}
	}

	public void messageWait(String msg) {// ���� �����
		for (int i = 0; i < waitVO.size(); i++) {
			Service service = waitVO.get(i); 
			try {
				service.sendMsg(msg);
			} catch (IOException e) {
				waitVO.remove(i--);
				System.out.println("Ŭ���̾�Ʈ ���� ����!!");
			}
		}
	}

	public void messageRoom(String msg) {// ��ȭ������
		for (int i = 0; i < RoomVO.userVO.size(); i++) {
			Service service = RoomVO.userVO.get(i);
			try {
				service.sendMsg(msg);
			} catch (IOException e) {
				RoomVO.userVO.remove(i--);
				System.out.println("Ŭ���̾�Ʈ ���� ����!!");
			}
		}
	}
	

	
 
	// Ŭ���̾�Ʈ���� �޽��� ����
	public void sendMsg(String msg) throws IOException {
		out.write((msg + "\n").getBytes());
	}
}
