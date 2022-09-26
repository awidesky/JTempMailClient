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
