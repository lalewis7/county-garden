package src;
import java.sql.*;
import java.util.*;

public class Adjuster implements TableItem{

    private int adj_id;
    private String name;

    public static void viewAllAdjusters(Connection conn, Scanner scan){
        try{
            Adjuster[] adjusters = Adjuster.getAll(conn);
            System.out.printf("%-6s%-32s\n", "ID", "Name");
            for (int i = 0; i < adjusters.length; i++){
                System.out.printf("%-6d%-32s\n", adjusters[i].getID(), adjusters[i].getName());
            }
        } catch (SQLException e){
            CountyGarden.handleError(e);
        }
    }

    public static Adjuster findAdjuster(Connection conn, Scanner scan){
        int id = CountyGarden.number("Adjuster ID", scan);
        return findAdjuster(conn, id);
    }

    public static Adjuster findAdjuster(Connection conn, int id){
        try {
            PreparedStatement pState = conn.prepareStatement("select * from adjusters where adj_id = ?");
            pState.setInt(1, id);
            ResultSet res = pState.executeQuery();
            if (res.next()){
                Adjuster adj = new Adjuster(res);
                res.close();
                pState.close();
                return adj;
            }
            res.close();
            pState.close();
        } catch (SQLException e) {
            CountyGarden.handleError(e);
        }
        return null;
    }

    public Adjuster() {}

    public Adjuster(ResultSet res) throws SQLException {
        getContents(res);
    }

    @Override
    public void getContents(Scanner scan) {
        name = CountyGarden.string("Name", scan, 3, 128);
    }

    @Override
    public void getContents(ResultSet res) throws SQLException {
        adj_id = res.getInt("adj_id");
        name = res.getString("name");
    }

    @Override
    public void printContents(Connection conn) throws SQLException{
        System.out.println("--- Agent "+adj_id+" ---");
        System.out.printf("%-16s %d\n", "ID", adj_id);
        System.out.printf("%-16s %s\n", "Name", name);

        System.out.println("- Claims -");

        PreparedStatement pState = conn.prepareStatement("select * from claims where adj_id = ?");
        pState.setInt(1, adj_id);

        ResultSet res = pState.executeQuery();

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
        PreparedStatement pState = conn.prepareStatement("select * from adjusters order by adj_id desc");
        ResultSet res = pState.executeQuery();
        if (res.next())
            adj_id = res.getInt("adj_id")+1;
        res.close();
        pState.close();
    }

    public static Adjuster[] getAll(Connection conn) throws SQLException{
        PreparedStatement pState = conn.prepareStatement("select * from adjusters");
        ResultSet res = pState.executeQuery();
        LinkedList<Adjuster> adjList = new LinkedList<Adjuster>();
        while (res.next()) {
            adjList.add(new Adjuster(res));
        }
        res.close();
        pState.close();
        return adjList.toArray(new Adjuster[adjList.size()]);
    }

    public void setID(int adj_id) {
        this.adj_id = adj_id;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getID(){
        return adj_id;
    }

    public String getName(){
        return name;
    }
    
}
