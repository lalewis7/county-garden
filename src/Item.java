package src;
import java.sql.*;
import java.util.*;

public class Item implements TableItem{

    private int item_id, value;
    private String type, name;

    public static String[] TYPES = {"car", "drug", "house", "person"};

    public static void newItem(Connection conn, Scanner scan, Policy policy){
        // create item instance
        Item item = new Item();
        // get contents from user
        item.getContents(scan);
        // double check with user
        if (CountyGarden.question("Are you sure you want to add " + item.getName() + "?", scan)){
            try {
                // get new id
                item.getNewID(conn);
                // create and prepare statement
                PreparedStatement pState = conn.prepareStatement("insert into items (item_id, name, type, value) VALUES (?, ?, ?, ?)");
                pState.setInt(1, item.getID());
                pState.setString(2, item.getName());
                pState.setString(3, item.getType());
                pState.setInt(4, item.getValue());
                pState.execute();
                pState.close();
                // create and prepare covered statement
                pState = conn.prepareStatement("insert into covered (item_id, policy_id) VALUES (?, ?)");
                pState.setInt(1, item.getID());
                pState.setInt(2, policy.getPolicyID());
                pState.execute();
                pState.close();
                System.out.println("Item Added!");
            } catch (SQLException e) {
                CountyGarden.handleError(e);
            }
        }
    }

    public static void removeItem(Connection conn, Scanner scan, Policy policy){
        // get id of item
        int id = CountyGarden.number("Item ID", scan);
        try {
            // make sure item exists and belongs to policy
            PreparedStatement pState = conn.prepareStatement("select * from items natural join covered where item_id = ? and policy_id = ?");
            pState.setInt(1, id);
            pState.setInt(2, policy.getPolicyID());
            ResultSet res = pState.executeQuery();
            if (res.next()){
                // create item instance
                Item item = new Item(res);
                res.close();
                pState.close();
                // delete covered relation schema
                if (CountyGarden.question("Are you sure you want to delete " + item.getName() + " item (" + item.getID() + ")?", scan)){
                    PreparedStatement pState2 = conn.prepareStatement("delete covered where item_id = ? and policy_id = ?");
                    pState2.setInt(1, id);
                    pState2.setInt(2, policy.getPolicyID());
                    pState2.executeUpdate();
                    pState2.close();
                    System.out.println("Item removed from policy.");
                } 
            }
            else
                System.out.println("No item found with matching ID under policy.");
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
    }

    public static Item findItem(Connection conn, Scanner scan){
        int id = CountyGarden.number("Item ID", scan);
        return findItem(conn, id);
    }

    public static Item findItem(Connection conn, int id){
        try {
            PreparedStatement pState = conn.prepareStatement("select * from items where item_id = ?");
            pState.setInt(1, id);
            ResultSet res = pState.executeQuery();
            if (res.next()){
                Item item = new Item(res);
                res.close();
                pState.close();
                return item;
            }
            res.close();
            pState.close();
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
        return null;
    }

    public Item(){}

    public Item(ResultSet res) throws SQLException{
        getContents(res);
    }

    @Override
    public void getContents(Scanner scan) {
        type = TYPES[CountyGarden.menu("Type", TYPES, scan)];
        name = CountyGarden.string("Name", scan, 3, 128);
        value = CountyGarden.number("Value ($)", scan);
    }

    @Override
    public void getContents(ResultSet res) throws SQLException {
        item_id = res.getInt("item_id");
        name = res.getString("name");
        type = res.getString("type");
        value = res.getInt("value");
    }

    @Override
    public void printContents(Connection conn) throws SQLException {
        System.out.println("--- Item "+item_id+" ---");
        System.out.println("ID: \t"+item_id);
        System.out.println("Name: \t"+name);
        System.out.println("Type: \t"+type);
        System.out.println("Value: \t"+value);
    }

    @Override
    public void getNewID(Connection conn) throws SQLException {
        PreparedStatement pState = conn.prepareStatement("select * from items order by item_id desc");
        ResultSet res = pState.executeQuery();
        if (res.next())
            item_id = res.getInt("item_id")+1;
        res.close();
        pState.close();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setID(int item_id){
        this.item_id = item_id;
    }

    public void setValue(int value){
        this.value = value;
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public int getID(){
        return item_id;
    }

    public int getValue(){
        return value;
    }
    
}
