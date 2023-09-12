package src;
import java.sql.*;
import java.util.*;

public class AdjusterUI extends UserInterface{

    public AdjusterUI(Scanner scanner, Connection conn) {
        super(scanner, conn);
    }

    @Override
    public void start() {
        while (true) {
            int menu1 = CountyGarden.menu("--- County Garden Insurance Adjuster Interface ---", new String[]{"View All Adjusters", "View Adjuster", "Back"}, scan);
            if (menu1 == 0){ // view all
                Adjuster.viewAllAdjusters(conn, scan);
            } else if (menu1 == 1){ // view single
                Adjuster adjuster = Adjuster.findAdjuster(conn, scan);
                if (adjuster != null)
                    adjusterUI(adjuster);
                else
                    System.out.println("No adjuster found with matching ID. Use view all adjusters to find adjusters.");
            } else {
                break;
            }
        }
    }

    private void adjusterUI(Adjuster adjuster) {
        while (true) {
            try {
                adjuster.printContents(conn);
            } catch (SQLException e){
                CountyGarden.handleError(e);
                break;
            }
            int menu1 = CountyGarden.menu("-------------------", new String[]{"View Claim", "Back"}, scan);
            if (menu1 == 0){ // view claim
                Claim claim = Claim.findClaim(conn, scan);
                if (claim != null && claim.getAdjusterID() == adjuster.getID())
                    claimUI(claim);
                else
                    System.out.println("No Claim found with matching ID under agent.");
            } else { // back
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
            int menu1 = CountyGarden.menu("-------------------", new String[]{"Submit Payment", "Outsource Payment", "Back"}, scan);
            if (menu1 == 0){
                Claim.newPayment(conn, scan, claim);
            } else if (menu1 == 0){
                Claim.newOutsourcePayment(conn, scan, claim);
            } else {
                break;
            }
        }
    }
    
}
