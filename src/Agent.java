package src;
import java.sql.*;
import java.util.*;

public class Agent implements TableItem{

    private int agt_id;
    private String name;

    public static void viewAllAgents(Connection conn, Scanner scan){
        try{
            Agent[] agents = Agent.getAll(conn);
            System.out.printf("%-6s%-32s\n", "ID", "Name");
            for (int i = 0; i < agents.length; i++){
                System.out.printf("%-6d%-32s\n", agents[i].getID(), agents[i].getName());
            }
        } catch (SQLException e){
            CountyGarden.handleError(e);
        }
    }

    public static Agent findAgent(Connection conn, Scanner scan){
        int id = CountyGarden.number("Agent ID", scan);
        return findAgent(conn, id);
    }

    public static Agent findAgent(Connection conn, int id){
        try {
            PreparedStatement pState = conn.prepareStatement("select * from agents where agt_id = ?");
            pState.setInt(1, id);
            ResultSet res = pState.executeQuery();
            if (res.next()){
                Agent agt = new Agent(res);
                res.close();
                pState.close();
                return agt;
            }
            res.close();
            pState.close();
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
        return null;
    }

    public static void agentReport(Connection conn, Agent agent){
        try {
            PreparedStatement pState = conn.prepareStatement("select * from policies natural join customers where agt_id = ? and canceled = 0");
            pState.setInt(1, agent.getID());
            ResultSet res = pState.executeQuery();

            LinkedList<Policy> policies = new LinkedList<Policy>();

            while (res.next()){
                policies.add(new Policy(res));
            }

            res.close();
            pState.close();

            int psum = 0, totsum = 0;
            for (int i = 0; i < policies.size(); i++){
                psum += policies.get(i).getCost();
                pState = conn.prepareStatement("select sum(amt) as tot from policy_payments group by policy_id having policy_id = ?");
                pState.setInt(1, policies.get(i).getPolicyID());
                res = pState.executeQuery();
                if (res.next())
                    totsum += res.getInt("tot");
                res.close();
                pState.close();
            }

            System.out.println("--- Agent "+agent.getID()+" Report ---");
            System.out.printf("%-26s %,d\n", "Active Policies", policies.size());
            System.out.printf("%-26s $%,d\n", "Yearly Revenue", psum);
            System.out.printf("%-26s $%,d\n", "Total Revenue", totsum);

        } catch (SQLException e){
            CountyGarden.handleError(e);
        }
    }

    public Agent(){}

    public Agent(ResultSet res) throws SQLException{
        getContents(res);
    }

    @Override
    public void getContents(Scanner scan) {
        name = CountyGarden.string("Name", scan, 4, 128);
    }

    @Override
    public void getContents(ResultSet res) throws SQLException {
        name = res.getString("name");
        agt_id = res.getInt("agt_id");
    }

    @Override
    public void printContents(Connection conn) throws SQLException {
        System.out.println("--- Agent "+agt_id+" ---");
        System.out.printf("%-16s %d\n", "ID", agt_id);
        System.out.printf("%-16s %s\n", "Name", name);

        System.out.println("- Customers -");

        PreparedStatement pState = conn.prepareStatement("select * from customers where agt_id = ?");
        pState.setInt(1, agt_id);
        ResultSet res = pState.executeQuery();

        LinkedList<Customer> customers = new LinkedList<Customer>();

        while (res.next()){
            customers.add(new Customer(res));
        }

        // print customers
        System.out.printf("%-6s%-32s%s\n", "ID", "Name", "SSN");
        for (int i = 0; i < customers.size(); i++) {
            System.out.printf("%-6d%-32s%s\n", customers.get(i).getCustomerID(), customers.get(i).getName(), customers.get(i).getSSN());
        }

        res.close();
        pState.close();

        System.out.println("- Claims -");

        pState = conn.prepareStatement("select * from claims where agt_id = ?");
        pState.setInt(1, agt_id);

        res = pState.executeQuery();

        LinkedList<Claim> claims = new LinkedList<Claim>();

        while (res.next()){
            claims.add(new Claim(res));
        }

        res.close();
        pState.close();

        System.out.printf("%-6s%-32s%-10s%s\n", "ID", "Event", "Serviced", "Description");
        for (int i = 0; i < claims.size(); i++) {
            boolean serviced = false;
            pState = conn.prepareStatement("select * from claim_payment where claim_id = ?");
            pState.setInt(1, claims.get(i).getClaimID());
            res = pState.executeQuery();
            if (res.next())
                serviced = true;
            res.close();
            pState.close();
            if (!serviced){
                pState = conn.prepareStatement("select * from claim_out_payment where claim_id = ?");
                pState.setInt(1, claims.get(i).getClaimID());
                res = pState.executeQuery();
                if (res.next())
                    serviced = true;
                res.close();
                pState.close();
            }
            System.out.printf("%-6d%-32s%-10s%s\n", claims.get(i).getClaimID(), claims.get(i).getEvent(), serviced,
                claims.get(i).getDescription() == null ? "" : claims.get(i).getDescription());
        }
    }

    @Override
    public void getNewID(Connection conn) throws SQLException {
        PreparedStatement pState = conn.prepareStatement("select * from agents order by agt_id desc");
        ResultSet res = pState.executeQuery();
        if (res.next())
            agt_id = res.getInt("agt_id")+1;
        res.close();
        pState.close();
    }

    public static Agent[] getAll(Connection conn) throws SQLException{
        PreparedStatement pState = conn.prepareStatement("select * from agents");
        ResultSet res = pState.executeQuery();
        LinkedList<Agent> agtList = new LinkedList<Agent>();
        while (res.next()) {
            agtList.add(new Agent(res));
        }
        res.close();
        pState.close();
        return agtList.toArray(new Agent[agtList.size()]);
    }

    public void setID(int agt_id) {
        this.agt_id = agt_id;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getID(){
        return agt_id;
    }

    public String getName(){
        return name;
    }
    
}
