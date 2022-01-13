package test;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

class Client extends Frame implements ActionListener,MouseListener {
    TextField tf,tf2;
    TextArea ta;
    Button b,b2;
    List list;
    Dialog dia;
    BufferedReader br;
    BufferedWriter bw;
    PopupMenu pm;
    String id;
    MenuItem mi;

    Client(String id) throws Exception {

        super(id+"'s chatting");        // 채팅장 제목
        this.id = id;
        mi = new MenuItem("secret msg");        // 비밀채팅 버튼
        pm = new PopupMenu();       // 우클릭 설정
        pm.add(mi);                      // 우클릭시 비밀채팅버튼 생성
        dia = new Dialog(this); // 대화창
        tf = new TextField(15);     // 메세지 보낸 텍스트필드
        ta = new TextArea();        // 텍스트창
        b = new Button("exit");     // 종료 버튼
        b2 = new Button("send");    //보내기 버튼
        list = new List();
        tf2=new TextField(); // 비밀채팅 보내는 텍스트 필드
        list.add(pm);
        Panel p1 = new Panel();     // panel1 생성
        Panel p2 = new Panel();      // panel 2 생성

        p1.setLayout(new BorderLayout());       // panel1 레이아웃 생성
        p2.setLayout(new BorderLayout());       // panel2 레이아웃 생성

        p1.add(ta);
        p1.add(list, "East");
        p2.add(tf);
        p2.add(b, "East");
        add(p1, "Center");
        add(p2, "South");

        list.addMouseListener(this);
        tf.addActionListener(this);
        b.addActionListener(this);
        b2.addActionListener(this);
        mi.addActionListener(this);
    }



    void initNet() throws Exception {

        Socket socket = new Socket("localhost", 8877);          // 해당 컴퓨터 ip주소에 포트번호 8877로 소켓 생성
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        br = new BufferedReader(isr); // 소켓을 통한 인풋 스트림 생성

        OutputStream os = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        bw = new BufferedWriter(osw);   // 소켓을 통한 아웃풋 스트림 생성

        sendMsg("enter/" + id);         // 새로운 사용자 들어온 경우
    }
    public void readMsg() {
        try {
            while (true) {

                String line = br.readLine();
                System.out.println(line);
                String array[] = line.split("/");
                switch (array[0]) {

                    case "enter":  // 새로운 사용자 들어온 경우
                        ta.append(array[1] + " entered into this room.\n");
                        break;

                    case "msg":         // 전체 전송 메세지
                        ta.append(array[1] + "\n");
                        break;

                    case "guestlist":
                        list.removeAll();
                        int len = array.length;
                        for (int i = 1; i < len; i++)
                            list.add(array[i]);
                        break;

                    case "귓속말":         // 비밀 채팅 보내는 경우

                        String arr[] = array[1].split("&");
                        ta.append("["+arr[0]+"->"+arr[1]+"]"+arr[2] + "\n");
                        break;



                }

            }

        } catch (Exception e) {         // 파일 읽다가 에러
            System.out.println("Error during reading message");
        }
    }



    public void sendMsg(String msg) throws Exception {  // 입력한 메세지 outstream으로 보냄
        bw.write(msg + "\n");
        bw.flush();
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == tf) {          //텍스트필드로 메세지 보낸 경우
            try {
                sendMsg("msg/" + tf.getText());     // 전체 메세지 전송
                tf.setText("");

            } catch (Exception ee) {        // 메세지 보내다가 에러난 경우우
                System.out.println("Error during sending message");
            }

        } else if (e.getActionCommand().equals("exit")) {           //exit 버튼을 누른 경우, 사용자 나감

            try {
                System.exit(0);         // 채팅 종료, 사용자 나감

            } catch (Exception e1) {        // exit 하다가 에러난 경우
                e1.printStackTrace();
            }
        }
        else if(e.getSource()==b2){         // secret msg 버튼 누른 경우, 비밀 채팅 보냄

            try {
                ta.append("["+id+"->"+list.getSelectedItem()+"]"+tf2.getText() + "\n");
                sendMsg("귓속말/"+id+"&"+list.getSelectedItem()+"&"+tf2.getText());
                dia.setVisible(false);
                dia.dispose();

            } catch (Exception e1) {            // 비밀 채팅 보내다가 에러난 경우
                e1.printStackTrace();
            }
        }

        else if(e.getSource() == mi){       // secret 메세지를 보내고자 하는 경우

            dia.add(tf2);
            dia.add(b2);            // 버튼 추가

            dia.setLayout(new GridLayout(2,0));         // layout
            dia.setBounds(300, 300, 200, 200);
            dia.setVisible(true);       // 화면 보여줌
        }
    }

    public void mousePressed(java.awt.event.MouseEvent e) {         // 마우스 이벤트 설정

        if(e.getButton()==3 && list.getSelectedItem() != null ){
            pm.show(list, e.getX(), e.getY());
        }

    }
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
    }



    public static void main(String args[]) throws Exception {
        System.out.print("Type a user name:");
        Scanner scanner= new Scanner(System.in);

        Client client = new Client(scanner.nextLine());     // 클라이언트 소켓 생성

        client.initNet();           // 클라이언트 소켓 초기화

        client.setBounds(200, 200, 500, 400);
        client.setVisible(true);        // 화면에 채팅창 생성

        client.readMsg();           // 메세지 읽음
    }

}