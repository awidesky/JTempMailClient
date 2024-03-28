package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import me.shivzee.JMailTM;
import me.shivzee.util.Message;

public class Main {

	protected static JMailTM mailer = null;
	protected static List<Message> msgList = new ArrayList<>(); 
	
	public final static String version = "v0.5";

	/*
	 * TODO $> login ubercharge@awgarstone.com dnqjckwl SLF4J: Failed to load class
	 * "org.slf4j.impl.StaticLoggerBinder". SLF4J: Defaulting to no-operation (NOP)
	 * logger implementation SLF4J: See
	 * http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
	 * logged in to : ubercharge@awgarstone.com
	 * 
	 * $> fetch me.shivzee.exceptions.MessageFetchException:
	 * me.shivzee.exceptions.MessageFetchException: java.lang.NullPointerException:
	 * Cannot invoke "Object.toString()" because the return value of
	 * "org.json.simple.JSONObject.get(Object)" is null at
	 * me.shivzee.JMailTM.fetchMessages(JMailTM.java:325) at
	 * main.Command$5.run(Command.java:231) at main.Command.exec(Command.java:46) at
	 * main.Main.main(Main.java:32)
	 */
	public static void main(String[] args) {

		Command.init();
		System.out.println("JTempMailClient " + version);
		
		Scanner sc = new Scanner(System.in);
		String input = "";
		
		while(!input.equals("exit")) {
			System.out.print(" $> ");
			input = sc.nextLine();
			int index = input.indexOf(" ");
			if (index != -1 && index != input.length()) {
				Command.getCmd(input.substring(0, index)).exec(input.substring(index + 1).split(" "));
			} else {
				Command.getCmd(input).exec(new String[] {});
			}
			System.err.flush();
			System.out.flush();
			System.out.println();
		}
		
		sc.close();
		
	}

}
