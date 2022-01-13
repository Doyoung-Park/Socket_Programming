package test;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.*;

class Server {
    ArrayList<Guest> list;

    void initNet() throws Exception {
        list = new ArrayList<Guest>();      // 사용자 list

        ServerSocket ss = new ServerSocket(8877);       // 포트번호 9977 로 소켓 열기

        while (true) {
            Socket s = ss.accept();         // 사용자의 접속을 입력 받음
            Guest g = new Guest(this, s);       // 접속한 사용자의 소켓을 서버에 만듦

            g.start();
            addGuest(g);            // 접속한 사용자를 접속자 목록에 추가
        }
    }

    void addGuest(Guest g) {         // 접속한 사용자를 접속자 목록에 추가
        list.add(g);
        System.out.println("number of Users:" + list.size());      // 접속한 사용자를 접속자 수 출력
    }

    public void talkMsg(String talk, String talk2, String talk3)  {     // 메세지 보내기
        //talk : 보낸 사람
        //talk2 : 메세지를 받은 사람
        //talk3 : 메세지 내용

        for (Guest g : list) {      // 전체 접속자에게 메세지 전송

            if(g.id.equals(talk2)){         // 사용자에게 귓속말 보내는 경우
                try {
                    g.sendMsg("귓속말/"+talk+"&"+talk2+"&"+talk3);
                } catch (Exception e) {
                    System.out.println("during sending a secret message"+e.getMessage());
                }
            }
        }
    }

    void removeGuest(Guest g) {         // 접속자가 나가는 경우

        list.remove(g);         // 접속자 목록에서 지움
        System.out.println("접속자수:" + list.size());      // 접속자 수 1감소

    }



    void broadcast(String msg) throws Exception {       // 전체 사용자에게 보내는 메세지

        for (Guest g : list) {
            g.sendMsg(msg);
        }
    }



    void makeGuestlist() throws Exception {  // --> 예시: guestlist/이민갑/김갑동/김장동/

        StringBuffer buffer = new StringBuffer("guestlist/");

        for (Guest g : list) {
            buffer.append(g.id + "/");          // 접속자 리스트를 만듦
        }
        broadcast(buffer.toString());       // 접속자 리스트를 콘솔창에 출력
    }



    public static void main(String args[]) throws Exception {

        Server server = new Server();       // 서버 객체 실행

        server.initNet();   // 서버 소켓 초기화

    }

}




class Guest extends Thread {

    String id;
    Server server;
    Socket socket;
    BufferedReader br;
    BufferedWriter bw;


    Guest(Server server, Socket socket) throws Exception {          // 서버에 새로 접속한 유저의 소켓 만듦

        this.server = server;
        this.socket = socket;

        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        br = new BufferedReader(isr);      // input 스트림 생성

        OutputStream os = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        bw = new BufferedWriter(osw);       // output 스트림 생성
    }



    public void run() {         // 실행

        try {

            while (true) {
                String line = br.readLine();
                System.out.println(line+"is read.");

                String array[] = line.split("/");       // 유저가 보낸 메세지 분할 --> 케이스별로 다르게 인식

                switch (array[0]) {

                    case "enter":           // 사용자 들어온 경우
                        id = array[1];
                        server.makeGuestlist();     // 사용자 리스트에 추가
                        server.broadcast(line);     // 들어왔다는 알림
                        break;

                    case "msg":             // 전체 전송 메세지
                        String str = "msg/[" + id + "]" + array[1];
                        server.broadcast(str);
                        break;



                    case "귓속말":             // 귓속말로 보낸 경우
                        String[] talk=array[1].split("&");
                        server.talkMsg(talk[0],talk[1],talk[2]);

                        //talk[0] : 보내는 사람
                        //talk[1] : 받는 사람
                        //talk[1] : 보내는 메세지

                }
            }
        } catch (Exception e) {         // 예외발생한 경우, 누구 때문인지 알려줌
            // e.printStackTrace();
            System.out.println(e.getMessage()+"get message");
            System.out.println("Error because of "+ id + "'s message");


            //server.removeGuest(this);

            try {
                server.broadcast("exit/" + id +"is exited");    // 사용자 나간 경우 알려줌

            } catch (Exception e1) {        // 에러난 경우
                e1.printStackTrace();
            }
        }
    }

    public void sendMsg(String msg) throws Exception {

        bw.write(msg + "\n");           // 보낸 메세지를 버퍼에 올림
        bw.flush();                         // 버퍼에 올린 메세지를 스트림을 통해 전송
    }
}