package src;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class AgentUI extends UserInterface{

    public AgentUI(Scanner scanner, Connection conn) {
        super(scanner, conn);
    }

    @Override
    public void start() {
        while (true) {
            int menu1 = CountyGarden.menu("--- County Garden Insurance Agent Interface ---", new String[]{"View All Agents", "View Agent", "Back"}, scan);
            if (menu1 == 0){ // view all
                Agent.viewAllAgents(conn, scan);
            } else if (menu1 == 1){ // view single
                Agent agent = Agent.findAgent(conn, scan);
                if (agent != null)
                    agentUI(agent);
                else
                    System.out.println("No agent found with matching ID. Use view all agents to find agents.");
            } else {
                break;
            }
        }
    }

    private void agentUI(Agent agent){
        while (true) {
            try {
                agent.printContents(conn);
            } catch (SQLException e){
                CountyGarden.handleError(e);
                break;
            }
            int menu1 = CountyGarden.menu("-------------------", new String[]{"View Customer", "View Claim", "Agent Revenue Report", "Back"}, scan);
            if (menu1 == 0){ // view customer
                Customer customer = Customer.findCustomer(conn, scan);
                if (customer != null)
                    customerUI(customer);
            } else if (menu1 == 1){ // view claim
                Claim claim = Claim.findClaim(conn, scan);
                if (claim != null && claim.getAgentID() == agent.getID())
                    claimUI(claim);
                else
                    System.out.println("No Claim found with matching ID under agent.");
            } else if (menu1 == 2){ // report
                Agent.agentReport(conn, agent);
            } else { // back
                break;
            }
        }
    }

    private void customerUI(Customer customer){
        while (true) {
            try {
                customer.printContents(conn);
            } catch (SQLException e){
                CountyGarden.handleError(e);
                break;
            }
            int menu1 = CountyGarden.menu("-------------------", new String[]{"View Policy", "Cancel Policy", "Back"}, scan);
            if (menu1 == 0){ // view policy
                Policy policy = Policy.findPolicy(conn, scan);
                if (policy != null && policy.getCustomerID() == customer.getCustomerID())
                    policyUI(policy);
                else
                    System.out.println("No Policy found with matching ID under customer.");
            } else if (menu1 == 1){ // remove policy
                Policy.removePolicy(conn, scan, customer);
            } else { // back
                break;
            }
        }
    }

    private void policyUI(Policy policy){
        while (true){
            try {
                policy.printContents(conn);
            } catch(SQLException e){
                CountyGarden.handleError(e);
                break;
            }
            int menu1 = CountyGarden.menu("-------------------", new String[]{"View Claim", "Back"}, scan);
            if (menu1 == 0){ // add item
                Claim claim = Claim.findClaim(conn, scan);
                if (claim != null && claim.getPolicyID() == policy.getPolicyID())
                    claimUI(claim);
                else
                    System.out.println("No Claim found with matching ID under policy.");
            } else {
                break;
            }
        }
    }

    private void claimUI(Claim claim){
        while (true){
            try {
                claim.printContents(conn);
            } catch(SQLException e){
                CountyGarden.handleError(e);
                break;
            }
            int menu1 = CountyGarden.menu("-------------------", new String[]{"Back"}, scan);
            if (menu1 == 0){
                break;
            }
        }
    }
    
}
