package ca.mcgill.ecse223.resto.controller;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import ca.mcgill.ecse223.resto.application.RestoApplication;
import ca.mcgill.ecse223.resto.model.Menu;
import ca.mcgill.ecse223.resto.model.MenuItem;
import ca.mcgill.ecse223.resto.model.MenuItem.ItemCategory;
import ca.mcgill.ecse223.resto.model.Order;
import ca.mcgill.ecse223.resto.model.PricedMenuItem;
import ca.mcgill.ecse223.resto.model.Reservation;
import ca.mcgill.ecse223.resto.model.RestoApp;
import ca.mcgill.ecse223.resto.model.Seat;
import ca.mcgill.ecse223.resto.model.Table;

public class RestoAppController {

	public static List<Table> getTables() {
		return RestoApplication.getRestoApp().getCurrentTables();
	}

	public static void createTable(int numberOfSeats, int x, int y, int width, int length)
			throws InvalidInputException {
		RestoApp ra = RestoApplication.getRestoApp();
		int generatedTableNumber = RestoAppController.generateTableNumber();

		String error = "";
		if (numberOfSeats <= 0) {
			error = "The number of seats must be greater than 0. ";
		}
		if (x < 0) {
			error = error + "The x location must be non-negative. ";
		}
		if (y < 0) {
			error = error + "The y location must be non-negative. ";
		}
		if (width <= 0) {
			error = error + "The width must be non-negative. ";
		}
		if (length <= 0) {
			error = error + "The location must be non-negative. ";
		}
		if (isDuplicateTableNumber(generatedTableNumber)) {
			error = error + "Table with table number " + generatedTableNumber + " already exists. ";
		}
		if (isTableOverlapping(x, y, width, length)) {
			error = error + "Table will overlap with another table. ";
		}
		if (error.length() > 0) {
			throw new InvalidInputException(error.trim());
		}

		try {
			Table table = new Table(generatedTableNumber, x, y, width, length, ra);

			for (int i = 0; i < numberOfSeats; i++) {
				table.addCurrentSeat(new Seat());
			}
			ra.addCurrentTable(table);
			RestoApplication.save();

		} catch (Exception e) {
			error = e.getMessage();
			throw new InvalidInputException(e.getMessage());
		}
	}

	public static void removeTable(Integer selectedTableNumber) throws InvalidInputException {
		RestoApp ra = RestoApplication.getRestoApp();
		String error = "";

		try {
			for (Table table : ra.getCurrentTables()) {
				if (selectedTableNumber.equals(table.getNumber())) {
					ra.removeCurrentTable(table);
					break;
				}
			}

			RestoApplication.save();

		} catch (Exception e) {
			error = e.getMessage();
			throw new InvalidInputException(e.getMessage());
		}
	}

	public static void reserveTable(Date aDate, Time aTime, int aNumberInParty, String aContactName,
			String aContactEmailAddress, String aContactPhoneNumber, Table[] aTables, Integer selectedTableNumber)
			throws InvalidInputException {
		String error = "";
		RestoApp ra = RestoApplication.getRestoApp();
		long time = System.currentTimeMillis();
		java.sql.Date dateNow = new java.sql.Date(time);

		if (aDate == null || dateNow.compareTo(aDate) > 0) {
			error = error + "Please enter the date of the reservation. Make sure it is in the future.";
		}

		if (aTime == null) {
			error = error + "Please enter the time of the reservation";
		}

		if (aContactName == null) {
			error = error + "Please enter your name";
		}

		if (aContactEmailAddress == null) {
			error = error + "Please enter your email address";
		}

		if (aContactPhoneNumber == null) {
			error = error + "Please enter your phone number";
		}

		if (aNumberInParty <= 0) {
			error = error + "Please enter a positive number (larger than 0)";
		}

		if (aContactPhoneNumber == null) {
			error = error + "Please enter your phone number";
		}

		List<Table> allTables = ra.getCurrentTables();
		Table[] allTablesArray = allTables.toArray(new Table[allTables.size()]);
		int seatCapacity = 0;

		try {
			for (Table table : allTables) {
				if (selectedTableNumber.equals(table.getNumber())) {

					seatCapacity = table.numberOfCurrentSeats();

					List<Reservation> reservationsInTable = table.getReservations();

					for (Reservation reservationa : reservationsInTable) {
						if ((reservationa.getDate().compareTo(aDate) == 0)
								&& (reservationa.getTime().compareTo(aTime) == 0)) {
							error = error + "Please choose another time, table reserved at the requested time";
							break;
						}
					}

					if (seatCapacity < aNumberInParty) {
						error = error + "There are no enough seats on this table. Please choose another table";
					}

					Reservation reservation = new Reservation(aDate, aTime, aNumberInParty, aContactName,
							aContactEmailAddress, aContactPhoneNumber, aTables);
					table.addReservation(reservation);
					break;
				}
			}
			RestoApplication.save();
		} catch (Exception e) {
			error = e.getMessage();
			throw new InvalidInputException(e.getMessage());
		}

	}

