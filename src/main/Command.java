package main;

import java.util.ArrayList;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import me.shivzee.util.JMailBuilder;

public abstract class Command {
	
	public abstract void run(String[] args);
	public abstract String getUsage();
	
	//public boolean eq 
	
	public static ArrayList<Command> getCmdList(){
		return new ArrayList<Command>(Arrays.asList(
				
				new Command() { // login

					@Override
					public void run(String[] args) {
						if(args.length != 2) {
							System.err.println("invalide argument!");
							System.out.println(getUsage());
						}
						
						try {
							Main.mailer = JMailBuilder.login(args[0] , args[1]);
						} catch (LoginException e) {
							e.printStackTrace();
						}
						
					}

					@Override
					public String getUsage() {
						return "login <email> <password>\n" +
									"\tLogin to existing account";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "login".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // gen

					@Override
					public void run(String[] args) {
						if(args.length != 3 && args.length !=2) {
							System.err.println("invalide argument!");
							System.out.println(getUsage());
						}
						
						if(args.length == 2) {
							
						}
					}

					@Override
					public String getUsage() {
						return "gen [id] --password <password>\r\n"
								+ "\r\n"
								+ "Generate new email combined with given id(optional) and random domain.\r\n"
								+ "If id is not given, random id will be used.";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "gen".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() {

					@Override
					public void run(String[] args) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public String getUsage() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "login".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() {

					@Override
					public void run(String[] args) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public String getUsage() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "login".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() {

					@Override
					public void run(String[] args) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public String getUsage() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "login".equals(o)) return true;
						else return false;
					}
					
				}
				
				));
	}
}
