package main;

import static main.Main.mailer;
import static main.Main.msgList;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.shivzee.callbacks.MessageFetchedCallback;
import me.shivzee.util.Domains;
import me.shivzee.util.JMailBuilder;
import me.shivzee.util.Message;
import me.shivzee.util.Response;

public abstract class Command {
	
	
	protected abstract void run(String[] args) throws Exception;
	public abstract String getUsage();
	
	public static ArrayList<Command> cmdList = null;

	
	public void exec(String[] args) {
		try {
			run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void invalidArgs() {
		System.err.println("Invalid argument!");
		System.err.flush();
		System.out.println(getUsage());
	}
	
	public void invalidArgs(String arg) {
		System.err.println("Invalid argument : " + arg);
		System.err.flush();
		System.out.println(getUsage());
	}
	
	public static ArrayList<Command> getCmdList() {
		return cmdList;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static Command getCmd(String cmd) {
		for(Command c : cmdList) {
			if(c.equals(cmd)) return c;
		}
		return new NULLCMD(cmd);
	}
	
	public static void init(){
		cmdList = new ArrayList<Command>(Arrays.asList(
				
				new Command() { // help

					@Override
					public void run(String[] args) throws Exception {
						
						if(args.length == 0) {
							for(Command c : cmdList) {
								System.out.println(c.getUsage());
								System.out.println();
							}
						} else if(args.length == 1 || !(getCmd(args[0]) instanceof NULLCMD)) {
							System.out.println(getCmd(args[0]).getUsage());
						} else {
							invalidArgs();
						}
						
					}

					@Override
					public String getUsage() {
						return "help [command]\n"
								+"\tGet usage of certain command.\n"
								+"\tIf no argument is given, print help of all command.";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "help".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // login

					//TODO : .autologin
					@Override
					public void run(String[] args) throws Exception {
						if(args.length != 2) {
							invalidArgs();
							return;
						}
						mailer = JMailBuilder.login(args[0] , args[1]);
						mailer.init();
						System.out.println("logged in to : " + mailer.getSelf().getEmail());
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
					public void run(String[] args) throws Exception {
						if (args.length != 1 && args.length != 2) {
							invalidArgs();
							return;
						}

						if (args.length == 1) {
							mailer = JMailBuilder.createDefault(args[0]);
						} else {
							String j = args[0] + "@" + Domains.getRandomDomain().getDomainName();
							System.out.println(j);
							mailer = JMailBuilder.createAndLogin(j, args[1]);
						}
						mailer.init();
						System.out.println("logged in to : " + mailer.getSelf().getEmail());
					}

					@Override
					public String getUsage() {
						return "gen [id] <password>\n"
								+ "\tGenerate new email combined with given id(optional) and random domain.\n"
								+ "\tIf id is not given, random id will be used.";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "gen".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // delaccount

					@Override
					public void run(String[] args) throws Exception {
						if (args.length != 0) {
							invalidArgs();
							return;
						}
						
						if (mailer.deleteSync()) System.out.println("Account is Deleted");
						
					}

					@Override
					public String getUsage() {
						return "delaccount\n"
								+ "\tDelete self account.";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "delaccount".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // fetch

					private boolean overwrite = false;
					
					@Override
					public void run(String[] args) throws Exception {
						if (args.length >= 3) {
							invalidArgs();
							return;
						}
						
						int limit = -1;
						
						for(String s : args) {
							if("--overwrite".equals(s) || "-o".equals(s)) {
								overwrite = true;
							} else {
								try {
									limit = Integer.parseInt(s);
								} catch (NumberFormatException e) {
									invalidArgs(s);
									return;
								}
							}
						}
						
						MessageFetchedCallback callback = new MessageFetchedCallback() {
							@Override
							public void onMessagesFetched(List<Message> list) {
								if(list.isEmpty()) {
									System.out.println("No message to fetch!");
									return;
								}
								consumMsgList(list);
								System.out.println("Fetched Messages : ");
								for (Message m : list) {
									System.out.println("ID : " + m.getId());
									System.out.println("Sent by : " + m.getSenderAddress());
									System.out.println("Title : " + m.getSubject());
									System.out.println();
								}
								System.out.println("Use listmsg command to show all fetched messages");
							}

							@Override
							public void onError(Response response) {
								System.err.println("Error while fetching messages! responseCode : " + response.getResponseCode());
								System.err.println(response.getResponse());
							}
						};
						
						if (limit != -1) {
							mailer.fetchMessages(Integer.parseInt(args[0]), callback);
						} else {
							mailer.fetchMessages(callback);
						}

					}
					
					private void consumMsgList(List<Message> list) {
						if(overwrite) {
							msgList = list;
						} else {
							list.stream().filter(m -> msgList.stream().map(Message::getId).allMatch(id -> !id.equals(m.getId()))).forEach(msgList::add);
						}
					}

					@Override
					public String getUsage() {
						return "fetch [--overwrite|-o] [limit]\n" 
								+ "\tFetch messages from server. To show messages, use 'listmsg' command.\n"
								+ "\tIf --overwrite or -o option is given, fetched message before\n"
								+ "\twill be overwrited to fresh fetched message\n"
								+ "\tIf limit is given, up to <limit> messages will be fetched";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "fetch".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // listmsg

					@Override
					public void run(String[] args) throws Exception {
						if (args.length != 0) {
							invalidArgs();
							return;
						}
						
						for (Message m : msgList) {
							System.out.println("ID : " + m.getId());
							System.out.println("Sent by : " + m.getSenderAddress());
							System.out.println("Title : " + m.getSubject());
							System.out.println();
						}
					}

					@Override
					public String getUsage() {
						return "listmsg\n"
								+ "\tList all fetched message.";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "listmsg".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // showmsg

					@Override
					public void run(String[] args) throws Exception {

						if (args.length == 0) {
							msgList.forEach(this::showMessage);
						} else if (args.length == 1) {
							for(Message m : msgList) {
								if(args[0].equals(m.getId())) {
									showMessage(m);
									return;
								}
							}
							invalidArgs(args[0] + " does not exist!");
						} else {
							invalidArgs();
						}
						
						
					}

					private void showMessage(Message m) {
						System.out.println("ID : " + m.getId());
	                    System.out.println("Sent by : " + m.getSenderAddress() + " (" + m.getSenderName() + ")");
	                    System.out.println("Title : " + m.getSubject());
	                    if(m.hasAttachments()) {
	                    	System.out.println("Attatchment(s) : " + m.getAttachments().stream().map(a -> a.getFilename() + "(" + a.getSize() + "KB)")
	                    			.collect(Collectors.joining(", ")));
	                    }
	                    System.out.println();
	                    System.out.println(m.getContent());
	                    System.out.println();
	                    System.out.println();
					}
					
					@Override
					public String getUsage() {
						return "showmsg [id]\n"
								+ "\tShow message that have given id.\n"
								+ "\tIf id is not given, show ALL message.";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "showmsg".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // getattat

					private String path = "." + File.separator;

					@Override
					public void run(String[] args) throws Exception {
						
						String id;
						
						if(args.length == 1 && !args[0].equals("--path")) {
							id = args[0];
						} else if(args.length == 3 && args[1].equals("--path")) {
							id = args[0];
							path = args[2].replace("/", File.separator).replace("\\", File.separator);
						} else {
							invalidArgs();
							return;
						}
						
						for(Message m : msgList) {
							if(id.equals(m.getId())) {
								m.getAttachments().forEach(attat -> {
									if (attat.saveSync(getPath(), attat.getFilename())) System.out.println(attat.getFilename() + " is Downloaded");
								});
								return;
							}
						}
					}

					private String getPath() { return path + File.separator; }
					@Override
					public String getUsage() {
						return "getattat <id> [--path path]\n"
								+"\tDownload attatchment of mail that have given id.\n"
								+"\tIf path is given, the file(s) are stored in there.\n"
								+"\tElse, files will be downloaded at working directory (" + Paths.get(".").toFile().getAbsolutePath() + ")";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "getattat".equals(o)) return true;
						else return false;
					}
					
				},
				new Command() { // exit

					@Override
					public void run(String[] args) throws Exception { }

					@Override
					public String getUsage() {
						return "exit\n"
								+"\tKills the application.";
					}
					
					@Override
					public boolean equals(Object o) {
						if(o == this || "exit".equals(o)) return true;
						else return false;
					}
					
				}
				));
	}
	
	
	static class NULLCMD extends Command {

		private String str;

		public NULLCMD(String cmd) { this.str = cmd; }
		
		@Override
		protected void run(String[] args) throws Exception {
			System.err.println(getUsage());
			System.err.flush();
		}

		@Override
		public String getUsage() {
			return "Invalid command : " + str;
		}
		
	};
	
}