	public static void startOrder(List<Table> tables) throws InvalidInputException {

		RestoApp r = RestoApplication.getRestoApp();
		List<Table> currentTables = r.getCurrentTables();
		String error = "";

		if (tables == null) {
			throw (new InvalidInputException("tables is null, please add a table! "));
		}
		for (Table table : tables) {
			if (!currentTables.contains(table)) {
				throw (new InvalidInputException("Not a current table! "));
			}
		}

		boolean orderCreated = false;
		Order newOrder = null;

		for (Table table : tables) {
			if (orderCreated) {
				table.addToOrder(newOrder);
			} else {
				Order lastOrder = null;
				if (table.numberOfOrders() > 0) {
					lastOrder = table.getOrder(table.numberOfOrders() - 1);
				}

				table.startOrder();
				if ((table.numberOfOrders() > 0) && (!table.getOrder(table.numberOfOrders() - 1).equals(lastOrder))) {
					orderCreated = true;
					newOrder = table.getOrder(table.numberOfOrders() - 1);
				}
			}
		}

		if (!orderCreated) {
			throw (new InvalidInputException("No order created! "));
		}

		r.addCurrentOrder(newOrder);

		try {

			RestoApplication.save();

		} catch (RuntimeException e) {
			throw (new InvalidInputException(e.getMessage()));
		}
	}

	public static void endOrder(Order order) throws InvalidInputException {

		RestoApp r = RestoApplication.getRestoApp();
		List<Order> currentOrders = r.getCurrentOrders();
		String error = "";

		if (order == null) {
			throw (new InvalidInputException("order is null, please add an order! "));
		}

		if (!currentOrders.contains(order)) {
			throw (new InvalidInputException("Not a current order! "));
		}

		List<Table> tables = order.getTables();

		for (Table table : tables) {
			if ((table.numberOfOrders() > 0) && (table.getOrder(table.numberOfOrders() - 1).equals(order))) {
				table.endOrder(order);
			}
		}

		if (allTablesAvailableOrDifferentCurrentOrder(tables, order)) {
			r.removeCurrentOrder(order);
		}

		try {

			RestoApplication.save();

		} catch (RuntimeException e) {
			throw (new InvalidInputException(e.getMessage()));
		}
	}

