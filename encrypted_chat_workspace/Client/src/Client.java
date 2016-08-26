import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

public class Client 
{
	private final static int PORTNUMBER = 12345;
	
    private static Socket echoSocket;

    private static String hostName;
    
    private static BigInteger primeNumber_Server;
    private static BigInteger publicKey_Server;
    private static BigInteger result_Server;
    
    private static BigInteger privateKey_Client;
    private static BigInteger originalResult_Client;
    private static BigInteger resultToSendToServer;
    
	private static String encryptedString;
	private static String decryptedString;

	private static AES temp;
	
	private static Scanner kr;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//														MAIN														//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws InterruptedException
    {
		try (BufferedReader br = new BufferedReader(new FileReader( System.getProperty("user.dir") + "/asciiart.txt")))
		{
			   String line = null;
			   while ((line = br.readLine()) != null) 
			   {
			       System.out.println(line);
			   }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	System.out.println("Client Version: 2.5");
    	
    	kr = new Scanner(System.in);
    	
    	System.out.print("\nServer IP Address: ");
    	hostName = kr.nextLine();
        
        connectToSocket();
        
        startAES();
        
        while(true)
        {
            decryptIncomingMessage();
            System.out.println("\n(Server): " + decryptedString);
            
            encryptOutgoingMessage();   
            sendEncryptedMessageToServer(encryptedString);
        }
    }
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//													SOCKETS															//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void connectToSocket()
	{
		try
        {
        	echoSocket = new Socket(hostName, PORTNUMBER);
        	System.out.println("\nClient: Connected to Server...");
        }
        catch(IOException e)
        {
        	System.out.println(e.getMessage());
        }
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//														AES    														//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void startAES()
	{
		generateAES_Seed();
        
        temp = new AES();
        temp.setKey(Long.toString(originalResult_Client.longValue()));
	}
	
	private static void generateAES_Seed()
	{
		privateKey_Client = new BigInteger(512, new Random());
        
        receiveElGamalInformationFromServer(hostName);
        
        originalResult_Client = modulo(result_Server, privateKey_Client, primeNumber_Server);
        
        resultToSendToServer = modulo(publicKey_Server, privateKey_Client, primeNumber_Server);
        
        sendBigIntegerToServer(PORTNUMBER);
	}
	
	public static void receiveElGamalInformationFromServer(String hostName)
	{
		try
        {
			InputStream is = echoSocket.getInputStream();  
			ObjectInputStream ois = new ObjectInputStream(is); 
			
			primeNumber_Server = (BigInteger) ois.readObject();
			publicKey_Server = (BigInteger) ois.readObject();
			result_Server = (BigInteger) ois.readObject();
	  
        } 
        catch (UnknownHostException e) 
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sendBigIntegerToServer(int portNumber)
	{
		try 
        {
    		OutputStream os = echoSocket.getOutputStream();  
    		ObjectOutputStream oos = new ObjectOutputStream(os);  
    		
    		oos.writeObject(resultToSendToServer);  
        }
        catch (IOException e) 
        {
            System.out.println("Exception Here");
            System.out.println(e.getMessage());
        }
	}
	
	private static BigInteger modulo(BigInteger base, BigInteger exponent, final BigInteger modulo)
	{
	    BigInteger result = BigInteger.ONE;
	    
	    while (exponent.compareTo(BigInteger.ZERO) > 0)
	    {
	        if (exponent.testBit(0)) // then exponent is odd
	        {
	            result = (result.multiply(base)).mod(modulo);
	        }
	        
	        exponent = exponent.shiftRight(1);
	        base = (base.multiply(base)).mod(modulo);
	    }
	    return result.mod(modulo);
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//													SENDING MESSAGE  												//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void decryptIncomingMessage()
	{
		encryptedString = receiveEncryptedMessageFromServer(hostName);
		
        AES.setEncryptedString(encryptedString);
        
        temp.decrypt(encryptedString);
        
        decryptedString = AES.getDecryptedString();
        
        AES.setDecryptedString(decryptedString);
	}
	
	private static void encryptOutgoingMessage()
	{
		System.out.print("\n(You): ");
		String messageToEncrypt = kr.nextLine();
        temp.encrypt(messageToEncrypt);
        encryptedString = AES.getEncryptedString();
	}
	
	private static String receiveEncryptedMessageFromServer(String hostName)
	{		
		String encryptedMessage = "";
		try
        {
			InputStream is = echoSocket.getInputStream();  
			ObjectInputStream ois = new ObjectInputStream(is); 
			
			encryptedMessage = (String) ois.readObject();
        } 
        catch (UnknownHostException e) 
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		return encryptedMessage;
	}
	
	private static void sendEncryptedMessageToServer(String message)
	{
		try 
        {
    		OutputStream os = echoSocket.getOutputStream();  
    		ObjectOutputStream oos = new ObjectOutputStream(os);  
    		
    		oos.writeObject(message);  
        }
        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to listen");
            System.out.println(e.getMessage());
        }
	}
	
	
	
	
}
