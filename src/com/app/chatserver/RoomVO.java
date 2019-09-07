package com.app.chatserver;

import java.util.ArrayList;
import java.util.List;

public class RoomVO {
	String title;// 방제목
	int count;// 방 인원수
	String boss;// 방장(방 개설자)
	List<Service> userVO;// userVO: 같은 방에 접속한 Client정보 저장
	
	public RoomVO() {
		userVO = new ArrayList<>();
	}
}