	private static boolean allTablesAvailableOrDifferentCurrentOrder(List<Table> tables, Order order) {
		for (Table table : tables) {
			if ((table.getStatusFullName() == "Available")
					|| (!((table.getOrder(table.numberOfOrders() - 1)).equals(order)))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isDuplicateTableNumber(int number) {
		RestoApp ra = RestoApplication.getRestoApp();
		List<Table> tables = ra.getCurrentTables();
		for (int i = 0; i < tables.size(); i++) {
			if (tables.get(i).getNumber() == number) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTableReserved(Integer selectedTableNumber) {
		RestoApp ra = RestoApplication.getRestoApp();
		Table table = ra.getCurrentTable(selectedTableNumber);
		if (table.hasReservations()) {
			return true;
		} else {
			return false;
		}

	}

	public static int generateTableNumber() {
		RestoApp ra = RestoApplication.getRestoApp();
		List<Table> tables = ra.getCurrentTables();
		if (tables.size() == 0) {
			return 1;
		} else {
			return tables.get(tables.size() - 1).getNumber() + 1;
		}

	}

	// Checks if the table that you are trying to input overlaps with another
	// It starts counting at index [0][0] for x and y.
	public static boolean isTableOverlapping(int x, int y, int width, int length) {
		RestoApp ra = RestoApplication.getRestoApp();
		List<Table> tables = ra.getCurrentTables();
		int[][] tableMap = new int[9999][9999];

		for (int k = 0; k < tables.size(); k++) {
			for (int i = 0; i < tables.get(k).getWidth(); i++) {
				for (int j = 0; j < tables.get(k).getLength(); j++) {
					tableMap[tables.get(k).getX() + i][tables.get(k).getY() + j] = 1;
				}
			}
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < length; j++) {
				if (tableMap[i + x][j + y] == 1) {
					return true;
				}
			}
		}
		return false;
	}

	public static ArrayList<MenuItem> getMenuItem(ItemCategory itemCategory) throws InvalidInputException {

		if (itemCategory.equals(null)) {
			throw new InvalidInputException("Item category is null");
		}

		RestoApp ra = RestoApplication.getRestoApp();
		ArrayList<MenuItem> itemList = new ArrayList<MenuItem>();
		Menu menu = ra.getMenu();

		for (int j = 0; j < menu.numberOfMenuItems(); j++) {
			MenuItem currItem = menu.getMenuItem(j);
			if (currItem.getItemCategory().equals(itemCategory)) {
				itemList.add(currItem);
			}
		}
		return itemList;
	}

	public static void moveTable(Table table, int x, int y) throws InvalidInputException {

		String error = "";
		if (table == null) {
			error += "Table cannot be null! ";
		}

		if (x < 0) {
			error += "x cannot be negative! ";
		}

		if (y < 0) {
			error += "y cannot be negative! ";
		}

		if (error.length() > 0) {
			throw (new InvalidInputException(error.trim()));
		}

		int width = table.getWidth();
		int length = table.getLength();
		RestoApp r = RestoApplication.getRestoApp();
		List<Table> currentTables = r.getCurrentTables();

		if ((table.doesOverlap(x, y, width, length))) {
			throw (new InvalidInputException("Tables overlap! Change table configuration."));
		}

		table.setX(x);
		table.setY(y);

		try {

			RestoApplication.save();

		} catch (RuntimeException e) {
			throw (new InvalidInputException(e.getMessage()));
		}
	}

	public static void addMenuItem(String name, String category, String price) throws InvalidInputException {
		String error = "";
		RestoApp ra = RestoApplication.getRestoApp();

		 MenuItem newMenuItem = new MenuItem(name, ra.getMenu());

		
		 if (category.equals("Appetizer")) {
		 newMenuItem.setItemCategory(MenuItem.ItemCategory.Appetizer); } else if
		 (category.equalsIgnoreCase("Main")) {
		 newMenuItem.setItemCategory(MenuItem.ItemCategory.Main); } else if
		 (category.equalsIgnoreCase("Dessert")) {
		 newMenuItem.setItemCategory(MenuItem.ItemCategory.Dessert); } else if
		 (category.equalsIgnoreCase("Alcoholic Beverage")) {
		 newMenuItem.setItemCategory(MenuItem.ItemCategory.AlcoholicBeverage); } else
		 if (category.equalsIgnoreCase("Non Alcoholic Beverage")) {
		 newMenuItem.setItemCategory(MenuItem.ItemCategory.NonAlcoholicBeverage); }
		

		int dotCount = 0;
		for (int i = 0; i < price.length(); i++) {

			if ((price.charAt(i) > 47 && price.charAt(i) < 58) || price.charAt(i) == 46) { // Check if price is only
																							// numbers and .
				if (price.charAt(i) == 46) {
					dotCount++; // Keep track of how many . (there can only be 1)
				}
			} else {
				throw new InvalidInputException("Invalid price format (ex: 2.99");
			}

		}

		if (dotCount > 1) {
			throw new InvalidInputException("You cannot have more than one decimal point");
		}

		double priceDouble = Double.parseDouble(price);

		if (priceDouble < 0) {
			throw new InvalidInputException("Cannot have a negative price");
		} else if (priceDouble == 0) {
			throw new InvalidInputException("Cannot have 0 as price");
		} else {
			// PricedMenuItem newPricedMenuItem = ra.addPricedMenuItem(priceDouble,
			// newMenuItem);
		}

		RestoApplication.save();

	}

	public static void updateTable(Table table, int newNumber, int numOfSeats) throws InvalidInputException {
		RestoApp ra = RestoApplication.getRestoApp();

		String error = "";
		if (table == null) {
			error = error + "The table does not exist, please add the table first";
		}
		if (numOfSeats <= 0) {
			error = error + "The number of seats must be greater than 0";
		}

		if (newNumber < 0) {
			error = error + "You entered a negative number. Please add a positive table number";
		}

		if (table.hasReservations() == true) {
			error = error + "The table is reserves, you can not update its details";
		}

		if (isDuplicateTableNumber(newNumber)) {
			error = error + "The new table number already exists. Please choose another number";
		}

		List<Order> currentOrders = ra.getCurrentOrders();
		for (int i = currentOrders.size(); i > 0; i--) {
			List<Table> tables = currentOrders.get(i).getTables();
			boolean inUse = tables.contains(table);

			if (inUse == true) {
				error = "Table is in use, choose another table";
			}
		}

		try {
			table.setNumber(newNumber);
			int n = table.numberOfCurrentSeats();

			if (numOfSeats > n) {
				for (int j = numOfSeats - n; j > 0; j--) {
					table.addCurrentSeat(new Seat());
				}
			} else if (numOfSeats < n) {
				for (int j = n - numOfSeats; j > 0; j--) {
					Seat seatToRemove = table.getCurrentSeat(0);
					table.removeCurrentSeat(seatToRemove);
				}
			}
			RestoApplication.save();
		} catch (Exception e) {
			error = e.getMessage();
			throw new InvalidInputException(e.getMessage());
		}
	}
}
