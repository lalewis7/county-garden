package src;
import java.sql.*;
import java.util.*;

public class Policy implements TableItem{

    private int policy_id, cust_id, cost;
    private String type;
    private boolean canceled;
    private String[] dependents;

    public static final String[] TYPES = {"automobile", "health", "home", "liability", "life", "property"};

    public static void newPolicy(Connection conn, Scanner scan, Customer customer){
        // create new policy instance
        Policy policy = new Policy();
        // set customer id and get contents from user
        policy.setCustomerID(customer.getCustomerID());
        policy.getContents(scan);
        // double check they want to add
        if (CountyGarden.question("Are you sure you would like to add this policy?", scan)){
            try{
                // get new policy id
                policy.getNewID(conn);
                // create insert statement
                PreparedStatement pState = conn.prepareStatement("insert into policies (policy_id, cust_id, type, cost, canceled) VALUES (?, ?, ?, ?, ?)");
                pState.setInt(1, policy.getPolicyID());
                pState.setInt(2, policy.getCustomerID());
                pState.setString(3, policy.getType());
                pState.setInt(4, policy.getCost());
                pState.setBoolean(5, policy.isCanceled());
                pState.execute();
                pState.close();
                // add dependents
                for (int i = 0; i < policy.getDepedents().length; i++){
                    PreparedStatement pState2 = conn.prepareStatement("insert into dependents (policy_id, name) VALUES (?, ?)");
                    pState2.setInt(1, policy.getPolicyID());
                    pState2.setString(2, policy.getDepedents()[i]);
                    pState2.execute();
                    pState2.close();
                }
                System.out.println("Policy Added!");
            } catch (SQLException e) {
                CountyGarden.handleError(e);
            }
        }
    }

