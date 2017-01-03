package iperf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.jar.JarFile;

public class Iperfer {
	
public static void main(String[] args) throws IOException {
        
        if (args.length <2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
        
        boolean clientFlag = args[0].equals("-c")?true:false;//TODO: Add more error checking here
        
        if(clientFlag){
        	String hostName = args[2];
        	int portNumber = Integer.parseInt(args[4]);
        	int time = Integer.parseInt(args[6]);
        	client(hostName, portNumber, time);
        }else{
        	int portNumber = Integer.parseInt(args[2]);
        	server(portNumber);
        }
 
        
    }

public static void client(String hostName, int portNumber, int time){

    try (
        Socket tcpSocket = new Socket(hostName, portNumber);
        PrintWriter out =
            new PrintWriter(tcpSocket.getOutputStream(), true);
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(tcpSocket.getInputStream()));
    ) {
    		long totalTime = (long) (time*Math.pow(10,9));
    		long startTime = System.nanoTime();
    		boolean toFinish = false;
    		long totalNumberOfBytes = 0;
    		while(!toFinish){
	        	byte[] dataChunk = new byte[1000];
	        	totalNumberOfBytes+=(long)1000;
	        	Arrays.fill(dataChunk, (byte)0);
	            out.println(dataChunk);
	            in.readLine();
	            toFinish = (System.nanoTime() - startTime >= totalTime);
    		}
    		int sentInKB = (int) (totalNumberOfBytes/1024);
    		long rate = (totalNumberOfBytes/(long)Math.pow(2,20 ))/time;
    		System.out.print("sent="+sentInKB+"KB rate="+rate+"Mbps");
    } catch (UnknownHostException e) {
        System.err.println("Don't know about host " + hostName);
        System.exit(1);
    } catch (IOException e) {
        System.err.println("Couldn't get I/O for the connection to " +
            hostName);
        System.exit(1);
    } 
}

public static void server(int portNumber){
	long totalSize = 0;
    long startTime = 0;
	try (
            ServerSocket serverSocket =
                new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();     
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            boolean firstTime = true;
            while ((inputLine = in.readLine()) != null) {
            	if(firstTime){
            		startTime = System.nanoTime();
            		firstTime = false;
            	}
            	totalSize+=1000;
                out.println(inputLine);
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }finally{
        	int recievedInKB = (int) (totalSize/1024);
        	long time = System.nanoTime() - startTime;
    		long rate = (long) ((totalSize/(long)Math.pow(2,20 ))/(time/Math.pow(10,9)));
    		System.out.print("sent="+recievedInKB+"KB rate="+rate+"Mbps");
        }
    }
}

