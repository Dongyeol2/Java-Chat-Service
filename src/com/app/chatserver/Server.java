package com.app.chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
	// Server클래스: 소켓을 통한 접속서비스, 접속클라이언트 관리
	List<Service> allVO;// 모든 사용자(대기실사용자 + 대화방사용자)
	List<Service> waitVO;// 대기실 사용자
	List<RoomVO> roomVO;// 개설된 대화방 Room-vs(Vector) : 대화방사용자

	public Server() {
		allVO = new ArrayList<>();
		waitVO = new ArrayList<>();
		roomVO = new ArrayList<>();
		// Thread t = new Thread(run메소드의 위치); t.start();
		new Thread(this).start();
	}// 생성자

	@Override
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(5000);
			// 현재 실행중인 ip + 명시된 port ----> 소켓서비스
			System.out.println("Start Server.......");
			while (true) {
				Socket s = ss.accept();// 클라이언트 접속 대기
				// s: 접속한 클라이언트의 소켓정보
				Service ser = new Service(s, this);
				// allV.add(ser);//전체사용자에 등록
				// waitV.add(ser);//대기실사용자에 등록
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// run

	public static void main(String[] args) {
		new Server();
	}
}
