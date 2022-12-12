package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import loadbalance.ILoadBalancer;
import datastore.BookingDetails;

public class Client implements IClient {

  private static ArrayList<Integer> serverPorts = new ArrayList<>();
  private static Object List;

  public static void main(String args[]) {

    for (int i = 0; i < args.length; i++) {
      serverPorts.add(Integer.parseInt(args[i]));
    }
    Random rand = new Random();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    Scanner scanner = new Scanner(System.in);

    System.out.println("**************** Welcome to TicketMaster ****************");

    while (true) {
      int port = serverPorts.get(rand.nextInt(serverPorts.size()));
      try {
        Registry reg = LocateRegistry.getRegistry(port);
        ILoadBalancer lb = (ILoadBalancer) reg.lookup("TicketMaster" + port);
        System.out.println("Enter option 1.Book Ticket 2.Get Ticket 3. Cancel Booking");
        String option = scanner.nextLine();
        if (option.equalsIgnoreCase("1")) {
          System.out.println(
              "Enter the theatre you want to book tickets from: \n 1. AMC Cinemas 2. Regal Cinemas 3. Cinemark");
          String theatre = scanner.nextLine();
          if (theatre.equals("1")) {
            theatre = "AMC";
          } else if (theatre.equals("2")) {
            theatre = "REG";
          } else if (theatre.equals("3")) {
            theatre = "CIN";
          }
          List<String> availableSeats = lb.getAvailableSeats(theatre);
          if (availableSeats.size() == 0) {
            System.out.println("Sorry tickets not available. Try another theatre");
            continue;
          }
          System.out.println("The available seats are: ");
          for (String seat : availableSeats) {
            System.out.print(seat + " ");
          }
          System.out.println(
              "\nEnter the seats you want to book as space separated number: (Max 5)");
          String[] seats = scanner.nextLine().split(" ");
          while (seats.length > 5 || !availableSeats.containsAll(Arrays.asList(seats))) {
            System.out.println(
                "Exceeded booking limit or chosen unavailable tickets. Please enter again.");
            seats = scanner.nextLine().split(" ");
          }
          if (!lb.blockSeats(theatre, Arrays.asList(seats))) {
            System.out.println("Sorry the selected seats have been booked.");
            continue;
          }
          System.out.println("\n Please enter your name: ");
          String name = scanner.nextLine();
          System.out.println("\n Please enter your email id: ");
          String mailId = scanner.nextLine();
          String ticketNum = lb.bookTicket(name, mailId, theatre, Arrays.asList(seats));
          if (ticketNum == null) {
            System.out.println("Sorry transaction failed. Try again.");
            continue;
          }
          System.out.println(
              "Transaction Successful\n" + "Ticket Details are:\n" + "Name: " + name + "\nE-mail:"
                  + mailId + "Ticket No:" + ticketNum);

        } else if (option.equalsIgnoreCase("2")) {
          System.out.println("Enter Ticket Number");
          String num = scanner.nextLine();
          System.out.println("Enter theatre");
          String theatre = scanner.nextLine();
          BookingDetails ticketDetails = lb.getTicketDetails(num, theatre);
          if (ticketDetails != null) {
            System.out.println("Ticket Details:\n" + ticketDetails);
          } else {
            System.out.println("Incorrect ticket number");
          }
        } else if (option.equalsIgnoreCase("3")) {
          System.out.println("Enter Ticket Number");
          String num = scanner.nextLine();
          System.out.println("Enter theatre");
          String theatre = scanner.nextLine();
          if (lb.deleteTicket(num, theatre)) {
            System.out.println("Ticket deleted successfully");
          } else {
            System.out.println("Sorry. Unable to cancel booking");
          }
        }
        System.out.println("Do you want to continue? (Yes / No)");
        if (scanner.nextLine().equalsIgnoreCase("no")) {
          break;
        }
      } catch (Exception e) {
        System.out.println("Encounter Exception: " + e);
        continue;
      }
    }

  }


}

