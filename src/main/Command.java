package main;

import static main.Main.mailer;
import static main.Main.msgList;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import me.shivzee.callbacks.MessageFetchedCallback;
import me.shivzee.util.Domains;
import me.shivzee.util.JMailBuilder;
import me.shivzee.util.Message;
import me.shivzee.util.Response;

public abstract class Command {
	
	private String commandStr;
	public static HashMap<String, Command> cmdMap = null;
	

	protected abstract void run(String[] args) throws Exception;
	public abstract String getUsage();

	public Command(String commandString) {
		this.commandStr = commandString;
	}
	@Override
	public boolean equals(Object other) {
		if (other == this || commandStr.equals(other.toString()))
			return true;
		else
			return false;
	}
	@Override
	public String toString() {
		return commandStr;
	}
	
	
	public void exec(String[] args) {
		try {
			run(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.flush();
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
	
	public static HashMap<String, Command> getCmdList() {
		return cmdMap;
	}
	
	public static Command getCmd(String cmd) {
		return cmdMap.getOrDefault(cmd, new NULLCMD(cmd));
	}
	
	public static void init() {
		cmdMap = new LinkedHashMap<String, Command>();

		cmdMap.put("help", new Command("help") {

			@Override
			public void run(String[] args) throws Exception {

				if (args.length == 0) {
					for (Command c : cmdMap.values()) {
						System.out.println(c.getUsage());
						System.out.println();
					}
				} else if (args.length == 1 || !(getCmd(args[0]) instanceof NULLCMD)) {
					System.out.println(getCmd(args[0]).getUsage());
				} else {
					invalidArgs();
				}

			}

			@Override
			public String getUsage() {
				return "help [command]\n" + "\tGet usage of certain command.\n"
						+ "\tIf no argument is given, print help of all command.";
			}
		});

		cmdMap.put("login", new Command("login") {

			@Override
			public void run(String[] args) throws Exception {

				String email, password;
				if (args.length == 0) {
					Scanner sc = new Scanner(new File("." + File.separator + "autologin.txt"));
					email = sc.nextLine();
					password = sc.nextLine();
				} else if (args.length == 2) {
					email = args[0];
					password = args[1];
				} else {
					invalidArgs();
					return;
				}

				mailer = JMailBuilder.login(email, password);
				mailer.init();
				System.out.println("logged in to : " + mailer.getSelf().getEmail());
			}

			@Override
			public String getUsage() {
				return "login [<email> <password>]\n" + "\tLogin to existing account\n"
						+ "\tIf no option given, find \"autologin.txt\" in working directory.\n"
						+ "\tYou can write your email & pasword there to login.";
			}
		});
		cmdMap.put("gen", new Command("gen") {

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
		});
		cmdMap.put("delaccount", new Command("delaccount") {

			@Override
			public void run(String[] args) throws Exception {
				if (args.length != 0) {
					invalidArgs();
					return;
				}

				if (mailer.delete())
					System.out.println("Account is Deleted");

			}

			@Override
			public String getUsage() {
				return "delaccount\n" + "\tDelete self account.";
			}
		});
		cmdMap.put("fetch", new Command("fetch") {

			private boolean overwrite = false;

			@Override
			public void run(String[] args) throws Exception {
				if (args.length >= 3) {
					invalidArgs();
					return;
				}

				int limit = -1;

				for (String s : args) {
					if ("--overwrite".equals(s) || "-o".equals(s)) {
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
						if (list.isEmpty()) {
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
						System.err
								.println("Error while fetching messages! responseCode : " + response.getResponseCode());
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
				if (overwrite) {
					msgList = list;
				} else {
					list.stream()
							.filter(m -> msgList.stream().map(Message::getId).allMatch(id -> !id.equals(m.getId())))
							.forEach(msgList::add);
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
		});
		cmdMap.put("listmsg", new Command("listmsg") {

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
				return "listmsg\n" + "\tList all fetched message.";
			}
		});
		cmdMap.put("showmsg", new Command("showmsg") {

			@Override
			public void run(String[] args) throws Exception {

				if (args.length == 0) {
					msgList.forEach(this::showMessage);
				} else if (args.length == 1) {
					for (Message m : msgList) {
						if (args[0].equals(m.getId())) {
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
				if (m.hasAttachments()) {
					System.out.println("Attatchment(s) : " + m.getAttachments().stream()
							.map(a -> a.getFilename() + "(" + a.getSize() + "KB)").collect(Collectors.joining(", ")));
				}
				System.out.println();
				System.out.println(m.getContent());
				System.out.println();
				System.out.println();
			}

			@Override
			public String getUsage() {
				return "showmsg [id]\n" + "\tShow message that have given id.\n"
						+ "\tIf id is not given, show ALL message.";
			}
		});
		cmdMap.put("getattat", new Command("getattat") {

			private String path = new File("").getAbsolutePath();

			@Override
			public void run(String[] args) throws Exception {

				String id;

				if (args.length == 1 && !args[0].equals("--path")) {
					id = args[0];
				} else if (args.length == 3 && args[1].equals("--path")) {
					id = args[0];
					path = args[2].replace("/", File.separator).replace("\\", File.separator);
				} else {
					invalidArgs();
					return;
				}

				System.out.println("download to : " + getPath());
				for (Message m : msgList) {
					if (id.equals(m.getId())) {
						m.getAttachments().forEach(attat -> {
							if (attat.saveSync(getPath(), attat.getFilename()))
								System.out.println(attat.getFilename() + " is Downloaded");
						});
						return;
					}
					System.err.println("Unable to find mail id : " + id);
					System.err.println("Try \"fetch\" and re-check!");
				}
			}

			private String getPath() {
				return path + File.separator;
			}

			@Override
			public String getUsage() {
				return "getattat <id> [--path path]\n" + "\tDownload attatchment of mail that have given id.\n"
						+ "\tIf path is given, the file(s) are stored in there.\n"
						+ "\tElse, files will be downloaded at working directory (" + new File(".").getAbsolutePath()
						+ ")";
			}
		});
		cmdMap.put("exit", new Command("exit") {

			@Override
			public void run(String[] args) throws Exception {
			}

			@Override
			public String getUsage() {
				return "exit\n" + "\tKills the application.";
			}
		});
	}
	
	
	static class NULLCMD extends Command {

		public NULLCMD(String commandString) {
			super(commandString);
		}

		@Override
		protected void run(String[] args) throws Exception {
			System.err.println(getUsage());
			System.err.flush();
		}

		@Override
		public String getUsage() {
			return "Invalid command : " + toString();
		}
		
	};
	
}
