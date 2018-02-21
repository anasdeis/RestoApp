/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.20.1.4071 modeling language!*/

package ca.mcgill.ecse223.resto.model;
import java.io.Serializable;
import java.util.*;

// line 10 "../../../../../../RestoAppPersistence.ump"
// line 68 "../../../../../../restoAppModel.ump"
public class Bill implements Serializable
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Bill Associations
  private Order order;
  private List<Seat> issuedForSeats;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Bill(Order aOrder, Seat... allIssuedForSeats)
  {
    boolean didAddOrder = setOrder(aOrder);
    if (!didAddOrder)
    {
      throw new RuntimeException("Unable to create bill due to order");
    }
    issuedForSeats = new ArrayList<Seat>();
    boolean didAddIssuedForSeats = setIssuedForSeats(allIssuedForSeats);
    if (!didAddIssuedForSeats)
    {
      throw new RuntimeException("Unable to create Bill, must have at least 1 issuedForSeats");
    }
  }

  //------------------------
  // INTERFACE
  //------------------------

  public Order getOrder()
  {
    return order;
  }

  public Seat getIssuedForSeat(int index)
  {
    Seat aIssuedForSeat = issuedForSeats.get(index);
    return aIssuedForSeat;
  }

  /**
   * only from order.orderItems.seats;
   */
  public List<Seat> getIssuedForSeats()
  {
    List<Seat> newIssuedForSeats = Collections.unmodifiableList(issuedForSeats);
    return newIssuedForSeats;
  }

  public int numberOfIssuedForSeats()
  {
    int number = issuedForSeats.size();
    return number;
  }

  public boolean hasIssuedForSeats()
  {
    boolean has = issuedForSeats.size() > 0;
    return has;
  }

  public int indexOfIssuedForSeat(Seat aIssuedForSeat)
  {
    int index = issuedForSeats.indexOf(aIssuedForSeat);
    return index;
  }

  public boolean setOrder(Order aOrder)
  {
    boolean wasSet = false;
    if (aOrder == null)
    {
      return wasSet;
    }

    Order existingOrder = order;
    order = aOrder;
    if (existingOrder != null && !existingOrder.equals(aOrder))
    {
      existingOrder.removeBill(this);
    }
    order.addBill(this);
    wasSet = true;
    return wasSet;
  }

  public boolean isNumberOfIssuedForSeatsValid()
  {
    boolean isValid = numberOfIssuedForSeats() >= minimumNumberOfIssuedForSeats();
    return isValid;
  }

  public static int minimumNumberOfIssuedForSeats()
  {
    return 1;
  }

  public boolean addIssuedForSeat(Seat aIssuedForSeat)
  {
    boolean wasAdded = false;
    if (issuedForSeats.contains(aIssuedForSeat)) { return false; }
    issuedForSeats.add(aIssuedForSeat);
    if (aIssuedForSeat.indexOfBill(this) != -1)
    {
      wasAdded = true;
    }
    else
    {
      wasAdded = aIssuedForSeat.addBill(this);
      if (!wasAdded)
      {
        issuedForSeats.remove(aIssuedForSeat);
      }
    }
    return wasAdded;
  }

  public boolean removeIssuedForSeat(Seat aIssuedForSeat)
  {
    boolean wasRemoved = false;
    if (!issuedForSeats.contains(aIssuedForSeat))
    {
      return wasRemoved;
    }

    if (numberOfIssuedForSeats() <= minimumNumberOfIssuedForSeats())
    {
      return wasRemoved;
    }

    int oldIndex = issuedForSeats.indexOf(aIssuedForSeat);
    issuedForSeats.remove(oldIndex);
    if (aIssuedForSeat.indexOfBill(this) == -1)
    {
      wasRemoved = true;
    }
    else
    {
      wasRemoved = aIssuedForSeat.removeBill(this);
      if (!wasRemoved)
      {
        issuedForSeats.add(oldIndex,aIssuedForSeat);
      }
    }
    return wasRemoved;
  }

  public boolean setIssuedForSeats(Seat... newIssuedForSeats)
  {
    boolean wasSet = false;
    ArrayList<Seat> verifiedIssuedForSeats = new ArrayList<Seat>();
    for (Seat aIssuedForSeat : newIssuedForSeats)
    {
      if (verifiedIssuedForSeats.contains(aIssuedForSeat))
      {
        continue;
      }
      verifiedIssuedForSeats.add(aIssuedForSeat);
    }

    if (verifiedIssuedForSeats.size() != newIssuedForSeats.length || verifiedIssuedForSeats.size() < minimumNumberOfIssuedForSeats())
    {
      return wasSet;
    }

    ArrayList<Seat> oldIssuedForSeats = new ArrayList<Seat>(issuedForSeats);
    issuedForSeats.clear();
    for (Seat aNewIssuedForSeat : verifiedIssuedForSeats)
    {
      issuedForSeats.add(aNewIssuedForSeat);
      if (oldIssuedForSeats.contains(aNewIssuedForSeat))
      {
        oldIssuedForSeats.remove(aNewIssuedForSeat);
      }
      else
      {
        aNewIssuedForSeat.addBill(this);
      }
    }

    for (Seat anOldIssuedForSeat : oldIssuedForSeats)
    {
      anOldIssuedForSeat.removeBill(this);
    }
    wasSet = true;
    return wasSet;
  }

  public boolean addIssuedForSeatAt(Seat aIssuedForSeat, int index)
  {  
    boolean wasAdded = false;
    if(addIssuedForSeat(aIssuedForSeat))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfIssuedForSeats()) { index = numberOfIssuedForSeats() - 1; }
      issuedForSeats.remove(aIssuedForSeat);
      issuedForSeats.add(index, aIssuedForSeat);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMoveIssuedForSeatAt(Seat aIssuedForSeat, int index)
  {
    boolean wasAdded = false;
    if(issuedForSeats.contains(aIssuedForSeat))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfIssuedForSeats()) { index = numberOfIssuedForSeats() - 1; }
      issuedForSeats.remove(aIssuedForSeat);
      issuedForSeats.add(index, aIssuedForSeat);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addIssuedForSeatAt(aIssuedForSeat, index);
    }
    return wasAdded;
  }

  public void delete()
  {
    Order placeholderOrder = order;
    this.order = null;
    placeholderOrder.removeBill(this);
    ArrayList<Seat> copyOfIssuedForSeats = new ArrayList<Seat>(issuedForSeats);
    issuedForSeats.clear();
    for(Seat aIssuedForSeat : copyOfIssuedForSeats)
    {
      aIssuedForSeat.removeBill(this);
    }
  }
  
  //------------------------
  // DEVELOPER CODE - PROVIDED AS-IS
  //------------------------
  
  // line 13 ../../../../../../RestoAppPersistence.ump
  private static final long serialVersionUID = 2315072607928790501L ;

  
}