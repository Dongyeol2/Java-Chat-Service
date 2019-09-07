package com.app.chatserver;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class ChatModel extends JFrame {
	// ä�ù�
	JTextField textField;
	JLabel msgLabel;
	JTextArea textArea;
	JScrollPane textList, memInfoList;
	JList<String> memList;
	JButton broadcastBtn, unicastBtn, exitBtn, outBtn, inviteBtn;
	JPanel panel;
	
	String selectNick;
	
	public ChatModel() {
		setTitle("ä�ù�");
		textField = new JTextField(15);
		msgLabel = new JLabel("Message");
		textArea = new JTextArea();
		textArea.setLineWrap(true);// TextArea ���α��̸� ����� text�߻��� �ڵ� �ٹٲ�
		memList = new JList<String>();
		memList.setBorder(new TitledBorder("���� �ο�"));
		memList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String str = memList.getSelectedValue();
				if (str == null)
					return;
				System.out.println("nickname=" + str);
				// "�ڹٹ�" <---- substring(0,3)
				// ��ȭ�� ���� �ο�����
				selectNick = str;
			}
		});
		
		textList = new JScrollPane(textArea);
		memInfoList = new JScrollPane(memList);
		outBtn = new JButton("����");
		inviteBtn = new JButton("�ʴ�");
		unicastBtn = new JButton("�ӼӸ� �ϱ�");
		broadcastBtn = new JButton("��ġ��");
		exitBtn = new JButton("������");
		panel = new JPanel();
		textList.setBounds(10, 10, 380, 390);
		msgLabel.setBounds(10, 410, 60, 30);
		textField.setBounds(70, 410, 320, 30);
		memInfoList.setBounds(400, 10, 120, 270);
		outBtn.setBounds(400, 290, 60, 30);
		inviteBtn.setBounds(465, 290, 60, 30);
		unicastBtn.setBounds(400, 330, 120, 30);
		broadcastBtn.setBounds(400, 370, 120, 30);
		exitBtn.setBounds(400, 410, 120, 30);
		panel.setLayout(null);
		//panel.setBackground(Color.PINK);
		panel.add(textList);
		panel.add(msgLabel);
		panel.add(textField);
		panel.add(memInfoList);
		panel.add(outBtn);
		panel.add(inviteBtn);
		panel.add(unicastBtn);
		panel.add(broadcastBtn);
		panel.add(exitBtn);
		add(panel);
		setBounds(300, 200, 550, 500);
		// setVisible(true);
		textField.requestFocus();
	}// ������
}