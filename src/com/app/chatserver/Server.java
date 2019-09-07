package com.app.chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
	// ServerŬ����: ������ ���� ���Ӽ���, ����Ŭ���̾�Ʈ ����
	List<Service> allVO;// ��� �����(���ǻ���� + ��ȭ������)
	List<Service> waitVO;// ���� �����
	List<RoomVO> roomVO;// ������ ��ȭ�� Room-vs(Vector) : ��ȭ������

	public Server() {
		allVO = new ArrayList<>();
		waitVO = new ArrayList<>();
		roomVO = new ArrayList<>();
		// Thread t = new Thread(run�޼ҵ��� ��ġ); t.start();
		new Thread(this).start();
	}// ������

	@Override
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(5000);
			// ���� �������� ip + ��õ� port ----> ���ϼ���
			System.out.println("Start Server.......");
			while (true) {
				Socket s = ss.accept();// Ŭ���̾�Ʈ ���� ���
				// s: ������ Ŭ���̾�Ʈ�� ��������
				Service ser = new Service(s, this);
				// allV.add(ser);//��ü����ڿ� ���
				// waitV.add(ser);//���ǻ���ڿ� ���
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// run

	public static void main(String[] args) {
		new Server();
	}
}