    public static void removePolicy(Connection conn, Scanner scan, Customer customer){
        // get policy id
        int id = CountyGarden.number("Policy ID", scan);
        try {
            PreparedStatement pState = conn.prepareStatement("select * from policies where cust_id = ? and policy_id = ?");
            pState.setInt(1, customer.getCustomerID());
            pState.setInt(2, id);
            ResultSet res = pState.executeQuery();
            if (res.next()){
                Policy policy = new Policy(res);
                res.close();
                pState.close();
                PreparedStatement pState2 = conn.prepareStatement("update policies set canceled = 1 where cust_id = ? and policy_id = ?");
                if (CountyGarden.question("Are you sure you want to cancel " + policy.getType() + " policy (" + policy.getPolicyID() + ")?", scan)){
                    pState2.setInt(1, customer.getCustomerID());
                    pState2.setInt(2, id);
                    pState2.executeUpdate();
                    pState2.close();
                    System.out.println("Policy canceled.");
                } 
                
            }
            else
                System.out.println("No policy found with matching ID under customer.");
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
    }

    public static Policy findPolicy(Connection conn, Scanner scan){
        int id = CountyGarden.number("Policy ID", scan);
        return findPolicy(conn, id);
    }

    public static Policy findPolicy(Connection conn, int id){
        try {
            PreparedStatement pState = conn.prepareStatement("select * from policies where policy_id = ?");
            pState.setInt(1, id);
            ResultSet res = pState.executeQuery();
            if (res.next()){
                Policy policy = new Policy(res);
                res.close();
                pState.close();
                policy.getDepedents(conn);
                return policy;
            }
            res.close();
            pState.close();
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
        return null;
    }

    public static void newPayment(Connection conn, Scanner scan, Policy policy){
        // get values
        int year = CountyGarden.number("Year of Payment", scan);
        int value = CountyGarden.number("Value of Payment", scan);
        // double check with user
        if (CountyGarden.question("Are you sure you would like to submit this payment?", scan)){
            try{
                PreparedStatement pState = conn.prepareStatement("insert into policy_payments (policy_id, year, amt) values (?, ?, ?)");
                pState.setInt(1, policy.getPolicyID());
                pState.setInt(2, year);
                pState.setInt(3, value);
                pState.execute();
                pState.close();
                System.out.println("Payment submitted!");
            } catch (SQLException e) {
                CountyGarden.handleError(e);
            }
        }
    }

    public Policy(){}

    public Policy(ResultSet res) throws SQLException{
        getContents(res);
    }

    @Override
    public void getContents(Scanner scan) {
        type = TYPES[CountyGarden.menu("Policy Type", TYPES, scan)];
        cost = CountyGarden.number("Cost", scan);
        canceled = false;
        dependents = CountyGarden.stringlist("Dependent", scan, 3, 128);
    }

    @Override
    public void getContents(ResultSet res) throws SQLException {
        policy_id = res.getInt("policy_id");
        cust_id = res.getInt("cust_id");
        cost = res.getInt("cost");
        type = res.getString("type");
        canceled = res.getBoolean("canceled");
    }

    public void getDepedents(Connection conn) throws SQLException{
        PreparedStatement pState = conn.prepareStatement("select * from dependents where policy_id = ?");
        pState.setInt(1, policy_id);
        ResultSet res = pState.executeQuery();
        LinkedList<String> deps = new LinkedList<String>();
        while (res.next()) {
            deps.add(res.getString("name"));
        }
        dependents = deps.toArray(new String[deps.size()]);
        pState.close();
    }

    @Override
    public void printContents(Connection conn) throws SQLException{
        String cust = Customer.findCustomer(conn, cust_id).getName();
        System.out.println("--- Policy "+policy_id+" ---");
        System.out.printf("%-16s %d%n", "ID", policy_id);
        System.out.printf("%-16s %s%n", "Customer", cust+" ("+cust_id+")");
        System.out.printf("%-16s %s%n", "Type", type);
        System.out.printf("%-16s $%,d%n", "Cost", cost);
        System.out.printf("%-16s %s%n", "Canceled", canceled);
        System.out.printf("%-16s %s%n", "Dependents", Arrays.toString(dependents).substring(1, Arrays.toString(dependents).length()-1));
        
        // items
        System.out.println("- Covered Items -");

        PreparedStatement pState = conn.prepareStatement("select * from covered natural join items where policy_id = ?");
        pState.setInt(1, policy_id);
        ResultSet res = pState.executeQuery();

        LinkedList<Item> items = new LinkedList<Item>();

        while (res.next()){
            items.add(new Item(res));
        }

        // print items
        System.out.printf("%-8s%-12s%-46s %s%n", "ID", "Type", "Name", "Value");
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("%-8d%-12s%-46s $%,d%n", items.get(i).getID(), items.get(i).getType(), 
            items.get(i).getName(), items.get(i).getValue());
        }

        res.close();
        pState.close();

        // claims
        System.out.println("- Claims -");

        PreparedStatement pState2 = conn.prepareStatement("select * from claims where policy_id = ?");
        pState2.setInt(1, policy_id);
        ResultSet res2 = pState2.executeQuery();

        LinkedList<Claim> claims = new LinkedList<Claim>();

        while (res2.next()){
            claims.add(new Claim(res2));
        }

        // print claims
        System.out.printf("%-8s%-16s%-12s%n", "ID", "Event", "Description");
        for (int i = 0; i < claims.size(); i++) {
            System.out.printf("%-8d%-16s%-12s%n", claims.get(i).getClaimID(), claims.get(i).getEvent(), 
                claims.get(i).getDescription()==null ? "" : claims.get(i).getDescription());
        }

        res2.close();
        pState2.close();

        // payments
        System.out.println("- Payments -");

        PreparedStatement pState3 = conn.prepareStatement("select * from policy_payments where policy_id = ? order by year asc");
        pState3.setInt(1, policy_id);
        ResultSet res3 = pState3.executeQuery();

        System.out.printf("%-8s %s%n", "Year", "Amount");
        while (res3.next()){
            System.out.printf("%-8d $%,d%n", res3.getInt("year"), res3.getInt("amt"));
        }

        res3.close();
        pState3.close();
        
    }

    @Override
    public void getNewID(Connection conn) throws SQLException {
        PreparedStatement pState = conn.prepareStatement("select * from policies order by policy_id desc");
        ResultSet res = pState.executeQuery();
        if (res.next())
            policy_id = res.getInt("policy_id")+1;
        res.close();
        pState.close();
    }

    public void setPolicyID(int policy_id) {
        this.policy_id = policy_id;
    }

    public void setCustomerID(int cust_id){
        this.cust_id = cust_id;
    }

    public void setCost(int cost){
        this.cost = cost;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setCanceled(boolean canceled){
        this.canceled = canceled;
    }

    public void setDependents(String[] dependents){
        this.dependents = dependents;
    }

    public int getPolicyID(){
        return policy_id;
    }

    public int getCustomerID(){
        return cust_id;
    }

    public int getCost(){
        return cost;
    }

    public String getType(){
        return type;
    }

    public boolean isCanceled(){
        return canceled;
    }

    public String[] getDepedents(){
        return dependents;
    }
    
}
