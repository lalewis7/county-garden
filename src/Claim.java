package src;
import java.sql.*;
import java.util.*;

public class Claim implements TableItem{

    private int claim_id, item_id, adj_id, agt_id, policy_id;
    private String event, description;

    public static void newClaim(Connection conn, Scanner scan, Policy policy){
        try{
            // create claim instance
            Claim claim = new Claim();
            // set policy id
            claim.setPolicyID(policy.getPolicyID());
            // get item for claim
            claim.setItemID(CountyGarden.number("Item ID", scan));
            // set random adjuster
            Adjuster[] allAdjusters = Adjuster.getAll(conn);
            claim.setAdjusterID(allAdjusters[(int) (Math.random()*allAdjusters.length)].getID());
            // get customer who owns policy to get their agent
            PreparedStatement pState = conn.prepareStatement("select * from customers where cust_id = ?");
            pState.setInt(1, policy.getCustomerID());
            ResultSet res = pState.executeQuery();
            res.next();
            Customer cust = new Customer(res);
            res.close();
            pState.close();
            claim.setAgentID(cust.getAgentID());
            // make sure item exists and belongs to policy
            pState = conn.prepareStatement("select * from items natural join covered where item_id = ? and policy_id = ?");
            pState.setInt(1, claim.getItemID());
            pState.setInt(2, policy.getPolicyID());
            res = pState.executeQuery();
            if (res.next()){
                res.close();
                pState.close();
                // get contents of claim
                claim.getContents(scan);
                // double check with user
                if (CountyGarden.question("Are you sure you would like to add this claim?", scan)){
                    // get new claim id
                    claim.getNewID(conn);
                    // prepare insert statement
                    pState = conn.prepareStatement("insert into claims (claim_id, item_id, adj_id, agt_id, policy_id, event, description) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    pState.setInt(1, claim.getClaimID());
                    pState.setInt(2, claim.getItemID());
                    pState.setInt(3, claim.getAdjusterID());
                    pState.setInt(4, claim.getAgentID());
                    pState.setInt(5, claim.getPolicyID());
                    pState.setString(6, claim.getEvent());
                    pState.setString(7, claim.getDescription());
                    pState.execute();
                    pState.close();
                    System.out.println("Claim Made!");
                }
            } else {
                System.out.println("No item found with matching ID under policy.");
            }
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
    }

    public static Claim findClaim(Connection conn, Scanner scan){
        int id = CountyGarden.number("Claim ID", scan);
        return findClaim(conn, id);
    }

    public static Claim findClaim(Connection conn, int id){
        try {
            PreparedStatement pState = conn.prepareStatement("select * from claims where claim_id = ?");
            pState.setInt(1, id);
            ResultSet res = pState.executeQuery();
            if (res.next()){
                Claim claim = new Claim(res);
                res.close();
                pState.close();
                return claim;
            }
            res.close();
            pState.close();
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
        return null;
    }

    public static void newPayment(Connection conn, Scanner scan, Claim claim){
        // get values
        int value = CountyGarden.number("Value of Payment", scan);
        // double check with user
        if (CountyGarden.question("Are you sure you would like to submit this payment?", scan)){
            try{
                PreparedStatement pState = conn.prepareStatement("insert into claim_payment (claim_id, amt) values (?, ?)");
                pState.setInt(1, claim.getClaimID());
                pState.setInt(2, value);
                pState.execute();
                pState.close();
                System.out.println("Payment submitted!");
            } catch (SQLException e) {
                CountyGarden.handleError(e);
            }
        }
    }

    public static void newOutsourcePayment(Connection conn, Scanner scan, Claim claim){
        // get values
        String name = CountyGarden.string("Company name", scan, 3, 64);
        String type = CountyGarden.string("Company type", scan, 3, 64);
        String email = CountyGarden.string("Company email", scan, 3, 64);
        int phone = CountyGarden.number("Company phone number", scan);
        int value = CountyGarden.number("Value of Payment", scan);
        // double check with user
        if (CountyGarden.question("Are you sure you would like to submit this payment?", scan)){
            try{
                PreparedStatement pState = conn.prepareStatement("insert into claim_out_payment (claim_id, name, type, email, phone, amt) values (?, ?, ?, ?, ?, ?)");
                pState.setInt(1, claim.getClaimID());
                pState.setString(2, name);
                pState.setString(3, type);
                pState.setString(4, email);
                pState.setInt(5, phone);
                pState.setInt(6, value);
                pState.execute();
                pState.close();
                System.out.println("Payment submitted!");
            } catch (SQLException e) {
                CountyGarden.handleError(e);
            }
        }
    }

    public Claim(){}

    public Claim(ResultSet res) throws SQLException{
        getContents(res);
    }

    @Override
    public void getContents(Scanner scan) {
        event = CountyGarden.string("Event", scan, 3, 128);
        description = CountyGarden.string("Description", scan, 3, 128);
    }

    @Override
    public void getContents(ResultSet res) throws SQLException {
        claim_id = res.getInt("claim_id");
        item_id = res.getInt("item_id");
        adj_id = res.getInt("adj_id");
        agt_id = res.getInt("agt_id");
        policy_id = res.getInt("policy_id");
        event = res.getString("event");
        description = res.getString("description");
    }

    @Override
    public void printContents(Connection conn) throws SQLException {
        String agent = Agent.findAgent(conn, agt_id).getName();
        String adjuster = Adjuster.findAdjuster(conn, adj_id).getName();
        Item i = Item.findItem(conn, item_id);
        String item = i.getName();
        String policy = Policy.findPolicy(conn, policy_id).getType();
        System.out.println("--- Claim "+claim_id+" ---");
        System.out.printf("%-16s %d\n", "ID", claim_id);
        System.out.printf("%-16s %s (ID:%d) ($%,d)\n", "Item", item, item_id, i.getValue());
        System.out.printf("%-16s %s (ID:%d)\n", "Policy", policy, policy_id);
        System.out.printf("%-16s %s (ID:%d)\n", "Agent", agent, agt_id);
        System.out.printf("%-16s %s (ID:%d)\n", "Adjuster", adjuster, adj_id);
        System.out.printf("%-16s %s\n", "Event", event);
        System.out.printf("%-16s %s\n", "Description", description == null ? "" : description);

        System.out.println("- Payments -");

        PreparedStatement pState = conn.prepareStatement("select * from claim_payment where claim_id = ?");
        pState.setInt(1, claim_id);
        ResultSet res = pState.executeQuery();

        while (res.next()){
            System.out.printf("$%,d\n", res.getInt("amt"));
        }

        res.close();
        pState.close();

        System.out.println("- Outsourced Payments -");

        pState = conn.prepareStatement("select * from claim_out_payment where claim_id = ?");
        pState.setInt(1, claim_id);
        res = pState.executeQuery();

        System.out.printf("%-12s%-16s%-18s%-24s%s\n", "Amount", "Type", "Name", "Email", "Phone");
        while (res.next()){
            System.out.printf("%,-12d%-16s%-18s%-24s%s\n", res.getInt("amt"), res.getString("type"), res.getString("name"), res.getString("email"), res.getString("phone"));
        }

        pState.close();
        res.close();

    }

    @Override
    public void getNewID(Connection conn) throws SQLException {
        PreparedStatement pState = conn.prepareStatement("select * from claims order by claim_id desc");
        ResultSet res = pState.executeQuery();
        if (res.next())
            claim_id = res.getInt("claim_id")+1;
        else
            claim_id = 1;
        res.close();
        pState.close();
    }

    public void setClaimID(int claim_id) {
        this.claim_id = claim_id;
    }

    public void setItemID(int item_id) {
        this.item_id = item_id;
    }

    public void setAdjusterID(int adj_id){
        this.adj_id = adj_id;
    }

    public void setAgentID(int agt_id){
        this.agt_id = agt_id;
    }

    public void setPolicyID(int policy_id){
        this.policy_id = policy_id;
    }

    public void setEvent(String event){
        this.event = event;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public int getClaimID(){
        return claim_id;
    }

    public int getItemID(){
        return item_id;
    }

    public int getAdjusterID(){
        return adj_id;
    }

    public int getAgentID(){
        return agt_id;
    }

    public int getPolicyID(){
        return policy_id;
    }

    public String getEvent(){
        return event;
    }

    public String getDescription(){
        return description;
    }
    
}
