package com.app.chatserver;

import java.util.ArrayList;
import java.util.List;

public class RoomVO {
	String title;// ������
	int count;// �� �ο���
	String boss;// ����(�� ������)
	List<Service> userVO;// userVO: ���� �濡 ������ Client���� ����
	
	public RoomVO() {
		userVO = new ArrayList<>();
	}
}