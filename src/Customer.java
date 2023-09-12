package src;
import java.sql.*;
import java.util.LinkedList;
import java.util.Scanner;

public class Customer implements TableItem{

    private int cust_id, agt_id;
    private String name, ssn, card_type;
    private String bday;
    private long card_number;

    public static String[] CARD_TYPES = {"americanexpress", "mastercard", "visa"}; 

    public static void newCustomer(Connection conn, Scanner scan) {
        // create customer and get content
        Customer customer = new Customer();
        customer.getContents(scan);
        // double check with user
        if (CountyGarden.question("Are you sure you want to add " + customer.getName() + "?", scan)){
            try {
                // generate new id
                customer.getNewID(conn);
                // get random agent id for new user
                Agent[] allAgents = Agent.getAll(conn);
                customer.setAgentID(allAgents[(int) (Math.random()*allAgents.length)].getID());
                // create insert statement 
                PreparedStatement pState = conn.prepareStatement("insert into customers (cust_id, name, ssn, bday, agt_id, card_type, card_number) VALUES (?, ?, ?, ?, ?, ?, ?)");
                pState.setInt(1, customer.getCustomerID());
                pState.setString(2, customer.getName());
                pState.setString(3, customer.getSSN());
                pState.setString(4, customer.getBday());
                pState.setInt(5, customer.getAgentID());
                pState.setString(6, customer.getCardType());
                pState.setLong(7, customer.getCardNumber());
                pState.execute();
                System.out.println("Customer Added!");
            } catch (SQLException e) {
                CountyGarden.handleError(e);
            }
        }
    }

    public static void viewAllCustomers(Connection conn, Scanner scan){
        try{
            Customer[] customers = Customer.getAll(conn);
            System.out.printf("%-6s%-32s%-12s\n", "ID", "Name", "Agent ID");
            for (int i = 0; i < customers.length; i++){
                System.out.printf("%-6d%-32s%-12d\n", customers[i].getCustomerID(), customers[i].getName(), customers[i].getAgentID());
            }
        } catch (SQLException e){
            CountyGarden.handleError(e);
        }
    }

    public static Customer findCustomer(Connection conn, Scanner scan){
        int id = CountyGarden.number("Customer ID", scan);
        return findCustomer(conn, id);
    }

    public static Customer findCustomer(Connection conn, int id){
        try {
            PreparedStatement pState = conn.prepareStatement("select * from customers where cust_id = ?");
            pState.setInt(1, id);
            ResultSet res = pState.executeQuery();
            if (res.next()){
                Customer cust = new Customer(res);
                res.close();
                pState.close();
                return cust;
            }
            res.close();
            pState.close();
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
        return null;
    }

    public Customer(){}

    public Customer(ResultSet res) throws SQLException {
        getContents(res);
    }

    @Override
    public void getContents(Scanner scan) {
        name = CountyGarden.string("Name", scan, 3, 128);
        ssn = CountyGarden.string("Social Security Number", scan, 11, 11);
        card_type = CARD_TYPES[CountyGarden.menu("Card Type", CARD_TYPES, scan)];
        card_number = CountyGarden.getLong("Card Number", scan);
        bday = CountyGarden.date("Birthday", scan);
    }

    @Override
    public void getContents(ResultSet res) throws SQLException {
        cust_id = res.getInt("cust_id");
        agt_id = res.getInt("agt_id");
        card_number = res.getLong("card_number");
        name = res.getString("name");
        ssn = res.getString("ssn");
        card_type = res.getString("card_type");
        bday = res.getString("bday");
    }

    @Override
    public void printContents(Connection conn) throws SQLException{
        String agt = Agent.findAgent(conn, agt_id).getName();
        System.out.println("--- Customer "+cust_id+" ---");
        System.out.printf("%-16s %d\n", "ID", cust_id);
        System.out.printf("%-16s %s\n", "Name", name);
        System.out.printf("%-16s %s\n", "Agent", agt+" (ID:"+agt_id+")");
        System.out.printf("%-16s %s\n", "SSN", ssn);
        System.out.printf("%-16s %s\n", "Card Type", card_type);
        System.out.printf("%-16s %d\n", "Card Number", card_number);
        System.out.printf("%-16s %s\n", "Birthday", bday);

        System.out.println("- Policies -");

        PreparedStatement pState = conn.prepareStatement("select * from policies where cust_id = ?");
        pState.setInt(1, cust_id);
        ResultSet res = pState.executeQuery();

        LinkedList<Policy> policies = new LinkedList<Policy>();

        while (res.next()){
            policies.add(new Policy(res));
        }

        res.close();
        pState.close();

        // add dependents
        for (int i = 0; i < policies.size(); i++) {
            policies.get(i).getDepedents(conn);
        }

        // print policies
        System.out.printf("%-6s%-16s %-12s%-16s\n", "ID", "Type", "Cost", "Dependents");
        for (int i = 0; i < policies.size(); i++) {
            System.out.printf("%-6d%-16s $%,-11d%-16d%S\n", policies.get(i).getPolicyID(), policies.get(i).getType(), 
                policies.get(i).getCost(), policies.get(i).getDepedents().length, policies.get(i).isCanceled() ? "<CANCELED>" : "");
        }
    }

    @Override
    public void getNewID(Connection conn) throws SQLException {
        PreparedStatement pState = conn.prepareStatement("select * from customers order by cust_id desc");
        ResultSet res = pState.executeQuery();
        if (res.next())
            cust_id = res.getInt("cust_id")+1;
        res.close();
        pState.close();
    }

    public static Customer[] getAll(Connection conn) throws SQLException{
        PreparedStatement pState = conn.prepareStatement("select * from customers");
        ResultSet res = pState.executeQuery();
        LinkedList<Customer> custList = new LinkedList<Customer>();
        while (res.next()) {
            custList.add(new Customer(res));
        }
        res.close();
        pState.close();
        return custList.toArray(new Customer[custList.size()]);
    }

    public void setCustomerID(int cust_id){
        this.cust_id = cust_id;
    }

    public void setAgentID(int agt_id){
        this.agt_id = agt_id;
    }

    public void setCardNumber(long card_number){
        this.card_number = card_number;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setSSN(String ssn){
        this.ssn = ssn;
    }

    public void setCardType(String card_type){
        this.card_type = card_type;
    }

    public void setBday(String bday){
        this.bday = bday;
    }

    public int getCustomerID(){
        return cust_id;
    }

    public int getAgentID(){
        return agt_id;
    }

    public long getCardNumber(){
        return card_number;
    }

    public String getName(){
        return name;
    }

    public String getSSN(){
        return ssn;
    }

    public String getCardType(){
        return card_type;
    }

    public String getBday(){
        return bday;
    }
    
}
