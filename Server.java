import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

public class Server 
{
	private final static int PORTNUMBER = 12345;
	
    private static ServerSocket serverSocket;
	private static Socket clientSocket;
	
	private static BigInteger primeNumber_Server;
	private static BigInteger publicKey_Server;
	private static BigInteger privateKey_Server;
	private static BigInteger result_Server;
	
	private static BigInteger finalResult;
	
	private static String encryptedString;
	private static String decryptedString;
	
	private static AES temp;
	
	private static Scanner kr;

	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//														MAIN														//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws InterruptedException
    {   	
		/*try (BufferedReader br = new BufferedReader(new FileReader( System.getProperty("user.dir") + "/asciiart.txt")))
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
		}*/
		
		System.out.println("Server Version: 2.5");
        
        kr = new Scanner(System.in);
        
        connectToSocket();
        
    	startAES();   
    		
    	while(true)
    	{    		
    		encryptOutgoingMessage();
	    	sendEncryptedMessageToClient(encryptedString);
	    	
	    	decryptIncomingMessage();
	    	System.out.println("\n(Client): " + decryptedString + "\n");
    	}
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//													SOCKETS															//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static void connectToSocket()
	{
		try
        {
        	serverSocket = new ServerSocket(PORTNUMBER);
        	clientSocket = serverSocket.accept();  
        	System.out.println("\nServer: Connected to Client...\n");
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
    	temp.setKey(Long.toString(finalResult.longValue()));
	}
	
	private static void generateAES_Seed()
	{
		primeNumber_Server = BigInteger.probablePrime(1024, new Random());
    	publicKey_Server = new BigInteger(512, new Random());
    	privateKey_Server = new BigInteger(512, new Random());
    	
    	result_Server = modulo(publicKey_Server, privateKey_Server, primeNumber_Server);
    	
    	sendElGamalInformationToClient(result_Server);
    	
    	BigInteger resultFromClient = receiveBigIntegerFromClient();
    	
		finalResult = modulo(resultFromClient, privateKey_Server, primeNumber_Server);
    	
    	//System.out.println("Needs To Match This: " + finalResult);
	}
	
	private static void sendElGamalInformationToClient(final BigInteger result)
	{		
        try 
        {
    		OutputStream os = clientSocket.getOutputStream();  
    		ObjectOutputStream oos = new ObjectOutputStream(os);  
    		
    		oos.writeObject(primeNumber_Server);  
    		oos.writeObject(publicKey_Server);
    		oos.writeObject(result);
        }
        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to listen");
            System.out.println(e.getMessage());
        }
	}
	
	private static BigInteger receiveBigIntegerFromClient()
	{
		BigInteger readIn = null;
		
		try
        {
			InputStream is = clientSocket.getInputStream();  
			ObjectInputStream ois = new ObjectInputStream(is); 
			
			readIn = (BigInteger) ois.readObject();
        } 
        catch (UnknownHostException e) 
        {
            System.err.println("Don't know about host");
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection");
            System.exit(1);
        } 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return readIn;
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
	
	private static void encryptOutgoingMessage()
	{
		 System.out.print("(You): ");
		 String outgoingMessage = kr.nextLine();
		 
		 temp.encrypt(outgoingMessage);
		 
		 encryptedString = AES.getEncryptedString();
	}
	
	private static void decryptIncomingMessage()
	{
		receiveEncryptedMessageFromClient();
		 
		 AES.setEncryptedString(encryptedString);
		 
		 temp.decrypt(encryptedString);
		 
		 decryptedString = AES.getDecryptedString();
		 
		 AES.setDecryptedString(decryptedString);
	}
	
	private static void sendEncryptedMessageToClient(String message)
	{
		try 
        {
    		OutputStream os = clientSocket.getOutputStream();  
    		ObjectOutputStream oos = new ObjectOutputStream(os);  
    		
    		oos.writeObject(message);  
        }
        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to listen");
            System.out.println(e.getMessage());
        }
	}
	
	private static void receiveEncryptedMessageFromClient()
	{
		try
        {
			InputStream is = clientSocket.getInputStream();  
			ObjectInputStream ois = new ObjectInputStream(is); 
			
			encryptedString = (String) ois.readObject();
        } 
        catch (UnknownHostException e) 
        {
            System.err.println("Don't know about host");
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection");
            System.exit(1);
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	
	
	
}
