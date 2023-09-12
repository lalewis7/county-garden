package src;
import java.sql.*;
import java.util.Scanner;

public class CustomerUI extends UserInterface{

    public CustomerUI(Scanner scanner, Connection conn) {
        super(scanner, conn);
    }

    @Override
    public void start() {
        while (true) {
            int menu1 = CountyGarden.menu("--- County Garden Insurance Customer Interface ---", new String[]{"View All Customers", "View Customer", "New Customer", "Back"}, scan);
            if (menu1 == 0){ // view all
                Customer.viewAllCustomers(conn, scan);
            } else if (menu1 == 1){ // view single
                Customer customer = Customer.findCustomer(conn, scan);
                if (customer != null)
                    customerUI(customer);
                else
                    System.out.println("No Customer found with matching ID. Use view all to see all customers.");
            } else if (menu1 == 2){ // new
                Customer.newCustomer(conn, scan);
            } else {
                break;
            }
        }
    }

    private void customerUI(Customer customer){
        while (true){
            // print customer content
            try {
                customer.printContents(conn);
            } catch (SQLException e) {
                CountyGarden.handleError(e);
                break;
            }
            int menu1 = CountyGarden.menu("-------------------", new String[]{"View Policy", "Add Policy", "Cancel Policy", "Back"}, scan);
            if (menu1 == 0){ // view policy
                Policy policy = Policy.findPolicy(conn, scan);
                if (policy != null && policy.getCustomerID() == customer.getCustomerID())
                    policyUI(policy);
                else
                    System.out.println("No Policy found with matching ID under customer.");
            } else if (menu1 == 1){ // add policy
                Policy.newPolicy(conn, scan, customer);
            } else if (menu1 == 2){ // remove policy
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
            if (!policy.isCanceled()){
                int menu1 = CountyGarden.menu("-------------------", new String[]{"Add Item", "Remove Item", "View Claim", "Make Claim", "Make Payment", "Back"}, scan);
                if (menu1 == 0){ // add item
                    Item.newItem(conn, scan, policy);
                } else if (menu1 == 1){ // remove item
                    Item.removeItem(conn, scan, policy);
                } else if (menu1 == 2){ // view claim
                    Claim claim = Claim.findClaim(conn, scan);
                    if (claim != null && claim.getPolicyID() == policy.getPolicyID())
                        claimUI(claim);
                    else
                        System.out.println("No Claim found with matching ID under policy.");
                } else if (menu1 == 3){ // make claim
                    Claim.newClaim(conn, scan, policy);
                } else if (menu1 == 4){ // make payment
                    Policy.newPayment(conn, scan, policy);
                } else {
                    break;
                }
            }
            else {
                int menu1 = CountyGarden.menu("-------------------", new String[]{"Back"}, scan);
                if (menu1 == 0){
                    break;
                }
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
